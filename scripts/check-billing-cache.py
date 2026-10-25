"""Assert debug-app billing cache state without printing account or receipt identifiers."""
import argparse
import json
import subprocess
import time
import xml.etree.ElementTree as ET
from pathlib import Path

p=argparse.ArgumentParser()
p.add_argument('--adb',required=True)
p.add_argument('--serial',required=True)
p.add_argument('--expect',choices=['premium','free'],required=True)
p.add_argument('--output',required=True)
p.add_argument('--environment',help='Require the cached verifier identity to match this endpoint')
a=p.parse_args()
result=subprocess.run([a.adb,'-s',a.serial,'exec-out','run-as','com.craftfolio.patterns','cat','shared_prefs/verified-entitlement.xml'],capture_output=True,check=True)
root=ET.fromstring(result.stdout)
values={e.attrib['name']:e.text if e.tag=='string' else e.attrib.get('value') for e in root}
leases=json.loads(values.get('leases','{}'))
if 'leases' not in values and values.get('receipt'):
    leases={'legacy':{'checked':int(values.get('checked',0)),'until':int(values.get('until',0))}}
now=int(time.time()*1000)
active=[x for x in leases.values() if 0<x['checked']<=now<x['until'] and now-x['checked']<72*3600000]
summary={'accountStored':bool(values.get('user')),'verifiedReceiptCount':len(leases),'activeLeaseCount':len(active),
         'premium':bool(values.get('user')) and bool(active),'checkedAt':now,
         'leases':[{'checked':x['checked'],'validUntil':x['until']} for x in leases.values()]}
summary['verifier']=values.get('environment')
if a.environment and summary['verifier'] != a.environment: summary['premium']=False
summary['expected']=a.expect
summary['pass']=summary['premium']==(a.expect=='premium')
path=Path(a.output); path.parent.mkdir(parents=True,exist_ok=True)
path.write_text(json.dumps(summary,indent=2),encoding='utf-8')
print(json.dumps(summary))
if not summary['pass']: raise SystemExit(1)
