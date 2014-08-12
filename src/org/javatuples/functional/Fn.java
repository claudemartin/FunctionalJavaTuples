package org.javatuples.functional;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.javatuples.Pair;
import org.javatuples.Tuple;

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
   * */
  default <P> UnitFn<P, R> point(Function<? super P, ? extends T> f) {
    requireNonNull(f, "f");
    return x -> this.applyTuple(f.apply(x));
  }

  /**
   * Pipe the output of this to a given, uncurried function.
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
}