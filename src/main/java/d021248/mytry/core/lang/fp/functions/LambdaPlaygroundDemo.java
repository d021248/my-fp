package d021248.mytry.core.lang.fp.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/// Fun and entertaining lambda expressions!
///
/// "Because functional programming should be FUN!" 🎉
///
/// Features:
/// - Lambda-powered games
/// - Functional ASCII art
/// - Recursive madness
/// - Mind-bending compositions
public final class LambdaPlaygroundDemo {

  public static void main() {
    System.out.println("=== Lambda Playground - Let's Have Fun! ===\n");

    playNumberGame();
    generateAsciiArt();
    demonstrateFizzBuzzVariations();
    playWithRecursion();
    compositionMadness();
  }

  /// Number guessing game powered by lambdas!
  private static void playNumberGame() {
    System.out.println("1. 🎲 NUMBER GAME - Lambda Edition\n");

    Function<Integer, String> giveHint =
        guess -> {
          int secret = 42; // The answer to everything!
          if (guess == secret) return "🎉 YOU WIN!";
          if (guess < secret) return "⬆️  Higher!";
          return "⬇️  Lower!";
        };

    List<Integer> guesses = Arrays.asList(50, 30, 40, 42);
    System.out.println("   Secret number: 42");
    System.out.println("   Your guesses:");
    guesses.forEach(guess -> System.out.println("   " + guess + " -> " + giveHint.apply(guess)));
    System.out.println();
  }

  /// Generate ASCII art using lambda magic!
  private static void generateAsciiArt() {
    System.out.println("2. 🎨 ASCII ART GENERATOR\n");

    // Lambda-powered triangle
    BiFunction<Integer, Character, String> triangle =
        (size, ch) ->
            IntStream.rangeClosed(1, size)
                .mapToObj(i -> " ".repeat(size - i) + String.valueOf(ch).repeat(2 * i - 1))
                .collect(Collectors.joining("\n"));

    System.out.println("   Triangle:");
    System.out.println(triangle.apply(5, '*'));
    System.out.println();

    // Lambda wave generator
    Function<Integer, String> waveGenerator =
        width -> {
          double frequency = 2.0;
          return IntStream.range(0, 10)
              .mapToObj(
                  y ->
                      IntStream.range(0, width)
                          .mapToObj(
                              x -> {
                                double wavePos = Math.sin(x * frequency / width * Math.PI) * 3;
                                return Math.abs(y - 5 - wavePos) < 0.5 ? "~" : " ";
                              })
                          .collect(Collectors.joining()))
              .collect(Collectors.joining("\n"));
        };

    System.out.println("   Wave:");
    System.out.println(waveGenerator.apply(40));
    System.out.println();
  }

  /// FizzBuzz variations - because one is never enough!
  private static void demonstrateFizzBuzzVariations() {
    System.out.println("3. 🎯 FIZZBUZZ ARENA - Lambda Variations\n");

    // Classic FizzBuzz
    Function<Integer, String> classicFizzBuzz =
        n -> {
          if (n % 15 == 0) return "FizzBuzz";
          if (n % 3 == 0) return "Fizz";
          if (n % 5 == 0) return "Buzz";
          return String.valueOf(n);
        };

    System.out.println("   Classic FizzBuzz:");
    System.out.print("   ");
    IntStream.rangeClosed(1, 15)
        .mapToObj(classicFizzBuzz::apply)
        .forEach(s -> System.out.print(s + " "));
    System.out.println("\n");

    // Enterprise FizzBuzz (over-engineered on purpose!)
    System.out.println("   🏢 Enterprise FizzBuzz™ (with factories!):");
    System.out.print("   ");
    IntStream.rangeClosed(1, 21)
        .mapToObj(
            n -> {
              String result = "";
              if (n % 3 == 0) result += "Fizz";
              if (n % 5 == 0) result += "Buzz";
              if (n % 7 == 0) result += "Bazz";
              return result.isEmpty() ? String.valueOf(n) : result;
            })
        .forEach(s -> System.out.print(s + " "));
    System.out.println("\n");

    // Reverse FizzBuzz - decode the pattern!
    System.out.println("   🔄 Reverse FizzBuzz (decode!):");
    Function<String, Integer> decoder =
        s -> {
          if (s.equals("FizzBuzz")) return 15;
          if (s.equals("Fizz")) return 3;
          if (s.equals("Buzz")) return 5;
          return Integer.parseInt(s);
        };

    List<String> encoded = Arrays.asList("1", "2", "Fizz", "4", "Buzz", "Fizz", "7");
    System.out.print("   Decoded: ");
    encoded.stream().map(decoder).forEach(n -> System.out.print(n + " "));
    System.out.println("\n");
  }

  /// Recursive lambda madness!
  @SuppressWarnings("unchecked")
  private static void playWithRecursion() {
    System.out.println("4. ♾️  RECURSIVE LAMBDA MADNESS\n");

    // Tower of Hanoi with lambda recursion!
    System.out.println("   Tower of Hanoi (3 disks):");

    interface HanoiSolver {
      void solve(int n, String from, String to, String aux, List<String> moves);
    }

    HanoiSolver[] solver = new HanoiSolver[1];
    solver[0] =
        (n, from, to, aux, moves) -> {
          if (n == 1) {
            moves.add(from + " -> " + to);
          } else {
            solver[0].solve(n - 1, from, aux, to, moves);
            moves.add(from + " -> " + to);
            solver[0].solve(n - 1, aux, to, from, moves);
          }
        };

    List<String> moves = new ArrayList<>();
    solver[0].solve(3, "A", "C", "B", moves);
    moves.forEach(move -> System.out.println("   " + move));
    System.out.println("   (Solved in " + moves.size() + " moves!)\n");

    // String permutations
    System.out.println("   Permutations of \"ABC\":");
    Function<String, List<String>>[] permute = new Function[1];
    permute[0] =
        str -> {
          if (str.length() <= 1) return List.of(str);
          List<String> result = new ArrayList<>();
          for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            String remaining = str.substring(0, i) + str.substring(i + 1);
            for (String sub : permute[0].apply(remaining)) {
              result.add(ch + sub);
            }
          }
          return result;
        };

    permute[0].apply("ABC").forEach(p -> System.out.println("   " + p));
    System.out.println();
  }

  /// Function composition madness - pipeline to the extreme!
  private static void compositionMadness() {
    System.out.println("5. 🎭 COMPOSITION MADNESS - Pipeline Extreme\n");

    // Build an insane transformation pipeline
    Function<String, String> pipeline =
        ((Function<String, String>) String::trim)
            .andThen(String::toLowerCase)
            .andThen(s -> s.replaceAll("[^a-z ]", ""))
            .andThen(
                s ->
                    Arrays.stream(s.split(" "))
                        .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                        .collect(Collectors.joining(" ")))
            .andThen(s -> "✨ " + s + " ✨");

    String input = "  hELLo, WoRLD! 123  ";
    System.out.println("   Input:  \"" + input + "\"");
    System.out.println("   Output: \"" + pipeline.apply(input) + "\"\n");

    // Compose mathematical functions
    System.out.println("   Math Function Composition:");
    Function<Double, Double> f = x -> x * 2;
    Function<Double, Double> g = x -> x + 10;
    Function<Double, Double> h = x -> Math.pow(x, 2);

    Function<Double, Double> composed = f.andThen(g).andThen(h);

    System.out.println("   f(x) = x * 2");
    System.out.println("   g(x) = x + 10");
    System.out.println("   h(x) = x²");
    System.out.println("   (h ∘ g ∘ f)(5) = " + composed.apply(5.0));
    System.out.println("   Steps: 5 -> f(5)=10 -> g(10)=20 -> h(20)=400\n");
  }

  /// Enterprise FizzBuzz factory (satire of over-engineering).
  static class FizzBuzzFactory {
    Function<Integer, String> createRule(int divisor, String word) {
      return n -> n % divisor == 0 ? word : "";
    }
  }
}
