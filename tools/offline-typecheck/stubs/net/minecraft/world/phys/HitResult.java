package net.minecraft.world.phys;
public abstract class HitResult {
  public enum Type { MISS, BLOCK, ENTITY }
  public Type getType() { return Type.MISS; }
  public Vec3 getLocation() { return null; }
}
