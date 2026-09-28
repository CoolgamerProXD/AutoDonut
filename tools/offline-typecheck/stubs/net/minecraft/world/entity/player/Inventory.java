package net.minecraft.world.entity.player;
import net.minecraft.world.item.ItemStack;
public class Inventory {
  public int selected;
  public ItemStack getItem(int slot) { return null; }
  public void setItem(int slot, ItemStack stack) { }
  public int getContainerSize() { return 36; }
  public int getSelectedSlot() { return 0; }
  public void setSelectedSlot(int slot) { }
  public ItemStack getSelected() { return null; }
  public boolean add(ItemStack stack) { return false; }
  public int getFreeSlot() { return -1; }
}
