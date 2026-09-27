# AutoDonut — Project Handoff / Rebuild Prompt

This document is a complete, self-contained brief for rebuilding **AutoDonut** from
scratch in a new session or a different tool. Copy the section
[`## THE PROMPT`](#the-prompt) verbatim into a fresh agent session and it should be
able to pick the project back up without any other context.

The rest of the document is reference material: the feature spec, the exact
Minecraft 26.2 API notes that were discovered the hard way (these will save hours),
and the list of what was finished vs. still outstanding.

---

## Status as of this handoff

| Thing | State |
|---|---|
| GitHub repo `CoolgamerProXD/AutoDonut` | Created, **empty** (README only) — the push failed with HTTP 403 |
| Mod source (~36 Java files) | Was built and compiling in a previous sandbox; **not recovered** |
| Built jars / installer | Not recovered |
| This document | The surviving spec |

**The code was never pushed.** If you still have the old workspace or downloaded
`autodonut-mod-source.zip`, upload it — rebuilding from that is far cheaper than
starting over. Otherwise use the prompt below.

---

## THE PROMPT

> Copy everything between the horizontal rules into a new session.

---

I'm building a Minecraft Java Edition mod called **AutoDonut**. I had it working in a
previous session but lost the source, so I need it rebuilt. Here is the full spec.

### Ground rules / context

- **Target: Minecraft Java Edition 26.2, Fabric Loader.** Not 26.3. Not Forge.
- This is for **my own self-hosted survival server** that I run for me and my friends.
  It's not for DonutSMP or any third-party server — it's my server, my rules. It's a
  normal tech-mod + client-QoL project in the same category as Mekanism or
  Applied Energistics.
- My server hardware is weak and I'd rather **not install mods on the server at all**.
  So the priority is the **client-only** features. The server-side automation blocks
  are a lower-priority nice-to-have.
- Everything client-side must act through **legitimate client input paths** — real
  block-break timing, real movement input, real container-click packets. No packet
  injection, no impossible-state actions, no anti-cheat bypass.
- Deliverable should be **double-clickable** for non-technical friends: a Java Swing
  GUI installer jar that drops the mod + Fabric API into a `mods/` folder.

### Project layout

```
autodonut/
├── build.gradle                 # fabric-loom, Java 25 toolchain
├── gradle.properties            # versions
├── src/main/java/com/autodonut/         # common + server-side
│   ├── AutoDonut.java                   # ModInitializer entrypoint
│   ├── config/AutoDonutConfig.java      # Gson JSON config @ config/autodonut.json
│   ├── ai/AiClient.java                 # OpenAI-compatible chat completions client
│   ├── command/AutoDonutCommand.java    # /autodonut server command tree
│   ├── block/                           # AutoSmelterBlock, AutoFarmBlock, AutoMinerBlock
│   ├── block/entity/                    # AutomationBlockEntity base + 3 subclasses
│   └── registry/                        # ModBlocks, ModItems, ModBlockEntities
├── src/client/java/com/autodonut/client/ # client-only entrypoint
│   ├── AutoDonutClient.java             # ClientModInitializer, keybinds, client commands
│   ├── AutoDonutModMenuIntegration.java # optional ModMenu hook
│   ├── miner/                           # OreType, AutoMinerEngine
│   ├── economy/                         # AhListing, AhScanner, MarketDatabase, DealFinder,
│   │                                    #   BuyFlowEngine, ChestSellEngine, QuickSellEngine,
│   │                                    #   ProfitLedger, Watchlist, EconomyGuiHooks
│   ├── safety/                          # SafetyEngine, DiscordWebhook
│   └── gui/                             # AutoDonutConfigScreen (hub) + Automation, Miner,
│                                        #   Chat, Economy, Safety sub-screens
└── src/main/resources/
    ├── fabric.mod.json                  # main + client entrypoints, modmenu as suggested
    └── assets/autodonut/                # lang/en_us.json, blockstates, models, textures, icon
```

`build.gradle` needs `loom { splitEnvironmentSourceSets() }` and a `client` source set
so `src/client/java` compiles as client-only, with `fabric.mod.json` declaring both a
`"main"` and a `"client"` entrypoint.

### Features — client-only (priority 1)

**1. Auto Miner** (`/autodonutmine`, keybind `K` opens settings)
- Two modes, user-selectable in the GUI:
  - **Only Mine** — never moves the player, just auto-breaks blocks within reach.
  - **Walk + Mine** — digs a 1x2 tunnel and walks forward on its own.
- **Ore radar**: scans a configurable 2–32 block radius for ores and steers the
  tunnel toward them; also grabs ores exposed in the tunnel walls/floor/ceiling.
- **Per-ore toggles** in the GUI: Coal, Iron, Copper, Gold, Redstone, Lapis,
  Diamond, Emerald, Nether Quartz, Ancient Debris (each covers its regular +
  deepslate + nether variants). Identify ores by **block registry data, not
  textures**, so any resource pack (including ore-highlighter packs) works fine.
- **Vein clearing**: after hitting an ore, flood-fill the connected vein in all 6
  directions before returning to the tunnel line.
- **Tool durability guard**: stop and warn in chat before the pickaxe breaks.
  (Do *not* auto-swap tools — that needs an invisible inventory click.)
- **Auto-torch**: place torches on tunnel walls. This option must only be
  available/enabled when **both** (a) mode is Walk+Mine **and** (b) the player
  actually has torches in inventory — check live in the GUI and re-guard in the engine.
- **Return-to-chest when full** (opt-in, off by default): record the forward path,
  retrace it back to a chest placed at the tunnel start, shift-click deposit, retrace
  forward, resume. Path retracing only — not general pathfinding.
- **Safety**: refuse to mine into water/lava, stop at unbreakable blocks, don't walk
  off ledges — pause with a chat warning instead.
- **Session stats**: blocks mined, per-ore breakdown, runtime — via
  `/autodonutmine stats` (chat-based; the 26.2 HUD API is a new deferred-render API
  and was more trouble than it was worth).

**2. Economy tab + Chest Sell** (`/autodonuteconomy`)

My server's economy: `/ah` is a player auction house (real market prices),
`/sell` sells to the game at a flat price. Listing is `/ah sell <price>`, and `/ah`
has a search/filter. **All command formats and the price-parsing regex must be
user-editable in the settings GUI**, because plugin syntax varies — ship sensible
defaults (`/ah sell {price}`, `/ah search {item}`, price regex `\$([0-9][0-9,]*(?:\.[0-9]+)?)`)
but let me override them.

- **AH scanner**: whenever a GUI whose title contains a configurable keyword
  (default "Auction") is open, read every non-player slot's tooltip, parse the price,
  and log it into a local rolling price database (`config/autodonut_market.json`).
  Market value = median of observed per-unit prices, only trusted after N samples.
- **Deal finder**: overlay panel on the AH screen listing items priced X% below
  market value (X configurable). Clicking a deal **re-verifies** the listing is still
  there at that price, then clicks it once to buy. Never blind-buy stale data. No
  travel/navigation — it's just a menu click, like DonutSMP click-to-buy.
- **Chest Sell**: three buttons on any chest/barrel/shulker/ender-chest/inventory GUI —
  **At Market**, **Below Market** (undercut, sells fast), **Above Market** (premium,
  sits longer). Percentages configurable. For each distinct stack: pick it up, stage it
  in a dedicated hotbar slot, select it, run the sell command. One item at a time with
  small pauses; abort safely with a chat message if the cursor/staging slot isn't clear
  or the chest closes mid-run.
- **Do Not Sell list** — protected items that Chest Sell will never touch. Also skip
  anything with no market data rather than guessing a price.
- **Quick-Sell keybind** (unbound by default): sell whatever's in your main hand at
  your default tier, no GUI needed. Optional flat-`/sell` mode that *learns* the
  server's flat price by regex-matching the confirmation message.
- **Watchlist**: `watch add <item> <maxPrice>` → cooldown-throttled chat alert + sound
  when a listing beats your target.
- **Shopping / want list**: `want add <item> <qty>`, auto-decrements as buys land.
- **Price trend arrows** (↑/↓/→) from recent observation history.
- **Profit ledger** (`config/autodonut_ledger.json`): every buy/sell logged, with
  session and all-time totals.
- **Shared price DB**: `sharedb export <name>` / `sharedb import <name>` so friends can
  merge their market history.
- **`dumplore` debug command**: print every visible item's exact tooltip lines to chat,
  so I can tune the price regex when nothing matches.
- *Deferred:* auto-relist of unsold listings — needs the real AH lore format first.

**3. Safety utilities** (all off by default)
- **Auto-eat** below a configurable hunger threshold: switch to food in hotbar, eat,
  restore the previous hotbar slot.
- **Threat pause**: pause all automation when a hostile mob (or optionally another
  player) enters a configurable radius, or on damage/low health. Chat warning plus an
  optional **Discord webhook** ping.
- **Anti-AFK nudge**: small randomized jump/look every 3–5 minutes while idle so my own
  server doesn't idle-kick me.

**4. Chat Modifiers** (100% client-side, filters only what I see)
- Hide all player chat.
- Hide likely-scam messages — keyword list, default includes `tp`, matched as a **whole
  word** so it doesn't eat "step"/"stop". Must also filter **system/game messages**,
  not just player chat, since scam pings often arrive as server broadcasts. Optional
  regex mode.
- Hide join/leave spam.
- Highlight messages that mention my name (as an extra annotated line — signed chat
  can't be rewritten client-side).
- Editable in GUI and via `/autodonutchat`.

### Features — server-side blocks (priority 2)

These need the mod on the server *and* every client, because they register real blocks.
Build them, but note in the README that they're optional.

- **Auto Smelter** — instant, fuel-free smelting. 5 input + 5 output slots, hopper
  compatible. Right-click with item to insert, empty-hand right-click to withdraw.
- **Auto Farm** — placed at crop level, harvests fully-grown wheat/carrots/potatoes/
  beetroot in a radius every second and replants. Sneak+right-click cycles radius 2/4/6/8.
- **Auto Miner (quarry block)** — fuel-powered (coal 16 mines / charcoal 16 / coal block
  160), digs a square shaft straight down, skips bedrock and liquids, 1 fuel + 27 storage
  slots. Sneak+right-click cycles width 1x1/3x3/5x5.
- `/autodonut kit` gives one of each (permission level 2).

**Outstanding idea:** convert Auto Smelter and Auto Farm to client-only equivalents
(auto-manage a nearby vanilla furnace; auto-harvest/replant around the player using
normal break/place actions) so the server can stay 100% vanilla. This was planned but
never built.

### AI advisor

`/autodonut ai setkey <key>`, `baseurl`, `model`, `ask <question>`. A minimal
OpenAI-compatible `/v1/chat/completions` client using `java.net.http.HttpClient` +
Gson. Works with OpenAI or any compatible endpoint (Ollama, OpenRouter, LocalAI).
Key stored locally in `config/autodonut.json`, sent only to the configured endpoint.

### Config & GUI

- Single Gson-backed `config/autodonut.json`, loaded/saved through a static
  `AutoDonutConfig.get()` / `.save()`.
- A hub settings screen with buttons to sub-screens: **Automation**, **Miner**,
  **Chat Modifiers**, **Economy & Chest Sell**, **Safety**.
- Openable three ways: keybind `K` (rebindable), `/autodonutmenu`, and the Mods button
  if **Mod Menu** is installed. The Mod Menu hook must be a **soft/optional** dependency —
  zero effect if Mod Menu isn't present.

### Deliverables

1. `autodonut-1.0.0.jar` — the mod.
2. `AutoDonut-Installer.jar` — double-clickable Swing GUI installer bundling the mod +
   Fabric API as internal resources. Auto-detects `.minecraft` on Windows/macOS/Linux,
   client-or-server mode, folder browser, optional AI key field, writes
   `config/autodonut.json`, and a button linking to https://fabricmc.net/use/.
3. `autodonut-mod-source.zip` — full source.
4. A polished `README.md` with badges, a table of contents, feature sections, the full
   command reference in a collapsible `<details>` block, install guide, and build steps.
5. `.gitignore`, `LICENSE` (CC0-1.0), and a GitHub Actions `build.yml` workflow.

### Please verify, don't guess

Minecraft 26.2's API differs a lot from older versions and from most training data.
Before writing code against an API, **decompile the actual game jar and check the real
signature**. Build with `./gradlew build` and fix errors iteratively, and smoke-test by
booting a real Fabric dedicated server to confirm (a) the mod loads clean and (b) the
client-only code has zero server-side impact. See the API notes below — these were all
verified against the real 26.2 jar and will save you a lot of time.

---

## Verified Minecraft 26.2 / Fabric API notes

These were all confirmed by decompiling the real game jar in the previous session.
Most of them differ from older Minecraft versions and from what a model will guess.

### Toolchain
- **Java 25** toolchain (`options.release = 25`), Fabric Loom **1.17**.
- Loom now defaults to **Mojang official mappings** — no Yarn block needed.
- Fabric API **0.161.0+26.2**. Fabric Loader **0.19.0+**.
- Runtime needs Java 21+, 25 recommended.

### Registration
- `Identifier.fromNamespaceAndPath(ns, path)` — the class is `net.minecraft.resources.Identifier`
  (not `ResourceLocation`).
- Blocks: `BlockBehaviour.Properties.of().setId(resourceKey)` — the `setId` call is
  **mandatory**, and the key must be created before the block instance.
- Items: `new Item.Properties().setId(itemResourceKey)`, likewise mandatory.
- `Registry.register(BuiltInRegistries.BLOCK, key, block)` etc.
- Block entities: `FabricBlockEntityTypeBuilder.create(Ctor::new, block).build()`.

### Block entities
- NBT is now the `ValueInput` / `ValueOutput` API:
  ```java
  @Override protected void loadAdditional(ValueInput input) {
      super.loadAdditional(input);
      this.radius = input.getIntOr("Radius", 4);   // note: getIntOr, with a default
  }
  @Override protected void saveAdditional(ValueOutput output) {
      super.saveAdditional(output);
      output.putInt("Radius", radius);
  }
  ```
- Any `BaseEntityBlock` subclass **must override `codec()`** or the game fails at
  registration.
- Ticker pattern: `createTickerHelper(type, ModBlockEntities.X, (lvl, pos, st, be) -> X.serverTick((ServerLevel) lvl, pos, st, be))`,
  returning `null` when `level.isClientSide()`.
- Interaction methods are `useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)`
  and `useWithoutItem(BlockState, Level, BlockPos, Player, BlockHitResult)`, both `protected`.

### Commands
- Permissions: `.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))`
  — **not** the old `source -> source.hasPermission(2)`.
- Client commands: `ClientCommandRegistrationCallback.EVENT` +
  `ClientCommands.literal(...)` / `ClientCommands.argument(...)`, source type
  `FabricClientCommandSource`, feedback via `ctx.getSource().sendFeedback(Component)`.

### Recipes
- `RecipeManager.createCheck(RecipeType.SMELTING)` → `RecipeManager.CachedCheck<SingleRecipeInput, SmeltingRecipe>`,
  then `check.getRecipeFor(new SingleRecipeInput(stack), level)` → `Optional<RecipeHolder<...>>`,
  then `holder.value().assemble(recipeInput)`.

### Client / GUI
- `minecraft.gui.setScreen(parent)` — **not** `minecraft.setScreen(parent)`.
- `Checkbox.builder(Component, Font).pos(x, y).selected(bool).onValueChange((cb, v) -> …).build()`.
- `new EditBox(Font, x, y, width, height, Component narration)`, `.setResponder(...)`.
- `MultiLineTextWidget(x, y, Component, Font)` + `.setMaxWidth(n)`.
- `StringWidget(x, y, w, h, Component, Font)`; `setMessage`/`getMessage` live on
  `AbstractWidget` (so `Button` has them). `Button.active` is public.
- Injecting widgets into someone else's screen: `ScreenEvents.AFTER_INIT` +
  `Screens.getWidgets(screen)` → mutable `List<AbstractWidget>`.
- Keybind categories: `KeyMapping.Category.register(Identifier)`.
- The HUD render API is new/deferred in 26.2 — chat-based output was used instead of a
  HUD overlay. Revisit if you want a real overlay.

### Containers / inventory
- Clicking a slot: `mc.gameMode.handleContainerInput(menu.containerId, slotIndex, button, ContainerInput.PICKUP, mc.player)`.
  `ContainerInput` is the new enum replacing the old `ClickType`.
- Player inventory always occupies the **last 36 slots** of a menu, so container slot
  count = `menu.slots.size() - 36`.
- Tooltips: `stack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.NORMAL)`.

### Chat events
- `ClientReceiveMessageEvents.ALLOW_CHAT` — lambda takes **5 params**:
  `(message, signedMessage, sender, params, receptionTimestamp)`, return `false` to hide.
- `ClientReceiveMessageEvents.ALLOW_GAME` — `(message, overlay)`; skip filtering when
  `overlay` is true so you don't eat the action bar.
- Signed player chat **cannot be rewritten** client-side — only allowed or blocked.
  Mention highlighting therefore has to be an *additional* line.

### Misc
- `Direction.getApproximateNearest(double x, double y, double z)`.
- `player.sendSystemMessage(Component)` is the right call for client-only local display
  from a client entrypoint (it does not send anything to the server).

---

## What was done vs. outstanding

**Done and compiling in the lost build** (~36 Java files, `./gradlew build` green,
smoke-tested on a real Fabric dedicated server):
- All three server-side automation blocks + registries + `/autodonut` command tree
- AI advisor client
- Client Auto Miner with both modes, ore radar, per-ore toggles, vein clearing,
  durability guard, conditional auto-torch, return-to-chest, session stats
- Economy: AH scanner, market DB, deal finder, buy flow, Chest Sell (3 tiers),
  Quick Sell, watchlist, want list, trend arrows, profit ledger, shared DB export/import,
  `dumplore`
- Safety: auto-eat, threat pause + Discord webhook, anti-AFK nudge
- Chat modifiers: hide players, scam keywords (+regex), join/leave, mention highlight
- Full GUI hub + 5 sub-screens, keybinds, optional Mod Menu hook
- Swing installer, README, `.gitignore`, CC0 LICENSE, GH Actions workflow

**Never built:**
- Client-only conversion of Auto Smelter / Auto Farm (so the server can stay vanilla)
- Auto-relist of unsold AH listings (needs the real AH lore format)
- A true HUD overlay for miner stats (26.2 deferred HUD API)
- First-run setup wizard for the Economy regex/commands

**Never verified against a live server** (inherently untestable from a sandbox):
- Whether the default `/ah` command templates and price regex match the real plugin.
  `dumplore` exists specifically to debug this.

---

## Lessons for next time

1. **Commit and push early and often.** The entire loss here is because ~20 minutes ×
   several sessions of work lived only in an ephemeral sandbox. Push after every
   working build.
2. **Keep build artifacts out of the workspace.** The Gradle cache, decompiled
   Minecraft jars, and a test server were what blew the budget. Add them to
   `.gitignore` and delete them before the turn ends.
3. **Never paste a token into chat.** Use the platform's GitHub connection instead.
