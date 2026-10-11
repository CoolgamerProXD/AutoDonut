package com.mojang.blaze3d.platform;
public class InputConstants {
  public static final int KEY_K = 75;
  public static final Key UNKNOWN = new Key();
  public enum Type { KEYSYM, SCANCODE, MOUSE }
  public static class Key { public int getValue() { return -1; } }
}
