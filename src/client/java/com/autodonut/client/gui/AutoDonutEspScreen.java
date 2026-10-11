package com.autodonut.client.gui;

import com.autodonut.client.esp.BlockEspEngine;
import com.autodonut.client.esp.BlockEspRenderer;
import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Settings for Block ESP.
 *
 * <p>Credit: inspired by BaseFinder (Block ESP) by ShadowsIsNothing. No code
 * from that mod is used here - see CREDITS.md.
 */
public class AutoDonutEspScreen extends Screen {

	private final Screen parent;

	public AutoDonutEspScreen(Screen parent) {
		super(Component.literal("AutoDonut - Block ESP / Base Finder"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		AutoDonutConfig cfg = AutoDonutConfig.get();
		int cx = this.width / 2;
		int top = this.height / 2 - 118;

		this.addRenderableWidget(new StringWidget(cx - 120, top - 22, 240, 20, this.title, this.font));

		MultiLineTextWidget blurb = new MultiLineTextWidget(cx - 150, top, Component.literal(
				"Highlights blocks you care about near you. Read-only: it looks at block data "
						+ "the client already has and never touches anything. Inspired by BaseFinder "
						+ "by ShadowsIsNothing - credit to him, no code from it is used here."), this.font);
		blurb.setMaxWidth(300);
		this.addRenderableWidget(blurb);

		int y = top + 48;

		this.addRenderableWidget(Checkbox.builder(Component.literal("Enable Block ESP"), this.font)
				.pos(cx - 150, y).selected(cfg.espEnabled)
				.onValueChange((cb, v) -> {
					cfg.espEnabled = v;
					AutoDonutConfig.save();
					if (!v) BlockEspEngine.clear();
				})
				.build());

		y += 24;
		this.addRenderableWidget(Checkbox.builder(Component.literal("Draw boxes in the world"), this.font)
				.pos(cx - 150, y).selected(cfg.espRenderBoxes)
				.onValueChange((cb, v) -> {
					cfg.espRenderBoxes = v;
					AutoDonutConfig.save();
					BlockEspRenderer.resetFailure();
				})
				.build());

		y += 26;
		y = stepper(cx, y, "Scan radius", cfg.espRadius, 4, 128, 4,
				v -> { cfg.espRadius = v; AutoDonutConfig.save(); }, " blocks");

		y = stepper(cx, y, "Vertical radius", cfg.espVerticalRadius, 1, 128, 4,
				v -> { cfg.espVerticalRadius = v; AutoDonutConfig.save(); }, " blocks");

		y = stepper(cx, y, "Max highlighted", cfg.espMaxTargets, 16, 4096, 64,
				v -> { cfg.espMaxTargets = v; AutoDonutConfig.save(); }, "");

		y = stepper(cx, y, "Rescan every", cfg.espScanIntervalTicks, 5, 100, 5,
				v -> { cfg.espScanIntervalTicks = v; AutoDonutConfig.save(); }, " ticks");

		y += 28;
		int tracked = cfg.espTracked == null ? 0 : cfg.espTracked.size();
		this.addRenderableWidget(new StringWidget(cx - 150, y, 300, 16, Component.literal(
				"Tracking " + tracked + " block types \u2014 edit with /autodonutesp add|remove"), this.font));

		y += 18;
		String status = BlockEspRenderer.isRenderingDisabled()
				? "Box drawing stopped: " + BlockEspRenderer.lastError()
				: BlockEspEngine.hits().size() + " currently detected";
		this.addRenderableWidget(new StringWidget(cx - 150, y, 300, 16,
				Component.literal(status), this.font));

		y += 26;
		this.addRenderableWidget(Button.builder(Component.literal("Rescan now"), b -> {
			BlockEspEngine.clear();
			BlockEspRenderer.resetFailure();
			this.minecraft.gui.setScreen(new AutoDonutEspScreen(this.parent));
		}).bounds(cx - 150, y, 145, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
				.bounds(cx + 5, y, 145, 20).build());
	}

	@Override
	public void onClose() {
		AutoDonutConfig.save();
		this.minecraft.gui.setScreen(parent);
	}

	/**
	 * A labelled value with - and + buttons, matching the pattern the other
	 * AutoDonut screens already use. Returns the next free y.
	 */
	private int stepper(int cx, int y, String label, int initial, int min, int max, int step,
			java.util.function.IntConsumer onChange, String unit) {
		int[] current = { initial };
		StringWidget valueLabel = new StringWidget(cx - 150, y + 5, 150, 16,
				Component.literal(label + ": " + current[0] + unit), this.font);
		this.addRenderableWidget(valueLabel);

		this.addRenderableWidget(Button.builder(Component.literal("-"), b -> {
			current[0] = Math.max(min, current[0] - step);
			onChange.accept(current[0]);
			valueLabel.setMessage(Component.literal(label + ": " + current[0] + unit));
		}).bounds(cx + 10, y, 20, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
			current[0] = Math.min(max, current[0] + step);
			onChange.accept(current[0]);
			valueLabel.setMessage(Component.literal(label + ": " + current[0] + unit));
		}).bounds(cx + 35, y, 20, 20).build());

		return y + 24;
	}
}
