package net.minecraft.world.item.crafting;
public interface RecipeType<T extends Recipe<?>> {
  RecipeType<SmeltingRecipe> SMELTING = null;
  RecipeType<BlastingRecipe> BLASTING = null;
}
