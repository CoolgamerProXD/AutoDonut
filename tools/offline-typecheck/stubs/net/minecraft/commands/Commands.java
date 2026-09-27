package net.minecraft.commands;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
public class Commands {
  public enum CommandSelection { ALL, DEDICATED, INTEGRATED }
  public static LiteralArgumentBuilder<CommandSourceStack> literal(String name) { return null; }
  public static <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String name, ArgumentType<T> type) { return null; }
  public static final int LEVEL_ALL = 0, LEVEL_MODERATORS = 1, LEVEL_GAMEMASTERS = 2, LEVEL_ADMINS = 3, LEVEL_OWNERS = 4;
  public static java.util.function.Predicate<CommandSourceStack> hasPermission(int level) { return s -> true; }
}
