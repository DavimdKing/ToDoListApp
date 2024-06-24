import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ToDoList {
    private static final String DB_URL = "jdbc:sqlite:todo.db";
    private List<Task> tasks;
    private int nextId;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Error loading SQLite JDBC Driver: " + e.getMessage());
        }
    }

    public ToDoList() {
        tasks = new ArrayList<>();
        nextId = 1;
        createTables();
    }

    private void createTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String createTasksTable = "CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "list_name TEXT NOT NULL," +
                    "description TEXT NOT NULL," +
                    "is_completed BOOLEAN NOT NULL DEFAULT 0)";
            stmt.execute(createTasksTable);

            String createLogsTable = "CREATE TABLE IF NOT EXISTS logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "timestamp TEXT NOT NULL," +
                    "action TEXT NOT NULL," +
                    "description TEXT)";
            stmt.execute(createLogsTable);
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }

    public void addTask(String listName, String description) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO tasks (list_name, description) VALUES (?, ?)")) {
            pstmt.setString(1, listName);
            pstmt.setString(2, description);
            pstmt.executeUpdate();
            logAction("Add", "Added task: " + description + " to list: " + listName);
            System.out.println("Task added: " + description);
        } catch (SQLException e) {
            System.err.println("Error adding task: " + e.getMessage());
        }
    }

    public List<Task> getTasks(String listName) {
        tasks.clear();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM tasks WHERE list_name = ?")) {
            pstmt.setString(1, listName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Task task = new Task(rs.getInt("id"), rs.getString("description"));
                task.setCompleted(rs.getBoolean("is_completed"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving tasks: " + e.getMessage());
        }
        return tasks;
    }

    public void deleteTask(String listName, String description) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM tasks WHERE list_name = ? AND description = ?")) {
            pstmt.setString(1, listName);
            pstmt.setString(2, description);
            pstmt.executeUpdate();
            logAction("Delete", "Deleted task: " + description + " from list: " + listName);
            System.out.println("Task deleted: " + description);
        } catch (SQLException e) {
            System.err.println("Error deleting task: " + e.getMessage());
        }
    }

    public void updateTaskDescription(String listName, Task task, String newDescription) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("UPDATE tasks SET description = ?, is_completed = ? WHERE id = ?")) {
            pstmt.setString(1, newDescription);
            pstmt.setBoolean(2, task.isCompleted());
            pstmt.setInt(3, task.getId());
            pstmt.executeUpdate();
            task.setDescription(newDescription);
            logAction("Update", "Updated task ID " + task.getId() + " in list: " + listName + " to description: " + newDescription);
        } catch (SQLException e) {
            System.err.println("Error updating task: " + e.getMessage());
        }
    }

    private void logAction(String action, String description) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO logs (timestamp, action, description) VALUES (?, ?, ?)")) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            pstmt.setString(1, timestamp);
            pstmt.setString(2, action);
            pstmt.setString(3, description);
            pstmt.executeUpdate();
            System.out.println("Logged action: " + action + " - " + description);
        } catch (SQLException e) {
            System.err.println("Error logging action: " + e.getMessage());
        }
    }

    public List<String> getLogEntries() {
        List<String> logs = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM logs");
            while (rs.next()) {
                String logEntry = rs.getString("timestamp") + " - " + rs.getString("action") + ": " + rs.getString("description");
                logs.add(logEntry);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving logs: " + e.getMessage());
        }
        return logs;
    }

    public void saveTasksToDatabase(String listName) {
        // Saving is automatically done through SQL operations; nothing specific needed here
    }

    public void loadTasksFromDatabase(String listName) {
        getTasks(listName);
    }
}
