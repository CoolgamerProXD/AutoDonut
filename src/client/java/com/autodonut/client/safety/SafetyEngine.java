package com.autodonut.client.safety;

import com.autodonut.client.miner.AutoMinerEngine;
import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;

/**
 * Small, all-opt-in, all-off-by-default safety helpers. Every action here is
 * something a real player could do (eating, pressing a hotbar number,
 * jumping) - it just does it for you at the right moment.
 */
public final class SafetyEngine {

    private static final Random RANDOM = new Random();
    private static boolean threatActive = false;
    private static int afkCooldownTicks = randomAfkInterval();

    private SafetyEngine() {}

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        AutoDonutConfig cfg = AutoDonutConfig.get();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        tickThreatPause(mc, cfg, player);
        tickAutoEat(mc, cfg, player);
        tickAntiAfk(mc, cfg, player);
    }

    private static void tickThreatPause(Minecraft mc, AutoDonutConfig cfg, LocalPlayer player) {
        if (!cfg.safetyThreatPauseEnabled) {
            if (threatActive) {
                threatActive = false;
                AutoMinerEngine.externallyPaused = false;
            }
            return;
        }

        double r = Math.max(2.0, Math.min(64.0, cfg.safetyThreatRadius));
        AABB box = player.getBoundingBox().inflate(r);
        List<Entity> threats = mc.level.getEntities(player, box, e -> {
            if (e instanceof Monster) return true;
            return cfg.safetyThreatIncludesPlayers && e instanceof Player && e != player;
        });

        boolean nowThreatened = !threats.isEmpty();
        if (nowThreatened && !threatActive) {
            threatActive = true;
            AutoMinerEngine.externallyPaused = true;
            String what = threats.get(0).getName().getString();
            player.sendSystemMessage(Component.literal("[AutoDonut Safety] Paused automation - " + what + " is nearby!"));
            DiscordWebhook.send(cfg.safetyDiscordWebhookUrl, "AutoDonut: automation paused, " + what + " got close to " + player.getName().getString() + ".");
        } else if (!nowThreatened && threatActive) {
            threatActive = false;
            AutoMinerEngine.externallyPaused = false;
            player.sendSystemMessage(Component.literal("[AutoDonut Safety] Area clear, resuming automation."));
        }
    }

    private static void tickAutoEat(Minecraft mc, AutoDonutConfig cfg, LocalPlayer player) {
        if (!cfg.safetyAutoEatEnabled) return;
        if (player.isUsingItem()) return;
        if (player.getFoodData().getFoodLevel() > cfg.safetyAutoEatHungerThreshold) return;

        int foodSlot = findFoodInHotbar(player);
        if (foodSlot < 0) return;

        if (player.getInventory().getSelectedSlot() != foodSlot) {
            player.getInventory().setSelectedSlot(foodSlot);
            player.connection.send(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(foodSlot));
            return; // give the slot-switch a tick to land before eating
        }

        mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
    }

    private static int findFoodInHotbar(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.has(net.minecraft.core.component.DataComponents.FOOD)) {
                return i;
            }
        }
        return -1;
    }

    private static void tickAntiAfk(Minecraft mc, AutoDonutConfig cfg, LocalPlayer player) {
        if (!cfg.safetyAntiAfkEnabled) return;
        // Don't bother if the Auto Miner is already generating real activity.
        if (cfg.clientMinerEnabled && !AutoMinerEngine.externallyPaused) return;

        if (afkCooldownTicks-- > 0) return;
        afkCooldownTicks = randomAfkInterval();
        if (player.onGround()) {
            player.input.keyPresses = new Input(false, false, false, false, true, false, false);
        }
    }

    private static int randomAfkInterval() {
        return 3600 + RANDOM.nextInt(2400); // roughly 3-5 minutes at 20 ticks/sec
    }
}
