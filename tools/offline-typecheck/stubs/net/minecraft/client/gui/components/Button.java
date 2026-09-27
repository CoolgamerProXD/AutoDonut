package net.minecraft.client.gui.components;
import net.minecraft.network.chat.Component;
public class Button extends AbstractWidget {
  @FunctionalInterface public interface OnPress { void onPress(Button button); }
  public static Builder builder(Component message, OnPress onPress) { return new Builder(); }
  public static class Builder {
    public Builder bounds(int x, int y, int w, int h) { return this; }
    public Builder pos(int x, int y) { return this; }
    public Builder width(int w) { return this; }
    public Builder size(int w, int h) { return this; }
    public Builder tooltip(Object tooltip) { return this; }
    public Button build() { return null; }
  }
}
