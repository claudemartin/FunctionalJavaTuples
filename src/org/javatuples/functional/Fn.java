package org.javatuples.functional;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.javatuples.Tuple;
import org.javatuples.valueintf.*;

/**
 * A base type for a function that takes 1, 2, ... or 10 arguments.
 * <p>
 * Every extending interface is a {@link FunctionalInterface functional interface}.
 * <p>
 * This represents a regular Java Lambda, not a {@link Function}. The curried form is a Function and
 * allows partial application. The uncurried form is also a Function, but takes a {@link Tuple}.
 * There are static constructor methods starting with "of" in all extending interfaces to create Fn
 * instances from (un)curried Functions.
 * 
 * @see Function
 * @see BiFunction
 */
public interface Fn<T extends Tuple, A, R> {
  /** Converts this to a curried function. */
  Function<A, ?> curry();

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
   * Applies all values of a given array to this function.
   * 
   * @param a
   *          array containing all arguments
   */
  @SuppressWarnings("unchecked")
  default R applyArray(Object[] array) {
    requireNonNull(array, "array");
    if (array.length != this.arity())
      throw new IllegalArgumentException("Length of array must be " + this.arity());

    Function<Object, ?> f = (Function<Object, ?>) this.curry();
    for (int i = 0; i < array.length - 1; i++) {
      Object arg = array[i];
      Object next = f.apply(arg);
      if (!(next instanceof Function))
        throw new IllegalArgumentException("Result was not a function, but more arguments remain.");
      f = (Function<Object, ?>) next;
    }
    return (R) f.apply(array[array.length - 1]);
  }

  /**
   * Applies all values of a given {@link List} to this function.
   * 
   * @param a
   *          list containing all arguments
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  default R applyList(List list) {
    requireNonNull(list, "list");
    if (list.size() != this.arity())
      throw new IllegalArgumentException("Length of list must be " + this.arity());

    Function<Object, ?> f = (Function<Object, ?>) this.curry();
    final Iterator itr = list.iterator();
    while (true) {
      Object arg = itr.next();
      Object next = f.apply(arg);
      if (!itr.hasNext())
        return (R) next;
      if (!(next instanceof Function))
        throw new IllegalArgumentException("Result was not a function, but more arguments remain.");
      f = (Function<Object, ?>) next;
    }
  }

  /**
   * Partial application. Returns curried function.<br>
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
   * Pipe the output of this to a given, uncurried function.
   * 
   * @see #pipe(UnitFn)
   * @see #andThen(Function)
   * @see #point(UnitFn)
   * @see Function#andThen(Function)
   * @see BiFunction#andThen(Function)
   * */
  default <P> UnitFn<T, P> pipe(Function<? super R, ? extends P> f) {
    requireNonNull(f, "f");
    return x -> f.apply(this.applyTuple(x));
  }
  
  /**
   * Pipe the output of this to a given, uncurried function.
   * 
   * @see #pipe(Function)
   * @see #andThen(Function)
   * @see #point(UnitFn)
   * @see Function#andThen(Function)
   * @see BiFunction#andThen(Function)
   * */
  default <P> UnitFn<T, P> pipe(UnitFn<? super R, ? extends P> f) {
    requireNonNull(f, "f");
    return x -> f.apply(this.applyTuple(x));
  }

  /** Function to get the first element of a Tuple. @see IValue0#getValue0() */
  static <T> UnitFn<IValue0<T>, T> first() {
    return IValue0::getValue0;
  }

  /** Function to get the second element of a Tuple. @see IValue1#getValue1() */
  static <T> UnitFn<IValue1<T>, T> second() {
    return IValue1::getValue1;
  }

  /** Function to get the third element of a Tuple. @see IValue2#getValue2() */
  static <T> UnitFn<IValue2<T>, T> third() {
    return IValue2::getValue2;
  }

  /** Function to get the fourth element of a Tuple. @see IValue3#getValue3() */
  static <T> UnitFn<IValue3<T>, T> fourth() {
    return IValue3::getValue3;
  }
  // TODO: fifth, sixth, seventh, eighth, ninth, tenth
}