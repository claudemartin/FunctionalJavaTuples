package org.javatuples.functional;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import org.javatuples.Triplet;

/** @see Triplet */
@FunctionalInterface
public interface TripletFn<A, B, C, R> extends Fn<Triplet<A, B, C>, A, R> {
  /**
   * Applies this function to the given arguments.
   *
   * @param a
   *          the first function argument
   * @param b
   *          the second function argument
   * @param c
   *          the third function argument
   * @return the function result
   */
  R apply(A a, B b, C c);

  /** Converts an uncurried function to a curried function. */
  static <A, B, C, R> Function<A, Function<B, Function<C, R>>> curry(
      final Function<Triplet<A, B, C>, R> f) {
    return a -> b -> c -> f.apply(Triplet.with(a, b, c));
  }

  /** Converts a curried function to a function on triplets. */
  static <A, B, C, R> Function<Triplet<A, B, C>, R> uncurry(
      final Function<A, Function<B, Function<C, R>>> f) {
    return t -> f.apply(t.getValue0()).apply(t.getValue1()).apply(t.getValue2());
  }

  /** Converts an uncurried function to a TripletFn. */
  static <A, B, C, R> TripletFn<A, B, C, R> ofUncurried(final Function<Triplet<A, B, C>, R> f) {
    return (a, b, c) -> f.apply(Triplet.with(a, b, c));
  }

  /** Converts an curried function to a BiFunction. */
  static <A, B, C, R> TripletFn<A, B, C, R> ofCurried(
      final Function<A, Function<B, Function<C, R>>> f) {
    return (a, b, c) -> f.apply(a).apply(b).apply(c);
  }

  @Override
  default Function<A, Function<B, Function<C, R>>> curry() {
    return a -> b -> c -> this.apply(a, b, c);
  }

  @Override
  default Function<Triplet<A, B, C>, R> uncurry() {
    return (Triplet<A, B, C> t) -> this.apply(t.getValue0(), t.getValue1(), t.getValue2());
  }

  @Override
  public default R applyTuple(Triplet<A, B, C> t) {
    return this.apply(t.getValue0(), t.getValue1(), t.getValue2());
  }

  @Override
  default Function<B, Function<C, R>> partial(A a) {
    return b -> c -> this.apply(a, b, c);
  }

  @Override
  default int arity() {
    return 3;
  }

  @Override
  public default <R2> TripletFn<A, B, C, R2> andThen(Function<? super R, ? extends R2> after) {
    requireNonNull(after, "after");
    return (A a, B b, C c) -> after.apply(apply(a, b, c));
  }
}