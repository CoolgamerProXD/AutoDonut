package net.minecraft.client.gui.screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
public abstract class Screen {
  protected Component title;
  public int width, height;
  public Minecraft minecraft;
  public Font font;
  protected Screen(Component title) { this.title = title; }
  protected void init() { }
  public void onClose() { }
  public void render(net.minecraft.client.gui.GuiGraphics g, int mx, int my, float dt) { }
  public void tick() { }
  public boolean isPauseScreen() { return false; }
  public Component getTitle() { return title; }
  protected <T extends net.minecraft.client.gui.components.AbstractWidget> T addRenderableWidget(T widget) { return widget; }
  public void resize(Minecraft mc, int w, int h) { }
  public net.minecraft.client.gui.Font getFont() { return font; }
}
