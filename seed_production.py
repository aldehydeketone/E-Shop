"""
seed_production.py
==================
E-Shop Production Database Seeder -- SAFE & IDEMPOTENT

Production URL : https://sb-ecom-latest-qurh.onrender.com
Catalog        : ~9 products across 5 categories

SAFETY GUARANTEES
-----------------
  * Categories : GET first -> create only if name does not exist.
  * Products   : GET all names -> insert only if name not already present.
  * Images     : Download from Unsplash -> PUT upload per product after create.
  * NO DELETE  : This script never deletes, truncates, or overwrites data.
  * IDEMPOTENT : Running multiple times produces zero duplicates.

USAGE
-----
  python seed_production.py

REQUIREMENTS
------------
  pip install requests
"""

import os
import sys
import time
import requests

# ── Configuration ──────────────────────────────────────────────────────────────
BASE_URL        = "https://sb-ecom-latest-qurh.onrender.com"
ADMIN_USER      = "admin"
ADMIN_PASS      = "adminPass"
REQUEST_TIMEOUT = 30   # seconds per HTTP call
IMAGE_TIMEOUT   = 25   # seconds for image downloads
DELAY_BETWEEN   = 0.4  # seconds between product inserts
# ──────────────────────────────────────────────────────────────────────────────

API = BASE_URL.rstrip("/")

# ── CATALOG ────────────────────────────────────────────────────────────────────
CATALOG = [
    # ── Laptops ──────────────────────────────────────────────────────────────
    {
        "name":      "MacBook Air M4",
        "category":  "Laptops",
        "price":     1299.00,
        "discount":  0.0,
        "qty":       40,
        "desc":      "Super thin and light MacBook Air with the powerful new M4 chip, all-day battery life, and gorgeous Liquid Retina display for ultimate productivity.",
        "image_url": "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&q=80",
    },
    {
        "name":      "Dell XPS 15",
        "category":  "Laptops",
        "price":     1499.00,
        "discount":  5.0,
        "qty":       35,
        "desc":      "Premium Windows laptop with 4K OLED InfinityEdge display and Intel Core Ultra processor for power users.",
        "image_url": "https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=600&q=80",
    },
    {
        "name":      "ASUS ROG Strix",
        "category":  "Laptops",
        "price":     1799.00,
        "discount":  0.0,
        "qty":       20,
        "desc":      "High-performance gaming laptop with RTX 4080, 240Hz display, and per-key RGB keyboard.",
        "image_url": "https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=600&q=80",
    },

    # ── Audio ─────────────────────────────────────────────────────────────────
    {
        "name":      "Sony WH-1000XM5",
        "category":  "Audio",
        "price":     399.00,
        "discount":  10.0,
        "qty":       120,
        "desc":      "Industry-leading noise canceling wireless headphones with crystal clear audio and 30-hour battery.",
        "image_url": "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=600&q=80",
    },
    {
        "name":      "Bose QuietComfort Ultra",
        "category":  "Audio",
        "price":     429.00,
        "discount":  0.0,
        "qty":       90,
        "desc":      "Premium comfort and immersive spatial audio from Bose top-tier noise canceling headphones.",
        "image_url": "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600&q=80",
    },
    {
        "name":      "Apple AirPods Pro 2",
        "category":  "Audio",
        "price":     249.00,
        "discount":  0.0,
        "qty":       150,
        "desc":      "AirPods Pro 2nd generation with Active Noise Cancellation, Transparency mode, Adaptive EQ, and up to 30 hours total battery with the charging case.",
        "image_url": "https://images.unsplash.com/photo-1603351154351-5e2d0600bb77?w=600&q=80",
    },

    # ── Wearables ─────────────────────────────────────────────────────────────
    {
        "name":      "Apple Watch Series 10",
        "category":  "Wearables",
        "price":     499.00,
        "discount":  5.0,
        "qty":       75,
        "desc":      "Apple Watch Series 10 with thinner design, larger display, faster charging, and advanced health sensors including blood oxygen and ECG monitoring.",
        "image_url": "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=600&q=80",
    },

    # ── Speakers ──────────────────────────────────────────────────────────────
    {
        "name":      "JBL Charge 5 Speaker",
        "category":  "Speakers",
        "price":     179.00,
        "discount":  10.0,
        "qty":       100,
        "desc":      "JBL Charge 5 portable Bluetooth speaker with powerful sound, deep bass, IP67 waterproof and dustproof rating, and 20 hours of playtime.",
        "image_url": "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=600&q=80",
    },

    # ── Tablets ───────────────────────────────────────────────────────────────
    {
        "name":      "Apple iPad Air M2",
        "category":  "Tablets",
        "price":     749.00,
        "discount":  0.0,
        "qty":       50,
        "desc":      "Apple iPad Air with M2 chip, 11-inch Liquid Retina display, USB-C connectivity, Apple Pencil support, and all-day battery life for creativity and productivity.",
        "image_url": "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600&q=80",
    },
]

REQUIRED_CATEGORIES = list(dict.fromkeys(p["category"] for p in CATALOG))


# ── Helpers ────────────────────────────────────────────────────────────────────

def login():
    print("[AUTH] Logging in to production backend ...")
    try:
        r = requests.post(
            f"{API}/api/auth/signin",
            json={"username": ADMIN_USER, "password": ADMIN_PASS},
            timeout=REQUEST_TIMEOUT,
        )
    except requests.exceptions.ConnectionError:
        print(f"[ERROR] Cannot reach {API}")
        sys.exit(1)

    if r.status_code != 200:
        print(f"[ERROR] Login failed ({r.status_code}): {r.text[:300]}")
        sys.exit(1)

    token = r.json().get("jwtToken")
    if not token:
        print("[ERROR] No JWT token in response.")
        sys.exit(1)

    print("[OK]   Authenticated\n")
    return token


def json_headers(token):
    return {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}


def bare_headers(token):
    return {"Authorization": f"Bearer {token}"}


def get_existing_categories(token):
    r = requests.get(f"{API}/api/public/categories?pageSize=100", timeout=REQUEST_TIMEOUT)
    cats = r.json().get("content", []) if r.ok else []
    return {c["categoryName"].lower(): c["categoryId"] for c in cats}


def ensure_categories(token):
    print("[CATS] Checking categories ...")
    existing = get_existing_categories(token)
    cat_map = {}
    cats_created = 0
    cats_skipped = 0

    for name in REQUIRED_CATEGORIES:
        key = name.lower()
        if key in existing:
            cat_map[name] = existing[key]
            print(f"       REUSE  '{name}'  (id={existing[key]})")
            cats_skipped += 1
        else:
            r = requests.post(
                f"{API}/api/admin/categories",
                headers=json_headers(token),
                json={"categoryName": name},
                timeout=REQUEST_TIMEOUT,
            )
            if r.status_code in (200, 201):
                new_id = r.json().get("categoryId")
                cat_map[name] = new_id
                print(f"       CREATE '{name}'  (id={new_id})")
                cats_created += 1
            else:
                print(f"       FAIL   '{name}': {r.status_code} -- {r.text[:200]}")

    print()
    return cat_map, cats_created, cats_skipped


def get_existing_product_names(token):
    r = requests.get(f"{API}/api/public/products?pageSize=500", timeout=REQUEST_TIMEOUT)
    products = r.json().get("content", []) if r.ok else []
    return {p["productName"].lower() for p in products}


def upload_image(token, product_id, image_url, isAdmin=True):
    try:
        img_resp = requests.get(image_url, timeout=IMAGE_TIMEOUT)
        img_resp.raise_for_status()
        img_bytes = img_resp.content
    except Exception as exc:
        print(f"         [WARN] Image download failed: {exc}")
        return False

    tmp_file = f"_tmp_seed_{product_id}.jpg"
    success = False
    try:
        with open(tmp_file, "wb") as fh:
            fh.write(img_bytes)
        with open(tmp_file, "rb") as fh:
            endpoint = "/admin/products/" if isAdmin else "/seller/products/"
            res = requests.put(
                f"{API}/api{endpoint}{product_id}/image",
                headers=bare_headers(token),
                files={"image": (f"product_{product_id}.jpg", fh, "image/jpeg")},
                timeout=REQUEST_TIMEOUT,
            )
        if res.status_code == 200:
            print(f"         [IMG]  Uploaded OK")
            success = True
        else:
            print(f"         [WARN] Image upload returned {res.status_code}: {res.text[:150]}")
    except Exception as exc:
        print(f"         [WARN] Image upload error: {exc}")
    finally:
        if os.path.exists(tmp_file):
            os.remove(tmp_file)

    return success


# ── Main ───────────────────────────────────────────────────────────────────────

def seed():
    print("=" * 60)
    print("  E-Shop Production Seeder")
    print(f"  Target  : {API}")
    print(f"  Catalog : {len(CATALOG)} products | {len(REQUIRED_CATEGORIES)} categories")
    print("=" * 60)
    print()

    token = login()
    cat_map, cats_created, cats_skipped = ensure_categories(token)
    existing = get_existing_product_names(token)

    print(f"[INFO] Products already in database : {len(existing)}")
    print(f"[INFO] Products in seeding catalog  : {len(CATALOG)}")
    print()

    prods_created = 0
    prods_skipped = 0
    imgs_success  = 0
    imgs_failed   = 0

    print("[SEED] Inserting products ...\n")
    for product in CATALOG:
        name     = product["name"]
        category = product["category"]
        cat_id   = cat_map.get(category)

        print(f"  [{category}]  {name}")

        if name.lower() in existing:
            print("       SKIP   Already exists -- not modified")
            prods_skipped += 1
            continue

        if not cat_id:
            print(f"       FAIL   No category id for '{category}'")
            continue

        special = round(product["price"] * (1 - product["discount"] / 100), 2)

        payload = {
            "productName":  name,
            "description":  product["desc"],
            "quantity":     product["qty"],
            "price":        product["price"],
            "discount":     product["discount"],
            "specialPrice": special,
            "image":        "default.png",
        }

        res = requests.post(
            f"{API}/api/admin/categories/{cat_id}/product",
            headers=json_headers(token),
            json=payload,
            timeout=REQUEST_TIMEOUT,
        )

        if res.status_code in (200, 201):
            product_id = res.json().get("productId")
            print(f"       OK     Created (id={product_id})")
            prods_created += 1

            if upload_image(token, product_id, product["image_url"]):
                imgs_success += 1
            else:
                imgs_failed += 1
        else:
            print(f"       FAIL   ({res.status_code}): {res.text[:200]}")

        time.sleep(DELAY_BETWEEN)

    print()
    print("=" * 60)
    print("  SEEDING SUMMARY")
    print("=" * 60)
    print(f"  Categories created : {cats_created}")
    print(f"  Categories skipped : {cats_skipped}")
    print(f"  Products created   : {prods_created}")
    print(f"  Products skipped   : {prods_skipped}")
    print(f"  Images succeeded   : {imgs_success}")
    print(f"  Images failed      : {imgs_failed}")
    print("=" * 60)
    print()

if __name__ == "__main__":
    seed()
