# Rosewood Payroll: net pay service, brought up to AmaliTech standard
 
Rosewood Payroll works out an employee's net pay from hours, hourly rate, overtime and tax bands.
The service was generated quickly with an AI assistant. It compiled and its tests were green, but
it was not safe for real pay. This repository is that service after refactoring to Clean Code and
SOLID, with proper failure handling and a test suite that would catch a wrong payslip.
 

 

 
## 1. Running it
 
JDK 21 or newer and Maven.
 

```bash
mvn compile
mvn exec:java -Dexec.mainClass=com.rosewood.payroll.Demo   # run the demo
mvn test                                                   # run the tests
mvn clean verify                                           # tests + JaCoCo report + mutation gate
mvn test-compile org.pitest:pitest-maven:mutationCoverage   # PIT report only
```
 
- Coverage report: `target/site/jacoco/index.html`
- Mutation report: `target/pit-reports/index.html`
- `mvn clean verify` **fails** if the mutation score drops below the threshold (see section 9).


Demo output: 
```
Net pay for Efua Sarpong: GHS 760.00
Result e1, 40h @ 20     -> 760.00
Net pay for Kwame Owusu: GHS 1402.50
Result e2, 50h @ 30     -> 1402.50
Result unknown employee -> UNKNOWN EMPLOYEE
```
 
---
 
## 2. Results at a glance
 
| Measure | Inherited code and tests | Now |
|---|---|---|
| Tests | 3 (none proving anything) | 72 test executions in 10 classes |
| JaCoCo line / branch coverage | 63% lines, 33% branches | 100% / 100% |
| PIT mutation coverage | 14% (3 of 21 mutants killed) | 100% (33 of 33 killed) |
| PIT test strength | 27% | 100% |
| Failure handling | every failure returned `0` | named exceptions, cause kept, nothing swallowed |
| Money | `double`, unrounded (`949.9905`) | `BigDecimal`, 2 decimal places, half-up |
| Adding a tax band | edit an `if / else if` chain | add one class and one list entry |
 

 
---
 
## 3. The design
 
### Who does what
 
| Class | One job |
|---|---|
| `PayrollCalculator` | Coordinates one payslip: find employee, work out gross, take tax, build the payslip |
| `EmployeeRepository` (interface), `InMemoryEmployeeRepository` | Where employees come from |
| `GrossPayCalculator` | Validates hours and rate, then works out gross pay including overtime |
| `TaxCalculator` | Picks the first tax band that applies and asks it for the tax |
| `TaxBand` (interface), `FlatRateTaxBand` | One band's rule |
| `StandardTaxBands` | The current table of band limits and rates |
| `Money` | The one rounding rule (2 decimal places, half-up) |
| `Payslip` | The immutable result (`employeeName`, `net`) |
| `exceptions` package | Failures named in business terms |
| `Demo` | Runnable demonstration |
 
### Dependencies
 
```
Demo --> PayrollCalculator --> EmployeeRepository <-- InMemoryEmployeeRepository
                |
                +--> GrossPayCalculator --> Money
                |
                +--> TaxCalculator --> TaxBand <-- FlatRateTaxBand --> Money
                                         ^
                       StandardTaxBands supplies the ordered list
```
 
`PayrollCalculator` receives all three collaborators through its constructor. Only `Demo` knows which concrete classes are used. The constructor rejects `null`
arguments immediately, with a message naming the missing one.
 
### Why this shape
 
- **SRP.** The original `calculateNetPay` looked up the employee, worked out overtime, chose
  a tax band, computed tax, printed, and decided the failure policy: five reasons to change in one
  method. Each of those now lives in one place.
- **DIP.** The original built `new InMemoryEmployeeRepository()` inside the class, so no test
  could simulate a missing employee or a failing data source. The interface already existed; the fix
  was to accept it instead of constructing it. That one change is what made the failure tests possible.
- **Small methods.** No method is longer than a screen; each does one thing, and
  names say what it is (`validateHours`, `overtimePay`, `findEmployee`).
- **Named constants.** `STANDARD_HOURS`, `OVERTIME_RATE`, `MAX_HOURS_PER_PERIOD`,
  `LOW_BAND_UPPER_LIMIT` and so on replace the bare `40`, `0.5`, `1000`, `5000`.
---
 
## 4. How the design absorbs the coming tax band
 
The brief says a new band "with its own rules" is coming. Each band is an object with two methods:
 
```java
public interface TaxBand {
    boolean appliesTo(BigDecimal gross);
    BigDecimal taxOn(BigDecimal gross);
}
```
 
`TaxCalculator` loops through an ordered list, uses the first band that applies, and falls back to
the last band when none does (the job the old final `else` did). It never mentions a limit or a rate.
 
**To add a new band:**
1. Write one class implementing `TaxBand` (or reuse `FlatRateTaxBand` if it is a flat rate).
2. Put it in the list in `StandardTaxBands.create()`.
`TaxCalculator`, `PayrollCalculator`, `GrossPayCalculator` and `TaxBand` do not change.
 
**Proof, not just a claim.** `TaxBandExtensionTest` defines a `FixedFeeBand` (a flat fee below a
ceiling, a rule shape that exists nowhere in production code), slots it between two existing bands, and
asserts that the new band is used, that the old bands still apply around it, and that the first
applicable band wins.
 
**The honest limits:**
- Adding a band still edits **one** place: the list in `StandardTaxBands`. That is configuration, not
  calculation logic, but it is an edit. Open-closed means the logic is not rewritten, not that no file
  ever changes.
- **Order matters** because the first match wins. A band placed in the wrong position would silently
  take the wrong rate. `TaxBandExtensionTest` and `TaxCalculatorTest` pin the ordering behaviour.
- The fallback to the highest band means a gross above every limit is taxed in the top band. That
  matches the original `else`.
---
 
## 5. Exception handling
 
```
PayrollException (unchecked base)
 +-- EmployeeNotFoundException   no employee with that id
 +-- EmployeeLookupException     the data source failed; original cause kept
 +-- InvalidInputException       hours or rate (or employee id) unacceptable
```
 
| Rule applied | Where you can see it |
|---|---|
| Never swallow | the old `catch (Exception) { return 0; }` is gone; there is no `return 0` anywhere |
| Catch only where you can do something useful | exactly **one** `try/catch` in production code: `PayrollCalculator.findEmployee`, around the repository call |
| Keep the cause, add context | a repository failure becomes `EmployeeLookupException("Could not look up employee 'e1'", cause)` |
| Name it in business terms | `EmployeeNotFoundException`, not `NoSuchElementException` |

 
**Why unchecked?** Most of these failures cannot be fixed where they happen: bad input is a caller
error (like Java's own `IllegalArgumentException`), and a database outage can only be handled at the top.
Checked exceptions would put `throws` on every method between, leak infrastructure details into the
business layer, and make lambdas and mocks awkward. The one debatable case is
`EmployeeNotFoundException`, where I chose unchecked so the caller must deliberately decide to skip.
 
**Validation lives in `GrossPayCalculator`**, because the hours limit and the rate rule are
preconditions of the pay calculation. If validation rules grow, they should move to their own class.
 
 
## 6. Decisions and their reasons
 
| Decision | Reason | Alternative I rejected |
|---|---|---|
| Money is `BigDecimal`, rounded to **2 decimal places, half-up**, in one class (`Money`) | `double` cannot hold many decimals exactly; one place for the rule ( DRY) | `double`, or rounding in several classes |
| Hours stay `double` | hours are not money | `BigDecimal` hours, more ceremony for no gain |
| Money compared with `compareTo`, never `equals` | `equals` is scale-sensitive: `1000.00` is not `equals` to `1000` | n/a |
| Hours must be finite and between 0 and **120** (inclusive); zero hours accepted | 120 was a design decision; zero is a legitimate period | no limit |
| Hourly rate must be provided and **positive** | a zero or negative rate is not a real pay rate | accepting zero |
| An unknown employee throws; it never returns `0` | a failure must not look like a valid pay figure | returning `0`, or `null` |
| `findById` returns `Optional<Employee>` | absence becomes part of the type; the old code forgot to check `null` | returning `null` |
| Overtime hours computed as `max(hours - 40, 0)` | one fewer branch; reads as "hours above 40" | `if (hours > 40)` |
| `Money` and `StandardTaxBands` are static utilities | stateless, nothing to replace or inject | injecting a rounding object |
 
---
 
## 7. What changed on purpose, and what did not
 
Refactoring changes structure, never behaviour. The history keeps the two apart: commits that moved
code kept every output identical; these commits changed behaviour deliberately.
 
| Change | Before | After |
|---|---|---|
| Money is rounded | `949.9905` | `949.99` |
| Money has 2 decimals | `760.0` | `760.00` |
| Unknown employee | returns `0` | throws `EmployeeNotFoundException`; `Demo` prints `UNKNOWN EMPLOYEE` |
| Invalid hours or rate | accepted (`-190.0`, `NaN`) | `InvalidInputException` |
| Console output | printed from inside the calculation | printed by `Demo` from the returned `Payslip` |
 
**Preserved on purpose:**
- The `Demo` text and its five lines. Only the two-decimal money and the unknown-employee line differ.

---
 
## 8. The tests
 
### Strategy
 
- **Pure logic is tested with real collaborators.** `GrossPayCalculator`, `Money`, `FlatRateTaxBand`,
  `TaxCalculator` and `StandardTaxBands` need no mocks, so their tests assert exact values.
- **Mockito is used only at the seam.** `PayrollCalculatorTest` mocks the repository (and stubs gross
  and tax with unrealistic round numbers so the arithmetic is isolated). It asserts the **returned
  payslip**, not merely that a mock was called. `verifyNoInteractions` appears once, as a secondary check
  that nothing is calculated for an unknown employee.
- **One end-to-end layer** runs the real classes together over a table of inputs.
- **Every failure path is asserted**: exception type, and where it matters the message and the cause.
- One concept per test, behaviour-describing names, and `@ParameterizedTest` for tables of cases.

| Test class | What it proves |
|---|---|
| `GrossPayCalculatorTest` | standard hours, overtime, the 120-hour limit, and every rejection (negative, over 120, `NaN`, infinite, null, zero or negative rate) |
| `MoneyTest` | half-up rounding on exact ties, below and above ties, negatives, always 2 decimals |
| `FlatRateTaxBandTest` | the limit is exclusive, scale-insensitive (`1000.00` equals `1000`), half-up tax rounding |
| `StandardTaxBandsTest` | the real table: three bands, their limits and rates, and an immutable list |
| `TaxCalculatorTest` | band selection at and around each limit, fallback to the highest band, an empty list is rejected |
| `TaxBandExtensionTest` | a brand-new kind of band works with no change to `TaxCalculator`; first match wins; the list is copied defensively |
| `PayrollCalculatorTest` | net = gross minus tax, the employee's name reaches the payslip, unknown employee, repository failure wrapped with its cause, invalid input propagates, constructor null guards |
| `PayrollCalculatorIntegrationTest` | seven end-to-end hour/rate combinations, unknown employee, over-limit hours, the null-id behaviour |
| `PayrollExceptionsTest` | messages, causes, and that every failure is an unchecked `PayrollException` |
| `InMemoryEmployeeRepositoryTest` | found, not found, case sensitivity, null id |
 
---
 
## 9. The improvement beyond the brief: a mutation-score gate
 
**What:** `mvn verify` runs PIT and **fails the build** if the mutation score falls below the threshold
(currently 95%, set in `pom.xml`).
 
**Why this one:** the whole point of this lab is that green tests proved nothing. The standard
says a test must make an assertion that would fail if the code were wrong. Without a gate, that protection exists only when someone remembers to run PIT. With it, a
hollow test or an untested feature stops the build. That matters most for the coming tax band: if its
tests do not really check it, new mutants survive and the build fails.
 
**Evidence that it bites.** I added a tiny untested method to `Money` as an experiment. PIT generated two
mutants for it that no test could kill, the score dropped to 94% (33 of 35), and the build failed against
the 95% threshold. I removed the method and the build passed again.
 
**Policy:** raise the threshold when the score improves; lowering it needs a reviewer's agreement and a
stated reason.
 
**Limits of the gate:**
- A gate can be gamed by lowering it; hence the review rule above.
 
## 10. Known limits and tooling notes (please read)
 
### Four "uncovered" lines in the PIT report are a tooling artifact
 
PIT reports 94-95% line coverage while JaCoCo reports 100%. The difference is four lines: the **empty
private constructors** of `Money` (lines 11-12) and `StandardTaxBands` (lines 16-17). JaCoCo
automatically filters empty private constructors; PIT 1.16.1 does not. This is **not a gap in the
tests**. I deliberately did not write a reflection test to "cover" them, because a test that calls a
constructor to raise a number, and asserts nothing about behaviour, is exactly the hollow-test pattern
the analysis criticises.
 

### Other known gaps
 
- **`Demo` is excluded from coverage and mutation**, so nothing automatically guards its output. I
  checked the five lines against the original by hand. A small test that captures `System.out` would
  close this.
 
## 11. The AI's code and tests: kept, fixed, rewrote
 
### Code
 
| Part | Verdict | Why |
|---|---|---|
| `EmployeeRepository` interface, `Employee`, seed data (`e1`, `e2`) | **Kept** | already the right abstraction; the only fault was how it was wired |
| Overtime rule and all band limits and rates | **Kept** | the numbers were correct; only their form was wrong (magic numbers, `double`) |
| `Demo` scenarios and output text | **Kept** | the output had to stay identical |
| Hard-wired repository | **Fixed** | constructor injection |
| `if / else if` tax chain | **Rewrote** | strategy objects behind `TaxCalculator` |
| `calculateNetPay` (five jobs in one) | **Rewrote** | split into coordinator, gross-pay calculator, tax calculator, `Money` |
| `catch (Exception) { return 0; }` | **Rewrote** | custom exceptions, validation, one purposeful `try/catch` |
| `double` money | **Rewrote** | `BigDecimal` with an explicit rounding rule |
| `System.out.println` inside the calculation | **Rewrote** | a `Payslip` is returned; `Demo` prints |
| Magic numbers and "what" comments | **Fixed** | named constants and descriptive method names |
 
### Tests
 
| Inherited test | Verdict | Why |
|---|---|---|
| `calculatesOvertimePay` | **Deleted** | mocked the class under test and asserted the stubbed answer; no production code ran |
| `returnsPayForAnEmployee` | **Deleted** | `assertNotNull` on a primitive `double` can never fail |
| `calculatesNetPay` | **Rewritten** | the scenario (40 hours at 20) was kept; `assertTrue(net > 0)` became an exact-value assertion, and the boundaries, overtime and failures it never covered were added |
 
---
 

## 12. Commit history
 
| Group | Commits |
|---|---|
| Baseline and analysis | import starter unchanged; analysis document |
| Behaviour-preserving refactors | inject repository; name constants and extract steps; extract `GrossPayCalculator`; tax bands as strategies |
| Deliberate behaviour changes | `BigDecimal` money; `Optional` repository; custom exceptions and validation; `Payslip` and removed console output |
| Tests | one commit per area, replacing the hollow suite |
| Tooling | exclude `Demo` from analysis; streamline gross pay; mutation gate |
| Docs | this README |
| Clean up | update Employee |
 

