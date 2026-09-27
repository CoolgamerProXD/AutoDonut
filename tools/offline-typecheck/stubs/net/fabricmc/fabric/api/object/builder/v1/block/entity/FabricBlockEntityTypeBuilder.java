package net.fabricmc.fabric.api.object.builder.v1.block.entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
public final class FabricBlockEntityTypeBuilder<T extends BlockEntity> {
  @FunctionalInterface public interface Factory<T extends BlockEntity> {
    T create(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state);
  }
  public static <T extends BlockEntity> FabricBlockEntityTypeBuilder<T> create(Factory<T> factory, net.minecraft.world.level.block.Block... blocks) { return null; }
  public BlockEntityType<T> build() { return null; }
}
