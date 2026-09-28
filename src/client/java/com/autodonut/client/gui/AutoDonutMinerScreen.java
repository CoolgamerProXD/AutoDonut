package com.autodonut.client.gui;

import com.autodonut.client.miner.AutoMinerEngine;
import com.autodonut.client.miner.OreType;
import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Fully client-side Auto Miner: no server install needed at all, since it
 * only ever automates YOUR OWN player breaking/walking with normal vanilla
 * actions - same as a human holding left-click and W.
 */
public class AutoDonutMinerScreen extends Screen {

	private final Screen parent;
	private Button modeButton;
	private StringWidget radiusLabel;

	public AutoDonutMinerScreen(Screen parent) {
		super(Component.literal("AutoDonut - Auto Miner"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		AutoDonutConfig cfg = AutoDonutConfig.get();
		int cx = this.width / 2;
		int top = 20;

		this.addRenderableWidget(new StringWidget(cx - 100, top, 200, 20, this.title, this.font));

		int y = top + 24;
		this.addRenderableWidget(Checkbox.builder(Component.literal("Enable Auto Miner"), this.font)
				.pos(cx - 150, y).selected(cfg.clientMinerEnabled)
				.onValueChange((cb, v) -> { cfg.clientMinerEnabled = v; AutoDonutConfig.save(); })
				.build());

			y += 24;
			modeButton = Button.builder(modeText(cfg), b -> {
				cfg.minerWalkEnabled = !cfg.minerWalkEnabled;
				AutoDonutConfig.save();
				// Rebuild the whole screen: the torch option below only appears in Walk+Mine mode.
				this.minecraft.gui.setScreen(new AutoDonutMinerScreen(parent));
			}).bounds(cx - 150, y, 300, 20).build();
			this.addRenderableWidget(modeButton);

		MultiLineTextWidget modeInfo = new MultiLineTextWidget(cx - 150, y + 22, Component.literal(
				"Only Mine = never moves you, just breaks whatever ore/block is in reach.  " +
				"Walk + Mine = digs a tunnel and walks forward on its own (this WILL move your camera/character - don't touch WASD/mouse while it's running)."),
				this.font);
		modeInfo.setMaxWidth(300);
		this.addRenderableWidget(modeInfo);

		y += 60;
		this.addRenderableWidget(Checkbox.builder(Component.literal("Seek out nearby ores (radar)"), this.font)
				.pos(cx - 150, y).selected(cfg.minerOreSeekingEnabled)
				.onValueChange((cb, v) -> { cfg.minerOreSeekingEnabled = v; AutoDonutConfig.save(); })
				.build());

		y += 22;
		radiusLabel = new StringWidget(cx - 150, y + 5, 140, 16,
				Component.literal("Radar radius: " + cfg.minerOreRadarRadius + " blocks"), this.font);
		this.addRenderableWidget(radiusLabel);
		this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
			cfg.minerOreRadarRadius = Math.max(2, cfg.minerOreRadarRadius - 2);
			AutoDonutConfig.save();
			radiusLabel.setMessage(Component.literal("Radar radius: " + cfg.minerOreRadarRadius + " blocks"));
		}).bounds(cx, y, 20, 20).build());
		this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
			cfg.minerOreRadarRadius = Math.min(32, cfg.minerOreRadarRadius + 2);
			AutoDonutConfig.save();
			radiusLabel.setMessage(Component.literal("Radar radius: " + cfg.minerOreRadarRadius + " blocks"));
		}).bounds(cx + 25, y, 20, 20).build());

			y += 26;
			this.addRenderableWidget(Checkbox.builder(Component.literal("Clear whole ore veins, not just one block"), this.font)
					.pos(cx - 150, y).selected(cfg.minerVeinClearEnabled)
					.onValueChange((cb, v) -> { cfg.minerVeinClearEnabled = v; AutoDonutConfig.save(); })
					.build());

			y += 22;
			this.addRenderableWidget(Checkbox.builder(Component.literal("Stop before my pickaxe breaks"), this.font)
					.pos(cx - 150, y).selected(cfg.minerToolDurabilityGuardEnabled)
					.onValueChange((cb, v) -> { cfg.minerToolDurabilityGuardEnabled = v; AutoDonutConfig.save(); })
					.build());

			y += 22;
			boolean walkMode = cfg.minerWalkEnabled;
			boolean hasTorches = AutoMinerEngine.playerHasTorches(Minecraft.getInstance().player);
			if (walkMode && hasTorches) {
				this.addRenderableWidget(Checkbox.builder(Component.literal("Auto-place torches while tunneling"), this.font)
						.pos(cx - 150, y).selected(cfg.minerAutoTorchEnabled)
						.onValueChange((cb, v) -> { cfg.minerAutoTorchEnabled = v; AutoDonutConfig.save(); })
						.build());
			} else {
				cfg.minerAutoTorchEnabled = false;
				String why = !walkMode ? "Switch to Walk + Mine mode" : "Carry some torches";
				this.addRenderableWidget(new StringWidget(cx - 150, y + 2, 300, 12,
						Component.literal(why + " to unlock auto-torch placement."), this.font));
			}

			y += 22;
			this.addRenderableWidget(Checkbox.builder(Component.literal("Auto-return to a chest at my start point when full (experimental)"), this.font)
					.pos(cx - 150, y).selected(cfg.minerAutoReturnEnabled)
					.onValueChange((cb, v) -> { cfg.minerAutoReturnEnabled = v; AutoDonutConfig.save(); })
					.build());

			y += 26;
			this.addRenderableWidget(Button.builder(Component.literal("Announce session stats in chat"), b ->
					AutoMinerEngine.announceStats())
					.bounds(cx - 150, y, 300, 20).build());

			y += 30;
			this.addRenderableWidget(new StringWidget(cx - 150, y, 300, 12,
					Component.literal("Ores to mine (untick any you want left alone):"), this.font));

		y += 16;
		OreType[] ores = OreType.values();
		int rowStartY = y;
		int colWidth = 150;
		int rows = (ores.length + 1) / 2;
		for (int index = 0; index < ores.length; index++) {
			OreType ore = ores[index];
			int r = index % rows;
			int c = index / rows;
			this.addRenderableWidget(Checkbox.builder(Component.literal(ore.displayName), this.font)
					.pos(cx - 150 + c * colWidth, rowStartY + r * 20)
					.selected(ore.isEnabled(cfg))
					.onValueChange((cb, v) -> {
						if (v) cfg.minerDisabledOres.remove(ore.key);
						else if (!cfg.minerDisabledOres.contains(ore.key)) cfg.minerDisabledOres.add(ore.key);
						AutoDonutConfig.save();
					})
					.build());
		}

		y = rowStartY + rows * 20 + 10;
		this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
				.bounds(cx - 50, y, 100, 20).build());
	}

	private static Component modeText(AutoDonutConfig cfg) {
		return Component.literal("Mode: " + (cfg.minerWalkEnabled ? "Walk + Mine (tunnels forward)" : "Only Mine (stay in place)"));
	}

	@Override
	public void onClose() {
		AutoDonutConfig.save();
		this.minecraft.gui.setScreen(parent);
	}
}
