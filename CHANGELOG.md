# Changelog

All notable changes to AutoDonut are recorded here.
This project follows [Semantic Versioning](https://semver.org/).

## [1.0.0] — unreleased

First release. **AutoDonut is a client-only mod: your server installs nothing.**

Targets **Minecraft Java 26.2** on Fabric Loader `>=0.19.0`, Java 21+.

### Automation (all client-side, all off by default)

- **Auto Miner** — two modes, *Only Mine* (stationary) and *Walk + Mine*
  (tunnels). Ore radar with a 2–32 block radius and per-ore toggles for coal,
  iron, copper, gold, redstone, lapis, diamond, emerald, nether quartz and
  ancient debris. Vein-clearing follows a seam through all six neighbours.
  Pickaxe durability guard stops before your tool breaks (it does **not**
  swap tools for you). Optional auto-torch, offered only when you're in
  Walk + Mine *and* actually carrying torches. Optional retraced walk back to
  your chest when your inventory fills. Session stats via `/autodonutmine stats`.
- **Auto Smelter** — tends ordinary vanilla Furnaces, Blast Furnaces and
  Smokers you've already placed: takes the output, tops up fuel, refills the
  input, closes. Inputs are an explicit allow-list, so it can never feed a
  furnace something valuable by accident.
- **Auto Farm** — harvests fully-grown wheat, carrots, potatoes, beetroot and
  nether wart within reach and replants from hotbar seeds. Tells you once and
  stops replanting when a seed runs out, rather than silently leaving holes.

All three clamp their radius to `player.blockInteractionRange()`, act at most
once per tick, and stand down when you open a GUI yourself.

### Economy

- Scans `/ah` listing tooltips into a local rolling-median price database
  (`config/autodonut_market.json`).
- Deal finder panel: underpriced listings, one click to re-verify then buy.
  No travel or navigation — you click, it buys.
- **Chest Sell** on any container you open, at *At market*, *Below market* or
  *Above market*, with a Do-Not-Sell list.
- Quick-sell keybind, watchlist alerts, shopping list, price trend arrows,
  profit ledger (`config/autodonut_ledger.json`), and price-DB export/import
  so friends can share data.
- Every command template and the price regex are user-editable, because the
  exact plugin format varies between servers. `dumplore` dumps raw tooltip
  text to help you match yours.

### Chat modifiers

Hide all player chat, hide scam-keyword messages (whole-word by default,
regex mode available), hide join/leave spam, and highlight mentions. Purely
local — it never changes what you send.

### Safety (all off by default)

Auto-eat, threat-pause with an optional Discord webhook, and an anti-AFK jump
nudge that stays quiet while any automation is already running.

### Interface

Native settings screens reachable from the `K` keybind (rebindable) or
`/autodonutmenu`, with optional Mod Menu integration. Client commands:
`/autodonut`, `/autodonutmine`, `/autodonutsmelt`, `/autodonutfarm`,
`/autodonuteconomy`, `/autodonutchat`, `/autodonutmenu`.

### AI advisor (optional)

`/autodonut ai setkey|forgetkey|baseurl|model|ask` against any
OpenAI-compatible endpoint. **With no key set, AutoDonut makes no network
requests at all.** The key is stored only in `config/autodonut.json`.

### Distribution

`AutoDonut-Installer.jar` — a double-clickable Swing installer that finds
`.minecraft`, installs the mod plus Fabric API, and can set your AI key.

### Notes on scope

- **No mixins, no packet forging, no reach extension, no anti-cheat bypass.**
  Every action is one a player can perform by hand. The complete list of
  external APIs the mod touches is in [`API-SURFACE.md`](API-SURFACE.md).
- Built for self-hosted servers you own and administrate. Not affiliated with
  DonutSMP.

### Known limitations

- **`./gradlew build` has not been run against the real Minecraft artifacts
  yet.** The source is fully type-checked against API stubs
  (`tools/offline-typecheck/`), which proves it is internally consistent but
  not that every external signature is spelled right for 26.2. See
  [Verification](README.md#-verification).
- The `/ah` command templates and price regex are educated defaults, not
  verified against a live auction-house plugin.
- Auto-relist is not implemented; it needs a confirmed AH lore format first.
- Session stats are printed to chat rather than drawn as a HUD overlay.
- Auto Smelter services one furnace at a time, not a furnace array.
