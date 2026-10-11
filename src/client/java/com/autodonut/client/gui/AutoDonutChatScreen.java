package com.autodonut.client.gui;

import com.autodonut.config.AutoDonutConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Client-side-only chat filtering: hide player chat, and/or hide likely-scam ("tp") messages. */
public class AutoDonutChatScreen extends Screen {

	private final Screen parent;
	private EditBox keywordBox;
	private StringWidget keywordListLabel;

	public AutoDonutChatScreen(Screen parent) {
		super(Component.literal("AutoDonut - Chat Modifiers"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		AutoDonutConfig cfg = AutoDonutConfig.get();
		int cx = this.width / 2;
		int top = this.height / 2 - 100;

		this.addRenderableWidget(new StringWidget(cx - 100, top - 20, 200, 20, this.title, this.font));

		MultiLineTextWidget info = new MultiLineTextWidget(cx - 140, top, Component.literal(
				"100% client-side: only changes what YOU see. No server install needed, works on any server."),
				this.font);
		info.setMaxWidth(280);
		this.addRenderableWidget(info);

		int y = top + 30;
		this.addRenderableWidget(Checkbox.builder(Component.literal("Hide all player chat messages"), this.font)
				.pos(cx - 130, y).selected(cfg.chatHidePlayerMessages)
				.onValueChange((cb, v) -> { cfg.chatHidePlayerMessages = v; AutoDonutConfig.save(); })
				.build());

		y += 22;
		this.addRenderableWidget(Checkbox.builder(Component.literal("Hide likely-scam messages (contains \"tp\", etc.)"), this.font)
				.pos(cx - 130, y).selected(cfg.chatHideScamKeywordMessages)
				.onValueChange((cb, v) -> { cfg.chatHideScamKeywordMessages = v; AutoDonutConfig.save(); })
				.build());

		y += 26;
		keywordListLabel = new StringWidget(cx - 130, y, 280, 12, keywordsText(cfg), this.font);
		this.addRenderableWidget(keywordListLabel);

		y += 18;
		keywordBox = new EditBox(this.font, cx - 130, y, 180, 20, Component.literal("Add keyword"));
		keywordBox.setMaxLength(32);
		this.addRenderableWidget(keywordBox);

		this.addRenderableWidget(Button.builder(Component.literal("Add"), b -> {
			String word = keywordBox.getValue().trim().toLowerCase();
			if (!word.isEmpty() && !cfg.chatScamKeywords.contains(word)) {
				cfg.chatScamKeywords.add(word);
				AutoDonutConfig.save();
				keywordListLabel.setMessage(keywordsText(cfg));
				keywordBox.setValue("");
			}
		}).bounds(cx + 55, y, 75, 20).build());

		y += 26;
		this.addRenderableWidget(Button.builder(Component.literal("Remove last keyword"), b -> {
			if (!cfg.chatScamKeywords.isEmpty()) {
				cfg.chatScamKeywords.remove(cfg.chatScamKeywords.size() - 1);
				AutoDonutConfig.save();
				keywordListLabel.setMessage(keywordsText(cfg));
			}
		}).bounds(cx - 130, y, 260, 20).build());

		y += 24;
		this.addRenderableWidget(Checkbox.builder(Component.literal("Treat keywords as regex (advanced)"), this.font)
				.pos(cx - 130, y).selected(cfg.chatKeywordsAreRegex)
				.onValueChange((cb, v) -> { cfg.chatKeywordsAreRegex = v; AutoDonutConfig.save(); })
				.build());

		y += 22;
		this.addRenderableWidget(Checkbox.builder(Component.literal("Hide join/leave messages"), this.font)
				.pos(cx - 130, y).selected(cfg.chatHideJoinLeave)
				.onValueChange((cb, v) -> { cfg.chatHideJoinLeave = v; AutoDonutConfig.save(); })
				.build());

		y += 22;
		this.addRenderableWidget(Checkbox.builder(Component.literal("Highlight messages mentioning your name"), this.font)
				.pos(cx - 130, y).selected(cfg.chatHighlightMentions)
				.onValueChange((cb, v) -> { cfg.chatHighlightMentions = v; AutoDonutConfig.save(); })
				.build());

		y += 30;
		this.addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
				.bounds(cx - 50, y, 100, 20).build());
	}

	private static Component keywordsText(AutoDonutConfig cfg) {
		return Component.literal("Keywords: " + cfg.chatScamKeywords);
	}

	@Override
	public void onClose() {
		AutoDonutConfig.save();
		this.minecraft.gui.setScreen(parent);
	}
}
