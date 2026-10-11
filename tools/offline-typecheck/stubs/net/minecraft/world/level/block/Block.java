package net.minecraft.world.level.block;
public class Block extends net.minecraft.world.level.block.state.BlockBehaviour implements net.minecraft.world.level.ItemLike {
  public Block(Properties props) { super(props); }
  public net.minecraft.world.level.block.state.BlockState defaultBlockState() { return null; }
  public net.minecraft.world.item.Item asItem() { return null; }
  public String getDescriptionId() { return ""; }
  public static void popResource(net.minecraft.world.level.Level level, net.minecraft.core.BlockPos pos, net.minecraft.world.item.ItemStack stack) { }
  protected static <B extends Block> com.mojang.serialization.MapCodec<B> simpleCodec(java.util.function.Function<Properties, B> f) { return null; }
}
