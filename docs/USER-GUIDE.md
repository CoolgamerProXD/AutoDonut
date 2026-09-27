# AutoDonut

A **100% client-side** automation mod for **Minecraft Java 26.2**, built on
Fabric. Auto Miner, Auto Smelter, Auto Farm, an economy/chest-sell helper,
chat filters, safety tools and an optional AI advisor.

**Your server installs nothing.** It can stay completely vanilla. Every
feature works by performing the same actions you could perform by hand -
walking, looking at a block, breaking it, clicking a slot in a GUI - just
without you having to sit there doing it.

Made for a self-hosted server you own and run for you and your friends. It
is **not affiliated with DonutSMP**, and it does not bypass anti-cheat,
extend your reach, fake packets or see through walls. Don't use it to break
another server's rules.

## What's inside

| File | What it's for |
|---|---|
| `AutoDonut-Installer.jar` | **Double-click this.** A small installer app that copies the mod into your `.minecraft` folder (client) or your server folder, and lets you set your AI key. |
| `mods/autodonut-1.0.0.jar` | The mod itself, if you'd rather drag-and-drop it manually. |
| `mods/fabric-api-0.161.0+26.2.jar` | Required dependency (Fabric API). |
| `autodonut-mod-source.zip` | Full Java source + Gradle project, in case you want to tweak or rebuild it yourself. |

## Requirements

| | |
|---|---|
| **Minecraft** | Java Edition **26.2** |
| **Fabric Loader** | On your client. Get it at https://fabricmc.net/use/ (the installer has a button for it) |
| **Fabric API** | Bundled with the installer |
| **Java** | 21 or newer (25 recommended) |
| **Server** | **Nothing at all.** Your server stays completely vanilla. |
| **Mod Menu** | Optional. Only adds a settings button to the Mods screen - the `K` keybind and the chat commands work without it. |

## Installing

**Easiest way:** double-click `AutoDonut-Installer.jar`. Double-click works
because you already have a JVM installed to play Minecraft. If your system
doesn't associate `.jar` files, run `java -jar AutoDonut-Installer.jar`.

In the installer:
1. Pick **client** - your `.minecraft` folder. This is all you need.
2. Optionally paste an AI API key (OpenAI, or any OpenAI-compatible
   endpoint). Leave it blank and set it later in-game if you prefer.
3. Click **Install AutoDonut**. Do this once per computer - you and each
   friend who wants it. Nothing is installed on the server.

There is also a **server** option in the installer. You only need it for the
legacy blocks described near the bottom of this guide, and you almost
certainly don't.

**Manual way:** copy both jars from the `mods/` folder into the `mods`
folder inside `.minecraft`.

**First run:** launch the game, join your server, and press **K** to open
AutoDonut Settings. Everything is off by default - nothing starts running
until you switch it on.

## Chat Modifiers (100% client-side)

Unlike the automation blocks, this part only affects what **you personally
see** in chat. It doesn't need to be on the server, doesn't touch what you
send, and doesn't need op/admin permission — anyone can use it on any
server. Controlled with `/autodonutchat` or **AutoDonut Settings > Chat
Modifiers**:

```
/autodonutchat                              Show current chat filter settings
/autodonutchat hideplayers <true|false>     Hide ALL player chat messages
/autodonutchat hidescam <true|false>        Hide messages containing a scam keyword (default: ON)
/autodonutchat keyword add <word>           Add a word to the scam-keyword filter
/autodonutchat keyword remove <word>        Remove a word from the filter
/autodonutchat keyword                      List current scam keywords
```

By default, `hidescam` is **on** and filters out any chat or server message
containing the whole word "tp" (matches "tp" on its own, not inside other
words like "step") — useful since "meet me at spawn, tp me" / "tpa scam"
style messages are a very common trick on economy servers. Add more
keywords any time, e.g. `/autodonutchat keyword add duplicate`.

`hideplayers` is a blunt "silence everyone" switch for when you're AFK at
your farms and just don't want chat noise — it only hides normal player
chat, not server/system messages.

Three extra checkboxes live in the GUI (no chat command yet, GUI only):

- **Treat scam keywords as regex** — off by default (plain whole-word
  match). Turn on if you want a keyword entry like `t+p+a?` to be compiled
  as a real regular expression instead of matched literally.
- **Hide join/leave spam** — hides the vanilla " joined the game" / " left
  the game" messages. Off by default. This is a plain substring check, not
  translation-key based, so it only catches the default vanilla phrasing.
- **Highlight mentions** — on by default. Minecraft's signed chat protocol
  means a mod can't rewrite/recolor another player's message text, so
  instead, whenever a message contains your name, AutoDonut echoes an extra
  yellow `>> you were mentioned above <<` line right after it.

## Auto Smelter (100% client-side)

Tends **ordinary vanilla furnaces** that you have already placed and lit -
Furnace, Blast Furnace and Smoker. There is no AutoDonut block involved and
nothing is installed on the server.

Every few seconds it looks for a furnace within reach, and if it finds one:

1. Opens it, exactly as if you had right-clicked it.
2. Takes any finished item out of the output slot.
3. Tops the fuel slot up from your inventory.
4. Puts more raw material into the input slot.
5. Closes it again.

You keep your normal fuel economy - this does **not** smelt for free. It
just saves you standing there shuffling stacks.

**Settings** (`K` > Auto Smelter, or `/autodonutsmelt`)

| Setting | Default | What it does |
|---|---|---|
| Enabled | off | Master switch |
| Interval | 5s | How often to service a furnace |
| Radius | 4 | Blocks to search, clamped to your real reach |
| Take output | on | Pull finished items out |
| Insert fuel | on | Refill the fuel slot |
| Insert input | on | Refill the input slot |
| Fuel list | coal, charcoal, coal block, blaze rod, dried kelp block | What counts as fuel |
| Input list | 17 common ores/foods | **Allow-list.** Only these get fed in |

The input list is an explicit allow-list on purpose. AutoDonut will never
feed a furnace something just because it *could* be smelted, so it can't
quietly cook your diamond gear or a stack of something you were saving.

```
/autodonutsmelt                      Show current settings
/autodonutsmelt on|off               Turn it on or off
/autodonutsmelt interval <1-60>      Seconds between visits
/autodonutsmelt radius <1-6>         Search radius
/autodonutsmelt fuel add <item>      Add to the fuel list
/autodonutsmelt fuel remove <item>   Remove from the fuel list
/autodonutsmelt input add <item>     Add to the input allow-list
/autodonutsmelt input remove <item>  Remove from the input allow-list
/autodonutsmelt stats                Items smelted this session
/autodonutsmelt stats reset          Reset the counters
```

## Auto Farm (100% client-side)

Harvests **fully-grown vanilla crops** around you and replants them from
seeds in your hotbar. Again: no AutoDonut block, nothing on the server -
it's the same break-and-place a player does, performed for you.

Handles wheat, carrots, potatoes, beetroot and nether wart. It only ever
touches crops that are **fully grown**, so it won't trample a field you're
still waiting on.

If you run out of a seed it keeps harvesting, tells you once in chat, and
stops replanting that crop rather than leaving holes silently.

**Settings** (`K` > Auto Farm, or `/autodonutfarm`)

| Setting | Default | What it does |
|---|---|---|
| Enabled | off | Master switch |
| Radius | 4 | Blocks around you, clamped to your real reach |
| Replant | on | Put a seed back after harvesting |
| Include nether wart | on | Also farm soul-sand wart |
| Speed | 4 ticks | Ticks between actions - lower is faster |

```
/autodonutfarm                   Show current settings
/autodonutfarm on|off            Turn it on or off
/autodonutfarm radius <1-6>      Harvest radius
/autodonutfarm replant <bool>    Replant after harvesting
/autodonutfarm netherwart <bool> Include nether wart
/autodonutfarm speed <1-20>      Ticks between actions
/autodonutfarm stats             Crops harvested this session
/autodonutfarm stats reset       Reset the counters
```

### How these two stay honest

Both engines are deliberately boring about what they're allowed to do:

- **Radius is clamped to `player.blockInteractionRange()`** - your real,
  server-checked reach. Asking for radius 6 when you can reach 4.5 gets you
  4.5. There is no reach extension anywhere in AutoDonut.
- **One action per tick.** No burst clicking.
- **They yield.** If you open a GUI yourself, or the Auto Miner is running,
  they stand down until you're done.
- **Every interaction is one a player can perform** - the same
  `useItemOn` / `startDestroyBlock` / container-click calls the vanilla
  client makes when your hand does it.

## Auto Miner (100% client-side)

This one doesn't touch the server at all — it just automates YOUR OWN
player's mining, using exactly the actions a real person could do by hand:
holding left-click on a block (same block-breaking timing, sounds, and
tool/hardness rules as a real held click) and, in walking mode, pressing
forward. It works on any vanilla or modded server, since the server just
sees a normal player mining normally.

**Open the settings GUI** with the default keybind (`K` — rebindable in
Controls > Key Binds > AutoDonut), the `/autodonutmenu` command, or the
**Mods** button in-game if you have [Mod Menu](https://modrinth.com/mod/modmenu)
installed (totally optional — AutoDonut works fine without it).

In the GUI (or via `/autodonutmine`, see below) you can set:

- **Mode** — two options:
  - **Only Mine**: never moves you, just auto-breaks whatever ore/block is
    already in your reach (like an auto-clicker for mining). Safest option.
  - **Walk + Mine**: digs a 1x2 tunnel and walks forward on its own. This
    *will* move your character and camera — don't touch WASD/mouse while
    it's running, and it will stop itself (with a chat warning) if it hits
    an unbreakable block, water/lava, or a drop, rather than risk flooding
    the tunnel or falling.
- **Ore radar** — when on (default), it actively looks for nearby ores
  within a configurable radius and steers/prioritizes mining those over
  a plain straight tunnel, and will grab any ore touching the tunnel walls,
  floor, or ceiling as it goes ("mines any ore it sees" within its radar
  range and reach).
- **Per-ore toggles** — tick/untick exactly which ores it's allowed to go
  after: Coal, Iron, Copper, Gold, Redstone, Lapis, Diamond, Emerald,
  Nether Quartz, Ancient Debris. Untick any you want it to leave alone.

Chat commands (same settings, if you'd rather not open the GUI):

```
/autodonutmine <true|false>            Master on/off switch
/autodonutmine mode mineonly           Switch to "Only Mine"
/autodonutmine mode walkmine           Switch to "Walk + Mine"
/autodonutmine radar <true|false>      Toggle ore-seeking
/autodonutmine radius <2-32>           Ore radar radius, in blocks
/autodonutmine ore <name> <true|false> Toggle one ore, e.g. "diamond"
/autodonutmine ore list                Show every ore and its current state
/autodonutmine                         Show current status
```

**Resource/texture packs:** AutoDonut identifies ores and blocks by their
real in-game block data, not their textures, so it works fine alongside any
resource pack you have loaded — including ore-highlighter packs. Your
server, your rules: if you're already using an ore highlighter pack, this
just adds automation on top of it, it doesn't fight it or need it.

### Extra Auto Miner options (all in the GUI, all still just real actions)

- **Clear whole ore veins** (on by default) — when it breaks an ore block,
  it also checks the 6 blocks touching it and queues up any more of the
  same vein before going back to tunneling, instead of leaving half a vein
  behind.
- **Stop before my pickaxe breaks** (on by default, threshold configurable)
  — watches your held tool's remaining durability and halts mining with a
  chat warning once it drops below the threshold, so you can swap tools
  yourself. It deliberately does **not** auto-swap pickaxes for you — doing
  that with no inventory screen open wouldn't be something a human could
  see themselves doing, so it's left as a manual step by design.
- **Auto-place torches while tunneling** — only ever shown as an option
  when you're (a) in Walk + Mine mode and (b) currently carrying torches;
  it's meaningless in stationary Only-Mine mode and can't do anything
  without torches, so the checkbox simply doesn't appear otherwise. Places
  a torch on the tunnel wall behind you every few blocks, the same
  right-click a player would do, then switches back to your pickaxe.
- **Auto-return to a chest when full** (off by default — the most invasive
  option, opt-in only) — place a chest at your mining start point before
  you begin. When your inventory fills up, the miner retraces its own exact
  path in reverse back to that chest (this is **not** a general waypoint/
  teleport system — it can only walk back over ground it already tunneled
  through itself), opens the chest, shift-click-deposits everything except
  your pickaxe/torches, closes it, and retraces forward again to resume
  mining exactly where it left off. If no chest is found next to the start
  point, it just tells you in chat and skips the deposit.
- **Session stats** — `/autodonutmine stats` (or the button in the GUI)
  prints blocks mined and a per-ore breakdown for the current session;
  `/autodonutmine stats reset` clears it. This is chat-based rather than an
  on-screen HUD overlay — Fabric's newest HUD rendering API for 26.2 uses a
  deferred render-state design that doesn't have a simple "draw a string"
  hook, so a chat summary was the more reliable option for now.

## Economy & Chest Sell (100% client-side, no server install needed)

Built around a typical player-driven auction house command (`/ah`). It's a
small overlay panel that appears on top of GUIs you already have open — it
never opens a different screen, never closes your container, and never
walks/teleports you anywhere. Just like the Auto Miner, everything it does
(reading item tooltips, clicking a slot, typing a chat command) is something
you could do yourself by hand, just faster.

**Because every server's `/ah` plugin has different exact command syntax and
listing text, all of the specifics below are editable defaults, not
guarantees.** Open them from **AutoDonut Settings > Economy & Chest Sell**,
or with `/autodonuteconomy`. If deals aren't showing up or a sell command
doesn't do anything, open a chest/AH GUI and run `/autodonuteconomy dumplore`
— it prints every visible item's exact tooltip text to chat so you can see
what AutoDonut is (or isn't) matching, and fix the regex/command in settings.

### Economy tab (finds underpriced listings)

Whenever you open a GUI whose title contains "Auction" (configurable), a
small panel appears in the top-right with:

- A **search box + Go button** — runs your configured `/ah search` command,
  same as typing it yourself.
- A **Rescan Deals button** and up to 6 **deal rows**, each showing an
  item, its listed price, and how far below AutoDonut's computed market
  value it is (e.g. `Diamond $120 (-32%)`).
- Clicking a deal row **re-checks that exact listing is still there and
  still that price**, then clicks it once to buy — there's no travel or
  navigation, it's a straight click-to-buy on the listing you're already
  looking at, like a DonutSMP-style GUI shop.

Market value is learned passively: every time you have the `/ah` GUI open,
AutoDonut reads the price off every visible listing and rolls it into a
local price-history file (`config/autodonut_market.json`) — nothing is ever
sent anywhere. An item needs a handful of observed listings (default: 3)
before AutoDonut trusts a market value for it, so browse the AH a bit before
expecting deals to show up for a given item.

### Chest Sell (sell everything in any chest, at a price tier)

Open **any** chest (or barrel, shulker box, etc.) and three buttons appear
in the top-right: **Sell: At Market**, **Sell: Below Market** (cheaper, for
a faster sale), and **Sell: Above Market** (pricier, more profit but slower
to sell). Picking one:

1. Works out the market value (from the same local price history above) for
   every distinct item in the chest.
2. One item type at a time: picks it up, places it into a dedicated staging
   hotbar slot (slot 9 by default, must be empty when you start), selects
   that hotbar slot, and runs your configured `/ah sell <price>` command at
   the tier's price (market value × a configurable discount/premium %).
3. Skips anything on your **Do Not Sell** list, and skips (leaves in the
   chest) any item AutoDonut doesn't have enough market data for yet, since
   it refuses to guess a price out of thin air.
4. Reports a summary in chat when it's done ("Sold: ..., Skipped: ...").

Don't touch your inventory or close the chest while it's running — it'll
stop itself and tell you why if either happens.

### Settings (`AutoDonut Settings > Economy & Chest Sell`)

- Enable/disable the whole feature
- Auction house GUI title keyword
- Sell command template (`{price}` placeholder) — default `ah sell {price}`
- Search command template (`{item}` placeholder) — default `ah search {item}`
- Price-parsing regex (one capture group, matched against each item's tooltip)
- Deal threshold %, Below/Above Market %, minimum listings before trusting a price
- Chest Sell's staging hotbar slot (1-9)
- Do Not Sell list (comma-separated item ids, e.g. `minecraft:diamond`)
- "Clear market price history" button

Chat command equivalent: `/autodonuteconomy` (status), `/autodonuteconomy
<true|false>` (on/off), `/autodonuteconomy dumplore` (debug), `/autodonuteconomy
clearmarket`, `/autodonuteconomy dns add|remove|list <item>`.

### Quick-Sell keybind

Unbound by default (set it in Controls > Key Binds > AutoDonut). Press it
while holding an item to instantly sell your whole held stack, either via
the AH sell command at your chosen tier (At/Below/Above Market, same as
Chest Sell), or via a flat `/sell`-style command instead if you flip
**"Quick-Sell uses flat /sell"** on in Economy settings. When using flat
sell, AutoDonut also listens for the very next chat line matching a
configurable `flatSellConfirmPattern` regex (default matches a `$amount`)
to learn that item's flat-sell value, so the Economy panel can show both
its AH market value and its last-known flat-sell value side by side.

### Watchlist, shopping list, price trend, and profit ledger

All under `/autodonuteconomy` (or partially in the settings GUI):

```
/autodonuteconomy watch add <item> <maxPrice>   Alert in chat if item lists at/under this price
/autodonuteconomy watch remove <item>
/autodonuteconomy watch                         List your watchlist
/autodonuteconomy want add <item> <qty>         Add to your shopping list (want-list)
/autodonuteconomy want remove <item>
/autodonuteconomy want                          List your shopping list
/autodonuteconomy ledger                        Show total spent/earned/net profit this session
/autodonuteconomy ledger clear                  Reset the ledger
```

- **Watchlist** alerts you in chat (with a per-item cooldown, so it won't
  spam) whenever a scanned AH listing matches an item on your list at or
  under your target price.
- **Shopping list (want-list)** decrements automatically as the Economy
  buy-flow buys matching items for you, and tells you in chat once you've
  hit your target quantity for an item.
- **Price trend** — the Economy panel's deal rows show a trend arrow based
  on recent recorded prices for that item (rising/falling/flat), computed
  from the same local price-history file used for market value.
- **Profit ledger** — every AutoDonut-driven buy and sell is recorded
  locally (`config/autodonut_ledger.json`); `ledger` shows your running
  total spent, earned, and net profit for the session.

### Sharing price data between friends

`/autodonuteconomy sharedb export <filename>` writes your local market
price history to `config/<filename>.json`; `sharedb import <filename>`
reads one back in and merges it. Send that file to a friend on the same
server (Discord, USB stick, whatever) so you don't both have to browse the
AH from scratch to build up price history — nothing is uploaded anywhere
automatically, it's a manual file you choose to share.

### What's *not* included here (and why)

- **Auto-relist assistant** — deliberately not built. Reliably telling
  apart "your own" AH listings from everyone else's needs to know your
  particular server's exact lore/seller-field format, and shipping a guess
  here risked silently relisting (or worse, misreading) someone else's
  items. If you want this, run `/autodonuteconomy dumplore` while looking
  at your own "my listings" AH page and it can be added correctly later.

## Safety (100% client-side, all off by default)

**AutoDonut Settings > Safety.** Everything here only ever does something a
real player sitting at the keyboard could do (eat, press a hotbar key,
jump) — it never fakes an inventory-screen click with no screen open.

- **Auto-eat** — when your hunger drops below a configurable threshold, it
  finds food in your hotbar, switches to it if needed, and eats — same as
  you right-clicking food yourself.
- **Threat pause** — when a hostile mob (optionally also other players)
  gets within a configurable radius, it pauses the Auto Miner and other
  AutoDonut automations, warns you in chat, and — if you've set a Discord
  webhook URL in settings — sends a one-line ping there too (best-effort,
  silently does nothing if the URL is blank or unreachable).
- **Anti-AFK-kick nudge** — every 3-5 minutes (randomized), if nothing else
  is actively moving you, presses jump once — enough to reset most servers'
  AFK-kick timers without actually playing for you.

## Optional: the legacy server-side blocks

> **You almost certainly don't want these.** Everything they do is now done
> client-side by the Auto Smelter and Auto Farm above, with nothing installed
> on the server. They're kept only for people who *do* run a modded server and
> prefer real in-world machines that everyone can see.
>
> **This is the one part of AutoDonut that needs the mod installed on the
> server as well as on every client that should see the blocks.**

Get one of each anytime with `/autodonut kit` (needs permission level 2,
i.e. an op/admin).

- **Auto Smelter** — feed raw items into it (right-click with an item in
  hand, or hopper it in) and it instantly smelts anything with a vanilla
  smelting recipe into its output side — no fuel needed, no waiting.
  Right-click empty-handed to grab a finished stack.
- **Auto Farm** — place it in the middle of a crop field (same height as
  the crops). Every second it harvests any fully-grown wheat / carrots /
  potatoes / beetroot in a radius around itself and instantly replants
  them, storing the produce inside. **Sneak + right-click** to cycle the
  radius (2/4/6/8). Right-click empty-handed to collect.
- **Auto Miner** — a fuel-powered quarry. Feed it coal/charcoal/a coal
  block (right-click with it in hand), and it digs a square shaft straight
  down beneath itself, storing everything it mines (skips bedrock and
  liquids). **Sneak + right-click** to cycle the dig width (1x1/3x3/5x5).
  Right-click empty-handed to collect.

All three are also compatible with hoppers for automatic item transport,
just like a furnace or chest.

## AI advisor and legacy server commands

> These run **on the server**, so they only exist if you installed AutoDonut
> server-side for the legacy blocks above. The client-side commands -
> `/autodonutmine`, `/autodonutsmelt`, `/autodonutfarm`, `/autodonuteconomy`,
> `/autodonutchat` and `/autodonutmenu` - always work, and so does the `K`
> settings screen.

Run `/autodonut status` to see current settings. Everything below needs
op/admin permission:

```
/autodonut automate <true|false>          Master switch for all automation
/autodonut machine smelter <true|false>   Toggle just the Auto Smelter
/autodonut machine farm <true|false>      Toggle just the Auto Farm
/autodonut machine miner <true|false>     Toggle just the Auto Miner
/autodonut kit                            Get one of each block

/autodonut ai setkey <key>                Save your AI API key
/autodonut ai baseurl <url>               Change the AI endpoint (default: OpenAI)
/autodonut ai model <name>                Change the AI model (default: gpt-4o-mini)
/autodonut ai ask <question>              Ask the AI advisor anything in chat
/autodonut ai auto <true|false>           Toggle the AI "auto controller" flag
```

The AI advisor (`/autodonut ai ask`) sends your question straight to
whichever API endpoint you configured (OpenAI by default) using your key —
nothing goes anywhere else. Your key is stored locally in
`config/autodonut.json`.

## Notes on how this was built (for the curious / skeptical)

This is real Java written against the actual Minecraft 26.2 / Fabric API, not
a mockup. The whole of `src/` type-checks with zero errors, and the installer
compiles against a plain JDK.

Being straight with you about what that does and doesn't mean: at the time of
writing the build has been verified with a **type-check against hand-written
stubs** of the Minecraft API (see `tools/offline-typecheck/`), because the
machine it was developed on couldn't reach Maven Central to download the real
Minecraft artifacts. That proves every line of AutoDonut's own code is
internally consistent and correctly typed. It does **not** prove that every
Minecraft signature it calls is spelled exactly right for 26.2 -
`API-SURFACE.md` lists all 433 external members so that assumption is
reviewable rather than hidden.

The authoritative check is `./gradlew build` (needs JDK 21+), which downloads
the real game and compiles against it. Run it before you trust a jar.

## Fair warning

This is for a server **you** control. If your server has its own rules
against automation for balance reasons (even a private one with friends),
that's between you and them — but there's nothing here that fakes player
input, bypasses anti-cheat, or targets a server you don't run.
