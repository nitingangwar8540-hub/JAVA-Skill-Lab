import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

/**
 * Handles reading and writing records to plain-text files.
 * Keeps all file I/O separate from the business rules in WorkshopToolSystem.
 *
 * File locations (relative to where the program is run):
 *   data/tools.txt
 *   data/members.txt
 *   data/loans.txt
 */
public class DataManager {

    private static final String DATA_DIR  = "data";
    private static final String TOOLS_FILE   = DATA_DIR + "/tools.txt";
    private static final String MEMBERS_FILE = DATA_DIR + "/members.txt";
    private static final String LOANS_FILE   = DATA_DIR + "/loans.txt";

    // Creates the data directory if it does not already exist
    public DataManager() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    // ─── TOOLS ──────────────────────────────────────────────────────────────

    /**
     * Loads all tools from file. Returns an empty list if the file does not exist.
     */
    public ArrayList<Tool> loadTools() {
        ArrayList<Tool> tools = new ArrayList<>();
        File file = new File(TOOLS_FILE);
        if (!file.exists()) {
            return tools;  // no file yet, first run
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    tools.add(Tool.fromFileString(line));
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not read tools file. " + e.getMessage());
        }
        return tools;
    }

    /**
     * Saves the full list of tools, overwriting the file completely.
     * Called whenever a tool is added or its maintenance status changes.
     */
    public void saveTools(ArrayList<Tool> tools) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TOOLS_FILE))) {
            for (Tool tool : tools) {
                writer.write(tool.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error: Could not save tools. " + e.getMessage());
        }
    }

    // ─── MEMBERS ────────────────────────────────────────────────────────────

    /**
     * Loads all members from file. Returns an empty list if the file does not exist.
     */
    public ArrayList<Member> loadMembers() {
        ArrayList<Member> members = new ArrayList<>();
        File file = new File(MEMBERS_FILE);
        if (!file.exists()) {
            return members;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    members.add(Member.fromFileString(line));
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not read members file. " + e.getMessage());
        }
        return members;
    }

    /**
     * Saves the full list of members, overwriting the file completely.
     */
    public void saveMembers(ArrayList<Member> members) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MEMBERS_FILE))) {
            for (Member member : members) {
                writer.write(member.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error: Could not save members. " + e.getMessage());
        }
    }

    // ─── LOANS ──────────────────────────────────────────────────────────────

    /**
     * Loads all loans from file. Returns an empty list if the file does not exist.
     */
    public ArrayList<Loan> loadLoans() {
        ArrayList<Loan> loans = new ArrayList<>();
        File file = new File(LOANS_FILE);
        if (!file.exists()) {
            return loans;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    loans.add(Loan.fromFileString(line));
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: Could not read loans file. " + e.getMessage());
        }
        return loans;
    }

    /**
     * Saves the full list of loans, overwriting the file completely.
     * All historical loans (returned and active) are preserved here.
     */
    public void saveLoans(ArrayList<Loan> loans) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOANS_FILE))) {
            for (Loan loan : loans) {
                writer.write(loan.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error: Could not save loans. " + e.getMessage());
        }
    }

    // ─── SAMPLE DATA ────────────────────────────────────────────────────────

    /**
     * Checks whether the data files are missing (first run).
     * Used by WorkshopToolSystem to decide whether to load sample data.
     */
    public boolean isFirstRun() {
        return !new File(TOOLS_FILE).exists()
            && !new File(MEMBERS_FILE).exists()
            && !new File(LOANS_FILE).exists();
    }
}
