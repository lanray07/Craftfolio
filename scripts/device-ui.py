"""Small adb UI test driver. Selects visible accessibility nodes by exact label."""
import argparse
import re
import subprocess
import time
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
sys.stdout.reconfigure(encoding='utf-8', errors='replace')
p=argparse.ArgumentParser()
p.add_argument('--adb', required=True)
p.add_argument('--serial',required=True)
p.add_argument('--tap')
p.add_argument('--type')
p.add_argument('--list',action='store_true')
p.add_argument('--capture')
p.add_argument('--redact-identifiers', action='store_true', help='Hide email and long identifier tokens in accessibility output')
a=p.parse_args(); base=[a.adb,'-s',a.serial]
def adb(*cmd): return subprocess.run(base+list(cmd),capture_output=True,check=True).stdout.decode(errors='replace')
if a.tap or a.list:
    adb('shell','input','keyevent','224')
    dump = ''
    for attempt in range(8):
        time.sleep(.4)
        dump = adb('shell','uiautomator','dump','/sdcard/craftfolio-ui.xml')
        if 'dumped to:' in dump: break
    if 'dumped to:' not in dump: raise SystemExit('No current accessibility tree; use a fresh screenshot instead of stale XML')
    root=ET.fromstring(adb('shell','cat','/sdcard/craftfolio-ui.xml'))
    if a.list:
        for n in root.iter('node'):
            label=n.attrib.get('text') or n.attrib.get('content-desc')
            if label:
                if a.redact_identifiers:
                    label=re.sub(r'[\w.+-]+@[\w.-]+\.[A-Za-z]+','<REDACTED EMAIL>',label)
                    label=re.sub(r'[A-Za-z0-9_+=/-]{28,}','<REDACTED ID>',label)
                print(n.attrib.get('class'),repr(label),n.attrib.get('bounds'))
    if a.tap:
        n=next((n for n in root.iter('node') if a.tap in [n.attrib.get('text'),n.attrib.get('content-desc')]),None)
        if n is None: raise SystemExit(f'Label not visible: {a.tap}')
        x1,y1,x2,y2=map(int,re.findall(r'\d+',n.attrib['bounds']))
        adb('shell','input','tap',str((x1+x2)//2),str((y1+y2)//2))
        time.sleep(1)
if a.type:
    if not re.fullmatch(r'[A-Za-z0-9 ._-]+',a.type): raise SystemExit('Use simple fixture text without shell metacharacters')
    adb('shell','input','text',a.type.replace(' ','%s'))
if a.capture:
    path=Path(a.capture); path.parent.mkdir(parents=True,exist_ok=True)
    with path.open('wb') as stream: subprocess.run(base+['exec-out','screencap','-p'],stdout=stream,check=True)
