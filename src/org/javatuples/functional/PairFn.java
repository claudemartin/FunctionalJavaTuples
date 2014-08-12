package org.javatuples.functional;

import static java.util.Objects.requireNonNull;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.javatuples.Pair;
import org.javatuples.Tuple;

/** @see Pair */
@FunctionalInterface
public interface PairFn<A, B, R> extends BiFunction<A, B, R>, Fn<Pair<A, B>, A, R> {
  /** Converts an uncurried function to a curried function. */
  static <A, B, R> Function<A, Function<B, R>> curry(final Function<Pair<A, B>, R> f) {
    return a -> b -> f.apply(Pair.with(a, b));
  }

  /** Converts a curried function to a function on pairs. */
  static <A, B, R> Function<Pair<A, B>, R> uncurry(final Function<A, Function<B, R>> f) {
    return (Pair<A, B> p) -> f.apply(p.getValue0()).apply(p.getValue1());
  }

  /** Converts an uncurried function to a PairFn. */
  static <A, B, R> PairFn<A, B, R> ofUncurried(final Function<Pair<A, B>, R> f) {
    return (a, b) -> f.apply(Pair.with(a, b));
  }

  /** Converts an curried function to a PairFn. */
  static <A, B, R> PairFn<A, B, R> ofCurried(final Function<A, Function<B, R>> f) {
    return (a, b) -> f.apply(a).apply(b);
  }
  
  /** Converts an  BiFunction to a PairFn.
   * Im most cases this isn't necessary, but sometimes it is (TODO : Why?). */
  static <A, B, R> PairFn<A, B, R> of(final BiFunction<A, B, R> f) {
    return (a, b) -> f.apply(a, b);
  }

  @Override
  default Function<A, Function<B, R>> curry() {
    return a -> b -> this.apply(a, b);
  }

  @Override
  default Function<Pair<A, B>, R> uncurry() {
    return (Pair<A, B> p) -> this.apply(p.getValue0(), p.getValue1());
  }

  @Override
  public default R applyTuple(Pair<A, B> p) {
    return this.apply(p.getValue0(), p.getValue1());
  }

  @Override
  default Function<B, R> partial(A a) {
    return b -> this.apply(a, b);
  }

  @Override
  default int arity() {
    return 2;
  }
  
  @Override
  public default <R2> PairFn<A, B, R2> andThen(Function<? super R, ? extends R2> after) {
    requireNonNull(after, "after");
    return (A a, B b) -> after.apply(apply(a, b));
  }
}