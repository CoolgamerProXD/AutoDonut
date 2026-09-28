package net.minecraft.world.level.block;
import net.minecraft.world.level.block.state.BlockState;
public class CropBlock extends Block {
  public CropBlock(Properties p) { super(p); }
  public boolean isMaxAge(BlockState state) { return false; }
  public BlockState getStateForAge(int age) { return null; }
  public int getMaxAge() { return 7; }
  public net.minecraft.world.level.block.state.properties.IntegerProperty getAgeProperty() { return null; }
}
