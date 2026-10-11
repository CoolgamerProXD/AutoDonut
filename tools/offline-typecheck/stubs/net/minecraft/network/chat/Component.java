package net.minecraft.network.chat;
public interface Component {
  static MutableComponent literal(String text) { return null; }
  static MutableComponent translatable(String key) { return null; }
  static MutableComponent empty() { return null; }
  String getString();
  MutableComponent copy();
}
