package net.minecraft.world.entity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
public abstract class Entity {
  public BlockPos blockPosition() { return null; }
  public Vec3 position() { return null; }
  public Vec3 getEyePosition() { return null; }
  public double getX() { return 0; } public double getY() { return 0; } public double getZ() { return 0; }
  public float getYRot() { return 0f; } public float getXRot() { return 0f; }
  public void setYRot(float y) { } public void setXRot(float x) { }
  public void sendSystemMessage(Component message) { }
  public Component getName() { return null; }
  public net.minecraft.world.level.Level level() { return null; }
  public double distanceToSqr(Entity other) { return 0; }
  public double distanceToSqr(double x, double y, double z) { return 0; }
  public boolean isAlive() { return true; }
  public boolean onGround() { return true; }
  public net.minecraft.world.phys.AABB getBoundingBox() { return null; }
  public boolean isUsingItem() { return false; }
}
