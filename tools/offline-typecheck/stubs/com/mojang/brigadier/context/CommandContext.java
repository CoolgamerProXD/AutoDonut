package com.mojang.brigadier.context;
public class CommandContext<S> {
  public S getSource() { return null; }
  public <T> T getArgument(String name, Class<T> clazz) { return null; }
}
