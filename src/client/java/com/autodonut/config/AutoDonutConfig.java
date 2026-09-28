package com.autodonut.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple JSON-backed configuration for AutoDonut.
 * Stored at config/autodonut.json in the game/server directory.
 * Holds the master "automate" toggle, per-machine toggles, and the AI settings
 * (including the API key, which is only ever sent directly to the configured
 * AI provider endpoint - never anywhere else).
 */
public class AutoDonutConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;
    private static AutoDonutConfig instance;

    // --- AI advisor (optional; no network request is made without a key) ---
    public String aiApiKey = "";
    public String aiBaseUrl = "https://api.openai.com/v1/chat/completions";
    public String aiModel = "gpt-4o-mini";

    // --- Chat modifiers (client-side only; these never affect what you send,
    //     only what you personally see) ---
    public boolean chatHidePlayerMessages = false;
    public boolean chatHideScamKeywordMessages = true;
    public java.util.List<String> chatScamKeywords = new java.util.ArrayList<>(java.util.List.of("tp"));

    // --- Client-side Auto Miner (100% client-side: it just automates YOUR
    //     own player breaking/walking, using the same actions and timing a
    //     real human clicking/walking would produce. No server-side mod
    //     needed for this - works on any vanilla server.) ---
    public boolean clientMinerEnabled = false;
    // false = "Mine Only" (never moves you, just auto-breaks); true = "Walk + Mine" (digs a tunnel and walks forward)
    public boolean minerWalkEnabled = false;
    public boolean minerOreSeekingEnabled = true;
    public int minerOreRadarRadius = 8;
    // Ore keys (see OreType) the user has turned OFF. Empty by default = every known ore is fair game.
    public java.util.List<String> minerDisabledOres = new java.util.ArrayList<>();

    // --- Economy tab + Chest Sell (100% client-side: it only ever reads the
    //     contents of GUIs YOU have open, and clicks slots / types chat
    //     commands YOU could type yourself - the server sees normal player
    //     actions). Built around the server's real /ah player auction house.
    //     None of the command syntax below is guaranteed to match the real
    //     plugin - these are sensible defaults you can fix in the Economy
    //     settings screen (or with /autodonuteconomy) if they don't work. ---
    public boolean economyEnabled = true;
    // Case-insensitive text that must appear in a GUI's title for AutoDonut to treat it as the auction house.
    public String ahScreenTitleKeyword = "Auction";
    // Run while holding the item, with {price} substituted. No leading slash (typed the same way vanilla chat strips it).
    public String ahSellCommandTemplate = "ah sell {price}";
    // {item} is substituted with the item's search name. Not required for buying (buying is a direct GUI click), just a handy manual search shortcut on the Economy overlay.
    public String ahSearchCommandTemplate = "ah search {item}";
    // Regex used to find a price inside an item's tooltip/lore lines. Must have exactly one capturing group with just the number.
    public String economyPriceLorePattern = "\\$([0-9][0-9,]*(?:\\.[0-9]+)?)";
    // A listing must be priced at least this % below the computed market value to show up as a "deal".
    public double economyDealThresholdPercent = 20.0;
    // Chest Sell tiers, as a % discount/premium off the computed market value.
    public double economySellBelowMarketPercent = 10.0;
    public double economySellAboveMarketPercent = 15.0;
    // Minimum number of real listings AutoDonut must have seen for an item before it trusts a market value for it.
    public int economyMinListingsForPrice = 3;
    public int economyPriceDecimalPlaces = 0;
    // Hotbar slot (0-8, i.e. 1-9) Chest Sell stages items into one at a time. Must be empty before Chest Sell starts.
    public int economyChestSellHotbarSlot = 8;
    // Item ids (e.g. "minecraft:diamond") Chest Sell will never sell, no matter which chest they're in.
    public java.util.List<String> economyDoNotSellList = new java.util.ArrayList<>();
    // Item ids AutoDonut is watching - alert in chat when a listing at/under the target price is seen.
    public java.util.Map<String, Double> economyWatchlist = new java.util.LinkedHashMap<>();
    // Item ids -> quantity still wanted (a simple shopping list; decremented optimistically whenever you buy a matching deal).
    public java.util.Map<String, Integer> economyWantList = new java.util.LinkedHashMap<>();
    // Quick-Sell keybind: sells whatever you're holding at this tier ("AT_MARKET", "BELOW_MARKET", "ABOVE_MARKET") via the AH sell command.
    public String quickSellTier = "AT_MARKET";
    // If true, the Quick-Sell keybind uses flatSellCommandTemplate (sell-to-game) instead of the AH tiered price.
    public boolean quickSellUseFlatSell = false;
    // Flat "sell to the game" command, run while holding the item (no {price} - the server decides the price).
    public String flatSellCommandTemplate = "sell hand";
    // Regex (1 capture group = the $ amount) used to learn what the flat /sell command actually paid, from the server's own confirmation chat line.
    public String flatSellConfirmPattern = "\\$([0-9][0-9,]*(?:\\.[0-9]+)?)";

    // --- Auto Miner extras ---
    // Only ever placed when minerWalkEnabled (Walk + Mine) is on AND you're actually carrying torches - see AutoDonutMinerScreen.
    public boolean minerAutoTorchEnabled = false;
    public int minerTorchSpacing = 6;
    public boolean minerVeinClearEnabled = true;
    public boolean minerToolDurabilityGuardEnabled = true;
    // Stops (and warns) once the held tool has this many uses or fewer left.
    public int minerToolDurabilityGuardThreshold = 15;
    // Opt-in: retraces its own dug tunnel back to where it started when your inventory is full, deposits into a
    // chest placed right at your starting position, then retraces forward again to resume exactly where it left off.
    // This is NOT generic navigation/waypoints - it only ever re-walks the exact path it already dug.
    public boolean minerAutoReturnEnabled = false;

    // --- Safety (100% client-side, all opt-in/off by default) ---
    public boolean safetyAutoEatEnabled = false;
    public int safetyAutoEatHungerThreshold = 14; // out of 20
    public boolean safetyThreatPauseEnabled = false;
    public double safetyThreatRadius = 12.0;
    public boolean safetyThreatIncludesPlayers = true;
    public boolean safetyAntiAfkEnabled = false;
    public String safetyDiscordWebhookUrl = "";

    // --- Chat modifier extras ---
    public boolean chatHideJoinLeave = false;
    public boolean chatHighlightMentions = true;
    public boolean chatKeywordsAreRegex = false;

    // --- Client-side Auto Smelter (100% client-side: it tends ordinary
    //     VANILLA furnaces/blast furnaces/smokers that are already in your
    //     world, by opening them and moving items exactly the way you would
    //     by hand. No modded block, nothing installed on the server.) ---
    public boolean clientSmelterEnabled = false;
    // How far away (in blocks) to look for a furnace to tend. Clamped to your real reach at runtime.
    public int smelterSearchRadius = 4;
    // Seconds to wait between service runs. Each run opens the furnace, empties the output, tops up fuel/input, and closes again.
    public int smelterIntervalSeconds = 5;
    public boolean smelterTakeOutput = true;
    public boolean smelterInsertFuel = true;
    public boolean smelterInsertInput = true;
    // Items treated as furnace fuel. Add your own if you burn something unusual.
    public java.util.List<String> smelterFuelItems = new java.util.ArrayList<>(java.util.List.of(
            "minecraft:coal", "minecraft:charcoal", "minecraft:coal_block",
            "minecraft:blaze_rod", "minecraft:dried_kelp_block"));
    // Items AutoDonut is allowed to feed INTO a furnace. Deliberately an allow-list so it can
    // never shovel something valuable into a furnace by mistake. Edit freely in the settings screen.
    public java.util.List<String> smelterInputItems = new java.util.ArrayList<>(java.util.List.of(
            "minecraft:raw_iron", "minecraft:raw_gold", "minecraft:raw_copper",
            "minecraft:sand", "minecraft:red_sand", "minecraft:cobblestone",
            "minecraft:cobbled_deepslate", "minecraft:clay_ball", "minecraft:netherrack",
            "minecraft:kelp", "minecraft:potato", "minecraft:beef", "minecraft:porkchop",
            "minecraft:chicken", "minecraft:mutton", "minecraft:cod", "minecraft:salmon"));

    // --- Client-side Auto Farm (100% client-side: it breaks and replants
    //     ordinary VANILLA crops around you using the same break/place
    //     actions and timings you'd produce by hand. No modded block,
    //     nothing installed on the server.) ---
    public boolean clientFarmEnabled = false;
    // Horizontal radius around you to harvest, in blocks. Clamped to your real reach at runtime.
    public int farmRadius = 4;
    // Replant the crop immediately after harvesting it (needs the matching seed in your hotbar).
    public boolean farmReplant = true;
    // Also harvest fully-grown nether wart.
    public boolean farmIncludeNetherWart = true;
    // Ticks between harvest actions. Lower = faster, but less human-looking. 4 = five harvests/second.
    public int farmActionIntervalTicks = 4;

    public static synchronized AutoDonutConfig get() {
        if (instance == null) {
            load();
        }
        return instance;
    }

    public static synchronized void load() {
        try {
            Path dir = FabricLoader.getInstance().getConfigDir();
            configPath = dir.resolve("autodonut.json");
            if (Files.exists(configPath)) {
                try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                    instance = GSON.fromJson(reader, AutoDonutConfig.class);
                }
            }
            if (instance == null) {
                instance = new AutoDonutConfig();
            }
            save();
        } catch (IOException e) {
            instance = new AutoDonutConfig();
        }
    }

    public static synchronized void save() {
        try {
            if (configPath == null) {
                configPath = FabricLoader.getInstance().getConfigDir().resolve("autodonut.json");
            }
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                GSON.toJson(instance == null ? new AutoDonutConfig() : instance, writer);
            }
        } catch (IOException ignored) {
            // Non-fatal: config just won't persist this session.
        }
    }
}
