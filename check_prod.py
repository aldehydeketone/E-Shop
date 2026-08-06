import requests

BASE = "https://sb-ecom-latest-qurh.onrender.com"

print("Testing production backend ...")
try:
    r = requests.post(BASE + "/api/auth/signin",
                      json={"username": "admin", "password": "adminPass"},
                      timeout=35)
    print("Login status:", r.status_code)
    if r.status_code == 200:
        token = r.json().get("jwtToken", "")
        print("Token OK:", token[:25] + "...")
        cats  = requests.get(BASE + "/api/public/categories?pageSize=100", timeout=15)
        prods = requests.get(BASE + "/api/public/products?pageSize=10",    timeout=15)
        print("Categories:", cats.status_code, "count:", len(cats.json().get("content", [])))
        print("Products:",   prods.status_code, "total:", prods.json().get("totalElements", 0))

        print("\nTesting Contact Message...")
        contact_res = requests.post(BASE + "/api/public/contact-messages", json={"name":"test", "email":"test@test.com", "message":"hello"}, timeout=15)
        print("Contact endpoint:", contact_res.status_code)
        
        print("\nTesting Cart Update...")
        headers = {"Authorization": f"Bearer {token}"}
        # First add something to cart to ensure it exists
        # Actually cart endpoints in ecom-frontend say: await api.post('/cart/create', sendCartItems);
        # Let's just try to get the cart
        cart_res = requests.get(BASE + "/api/carts/users/cart", headers=headers)
        print("Get Cart:", cart_res.status_code)
        if cart_res.status_code == 200:
            print("Cart update (fake product 999 to see if it reaches logic without 500 mapping error):")
            update_res = requests.put(BASE + "/api/cart/products/999/quantity/add", headers=headers)
            print("Update Cart (999):", update_res.status_code, update_res.text[:100])
    else:
        print("Login response:", r.text[:300])
except Exception as e:
    print("Error:", e)
