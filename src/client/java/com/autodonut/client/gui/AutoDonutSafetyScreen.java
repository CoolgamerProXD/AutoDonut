package com.autodonut.client.gui;

import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** All opt-in, all off by default. Every action is one a real player could take. */
public class AutoDonutSafetyScreen extends Screen {

    private final Screen parent;

    public AutoDonutSafetyScreen(Screen parent) {
        super(Component.literal("AutoDonut - Safety"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        AutoDonutConfig cfg = AutoDonutConfig.get();
        int cx = this.width / 2;
        int left = cx - 150;
        int y = 16;

        this.addRenderableWidget(new StringWidget(left, y, 300, 16, this.title, this.font));
        y += 24;

        this.addRenderableWidget(Checkbox.builder(Component.literal("Auto-eat when hungry"), this.font)
                .pos(left, y).selected(cfg.safetyAutoEatEnabled)
                .onValueChange((cb, v) -> { cfg.safetyAutoEatEnabled = v; AutoDonutConfig.save(); })
                .build());
        y += 22;
        StringWidget hungerLabel = new StringWidget(left, y + 4, 200, 12,
                Component.literal("Eat below hunger: " + cfg.safetyAutoEatHungerThreshold + "/20"), this.font);
        this.addRenderableWidget(hungerLabel);
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            cfg.safetyAutoEatHungerThreshold = Math.max(1, cfg.safetyAutoEatHungerThreshold - 1);
            AutoDonutConfig.save();
            hungerLabel.setMessage(Component.literal("Eat below hunger: " + cfg.safetyAutoEatHungerThreshold + "/20"));
        }).bounds(left + 205, y, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            cfg.safetyAutoEatHungerThreshold = Math.min(19, cfg.safetyAutoEatHungerThreshold + 1);
            AutoDonutConfig.save();
            hungerLabel.setMessage(Component.literal("Eat below hunger: " + cfg.safetyAutoEatHungerThreshold + "/20"));
        }).bounds(left + 230, y, 20, 20).build());
        y += 26;

        this.addRenderableWidget(Checkbox.builder(Component.literal("Pause automation when a mob/player gets close"), this.font)
                .pos(left, y).selected(cfg.safetyThreatPauseEnabled)
                .onValueChange((cb, v) -> { cfg.safetyThreatPauseEnabled = v; AutoDonutConfig.save(); })
                .build());
        y += 22;
        this.addRenderableWidget(Checkbox.builder(Component.literal("...also count other players as a threat"), this.font)
                .pos(left + 10, y).selected(cfg.safetyThreatIncludesPlayers)
                .onValueChange((cb, v) -> { cfg.safetyThreatIncludesPlayers = v; AutoDonutConfig.save(); })
                .build());
        y += 22;
        StringWidget radiusLabel = new StringWidget(left, y + 4, 200, 12,
                Component.literal("Threat radius: " + (int) cfg.safetyThreatRadius + " blocks"), this.font);
        this.addRenderableWidget(radiusLabel);
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
            cfg.safetyThreatRadius = Math.max(2, cfg.safetyThreatRadius - 2);
            AutoDonutConfig.save();
            radiusLabel.setMessage(Component.literal("Threat radius: " + (int) cfg.safetyThreatRadius + " blocks"));
        }).bounds(left + 205, y, 20, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            cfg.safetyThreatRadius = Math.min(48, cfg.safetyThreatRadius + 2);
            AutoDonutConfig.save();
            radiusLabel.setMessage(Component.literal("Threat radius: " + (int) cfg.safetyThreatRadius + " blocks"));
        }).bounds(left + 230, y, 20, 20).build());
        y += 26;

        this.addRenderableWidget(Checkbox.builder(Component.literal("Anti-AFK-kick nudge (occasional jump when idle)"), this.font)
                .pos(left, y).selected(cfg.safetyAntiAfkEnabled)
                .onValueChange((cb, v) -> { cfg.safetyAntiAfkEnabled = v; AutoDonutConfig.save(); })
                .build());
        y += 26;

        this.addRenderableWidget(new StringWidget(left, y, 150, 12,
                Component.literal("Discord webhook URL (optional):"), this.font));
        y += 14;
        EditBox webhookBox = new EditBox(this.font, left, y, 300, 16, Component.literal("webhook url"));
        webhookBox.setMaxLength(300);
        webhookBox.setValue(cfg.safetyDiscordWebhookUrl == null ? "" : cfg.safetyDiscordWebhookUrl);
        webhookBox.setResponder(v -> { cfg.safetyDiscordWebhookUrl = v; AutoDonutConfig.save(); });
        this.addRenderableWidget(webhookBox);
        y += 24;

        MultiLineTextWidget note = new MultiLineTextWidget(left, y, Component.literal(
                "Everything above is off by default and only ever does something a real player could do " +
                "(eating, pressing a hotbar key, jumping). If a webhook URL is set, a threat-pause also pings it."), this.font);
        note.setMaxWidth(300);
        this.addRenderableWidget(note);
        y += 46;

        this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
                .bounds(cx - 50, y, 100, 20).build());
    }

    @Override
    public void onClose() {
        AutoDonutConfig.save();
        this.minecraft.gui.setScreen(parent);
    }
}
