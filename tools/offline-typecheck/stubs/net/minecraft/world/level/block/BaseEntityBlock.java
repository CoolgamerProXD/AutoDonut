package net.minecraft.world.level.block;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
public abstract class BaseEntityBlock extends Block {
  protected BaseEntityBlock(Properties props) { super(props); }
  protected abstract com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec();
  public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) { return null; }
  protected net.minecraft.world.level.block.RenderShape getRenderShape(BlockState state) { return null; }
  protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hit) { return null; }
  protected static <T extends BlockEntity, E extends BlockEntity> BlockEntityTicker<T> createTickerHelper(BlockEntityType<T> a, BlockEntityType<E> b, BlockEntityTicker<? super E> t) { return null; }
}
