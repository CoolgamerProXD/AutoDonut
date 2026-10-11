package net.minecraft.client.gui.components;
public abstract class AbstractWidget {
  public boolean visible = true, active = true;
  public int getX() { return 0; }  public int getY() { return 0; }
  public void setX(int x) { }      public void setY(int y) { }
  public int getWidth() { return 0; } public int getHeight() { return 0; }
  public void setWidth(int w) { }
  public void setMessage(net.minecraft.network.chat.Component m) { }
  public net.minecraft.network.chat.Component getMessage() { return null; }
  public void setTooltip(Object t) { }
  public void visible(boolean v) { }
}
