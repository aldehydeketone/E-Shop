import requests, json

r = requests.post('http://localhost:8080/api/auth/signin', json={'username':'admin','password':'adminPass'})
token = r.json()['jwtToken']
headers = {'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json'}

new_products = [
    {'name': 'Apple Watch Series 10', 'price': 499.0, 'qty': 75, 'cat': 4, 'discount': 5.0, 'desc': 'Apple Watch Series 10 with thinner design, larger display, faster charging, and advanced health sensors including blood oxygen and ECG monitoring.'},
    {'name': 'Apple AirPods Pro 2', 'price': 249.0, 'qty': 150, 'cat': 3, 'discount': 0.0, 'desc': 'AirPods Pro 2nd generation with Active Noise Cancellation, Transparency mode, Adaptive EQ, and up to 30 hours total battery with the charging case.'},
    {'name': 'JBL Charge 5 Speaker', 'price': 179.0, 'qty': 100, 'cat': 6, 'discount': 10.0, 'desc': 'JBL Charge 5 portable Bluetooth speaker with powerful sound, deep bass, IP67 waterproof and dustproof rating, and 20 hours of playtime.'},
    {'name': 'Apple iPad Air M2', 'price': 749.0, 'qty': 50, 'cat': 5, 'discount': 0.0, 'desc': 'Apple iPad Air with M2 chip, 11-inch Liquid Retina display, USB-C connectivity, Apple Pencil support, and all-day battery life for creativity and productivity.'},
]

for p in new_products:
    special = round(p['price'] * (1 - p['discount']/100), 2)
    payload = {
        'productName': p['name'],
        'description': p['desc'],
        'quantity': p['qty'],
        'price': p['price'],
        'discount': p['discount'],
        'specialPrice': special
    }
    cat_id = p['cat']
    res = requests.post(f'http://localhost:8080/api/admin/categories/{cat_id}/product', headers=headers, json=payload)
    print(f'Added {p["name"]}: {res.status_code} -> id={res.json().get("productId","?")}')
