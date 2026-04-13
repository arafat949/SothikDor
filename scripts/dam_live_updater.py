#!/usr/bin/env python3
import requests, warnings, re, firebase_admin, time
from firebase_admin import credentials, db
from datetime import datetime
warnings.filterwarnings('ignore')

if not firebase_admin._apps:
    cred = credentials.Certificate('scripts/serviceAccountKey.json')
    firebase_admin.initialize_app(cred, {'databaseURL': 'https://sothik-dor-default-rtdb.asia-southeast1.firebasedatabase.app'})

PRODUCT_MAP = {
    'Aman-Fine':             ('p101', 'আমন চাল (সরু)',      '🌾', 'চাল-ডাল',  'কেজি'),
    'Aman-Medium':           ('p102', 'আমন চাল (মাঝারি)',   '🌾', 'চাল-ডাল',  'কেজি'),
    'Aman-Coarse':           ('p103', 'আমন চাল (মোটা)',     '🌾', 'চাল-ডাল',  'কেজি'),
    'Boro-Fine':             ('p104', 'বোরো চাল (সরু)',     '🌾', 'চাল-ডাল',  'কেজি'),
    'Boro-Medium':           ('p105', 'বোরো চাল (মাঝারি)', '🌾', 'চাল-ডাল',  'কেজি'),
    'Boro-Coarse':           ('p106', 'বোরো চাল (মোটা)',   '🌾', 'চাল-ডাল',  'কেজি'),
    'Ata (packet)':          ('p107', 'আটা (প্যাকেট)',      '🌾', 'চাল-ডাল',  'কেজি'),
    'Farm-raised Hen':       ('p010', 'ব্রয়লার মুরগি',     '🍗', 'মাংস',     'কেজি'),
    'Beef':                  ('p011', 'গরুর মাংস',          '🥩', 'মাংস',     'কেজি'),
    'Mutton':                ('p108', 'খাসির মাংস',         '🥩', 'মাংস',     'কেজি'),
    'Egg Farm-Red':          ('p012', 'ডিম (ফার্ম)',        '🥚', 'ডিম',      'হালি'),
    'Sugar (Local)':         ('p109', 'চিনি (লোকাল)',       '🍬', 'মশলা',     'কেজি'),
    'Iodized Salt (Packed)': ('p110', 'লবণ (আয়োডিন)',      '🧂', 'মশলা',     'কেজি'),
    'Mung':                  ('p111', 'মুগ ডাল',            '🫘', 'চাল-ডাল',  'কেজি'),
    'Gram-Whole':            ('p112', 'ছোলা',               '🫘', 'চাল-ডাল',  'কেজি'),
    'Soybean':               ('p007', 'সয়াবিন তেল',        '🛢', 'তেল-মশলা', 'লিটার'),
    'Onion-local':           ('p002', 'পেঁয়াজ (দেশি)',     '🧅', 'সবজি',     'কেজি'),
    'Garlic-local':          ('p003', 'রসুন (দেশি)',        '🧄', 'মশলা',     'কেজি'),
    'Garlic-Imported':       ('p113', 'রসুন (আমদানি)',      '🧄', 'মশলা',     'কেজি'),
    'Green Chili':           ('p114', 'কাঁচা মরিচ',         '🌶', 'সবজি',     'কেজি'),
    'Ginger-local':          ('p005', 'আদা (দেশি)',         '🫚', 'মশলা',     'কেজি'),
    'Ginger-Imported':       ('p115', 'আদা (আমদানি)',       '🫚', 'মশলা',     'কেজি'),
}

MARKETS = [
    ('m001', 'কারওয়ান বাজার',     1.00),
    ('m002', 'শান্তিনগর বাজার',   1.03),
    ('m003', 'মোহাম্মদপুর বাজার', 0.97),
    ('m004', 'মিরপুর ১০ বাজার',   0.98),
    ('m005', 'রামপুরা বাজার',      1.02),
]

def fetch_dam_prices():
    r = requests.get('http://market.dam.gov.bd/market_daily_price_report?L=E',
        headers={'User-Agent': 'Mozilla/5.0'}, timeout=20, verify=False)
    pattern = r'<span class="stockbox"><a href="#([^"]+)">([^<]+)</a>:&nbsp;\s*([\d.]+)\s*-\s*([\d.]+)'
    return re.findall(pattern, r.text)

def push_to_firebase(stockboxes):
    today = datetime.now().strftime('%Y-%m-%d')
    ref = db.reference('prices/' + today)
    count = 0
    for s in stockboxes:
        dam_name = s[0]
        min_p = float(s[2])
        max_p = float(s[3])
        if dam_name not in PRODUCT_MAP:
            continue
        pid, pname, emoji, cat, unit = PRODUCT_MAP[dam_name]
        for mid, mname, factor in MARKETS:
            key = pid + '_' + mid
            ref.child(key).set({
                'productId': pid,
                'productName': pname,
                'productEmoji': emoji,
                'category': cat,
                'unit': unit,
                'marketId': mid,
                'marketName': mname,
                'minPrice': round(min_p * factor),
                'maxPrice': round(max_p * factor),
                'avgPrice': round((min_p + max_p) / 2 * factor),
                'date': today,
                'timestamp': int(time.time() * 1000),
                'source': 'DAM Live'
            })
            count += 1
        print('OK:', pname, '|', int(min_p), '-', int(max_p), 'টাকা')
    print()
    print('মোট', count, 'টি দাম', len(MARKETS), 'টি বাজারে আপডেট হয়েছে!')
    print('তারিখ:', today)

print('DAM থেকে live দাম আনা হচ্ছে...')
data = fetch_dam_prices()
print('পাওয়া গেছে:', len(data), 'টি পণ্য')
print()
push_to_firebase(data)
