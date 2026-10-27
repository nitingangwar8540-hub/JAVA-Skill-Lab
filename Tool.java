import java.util.ArrayList;

/**
 * Represents a physical tool available for checkout in the workshop.
 * Each tool has an ID, a name, a required training code, and a maintenance status.
 */
public class Tool {

    private String toolId;
    private String name;
    private String requiredTraining;  // training code a member must have completed
    private boolean underMaintenance;

    // Constructor: sets up a new tool with all required information
    public Tool(String toolId, String name, String requiredTraining, boolean underMaintenance) {
        this.toolId = toolId;
        this.name = name;
        this.requiredTraining = requiredTraining;
        this.underMaintenance = underMaintenance;
    }

    // --- Getters ---

    public String getToolId() {
        return toolId;
    }

    public String getName() {
        return name;
    }

    public String getRequiredTraining() {
        return requiredTraining;
    }

    public boolean isUnderMaintenance() {
        return underMaintenance;
    }

    // --- Setter ---

    // Only maintenance status can be changed after creation; other fields are fixed
    public void setUnderMaintenance(boolean underMaintenance) {
        this.underMaintenance = underMaintenance;
    }

    /**
     * Checks whether this tool is available for checkout.
     * A tool is available only when it is not under maintenance AND has no active loan.
     * The activeLoans list is passed in so Tool does not need to know about Loan objects.
     */
    public boolean isAvailable(ArrayList<Loan> activeLoans) {
        if (underMaintenance) {
            return false;
        }
        // Check if any active loan already covers this tool
        for (Loan loan : activeLoans) {
            if (loan.getToolId().equals(toolId) && loan.isActive()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a formatted one-line summary of this tool's status.
     */
    public String getSummary(ArrayList<Loan> activeLoans) {
        String availability;
        if (underMaintenance) {
            availability = "MAINTENANCE";
        } else if (isAvailable(activeLoans)) {
            availability = "AVAILABLE";
        } else {
            availability = "CHECKED OUT";
        }
        return String.format("%-8s | %-25s | %-30s | %s",
                toolId, name, requiredTraining, availability);
    }

    /**
     * Converts this tool to a pipe-delimited string for saving to a file.
     * Format: toolId|name|requiredTraining|underMaintenance
     */
    public String toFileString() {
        return toolId + "|" + name + "|" + requiredTraining + "|" + underMaintenance;
    }

    /**
     * Rebuilds a Tool object from a pipe-delimited file line.
     */
    public static Tool fromFileString(String line) {
        String[] parts = line.split("\\|");
        String toolId = parts[0];
        String name = parts[1];
        String requiredTraining = parts[2];
        boolean underMaintenance = Boolean.parseBoolean(parts[3]);
        return new Tool(toolId, name, requiredTraining, underMaintenance);
    }
}
