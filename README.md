# Workshop Tool Checkout with Safety Gate

A Java command-line application for managing tool loans in a workshop or makerspace. Members can only borrow tools after completing the required safety training. Records survive application restarts through plain-text file storage.

---

## Folder Structure

```
Skill_Lab/
├── Main.java                  ← Entry point; menu and user input
├── Tool.java                  ← Represents a physical tool
├── Member.java                ← Represents a registered member
├── Loan.java                  ← Represents one checkout transaction
├── WorkshopToolSystem.java    ← Business rules (checkout, returns, reports)
├── DataManager.java           ← File reading and writing
├── data/                      ← Created automatically on first run
│   ├── tools.txt
│   ├── members.txt
│   └── loans.txt
├── README.md
├── CODE_EXPLANATION.md
├── VIVA_GUIDE.md
└── POSTER_CONTENT.md
```

---

## Requirements

- Java 11 or later (check with `java -version`)
- No external libraries needed

---

## Compilation

Open a terminal in the `Skill_Lab` folder and run:

```
javac Tool.java Member.java Loan.java DataManager.java WorkshopToolSystem.java Main.java
```

All six `.java` files must be in the same folder. If compilation succeeds, you will see six `.class` files appear.

---

## Running the Program

```
java Main
```

The program creates the `data/` folder automatically on first run and loads sample data.

---

## Sample Data (loaded on first run only)

### Tools

| Tool ID | Name              | Required Training   | Status         |
|---------|-------------------|---------------------|----------------|
| T001    | Oscilloscope      | ELECTRICAL_SAFETY   | AVAILABLE      |
| T002    | Soldering Station | SOLDERING_SAFETY    | AVAILABLE      |
| T003    | Angle Grinder     | POWER_TOOL_SAFETY   | AVAILABLE      |
| T004    | Drill Press       | POWER_TOOL_SAFETY   | MAINTENANCE    |

### Members

| Member ID | Name         | Completed Training                          |
|-----------|--------------|---------------------------------------------|
| M001      | Alice Sharma | ELECTRICAL_SAFETY, POWER_TOOL_SAFETY        |
| M002      | Bob Patel    | SOLDERING_SAFETY                            |
| M003      | Carol Nair   | (none)                                      |

### Training Codes Used

| Code                | Meaning                                  |
|---------------------|------------------------------------------|
| ELECTRICAL_SAFETY   | Required for electrical test equipment   |
| SOLDERING_SAFETY    | Required for soldering tools             |
| POWER_TOOL_SAFETY   | Required for power tools                 |

---

## Five-Minute Terminal Demonstration Plan

Work through these steps in order. Each step takes about 30 seconds.

### Step 1 — View tools (Menu option 1)
You will see all four sample tools with their status. T004 shows MAINTENANCE.

### Step 2 — View members (Menu option 2)
You will see the three sample members and their training records.

### Step 3 — Reject checkout: missing training (Menu option 6)
- Member ID: `M003`
- Tool ID: `T001`
- Duration: `7`

Expected result: *"Checkout rejected: Carol Nair has not completed 'ELECTRICAL_SAFETY' training..."*

### Step 4 — Reject checkout: tool under maintenance (Menu option 6)
- Member ID: `M001`
- Tool ID: `T004`
- Duration: `3`

Expected result: *"Checkout rejected: 'Drill Press' is currently under maintenance."*

### Step 5 — Approve a valid checkout (Menu option 6)
- Member ID: `M001`
- Tool ID: `T001`
- Duration: `5`

Expected result: Loan approved with a loan ID (e.g. `L001`), checkout and due date displayed.

### Step 6 — Reject second checkout of same tool (Menu option 6)
- Member ID: `M001`
- Tool ID: `T001`
- Duration: `2`

Expected result: *"Checkout rejected: 'Oscilloscope' is already checked out."*

### Step 7 — View active loans (Menu option 8)
You will see the active loan for T001.

### Step 8 — Return the tool with damage (Menu option 7)
- Loan ID: `L001`
- Condition: `DAMAGED`
- Note: `cracked probe tip`

Expected result: Return recorded. T001 is flagged for maintenance.

### Step 9 — View maintenance report (Menu option 10)
T001 and T004 will both appear under maintenance.

### Step 10 — Save and exit (Menu option 0), then restart
Run `java Main` again. All records reload from files — no data was lost.

---

## Notes for Students

- The `data/` folder is created in the same directory from which you run `java Main`.
- Sample data is only loaded once. Deleting the `data/` folder resets everything.
- Loan IDs are generated automatically: L001, L002, etc.
- Always run the program from the `Skill_Lab` folder so file paths work correctly.
