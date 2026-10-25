"""Check complete string coverage, formatting contracts and local store-copy limits."""
import json
import re
from pathlib import Path
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
resources = root / 'app/src/main/res'
def strings(folder):
    elements = ET.parse(resources / folder / 'strings.xml').getroot().findall('string')
    result = {e.attrib['name']: ''.join(e.itertext()) for e in elements}
    assert len(result) == len(elements), f'Duplicate keys in {folder}'
    return result

base = strings('values')
tokens = lambda value: sorted(re.findall(r'%\d+\$[sd]', value))
locales = json.loads((root / 'store-assets/locales.json').read_text(encoding='utf-8'))
registry = (root / 'app/src/main/java/com/craftfolio/patterns/AppLanguages.kt').read_text(encoding='utf-8')
registered = re.findall(r'"([a-z]{2}-[A-Z]{2})"', re.search(r'val supported = listOf\((.*?)\)', registry).group(1))
assert set(registered) == {item['tag'] for item in locales}, 'App selector and locale matrix differ'
for folder in [locale['resource'] for locale in locales]:
    translated = strings(folder)
    assert translated.keys() == base.keys(), (folder, base.keys() - translated.keys(), translated.keys() - base.keys())
    for key, value in translated.items():
        assert value.strip(), (folder, key, 'Empty translation')
        assert tokens(value) == tokens(base[key]), (folder, key, 'Format placeholders differ')
    print(f'{folder}: {len(translated)} strings; coverage and placeholders PASS')

for locale in [item['tag'] for item in locales]:
    data = json.loads((root / 'store-assets' / locale / 'metadata/listing.json').read_text(encoding='utf-8'))
    assert len(data['title']) <= 200
    assert len(data['short_description'].encode('utf-8')) <= 1200
    assert len(data['long_description']) <= 4000
    assert 3 <= len(data['feature_bullets']) <= 10
    assert all(data['feature_bullets'])
    assert len(data.get('premium_description','')) <= 1200
    print(f'{locale}: store-copy lengths PASS')
