package net.minecraft.client;
public class Minecraft {
  public static Minecraft getInstance() { return null; }
  public net.minecraft.client.player.LocalPlayer player;
  public net.minecraft.client.multiplayer.ClientLevel level;
  public net.minecraft.client.gui.Gui gui;
  public net.minecraft.client.multiplayer.MultiPlayerGameMode gameMode;
  public net.minecraft.client.gui.Font font;
  public net.minecraft.client.gui.screens.Screen screen;
  public net.minecraft.world.phys.HitResult hitResult;
  public void setScreen(net.minecraft.client.gui.screens.Screen s) { }
  public void execute(Runnable r) { }
  public boolean isSingleplayer() { return false; }
}
