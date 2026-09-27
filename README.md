# 🍩 AutoDonut

<p align="center">
  <img src="src/main/resources/assets/autodonut/icon.png" alt="AutoDonut Icon" width="128" height="128" />
</p>

<p align="center">
  <strong>100% Client-Side Mining, Smelting, Farming, Economy, Safety & Chat Tools</strong>
  <br />
  <em>Built for Minecraft Java Edition 26.2 on the Fabric Loader</em>
  <br />
  <strong>Install it on your client only — your server stays completely vanilla.</strong>
</p>

<p align="center">
  <a href="https://github.com/CoolgamerProXD/AutoDonut/actions"><img src="https://img.shields.io/github/actions/workflow/status/CoolgamerProXD/AutoDonut/build.yml?branch=main&style=flat-square&label=Build" alt="Build Status" /></a>
  <a href="https://fabricmc.net/"><img src="https://img.shields.io/badge/Minecraft-26.2-blue?style=flat-square&logo=minecraft" alt="Minecraft 26.2" /></a>
  <a href="https://fabricmc.net/"><img src="https://img.shields.io/badge/Fabric%20Loader-%3E%3D0.19.0-dbcfb3?style=flat-square" alt="Fabric Loader" /></a>
  <a href="https://www.oracle.com/java/"><img src="https://img.shields.io/badge/Java-21%2B%20%7C%2025%20recommended-orange?style=flat-square&logo=openjdk" alt="Java 21+" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-CC0--1.0-green?style=flat-square" alt="License: CC0-1.0" /></a>
  <img src="https://img.shields.io/badge/Server%20install-not%20required-brightgreen?style=flat-square" alt="No server install required" />
</p>

---

## 📖 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
  - [⛏️ Auto Miner](#️-auto-miner)
  - [🔥 Auto Smelter](#-auto-smelter)
  - [🌾 Auto Farm](#-auto-farm)
  - [💰 Economy & Chest Sell Utilities](#-economy--chest-sell-utilities)
  - [🛡️ Safety Utilities](#️-safety-utilities)
  - [💬 Chat Modifiers & Filters](#-chat-modifiers--filters)
  - [🤖 AI Survival Advisor](#-ai-survival-advisor)
- [Requirements](#-requirements)
- [Installation Guide](#-installation-guide)
- [Command Reference](#-command-reference)
- [Building from Source](#-building-from-source)
- [Verification](#-verification)
- [Architecture & Design Principles](#-architecture--design-principles)
- [License](#-license)

---

## 🌟 Overview

**AutoDonut** is a **100% client-side** Fabric mod for **Minecraft Java 26.2** that automates the boring, repetitive parts of survival play — mining, smelting, farming, and market trading — on a server you own.

**You install it on your own computer. Your server installs nothing.** That means it runs fine on weak, low-RAM server hardware, and your friends can join with or without it.

Every action AutoDonut performs is one you could physically do by hand: it uses real block-break timing, real right-click interactions, real container clicks, and real movement input. There is **no packet forging, no reach extension, and no anti-cheat bypass** anywhere in the codebase.

> ⚠️ **Disclaimer:** AutoDonut is built for self-hosted servers you own and administrate for you and your friends. It is **not** affiliated with DonutSMP. If a server you play on forbids automation, don't use it there.

---

## ✨ Key Features

> Every feature below is **client-side only**. Open the settings hub with **`K`**, `/autodonutmenu`, or the Mods button if you have [Mod Menu](https://modrinth.com/mod/modmenu) installed.

### ⛏️ Auto Miner

*Uses real player mining packets and block-hardness timers.*

- **Modes:**
  - **Only Mine:** Stationary reach-based mining (safe auto-breaker).
  - **Walk + Mine:** Automated 1x2 tunnel excavation with obstacle and hazard detection (stops on liquids, drops, or bedrock).
- **Ore Radar & Target Filtering:** Proactively scans nearby ores (configurable 2–32 block radius) and prioritizes them. Selectively toggle target ores: Coal, Iron, Copper, Gold, Redstone, Lapis, Diamond, Emerald, Nether Quartz, Ancient Debris.
- **Vein-Clearing:** Detects adjacent connected ore blocks in all 6 directions to ensure full veins are harvested before resuming tunnel path.
- **Pickaxe Durability Guard:** Monitors held tool durability and halts mining with an audio/chat warning before the tool breaks (manual swap preserved by design).
- **Smart Torch Placement:** Places torches on tunnel walls dynamically. *Option is dynamically displayed only when Walk+Mine mode is active and torches are present in inventory.*
- **Retraced Return-to-Chest:** Records forward movement path; when inventory fills, retraces steps back to a designated chest at the start point, shift-click deposits items, and retraces forward to resume mining.
- **Session Stats:** Track total blocks mined and per-ore breakdown via `/autodonutmine stats` or GUI.

---

### 🔥 Auto Smelter

*Tends the ordinary vanilla furnaces already in your world — it does not add a machine.*

Stand near a **Furnace**, **Blast Furnace**, or **Smoker**. Every few seconds AutoDonut opens it, pulls the finished output into your inventory, tops up the fuel and input slots from your inventory, and closes it again — exactly the clicks you'd make by hand.

- **Works on any vanilla furnace**, including ones you built before installing the mod.
- **Allow-listed inputs.** AutoDonut will only feed a furnace items on an explicit list, so it can never shove your netherite into a smoker by accident. Fully editable in the settings screen.
- **Configurable fuel list** — coal, charcoal, coal blocks, blaze rods and dried kelp blocks by default.
- **Reach-clamped.** The furnace must be inside your real interaction range; the search radius is capped to it automatically.
- **Never fights you.** If you open a GUI yourself, the smelter backs off until you're done.
- **Session stats** via `/autodonutsmelt stats`.

---

### 🌾 Auto Farm

*Harvests and replants the ordinary vanilla crops around you — it does not add a machine.*

Stand in your field, or walk around it, and AutoDonut keeps it harvested.

- **Supported crops:** wheat, carrots, potatoes, beetroot, and nether wart.
- **Automatic replanting** from seeds in your hotbar, with the correct seed matched to each crop. It temporarily selects the seed slot and switches straight back.
- **Reach-clamped.** Only crops inside your real interaction range are ever touched — the radius setting is capped to it.
- **Adjustable pace** (`1–20` ticks between actions) so it can be as gentle or as brisk as you like.
- **Yields to the Auto Miner** and to any GUI you open, so the two never fight over block-breaking.
- **Session stats** via `/autodonutfarm stats`.

---

### 💰 Economy & Chest Sell Utilities

*Designed for auction house (`/ah`) and container selling workflows.*

- **AH Scanner & Deal Finder:** Scans visible auction listings, tracks passive market value history in `config/autodonut_market.json`, and displays deals under market value with one-click purchasing.
- **Chest Quick-Sell:** Open any chest/container and sell contents at customizable price tiers (**At Market**, **Below Market**, or **Above Market**). Automatically stages items into slot 9 and handles commands.
- **Quick-Sell Keybind:** Configurable hotkey to sell held items immediately via auction command or flat `/sell` regex learning.
- **Watchlist & Price Alerts:** Configure target buy prices with cooldown-throttled chat alerts (`/autodonuteconomy watch`).
- **Shopping List (Want-List):** Tracks target purchase quantities and decrements automatically as buy-flows complete.
- **Price Trend Indicators:** Visual trend arrows (rising, falling, flat) computed from observed listing history.
- **Profit Ledger:** Tracks cumulative revenue, expenditures, and net profits across sessions (`/autodonuteconomy ledger`).
- **Shared Market Database:** Export (`sharedb export <name>`) and import (`sharedb import <name>`) price history files to synchronize market data with friends.

---

### 🛡️ Safety Utilities

*100% client-side safety triggers (disabled by default).*

- **🍖 Auto-Eat:** Detects low hunger thresholds, switches to hotbar food, consumes it, and restores previous hotbar selection.
- **🚨 Threat-Pause & Webhook:** Pauses all active automations when hostile entities or players enter a danger radius, sending an optional alert to a configured Discord webhook.
- **⏱️ Anti-AFK Nudge:** Issues subtle randomized jump actions every 3–5 minutes during idle states to prevent AFK kicks.

---

### 💬 Chat Modifiers & Filters

- **🚫 Anti-Scam Filter:** Client-side filtering for spam/scam keywords (e.g. "tpa scam", teleport traps) with optional regex matching.
- **🔕 Mute Player Chat:** Toggle off general player chat while preserving essential system/server notices.
- **🚪 Hide Join/Leave Messages:** Suppresses connection spam on busy servers.
- **⭐ Mention Highlighting:** Echoes a distinct visual marker when your username is mentioned in signed chat.

---

### 🤖 AI Survival Advisor

- Context-aware survival assistant integrated directly into in-game chat (`/autodonut ai ask <question>`).
- Supports OpenAI API keys and custom OpenAI-compatible endpoints/models (e.g. Ollama, OpenRouter, LocalAI) configured in `config/autodonut.json`.

---

## 📋 Requirements

| Component | Requirement |
|---|---|
| **Minecraft** | Java Edition `26.2` |
| **Mod Loader** | [Fabric Loader](https://fabricmc.net/) `0.19.0+` |
| **Dependencies** | [Fabric API](https://modrinth.com/mod/fabric-api) `0.161.0+26.2` |
| **Java Runtime** | Java 21+ (Java 25 recommended) |
| **Optional** | [Mod Menu](https://modrinth.com/mod/modmenu) for in-game configuration GUI |

---

## 🚀 Installation Guide

> **Install on your own computer only.** Do not touch your server — AutoDonut needs nothing there. Friends who want the same tools install it on their own machines; friends who don't can play alongside you with plain vanilla Fabric.

### Option 1: GUI Installer (Recommended)
1. Download `AutoDonut-Installer.jar` from the [Releases](https://github.com/CoolgamerProXD/AutoDonut/releases) page.
2. Double-click the jar (or run `java -jar AutoDonut-Installer.jar`).
3. Pick **Client** and confirm your `.minecraft` directory.
4. (Optional) Enter your AI API key.
5. Click **Install AutoDonut**.

### Option 2: Manual Installation
1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 26.2.
2. Place `autodonut-1.0.0.jar` and `fabric-api-0.161.0+26.2.jar` into your `.minecraft/mods` directory.
3. Launch Minecraft with the Fabric profile.

### First run
Press **`K`** in-game to open the settings hub, then enable whichever modules you want. Everything is **off by default**.

---

## ⌨️ Command Reference

<details>
<summary><strong>Click to expand full command table</strong></summary>

### 🔧 AI Advisor & Legacy Server Commands

> These require the mod to also be installed **on the server**, which you almost certainly don't want. The `ai` subcommands are the only ones most people need.

```bash
/autodonut status                         # Show current mod configuration
/autodonut automate <true|false>          # LEGACY: server-block master toggle
/autodonut machine smelter <true|false>   # LEGACY: toggle Auto Smelter block
/autodonut machine farm <true|false>      # LEGACY: toggle Auto Farm block
/autodonut machine miner <true|false>     # LEGACY: toggle Auto Miner quarry block
/autodonut kit                            # LEGACY: receive one of each block
/autodonut ai setkey <key>                # Configure AI advisor API key
/autodonut ai baseurl <url>               # Configure AI API endpoint URL
/autodonut ai model <name>                # Set AI model (default: gpt-4o-mini)
/autodonut ai ask <question>              # Query the AI survival advisor in chat
```

### ⛏️ Client Miner Commands
```bash
/autodonutmine                            # Show current mining state and active settings
/autodonutmine <true|false>               # Toggle client-side auto mining on/off
/autodonutmine mode mineonly              # Set mode to stationary block breaking
/autodonutmine mode walkmine              # Set mode to automated 1x2 tunnel mining
/autodonutmine radar <true|false>         # Enable or disable ore-seeking radar
/autodonutmine radius <2-32>              # Adjust ore scanning radius (in blocks)
/autodonutmine ore list                   # View status of all targetable ores
/autodonutmine ore <name> <true|false>    # Enable/disable specific ore (e.g. diamond)
/autodonutmine stats                      # Print session blocks and ore breakdown
/autodonutmine stats reset                # Reset session mining stats
```

### 🔥 Auto Smelter Commands
```bash
/autodonutsmelt                           # Show current smelter state and settings
/autodonutsmelt <true|false>              # Toggle the client-side Auto Smelter
/autodonutsmelt interval <1-60>           # Seconds between furnace service runs
/autodonutsmelt radius <1-6>              # Furnace search radius (clamped to real reach)
/autodonutsmelt fuel                      # List accepted fuel items
/autodonutsmelt fuel add <item>           # Add a fuel item (e.g. minecraft:coal)
/autodonutsmelt fuel remove <item>        # Remove a fuel item
/autodonutsmelt input                     # List allow-listed smeltable inputs
/autodonutsmelt input add <item>          # Allow an item to be fed into furnaces
/autodonutsmelt input remove <item>       # Disallow an item
/autodonutsmelt stats                     # Print session smelting stats
/autodonutsmelt stats reset               # Reset session smelting stats
```

### 🌾 Auto Farm Commands
```bash
/autodonutfarm                            # Show current farm state and settings
/autodonutfarm <true|false>               # Toggle the client-side Auto Farm
/autodonutfarm radius <1-6>               # Harvest radius (clamped to real reach)
/autodonutfarm replant <true|false>       # Replant from hotbar seeds after harvesting
/autodonutfarm netherwart <true|false>    # Include fully-grown nether wart
/autodonutfarm speed <1-20>               # Ticks between harvest actions
/autodonutfarm stats                      # Print session harvest stats
/autodonutfarm stats reset                # Reset session harvest stats
```

### 💰 Economy Commands
```bash
/autodonuteconomy                         # View economy module status
/autodonuteconomy <true|false>            # Master toggle for Economy & Chest Sell
/autodonuteconomy dumplore                # Output tooltip text of hovered inventory items
/autodonuteconomy clearmarket             # Reset cached price history database
/autodonuteconomy dns add <item>          # Add item to Do-Not-Sell blacklist
/autodonuteconomy dns remove <item>       # Remove item from Do-Not-Sell blacklist
/autodonuteconomy dns                     # List all blacklisted items
/autodonuteconomy watch add <item> <max>  # Add price alert for item at or below target
/autodonuteconomy watch remove <item>     # Remove price alert
/autodonuteconomy watch                   # View active watchlist
/autodonuteconomy want add <item> <qty>   # Add item to shopping list with target quantity
/autodonuteconomy want remove <item>      # Remove item from shopping list
/autodonuteconomy want                    # View current shopping list
/autodonuteconomy ledger                  # View total spent, earned, and net session profit
/autodonuteconomy ledger clear            # Clear profit ledger history
/autodonuteconomy sharedb export <name>   # Export price database to config/<name>.json
/autodonuteconomy sharedb import <name>   # Merge price database from config/<name>.json
```

### 💬 Chat Filter Commands
```bash
/autodonutchat                            # Display current chat filter configuration
/autodonutchat hideplayers <true|false>   # Toggle hiding all player chat messages
/autodonutchat hidescam <true|false>      # Toggle anti-scam keyword filter (default: ON)
/autodonutchat keyword add <word>         # Add keyword to scam filter list
/autodonutchat keyword remove <word>      # Remove keyword from scam filter list
/autodonutchat keyword                    # List all active scam filter keywords
```

</details>

---

## 🔨 Building from Source

### Prerequisites
- JDK 21 or JDK 25 installed
- Git

### Build Steps

```bash
# Clone the repository
git clone https://github.com/CoolgamerProXD/AutoDonut.git
cd AutoDonut

# Build the Fabric mod jar
./gradlew build

# Built artifacts will be in build/libs/:
# - build/libs/autodonut-1.0.0.jar
# - build/libs/autodonut-1.0.0-sources.jar
```

---

## 🔍 Verification

| Check | Status |
|---|---|
| Type-check of all 40 sources | ✅ **zero errors** |
| Installer compile (plain JDK) | ✅ passes |
| Warning pass (dead code, null deref, unused, fallthrough, leaks) | ✅ clean |
| `./gradlew build` against the real game | ⏳ not yet run |

The type-check is performed against hand-written stubs of the Minecraft and
Fabric APIs, because the development machine could not reach Maven Central.
That proves every line of AutoDonut's own code is internally consistent and
correctly typed; it does not prove every external signature is spelled right
for 26.2. **[`API-SURFACE.md`](API-SURFACE.md)** lists all 131 external types
and 433 members so that assumption is reviewable rather than hidden, and the
harness is checked in at [`tools/offline-typecheck/`](tools/offline-typecheck/).

```bash
ECJ_JAR=/path/to/ecj.jar ./tools/offline-typecheck/run.sh
```

`./gradlew build` remains the authoritative build.

## 🏛️ Architecture & Design Principles

1. **Strict Client-Side Legitimacy:** Client-side automations (Auto Miner, Quick-Sell, Auto-Eat) rely on real game client interaction routines (`MultiPlayerGameMode.useItemOn`, standard input injection, and client tick events). They deliberately avoid illegal instant packet injection or impossible inventory actions with closed screens.
2. **Modular Configuration:** All modules are independently toggleable via `config/autodonut.json` or the Mod Menu GUI.
3. **Zero Hard Dependencies on Server:** Client modules degrade gracefully and operate in 100% unmodded vanilla multiplayer environments.

---

## 📄 License

This project is released under the [Creative Commons Zero v1.0 Universal](LICENSE) (CC0-1.0) Public Domain Dedication.
