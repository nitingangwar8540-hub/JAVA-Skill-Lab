# POSTER CONTENT
## Workshop Tool Checkout with Safety Gate
### Java Command-Line Application

---

## The Problem

Makerspaces and laboratories lend tools to members. Without a tracking system:
- Tools are lost or left unchecked
- Members borrow equipment they are not trained to use
- Nobody knows which tools are broken or under repair
- There is no record of who borrowed what and when

---

## Our Solution

A Java command-line application that manages the complete lifecycle of tool loans with a built-in **safety gate** that enforces training prerequisites before any checkout is approved.

---

## Workflow

```
Member requests a tool
        │
        ▼
  Is the member registered?  ──NO──▶  REJECTED
        │ YES
        ▼
  Does the tool exist?        ──NO──▶  REJECTED
        │ YES
        ▼
  Is tool under maintenance?  ─YES──▶  REJECTED
        │ NO
        ▼
  Is tool already checked out?─YES──▶  REJECTED
        │ NO
        ▼
  Has member completed         ─NO──▶  REJECTED
  required training?                  (Safety Gate)
        │ YES
        ▼
  Loan duration > 0?          ──NO──▶  REJECTED
        │ YES
        ▼
  CHECKOUT APPROVED ✓
  Loan ID + Due Date generated
  Record saved to file
```

---

## Main Features

| Feature | Description |
|---|---|
| Tool management | Add tools with required training codes |
| Member registration | Register members, record training completions |
| Safety gate checkout | Validates 6 conditions before approving |
| Tool return | Records condition: GOOD / DAMAGED / NEEDS_INSPECTION |
| Auto-maintenance flag | Damaged or needs-inspection tools flagged automatically |
| Active loans report | See everything currently checked out |
| Overdue loans report | Highlights loans past their due date |
| Maintenance report | Lists all tools awaiting repair |
| Full loan history | Complete audit trail of all borrowing |
| Persistent storage | Records survive program restarts via text files |

---

## Java Concepts Used and Why

| Concept | Where and Why |
|---|---|
| **Classes & Objects** | `Tool`, `Member`, `Loan` each model one real-world entity |
| **Constructors** | Ensure every object is created with required information |
| **Encapsulation** | `private` fields protect data; setters only where needed |
| **Methods** | Each method does one thing: validate, checkout, return, report |
| **Conditions (if)** | Enforce each of the 6 checkout rules in order |
| **Loops & switch** | Menu loop keeps running; switch maps choices to actions |
| **ArrayList** | Stores tools, members, and loans that grow at runtime |
| **try-catch** | Handles file errors and invalid user input without crashing |
| **LocalDate** | Calculates due dates and detects overdue loans accurately |
| **File Handling** | Saves all records so data survives after the program closes |

---

## Sample Demonstration

| Test | Member | Tool | Result |
|---|---|---|---|
| Missing training | Carol (M003) | Oscilloscope (T001) | **REJECTED** — ELECTRICAL_SAFETY required |
| Maintenance blocked | Alice (M001) | Drill Press (T004) | **REJECTED** — tool under maintenance |
| Valid checkout | Alice (M001) | Oscilloscope (T001) | **APPROVED** — Loan ID: L001 |
| Duplicate checkout | Alice (M001) | Oscilloscope (T001) | **REJECTED** — already checked out |
| Return with damage | Loan L001 | DAMAGED condition | Tool flagged for maintenance |

---

## Project Structure

6 Java files · 3 data files · 4 documentation files
No external libraries · Compiles with standard `javac`

```
javac *.java
java Main
```

---

## Expected Outcomes

- Members can only borrow tools they are trained to use
- Broken tools are automatically removed from availability
- Full audit trail of every loan, return, and damage event
- Records reload correctly after every program restart
- The application is explainable by any team member

---

## Team

Second-Year Engineering · Skill Lab Project · 2026
