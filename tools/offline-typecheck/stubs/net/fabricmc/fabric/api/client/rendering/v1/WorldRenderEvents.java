package net.fabricmc.fabric.api.client.rendering.v1;
public final class WorldRenderEvents {
  public interface AfterTranslucent { void afterTranslucent(WorldRenderContext context); }
  public static final Event AFTER_TRANSLUCENT = new Event();
  public static final class Event {
    public void register(java.util.function.Consumer<WorldRenderContext> listener) {}
  }
}
