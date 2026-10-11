package net.fabricmc.fabric.api.client.command.v2;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
public final class ClientCommands {
  public static LiteralArgumentBuilder<FabricClientCommandSource> literal(String name) { return null; }
  public static <T> RequiredArgumentBuilder<FabricClientCommandSource, T> argument(String name, ArgumentType<T> type) { return null; }
}
