package net.minecraft.world.level.block.entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
public abstract class BlockEntity {
  protected BlockPos worldPosition;
  protected net.minecraft.world.level.Level level;
  public BlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) { }
  public BlockPos getBlockPos() { return worldPosition; }
  public net.minecraft.world.level.Level getLevel() { return level; }
  public BlockState getBlockState() { return null; }
  public void setChanged() { }
  protected void loadAdditional(net.minecraft.world.level.storage.ValueInput in) { }
  protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput out) { }
}
