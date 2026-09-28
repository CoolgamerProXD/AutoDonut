package net.fabricmc.fabric.api.client.event.lifecycle.v1;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.Minecraft;
public final class ClientTickEvents {
  public static final Event<EndTick> END_CLIENT_TICK = null;
  public static final Event<StartTick> START_CLIENT_TICK = null;
  @FunctionalInterface public interface EndTick { void onEndTick(Minecraft client); }
  @FunctionalInterface public interface StartTick { void onStartTick(Minecraft client); }
}
