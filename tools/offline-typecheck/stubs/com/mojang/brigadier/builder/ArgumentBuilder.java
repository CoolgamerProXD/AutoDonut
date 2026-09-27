package com.mojang.brigadier.builder;
public abstract class ArgumentBuilder<S, B extends ArgumentBuilder<S, B>> {
  @SuppressWarnings("unchecked") public B then(ArgumentBuilder<S, ?> a) { return (B) this; }
  @SuppressWarnings("unchecked") public B executes(com.mojang.brigadier.Command<S> c) { return (B) this; }
  @SuppressWarnings("unchecked") public B requires(java.util.function.Predicate<S> p) { return (B) this; }
}
