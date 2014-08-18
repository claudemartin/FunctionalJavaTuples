package org.javatuples.functional;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.javatuples.Triplet;
import org.javatuples.Unit;

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

  /** <code>Triplet::with</code> */
  static <A, B, C> TripletFn<A, B, C, Triplet<A, B, C>> with() {
    return Triplet::with;
  }

  /**
   * Takes three lists and returns a list of corresponding triplets. If one input list is short,
   * excess elements of the longer lists are discarded.
   */
  static <A, B, C> List<Triplet<A, B, C>> zip(Collection<A> a, Collection<B> b, Collection<C> c) {
    requireNonNull(a, "a");
    requireNonNull(b, "b");
    requireNonNull(c, "c");
    int size = Math.min(a.size(), Math.min(b.size(), c.size()));
    return zip(//
        () -> new ArrayList<>(size), // creates new List
        (x, y, z) -> Triplet.with(x, y, z), // Creates Triplet of 3 elements
        a, b, c); // both lists.
  }

  /**
   * Generalizes {@link #zip(List, List, List) zip}.
   */
  static <A, B, C> List<Triplet<A, B, C>> zip(Supplier<List<Triplet<A, B, C>>> supplier,
      TripletFn<A, B, C, Triplet<A, B, C>> zipper, Iterable<A> a, Iterable<B> b, Iterable<C> c) {
    requireNonNull(supplier, "supplier");
    requireNonNull(zipper, "zipper");
    requireNonNull(a, "a");
    requireNonNull(b, "b");
    requireNonNull(c, "c");
    Iterator<A> itrA = a.iterator();
    Iterator<B> itrB = b.iterator();
    Iterator<C> itrC = c.iterator();
    List<Triplet<A, B, C>> result = supplier.get();
    while (itrA.hasNext() && itrB.hasNext() && itrC.hasNext())
      result.add(zipper.apply(itrA.next(), itrB.next(), itrC.next()));
    return result;
  }

  /**
   * Transforms a list of triplets into 3 lists.
   */
  static <A, B, C> Triplet<List<A>, List<B>, List<C>> unzip(List<Triplet<A, B, C>> triplets) {
    requireNonNull(triplets, "triplets");
    int size = triplets.size();
    ArrayList<A> a = new ArrayList<>(size);
    ArrayList<B> b = new ArrayList<>(size);
    ArrayList<C> c = new ArrayList<>(size);
    Triplet<List<A>, List<B>, List<C>> result = Triplet.with(a, b, c);

    triplets.forEach(p -> {
      a.add(p.getValue0());
      b.add(p.getValue1());
      c.add(p.getValue2());
    });
    return result;
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