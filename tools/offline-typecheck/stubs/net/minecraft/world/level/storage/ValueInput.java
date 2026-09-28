package net.minecraft.world.level.storage;
public interface ValueInput {
  int getIntOr(String key, int def);
  boolean getBooleanOr(String key, boolean def);
  String getStringOr(String key, String def);
  long getLongOr(String key, long def);
  double getDoubleOr(String key, double def);
}
