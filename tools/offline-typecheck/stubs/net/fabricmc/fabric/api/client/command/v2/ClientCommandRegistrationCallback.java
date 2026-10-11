package net.fabricmc.fabric.api.client.command.v2;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.event.Event;
@FunctionalInterface public interface ClientCommandRegistrationCallback {
  Event<ClientCommandRegistrationCallback> EVENT = null;
  void register(CommandDispatcher<FabricClientCommandSource> dispatcher, net.minecraft.commands.CommandBuildContext registryAccess);
}
