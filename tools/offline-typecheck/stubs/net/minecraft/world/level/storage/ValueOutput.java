package net.minecraft.world.level.storage;
public interface ValueOutput {
  void putInt(String key, int v);
  void putBoolean(String key, boolean v);
  void putString(String key, String v);
  void putLong(String key, long v);
  void putDouble(String key, double v);
}
