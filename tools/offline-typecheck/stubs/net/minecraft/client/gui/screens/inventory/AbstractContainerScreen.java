package net.minecraft.client.gui.screens.inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
public abstract class AbstractContainerScreen<T extends AbstractContainerMenu> extends net.minecraft.client.gui.screens.Screen {
  protected AbstractContainerScreen(T menu, net.minecraft.world.entity.player.Inventory inv, net.minecraft.network.chat.Component title) { super(title); }
  public T getMenu() { return null; }
}
