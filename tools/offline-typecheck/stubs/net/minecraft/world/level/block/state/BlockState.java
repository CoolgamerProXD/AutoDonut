package net.minecraft.world.level.block.state;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
public class BlockState {
  public boolean isAir() { return false; }
  public net.minecraft.world.level.block.Block getBlock() { return null; }
  public float getDestroySpeed(Object level, BlockPos pos) { return 1f; }
  public net.minecraft.world.phys.shapes.VoxelShape getCollisionShape(Object level, BlockPos pos) { return null; }
  public boolean is(net.minecraft.world.level.block.Block block) { return false; }
  public <T extends Comparable<T>> T getValue(net.minecraft.world.level.block.state.properties.Property<T> p) { return null; }
  public <T extends Comparable<T>, V extends T> BlockState setValue(net.minecraft.world.level.block.state.properties.Property<T> p, V v) { return this; }
  public boolean hasProperty(net.minecraft.world.level.block.state.properties.Property<?> p) { return false; }
  public net.minecraft.world.level.material.FluidState getFluidState() { return null; }
}
