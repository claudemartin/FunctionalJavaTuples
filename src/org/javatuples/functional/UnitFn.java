package org.javatuples.functional;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.javatuples.Unit;

/** @see Unit */
@FunctionalInterface
public interface UnitFn<A, R> extends Fn<Unit<A>, A, R> {
  /** Converts an uncurried function to a curried function. */
  static <A, R> Function<A, R> curry(final Function<Unit<A>, R> f) {
    return a -> f.apply(Unit.with(a));
  }

  /** Converts a curried function to a function on units. */
  static <A, R> Function<Unit<A>, R> uncurry(final Function<A, R> f) {
    return (final Unit<A> p) -> f.apply(p.getValue0());
  }

  /** Converts an uncurried function to a UnitFn. */
  static <A, R> UnitFn<A, R> ofUncurried(final Function<Unit<A>, R> f) {
    return (a) -> f.apply(Unit.with(a));
  }

  /** Converts an curried function to a UnitFn. */
  static <A, R> UnitFn<A, R> ofCurried(final Function<A, R> f) {
    return (a) -> f.apply(a);
  }

  /** Converts a {@link Supplier} to a {@link UnitFn}, which ignores the input. */
  static <A, R> UnitFn<A, R> ofSupplier(final Supplier<R> supplier) {
    return x -> supplier.get();
  }

  /** Converts a {@link Consumer} to a {@link UnitFn}, which returns an empty {@link Optional}. */
  static <A> UnitFn<A, Optional<Void>> ofConsumer(final Consumer<A> consumer) {
    return x -> {
      consumer.accept(x);
      return Optional.empty();
    };
  }

  /** <code>Unit::with</code> */
  static <A, B> UnitFn<A, Unit<A>> with() {
    return Unit::with;
  }

  /**
   * Applies this function to the given argument.
   *
   * @param a
   *          the function argument
   * @return the function result
   */
  abstract R apply(final A a);

  @Override
  default Function<A, R> curry() {
    return this::apply;
  }

  @Override
  default Function<Unit<A>, R> uncurry() {
    return (final Unit<A> p) -> this.apply(p.getValue0());
  }

  @Override
  public default R applyTuple(final Unit<A> u) {
    return this.apply(u.getValue0());
  }

  @SuppressWarnings("unchecked")
  @Override
  public default R applyArray(final Object[] array) {
    requireNonNull(array, "array");
    if (array.length != 1)
      throw new IllegalArgumentException("Length of array must be 1");
    return this.apply((A) array[0]);
  }

  /**
   * Applies this function to all elements of a collection in parallel.
   * 
   * @param collection
   *          A collection.
   * @return A possibly parallel stream of mapped values.
   * @see #seq(Collection)
   */
  default <A2 extends A> Stream<R> par(final Collection<A2> collection) {
    return collection.parallelStream().map(this::apply);
  }

  /**
   * Applies this function to all elements of a collection sequentially.
   * 
   * @param collection
   *          A collection.
   * @return A sequential stream of mapped values.
   * @see #par(Collection)
   */
  default <A2 extends A> Stream<R> seq(final Collection<A2> collection) {
    return collection.stream().map(this::apply);
  }

  /**
   * Partial application. Returns a result, because this only consumes one argument. This is
   * equivalent to <code>this.apply(a)</code>.
   */
  @Override
  public default R partial(final A a) {
    // No actual "partial application", because this only takes one argument.
    // Passing just one argument is already "complete application".
    return this.apply(a);
  }

  @Override
  default int arity() {
    return 1;
  }

  @Override
  public default <R2> UnitFn<A, R2> andThen(final Function<? super R, ? extends R2> after) {
    requireNonNull(after, "after");
    return (final A a) -> after.apply(apply(a));
  }

}