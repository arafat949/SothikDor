# সঠিক দর (Sothik Dor) 🛒
### ডিজিটাল কিচেন মার্কেট - Android App

## ✅ সম্পূর্ণ ফিচার তালিকা
| ফিচার | বর্ণনা | স্ট্যাটাস |
|-------|---------|-----------|
| 🔴 Real-time দাম | Firebase-এ দাম পরিবর্তন হলে অ্যাপে সাথে সাথে আপডেট | ✅ |
| 📊 Price Chart | MPAndroidChart দিয়ে ৭ দিনের দাম অ্যানিমেশন সহ | ✅ |
| ⚖️ বাজার তুলনা | বিভিন্ন বাজারের দাম পাশাপাশি তুলনা | ✅ |
| 💰 বাজেট ক্যালকুলেটর | পরিমাণ দিলে মোট খরচ হিসাব | ✅ |
| 📍 Google Maps | নিকটস্থ বাজার দেখানো | ✅ |
| 🌙 Dark/Light Mode | দুটি থিম সাপোর্ট | ✅ |
| 📱 অফলাইন | Firebase cache-এ ইন্টারনেট ছাড়াও কাজ করে | ✅ |
| 🔍 সার্চ ও ফিল্টার | পণ্য খোঁজা ও ক্যাটাগরি ফিল্টার | ✅ |
| 📤 শেয়ার | বাজার লিস্ট WhatsApp-এ শেয়ার করা | ✅ |

## 🚀 সেটআপ ধাপ

### ধাপ ১: Firebase প্রজেক্ট তৈরি
1. https://console.firebase.google.com এ যান
2. "Add project" → নাম: "sothik-dor"
3. Android App যোগ করুন, Package: com.sothikdor
4. google-services.json ডাউনলোড করে app/ ফোল্ডারে রাখুন

### ধাপ ২: Firebase Services চালু করা
- Authentication → Email/Password ও Anonymous: Enable
- Realtime Database → Create → Test mode → Enable

### ধাপ ৩: Firebase Rules
```json
{
  "rules": {
    "products": { ".read": true, ".write": "auth != null" },
    "prices": { ".read": true, ".write": "auth != null" },
    "markets": { ".read": true, ".write": "auth != null" },
    "users": { "$uid": { ".read": "auth.uid === $uid", ".write": "auth.uid === $uid" } }
  }
}
```

### ধাপ ৪: Google Maps API Key
AndroidManifest.xml এ "YOUR_GOOGLE_MAPS_API_KEY_HERE" এর জায়গায় আপনার key বসান।

### ধাপ ৫: Sample Data লোড
MainActivity.java এর onCreate() এ একবার এই লাইন যোগ করুন:
```java
FirebaseHelper.getInstance().insertSampleData();
```
ডেটা লোড হওয়ার পর এই লাইনটি মুছুন।

## 💡 টিচারকে ইমপ্রেস করার পয়েন্ট
1. Firebase Console থেকে সরাসরি দাম পরিবর্তন করুন → অ্যাপে সাথে সাথে আপডেট হবে
2. পণ্যে ক্লিক করুন → Chart অ্যানিমেশন দেখান
3. Internet বন্ধ করুন → অ্যাপ এখনও কাজ করবে (offline cache)
4. Dark mode টগল করে দেখান

## 📁 ফাইল স্ট্রাকচার
```
activities/    → SplashActivity, LoginActivity, MainActivity, ChartActivity, CompareActivity, BudgetActivity, MapActivity
adapters/      → PriceAdapter, CompareAdapter, BudgetAdapter
models/        → Product, Price, Market, User, BudgetItem
utils/         → FirebaseHelper, DateUtils
res/layout/    → সব XML লেআউট
res/values/    → colors, strings, themes (Light + Dark)
```
