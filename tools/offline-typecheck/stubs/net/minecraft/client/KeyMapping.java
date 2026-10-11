package net.minecraft.client;
public class KeyMapping {
  public KeyMapping(String name, com.mojang.blaze3d.platform.InputConstants.Type type, int code, Category category) { }
  public KeyMapping(String name, int code, String category) { }
  public boolean consumeClick() { return false; }
  public boolean isDown() { return false; }
  public static class Category {
    public static Category register(net.minecraft.resources.Identifier id) { return null; }
  }
}
