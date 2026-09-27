<h1 align="center">🍩 AutoDonut</h1>

<p align="center">
  <strong>A Fabric mod for Minecraft Java 26.2 — client-side mining, economy, safety and chat tools, plus optional server-side automation blocks.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-26.2-blue?style=flat-square&logo=minecraft" alt="Minecraft 26.2" />
  <img src="https://img.shields.io/badge/Loader-Fabric-dbcfb3?style=flat-square" alt="Fabric" />
  <img src="https://img.shields.io/badge/Java-21%2B%20(25%20recommended)-orange?style=flat-square&logo=openjdk" alt="Java 21+" />
  <img src="https://img.shields.io/badge/status-rebuilding-yellow?style=flat-square" alt="Status: rebuilding" />
</p>

---

> ### ⚠️ Repository status
>
> **The source code is not in this repository yet.** A working build existed in a
> previous development sandbox but was lost before it could be pushed.
>
> **[📄 `HANDOFF.md`](HANDOFF.md)** contains the complete rebuild brief: the full
> feature spec, the intended project layout, and a large set of **verified Minecraft
> 26.2 / Fabric API notes** gathered by decompiling the real game jar. Paste the
> `## THE PROMPT` section into a fresh coding-agent session to continue the project.

---

## What AutoDonut is

AutoDonut is built for a **self-hosted survival server you own and administrate**.
It comes in two halves:

### 🖥️ Client-side only — no server install needed

Works on a fully vanilla server. Everything acts through legitimate client input
paths (real block-break timing, real movement input, real container-click packets).

| Module | What it does |
|---|---|
| ⛏️ **Auto Miner** | *Only Mine* or *Walk + Mine* tunnelling, ore radar with per-ore toggles, vein clearing, durability guard, conditional auto-torch, return-to-chest when full, session stats |
| 💰 **Economy & Chest Sell** | Scans `/ah` listings into a local price database, flags below-market deals for one-click buying, sells whole chests at *At / Below / Above* market tiers, watchlist, want-list, profit ledger, shared price DB |
| 🛡️ **Safety** | Auto-eat, threat-pause with optional Discord webhook, anti-AFK nudge — all off by default |
| 💬 **Chat Modifiers** | Hide player chat, whole-word scam-keyword filter (catches system broadcasts too), hide join/leave spam, mention highlighting |

### 🧱 Server-side blocks — optional

Real in-world machines, in the spirit of Mekanism or Applied Energistics. These
require the mod on the server **and** every client.

- **Auto Smelter** — instant fuel-free smelting, hopper compatible
- **Auto Farm** — harvests and replants crops in a configurable radius
- **Auto Miner (quarry)** — fuel-powered downward shaft, skips bedrock and liquids

### 🤖 AI advisor

`/autodonut ai ask <question>` against OpenAI or any OpenAI-compatible endpoint.
Your key stays local in `config/autodonut.json`.

---

## Requirements

| Component | Version |
|---|---|
| Minecraft | Java Edition **26.2** |
| Mod loader | Fabric Loader 0.19.0+ |
| Dependency | Fabric API 0.161.0+26.2 |
| Java runtime | 21+ (25 recommended) |
| Optional | [Mod Menu](https://modrinth.com/mod/modmenu) for the in-game config GUI |

---

## Scope & fair use

AutoDonut is intended for **your own server**. It is not affiliated with DonutSMP or
any other public server, and it contains nothing that bypasses anti-cheat, fakes
impossible game states, or injects packets a real player couldn't produce. If a
server you play on prohibits automation, don't use it there.

---

## License

[CC0-1.0](LICENSE) — public domain dedication. *(License file to be added with the source.)*
