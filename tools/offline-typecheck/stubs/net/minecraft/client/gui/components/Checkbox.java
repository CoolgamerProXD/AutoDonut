package net.minecraft.client.gui.components;
import net.minecraft.network.chat.Component;
public class Checkbox extends AbstractWidget {
  @FunctionalInterface public interface OnValueChange { void onValueChange(Checkbox box, boolean selected); }
  public boolean selected() { return false; }
  public static Builder builder(Component message, net.minecraft.client.gui.Font font) { return new Builder(); }
  public static class Builder {
    public Builder pos(int x, int y) { return this; }
    public Builder selected(boolean b) { return this; }
    public Builder onValueChange(OnValueChange cb) { return this; }
    public Builder maxWidth(int w) { return this; }
    public Builder tooltip(Object t) { return this; }
    public Checkbox build() { return null; }
  }
}
