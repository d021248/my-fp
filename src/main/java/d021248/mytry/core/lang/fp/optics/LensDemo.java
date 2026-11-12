package d021248.mytry.core.lang.fp.optics;

/// Demonstrates {@link Lens} composition for deep immutable record updates.
///
/// The same domain as {@link d021248.mytry.core.lang.fp.optics.broken.DeepRecordUpdateDemo}
/// — `Employee → Company → Address → Street` — but every update is a
/// single expression, and adding a field to any record requires changing
/// only the affected lens, not every update site.
public final class LensDemo {

  record Street(String name, int number) {}

  record Address(Street street, String city, String country) {}

  record Company(String name, Address hq) {}

  record Employee(String name, Company company, int age) {}

  // ── Lens definitions (defined once, reused everywhere) ────────────────────

  static final Lens<Employee, Company> employeeCompany =
      Lens.of(Employee::company, (e, c) -> new Employee(e.name(), c, e.age()));

  static final Lens<Company, Address> companyHq =
      Lens.of(Company::hq, (c, a) -> new Company(c.name(), a));

  static final Lens<Address, Street> addressStreet =
      Lens.of(Address::street, (a, s) -> new Address(s, a.city(), a.country()));

  static final Lens<Address, String> addressCity =
      Lens.of(Address::city, (a, c) -> new Address(a.street(), c, a.country()));

  static final Lens<Street, String> streetName =
      Lens.of(Street::name, (s, n) -> new Street(n, s.number()));

  // ── Composed lenses ───────────────────────────────────────────────────────

  static final Lens<Employee, String> employeeCity =
      employeeCompany.andThen(companyHq).andThen(addressCity);

  static final Lens<Employee, String> employeeStreetName =
      employeeCompany.andThen(companyHq).andThen(addressStreet).andThen(streetName);

  // ── Main ──────────────────────────────────────────────────────────────────

  public static void main() {
    var employee =
        new Employee(
            "Alice",
            new Company("Acme", new Address(new Street("Main St", 1), "London", "GB")),
            30);

    System.out.println("=== Lens composition ===\n");

    // Read deep field
    System.out.println("City:   " + employeeCity.get(employee));
    System.out.println("Street: " + employeeStreetName.get(employee));

    // Update deep field — single expression, no repetition of siblings
    var relocated = employeeCity.set(employee, "Paris");
    System.out.println("\nAfter city update: " + employeeCity.get(relocated));

    var renamedStreet = employeeStreetName.set(employee, "New St");
    System.out.println("After street rename: " + employeeStreetName.get(renamedStreet));

    // Modify: transform the current value
    var uppercased = employeeCity.modify(employee, String::toUpperCase);
    System.out.println("After modify (uppercase city): " + employeeCity.get(uppercased));

    // Compose on the fly
    var streetNumber =
        employeeCompany
            .andThen(companyHq)
            .andThen(addressStreet)
            .andThen(Lens.<Street, Integer>of(Street::number, (s, n) -> new Street(s.name(), n)));

    var renumbered = streetNumber.set(employee, 42);
    System.out.println("After street renumber: " + renumbered.company().hq().street().number());
  }
}
