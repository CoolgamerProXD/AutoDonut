package net.minecraft.commands;
public class CommandSourceStack {
  public boolean hasPermission(int level) { return false; }
  public void sendSuccess(java.util.function.Supplier<net.minecraft.network.chat.Component> msg, boolean broadcast) { }
  public void sendFailure(net.minecraft.network.chat.Component msg) { }
  public net.minecraft.server.level.ServerPlayer getPlayerOrException() throws Exception { return null; }
  public net.minecraft.server.level.ServerLevel getLevel() { return null; }
  public net.minecraft.server.level.ServerPlayer getPlayer() { return null; }
  public void sendSystemMessage(net.minecraft.network.chat.Component msg) { }
}
