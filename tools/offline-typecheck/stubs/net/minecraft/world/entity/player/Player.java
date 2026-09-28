package net.minecraft.world.entity.player;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
public abstract class Player extends net.minecraft.world.entity.Entity {
  public Inventory getInventory() { return null; }
  public net.minecraft.world.inventory.AbstractContainerMenu containerMenu;
  public net.minecraft.world.inventory.AbstractContainerMenu inventoryMenu;
  public double blockInteractionRange() { return 4.5D; }
  public double entityInteractionRange() { return 3.0D; }
  public void closeContainer() { }
  public ItemStack getMainHandItem() { return null; }
  public ItemStack getOffhandItem() { return null; }
  public float getHealth() { return 20f; }
  public float getMaxHealth() { return 20f; }
  public FoodData getFoodData() { return null; }
  public boolean isShiftKeyDown() { return false; }
  public boolean isCreative() { return false; }
  public boolean addItem(net.minecraft.world.item.ItemStack stack) { return false; }
  public void displayClientMessage(net.minecraft.network.chat.Component msg, boolean actionBar) { }
}
