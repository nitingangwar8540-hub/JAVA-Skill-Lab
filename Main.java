import java.util.Scanner;

/**
 * Entry point of the Workshop Tool Checkout application.
 * Displays the menu, reads the user's input, and calls the matching method
 * in WorkshopToolSystem. All business logic stays in WorkshopToolSystem.
 */
public class Main {

    public static void main(String[] args) {

        WorkshopToolSystem system = new WorkshopToolSystem();
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("\nWelcome to the Workshop Tool Checkout System.");

        while (running) {
            printMenu();

            int choice = readIntInput(scanner);

            switch (choice) {
                case 1:
                    system.listTools();
                    break;

                case 2:
                    system.listMembers();
                    break;

                case 3:
                    addToolInteraction(system, scanner);
                    break;

                case 4:
                    registerMemberInteraction(system, scanner);
                    break;

                case 5:
                    recordTrainingInteraction(system, scanner);
                    break;

                case 6:
                    checkoutInteraction(system, scanner);
                    break;

                case 7:
                    returnInteraction(system, scanner);
                    break;

                case 8:
                    system.showActiveLoans();
                    break;

                case 9:
                    system.showOverdueLoans();
                    break;

                case 10:
                    system.showMaintenanceReport();
                    break;

                case 11:
                    system.showLoanHistory();
                    break;

                case 0:
                    System.out.println("Records saved. Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice. Please enter a number from the menu.");
            }
        }

        scanner.close();
    }

    // ─── MENU DISPLAY ───────────────────────────────────────────────────────

    private static void printMenu() {
        System.out.println("\n============================================");
        System.out.println(" WORKSHOP TOOL CHECKOUT WITH SAFETY GATE");
        System.out.println("============================================");
        System.out.println(" 1.  View tools");
        System.out.println(" 2.  View members");
        System.out.println(" 3.  Add a tool");
        System.out.println(" 4.  Register a member");
        System.out.println(" 5.  Record completed training");
        System.out.println(" 6.  Check out a tool");
        System.out.println(" 7.  Return a tool");
        System.out.println(" 8.  View active loans");
        System.out.println(" 9.  View overdue loans");
        System.out.println("10.  View maintenance report");
        System.out.println("11.  View loan history");
        System.out.println(" 0.  Save and exit");
        System.out.println("============================================");
        System.out.print("Enter your choice: ");
    }

    // ─── INTERACTION METHODS ────────────────────────────────────────────────

    private static void addToolInteraction(WorkshopToolSystem system, Scanner scanner) {
        System.out.print("Enter Tool ID       : ");
        String toolId = scanner.nextLine().trim();

        System.out.print("Enter Tool Name     : ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Required Training code (e.g. ELECTRICAL_SAFETY): ");
        String training = scanner.nextLine().trim().toUpperCase();

        if (toolId.isEmpty() || name.isEmpty() || training.isEmpty()) {
            System.out.println("Error: All fields are required. Tool not added.");
            return;
        }

        System.out.println(system.addTool(toolId, name, training));
    }

    private static void registerMemberInteraction(WorkshopToolSystem system, Scanner scanner) {
        System.out.print("Enter Member ID  : ");
        String memberId = scanner.nextLine().trim();

        System.out.print("Enter Member Name: ");
        String name = scanner.nextLine().trim();

        if (memberId.isEmpty() || name.isEmpty()) {
            System.out.println("Error: ID and name are both required. Member not registered.");
            return;
        }

        System.out.println(system.registerMember(memberId, name));
    }

    private static void recordTrainingInteraction(WorkshopToolSystem system, Scanner scanner) {
        System.out.print("Enter Member ID      : ");
        String memberId = scanner.nextLine().trim();

        System.out.print("Enter Training code  : ");
        String training = scanner.nextLine().trim();

        if (memberId.isEmpty() || training.isEmpty()) {
            System.out.println("Error: Member ID and training code are required.");
            return;
        }

        System.out.println(system.recordTraining(memberId, training));
    }

    private static void checkoutInteraction(WorkshopToolSystem system, Scanner scanner) {
        System.out.print("Enter Member ID      : ");
        String memberId = scanner.nextLine().trim();

        System.out.print("Enter Tool ID        : ");
        String toolId = scanner.nextLine().trim();

        System.out.print("Enter loan duration (days): ");
        int duration = readIntInput(scanner);

        if (memberId.isEmpty() || toolId.isEmpty()) {
            System.out.println("Error: Member ID and Tool ID are required.");
            return;
        }

        System.out.println(system.checkoutTool(memberId, toolId, duration));
    }

    private static void returnInteraction(WorkshopToolSystem system, Scanner scanner) {
        System.out.print("Enter Loan ID               : ");
        String loanId = scanner.nextLine().trim();

        System.out.println("Return condition options    : GOOD | DAMAGED | NEEDS_INSPECTION");
        System.out.print("Enter condition             : ");
        String condition = scanner.nextLine().trim();

        String damageNote = "";
        if (condition.equalsIgnoreCase("DAMAGED") || condition.equalsIgnoreCase("NEEDS_INSPECTION")) {
            System.out.print("Enter damage note (or press Enter to skip): ");
            damageNote = scanner.nextLine().trim();
        } else {
            scanner.nextLine(); // consume leftover newline if needed — safe no-op here
        }

        if (loanId.isEmpty()) {
            System.out.println("Error: Loan ID is required.");
            return;
        }

        System.out.println(system.returnTool(loanId, condition, damageNote));
    }

    // ─── INPUT UTILITIES ────────────────────────────────────────────────────

    /**
     * Reads an integer from the terminal.
     * If the user types something that is not a number, returns -1 and shows an error.
     * try-catch prevents the program from crashing on bad input.
     */
    private static int readIntInput(Scanner scanner) {
        try {
            String line = scanner.nextLine().trim();
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input: please enter a whole number.");
            return -1;
        }
    }
}
