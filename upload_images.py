import requests, os

r = requests.post('http://localhost:8080/api/auth/signin', json={'username':'admin','password':'adminPass'})
token = r.json()['jwtToken']
auth_headers = {'Authorization': 'Bearer ' + token}

# Unsplash images for new products
image_urls = {
    9: 'https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=600&q=80',   # Apple Watch
    10: 'https://images.unsplash.com/photo-1603351154351-5e2d0600bb77?w=600&q=80', # AirPods
    11: 'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=600&q=80', # JBL Speaker
    12: 'https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600&q=80',   # iPad
}

for pid, url in image_urls.items():
    img_data = requests.get(url, timeout=15).content
    fname = f'temp_{pid}.jpg'
    with open(fname, 'wb') as f:
        f.write(img_data)
    with open(fname, 'rb') as f:
        files = {'image': (fname, f, 'image/jpeg')}
        res = requests.put(f'http://localhost:8080/api/admin/products/{pid}/image', headers=auth_headers, files=files)
        print(f'Uploaded image for product {pid}: {res.status_code}')
    os.remove(fname)
