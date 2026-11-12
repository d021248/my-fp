package d021248.mytry.core.lang.fp.zipper;

import module java.base;

/// Demonstrates `ListZipper`: navigation, modification, insertion, deletion,
/// and the comonad `extend` operation.
public final class ZipperDemo {

  public static void main() {
    System.out.println("=== ListZipper: functional list editing ===");

    var words = List.of("one", "two", "three", "four", "five");
    var zip = ListZipper.of(words).orElseThrow();

    System.out.println("Initial focus      : " + zip.focus());

    // Navigate
    var atTwo = zip.moveRight().orElseThrow();
    var atThree = atTwo.moveRight().orElseThrow();
    System.out.println("After 2 right moves: " + atThree.focus());

    // Modify
    var upper = atThree.modify(String::toUpperCase);
    System.out.println("After uppercase    : " + upper.toList());

    // Insert
    var inserted = atThree.insertRight("inserted");
    System.out.println("After insertRight  : " + inserted.toList());

    // Delete
    var deleted = atThree.delete().orElseThrow();
    System.out.println("After delete       : " + deleted.toList());

    // Extend: annotate each position with "pos=N:value"
    // left.size() is the 0-based index because left is nearest-first
    var indexed = zip.extend(ctx -> "pos=%d:%s".formatted(ctx.left().size(), ctx.focus()));
    System.out.println("Extended (indexed) : " + indexed.toList());
  }
}
