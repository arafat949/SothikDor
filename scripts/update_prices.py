import urllib.request
import json
import os
import random
from datetime import datetime

FIREBASE_URL = os.environ.get('FIREBASE_URL',
    'https://sothik-dor-default-rtdb.asia-southeast1.firebasedatabase.app')

today = datetime.now().strftime("%Y-%m-%d")
url = f"{FIREBASE_URL}/prices/{today}.json"
ts  = int(datetime.now().timestamp() * 1000)

def make_price(pid, name, emoji, cat, unit, mid, mname, base_min, base_max):
    # প্রতিদিন সামান্য variation যোগ করি (realistic fluctuation)
    variation = random.uniform(-0.05, 0.08)
    mn = round(base_min * (1 + variation))
    mx = round(base_max * (1 + variation))
    avg = (mn + mx) / 2
    return {
        "priceId": f"{pid}_{mid}",
        "productId": pid,
        "productName": name,
        "productEmoji": emoji,
        "category": cat,
        "unit": unit,
        "marketId": mid,
        "marketName": mname,
        "minPrice": float(mn),
        "maxPrice": float(mx),
        "avgPrice": float(round(avg)),
        "previousAvgPrice": float(round(avg * 0.97)),
        "date": today,
        "timestamp": ts
    }

markets = [
    ("m001", "কারওয়ান বাজার",      0),
    ("m002", "শান্তিনগর বাজার",    1),
    ("m003", "মোহাম্মদপুর বাজার", 2),
    ("m004", "মিরপুর ১০ বাজার",   3),
    ("m005", "রামপুরা বাজার",      4),
    ("m006", "যাত্রাবাড়ী বাজার", 5),
]

# [pid, name, emoji, cat, unit, base_min, base_max]
# এপ্রিল ২০২৬ এর বাস্তব বাজার দর অনুযায়ী
products = [
    ("p001","আলু (দেশি)",     "🥔","সবজি",    "কেজি", 30, 45),
    ("p002","পেঁয়াজ (দেশি)","🧅","সবজি",    "কেজি", 50, 70),
    ("p003","টমেটো",          "🍅","সবজি",    "কেজি", 40, 70),
    ("p004","বেগুন",          "🍆","সবজি",    "কেজি", 40, 70),
    ("p005","লাউ",            "🥬","সবজি",    "পিস",  35, 60),
    ("p006","মিনিকেট চাল",   "🌾","চাল-ডাল","কেজি", 65, 78),
    ("p007","নাজিরশাইল চাল", "🌾","চাল-ডাল","কেজি", 72, 85),
    ("p008","মসুর ডাল",      "🫘","চাল-ডাল","কেজি",110,140),
    ("p009","সয়াবিন তেল",   "🛢️","তেল-মশলা","লিটার",155,170),
    ("p010","রসুন",           "🧄","মশলা",    "কেজি",180,240),
    ("p011","আদা",            "🫚","মশলা",    "কেজি",120,180),
    ("p012","রুই মাছ",       "🐟","মাছ",     "কেজি",220,300),
    ("p013","কাতলা মাছ",     "🐠","মাছ",     "কেজি",230,320),
    ("p014","ইলিশ মাছ",      "🐡","মাছ",     "কেজি",700,1200),
    ("p015","মুরগি (ব্রয়লার)","🍗","মাংস",  "কেজি",180,210),
    ("p016","গরুর মাংস",     "🥩","মাংস",    "কেজি",700,780),
    ("p017","ডিম (হালি)",    "🥚","ডিম",     "হালি", 48, 56),
    ("p018","কলা",            "🍌","ফল",      "হালি", 40, 65),
]

prices = {}
for pid, name, emoji, cat, unit, bmin, bmax in products:
    for mid, mname, idx in markets:
        # প্রতিটি বাজারে সামান্য আলাদা দাম
        market_offset = idx * 0.02
        mn = round(bmin * (1 + market_offset))
        mx = round(bmax * (1 + market_offset))
        # দৈনিক random variation
        v = random.uniform(-0.04, 0.06)
        mn = round(mn * (1 + v))
        mx = round(mx * (1 + v))
        avg = round((mn + mx) / 2)
        key = f"{pid}_{mid}"
        prices[key] = {
            "priceId": key,
            "productId": pid,
            "productName": name,
            "productEmoji": emoji,
            "category": cat,
            "unit": unit,
            "marketId": mid,
            "marketName": mname,
            "minPrice": float(mn),
            "maxPrice": float(mx),
            "avgPrice": float(avg),
            "previousAvgPrice": float(round(avg * 0.97)),
            "date": today,
            "timestamp": ts
        }

data = json.dumps(prices).encode('utf-8')
req = urllib.request.Request(url, data=data, method='PATCH')
req.add_header('Content-Type', 'application/json')

try:
    response = urllib.request.urlopen(req)
    print(f"✅ {today}: {len(prices)}টি price update সফল! ({len(products)} পণ্য × {len(markets)} বাজার)")
except Exception as e:
    print(f"❌ Error: {e}")
    raise
