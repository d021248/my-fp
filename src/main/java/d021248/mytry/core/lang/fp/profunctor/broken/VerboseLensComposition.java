package d021248.mytry.core.lang.fp.profunctor.broken;

/// Manually composed getter/setter pairs — the pre-profunctor approach to optics.
///
/// Without the profunctor encoding, composing lenses requires explicit plumbing
/// of each getter and setter.  The composition does not follow from the types
/// automatically — you must write it by hand every time.
///
/// Compare with `StrongFn` where lens composition IS function composition
/// of profunctor transformers.
public final class VerboseLensComposition {

  private VerboseLensComposition() {}

  record Person(String name, int age) {}

  record Company(Person ceo, String name) {}

  // A Lens is a (getter, setter) pair
  static String getCeoName(Company c) {
    return c.ceo().name();
  }

  static Company setCeoName(Company c, String n) {
    return new Company(new Person(n, c.ceo().age()), c.name());
  }

  // To compose "Company → CEO → name" you write a new getter AND setter:
  static String getCompanyCeoName(Company c) {
    return getCeoName(c);
  }

  static Company setCompanyCeoName(Company c, String n) {
    return setCeoName(c, n);
  }

  public static void main() {
    var company = new Company(new Person("Alice", 45), "Acme");
    System.out.println("CEO name: " + getCompanyCeoName(company));
    System.out.println("After set: " + setCompanyCeoName(company, "Bob"));
    System.out.println("Problem: every composed lens needs its own getter AND setter.");
  }
}
