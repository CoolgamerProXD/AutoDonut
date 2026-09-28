package net.minecraft.world.item;
public class Item implements net.minecraft.world.level.ItemLike {
  public net.minecraft.network.chat.Component getName() { return null; }
  public Item asItem() { return this; }
  public interface TooltipContext { TooltipContext EMPTY = null; static TooltipContext of(net.minecraft.world.level.Level level) { return null; } }
  public static class Properties {
    public Properties stacksTo(int n) { return this; }
    public Properties setId(net.minecraft.resources.ResourceKey<Item> id) { return this; }
    public Properties useBlockDescriptionPrefix() { return this; }
  }
  public interface TooltipContextX { }
}
