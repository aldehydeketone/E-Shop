"""
seed_production.py
==================
E-Shop Production Database Seeder -- IDEMPOTENT & COMPLETE CATALOG

Production URL : https://sb-ecom-latest-qurh.onrender.com
Catalog        : 30 products across 5 categories (6 per category)

SAFETY & RELIABILITY
--------------------
  * Categories : Reuse existing categories, create missing ones.
  * Products   : Check existing by name -> insert only missing products.
  * Images     : Download high-quality tech photo from Unsplash -> PUT /api/admin/products/{id}/image.
  * Idempotent : Safe to execute multiple times without duplicates or data loss.
"""

import os
import sys
import time
import requests

BASE_URL        = "https://sb-ecom-latest-qurh.onrender.com"
ADMIN_USER      = "admin"
ADMIN_PASS      = "adminPass"
REQUEST_TIMEOUT = 30
IMAGE_TIMEOUT   = 25
DELAY_BETWEEN   = 0.5

API = BASE_URL.rstrip("/")

CATALOG = [
    # ── Laptops ──────────────────────────────────────────────────────────────
    {
        "name":      "MacBook Air M4",
        "category":  "Laptops",
        "price":     1299.00,
        "discount":  0.0,
        "qty":       50,
        "desc":      "Super thin and light MacBook Air with the powerful new M4 chip, all-day 18-hour battery life, and Liquid Retina display.",
        "image_url": "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=600&q=80",
    },
    {
        "name":      "Dell XPS 15",
        "category":  "Laptops",
        "price":     1499.00,
        "discount":  5.0,
        "qty":       35,
        "desc":      "Premium Windows workstation featuring 4K OLED InfinityEdge display and Intel Core Ultra processor.",
        "image_url": "https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=600&q=80",
    },
    {
        "name":      "ASUS ROG Strix",
        "category":  "Laptops",
        "price":     1799.00,
        "discount":  8.0,
        "qty":       25,
        "desc":      "High-performance gaming laptop with NVIDIA GeForce RTX 4080, 240Hz ROG Nebula display, and RGB lighting.",
        "image_url": "https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=600&q=80",
    },
    {
        "name":      "Lenovo ThinkPad X1 Carbon",
        "category":  "Laptops",
        "price":     1399.00,
        "discount":  10.0,
        "qty":       40,
        "desc":      "Ultralight business laptop with ultra-durable carbon-fiber chassis, legendary TrackPoint, and enterprise security.",
        "image_url": "https://images.unsplash.com/photo-1541807084-5c52b6b3adef?w=600&q=80",
    },
    {
        "name":      "HP Spectre x360",
        "category":  "Laptops",
        "price":     1199.00,
        "discount":  5.0,
        "qty":       30,
        "desc":      "2-in-1 convertible laptop with gem-cut design, 3K2K OLED touchscreen, and included active tilt stylus.",
        "image_url": "https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?w=600&q=80",
    },
    {
        "name":      "Razer Blade 16",
        "category":  "Laptops",
        "price":     2499.00,
        "discount":  0.0,
        "qty":       15,
        "desc":      "Ultra-portable gaming flagship with dual-mode Mini-LED display and anodized aluminum CNC unibody design.",
        "image_url": "https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=600&q=80",
    },

    # ── Audio ─────────────────────────────────────────────────────────────────
    {
        "name":      "Sony WH-1000XM5",
        "category":  "Audio",
        "price":     399.00,
        "discount":  10.0,
        "qty":       100,
        "desc":      "Industry-leading noise-canceling wireless headphones with HD Noise Canceling Processor QN1 and 30hr battery.",
        "image_url": "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?w=600&q=80",
    },
    {
        "name":      "Bose QuietComfort Ultra",
        "category":  "Audio",
        "price":     429.00,
        "discount":  0.0,
        "qty":       80,
        "desc":      "World-class active noise cancellation with spatialized audio for immersive listening comfort anywhere.",
        "image_url": "https://images.unsplash.com/photo-1546435770-a3e426bf472b?w=600&q=80",
    },
    {
        "name":      "Apple AirPods Pro 2",
        "category":  "Audio",
        "price":     249.00,
        "discount":  0.0,
        "qty":       150,
        "desc":      "Active Noise Cancellation, Adaptive Audio, Personalized Spatial Audio, and USB-C MagSafe charging case.",
        "image_url": "https://images.unsplash.com/photo-1603351154351-5e2d0600bb77?w=600&q=80",
    },
    {
        "name":      "JBL Live 770NC",
        "category":  "Audio",
        "price":     199.00,
        "discount":  15.0,
        "qty":       90,
        "desc":      "Wireless over-ear headphones with True Adaptive Noise Cancelling, Personi-Fi 2.0, and up to 65 hours playtime.",
        "image_url": "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&q=80",
    },
    {
        "name":      "Marshall Major IV",
        "category":  "Audio",
        "price":     149.00,
        "discount":  10.0,
        "qty":       110,
        "desc":      "Iconic Marshall design with 80+ solid hours of wireless playtime, wireless charging, and custom-tuned drivers.",
        "image_url": "https://images.unsplash.com/photo-1583394838336-acd977736f90?w=600&q=80",
    },
    {
        "name":      "Sennheiser Momentum 4",
        "category":  "Audio",
        "price":     349.00,
        "discount":  5.0,
        "qty":       60,
        "desc":      "Audiophile-grade 42mm transducer system delivering benchmark acoustic performance and outstanding 60-hour battery.",
        "image_url": "https://images.unsplash.com/photo-1484704849700-f032a568e944?w=600&q=80",
    },

    # ── Wearables ─────────────────────────────────────────────────────────────
    {
        "name":      "Apple Watch Series 10",
        "category":  "Wearables",
        "price":     499.00,
        "discount":  5.0,
        "qty":       75,
        "desc":      "Thinnest Apple Watch ever with largest display area, faster charging, ECG app, and sleep apnea notifications.",
        "image_url": "https://images.unsplash.com/photo-1546868871-7041f2a55e12?w=600&q=80",
    },
    {
        "name":      "Samsung Galaxy Watch Ultra",
        "category":  "Wearables",
        "price":     649.00,
        "discount":  0.0,
        "qty":       45,
        "desc":      "Rugged titanium smartwatch built for outdoor extremes, dual-frequency GPS, Energy Score, and 100m water resistance.",
        "image_url": "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&q=80",
    },
    {
        "name":      "Garmin Venu 3",
        "category":  "Wearables",
        "price":     449.00,
        "discount":  0.0,
        "qty":       55,
        "desc":      "GPS fitness smartwatch with bright AMOLED display, advanced health tracking, wheelchair mode, and up to 14 days battery.",
        "image_url": "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=600&q=80",
    },
    {
        "name":      "Fitbit Charge 6",
        "category":  "Wearables",
        "price":     159.00,
        "discount":  12.0,
        "qty":       120,
        "desc":      "Advanced fitness tracker with Google tools, heart rate on gym equipment, built-in GPS, and 40+ exercise modes.",
        "image_url": "https://images.unsplash.com/photo-1557935728-e6d1eaed558b?w=600&q=80",
    },
    {
        "name":      "Google Pixel Watch 3",
        "category":  "Wearables",
        "price":     349.00,
        "discount":  0.0,
        "qty":       65,
        "desc":      "Actua AMOLED display, Fitbit readiness score, real-time running guidance, and seamless Google ecosystem integration.",
        "image_url": "https://images.unsplash.com/photo-1508685096489-7aacd43bd3b1?w=600&q=80",
    },
    {
        "name":      "Amazfit T-Rex 3",
        "category":  "Wearables",
        "price":     279.00,
        "discount":  10.0,
        "qty":       80,
        "desc":      "Military-grade outdoor GPS watch with 27-day battery life, offline maps, and ultra-bright 2000-nit display.",
        "image_url": "https://images.unsplash.com/photo-1510017803434-a899398421b3?w=600&q=80",
    },

    # ── Speakers ──────────────────────────────────────────────────────────────
    {
        "name":      "JBL Charge 5",
        "category":  "Speakers",
        "price":     179.00,
        "discount":  10.0,
        "qty":       100,
        "desc":      "Portable Bluetooth speaker with bold JBL Original Pro Sound, IP67 waterproof and dustproof design, and built-in powerbank.",
        "image_url": "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=600&q=80",
    },
    {
        "name":      "JBL Flip 6",
        "category":  "Speakers",
        "price":     129.00,
        "discount":  5.0,
        "qty":       140,
        "desc":      "Compact 2-way speaker system engineered for loud, crystal-clear, powerful sound with 12 hours of uninterrupted play.",
        "image_url": "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=600&q=80",
    },
    {
        "name":      "Sony SRS-XB100",
        "category":  "Speakers",
        "price":     59.00,
        "discount":  0.0,
        "qty":       200,
        "desc":      "Ultra-portable wireless speaker with Extra Bass, Sound Diffusion Processor, multiway strap, and 16hr battery life.",
        "image_url": "https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=600&q=80",
    },
    {
        "name":      "Marshall Acton III",
        "category":  "Speakers",
        "price":     279.00,
        "discount":  0.0,
        "qty":       50,
        "desc":      "Home Bluetooth speaker with wider soundstage, iconic vintage aesthetic, brass control knobs, and eco-friendly build.",
        "image_url": "https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b?w=600&q=80",
    },
    {
        "name":      "Ultimate Ears MEGABOOM 3",
        "category":  "Speakers",
        "price":     199.00,
        "discount":  15.0,
        "qty":       70,
        "desc":      "Powerful 360-degree sound speaker with intense bass, Magic Button controls, virtually indestructible waterproof enclosure.",
        "image_url": "https://images.unsplash.com/photo-1570829460005-c840387bb1ca?w=600&q=80",
    },
    {
        "name":      "Bose SoundLink Flex",
        "category":  "Speakers",
        "price":     149.00,
        "discount":  0.0,
        "qty":       90,
        "desc":      "PositionIQ technology automatically optimizes sound orientation, silicone body resists drops, floats in water.",
        "image_url": "https://images.unsplash.com/photo-1589003077984-894e133dabab?w=600&q=80",
    },

    # ── Tablets ───────────────────────────────────────────────────────────────
    {
        "name":      "Apple iPad Air M2",
        "category":  "Tablets",
        "price":     749.00,
        "discount":  0.0,
        "qty":       60,
        "desc":      "Redesigned 11-inch iPad Air powered by M2 chip, Liquid Retina display, landscape front camera, and Wi-Fi 6E.",
        "image_url": "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=600&q=80",
    },
    {
        "name":      "Samsung Galaxy Tab S10",
        "category":  "Tablets",
        "price":     999.00,
        "discount":  5.0,
        "qty":       40,
        "desc":      "Dynamic AMOLED 2X display with anti-reflection coating, S Pen included in box, Galaxy AI productivity tools.",
        "image_url": "https://images.unsplash.com/photo-1561154464-82e9adf32764?w=600&q=80",
    },
    {
        "name":      "Xiaomi Pad 7",
        "category":  "Tablets",
        "price":     399.00,
        "discount":  10.0,
        "qty":       100,
        "desc":      "144Hz 3.2K crystal-clear display, Snapdragon flagship processor, quad speakers with Dolby Atmos sound.",
        "image_url": "https://images.unsplash.com/photo-1585790050230-5dd28404ccb9?w=600&q=80",
    },
    {
        "name":      "Lenovo Tab Extreme",
        "category":  "Tablets",
        "price":     1099.00,
        "discount":  0.0,
        "qty":       20,
        "desc":      "Massive 14.5-inch 3K OLED screen with 120Hz refresh rate, 8 JBL speakers, dual-mode stand keyboard included.",
        "image_url": "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=600&q=80",
    },
    {
        "name":      "iPad Pro M4 13-inch",
        "category":  "Tablets",
        "price":     1299.00,
        "discount":  0.0,
        "qty":       30,
        "desc":      "Ultra-thin 5.1mm breakthrough design with Ultra Retina XDR tandem OLED display and Apple M4 performance engine.",
        "image_url": "https://images.unsplash.com/photo-1561154464-82e9adf32764?w=600&q=80",
    },
    {
        "name":      "OnePlus Pad 2",
        "category":  "Tablets",
        "price":     549.00,
        "discount":  8.0,
        "qty":       70,
        "desc":      "12.1-inch 3K 144Hz ReadFit display with 7:5 ratio, Snapdragon 8 Gen 3, 67W SUPERVOOC fast charging.",
        "image_url": "https://images.unsplash.com/photo-1585790050230-5dd28404ccb9?w=600&q=80",
    },
]

REQUIRED_CATEGORIES = list(dict.fromkeys(p["category"] for p in CATALOG))


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

    print("[OK]   Authenticated successfully\n")
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
    print("[CATS] Ensuring required categories exist ...")
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


def upload_image(token, product_id, image_url):
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
            res = requests.put(
                f"{API}/api/admin/products/{product_id}/image",
                headers=bare_headers(token),
                files={"image": (f"product_{product_id}.jpg", fh, "image/jpeg")},
                timeout=REQUEST_TIMEOUT,
            )
        if res.status_code == 200:
            print(f"         [IMG]  Uploaded image successfully")
            success = True
        else:
            print(f"         [WARN] Image upload failed ({res.status_code}): {res.text[:150]}")
    except Exception as exc:
        print(f"         [WARN] Image upload exception: {exc}")
    finally:
        if os.path.exists(tmp_file):
            os.remove(tmp_file)

    return success


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

    print(f"[INFO] Products currently in database : {len(existing)}")
    print(f"[INFO] Products in seeding catalog    : {len(CATALOG)}")
    print()

    prods_created = 0
    prods_skipped = 0
    imgs_success  = 0
    imgs_failed   = 0

    print("[SEED] Inserting products & uploading images ...\n")
    for product in CATALOG:
        name     = product["name"]
        category = product["category"]
        cat_id   = cat_map.get(category)

        print(f"  [{category}]  {name}")

        if name.lower() in existing:
            print("       SKIP   Already exists in production database")
            prods_skipped += 1
            continue

        if not cat_id:
            print(f"       FAIL   No category ID for '{category}'")
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
            print(f"       OK     Created product (id={product_id})")
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
