import requests

for cat in ['Smartphones','Laptops','Audio','Wearables','Tablets','Speakers']:
    r = requests.get(f'http://localhost:8080/api/public/products?category={cat}')
    items = r.json().get('content', [])
    names = [p['productName'] for p in items]
    print(f'{cat} ({len(items)} products): {names}')
