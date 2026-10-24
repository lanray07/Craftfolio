import json
from pathlib import Path
from PIL import Image
root = Path(__file__).resolve().parents[1] / 'store-assets'
report = []
for locale in ['en-GB','en-US']:
    data = json.loads((root/locale/'metadata/listing.json').read_text(encoding='utf-8'))
    assert len(data['long_description']) <= 4000
    assert len(data['short_description'].encode('utf-8')) <= 2000
    assert 3 <= len(data['feature_bullets']) <= 5
    files = list((root/locale/'screenshots').glob('*.png'))
    errors = []
    if not 3 <= len(files) <= 10: errors.append('Need 3–10 screenshots')
    for file in files:
        with Image.open(file) as img:
            if sorted(img.size) not in [[480,800],[600,1024],[720,1280],[800,1280],[1080,1920],[1200,1920],[1600,2560]]:
                errors.append(f'Invalid dimensions: {file.name}')
            if img.format != 'PNG': errors.append(f'Wrong format: {file.name}')
    report.append(dict(locale=locale,screenshot_count=len(files),metadata_lengths='pass',errors=errors,
                       remaining='Human review, exact signed-release capture and developer-console submission validation'))
(root/'validation.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report,indent=2))
if any(row['errors'] for row in report): raise SystemExit(1)
