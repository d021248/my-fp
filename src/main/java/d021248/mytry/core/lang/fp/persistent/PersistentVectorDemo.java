package d021248.mytry.core.lang.fp.persistent;

import java.util.ArrayList;

/// Demonstrates `PersistentVector` — structural sharing and immutability.
public final class PersistentVectorDemo {

  private PersistentVectorDemo() {}

  public static void main() {
    System.out.println("=== Persistent Vector Demo ===\n");

    // 1. Basic operations
    System.out.println("-- Basic operations --");
    var v0 = PersistentVector.<Integer>of();
    var v1 = v0.append(10).append(20).append(30);
    var v2 = v1.append(40);
    var v3 = v1.set(1, 99);

    System.out.println("v1 = " + v1);
    System.out.println("v2 = " + v2 + "  (v1.append(40))");
    System.out.println("v3 = " + v3 + "  (v1.set(1,99))");
    System.out.println("v1 unchanged: " + v1); // original unaffected

    // 2. Structural sharing — all versions valid simultaneously
    System.out.println("\n-- Structural sharing --");
    var base = PersistentVector.of("a", "b", "c");
    var branchA = base.append("x");
    var branchB = base.append("y");
    System.out.println("base    = " + base);
    System.out.println("branchA = " + branchA);
    System.out.println("branchB = " + branchB);

    // 3. Large vector spanning multiple trie levels
    System.out.println("\n-- Large vector (1024 elements) --");
    var large = PersistentVector.<Integer>of();
    for (var i = 0; i < 1024; i++) large = large.append(i);
    System.out.println("size     = " + large.size());
    System.out.println("get(0)   = " + large.get(0));
    System.out.println("get(31)  = " + large.get(31));
    System.out.println("get(32)  = " + large.get(32));
    System.out.println("get(1023)= " + large.get(1023));

    // 4. map / filter
    System.out.println("\n-- map / filter --");
    var nums = PersistentVector.of(1, 2, 3, 4, 5, 6);
    var even = nums.filter(n -> n % 2 == 0);
    var sq = even.map(n -> n * n);
    System.out.println("nums   = " + nums);
    System.out.println("even   = " + even);
    System.out.println("even²  = " + sq);

    // 5. Comparison with ArrayList — PersistentVector keeps old versions
    System.out.println("\n-- vs ArrayList --");
    var list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);
    list.add(3);
    var snapshot = list; // same reference — no snapshot possible with ArrayList
    list.add(4);
    System.out.println("ArrayList 'snapshot' sees new element: " + snapshot.contains(4));

    var pv1 = PersistentVector.of(1, 2, 3);
    pv1.append(4); // result is a new vector; pv1 is unchanged
    System.out.println(
        "PersistentVector snapshot unchanged: " + !pv1.stream().toList().contains(4));
  }
}
