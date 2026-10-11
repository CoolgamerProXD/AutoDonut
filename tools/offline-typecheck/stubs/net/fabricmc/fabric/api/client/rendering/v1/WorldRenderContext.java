package net.fabricmc.fabric.api.client.rendering.v1;
public interface WorldRenderContext {
  com.mojang.blaze3d.vertex.PoseStack matrixStack();
  net.minecraft.client.renderer.MultiBufferSource consumers();
  net.minecraft.client.Camera camera();
}
