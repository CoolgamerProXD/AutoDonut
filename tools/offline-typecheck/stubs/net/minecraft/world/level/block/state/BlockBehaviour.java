package net.minecraft.world.level.block.state;
public abstract class BlockBehaviour {
  protected BlockBehaviour(Properties props) { }
  protected net.minecraft.world.InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state,
      net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.entity.player.Player player,
      net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) { return null; }
  public static class Properties {
    public static Properties of() { return new Properties(); }
    public Properties strength(float s) { return this; }
    public Properties strength(float a, float b) { return this; }
    public Properties requiresCorrectToolForDrops() { return this; }
    public Properties mapColor(net.minecraft.world.level.material.MapColor c) { return this; }
    public Properties sound(net.minecraft.world.level.block.SoundType s) { return this; }
    public Properties lightLevel(java.util.function.ToIntFunction<BlockState> f) { return this; }
    public Properties setId(net.minecraft.resources.ResourceKey<net.minecraft.world.level.block.Block> id) { return this; }
    public Properties noOcclusion() { return this; }
  }
}
