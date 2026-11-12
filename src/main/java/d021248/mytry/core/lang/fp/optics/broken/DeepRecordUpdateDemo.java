package d021248.mytry.core.lang.fp.optics.broken;

/// ANTI-PATTERN: manual nested `new` chains when updating immutable records.
///
/// When data is three or more levels deep, every field update requires
/// reconstructing the entire path from root to the changed leaf.  Every
/// sibling field must be restated, making the code verbose, noisy, and
/// fragile — adding a field to any record requires updating every update site.
///
/// See {@link d021248.mytry.core.lang.fp.optics.LensDemo} for the fix.
public final class DeepRecordUpdateDemo {

  record Street(String name, int number) {}

  record Address(Street street, String city, String country) {}

  record Company(String name, Address hq) {}

  record Employee(String name, Company company, int age) {}

  public static void main() {
    var employee =
        new Employee(
            "Alice",
            new Company("Acme", new Address(new Street("Main St", 1), "London", "GB")),
            30);

    // ── Update: move the employee's company HQ to a new city ─────────────

    // Every level must be reconstructed; only "Paris" is the actual change
    var relocated =
        new Employee(
            employee.name(), // unchanged
            new Company(
                employee.company().name(), // unchanged
                new Address(
                    employee.company().hq().street(), // unchanged
                    "Paris", // ← the actual change
                    employee.company().hq().country() // unchanged
                    )),
            employee.age() // unchanged
            );

    System.out.println("Original: " + employee.company().hq().city());
    System.out.println("Relocated: " + relocated.company().hq().city());

    // ── Update: rename the street ─────────────────────────────────────────

    // Four levels deep — all sibling fields must be copied by hand
    var renamedStreet =
        new Employee(
            employee.name(),
            new Company(
                employee.company().name(),
                new Address(
                    new Street(
                        "New St", // ← only this changed
                        employee.company().hq().street().number()),
                    employee.company().hq().city(),
                    employee.company().hq().country())),
            employee.age());

    System.out.println("Original street: " + employee.company().hq().street().name());
    System.out.println("Renamed street:  " + renamedStreet.company().hq().street().name());

    System.out.println("\nSee lens/LensDemo for the correct approach.");
  }
}
