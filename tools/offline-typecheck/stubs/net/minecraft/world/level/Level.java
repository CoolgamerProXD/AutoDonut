package net.minecraft.world.level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
public abstract class Level {
  public BlockState getBlockState(BlockPos pos) { return null; }
  public BlockEntity getBlockEntity(BlockPos pos) { return null; }
  public net.minecraft.world.level.material.FluidState getFluidState(BlockPos pos) { return null; }
  public boolean isLoaded(BlockPos pos) { return false; }
  public boolean isClientSide() { return true; }
  public boolean isClientSide = true;
  public boolean setBlock(BlockPos pos, BlockState state, int flags) { return false; }
  public boolean setBlockAndUpdate(BlockPos pos, BlockState state) { return false; }
  public boolean destroyBlock(BlockPos pos, boolean drop) { return false; }
  public int getMinY() { return -64; }
  public int getMaxY() { return 320; }
  public <T extends net.minecraft.world.entity.Entity> java.util.List<T> getEntitiesOfClass(Class<T> c, net.minecraft.world.phys.AABB box) { return java.util.List.of(); }
  public long getGameTime() { return 0L; }
  public java.util.List<net.minecraft.world.entity.Entity> getEntities(net.minecraft.world.entity.Entity except, net.minecraft.world.phys.AABB box, java.util.function.Predicate<? super net.minecraft.world.entity.Entity> filter) { return java.util.List.of(); }
  public boolean removeBlock(net.minecraft.core.BlockPos pos, boolean isMoving) { return false; }
}
