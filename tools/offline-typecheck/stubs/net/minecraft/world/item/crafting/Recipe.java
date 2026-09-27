package net.minecraft.world.item.crafting;
public interface Recipe<T extends RecipeInput> {
  net.minecraft.world.item.ItemStack assemble(T input, Object registries);
}
