import requests, os

r = requests.post('http://localhost:8080/api/auth/signin', json={'username':'admin','password':'adminPass'})
token = r.json()['jwtToken']
auth_headers = {'Authorization': 'Bearer ' + token, 'Content-Type': 'application/json'}
auth_upload = {'Authorization': 'Bearer ' + token}

# Delete products 4-8 (MacBook, Dell, ASUS, Sony, Bose) and re-create under correct categories
# First get their current image names before deleting
r2 = requests.get('http://localhost:8080/api/public/products?pageSize=50')
all_products = r2.json().get('content', [])
img_map = {p['productId']: p['image'] for p in all_products}
print('Current images:', img_map)

# Products to delete and recreate in correct categories
# 4=MacBook->Laptops(2), 5=Dell->Laptops(2), 6=ASUS->Laptops(2), 7=Sony->Audio(3), 8=Bose->Audio(3)
to_fix = [
    {
        'delete_id': 4, 'cat': 2,
        'name': 'MacBook Air M4', 'price': 1299.0, 'discount': 0.0, 'qty': 40,
        'desc': 'Super thin and light MacBook Air with the powerful new M4 chip, all-day battery life, and gorgeous Liquid Retina display for ultimate productivity.',
        'img_file': 'macbook_img.jpg',
        'img_url': 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&q=80'
    },
    {
        'delete_id': 5, 'cat': 2,
        'name': 'Dell XPS 15', 'price': 1499.0, 'discount': 0.0, 'qty': 35,
        'desc': 'Premium Windows laptop with 4K OLED InfinityEdge display and Intel Core Ultra processor for power users.',
        'img_file': 'dell_img.jpg',
        'img_url': 'https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=600&q=80'
    },
    {
        'delete_id': 6, 'cat': 2,
        'name': 'ASUS ROG Strix', 'price': 1799.0, 'discount': 0.0, 'qty': 20,
        'desc': 'High-performance gaming laptop with RTX 4080, 240Hz display, and per-key RGB keyboard.',
        'img_file': 'asus_img.jpg',
        'img_url': 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=600&q=80'
    },
    {
        'delete_id': 7, 'cat': 3,
        'name': 'Sony WH-1000XM5', 'price': 399.0, 'discount': 0.0, 'qty': 120,
        'desc': 'Industry-leading noise canceling wireless headphones with crystal clear audio and 30-hour battery.',
        'img_file': 'sony_img.jpg',
        'img_url': 'https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=600&q=80'
    },
    {
        'delete_id': 8, 'cat': 3,
        'name': 'Bose QuietComfort Ultra', 'price': 429.0, 'discount': 0.0, 'qty': 90,
        'desc': 'Premium comfort and immersive spatial audio from Bose top-tier noise canceling headphones.',
        'img_file': 'bose_img.jpg',
        'img_url': 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600&q=80'
    },
]

# Download images first
for item in to_fix:
    img_data = requests.get(item['img_url'], timeout=15).content
    with open(item['img_file'], 'wb') as f:
        f.write(img_data)
    print(f'Downloaded image for {item["name"]}')

# Delete old products
for item in to_fix:
    del_res = requests.delete(f'http://localhost:8080/api/admin/products/{item["delete_id"]}', headers=auth_upload)
    print(f'Deleted product {item["delete_id"]}: {del_res.status_code}')

# Re-create under correct categories
for item in to_fix:
    cat_id = item['cat']
    payload = {
        'productName': item['name'],
        'description': item['desc'],
        'quantity': item['qty'],
        'price': item['price'],
        'discount': item['discount'],
        'specialPrice': round(item['price'] * (1 - item['discount']/100), 2)
    }
    res = requests.post(f'http://localhost:8080/api/admin/categories/{cat_id}/product', headers=auth_headers, json=payload)
    new_id = res.json().get('productId')
    print(f'Created {item["name"]} -> product id={new_id} under category {cat_id}: {res.status_code}')

    # Upload image
    with open(item['img_file'], 'rb') as f:
        files = {'image': (item['img_file'], f, 'image/jpeg')}
        img_res = requests.put(f'http://localhost:8080/api/admin/products/{new_id}/image', headers=auth_upload, files=files)
        print(f'  Image upload: {img_res.status_code}')

    os.remove(item['img_file'])
