package org.javatuples.functional;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.javatuples.Tuple;
import org.javatuples.valueintf.*;

/**
 * A base type for a function that takes 1, 2, ... or 10 arguments.
 * 
 * Every extending interface is a {@link FunctionalInterface functional interface}.
 * 
 * @see Function
 * @see BiFunction
 */
public interface Fn<T extends Tuple, A, R> {
  /** Converts this to a curried function. */
  Function<?, ?> curry();

  /** Converts this to a function on tuples. */
  Function<T, R> uncurry();

  /**
   * Applies a tuple to get the result. <br>
   * This is equivalent to:<br>
   * <code>fn.uncurry().apply(<i>Tuple</i>.with(...))</code>
   * 
   * @throws IllegalArgumentException
   *           if the arity does not match the size of the tuple or if application of the values
   *           fails.
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  default R applyTuple(T t) {
    try {
      if (this.arity() != t.getSize())
        throw new IllegalArgumentException(
            "Arity of this function does not match the size of the given tuple.");
      Object curry = this.curry();
      for (Object o : t.toList())
        curry = ((Function) curry).apply(o);
      return (R) curry;
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Tuple not applicable to this function.", e);
    }
  }

  /**
   * Partial application. Returns a result or a curried function.<br>
   * If this is a {@link UnitFn} then a result of type R is returned. In any other case this is
   * equivalent to:<br>
   * <code>fn.curry().apply(a);</code>
   * 
   * @return A {@link Function} or a result of type R.
   */
  Object partial(A a);

  /**
   * The <i>arity</i> is the number of arguments of this function.
   * 
   * @see Tuple#getSize()
   * @return The number of arguments this function takes.
   */
  int arity();

  /**
   * Compose a function that takes a Tuple with this function, which creates the tuple.
   * 
   * @see #point(Function)
   * @see #point(UnitFn)
   * @see Function#andThen(Function)
   * @see BiFunction#andThen(Function)
   */
  <R2> Fn<?, ?, ? extends R2> andThen(Function<? super R, ? extends R2> after);

  /**
   * Compose a function that returns a Tuple with this function, which consumes it. The name "point"
   * is used because "compose" is already used in Function (and therefore by UnitFn). Haskell
   * actually uses "." as an operator. This is also known as the <b>point</b>wise application of one
   * function to the result of another to create a third function.
   * 
   * <code>g.point(f)</code> is defined as: <code> (g ∘ f )(x) = g(f(x))</code>
   * 
   * <p>
   * Example:
   * 
   * <pre>
   * PairFn&lt;A, B, R&gt; g = (a, b) -&gt; ...;
   * UnitFn&lt;X, Pair&lt;A, B&gt;&gt; f = x -> ...;
   * UnitFn&lt;X, R&gt; g_f = g.point(f);
   * g_f.apply(new X());
   * </pre>
   * 
   * @see Function#compose(Function)
   * @see #pipe(UnitFn)
   * @see #andThen(Function)
   * */
  default <P> UnitFn<P, R> point(UnitFn<? super P, ? extends T> f) {
    requireNonNull(f, "f");
    return x -> this.applyTuple(f.apply(x));
  }

  /**
   * Compose a function that returns a Tuple with this function, which consumes it.
   * 
   * @see #point(UnitFn)
   * @see #andThen(Function)
   * */
  default <P> Function<P, R> point(Function<? super P, ? extends T> f) {
    requireNonNull(f, "f");
    return x -> this.applyTuple(f.apply(x));
  }

  /**
   * Pipe the output of this to a given, uncurryied function.
   * 
   * @see #andThen(Function)
   * @see #point(UnitFn)
   * @see Function#andThen(Function)
   * @see BiFunction#andThen(Function)
   * */
  default <P> UnitFn<T, P> pipe(Function<? super R, ? extends P> f) {
    requireNonNull(f, "f");
    return x -> f.apply(this.applyTuple(x));
  }

  // TODO: Would it be better to define an interface for each of these.
  // Then they would not have to be static. Each Fn could implement those it needs.

  /**
   * Get the first element of a Tuple.
   * <p>
   * Usage:<br>
   * {@code Function<IValue0<T>, T> fst = Fn::first; }
   * 
   * @see IValue0#getValue0()
   */
  static <T> T first(IValue0<T> t) {
    return t.getValue0();
  }

  /**
   * Get the second element of a Tuple.
   * 
   * @see IValue1#getValue1()
   */
  static <T> T second(IValue1<T> t) {
    return t.getValue1();
  }

  /**
   * Get the third element of a Tuple.
   * 
   * @see IValue2#getValue2()
   */
  static <T> T third(IValue2<T> t) {
    return t.getValue2();
  }

  /**
   * Get the fourth element of a Tuple.
   * 
   * @see IValue3#getValue3()
   */
  static <T> T fourth(IValue3<T> t) {
    return t.getValue3();
  }

  /**
   * Get the fifth element of a Tuple.
   * 
   * @see IValue4#getValue4()
   */
  static <T> T fifth(IValue4<T> t) {
    return t.getValue4();
  }

  /**
   * Get the sixth element of a Tuple.
   * 
   * @see IValue5#getValue5()
   */
  static <T> T sixth(IValue5<T> t) {
    return t.getValue5();
  }

  /**
   * Get the seventh element of a Tuple.
   * 
   * @see IValue6#getValue6()
   */
  static <T> T seventh(IValue6<T> t) {
    return t.getValue6();
  }

  /**
   * Get the eighth element of a Tuple.
   * 
   * @see IValue7#getValue7()
   */
  static <T> T eighth(IValue7<T> t) {
    return t.getValue7();
  }

  /**
   * Get the ninth element of a Tuple.
   * 
   * @see IValue8#getValue8()
   */
  static <T> T ninth(IValue8<T> t) {
    return t.getValue8();
  }

  /**
   * Get the tenth element of a Tuple.
   * 
   * @see IValue9#getValue9()
   */
  static <T> T tenth(IValue9<T> t) {
    return t.getValue9();
  }
}