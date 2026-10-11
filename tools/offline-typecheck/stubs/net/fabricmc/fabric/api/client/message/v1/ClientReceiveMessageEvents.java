package net.fabricmc.fabric.api.client.message.v1;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.network.chat.Component;
public final class ClientReceiveMessageEvents {
  public static final Event<AllowGame> ALLOW_GAME = null;
  public static final Event<AllowChat> ALLOW_CHAT = null;
  public static final Event<Chat> CHAT = null;
  @FunctionalInterface public interface AllowGame { boolean allowReceiveGameMessage(Component message, boolean overlay); }
  @FunctionalInterface public interface AllowChat {
    boolean allowReceiveChatMessage(Component message, Object signedMessage, Object sender, Object params, java.time.Instant receptionTimestamp);
  }
  @FunctionalInterface public interface Chat {
    void onReceiveChatMessage(Component message, Object signedMessage, Object sender, Object params, java.time.Instant receptionTimestamp);
  }
}
