package net.minecraft.world.phys;
public class Vec3 {
  public final double x, y, z;
  public Vec3(double x, double y, double z) { this.x=x; this.y=y; this.z=z; }
  public static Vec3 atCenterOf(net.minecraft.core.BlockPos pos) { return new Vec3(0,0,0); }
  public static Vec3 atLowerCornerOf(net.minecraft.core.BlockPos pos) { return new Vec3(0,0,0); }
  public Vec3 add(double x, double y, double z) { return this; }
  public Vec3 subtract(Vec3 o) { return this; }
  public Vec3 normalize() { return this; }
  public double distanceTo(Vec3 o) { return 0; }
  public double distanceToSqr(Vec3 o) { return 0; }
  public double length() { return 0; }
}
