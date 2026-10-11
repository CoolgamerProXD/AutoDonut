package net.minecraft.world.phys;
public class AABB {
  public AABB(double x1,double y1,double z1,double x2,double y2,double z2) { }
  public AABB(net.minecraft.core.BlockPos pos) { }
  public AABB inflate(double d) { return this; }
  public static AABB ofSize(Vec3 centre, double x, double y, double z) { return null; }
}
