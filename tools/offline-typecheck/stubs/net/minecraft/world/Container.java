package net.minecraft.world;
import net.minecraft.world.item.ItemStack;
public interface Container {
  int getContainerSize();
  boolean isEmpty();
  ItemStack getItem(int slot);
  ItemStack removeItem(int slot, int amount);
  ItemStack removeItemNoUpdate(int slot);
  void setItem(int slot, ItemStack stack);
  void setChanged();
  boolean stillValid(net.minecraft.world.entity.player.Player player);
  default int getMaxStackSize() { return 64; }
  default void clearContent() { }
  default boolean canPlaceItem(int slot, ItemStack stack) { return true; }
}
