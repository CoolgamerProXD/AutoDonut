package net.minecraft.client.gui.components;
import net.minecraft.network.chat.Component;
public class StringWidget extends AbstractWidget {
  public StringWidget(Component message, net.minecraft.client.gui.Font font) { }
  public StringWidget(int x, int y, int w, int h, Component message, net.minecraft.client.gui.Font font) { }
  public StringWidget alignLeft() { return this; }
  public StringWidget alignCenter() { return this; }
}
