package net.minecraft.world.item;
import net.minecraft.network.chat.Component;
public class ItemStack {
  public static final ItemStack EMPTY = new ItemStack();
  public ItemStack() { }
  public ItemStack(Item item) { }
  public ItemStack(Item item, int count) { }
  public ItemStack(net.minecraft.world.level.ItemLike item) { }
  public boolean isEmpty() { return false; }
  public int getCount() { return 0; }
  public void setCount(int n) { }
  public Item getItem() { return null; }
  public Component getHoverName() { return null; }
  public Component getDisplayName() { return null; }
  public ItemStack copy() { return this; }
  public boolean is(Item item) { return false; }
  public int getDamageValue() { return 0; }
  public int getMaxDamage() { return 0; }
  public boolean isDamageableItem() { return false; }
  public int getMaxStackSize() { return 64; }
  public void shrink(int n) { }
  public java.util.List<Component> getTooltipLines(Item.TooltipContext ctx, net.minecraft.world.entity.player.Player player, TooltipFlag flag) { return java.util.List.of(); }
  public boolean has(net.minecraft.core.component.DataComponentType<?> type) { return false; }
  public <T> T get(net.minecraft.core.component.DataComponentType<T> type) { return null; }
  public static boolean isSameItemSameComponents(ItemStack a, ItemStack b) { return false; }
  public void grow(int n) { }
  public ItemStack split(int n) { return this; }
}
