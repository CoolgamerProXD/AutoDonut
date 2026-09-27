package net.minecraft.world.item.crafting;
public class RecipeManager {
  public interface CachedCheck<I extends RecipeInput, T extends Recipe<I>> {
    java.util.Optional<RecipeHolder<T>> getRecipeFor(I input, net.minecraft.server.level.ServerLevel level);
  }
  public static <I extends RecipeInput, T extends Recipe<I>> CachedCheck<I, T> createCheck(RecipeType<T> type) { return null; }
}
