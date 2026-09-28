package net.fabricmc.fabric.api.client.command.v2;
public interface FabricClientCommandSource {
  void sendFeedback(net.minecraft.network.chat.Component message);
  void sendError(net.minecraft.network.chat.Component message);
  net.minecraft.client.Minecraft getClient();
  net.minecraft.client.player.LocalPlayer getPlayer();
}
