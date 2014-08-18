package org.javatuples.functional;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.javatuples.Pair;

/** @see Pair */
@FunctionalInterface
public interface PairFn<A, B, R> extends Fn<Pair<A, B>, A, R> {

  /**
   * Applies this function to the given arguments.
   *
   * @param a
   *          the first function argument
   * @param b
   *          the second function argument
   * @return the function result
   */
  abstract R apply(final A a, final B b);

  /** Converts an uncurried function to a curried function. */
  static <A, B, R> Function<A, Function<B, R>> curry(final Function<Pair<A, B>, R> f) {
    return a -> b -> f.apply(Pair.with(a, b));
  }

  /** Converts a curried function to a function on pairs. */
  static <A, B, R> Function<Pair<A, B>, R> uncurry(final Function<A, Function<B, R>> f) {
    return (final Pair<A, B> p) -> f.apply(p.getValue0()).apply(p.getValue1());
  }

  /** Converts an uncurried function to a PairFn. */
  static <A, B, R> PairFn<A, B, R> ofUncurried(final Function<Pair<A, B>, R> f) {
    return (a, b) -> f.apply(Pair.with(a, b));
  }

  /** Converts an curried function to a PairFn. */
  static <A, B, R> PairFn<A, B, R> ofCurried(final Function<A, Function<B, R>> f) {
    return (a, b) -> f.apply(a).apply(b);
  }

  /** Converts a BiFunction to a PairFn. */
  static <A, B, R> PairFn<A, B, R> of(final BiFunction<A, B, R> f) {
    return (a, b) -> f.apply(a, b);
  }

  /** Converts a {@link BiConsumer} to a {@link PairFn}, which returns an empty {@link Optional}. */
  static <A, B> PairFn<A, B, Optional<Void>> ofBiConsumer(final BiConsumer<A, B> consumer) {
    return (x, y) -> {
      consumer.accept(x, y);
      return Optional.empty();
    };
  }

  /** <code>Pair::with</code> */
  static <A, B> PairFn<A, B, Pair<A, B>> with() {
    return Pair::with;
  }

  /**
   * Takes two lists and returns a list of corresponding pairs. If one input list is short, excess
   * elements of the longer list are discarded.
   */
  static <A, B> List<Pair<A, B>> zip(final Collection<A> a, final Collection<B> b) {
    requireNonNull(a, "a");
    requireNonNull(b, "b");
    return zip(//
        () -> new ArrayList<>(Math.min(a.size(), b.size())), // creates new List
        (x, y) -> Pair.with(x, y), // Creates Pair of two elements
        a, b); // both lists.
  }

  /**
   * Generalizes {@link #zip(List, List) zip}.
   */
  static <A, B> List<Pair<A, B>> zip(final Supplier<List<Pair<A, B>>> supplier,
      final PairFn<A, B, Pair<A, B>> zipper, final Iterable<A> a, final Iterable<B> b) {
    requireNonNull(supplier, "supplier");
    requireNonNull(zipper, "zipper");
    requireNonNull(a, "a");
    requireNonNull(b, "b");
    final Iterator<A> itrA = a.iterator();
    final Iterator<B> itrB = b.iterator();
    final List<Pair<A, B>> result = supplier.get();
    while (itrA.hasNext() && itrB.hasNext())
      result.add(zipper.apply(itrA.next(), itrB.next()));
    return result;
  }

  /**
   * Transforms a list of pairs into a list of first components and a list of second components.
   */
  static <A, B> Pair<List<A>, List<B>> unzip(final List<Pair<A, B>> pairs) {
    requireNonNull(pairs, "pairs");
    final int size = pairs.size();
    final ArrayList<A> a = new ArrayList<>(size);
    final ArrayList<B> b = new ArrayList<>(size);
    final Pair<List<A>, List<B>> result = Pair.with(a, b);

    pairs.forEach(p -> {
      a.add(p.getValue0());
      b.add(p.getValue1());
    });
    return result;
  }

  @Override
  default Function<A, Function<B, R>> curry() {
    return a -> b -> this.apply(a, b);
  }

  @Override
  default Function<Pair<A, B>, R> uncurry() {
    return (final Pair<A, B> p) -> this.apply(p.getValue0(), p.getValue1());
  }

  @Override
  public default R applyTuple(final Pair<A, B> p) {
    return this.apply(p.getValue0(), p.getValue1());
  }

  /**
   * Applies this function to all entries of a map in parallel.
   * 
   * @param map
   *          A map.
   * @return A possibly parallel stream of mapped values.
   * @see PairFn#seq(Map)
   */
  default <A2 extends A, B2 extends B> Stream<R> par(final Map<A2, B2> map) {
    return map.entrySet().parallelStream().map(e -> this.apply(e.getKey(), e.getValue()));
  }

  /**
   * Applies this function to all entries of a map sequentially.
   * 
   * @param map
   *          A map.
   * @return A sequential stream of mapped values.
   * @see PairFn#par(Map)
   */
  default <A2 extends A, B2 extends B> Stream<R> seq(final Map<A2, B2> map) {
    return map.entrySet().stream().map(e -> this.apply(e.getKey(), e.getValue()));
  }

  @Override
  default Function<B, R> partial(final A a) {
    return b -> this.apply(a, b);
  }

  @Override
  default int arity() {
    return 2;
  }

  @Override
  public default <R2> PairFn<A, B, R2> andThen(final Function<? super R, ? extends R2> after) {
    requireNonNull(after, "after");
    return (final A a, final B b) -> after.apply(apply(a, b));
  }
}