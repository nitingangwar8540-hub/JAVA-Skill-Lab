import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single borrowing transaction (checkout → return).
 * A loan is "active" until a return date is recorded.
 */
public class Loan {

    // Standard date format used in both display and file storage
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private String loanId;
    private String toolId;
    private String memberId;
    private LocalDate checkoutDate;
    private LocalDate dueDate;
    private LocalDate returnDate;      // null while the loan is still active
    private String returnCondition;    // GOOD, DAMAGED, NEEDS_INSPECTION, or null
    private String damageNote;         // optional note about damage

    // Constructor for a new checkout (no return info yet)
    public Loan(String loanId, String toolId, String memberId,
                LocalDate checkoutDate, LocalDate dueDate) {
        this.loanId = loanId;
        this.toolId = toolId;
        this.memberId = memberId;
        this.checkoutDate = checkoutDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.returnCondition = null;
        this.damageNote = null;
    }

    // Constructor used when loading a complete record from file
    public Loan(String loanId, String toolId, String memberId,
                LocalDate checkoutDate, LocalDate dueDate,
                LocalDate returnDate, String returnCondition, String damageNote) {
        this.loanId = loanId;
        this.toolId = toolId;
        this.memberId = memberId;
        this.checkoutDate = checkoutDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.returnCondition = returnCondition;
        this.damageNote = damageNote;
    }

    // --- Getters ---

    public String getLoanId() {
        return loanId;
    }

    public String getToolId() {
        return toolId;
    }

    public String getMemberId() {
        return memberId;
    }

    public LocalDate getCheckoutDate() {
        return checkoutDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public String getReturnCondition() {
        return returnCondition;
    }

    public String getDamageNote() {
        return damageNote;
    }

    /**
     * A loan is active when it has not yet been returned.
     */
    public boolean isActive() {
        return returnDate == null;
    }

    /**
     * A loan is overdue when it is still active and today is past the due date.
     * LocalDate.now() returns today's date; compareTo returns negative if dueDate is before today.
     */
    public boolean isOverdue() {
        return isActive() && LocalDate.now().isAfter(dueDate);
    }

    /**
     * Records the return of this tool, including condition and an optional damage note.
     */
    public void recordReturn(LocalDate returnDate, String returnCondition, String damageNote) {
        this.returnDate = returnDate;
        this.returnCondition = returnCondition;
        this.damageNote = damageNote;
    }

    /**
     * Returns a formatted summary line for display in reports.
     */
    public String getSummary() {
        String returnInfo = isActive() ? "Active" : "Returned: " + returnDate.format(DATE_FORMAT);
        String condition = (returnCondition != null) ? returnCondition : "-";
        return String.format("%-8s | Tool:%-8s | Member:%-8s | Due:%s | %s | Cond:%s",
                loanId, toolId, memberId, dueDate.format(DATE_FORMAT), returnInfo, condition);
    }

    /**
     * Converts this loan to a pipe-delimited string for saving to a file.
     * Null fields are stored as the literal word "null" for easy parsing.
     * Format: loanId|toolId|memberId|checkoutDate|dueDate|returnDate|returnCondition|damageNote
     */
    public String toFileString() {
        String retDate = (returnDate != null) ? returnDate.format(DATE_FORMAT) : "null";
        String retCondition = (returnCondition != null) ? returnCondition : "null";
        String note = (damageNote != null && !damageNote.isEmpty()) ? damageNote : "null";
        return loanId + "|" + toolId + "|" + memberId + "|"
                + checkoutDate.format(DATE_FORMAT) + "|"
                + dueDate.format(DATE_FORMAT) + "|"
                + retDate + "|" + retCondition + "|" + note;
    }

    /**
     * Rebuilds a Loan object from a pipe-delimited file line.
     */
    public static Loan fromFileString(String line) {
        String[] parts = line.split("\\|", -1);
        String loanId = parts[0];
        String toolId = parts[1];
        String memberId = parts[2];
        LocalDate checkoutDate = LocalDate.parse(parts[3], DATE_FORMAT);
        LocalDate dueDate = LocalDate.parse(parts[4], DATE_FORMAT);
        LocalDate returnDate = parts[5].equals("null") ? null : LocalDate.parse(parts[5], DATE_FORMAT);
        String returnCondition = parts[6].equals("null") ? null : parts[6];
        String damageNote = parts[7].equals("null") ? null : parts[7];
        return new Loan(loanId, toolId, memberId, checkoutDate, dueDate, returnDate, returnCondition, damageNote);
    }
}
