# Analysis of the inherited Rosewood Payroll code and tests

## 1. Summary

The service compiles and its three tests pass, but it is not safe for real pay.
It returns `0` whenever anything goes wrong, so a failure looks like a valid result.
It cannot be tested properly because it builds its own repository. Its tests would
stay green even if the tax rates and the overtime rule were badly wrong.

**Most serious problem: the swallowed failure (`catch (Exception) { return 0; }`).**
Reasoning is in section 5.

## 2. Baseline evidence (inherited code, unmodified)

Output of `Demo`:

```
Net pay for Efua Sarpong: GHS 760.0
Result e1, 40h @ 20     -> 760.0
Net pay for Kwame Owusu: GHS 1402.5
Result e2, 50h @ 30     -> 1402.5
Result unknown employee -> 0.0
```

Extra probes against the real `PayrollCalculator`:

| Input (employee, hours, rate) | Result | What it shows |
|---|---|---|
| unknown id, 40h @ 20 | `0.0` | A missing employee gives a plausible pay figure |
| `null` id, 40h @ 20 | `0.0` | Same: bad input is silently hidden |
| e1, -10h @ 20 | `-190.0` | Negative hours accepted; negative "net pay" returned |
| e1, 40h @ -20 | `-760.0` | Negative rate accepted |
| e1, a @b | `NaN` | No validation of numeric input |
| e1, 500h @ 20 | `10950.0` | An impossible work period is accepted |
| e1, 1h @ 999.99 | `949.9905` | Result is not rounded to pesewas (2 decimal places) |

The unknown employee prints no "Net pay" line at all, because the failure happens at
`employee.getName()` (a `NullPointerException`) and is then swallowed.

### Coverage and mutation results for the inherited tests

Measured with JaCoCo 0.8.14 (`mvn verify`) and PIT 1.16.1 on the unmodified code.

| Measure | Result | In plain terms |
|---|---|---|
| JaCoCo instruction coverage | 60% (88 of 145 covered) | Most bytecode ran at least once |
| JaCoCo line coverage | 63% (22 of 35 covered) | 13 lines never ran |
| JaCoCo branch coverage | **33% (2 of 6 covered)** | 4 of 6 decision outcomes never ran |
| JaCoCo methods / classes | 6 of 9 / 3 of 4 covered | One class and three methods never ran |
| PIT mutants generated | 21 | Small deliberate bugs injected into the code |
| PIT mutation coverage | **14% (3 of 21 killed)** | Only 3 injected bugs made a test fail |
| PIT test strength | **27% (3 of 11 killed)** | Of the bugs on lines the tests reach, 3 in 11 were noticed |


- **Mutation coverage** = killed / all mutants = 3 / 21.
- **Test strength** = killed / mutants on lines that tests actually run = 3 / 11.
- So of 21 injected bugs: **3 were caught, 8 survived** (the tests ran that line and
  did not notice the change), and **10 sat on lines no test runs at all**.

## 3. Problems in the production code

Ranked from most to least serious.

### 3.1 Swallowed failure returns `0`, most serious

```java
} catch (Exception e) {
    // something went wrong (for example the employee was not found) - return zero
    return 0;
}
```

- A failure becomes a legitimate-looking number. A caller cannot tell "this person
  earns nothing" from "the lookup failed".
- It catches `Exception` broadly, so it hides every bug (typos, null pointers, future
  failures), not only the expected one.
- The original exception is discarded: no cause, no context, no log.
- Missing employees are detected **by accident**. There is no null check. The
  `NullPointerException` only occurs at `employee.getName()` in the `println`, after
  the calculation has already run.
- Invalid hours are not detected at all.

The standard's bad examples show this pattern, and this one is worse than "log and
carry on", because it does not even log.

### 3.2 Hard-wired dependency (DIP)

```java
private final EmployeeRepository repository = new InMemoryEmployeeRepository();
```

An interface exists, but the class instantiates the concrete implementation itself.
A test cannot substitute a fake or a Mockito mock, so it cannot simulate "employee
not found" or "repository failure" without editing production code. This is the root
cause of the weak tests: the code was not testable, so the tests were written around
that.

### 3.3 One method does five jobs (SRP)

`calculateNetPay` looks up the employee, computes overtime, selects a tax band,
computes tax, and prints. It also decides the failure policy. It has several
independent reasons to change: overtime rule, tax rule, output, error policy, data
source.
A function should be small and do one thing.

### 3.4 Not open for extension (OCP)

The `if / else if / else` chain on tax bands has a bad shape
which means every new rule means editing this method. The brief says a new tax band
with its own rules is coming. As written, adding it means editing and risking the
existing logic.

### 3.5 `double` for money

`double` cannot represent many decimal amounts exactly, and the results above are
unrounded (`949.9905`). Payroll needs `BigDecimal` with an explicit scale and rounding
mode. The rounding rule is a business decision that is currently implicit and
undocumented.

### 3.6 Hidden side effect

`System.out.println` inside a calculation method. It cannot be asserted on, it prints
for every caller, and the method name does not mention it.

### 3.7 Magic numbers

`40`, `0.5`, `1000`, `5000`, `0.05`, `0.15`, `0.25`. The comments `// LOW` and
`// MEDIUM` show the numbers need names. If a threshold changes, there is nothing
searchable to find.

### 3.8 Comments explain *what*, not *why*

For example `// overtime: extra hours beyond 40 are paid at time-and-a-half` and
`// tax band, worked out from gross pay`. These should be replaced by well-named
methods and constants. A comment should remain only for a real business reason.

### 3.9 API design smells

- Three parameters (§4.3); two are `double`s (`hoursWorked`, `hourlyRate`) and can be
  swapped without the compiler noticing.
- `EmployeeRepository.findById` returns `null` for "not found", documented only by a
  comment in the implementation. Callers must remember to check but the codebase did not.
- `Employee` is a class with getters only; a `record` with field validators would be better.


## 4. Tests that pass without testing anything

All three AI-generated tests are unreliable. In order of how empty they are:

| # | Test | Why it proves nothing | Evidence |
|---|---|---|---|
| 1 | `calculatesOvertimePay` | **Mocks the class under test**, stubs the answer `1402.5`, then asserts it gets `1402.5` back. It tests Mockito; no line of `PayrollCalculator` runs. `1402.5` happens to be the correct value for 50h @ 30, which makes it look legitimate. | With the overtime multiplier changed to `0.0`, the real class returns `1485.0` instead of `1402.5`; this test still passes. In the `mvn test` log it also prints no "Net pay" line, because no real code ran. |
| 2 | `returnsPayForAnEmployee` | `assertNotNull(net)` where `net` is a primitive `double`. It is auto-boxed, so the assertion **cannot fail**. The test would pass even for an unknown employee (which returns `0.0`). | By construction, it can never fail. |
| 3 | `calculatesNetPay` | `assertTrue(net > 0)` accepts any positive number. It runs real code but does not check the value. | With the LOW tax rate changed from 5% to 50%, 40h @ 20 gives `400.0` instead of `760.0`; the test still passes. |


### What the tests never check at all

- Unknown employee or `null` id
- Zero, negative or absurd hours
- The overtime boundary (exactly 40 vs 41 hours)
- The tax boundaries (`999.99`/`1000`, `4999.99`/`5000`) and the HIGH band
- That overtime is paid at the right multiplier
- That any exception is raised or reported

### Why green is meaningless here

The standard says a test must make an assertion that would fail if the behaviour were
wrong, and that coverage alone does not meet the standard. Measured result: the tests
reach 63% of lines but kill only 3 of 21 mutants (14%), and 8 mutants survive on lines
the tests do run (section 2). Because the code under test hid its failures (`return 0`), even a
well-written test of the "employee not found" path could not have been written against
it.

## 5. Which problem is the most serious, and why

**The swallowed failure (3.1).**

- In payroll, a wrong payslip is the real-world harm. This is the only issue that
  makes the system produce a wrong payslip with no signal at all.
- It removes the one thing a caller needs: knowing whether the result can be trusted.
- It hides all other defects, including ones introduced later (such as the new tax
  band).
- It undermines the test suite as a safety net, because failures never surface.

The hollow tests (section 4) are what let this ship, so they are a close second. The
hard-wired repository (3.2) is the **root cause** that kept the failure paths from
being testable.

## 6. Plan: how each finding will be addressed

| Finding | Planned response |
|---|---|
| 3.1 swallowed failure | Custom unchecked exceptions (`EmployeeNotFoundException`, `InvalidInputException`, shared base); explicit input validation; keep the cause when wrapping |
| 3.2 hard-wired dependency | Constructor injection of `EmployeeRepository` |
| 3.3 SRP | Separate classes for overtime/gross pay, tax calculation, and the service that coordinates them |
| 3.4 OCP | Tax bands as an injected; a new band is a new class or entry, with no edits to existing logic |
| 3.5 `double` money | `BigDecimal` with explicit scale and rounding |
| 3.6 side effect | Return a `Payslip` value; printing moves to the caller (`Demo`) |
| 3.7, 3.8 Magic numbers, names and useless comments | Named constants; descriptive method names; remove "what" comments |
| 3.9 API smells | `Optional<Employee>` from the repository; group parameters into a record |
| Tests (section 4) | Delete all three; replace with real assertions on exact values, boundaries and failures; use Mockito only for the repository seam |

## 7. Design decisions

These fill the gaps the brief leaves open

| Topic | Decision | Consequence for the code and tests |
|---|---|---|
| Money type | `BigDecimal` for rate, gross, tax and net | Replaces `double` money (3.5) |
| Rounding | 2 decimal places, `RoundingMode.HALF_UP` . Gross and tax are each rounded; net = gross - tax  | Payslip lines always add up exactly. Tests assert exact values such as `949.99` |
| Hours validation | Reject `NaN`, infinite and negative values . Reject more than 120. 120 itself is accepted . Zero hours accepted  | `InvalidInputException`. Boundary tests at 0, 120 and just above 120 |
| Maximum hours | 120 per pay period, a named constant | Design decision by choice |
| Hourly rate | Passed in by the caller. Negative rate rejected  | Prevent calculating negative amounts |
| Unknown employee | The service throws `EmployeeNotFoundException`. It never returns `0`. The caller reports the employee as "unknown" and continues with the next one | The failure stays visible|
