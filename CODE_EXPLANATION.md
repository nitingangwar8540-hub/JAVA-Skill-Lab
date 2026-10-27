# Code Explanation — Workshop Tool Checkout with Safety Gate

This document explains every class, important field, constructor, and method in the actual source code. Read this alongside the code files.

---

## Why Six Files?

Each file has exactly one responsibility. This makes the code easier to understand and easier to divide among four students.

| File | Responsibility |
|---|---|
| `Tool.java` | Stores everything about one physical tool |
| `Member.java` | Stores everything about one registered member |
| `Loan.java` | Stores everything about one checkout transaction |
| `WorkshopToolSystem.java` | Enforces all business rules |
| `DataManager.java` | Reads and writes data files |
| `Main.java` | Displays the menu and reads user input |

---

## Tool.java

### Why this class exists
A workshop has many physical tools. Each one needs its own identity (ID and name), its own safety requirement (required training), and a flag showing whether it is broken or in repair.

### Fields
```java
private String toolId;          // unique identifier, e.g. "T001"
private String name;            // human-readable name, e.g. "Oscilloscope"
private String requiredTraining; // training code the borrower must have
private boolean underMaintenance; // true = cannot be borrowed right now
```
All four fields are `private`. This is **encapsulation** — no other class can set `toolId` or `name` accidentally. Only `underMaintenance` has a setter, because that is the only field that legitimately changes after creation.

### Constructor
```java
public Tool(String toolId, String name, String requiredTraining, boolean underMaintenance)
```
Sets all four fields at once. A tool cannot exist without an ID, a name, and a training requirement — the constructor enforces this.

### `isAvailable(ArrayList<Loan> activeLoans)`
A tool is available when **two conditions are both true**: it is not under maintenance, AND it has no active loan. The method accepts the full loan list so `Tool` does not need to hold a reference to loans internally. This keeps each class focused on its own data.

### `getSummary(ArrayList<Loan> activeLoans)`
Formats one line of text for the "View tools" report. The status is derived fresh each time from `isUnderMaintenance()` and `isAvailable()` so it always reflects the current state.

### `toFileString()` and `fromFileString(String line)`
Convert between a `Tool` object and a pipe-separated line of text for saving to `tools.txt`. For example:
```
T001|Oscilloscope|ELECTRICAL_SAFETY|false
```
`fromFileString` is a **static method** because it creates a new object from raw text — it does not belong to any existing `Tool` instance.

---

## Member.java

### Why this class exists
A member has an identity, a name, and a growing list of completed training codes. The list changes over time (training is added), so it needs its own class to manage it.

### Fields
```java
private String memberId;                       // unique identifier, e.g. "M001"
private String name;                           // full name, e.g. "Alice Sharma"
private ArrayList<String> completedTraining;   // list of training codes completed
```
`completedTraining` is an **ArrayList** because the number of training records is not fixed. ArrayList grows automatically as training is added.

### Constructors
Two constructors exist:
1. `Member(String memberId, String name)` — used when registering a brand-new member. Training list starts empty.
2. `Member(String memberId, String name, ArrayList<String> completedTraining)` — used when loading from a file where the training list is already known.

### `hasCompletedTraining(String trainingCode)`
Loops through `completedTraining` and compares each entry using `equalsIgnoreCase`. Returns `true` if a match is found. This method is the **safety gate** — `WorkshopToolSystem` calls it during checkout validation.

### `addTraining(String trainingCode)`
First checks whether the training is already recorded (to prevent duplicates), then adds it. Returns `true` if newly added, `false` if it was already there.

---

## Loan.java

### Why this class exists
One checkout transaction involves many pieces of information: who borrowed what tool, when, for how long, and what happened when it was returned. Grouping this in one class keeps all transaction data together.

### Fields
```java
private String loanId;           // unique ID, e.g. "L001"
private String toolId;           // which tool was borrowed
private String memberId;         // who borrowed it
private LocalDate checkoutDate;  // the day it was taken
private LocalDate dueDate;       // the day it must be returned
private LocalDate returnDate;    // null while still active
private String returnCondition;  // GOOD, DAMAGED, or NEEDS_INSPECTION
private String damageNote;       // optional text description
```

### Why `LocalDate`?
`LocalDate` is Java's built-in type for calendar dates (year-month-day). It lets us:
- Calculate whether a loan is overdue: `LocalDate.now().isAfter(dueDate)`
- Add days: `today.plusDays(durationDays)` to compute the due date
- Format dates as readable strings: `dueDate.format(DATE_FORMAT)`

### `isActive()`
```java
public boolean isActive() {
    return returnDate == null;
}
```
A loan is active as long as no return date has been recorded. Simple and clear.

### `isOverdue()`
```java
public boolean isOverdue() {
    return isActive() && LocalDate.now().isAfter(dueDate);
}
```
A loan is overdue only when it is **still active** AND today's date is after the due date. Returned loans are never counted as overdue because `isActive()` returns false once `returnDate` is set.

### `recordReturn(...)`
Sets `returnDate`, `returnCondition`, and `damageNote` in one step. Before this is called, all three fields are null.

### File format for loans
```
L001|T001|M001|2026-10-27|2026-11-01|null|null|null
```
The literal word "null" is written for fields that have no value yet, and parsed back correctly by `fromFileString`.

---

## WorkshopToolSystem.java

### Why this class exists
This is the brain of the application. It knows all the rules: who can borrow what, what happens when a tool is returned damaged, and how to generate reports. Keeping all rules here means `Main.java` stays simple (just menu and input) and `DataManager.java` stays simple (just file operations).

### Fields
```java
private ArrayList<Tool>   tools;
private ArrayList<Member> members;
private ArrayList<Loan>   loans;
private DataManager       dataManager;
private int               loanCounter;
```
Three **ArrayList** collections hold all the objects in memory. The `loanCounter` tracks the next available loan number.

### Constructor
Calls `DataManager` to load all three lists from files. If this is the first run (no files exist), it calls `loadSampleData()`. Then it calls `calculateNextLoanCounter()` so loan IDs continue from where the file left off even after a restart.

### Checkout Validation — Step by Step

`checkoutTool(String memberId, String toolId, int durationDays)` checks six conditions **in order**. If any condition fails, it returns an error message immediately and no loan is created.

| Step | Condition checked | Error message if it fails |
|---|---|---|
| 1 | Does the member ID exist? | "Member ID not found" |
| 2 | Does the tool ID exist? | "Tool ID not found" |
| 3 | Is the tool free of maintenance? | "Tool is currently under maintenance" |
| 4 | Does the tool have no active loan? | "Tool is already checked out" |
| 5 | Has the member completed required training? | "Member has not completed training" |
| 6 | Is the duration at least 1 day? | "Loan duration must be at least 1 day" |

Only when all six pass does the method create a new `Loan` object, add it to the list, and save.

### Return Processing — `returnTool(...)`

1. Validate the condition string (must be GOOD, DAMAGED, or NEEDS_INSPECTION).
2. Find the active loan using `findActiveLoan(loanId)`.
3. If there is no active loan, check whether the loan exists at all — this gives a better error message for duplicate returns.
4. Call `loan.recordReturn(...)` to set the dates and condition.
5. If condition is DAMAGED or NEEDS_INSPECTION, set `tool.setUnderMaintenance(true)` and save tools.
6. Save loans.

### Private Helper Methods

`findTool`, `findMember`, `hasActiveLoan`, `findActiveLoan`, `findAnyLoan` — each loops through the appropriate ArrayList and returns a match or null. These small helpers are called by the bigger methods above. Keeping them private means they cannot be called from outside this class.

---

## DataManager.java

### Why this class exists
File reading and writing is kept completely separate from the business rules. `WorkshopToolSystem` never reads or writes files directly — it always calls `DataManager`. This means you can understand the checkout rules without thinking about files, and you can understand the file format without thinking about checkout rules.

### How files are organized
- `data/tools.txt` — one tool per line
- `data/members.txt` — one member per line
- `data/loans.txt` — one loan per line

### Load methods
Each `load` method (`loadTools`, `loadMembers`, `loadLoans`) uses a **BufferedReader** to read the file line by line. Each non-empty line is passed to the corresponding `fromFileString` method. If the file does not exist, an empty ArrayList is returned without crashing.

### Save methods
Each `save` method writes the entire list to the file using a **BufferedWriter**, overwriting the previous contents. All historical loans are preserved because the full list (including returned loans) is always written.

### try-catch for file errors
```java
try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
    ...
} catch (IOException e) {
    System.out.println("Warning: Could not read tools file. " + e.getMessage());
}
```
The `try-catch` here handles two realistic failures: the file cannot be opened (permissions problem) or a line cannot be read (disk error). The program prints a clear warning and continues rather than crashing.

The `try (...)` syntax automatically closes the file when the block ends, even if an error occurs.

### `isFirstRun()`
Returns true when none of the three data files exist. Called once at startup to decide whether to load sample data.

---

## Main.java

### Why this class exists
Separating the menu from the business logic means that if you want to change how questions are asked, you only edit `Main.java`. The rules never change because the display changed.

### `readIntInput(Scanner scanner)`
```java
private static int readIntInput(Scanner scanner) {
    try {
        String line = scanner.nextLine().trim();
        return Integer.parseInt(line);
    } catch (NumberFormatException e) {
        System.out.println("Invalid input: please enter a whole number.");
        return -1;
    }
}
```
The `try-catch` here handles the case where the user types something that is not a number. `Integer.parseInt` would throw a `NumberFormatException` if the input is "abc". By catching it, the program prints a friendly message and returns -1, which the `switch` statement treats as an invalid choice.

### `switch (choice)` — the menu loop
The `while (running)` loop keeps displaying the menu until the user types 0. Inside the loop, `switch` maps each number to the correct system method. The default case handles any number that does not match a menu option.

---

## How All Classes Communicate

```
Main.java
   │  calls methods on
   ▼
WorkshopToolSystem.java
   │  reads/writes via          │  accesses data from
   ▼                            ▼
DataManager.java         Tool.java  Member.java  Loan.java
   │  reads/writes
   ▼
data/tools.txt
data/members.txt
data/loans.txt
```

`Main` only talks to `WorkshopToolSystem`. `WorkshopToolSystem` talks to `DataManager` for file operations and holds `ArrayList` collections of `Tool`, `Member`, and `Loan` objects. `DataManager` only reads and writes files.

---

## How ArrayList is Used

Three ArrayLists hold the in-memory state of the application:
```java
ArrayList<Tool>   tools;
ArrayList<Member> members;
ArrayList<Loan>   loans;
```

- `tools.add(newTool)` — adds a tool when "Add a tool" is selected.
- `loans.add(loan)` — adds a loan when checkout is approved.
- Iteration: `for (Tool tool : tools)` — reads every tool to display the list.
- Search: the `findTool` helper loops through `tools` and compares IDs.

ArrayList is used because the number of tools, members, and loans changes at runtime. A fixed-size array would either waste space or run out of space.

---

## Why Encapsulation is Useful Here

All fields in `Tool`, `Member`, and `Loan` are `private`. This means:

- `WorkshopToolSystem` cannot accidentally change a tool's `toolId` after creation — there is no setter for it.
- The maintenance flag can only be changed through `setUnderMaintenance(boolean)`, which is called in one controlled place (after a damaged return).
- Training codes can only be added through `addTraining()`, which prevents duplicates.

Without encapsulation, any method in any class could change any field, making bugs very hard to trace.

---

## How `LocalDate` Identifies Overdue Loans

`LocalDate.now()` returns today's date.

When a loan is created:
```java
LocalDate today   = LocalDate.now();
LocalDate dueDate = today.plusDays(durationDays);
```

When checking for overdue status:
```java
public boolean isOverdue() {
    return isActive() && LocalDate.now().isAfter(dueDate);
}
```

`isAfter` returns true if today is strictly later than `dueDate`. A loan due today is not yet overdue. A returned loan is never overdue because `isActive()` returns false.

---

## Where try-catch is Used and What It Handles

| Location | What it catches | What it does |
|---|---|---|
| `DataManager.loadTools()` | `IOException` | File cannot be read; prints warning, returns empty list |
| `DataManager.saveTools()` | `IOException` | File cannot be written; prints error message |
| Same pattern in `loadMembers`, `saveMembers`, `loadLoans`, `saveLoans` | `IOException` | Same as above |
| `Main.readIntInput()` | `NumberFormatException` | User typed non-numeric text; prints message, returns -1 |
| `WorkshopToolSystem.calculateNextLoanCounter()` | `NumberFormatException` | Non-standard loan ID in file; skips that ID |

No catch block is empty. Each one either prints a message or returns a safe default value.
