# 🚌 Janmarg BRTS Guide — Ahmedabad Route Planner & Live Walking Navigation

## Overview

A modern Android app for navigating the **Ahmedabad BRTS (Janmarg)** transit system. Powered by official **GTFS Schedule Data** (149 routes, 193 stations, 36,290 fare pairs, full daily timetables), with **GPS Nearest Station Auto-Detection** and **1-tap Google Maps Walking Navigation**.

---

## 🚀 Key Features

1. **📍 GPS Nearest Station Finder**:
   - Detects where you are standing in Ahmedabad city via GPS.
   - Calculates exact walking distance and estimated walk time to the closest BRTS station.
   - Auto-sets the closest station as your journey departure point.

2. **🚶 1-Tap Google Maps Walking Navigation**:
   - Tap **"Walk in Maps"** to launch Google Maps in turn-by-turn walking mode from your exact location directly to the BRTS station entrance.
   - Seamless transition: navigate to the station in Maps, board the bus, and check real-time routes in the BRTS app.

3. **🚌 Multi-Bus Route Planner**:
   - Discovers every possible direct bus line connecting your departure and destination stations.
   - Live **Next Bus countdown** (e.g. `Next bus in 4 min`) and fallback schedule (`If you miss this bus: 08:42, 08:50...`).
   - 1-transfer journey connections with interchange station guidance.

4. **🗺️ 149 Official BRTS Lines & 193 Stations**:
   - Complete line schedules (`1D`, `1U`, `1E`, `15D`, `101-C`, etc.).
   - Full station sequence timeline from origin to destination.
   - Official GTFS distance-based fare lookup (Adult ₹5 – ₹30, Child ₹3 – ₹15).
   - 100% offline database with instant sub-millisecond queries.

---

## 📁 Project Structure

```
AhmedabadBRTS/
├── AhmedabadBRTS.apk                         ← Ready-to-install debug APK (~19.1 MB)
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   └── brts.db                      ← SQLite database with official GTFS data
│   │   ├── java/com/brts/ahmedabad/
│   │   │   ├── MainActivity.kt              ← Entry point + bottom navigation flow
│   │   │   ├── data/
│   │   │   │   ├── Models.kt                ← GTFS models (Station, BusRoute, Journey, NextBus)
│   │   │   │   ├── BRTSRepository.kt        ← SQLite query engine & caching
│   │   │   │   └── LocationHelper.kt        ← GPS proximity & Google Maps walking intent
│   │   │   └── ui/
│   │   │       ├── screens/
│   │   │       │   ├── RoutePlannerScreen.kt ← Journey planner + GPS nearest station + Walk in Maps
│   │   │       │   ├── AllRoutesScreen.kt    ← 149 lines with timeline & daily timetables
│   │   │       │   ├── AllStationsScreen.kt  ← 193 stations with "Stations Near Me" + Maps
│   │   │       │   └── InfoScreen.kt         ← Fares, timings, tips
│   │   │       └── theme/                   ← Modern dark theme with BRTS brand palette
│   │   ├── AndroidManifest.xml              ← Location permissions & Google Maps queries
│   │   └── res/
│   └── build.gradle.kts
```
│   │   ├── AndroidManifest.xml
│   │   └── res/
│   │       └── values/
│   │           ├── strings.xml
│   │           └── themes.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/libs.versions.toml               ← Dependency catalog
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🛠 How to Build

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11+
- Android SDK 35

### Steps

1. Open Android Studio
2. Select **File → Open** 
3. Wait for Gradle sync to complete
4. Connect an Android device (API 24+) or launch an emulator
5. Click **Run ▶**

---

## ✨ Features

| Feature | Details |
|---|---|
| **Route Planner** | Select source & destination from scrolling picker, get direct or 1-transfer routes |
| **Bus Lines** | Shows which BRTS routes to take (e.g., R1, R8, R18) |
| **Fare Calculator** | Distance-based fare estimation (₹5–₹30) |
| **Bus Timings** | First bus, last bus, peak & off-peak frequency per route |
| **Transfer Info** | Clear step-by-step transfer instructions |
| **All Routes** | 18 routes with full station sequences, expandable |
| **All Stations** | 70+ stations with Gujarati names and route badges |
| **Info Guide** | Fare table, payment methods, travel tips |
| **100% Offline** | No internet needed, all data embedded |
| **Search** | Filter stations by English or Gujarati name |
| **Swap** | Swap source/destination with one tap |

---

## 🚌 BRTS Routes Included

| Route | Corridor |
|---|---|
| R1 | RTO Circle ↔ Naroda Patiya |
| R2 | Kankaria ↔ Aslali |
| R3 | Nehrunagar ↔ Ajit Mill |
| R4 | Sola Bridge ↔ Shivranjani |
| R5 | Jashodanagar ↔ Narol |
| R6 | Akhbarnagar ↔ Chandkheda |
| R7 | Naroda ST Workshop Shuttle |
| R8 | Iscon ↔ Stadium (Circular) |
| R9 | RTO ↔ South Bopal |
| R10 | Maninagar ↔ Nikol |
| R11 | Sabarmati ↔ Gandhinagar Highway |
| R12 | Nehrunagar ↔ Sarkari Vasahat |
| R13 | Juhapura ↔ Ellisbridge |
| R14 | Kalupur ↔ Ajit Mill |
| R15 | GMDC ↔ Sola Bridge |
| R16 | Soni Ni Chali ↔ Naroda Gaam |
| R17 | RTO ↔ Juhapura |
| R18 | Maninagar ↔ Drive-In Road |

---

## 💡 Routing Algorithm

The offline routing engine:
1. **Direct routes**: Finds any single route containing both source and destination
2. **1-transfer routes**: Finds pairs of routes with a common transfer station
3. **Ranking**: Direct first, then by total stops (shortest trip first)
4. Returns up to 5 best options

---

## 📊 Fare Structure

| Distance | Fare |
|---|---|
| Minimum | ₹5 |
| Per km | ~₹1.50 |
| Maximum | ₹30 |

Payment: Cash at station counter or **Janmitra Smart Card**

---

## ⏰ Service Hours

- **Operating**: 6:00 AM – 11:00 PM daily
- **Peak frequency**: Every 2–5 minutes (8–10 AM & 5–8 PM)
- **Off-peak**: Every 5–15 minutes

---

## 🎨 Design

- **Dark theme** with BRTS navy + yellow brand colors
- **Glassmorphism** card effects
- **Animated transitions** between screens
- **Gujarati names** for all stations
- **Material 3** components throughout
