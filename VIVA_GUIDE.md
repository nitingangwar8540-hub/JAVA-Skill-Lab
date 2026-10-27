# Viva Guide — Workshop Tool Checkout with Safety Gate

This guide covers questions your teacher is likely to ask. Each answer is written in plain language so all four team members can explain it in their own words.

---

## Section 1 — About the Project

**Q1. What problem does this application solve?**

A workshop lends tools to members. Without a system, there is no way to know who has which tool, whether a tool is broken, or whether the person borrowing it knows how to use it safely. Our application tracks all of this: which tools exist, who is registered, what training each member has completed, and who currently has which tool. It prevents unsafe borrowing using a safety gate check.

---

**Q2. What is the safety gate?**

Before allowing a checkout, the program checks whether the member has completed the training that the tool requires. For example, the Oscilloscope requires `ELECTRICAL_SAFETY` training. If the member has not recorded that training, the checkout is rejected with a clear reason. This is the safety gate.

---

**Q3. What happens when you run the program for the first time?**

The program checks whether the `data/` folder and its three text files exist. If they do not, it creates the folder and loads sample data — four tools and three members with different training records. It saves these to the files so they are available every time the program restarts.

---

**Q4. How many Java files does the project have, and what is each one for?**

Six files:
- `Tool.java` — represents one physical tool
- `Member.java` — represents one registered member
- `Loan.java` — represents one checkout transaction
- `WorkshopToolSystem.java` — enforces all the rules (checkout, return, reports)
- `DataManager.java` — reads and writes the text files
- `Main.java` — displays the menu and reads the user's typing

---

## Section 2 — Java Concepts Used

**Q5. What is a class and an object? Give a real example from your project.**

A class is a blueprint. An object is one specific thing made from that blueprint. `Tool.java` is the class — it describes what every tool has (ID, name, training, maintenance status). When the program loads the Oscilloscope, it creates one `Tool` object with `toolId = "T001"`, `name = "Oscilloscope"`, etc. A class defines the structure; an object holds the actual values.

---

**Q6. What is a constructor and why did you use it?**

A constructor is a special method that runs when you create a new object. We use it to make sure every object starts with the information it needs. For example:
```java
new Tool("T001", "Oscilloscope", "ELECTRICAL_SAFETY", false)
```
This creates a `Tool` object and sets all four fields immediately. Without a constructor, we would have to set each field separately and could forget one.

---

**Q7. What is encapsulation and where do you use it?**

Encapsulation means making fields `private` and controlling access through methods. In our project, `Tool`, `Member`, and `Loan` all have `private` fields. For example, `toolId` in `Tool.java` has no setter. Once a tool is created, its ID cannot be changed from outside the class. This prevents accidental changes. The maintenance status has a setter (`setUnderMaintenance`) because it genuinely needs to change when a tool is returned damaged.

---

**Q8. Why did you use ArrayList instead of a regular array?**

The number of tools, members, and loans changes while the program runs — tools are added, members are registered, loans are created. A regular array has a fixed size decided at the start. ArrayList grows automatically when you add items. We never need to know in advance how many records there will be, so ArrayList is the right choice.

---

**Q9. How does the program use `LocalDate`?**

`LocalDate` is Java's class for calendar dates (year, month, day, no time). We use it in three ways:
1. `LocalDate.now()` — gets today's date when a tool is checked out or returned.
2. `today.plusDays(durationDays)` — calculates the due date.
3. `LocalDate.now().isAfter(dueDate)` — checks whether a loan is overdue.

Without `LocalDate`, we would have to handle date arithmetic manually, which is error-prone.

---

**Q10. Where do you use `try-catch` and why?**

Two places:
1. **File operations in `DataManager`** — Reading or writing a file can fail if the file is missing or there is a disk error. These are `IOException` errors. We catch them and print a clear warning instead of crashing.
2. **User input in `Main`** — When the menu asks for a number, the user might type letters. `Integer.parseInt` throws `NumberFormatException` in that case. We catch it and print "Invalid input: please enter a whole number." The program continues normally.

---

**Q11. How does file saving work?**

Each record is converted to a pipe-separated line of text using `toFileString()`. For example, a tool becomes `T001|Oscilloscope|ELECTRICAL_SAFETY|false`. All records are written to the file one line at a time. On the next startup, each line is read and converted back to an object using `fromFileString()`. This means data survives even after the program closes.

---

**Q12. How does the switch statement work in your program?**

`Main.java` reads the user's number and uses `switch (choice)` to decide what to do. Each `case` maps one number to one action. For example, `case 6` calls `checkoutInteraction(...)`. The `default` case handles any number that is not listed, printing "Invalid choice." A `while` loop keeps the menu running until the user types 0.

---

## Section 3 — Business Rules

**Q13. What are the six reasons a checkout can be rejected?**

1. The member ID is not registered.
2. The tool ID is not registered.
3. The tool is under maintenance.
4. The tool already has an active loan (it is checked out to someone else).
5. The member has not completed the training required for that tool.
6. The loan duration entered is zero or negative.

All six are checked in `WorkshopToolSystem.checkoutTool()`.

---

**Q14. What happens when a tool is returned as DAMAGED?**

1. The return date and condition are recorded on the loan.
2. The tool's `underMaintenance` flag is set to `true`.
3. The tools file is saved so the maintenance status persists.
4. From this point on, the tool appears as "MAINTENANCE" in the tools list and cannot be checked out.

The same happens for `NEEDS_INSPECTION`. Only `GOOD` returns leave the tool available.

---

**Q15. Can a loan that has already been returned be returned a second time?**

No. `returnTool()` first calls `findActiveLoan(loanId)`, which only returns loans where `returnDate == null`. If the loan has already been returned, `findActiveLoan` returns null. The code then checks whether the loan exists at all using `findAnyLoan` and prints the message: "Loan has already been returned." No changes are made to any record.

---

**Q16. How does the overdue report work?**

`showOverdueLoans()` loops through every loan and calls `loan.isOverdue()`. This method returns `true` only when `isActive()` is `true` (not yet returned) AND `LocalDate.now().isAfter(dueDate)` is `true` (today is past the due date). Returned loans are excluded automatically because their `isActive()` is `false`.

---

**Q17. How does the program prevent duplicate IDs?**

When adding a tool, `addTool()` calls `findTool(toolId)` first. If it finds an existing tool with the same ID, it returns an error message and does not add the tool. The same pattern is used in `registerMember()`.

---

## Section 4 — Design Decisions

**Q18. Why is `DataManager` a separate class instead of keeping file code in `WorkshopToolSystem`?**

If file code were inside `WorkshopToolSystem`, you would need to understand file handling just to read the checkout rules, and understand the checkout rules just to fix a file format problem. Separating them means each class has one clear purpose. This is called separation of concerns and it makes the code easier to understand and fix.

---

**Q19. Why does `Tool.isAvailable()` take the loan list as a parameter instead of holding it internally?**

If `Tool` stored a reference to the loans list, `Tool` and `WorkshopToolSystem` would be tightly connected — a change in one would require a change in the other. By passing the list as a parameter, `Tool` stays independent. It only knows about tools, and availability is computed on demand when needed.

---

**Q20. What are the limitations of this application?**

- There is no user login or password. Anyone who can run the program can do anything.
- The text file format would break if a tool name contained a pipe character `|`.
- There is no way to mark a tool as repaired and remove the maintenance flag from the menu (you would need to edit the file directly). This could be added as a future feature.
- The program is single-user only. Two people cannot run it at the same time against the same files.

---

**Q21. If the data folder is deleted, what happens?**

All records are lost and the program starts fresh. On the next run, it detects the files are missing, loads the sample data again, and creates new files. This is mentioned in the README.

---

**Q22. How would you add a new feature, for example "clear maintenance flag"?**

1. Add a new method `clearMaintenance(String toolId)` in `WorkshopToolSystem`.
2. Add a new menu option (e.g. "12. Clear maintenance flag") in `Main.java`.
3. Add a `case 12` in the switch statement that calls the new method.
4. No other files need to change.

This shows that keeping each class focused makes the system easy to extend.

---

## Section 5 — Team and Testing

**Q23. How did you test the safety gate?**

We ran menu option 6 (Check out a tool) with member M003 (Carol Nair, no training) and tool T001 (requires `ELECTRICAL_SAFETY`). The program rejected the request and printed the exact reason: "Carol Nair has not completed 'ELECTRICAL_SAFETY' training required for 'Oscilloscope'." We then tried with M001 (Alice, who has the training) and the checkout was approved.

---

**Q24. How did you verify that records survive a restart?**

After approving a checkout and returning a tool, we ran menu option 0 (Save and exit), then ran `java Main` again. Menu option 11 (Loan history) showed all previous records exactly as before. Menu option 1 (View tools) showed the correct maintenance status.

---

**Q25. Can you explain what happens line by line when this code runs?**

*(Expect the teacher to point to any method and ask you to walk through it. Practice explaining `checkoutTool` line by line — it is the most important method in the project.)*

Walk through it:
1. Look up the member by ID. If null, return error.
2. Look up the tool by ID. If null, return error.
3. Check `tool.isUnderMaintenance()`. If true, return error.
4. Check `hasActiveLoan(toolId)`. If true, return error.
5. Check `member.hasCompletedTraining(tool.getRequiredTraining())`. If false, return error.
6. Check `durationDays <= 0`. If true, return error.
7. Generate a loan ID, calculate due date, create a `Loan` object, add it to the list, save, return success message.
