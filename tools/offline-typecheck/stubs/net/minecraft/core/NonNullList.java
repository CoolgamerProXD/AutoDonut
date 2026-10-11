package net.minecraft.core;
public class NonNullList<E> extends java.util.AbstractList<E> {
  public static <E> NonNullList<E> withSize(int size, E fill) { return new NonNullList<>(); }
  public static <E> NonNullList<E> create() { return new NonNullList<>(); }
  public E get(int i) { return null; }
  public E set(int i, E e) { return null; }
  public int size() { return 0; }
}
