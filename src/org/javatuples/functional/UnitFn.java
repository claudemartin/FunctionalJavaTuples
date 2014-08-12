package org.javatuples.functional;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import org.javatuples.Unit;

/** @see Unit */
@FunctionalInterface
public interface UnitFn<A, R> extends Function<A, R>, Fn<Unit<A>, A, R> {
  /** Converts an uncurried function to a curried function. */
  static <A, R> Function<A, R> curry(final Function<Unit<A>, R> f) {
    return a -> f.apply(Unit.with(a));
  }

  /** Converts a curried function to a function on units. */
  static <A, R> Function<Unit<A>, R> uncurry(final Function<A, R> f) {
    return (Unit<A> p) -> f.apply(p.getValue0());
  }

  /** Converts an uncurried function to a Function. */
  static <A, R> UnitFn<A, R> ofUncurried(final Function<Unit<A>, R> f) {
    return (a) -> f.apply(Unit.with(a));
  }

  /** Converts an curried function to a Function. */
  static <A, R> UnitFn<A, R> ofCurried(final Function<A, R> f) {
    return (a) -> f.apply(a);
  }

  @Override
  default Function<A, R> curry() {
    return this;
  }

  @Override
  default Function<Unit<A>, R> uncurry() {
    return (Unit<A> p) -> this.apply(p.getValue0());
  }

  @Override
  public default R applyTuple(Unit<A> u) {
    return this.apply(u.getValue0());
  }

  // No actual "partial application", because this only takes one argument.
  // Passing just one argument is already "complete application".

  @Override
  public default R partial(A a) {
    return this.apply(a);
  }

  @Override
  default int arity() {
    return 1;
  }

  @Override
  public default <R2> UnitFn<A, R2> andThen(Function<? super R, ? extends R2> after) {
    requireNonNull(after, "after");
    return (A a) -> after.apply(apply(a));
  }
  
}