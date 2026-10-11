package com.autodonut.client.esp;

import com.autodonut.config.AutoDonutConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Draws boxes around whatever {@link BlockEspEngine} has found.
 *
 * <p><b>This is the single most version-fragile class in AutoDonut.</b>
 * Minecraft's rendering internals change more often than anything else, and
 * the offline type-check cannot prove these signatures against the real 26.2
 * jar. It is deliberately isolated for that reason:
 *
 * <ul>
 *   <li>Nothing else in the mod imports this class except the one registration
 *       call in {@code AutoDonutClient}.</li>
 *   <li>{@link BlockEspEngine} has zero rendering code, so detection,
 *       configuration and {@code /autodonutesp list} all keep working even if
 *       this file has to be disabled.</li>
 *   <li>Every draw is wrapped - one bad frame disables the renderer and tells
 *       you in chat instead of spamming the log sixty times a second.</li>
 * </ul>
 *
 * <p>Credit for the idea: BaseFinder (Block ESP) by ShadowsIsNothing. No code
 * from that mod is used here - see CREDITS.md.
 */
public final class BlockEspRenderer {

	private BlockEspRenderer() {}

	/** Flipped if rendering throws, so a broken API can't spam or tank the frame rate. */
	private static volatile boolean renderingDisabled = false;
	private static volatile String lastError = null;

	public static boolean isRenderingDisabled() {
		return renderingDisabled;
	}

	public static String lastError() {
		return lastError;
	}

	public static void resetFailure() {
		renderingDisabled = false;
		lastError = null;
	}

	/** Registered against WorldRenderEvents.AFTER_TRANSLUCENT. */
	public static void render(WorldRenderContext context) {
		if (renderingDisabled) return;

		AutoDonutConfig cfg = AutoDonutConfig.get();
		if (!cfg.espEnabled || !cfg.espRenderBoxes) return;

		List<BlockEspEngine.Hit> hits = BlockEspEngine.hits();
		if (hits.isEmpty()) return;

		try {
			drawBoxes(context, hits, cfg);
		} catch (Throwable t) {
			// Includes LinkageError: exactly what a changed signature looks like
			// at runtime. Disable rather than fail every frame.
			renderingDisabled = true;
			lastError = t.getClass().getSimpleName() + ": " + t.getMessage();
		}
	}

	private static void drawBoxes(WorldRenderContext context, List<BlockEspEngine.Hit> hits, AutoDonutConfig cfg) {
		PoseStack poseStack = context.matrixStack();
		MultiBufferSource consumers = context.consumers();
		if (poseStack == null || consumers == null) return;

		Vec3 camera = context.camera().getPosition();
		float alpha = clamp01(cfg.espBoxOpacity);

		// NOTE: RenderType.lines() is depth-tested, so boxes are hidden behind
		// terrain. Drawing *through* walls needs a render layer with the depth
		// test disabled, which means a custom RenderType - and that is exactly
		// the kind of signature this project cannot verify offline. Rather than
		// ship a "see through walls" toggle that silently does nothing, the
		// boxes are honest about being depth-tested and `/autodonutesp list`
		// gives you the coordinates regardless of line of sight. See the
		// Block ESP section of the README for the one-line change to make once
		// someone can test against the real 26.2 jar.
		VertexConsumer buffer = consumers.getBuffer(RenderType.lines());

		int limit = Math.max(1, cfg.espMaxTargets);
		int drawn = 0;

		poseStack.pushPose();
		poseStack.translate(-camera.x, -camera.y, -camera.z);
		try {
			for (BlockEspEngine.Hit hit : hits) {
				if (drawn++ >= limit) break;
				BlockPos pos = hit.pos();
				int rgb = hit.rgb();
				float r = ((rgb >> 16) & 0xFF) / 255.0f;
				float g = ((rgb >> 8) & 0xFF) / 255.0f;
				float b = (rgb & 0xFF) / 255.0f;

				ShapeRenderer.renderLineBox(poseStack, buffer,
						pos.getX(), pos.getY(), pos.getZ(),
						pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0,
						r, g, b, alpha);
			}
		} finally {
			poseStack.popPose();
		}
	}

	private static float clamp01(double v) {
		if (v < 0.05) return 0.05f;
		if (v > 1.0) return 1.0f;
		return (float) v;
	}
}
