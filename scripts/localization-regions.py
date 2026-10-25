"""Prepare explicit regional store copy; never translate personal content."""
import copy
import json
from pathlib import Path
root = Path(__file__).resolve().parents[1] / 'store-assets'
def read(tag): return json.loads((root/tag/'metadata/listing.json').read_text(encoding='utf-8'))
def save(tag, data):
    path = root/tag/'metadata/listing.json'
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

language_paragraphs = {
    'en-GB': ('Choose British English', 'Choose your language directly in the app. Imported patterns and personal project text are not translated automatically.'),
    'en-US': ('Choose US English', 'Choose your language directly in the app. Imported patterns and personal project text are not translated automatically.'),
    'de-DE': ('Die App unterstützt', 'Wähle deine Sprache direkt in der App. Importierte Anleitungen und persönliche Texte werden nicht automatisch übersetzt.'),
    'fr-FR': ('L’application propose', 'Choisissez la langue dans l’application. Les modèles importés et les textes personnels ne sont pas traduits automatiquement.'),
    'es-ES': ('La aplicación está disponible', 'Elige el idioma desde la aplicación. Los patrones importados y los textos personales no se traducen automáticamente.')
}
for tag,(start,replacement) in language_paragraphs.items():
    data=read(tag)
    data['long_description']='\n\n'.join(replacement if paragraph.startswith(start) else paragraph for paragraph in data['long_description'].split('\n\n'))
    if tag.startswith('en-'):
        data['premium_title']='Craftfolio Premium'
        data['premium_description']='More room for your knitting and crochet projects: unlimited pattern imports and projects, plus multiple named row counters per project, within available device storage. Monthly and annual plans include the same Premium features. Imported files and existing project data stay accessible when the subscription ends. Patterns are not included. Payment and automatic renewal follow the selected Amazon subscription terms. Verified Premium requires reconnection at least every 72 hours.'
        data['screenshot_captions']=['Your personal PDF pattern library','Never lose your row count','Patterns and counters side by side','Resume every creative project']
    save(tag,data)

for tag in ['en-AU','en-IN']: save(tag,copy.deepcopy(read('en-GB')))
mx=read('es-ES')
def transform(data, replacements):
    if isinstance(data,str):
        for before,after in replacements: data=data.replace(before,after)
        return data
    if isinstance(data,list): return [transform(x,replacements) for x in data]
    if isinstance(data,dict): return {k:transform(v,replacements) for k,v in data.items()}
    return data
mx=transform(mx,[('ganchillo','crochet'),('punto','tejido con agujas'),('tablets','tabletas')])
save('es-MX',mx)
pt=transform(read('pt-BR'), [('arquivos','ficheiros'),('Arquivos','Ficheiros'),('arquivo','ficheiro'),('tricô','tricot'),('crochê','croché'),('telas','ecrãs'),('aplicativo','aplicação'),('assinatura','subscrição'),('salva','guardada'),('Busque','Pesquise'),('busque','pesquise'),('busca','pesquisa')])
pt['short_description']='Organize receitas PDF, projetos de tricot e croché, contadores de carreiras, materiais e notas num espaço offline.'
pt['long_description']=pt['long_description'].replace('busque pelo título','pesquise pelo título').replace('A Amazon gerencia','A Amazon gere')
pt['screenshot_captions'][0]='A sua biblioteca de receitas PDF'
save('pt-PT',pt)
