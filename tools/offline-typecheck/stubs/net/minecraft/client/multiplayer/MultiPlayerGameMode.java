package net.minecraft.client.multiplayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.phys.BlockHitResult;
public class MultiPlayerGameMode {
  public boolean startDestroyBlock(BlockPos pos, Direction face) { return false; }
  public boolean continueDestroyBlock(BlockPos pos, Direction face) { return false; }
  public void stopDestroyBlock() { }
  public InteractionResult useItemOn(net.minecraft.client.player.LocalPlayer player, InteractionHand hand, BlockHitResult hit) { return null; }
  public void handleContainerInput(int containerId, int slot, int button, ContainerInput type, Player player) { }
  public boolean isDestroying() { return false; }
  public InteractionResult useItem(net.minecraft.world.entity.player.Player player, InteractionHand hand) { return null; }
}
