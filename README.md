# 🍏 MacNetMeter & DevCockpit

<p align="center">
  <img src="https://img.shields.io/badge/Platform-macOS%20(Apple%20Silicon%20M1%2FM2%2FM3)-black?style=for-the-badge&logo=apple" alt="macOS Apple Silicon">
  <img src="https://img.shields.io/badge/Java-21%20LTS-orange?style=for-the-badge&logo=openjdk" alt="Java 21">
  <img src="https://img.shields.io/badge/Build-Maven%20Wrapper%20(%20.%2Fmvnw%20)-blue?style=for-the-badge&logo=apachemaven" alt="Maven Wrapper">
  <img src="https://img.shields.io/badge/Packaging-jpackage%20(Standalone%20.app)-green?style=for-the-badge" alt="jpackage Standalone">
  <img src="https://img.shields.io/badge/License-MIT-purple?style=for-the-badge" alt="License MIT">
</p>

> **A lightweight, blazing-fast, and crystal-clear Menu Bar utility designed exclusively for macOS developers and power users.**  
> Monitor live Network speeds, CPU/RAM resource hogs, track total kernel bandwidth, protect cellular hotspot data, and prevent your Mac from sleeping — all in one native, standalone macOS application!

---

## 📸 Preview (شكل التطبيق في شريط المهام)

[ ☕ 🛡️  ↓ 1.4 MB/s  ↑ 220 KB/s  │  2.45 GB ]   [ ⚡ 14% │ 🧠 8.2G ]
---

## 🌟 Key Features (المميزات الرئيسية)

### 🌐 1. Advanced Network Telemetry
* **Real-Time Speeds:** Crystal-clear Download (`↓`) and Upload (`↑`) rates rendered on a 3x Retina canvas using Apple's native system typography.
* **Fluid Dynamic Layout:** Advanced font-metrics engine that dynamically auto-resizes the widget to prevent text overlap or clipping regardless of speed fluctuations.
* **Kernel-Level Hardware Traffic:** Directly measures raw cumulative network data from physical network interfaces (Ethernet / Wi-Fi) since midnight and since system boot — 100% independent of app restarts or quota resets.
* **Live App Traffic Inspector:** Pinpoint exactly which running apps (`Chrome`, `Docker`, `Slack`, `Telegram`, etc.) are consuming network bandwidth in real-time.

### 🛡️ 2. Hotspot Data Saver & Smart Firewall
* **Tethering Protection:** Activate `🛡️ Hotspot Data Saver` with a single click when connected to a cellular hotspot.
* **Permanent Rules Engine:** Block data-hungry background cloud syncs (`Dropbox`, `OneDrive`, `Photos Sync`) with one click (`🔴 Block on Hotspot`).
* **Instant Traffic Enforcement:** If a blacklisted application attempts to transmit data while Data Saver is active, it is terminated immediately, accompanied by an alert.
* **Persistence:** Whitelisted and blacklisted rules are remembered permanently across macOS reboots using system preferences.

### ⚡ 3. Modular CPU & Memory Taskmaster
* **Activity Monitor at a Glance:** Dedicated, independent menu bar widget showing real-time CPU load percentage and RAM usage (`⚡ 18% │ 🧠 8.4G`).
* **Top 5 Resource Hogs:** Instant dropdown displaying the top 5 CPU-intensive and RAM-heavy processes.
* **One-Click Force Kill:** Terminate hung or runaway processes (`❌ Force Kill`) without opening Terminal or Activity Monitor.
* **Inactive RAM Purge:** One-click memory cleaner (`🧹 Purge Inactive RAM`).
* **Show/Hide Toggle:** Can be completely hidden from the menu bar to save space when not needed.

### ☕ 4. Caffeinated (Keep-Awake Engine)
* **Prevent Sleep & Dimming:** Integrated macOS `caffeinate` controller to keep your display awake during downloads or long compiles.
* **Pixel-Perfect Vector Icon:** Displays a sharp, hand-drawn vector amber coffee cup (`☕`) with rising steam when active.

### 🔔 5. Smart Quota Alerts & Master Controls
* **Configurable Daily Limits:** Set daily bandwidth caps (e.g., 500 MB, 1 GB, 2 GB, 5 GB, or custom MB input).
* **Persistent Modal Alert Box:** Critical native macOS warning dialog with audio alerts (`Sosumi.aiff`) that stays on-screen until acknowledged.
* **Master Pause Mode:** Put monitoring on hold (`⏸ Paused`) to dim all counters into a faded gray mode and stop background polling.
* **Display Modes:** Seamlessly toggle between:
    1. `Full Dashboard` (Speeds + Daily Total)
    2. `Speeds Only` (Minimalist speeds)
    3. `Today Usage` (Compact daily total)
    4. `Minimal Dot` (Subtle active indicator)

---

## 🏗️ Architecture & Technology Stack

* **Language:** Java 21 LTS
* **System Metrics:** [OSHI (Operating System and Hardware Information)](https://github.com/oshi/oshi)
* **Packaging:** Official OpenJDK `jpackage` (Bundles its own ultra-lightweight, stripped runtime).
* **Architecture Support:** Native **Apple Silicon (M1/M2/M3)** & Intel x86_64.
* **Zero Runtime Dependencies:** Does **NOT** require Java to be installed on the user's machine.

```text
src/main/java/com/maknoon/
├── App.java                     # Application Lifecycle & Settings Orchestration
├── model/
│   ├── AppNetUsage.java         # Per-Process Network Model
│   └── DisplayMode.java         # UI Modes Enum
├── service/
│   ├── AlertService.java        # Quota Monitoring & Native AppleScript Dialogs
│   ├── AppNetworkService.java   # Native NetTop Poller Engine
│   ├── CaffeinateService.java   # Keep-Awake Process Controller
│   ├── CpuMemoryService.java    # CPU & RAM Metrics & Process Killer
│   ├── DataSaverService.java    # Hotspot Rules & Enforcement Engine
│   ├── MacSystemNetworkService.java # Raw Kernel Hardware Counters
│   └── SystemInfoService.java   # Mac Uptime & OS Information
└── ui/
    ├── CpuMenuBuilder.java      # Taskmaster Context Menu
    ├── CpuWidgetRenderer.java   # CPU/RAM Retina Renderer
    ├── MenuBuilder.java         # Main Dashboard Context Menu
    └── WidgetRenderer.java      # High-DPI Fluid Vector Menu Bar Engine

---

## 🚀 Building & Running Locally

### Prerequisites
- macOS Sonoma or later (Apple Silicon M1/M2/M3 recommended).
- Java 21 (Temurin, Corretto, or GraalVM).

### 🛠️ One-Click Build Script
We include a dedicated, automated build script `build-app.sh` that compiles the code and packages it into a native `.app` bundle in seconds:

```bash
# 1. Give execution permission
chmod +x build-app.sh

# 2. Run the one-click build script
./build-app.sh
```

### ⚡ Manual Build Steps
Alternatively, you can build manually using the Maven Wrapper:

```bash
# 1. Compile Fat JAR
./mvnw clean package

# 2. Package into Standalone Mac App
jpackage \
  --type app-image \
  --input target \
  --name MacNetMeter \
  --main-jar MacNetMeter-1.0.0-jar-with-dependencies.jar \
  --main-class com.maknoon.App \
  --dest dist \
  --java-options "-Dapple.awt.UIElement=true"
```

### 🏃‍♂️ Running the Application
Launch the standalone application:
```bash
open dist/MacNetMeter.app
```
*Or simply drag `MacNetMeter.app` into your macOS `/Applications` folder!*

---

## 🤖 Continuous Integration & GitHub Actions (CI/CD)

The repository is equipped with a fully automated GitHub Actions workflow (`.github/workflows/build-mac.yml`):
- Runs on official **`macos-14` (Apple Silicon M1/M2)** runners.
- Builds the application automatically on every `git push`.
- Packages and uploads `MacNetMeter-macOS-AppleSilicon.zip`.
- Automatically publishes a new release under the **Releases** tab when a version tag (e.g. `v1.0.0`) is pushed!

---

## 💡 Pro-Tip for macOS Users
- **Rearrange Icons:** Hold `Command (⌘)` and drag the widgets anywhere you like on your Menu Bar (next to Wi-Fi, Clock, or Battery).
- **Auto-Start on Login:** Add `MacNetMeter.app` to **System Settings ➔ General ➔ Login Items** to run automatically when your Mac boots.

---

## 📄 License
This project is open-source and licensed under the **MIT License** — free to use, modify, and distribute.

---
