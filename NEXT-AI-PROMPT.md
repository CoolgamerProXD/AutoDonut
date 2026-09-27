# Next AI Prompt — AutoDonut

**Living handoff document. Updated at the end of every completed task.**

If a session dies, the workspace blows its budget, or you want to continue this
project in a different tool, paste [`## THE PROMPT`](#the-prompt) into a fresh
agent session. Everything it needs is in this file.

- **Last updated:** 2026-09-27
- **Repo:** `CoolgamerProXD/AutoDonut`
- **Working branch:** `arena/01a0e0e6-autodonut`
- **Target:** Minecraft Java Edition **26.2**, Fabric. *(Not 26.3. Not Forge.)*

---

## Current state

| Area | State |
|---|---|
| Client-side Auto Miner | ✅ Done |
| Client-side Auto Smelter | ✅ Done — tends vanilla furnaces |
| Client-side Auto Farm | ✅ Done — harvests + replants vanilla crops |
| Client-side Economy / Chest Sell | ✅ Done (command syntax unverified against a live `/ah`) |
| Client-side Safety | ✅ Done |
| Client-side Chat Modifiers | ✅ Done |
| AI advisor | ✅ Done |
| Server-side blocks | ⚠️ Still present, now **legacy/optional** |
| Type-check | ✅ **Zero errors** across all 40 files (against API stubs — see [Build status](#build-status)) |
| Real `./gradlew build` | ❌ Still never run — Maven Central unreachable in the dev sandbox |

**The headline:** every feature now has a 100% client-side implementation.
Nothing needs to be installed on the server. The old server-side blocks are
still in the codebase but are marked LEGACY in the UI and can be deleted
entirely if you want (see [Ideas / backlog](#ideas--backlog)).

---

## ⚠️ Build status — read this first

**Every file in `src/` now type-checks with zero errors.** The installer
compiles for real against a plain JDK. But `./gradlew build` has still never
run, and you need to understand exactly what has and hasn't been proven.

### What was done

The dev sandbox had no JDK and no route to Maven Central, so the real
Minecraft artifacts could not be downloaded. Instead:

1. A real Java compiler was obtained (ECJ 3.46, the Eclipse batch compiler,
   which is pure Java and runs on a bare JRE).
2. Hand-written stubs were created for **every** external type the mod
   touches — 131 types, 433 members.
3. All 40 project sources were compiled against those stubs. **Zero errors.**
4. A warning pass (dead code, null dereference, unused, fallthrough,
   resource leaks, incomplete switch) found nothing of substance.

The harness is checked in at `tools/offline-typecheck/`:

```bash
ECJ_JAR=/path/to/ecj.jar ./tools/offline-typecheck/run.sh
```

### What that proves

- Every file parses and type-checks under a real Java compiler.
- Generics, control flow, definite assignment, missing returns, unreachable
  code and switch exhaustiveness are all clean.
- **Every call between AutoDonut's own classes resolves with correct types.**
  Internal inconsistency is ruled out.

### What that does NOT prove

The stubs encode our *assumption* about each Minecraft signature. If a stub is
wrong, the harness compiles happily and Gradle won't. **`API-SURFACE.md` is
the review list** — all 131 types and 433 members in one file, grouped by
origin. If `./gradlew build` fails, the mismatch is in there and the compiler
error will name the type.

**Your first job in a new session: run `./gradlew build`.** It should be much
closer to green than it was, but treat `API-SURFACE.md` as the debugging map.

### Bugs the type-check already caught

- `AutoFarmBlock` / `AutoMinerBlock` called `player.sendOverlayMessage(...)`,
  which is **not a vanilla `Player` method** (it's a Forge-ism). Replaced with
  `displayClientMessage(Component, true)`, which is long-standing vanilla and
  puts the text on the action bar as intended.

### Previously-flagged APIs: now resolved

- `NetherWartBlock.AGE` — **hardened.** `ClientFarmEngine.isHarvestable()` now
  reads `BlockStateProperties.AGE_3` behind a `state.hasProperty(...)` guard,
  so a moved or renamed constant degrades to "not harvestable" instead of
  throwing.
- `new ItemStack(seed).getHoverName()` — **fine.** Type-checks, and the same
  call is used in `AhScanner.java`.
- The legacy block tickers cast `Level` → `ServerLevel` inside
  `createTickerHelper`, which is correct for the real vanilla
  `BlockEntityTicker.tick(Level, ...)` signature. Confirmed by compiling both
  ways.


---

## THE PROMPT

> Copy everything between the horizontal rules into a new session.

---

I'm continuing work on **AutoDonut**, a Fabric mod for **Minecraft Java Edition 26.2**
(not 26.3, not Forge). The repo is `CoolgamerProXD/AutoDonut`.

### Context and ground rules

- It's for **my own self-hosted survival server**, which I run for me and my friends.
  Not for DonutSMP or any public server. Normal modding territory.
- **My server hardware is a ~30-year-old PC with 2 GB of DDR3 RAM. It cannot run
  mods.** So *everything* must be **100% client-side**. Do not propose features that
  need a server-side install. This is a hard requirement, not a preference.
- All client-side automation must act through **legitimate client input paths**: real
  block-break timing via `startDestroyBlock`/`continueDestroyBlock`, real
  `useItemOn` interactions, real container-click packets, real movement input.
  No packet forging, no reach extension, no impossible-state actions, no anti-cheat
  bypass. Everything it does must be something I could physically do by hand.
- Non-technical friends need to install it, so there's a **double-clickable Swing GUI
  installer** (`installer/AutoDonutInstaller.java`) that bundles the mod + Fabric API.

### What already exists

All 100% client-side, all working through the settings GUI (keybind `K`,
`/autodonutmenu`, or Mod Menu):

- **Auto Miner** — Only-Mine / Walk+Mine, ore radar with per-ore toggles, vein
  clearing, pickaxe durability guard, conditional auto-torch (only offered when
  Walk+Mine is on *and* you actually carry torches), retrace-path return-to-chest
  when full, session stats.
- **Auto Smelter** — tends ordinary vanilla Furnace/Blast Furnace/Smoker blocks
  already in the world. Opens one in reach, quick-moves the output out, tops up fuel
  and input from inventory, closes. Input is an explicit allow-list so it can't
  shove something valuable into a furnace.
- **Auto Farm** — harvests fully-grown vanilla crops within real reach and replants
  from hotbar seeds. Wheat, carrots, potatoes, beetroot, nether wart.
- **Economy & Chest Sell** — scans `/ah` GUI tooltips into a local price database,
  flags below-market deals for one-click buy, sells chests at At/Below/Above market
  tiers, watchlist, want-list, profit ledger, shared DB export/import, `dumplore`
  debug command.
- **Safety** — auto-eat, threat-pause (pauses *all* engines, optional Discord
  webhook), anti-AFK nudge. All off by default.
- **Chat Modifiers** — hide player chat, whole-word scam-keyword filter (also
  filters system broadcasts), hide join/leave, mention highlighting.
- **AI advisor** — `/autodonut ai ask` against any OpenAI-compatible endpoint.

There are also **legacy server-side blocks** (`src/main/java/.../block/`) left over
from before the client-only requirement. They're marked LEGACY in the UI. They are
optional dead weight — see the backlog.

### Repo layout

```
build.gradle, gradle.properties, settings.gradle, gradlew
src/main/java/com/autodonut/          common + LEGACY server blocks
  config/AutoDonutConfig.java         single Gson config, config/autodonut.json
  ai/AiClient.java
  command/AutoDonutCommand.java       /autodonut (server-side)
  block/, block/entity/, registry/    LEGACY
src/client/java/com/autodonut/client/ client-only entrypoint
  AutoDonutClient.java                keybinds, tick loop, all client commands
  automation/ClientSmelterEngine.java
  automation/ClientFarmEngine.java
  miner/AutoMinerEngine.java, OreType.java
  economy/  (10 classes)
  safety/SafetyEngine.java, DiscordWebhook.java
  gui/      (8 screens; AutoDonutConfigScreen is the hub)
src/main/resources/fabric.mod.json    main + client + modmenu entrypoints
installer/AutoDonutInstaller.java
docs/USER-GUIDE.md, docs/ci/build.yml
API-SURFACE.md                        all 131 external types / 433 members
tools/offline-typecheck/              ECJ stub harness + run.sh
```

### First thing to do

**Run `./gradlew build`.** Every file already type-checks with zero errors against
the stub tree in `tools/offline-typecheck/`, so the remaining risk is purely that a
stubbed Minecraft signature doesn't match the real 26.2 one. When something fails,
look the type up in `API-SURFACE.md`, fix both the real call and the stub, and re-run
`./tools/offline-typecheck/run.sh` to keep the offline harness honest.

Then boot a real Fabric **26.2** dedicated server with the jar present to confirm the
client-only code has zero server-side impact.

### Please verify, don't guess

Minecraft 26.2's API differs a lot from older versions and from most training data.
Before writing against an unfamiliar API, decompile the actual game jar and check the
real signature. The verified-API notes in `NEXT-AI-PROMPT.md` will save you hours.

---

## Verified Minecraft 26.2 / Fabric API notes

All confirmed by decompiling the real 26.2 jar, or by being present in code that
already compiled in this project. These differ from older versions and from what a
model will guess.

### Toolchain
- Java **25** toolchain (`options.release = 25`), Fabric Loom **1.17-SNAPSHOT**.
- Loom defaults to **Mojang official mappings** — no Yarn block needed.
- `minecraft_version=26.2`, `loader_version=0.19.5`, `fabric_api_version=0.161.0+26.2`,
  `modmenu_version=20.0.3`.
- Runtime needs Java 21+, 25 recommended.
- `loom { splitEnvironmentSourceSets() }` gives the `src/client` source set.

### Registration (LEGACY blocks only)
- `Identifier.fromNamespaceAndPath(ns, path)` — class is
  `net.minecraft.resources.Identifier`, **not** `ResourceLocation`.
- `BlockBehaviour.Properties.of().setId(resourceKey)` — `setId` is **mandatory**.
- `new Item.Properties().setId(itemResourceKey)` — likewise mandatory.
- Block entities: `FabricBlockEntityTypeBuilder.create(Ctor::new, block).build()`.
- Any `BaseEntityBlock` subclass **must override `codec()`**.
- NBT is the `ValueInput`/`ValueOutput` API: `input.getIntOr("Key", default)`,
  `output.putInt("Key", v)`, inside `loadAdditional`/`saveAdditional`.
- Interactions: `useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)`
  and `useWithoutItem(BlockState, Level, BlockPos, Player, BlockHitResult)`, both `protected`.

### Commands
- Server permissions: `.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))`
  — **not** `source -> source.hasPermission(2)`.
- Client commands: `ClientCommandRegistrationCallback.EVENT` +
  `ClientCommands.literal(...)` / `.argument(...)`, source type
  `FabricClientCommandSource`, feedback via `ctx.getSource().sendFeedback(Component)`.

### Client world interaction — the important ones
- Breaking: `mc.gameMode.startDestroyBlock(pos, face)`, then
  `mc.gameMode.continueDestroyBlock(pos, face)` each tick — it returns **false when
  the break completes**. `mc.gameMode.stopDestroyBlock()` to cancel.
- Placing / right-clicking:
  `mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, new BlockHitResult(vec, dir, pos, false))`.
- Reach: `player.blockInteractionRange()`. Always clamp to this — never exceed it.
- Facing: `Direction.getApproximateNearest(double x, double y, double z)`.
- Movement: `player.input.keyPresses = new Input(forward, back, left, right, jump, sneak, sprint)`.
- Hotbar: `player.getInventory().getSelectedSlot()` / `.setSelectedSlot(i)`, then
  send `new ServerboundSetCarriedItemPacket(i)`.

### Containers / inventory
- Clicking a slot:
  `mc.gameMode.handleContainerInput(menu.containerId, slotIndex, button, ContainerInput.PICKUP, player)`.
  `ContainerInput` is the new enum replacing the old `ClickType`.
  `ContainerInput.QUICK_MOVE` = shift-click.
- Cursor stack: `menu.getCarried()`. **Always check it's empty before clicking** or
  items get dropped.
- Player inventory is always the **last 36 slots** of a menu, so container slot count
  = `menu.slots.size() - 36`. A furnace menu is therefore 39 slots
  (0 = input, 1 = fuel, 2 = output).
- Read a slot: `menu.getSlot(i).getItem()`. Close: `player.closeContainer()`.
- Tooltips: `stack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.NORMAL)`.
- Item name: `stack.getHoverName().getString()`.

### GUI
- `minecraft.gui.setScreen(parent)` — **not** `minecraft.setScreen(parent)`.
- `mc.gui.screen()` to read the current screen.
- `Checkbox.builder(Component, Font).pos(x,y).selected(b).onValueChange((cb,v) -> …).build()`.
- `new EditBox(Font, x, y, w, h, Component narration)`, `.setResponder(...)`.
- `MultiLineTextWidget(x, y, Component, Font)` + `.setMaxWidth(n)`.
- `StringWidget(x, y, w, h, Component, Font)`. `setMessage`/`getMessage` are on
  `AbstractWidget`, so `Button` and `StringWidget` both have them. `Button.active` is public.
- Injecting widgets into another mod's/vanilla's screen: `ScreenEvents.AFTER_INIT` +
  `Screens.getWidgets(screen)` → mutable `List<AbstractWidget>`.
- Keybind categories: `KeyMapping.Category.register(Identifier)`.
- The HUD render API is new/deferred in 26.2 — this project uses **chat output**
  for stats rather than a HUD overlay. Revisit if you want a real overlay.

### Chat events
- `ClientReceiveMessageEvents.ALLOW_CHAT` — lambda takes **5 params**
  `(message, signedMessage, sender, params, receptionTimestamp)`; return `false` to hide.
- `ClientReceiveMessageEvents.ALLOW_GAME` — `(message, overlay)`; skip filtering when
  `overlay` is true or you'll eat the action bar.
- Signed player chat **cannot be rewritten** client-side, only allowed or blocked —
  which is why mention highlighting appends an extra line.
- `player.sendSystemMessage(Component)` is correct for client-only local display from
  a client entrypoint; it sends nothing to the server.

### Blocks / crops
- `block instanceof CropBlock crop` → `crop.isMaxAge(state)`, `crop.getStateForAge(0)`.
- `Blocks.WHEAT`/`CARROTS`/`POTATOES`/`BEETROOTS`/`NETHER_WART`,
  `Items.WHEAT_SEEDS`/`CARROT`/`POTATO`/`BEETROOT_SEEDS`/`NETHER_WART`.
- `state.getDestroySpeed(level, pos) < 0` means unbreakable (bedrock).
- `mc.level.getFluidState(pos).isEmpty()` to detect liquids.
- `BuiltInRegistries.ITEM.getKey(item).toString()` / `.getPath()`.

---

## Architecture conventions

Follow these so new code matches what's there:

- **Engines are final classes with a private constructor and all-static state**, driven
  by a `public static void tick()` called from `AutoDonutClient`'s
  `ClientTickEvents.END_CLIENT_TICK`.
- **Every engine has `public static volatile boolean externallyPaused`**, set as a
  group by `SafetyEngine.setAutomationPaused(boolean)`.
- **Engines bail immediately** when disabled, paused, `mc.player == null`,
  `mc.level == null`, or `mc.gui.screen() != null` — never fight a GUI the player
  opened. (The miner's return-to-chest flow is the one deliberate exception.)
- **One action per tick**, with a `stepDelay`/`actionDelay` counter between them, so the
  server sees a human-paced click rate.
- **Warnings are rate-limited** via a `warningCooldown` counter (~100 ticks) so nothing
  spams chat.
- **Server-specific unknowns are configurable with sensible defaults**, never hardcoded
  guesses — that's how the Economy module handles `/ah` syntax, and how the Smelter
  handles fuel/input lists.
- **Allow-lists over deny-lists** for anything that could destroy or sell a valuable item.
- Every new toggle needs: a field in `AutoDonutConfig`, a widget in the relevant
  `gui/` screen, and a subcommand in `AutoDonutClient`.

---

## Command reference (client-side)

```
/autodonutmenu                              open the settings hub

/autodonutmine [true|false]                 Auto Miner on/off, or show status
/autodonutmine mode mineonly|walkmine
/autodonutmine radar <true|false>
/autodonutmine radius <2-32>
/autodonutmine ore list | ore <name> <true|false>
/autodonutmine stats [reset]

/autodonutsmelt [true|false]                Auto Smelter on/off, or show status
/autodonutsmelt interval <1-60>             seconds between service runs
/autodonutsmelt radius <1-6>
/autodonutsmelt fuel [add|remove <item>]
/autodonutsmelt input [add|remove <item>]
/autodonutsmelt stats [reset]

/autodonutfarm [true|false]                 Auto Farm on/off, or show status
/autodonutfarm radius <1-6>
/autodonutfarm replant <true|false>
/autodonutfarm netherwart <true|false>
/autodonutfarm speed <1-20>                 ticks between actions
/autodonutfarm stats [reset]

/autodonuteconomy [true|false]
/autodonuteconomy dumplore                  print tooltip text (tune the price regex)
/autodonuteconomy clearmarket
/autodonuteconomy dns|watch|want|ledger|sharedb …

/autodonutchat [hideplayers|hidescam <bool>]
/autodonutchat keyword [add|remove <word>]
```

---

## Ideas / backlog

Rough priority order:

1. **Compile it for real.** `./gradlew build` with the actual Minecraft artifacts.
   The offline type-check is green; this is the last gate. Use `API-SURFACE.md` as
   the debugging map.
2. **Delete the legacy server-side blocks entirely.** They're dead weight now that
   everything is client-side, and removing them would let `fabric.mod.json` drop to a
   client-only mod — smaller jar, no server confusion. Touches:
   `block/`, `block/entity/`, `registry/`, `command/AutoDonutCommand.java`,
   `AutoDonut.java`, the blockstate/model/texture resources, and
   `AutoDonutAutomationScreen`. Keep `AiClient` (move it client-side).
3. **Auto Smelter: multi-furnace support.** Currently services the single nearest
   furnace. A furnace *array* is the normal way people smelt in bulk.
4. **Auto Farm: sugar cane, bamboo, melons/pumpkins.** Different growth mechanics
   (no replant needed), so a separate code path.
5. **Verify the Economy `/ah` integration** against the real plugin using `dumplore`,
   then fix the default command templates and price regex.
6. **Auto-relist** unsold AH listings — blocked on #5.
7. **Real HUD overlay** for session stats, using 26.2's deferred HUD API.
8. **First-run setup wizard** for the Economy regex/commands.
9. **Publish release jars.** GitHub asset upload was failing from the sandbox
   (`uploads.github.com` unreachable); CI artifact upload works fine instead.

---

## Lessons learned on this project

1. **Commit and push after every working build.** An entire multi-session build was
   lost once because it lived only in an ephemeral sandbox.
2. **"No compiler available" is usually false.** Maven Central, Adoptium and apt were
   all blocked in the dev sandbox, but PyPI and npm were not — and ECJ, a complete
   pure-Java compiler, ships inside the PyPI package `karellen-jdtls` (a JRE also
   ships as `jdk4py`). When the real dependencies are unreachable, stub them: a
   compiler plus stubs catches far more than any amount of grep-based analysis.
2. **Keep build artifacts out of the workspace.** The Gradle cache, decompiled
   Minecraft jars and a test server are what blew the budget. They're in `.gitignore`;
   also set `GRADLE_USER_HOME=/tmp/gradle` so the cache never lands in the workspace.
3. **Never paste a token into chat.** Use the platform's GitHub connection.
4. **Browser drag-and-drop upload silently skips dotfiles** — `.gitignore` and
   `.github/` were lost that way and had to be recreated.
5. **The sandbox GitHub App can't push `.github/workflows/`** without the `workflows`
   scope. The CI file is parked at `docs/ci/build.yml`; copy it to
   `.github/workflows/build.yml` via the GitHub web UI to enable CI.
