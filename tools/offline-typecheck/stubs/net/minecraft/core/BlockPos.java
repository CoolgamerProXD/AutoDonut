package net.minecraft.core;
public class BlockPos {
  public static final BlockPos ZERO = new BlockPos(0,0,0);
  public BlockPos(int x, int y, int z) { }
  public int getX() { return 0; } public int getY() { return 0; } public int getZ() { return 0; }
  public BlockPos above() { return this; }  public BlockPos above(int n) { return this; }
  public BlockPos below() { return this; }  public BlockPos below(int n) { return this; }
  public BlockPos north() { return this; }  public BlockPos south() { return this; }
  public BlockPos east() { return this; }   public BlockPos west() { return this; }
  public BlockPos offset(int x, int y, int z) { return this; }
  public BlockPos relative(Direction d) { return this; }
  public BlockPos relative(Direction d, int n) { return this; }
  public BlockPos immutable() { return this; }
  public double distSqr(BlockPos other) { return 0; }
  public double distToCenterSqr(net.minecraft.world.phys.Vec3 v) { return 0; }
  public static Iterable<BlockPos> betweenClosed(BlockPos a, BlockPos b) { return java.util.List.of(); }
  public static Iterable<BlockPos> betweenClosed(int x1,int y1,int z1,int x2,int y2,int z2) { return java.util.List.of(); }
  public int distManhattan(BlockPos o) { return 0; }
  public static class MutableBlockPos extends BlockPos {
    public MutableBlockPos() { super(0,0,0); }
    public MutableBlockPos(int x,int y,int z) { super(x,y,z); }
    public MutableBlockPos set(int x,int y,int z) { return this; }
    public MutableBlockPos setWithOffset(BlockPos p,int x,int y,int z) { return this; }
  }
}
