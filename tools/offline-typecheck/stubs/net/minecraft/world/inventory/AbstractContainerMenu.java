package net.minecraft.world.inventory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
public abstract class AbstractContainerMenu {
  public int containerId;
  public NonNullList<Slot> slots = NonNullList.create();
  public Slot getSlot(int i) { return null; }
  public ItemStack getCarried() { return null; }
  public java.util.List<ItemStack> getItems() { return java.util.List.of(); }
}
