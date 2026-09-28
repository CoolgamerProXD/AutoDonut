package net.minecraft.core;
public enum Direction {
  DOWN, UP, NORTH, SOUTH, WEST, EAST;
  public float toYRot() { return 0f; }
  public Direction getOpposite() { return this; }
  public int getStepX() { return 0; } public int getStepY() { return 0; } public int getStepZ() { return 0; }
  public static Direction getApproximateNearest(double x, double y, double z) { return UP; }
  public static Direction fromYRot(double yRot) { return UP; }
  public Direction getClockWise() { return this; }
  public Direction getCounterClockWise() { return this; }
}
