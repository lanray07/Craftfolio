"""Capture real UI through Compose assertions and Android UiAutomation pixels.
Build/install app and androidTest APKs first. The app language is selected through its real UI.
"""
import argparse
import hashlib
import json
import subprocess
from pathlib import Path
from PIL import Image

p=argparse.ArgumentParser()
p.add_argument('--adb',required=True)
p.add_argument('--serial',required=True)
p.add_argument('--locale',choices=[item['tag'] for item in json.loads((Path(__file__).resolve().parents[1]/'store-assets/locales.json').read_text(encoding='utf-8'))],default='en-GB')
a=p.parse_args(); base=[a.adb,'-s',a.serial]
def adb(*cmd): return subprocess.run(base+list(cmd),capture_output=True,check=True).stdout
adb('shell','input','keyevent','224')
adb('shell','wm','dismiss-keyguard')
adb('shell','am','force-stop','com.craftfolio.patterns')
result=adb('shell','am','instrument','-w','-e','class','com.craftfolio.patterns.StoreScreenshotCapture','-e','storeCapture','true','-e','storeLocale',a.locale,'com.craftfolio.patterns.test/androidx.test.runner.AndroidJUnitRunner').decode(errors='replace')
print(result)
if 'OK (1 test)' not in result: raise SystemExit('Native screen assertions failed; no assets accepted')
root=Path(__file__).resolve().parents[1]/'store-assets'/a.locale
out=root/'screenshots'; out.mkdir(parents=True,exist_ok=True)
scenes=[('01-pattern-library','Save and organise PDF patterns' if a.locale=='en-GB' else 'Save and organize PDF patterns','Search your private craft pattern library'),('02-knitting-row-counter','Never lose your row count','Read a pattern and count rows with undo'),('03-split-view-workspace','Patterns and counters side by side','A comfortable workspace for your Fire tablet'),('04-project-tracker','Remember every craft project','Resume saved counters, notes and requirements')]
metadata=json.loads((root/'metadata/listing.json').read_text(encoding='utf-8'))
if 'screenshot_captions' in metadata:
    scenes=[(scene[0],caption,caption) for scene,caption in zip(scenes,metadata['screenshot_captions'])]
manifest=[]
for name,headline,benefit in scenes:
    path=out/f'{name}.png'
    path.write_bytes(adb('exec-out','run-as','com.craftfolio.patterns','cat',f'files/store-capture/{a.locale}/{name}.png'))
    with Image.open(path) as image:
        size=image.size
        assert sorted(size)==[1200,1920] and image.format=='PNG'
    manifest.append(dict(file=path.name,dimensions=size,sha256=hashlib.sha256(path.read_bytes()).hexdigest(),headline=headline,benefit=benefit,origin='Android UiAutomation pixels after Compose screen and foreground-window assertions'))
(root/'screenshot-manifest.json').write_text(json.dumps(dict(locale=a.locale,ui_language=a.locale,locale_control='Real in-app language selector; previous preference restored after capture',sample_content='Original English CC0 sample documents and user-entered project text are not automatically translated',model=adb('shell','getprop','ro.product.model').decode().strip(),api=adb('shell','getprop','ro.build.version.sdk').decode().strip(),screenshots=manifest,review='Native assertions passed; visual review required. Captions are separate copy, not image overlays.'),indent=2),encoding='utf-8')
print(f'Captured and validated {len(manifest)} real app images for {a.locale}')
