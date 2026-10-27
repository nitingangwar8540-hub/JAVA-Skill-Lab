import java.util.ArrayList;

/**
 * Represents a registered workshop member.
 * Stores the member's ID, name, and list of completed training prerequisites.
 */
public class Member {

    private String memberId;
    private String name;
    // Each entry is a training code the member has completed (e.g. "ELECTRICAL_SAFETY")
    private ArrayList<String> completedTraining;

    // Constructor: creates a new member with no training completed yet
    public Member(String memberId, String name) {
        this.memberId = memberId;
        this.name = name;
        this.completedTraining = new ArrayList<>();
    }

    // Constructor used when loading from file (training list already known)
    public Member(String memberId, String name, ArrayList<String> completedTraining) {
        this.memberId = memberId;
        this.name = name;
        this.completedTraining = completedTraining;
    }

    // --- Getters ---

    public String getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getCompletedTraining() {
        return completedTraining;
    }

    /**
     * Returns true if this member has completed the specified training code.
     * Used during checkout validation to enforce the safety gate.
     */
    public boolean hasCompletedTraining(String trainingCode) {
        for (String completed : completedTraining) {
            if (completed.equalsIgnoreCase(trainingCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a training code to this member's record, but only if not already present.
     * Returns true if the training was newly added, false if it was already recorded.
     */
    public boolean addTraining(String trainingCode) {
        if (hasCompletedTraining(trainingCode)) {
            return false;  // already recorded, skip
        }
        completedTraining.add(trainingCode);
        return true;
    }

    /**
     * Returns a formatted one-line summary of this member.
     */
    public String getSummary() {
        String training = completedTraining.isEmpty() ? "None" : String.join(", ", completedTraining);
        return String.format("%-8s | %-20s | %s", memberId, name, training);
    }

    /**
     * Converts this member to a pipe-delimited string for saving to a file.
     * Training codes are joined by commas inside the pipe field.
     * Format: memberId|name|TRAINING1,TRAINING2,...
     */
    public String toFileString() {
        String trainingList = String.join(",", completedTraining);
        return memberId + "|" + name + "|" + trainingList;
    }

    /**
     * Rebuilds a Member object from a pipe-delimited file line.
     */
    public static Member fromFileString(String line) {
        String[] parts = line.split("\\|", -1);  // -1 keeps empty trailing fields
        String memberId = parts[0];
        String name = parts[1];
        ArrayList<String> training = new ArrayList<>();
        if (parts.length > 2 && !parts[2].isEmpty()) {
            String[] codes = parts[2].split(",");
            for (String code : codes) {
                training.add(code.trim());
            }
        }
        return new Member(memberId, name, training);
    }
}
