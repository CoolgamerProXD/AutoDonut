package net.minecraft.world.item.crafting;
public record RecipeHolder<T extends Recipe<?>>(Object id, T value) { }
