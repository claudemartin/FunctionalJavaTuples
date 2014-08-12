package org.javatuples.functional;

import java.util.Arrays;
import java.util.function.Function;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.javatuples.Unit;

/**
 * Utility class for functional programming with java tuples.
 * 
 * THIS IS JUST A PROTOTYPE.
 * 
 * @author Claude Martin
 *
 */
public class FunctionalDemo {
  @SafeVarargs
  static <T> void check(T expected, T... actual) {
    for (T a : actual) {
      if (!expected.equals(a))
        throw new RuntimeException(a + " != " + expected);
    }
  }

  /** Demo: */
  public static void main(String[] args) {
    {// concatenate 3 strings:
      TripletFn<String, String, String, String> concat3 = (a, b, c) -> a + b + c;

      String a = "Hello";
      String b = "World";
      String c = "!";
      String expected = a + b + c;

      check(expected, concat3.apply(a, b, c));
      check(expected, concat3.curry().apply(a).apply(b).apply(c));
      check(expected, concat3.partial(a).apply(b).apply(c));
      check(expected, concat3.applyTuple(Triplet.with(a, b, c)));
      check(expected, concat3.uncurry().apply(Triplet.with(a, b, c)));
    }

    {
      Triplet<Integer, Integer, Integer> _000 = Triplet.with(0, 0, 0);
      Triplet<Integer, Integer, Integer> _123 = Triplet.with(1, 2, 3);
      Triplet<Integer, Integer, Integer> _111 = Triplet.with(1, 1, 1);
      Triplet<Integer, Integer, Integer> _321 = Triplet.with(3, 2, 1);

      // sum of all three integers:
      TripletFn<Integer, Integer, Integer, Integer> multiply = (a, b, c) -> a * b * c;
      // product of all three integers:
      TripletFn<Integer, Integer, Integer, Integer> sum = (a, b, c) -> a + b + c;
      // quadratic equation (approximately):
      TripletFn<Integer, Integer, Integer, Integer> quadEq = //
      (a, b, c) -> (int) ((Math.sqrt(4d * a * c + b * b) - b) / (2d * a));

      for (Triplet<Integer, Integer, Integer> t : Arrays.asList(_000, _123, _111, _321)) {
        for (TripletFn<Integer, Integer, Integer, Integer> f : Arrays.asList(multiply, sum, quadEq)) {
          // A Pair that holds a Triplet and a TripletFn:
          final PairFn<Triplet<Integer, Integer, Integer>, TripletFn<Integer, Integer, Integer, Integer>, Integer> pair;
          pair = PairFn.of((triplet, fn3) -> fn3.applyTuple(triplet));
          Integer r1 = pair.apply(t, f);
          Integer r2 = pair.curry().apply(t).apply(f);
          Integer r3 = pair.partial(t).apply(f);
          Integer r4 = pair.applyTuple(Pair.with(t, f));
          Integer r5 = pair.uncurry().apply(Pair.with(t, f));

          check(r1, r2, r3, r4, r5);
        }
      }

    }

    PairFn<Integer, Integer, Integer> g = PairFn.of(Integer::sum);
    UnitFn<Integer, Pair<Integer, Integer>> f = UnitFn.ofCurried(x -> Pair.with(x, 42));
    {
      check(20, g.apply(7, 13));
      check(Pair.with(5, 42), f.apply(5));
      // g_f = x -> x + 42;
      Function<Integer, Integer> g_f = g.point(f);
      Integer result = g_f.apply(8);
      check(50, result);

      // Put integer to triplet then extract it again:
      TripletFn<Integer, Integer, Integer, Integer> g3 = (a, b, c) -> c;
      UnitFn<Integer, Triplet<Integer, Integer, Integer>> f3 = x -> Triplet.with(1, 2, x);
      g_f = g3.point(f3);
      result = g_f.apply(8);
      check(8, result);
    }
    
    {
      Integer result =  f.pipe(g.uncurry()).apply(Unit.with(8));
      check(50, result);
    }

    System.out.println("All tests passed successfully!");
  }
}
