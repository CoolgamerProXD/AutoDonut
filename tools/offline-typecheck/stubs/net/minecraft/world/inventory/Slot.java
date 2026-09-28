package net.minecraft.world.inventory;
import net.minecraft.world.item.ItemStack;
public class Slot {
  public final int index = 0;
  public ItemStack getItem() { return null; }
  public boolean hasItem() { return false; }
  public net.minecraft.world.Container container;
  public int getContainerSlot() { return 0; }
}
