"""Materialise reviewed-in-source translation data as Android resources.

This is a local authoring helper, not an automatic translation service.
"""
import json
from pathlib import Path
import xml.etree.ElementTree as ET
from xml.sax.saxutils import escape

root = Path(__file__).resolve().parents[1]
res = root / 'app/src/main/res'
base = {e.attrib['name']: e.text for e in ET.parse(res/'values/strings.xml').getroot()}
translations = root / 'scripts/localization'

def write(folder, data):
    assert data.keys() == base.keys(), (folder, base.keys()-data.keys(), data.keys()-base.keys())
    target = res / folder / 'strings.xml'
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text('<resources>\n' + ''.join(
        f'    <string name="{key}">{escape(value).replace(chr(39), chr(92)+chr(39))}</string>\n'
        for key, value in data.items()) + '</resources>\n', encoding='utf-8')

for locale in json.loads((root/'store-assets/locales.json').read_text()):
    source = translations / (locale['tag']+'.json')
    if source.exists():
        write(locale['resource'], json.loads(source.read_text(encoding='utf-8')))

for target, source in [('values-en-rAU','values-en-rGB'),('values-en-rIN','values-en-rGB'),('values-es-rMX','values-es')]:
    data = {e.attrib['name']: e.text for e in ET.parse(res/source/'strings.xml').getroot()}
    # Android resource apostrophes have already been escaped in the source.
    data = {k:v.replace("\\'", "'") for k,v in data.items()}
    if target == 'values-es-rMX':
        for key in data: data[key] = data[key].replace('ganchillo','crochet')
        data['knitting'] = 'Tejido con agujas'
        data['crochet'] = 'Crochet'
    write(target, data)

br = json.loads((translations/'pt-BR.json').read_text(encoding='utf-8'))
pt = {k:v.replace('arquivo','ficheiro').replace('Arquivo','Ficheiro').replace('tela','ecrã').replace('assinatura','subscrição').replace('Assinatura','Subscrição').replace('Tricô','Tricot').replace('Crochê','Croché').replace('tricô','tricot').replace('crochê','croché').replace('salvar','guardar').replace('salva','guardada').replace('registrado','registado').replace('aplicativo','aplicação').replace('Buscar','Pesquisar') for k,v in br.items()}
pt.update(library='Receitas', free='Grátis', rows='Carreiras', save='Guardar',
          language_system='Usar idioma do tablet', premium_monthly='Premium · Mensal', premium_annual='Premium · Anual')
write('values-pt-rPT',pt)
