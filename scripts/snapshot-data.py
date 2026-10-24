"""Read-only debug-build persistence evidence. Stop the app before copying SQLite/WAL files.
Example: python scripts/snapshot-data.py --adb C:/.../adb.exe --serial SERIAL --output test-evidence/after-force-stop.json
"""
import argparse
import hashlib
import json
import sqlite3
import subprocess
import tempfile
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument('--adb', required=True)
parser.add_argument('--serial', required=True)
parser.add_argument('--output', required=True)
args = parser.parse_args()
prefix = [args.adb, '-s', args.serial]
def read(*command):
    return subprocess.run(prefix + list(command), capture_output=True, check=True).stdout

running = subprocess.run(prefix + ['shell','pidof','com.craftfolio.patterns'], capture_output=True).stdout.strip()
if running: raise SystemExit('Stop the app first: adb shell am force-stop com.craftfolio.patterns')

with tempfile.TemporaryDirectory(prefix='craftfolio-evidence-') as folder:
    for suffix in ['', '-wal', '-shm']:
        result = subprocess.run(prefix + ['exec-out','run-as','com.craftfolio.patterns','cat',f'databases/craftfolio.db{suffix}'], capture_output=True)
        if result.returncode == 0: (Path(folder)/f'craftfolio.db{suffix}').write_bytes(result.stdout)
        elif suffix == '': raise RuntimeError('Cannot access debug database')
    con = sqlite3.connect(str(Path(folder)/'craftfolio.db'))
    con.row_factory = sqlite3.Row
    state = {table: [dict(row) for row in con.execute(f'SELECT * FROM {table} ORDER BY id')] for table in ['Pattern','Project','Counter','CounterChange','Note']}
    for project in state['Project']: project.pop('touched', None)
    for pattern in state['Pattern']:
        pattern['sha256'] = hashlib.sha256(read('exec-out','run-as','com.craftfolio.patterns','cat',pattern['path'])).hexdigest()
    con.close()
output = Path(args.output); output.parent.mkdir(parents=True,exist_ok=True)
output.write_text(json.dumps(dict(model=read('shell','getprop','ro.product.model').decode().strip(), boot_id=read('shell','cat','/proc/sys/kernel/random/boot_id').decode().strip(), wifi=read('shell','settings','get','global','wifi_on').decode().strip(), state=state),indent=2),encoding='utf-8')
print(f'Saved {len(state["Pattern"])} file hashes and project state to {output}')
