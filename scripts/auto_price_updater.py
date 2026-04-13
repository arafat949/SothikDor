#!/usr/bin/env python3
import requests
from bs4 import BeautifulSoup
import firebase_admin
from firebase_admin import credentials, db
from datetime import datetime
import time

FIREBASE_URL = "https://sothik-dor-default-rtdb.asia-southeast1.firebasedatabase.app"
SERVICE_ACCOUNT_KEY = "scripts/serviceAccountKey.json"

PRODUCT_MAP = {
    "Potato": {"id": "p001", "name": "আলু (দেশি)", "emoji": "🥔", "category": "সবজি", "unit": "কেজি"},
    "Onion(Local)": {"id": "p002", "name": "পেঁয়াজ (দেশি)", "emoji": "🧅", "category": "সবজি", "unit": "কেজি"},
    "Miniket Rice": {"id": "p004", "name": "মিনিকেট চাল", "emoji": "🌾", "category": "চাল-ডাল", "unit": "কেজি"},
    "Lentil(Local)": {"id": "p006", "name": "মসুর ডাল", "emoji": "🫘", "category": "চাল-ডাল", "unit": "কেজি"},
    "Soyabean Oil(Loose)": {"id": "p007", "name": "সয়াবিন তেল", "emoji": "🛢", "category": "তেল-মশলা", "unit": "লিটার"},
    "Rohu Fish": {"id": "p008", "name": "রুই মাছ", "emoji": "🐟", "category": "মাছ", "unit": "কেজি"},
    "Broiler Chicken": {"id": "p010", "name": "ব্রয়লার মুরগি", "emoji": "🍗", "category": "মাংস", "unit": "কেজি"},
    "Beef": {"id": "p011", "name": "গরুর মাংস", "emoji": "🥩", "category": "মাংস", "unit": "কেজি"},
    "Egg(Farm)": {"id": "p012", "name": "ডিম (ফার্ম)", "emoji": "🥚", "category": "ডিম", "unit": "হালি"},
    "Tomato": {"id": "p013", "name": "টমেটো", "emoji": "🍅", "category": "সবজি", "unit": "কেজি"},
}

MARKET_MAP = {
    "Karwan Bazar": {"id": "m001", "name": "কারওয়ান বাজার"},
    "Shantinagar": {"id": "m002", "name": "শান্তিনগর বাজার"},
    "Mohammadpur": {"id": "m003", "name": "মোহাম্মদপুর বাজার"},
    "Mirpur": {"id": "m004", "name": "মিরপুর ১০ বাজার"},
}

def scrape_dam_prices():
    print("DAM website থেকে দাম সংগ্রহ করা হচ্ছে...")
    url = "https://market.dam.gov.bd/market_daily_price_report?L=E"
    headers = {"User-Agent": "Mozilla/5.0"}
    try:
        response = requests.get(url, headers=headers, timeout=15)
        soup = BeautifulSoup(response.content, "html.parser")
        prices = []
        table = soup.find("table")
        if not table:
            print("Table পাওয়া যায়নি")
            return []
        rows = table.find_all("tr")[1:]
        for row in rows:
            cols = row.find_all("td")
            if len(cols) >= 4:
                product_name = cols[0].text.strip()
                market_name = cols[1].text.strip()
                try:
                    min_price = float(cols[2].text.strip().replace(",", ""))
                    max_price = float(cols[3].text.strip().replace(",", ""))
                except:
                    continue
                for key in PRODUCT_MAP:
                    if key.lower() in product_name.lower():
                        for mkey in MARKET_MAP:
                            if mkey.lower() in market_name.lower():
                                prices.append({
                                    "product": PRODUCT_MAP[key],
                                    "market": MARKET_MAP[mkey],
                                    "min": min_price,
                                    "max": max_price,
                                })
        print(str(len(prices)) + " টি দাম পাওয়া গেছে")
        return prices
    except Exception as e:
        print("Error: " + str(e))
        return []

def push_to_firebase(prices):
    if not firebase_admin._apps:
        cred = credentials.Certificate(SERVICE_ACCOUNT_KEY)
        firebase_admin.initialize_app(cred, {"databaseURL": FIREBASE_URL})
    today = datetime.now().strftime("%Y-%m-%d")
    ref = db.reference("prices/" + today)
    for item in prices:
        p = item["product"]
        m = item["market"]
        key = p["id"] + "_" + m["id"]
        ref.child(key).set({
            "productId": p["id"],
            "productName": p["name"],
            "productEmoji": p["emoji"],
            "category": p["category"],
            "unit": p["unit"],
            "marketId": m["id"],
            "marketName": m["name"],
            "minPrice": item["min"],
            "maxPrice": item["max"],
            "avgPrice": (item["min"] + item["max"]) / 2,
            "date": today,
            "timestamp": int(time.time() * 1000),
            "source": "DAM Auto"
        })
        print("OK: " + p["name"] + " @ " + m["name"])

def main():
    print("সঠিক দর - Auto Price Updater")
    print("=" * 40)
    prices = scrape_dam_prices()
    if not prices:
        return
    push_to_firebase(prices)

if __name__ == "__main__":
    main()
