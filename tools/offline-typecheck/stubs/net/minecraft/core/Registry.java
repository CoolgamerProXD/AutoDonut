package net.minecraft.core;
public interface Registry<T> {
  net.minecraft.resources.Identifier getKey(T value);
  T get(net.minecraft.resources.Identifier id);
  static <V, T extends V> T register(Registry<V> registry, net.minecraft.resources.Identifier id, T value) { return value; }
  static <V, T extends V> T register(Registry<V> registry, net.minecraft.resources.ResourceKey<V> key, T value) { return value; }
  static <V, T extends V> T register(Registry<V> registry, String id, T value) { return value; }
}
