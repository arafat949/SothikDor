import urllib.request
import json
import random
from datetime import datetime

FIREBASE_URL = "https://sothik-dor-default-rtdb.asia-southeast1.firebasedatabase.app"
today = datetime.now().strftime("%Y-%m-%d")
url = f"{FIREBASE_URL}/prices/{today}.json"
ts = int(datetime.now().timestamp() * 1000)

# বাংলাদেশের বাস্তব বাজার দর (এপ্রিল ২০২৬ অনুযায়ী)
# প্রতিদিন ±৫% realistic variation
BASE_PRICES = {
    "p001": {"name": "আলু (দেশি)",      "emoji": "🥔", "cat": "সবজি",    "unit": "কেজি",  "min": 30,  "max": 45},
    "p002": {"name": "পেঁয়াজ (দেশি)", "emoji": "🧅", "cat": "সবজি",    "unit": "কেজি",  "min": 45,  "max": 65},
    "p003": {"name": "টমেটো",           "emoji": "🍅", "cat": "সবজি",    "unit": "কেজি",  "min": 40,  "max": 80},
    "p004": {"name": "বেগুন",           "emoji": "🍆", "cat": "সবজি",    "unit": "কেজি",  "min": 40,  "max": 70},
    "p005": {"name": "লাউ",             "emoji": "🥬", "cat": "সবজি",    "unit": "পিস",   "min": 30,  "max": 60},
    "p006": {"name": "মিনিকেট চাল",    "emoji": "🌾", "cat": "চাল-ডাল", "unit": "কেজি",  "min": 65,  "max": 80},
    "p007": {"name": "নাজিরশাইল চাল",  "emoji": "🌾", "cat": "চাল-ডাল", "unit": "কেজি",  "min": 72,  "max": 88},
    "p008": {"name": "মসুর ডাল",       "emoji": "🫘", "cat": "চাল-ডাল", "unit": "কেজি",  "min": 110, "max": 140},
    "p009": {"name": "সয়াবিন তেল",    "emoji": "🛢️", "cat": "তেল-মশলা","unit": "লিটার", "min": 155, "max": 175},
    "p010": {"name": "রসুন",            "emoji": "🧄", "cat": "মশলা",    "unit": "কেজি",  "min": 180, "max": 250},
    "p011": {"name": "আদা",             "emoji": "🫚", "cat": "মশলা",    "unit": "কেজি",  "min": 120, "max": 180},
    "p012": {"name": "রুই মাছ",        "emoji": "🐟", "cat": "মাছ",     "unit": "কেজি",  "min": 220, "max": 300},
    "p013": {"name": "কাতলা মাছ",      "emoji": "🐠", "cat": "মাছ",     "unit": "কেজি",  "min": 230, "max": 320},
    "p014": {"name": "ইলিশ মাছ",       "emoji": "🐡", "cat": "মাছ",     "unit": "কেজি",  "min": 800, "max": 1400},
    "p015": {"name": "মুরগি (ব্রয়লার)","emoji": "🍗", "cat": "মাংস",    "unit": "কেজি",  "min": 180, "max": 220},
    "p016": {"name": "গরুর মাংস",      "emoji": "🥩", "cat": "মাংস",    "unit": "কেজি",  "min": 700, "max": 800},
    "p017": {"name": "ডিম (হালি)",     "emoji": "🥚", "cat": "ডিম",     "unit": "হালি",  "min": 46,  "max": 56},
    "p018": {"name": "কলা",             "emoji": "🍌", "cat": "ফল",      "unit": "হালি",  "min": 40,  "max": 70},
}

MARKETS = [
    ("m001", "কারওয়ান বাজার",       0.00),
    ("m002", "শান্তিনগর বাজার",     0.03),
    ("m003", "মোহাম্মদপুর বাজার",  0.02),
    ("m004", "মিরপুর ১০ বাজার",    0.04),
    ("m005", "রামপুরা বাজার",       0.03),
    ("m006", "যাত্রাবাড়ী বাজার",  0.05),
]

# Seasonal adjustment — এপ্রিল মাসে সবজির দাম একটু বেশি
month = datetime.now().month
seasonal = {
    "সবজি": 1.1 if month in [3,4,5] else 0.95 if month in [11,12,1] else 1.0,
    "মাছ":  1.05,
    "মাংস": 1.0,
    "চাল-ডাল": 1.0,
    "তেল-মশলা": 1.0,
    "মশলা": 1.0,
    "ডিম": 1.0,
    "ফল": 1.0 if month in [3,4,5,6] else 1.1,
}

prices = {}
random.seed(int(datetime.now().strftime("%Y%m%d")))  # same day = same prices

for pid, info in BASE_PRICES.items():
    s = seasonal.get(info["cat"], 1.0)
    daily_v = random.uniform(-0.04, 0.06)  # daily variation

    for mid, mname, market_offset in MARKETS:
        market_v = random.uniform(-0.02, 0.02)
        mn = round(info["min"] * s * (1 + daily_v + market_v))
        mx = round(info["max"] * s * (1 + daily_v + market_v))
        avg = round((mn + mx) / 2)
        prev = round(avg * random.uniform(0.94, 1.04))

        key = f"{pid}_{mid}"
        prices[key] = {
            "priceId": key,
            "productId": pid,
            "productName": info["name"],
            "productEmoji": info["emoji"],
            "category": info["cat"],
            "unit": info["unit"],
            "marketId": mid,
            "marketName": mname,
            "minPrice": float(mn),
            "maxPrice": float(mx),
            "avgPrice": float(avg),
            "previousAvgPrice": float(prev),
            "date": today,
            "timestamp": ts
        }

data = json.dumps(prices).encode('utf-8')
req = urllib.request.Request(url, data=data, method='PATCH')
req.add_header('Content-Type', 'application/json')

try:
    response = urllib.request.urlopen(req)
    print(f"✅ {today}: {len(prices)}টি price update সফল!")
    print(f"   {len(BASE_PRICES)} পণ্য × {len(MARKETS)} বাজার")
    print(f"   Seasonal adjustment: সবজি ×{seasonal['সবজি']}")
except Exception as e:
    print(f"❌ Error: {e}")
    raise
