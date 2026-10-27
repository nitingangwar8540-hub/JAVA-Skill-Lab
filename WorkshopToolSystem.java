import java.time.LocalDate;
import java.util.ArrayList;

/**
 * The central coordinator of the Workshop Tool Checkout system.
 * Enforces all business rules: checkout validation, return processing, and reports.
 * Delegates file I/O to DataManager and does not print to the terminal itself —
 * it returns result messages so Main.java can display them.
 */
public class WorkshopToolSystem {

    private ArrayList<Tool>   tools;
    private ArrayList<Member> members;
    private ArrayList<Loan>   loans;
    private DataManager       dataManager;

    // Counter used to generate unique loan IDs like L001, L002, ...
    private int loanCounter;

    public WorkshopToolSystem() {
        dataManager = new DataManager();

        // Load whatever is saved on disk (empty lists on first run)
        tools   = dataManager.loadTools();
        members = dataManager.loadMembers();
        loans   = dataManager.loadLoans();

        // Set the loan counter to one above the highest existing loan number
        loanCounter = calculateNextLoanCounter();

        // Load sample data only on the very first run (no files exist yet)
        if (dataManager.isFirstRun()) {
            loadSampleData();
        }
    }

    // ─── SETUP HELPERS ──────────────────────────────────────────────────────

    /**
     * Scans existing loan IDs to find the highest number so new IDs continue
     * from where the file left off. This avoids duplicate IDs after restarts.
     */
    private int calculateNextLoanCounter() {
        int maxFound = 0;
        for (Loan loan : loans) {
            String id = loan.getLoanId();
            // Loan IDs look like "L001"; extract the numeric part
            if (id.startsWith("L")) {
                try {
                    int num = Integer.parseInt(id.substring(1));
                    if (num > maxFound) {
                        maxFound = num;
                    }
                } catch (NumberFormatException e) {
                    // Non-standard ID format; skip it
                }
            }
        }
        return maxFound + 1;
    }

    /**
     * Populates realistic sample data on the first run so the user can
     * demonstrate the system immediately without manual data entry.
     *
     * Sample Tool IDs:  T001, T002, T003, T004
     * Sample Member IDs: M001, M002, M003
     * Training codes:    ELECTRICAL_SAFETY, SOLDERING_SAFETY, POWER_TOOL_SAFETY
     */
    private void loadSampleData() {
        // --- Sample Tools ---
        tools.add(new Tool("T001", "Oscilloscope",          "ELECTRICAL_SAFETY",  false));
        tools.add(new Tool("T002", "Soldering Station",     "SOLDERING_SAFETY",   false));
        tools.add(new Tool("T003", "Angle Grinder",         "POWER_TOOL_SAFETY",  false));
        tools.add(new Tool("T004", "Drill Press",           "POWER_TOOL_SAFETY",  true));  // under maintenance

        // --- Sample Members ---
        Member alice = new Member("M001", "Alice Sharma");
        alice.addTraining("ELECTRICAL_SAFETY");
        alice.addTraining("POWER_TOOL_SAFETY");

        Member bob = new Member("M002", "Bob Patel");
        bob.addTraining("SOLDERING_SAFETY");

        Member carol = new Member("M003", "Carol Nair");
        // Carol has no training — good for demonstrating rejected checkouts

        members.add(alice);
        members.add(bob);
        members.add(carol);

        // Save sample data immediately so it persists
        dataManager.saveTools(tools);
        dataManager.saveMembers(members);
        dataManager.saveLoans(loans);  // empty list on first run

        System.out.println("\n[INFO] Sample data loaded. See README.md for demo IDs.");
    }

    // ─── TOOL MANAGEMENT ────────────────────────────────────────────────────

    /**
     * Displays all tools and their current availability status.
     */
    public void listTools() {
        if (tools.isEmpty()) {
            System.out.println("No tools registered yet.");
            return;
        }
        System.out.println("\n--- TOOLS ---");
        System.out.printf("%-8s | %-25s | %-30s | %s%n",
                "ID", "Name", "Required Training", "Status");
        System.out.println("-".repeat(85));
        for (Tool tool : tools) {
            System.out.println(tool.getSummary(loans));
        }
    }

    /**
     * Adds a new tool after checking that the ID is not already taken.
     */
    public String addTool(String toolId, String name, String requiredTraining) {
        if (findTool(toolId) != null) {
            return "Error: Tool ID '" + toolId + "' is already registered.";
        }
        Tool newTool = new Tool(toolId, name, requiredTraining, false);
        tools.add(newTool);
        dataManager.saveTools(tools);
        return "Tool '" + name + "' (ID: " + toolId + ") added successfully.";
    }

    // ─── MEMBER MANAGEMENT ──────────────────────────────────────────────────

    /**
     * Displays all members and their completed training codes.
     */
    public void listMembers() {
        if (members.isEmpty()) {
            System.out.println("No members registered yet.");
            return;
        }
        System.out.println("\n--- MEMBERS ---");
        System.out.printf("%-8s | %-20s | %s%n", "ID", "Name", "Completed Training");
        System.out.println("-".repeat(70));
        for (Member member : members) {
            System.out.println(member.getSummary());
        }
    }

    /**
     * Registers a new member after checking that the ID is not already taken.
     */
    public String registerMember(String memberId, String name) {
        if (findMember(memberId) != null) {
            return "Error: Member ID '" + memberId + "' is already registered.";
        }
        Member newMember = new Member(memberId, name);
        members.add(newMember);
        dataManager.saveMembers(members);
        return "Member '" + name + "' (ID: " + memberId + ") registered successfully.";
    }

    /**
     * Records a completed training prerequisite for a member.
     * Duplicate entries are silently ignored.
     */
    public String recordTraining(String memberId, String trainingCode) {
        Member member = findMember(memberId);
        if (member == null) {
            return "Error: Member ID '" + memberId + "' not found.";
        }
        boolean added = member.addTraining(trainingCode.toUpperCase());
        if (!added) {
            return "Training '" + trainingCode + "' was already recorded for " + member.getName() + ".";
        }
        dataManager.saveMembers(members);
        return "Training '" + trainingCode + "' recorded for " + member.getName() + ".";
    }

    // ─── CHECKOUT ───────────────────────────────────────────────────────────

    /**
     * Validates and processes a checkout request.
     * Returns a message explaining the result — either success or the specific reason for rejection.
     *
     * Validation order:
     *  1. Member must exist.
     *  2. Tool must exist.
     *  3. Tool must not be under maintenance.
     *  4. Tool must not already be checked out (no active loan).
     *  5. Member must have completed the required training.
     *  6. Loan duration must be positive.
     */
    public String checkoutTool(String memberId, String toolId, int durationDays) {

        // Rule 1: Member must exist
        Member member = findMember(memberId);
        if (member == null) {
            return "Checkout rejected: Member ID '" + memberId + "' not found.";
        }

        // Rule 2: Tool must exist
        Tool tool = findTool(toolId);
        if (tool == null) {
            return "Checkout rejected: Tool ID '" + toolId + "' not found.";
        }

        // Rule 3: Tool must not be under maintenance
        if (tool.isUnderMaintenance()) {
            return "Checkout rejected: '" + tool.getName() + "' is currently under maintenance.";
        }

        // Rule 4: Tool must not already have an active loan
        if (hasActiveLoan(toolId)) {
            return "Checkout rejected: '" + tool.getName() + "' is already checked out.";
        }

        // Rule 5: Member must have the required training
        String requiredTraining = tool.getRequiredTraining();
        if (!member.hasCompletedTraining(requiredTraining)) {
            return "Checkout rejected: " + member.getName()
                    + " has not completed '" + requiredTraining + "' training required for '"
                    + tool.getName() + "'.";
        }

        // Rule 6: Duration must be positive
        if (durationDays <= 0) {
            return "Checkout rejected: Loan duration must be at least 1 day.";
        }

        // All rules passed — create the loan record
        String loanId = generateLoanId();
        LocalDate today = LocalDate.now();
        LocalDate dueDate = today.plusDays(durationDays);

        Loan loan = new Loan(loanId, toolId, memberId, today, dueDate);
        loans.add(loan);
        dataManager.saveLoans(loans);

        return "Checkout approved!\n"
                + "  Loan ID    : " + loanId + "\n"
                + "  Tool       : " + tool.getName() + " (" + toolId + ")\n"
                + "  Member     : " + member.getName() + " (" + memberId + ")\n"
                + "  Checked out: " + today + "\n"
                + "  Due back   : " + dueDate;
    }

    // ─── RETURN ─────────────────────────────────────────────────────────────

    /**
     * Processes the return of a tool identified by its loan ID.
     * Records the return condition and triggers maintenance flagging if needed.
     * Valid conditions: GOOD, DAMAGED, NEEDS_INSPECTION
     */
    public String returnTool(String loanId, String condition, String damageNote) {

        // Normalize input so users can type "good" or "GOOD"
        condition = condition.toUpperCase().trim();

        // Validate the condition value
        if (!condition.equals("GOOD") && !condition.equals("DAMAGED") && !condition.equals("NEEDS_INSPECTION")) {
            return "Error: Condition must be GOOD, DAMAGED, or NEEDS_INSPECTION.";
        }

        // Find the matching active loan
        Loan loan = findActiveLoan(loanId);
        if (loan == null) {
            // Check if this loan ID exists at all (already returned)
            Loan anyLoan = findAnyLoan(loanId);
            if (anyLoan != null) {
                return "Error: Loan '" + loanId + "' has already been returned.";
            }
            return "Error: No active loan found with ID '" + loanId + "'.";
        }

        // Record the return
        loan.recordReturn(LocalDate.now(), condition, damageNote);

        // If the tool came back damaged or needs inspection, flag it for maintenance
        Tool tool = findTool(loan.getToolId());
        String maintenanceNote = "";
        if (condition.equals("DAMAGED") || condition.equals("NEEDS_INSPECTION")) {
            if (tool != null) {
                tool.setUnderMaintenance(true);
                dataManager.saveTools(tools);
                maintenanceNote = "\n  [!] Tool flagged for maintenance due to condition: " + condition;
            }
        }

        dataManager.saveLoans(loans);

        return "Return recorded for Loan ID: " + loanId + "\n"
                + "  Tool     : " + (tool != null ? tool.getName() : loan.getToolId()) + "\n"
                + "  Returned : " + loan.getReturnDate() + "\n"
                + "  Condition: " + condition
                + maintenanceNote;
    }

    // ─── REPORTS ────────────────────────────────────────────────────────────

    /**
     * Shows all loans that are currently active (not yet returned).
     */
    public void showActiveLoans() {
        System.out.println("\n--- ACTIVE LOANS ---");
        boolean found = false;
        for (Loan loan : loans) {
            if (loan.isActive()) {
                System.out.println(loan.getSummary());
                found = true;
            }
        }
        if (!found) {
            System.out.println("No active loans at this time.");
        }
    }

    /**
     * Shows all active loans whose due date has passed today's date.
     */
    public void showOverdueLoans() {
        System.out.println("\n--- OVERDUE LOANS ---");
        boolean found = false;
        for (Loan loan : loans) {
            if (loan.isOverdue()) {
                // Calculate how many days overdue it is
                long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                        loan.getDueDate(), LocalDate.now());
                System.out.println(loan.getSummary() + " [" + daysOverdue + " days overdue]");
                found = true;
            }
        }
        if (!found) {
            System.out.println("No overdue loans.");
        }
    }

    /**
     * Shows all tools currently under maintenance.
     */
    public void showMaintenanceReport() {
        System.out.println("\n--- MAINTENANCE REPORT ---");
        boolean found = false;
        for (Tool tool : tools) {
            if (tool.isUnderMaintenance()) {
                System.out.println("  " + tool.getToolId() + " | " + tool.getName()
                        + " | Required training: " + tool.getRequiredTraining());
                found = true;
            }
        }
        if (!found) {
            System.out.println("No tools are currently under maintenance.");
        }
    }

    /**
     * Shows the complete history of all loans, including returned ones.
     */
    public void showLoanHistory() {
        System.out.println("\n--- LOAN HISTORY ---");
        if (loans.isEmpty()) {
            System.out.println("No loan records found.");
            return;
        }
        for (Loan loan : loans) {
            System.out.println(loan.getSummary());
        }
    }

    // ─── PRIVATE HELPERS ────────────────────────────────────────────────────

    /**
     * Finds a tool by ID. Returns null if not found.
     */
    private Tool findTool(String toolId) {
        for (Tool tool : tools) {
            if (tool.getToolId().equalsIgnoreCase(toolId)) {
                return tool;
            }
        }
        return null;
    }

    /**
     * Finds a member by ID. Returns null if not found.
     */
    private Member findMember(String memberId) {
        for (Member member : members) {
            if (member.getMemberId().equalsIgnoreCase(memberId)) {
                return member;
            }
        }
        return null;
    }

    /**
     * Checks whether a tool has any currently active loan.
     */
    private boolean hasActiveLoan(String toolId) {
        for (Loan loan : loans) {
            if (loan.getToolId().equalsIgnoreCase(toolId) && loan.isActive()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds an active loan by loan ID. Returns null if not found or already returned.
     */
    private Loan findActiveLoan(String loanId) {
        for (Loan loan : loans) {
            if (loan.getLoanId().equalsIgnoreCase(loanId) && loan.isActive()) {
                return loan;
            }
        }
        return null;
    }

    /**
     * Finds any loan by ID, regardless of active/returned status.
     */
    private Loan findAnyLoan(String loanId) {
        for (Loan loan : loans) {
            if (loan.getLoanId().equalsIgnoreCase(loanId)) {
                return loan;
            }
        }
        return null;
    }

    /**
     * Generates the next unique loan ID in the format L001, L002, ...
     */
    private String generateLoanId() {
        String id = String.format("L%03d", loanCounter);
        loanCounter++;
        return id;
    }
}
