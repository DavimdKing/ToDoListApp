import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

public class MainApp extends Application {

    private ToDoList toDoList = new ToDoList();
    private ListView<String> listView = new ListView<>();
    private TextField taskInput = new TextField();
    private ComboBox<String> listSelector = new ComboBox<>();
    private ListView<String> logView = new ListView<>();
    private VBox logSection = new VBox(10);

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("To-Do List Application");

        taskInput.setPromptText("Enter new task");
        taskInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                addTask(taskInput.getText());
            }
        });

        Button addButton = new Button("Add Task");
        addButton.setOnAction(e -> addTask(taskInput.getText()));

        Button deleteButton = new Button("Delete Selected Task");
        deleteButton.setOnAction(e -> deleteSelectedTask());

        Button completeButton = new Button("Mark as Completed");
        completeButton.setOnAction(e -> markTaskAsCompleted());

        listSelector.getItems().addAll("Daily", "Work", "Travel");
        listSelector.setValue("Daily"); // Set default value
        listSelector.setOnAction(e -> loadSelectedList());

        Button showLogsButton = new Button("Show Logs");
        showLogsButton.setOnAction(e -> toggleLogsVisibility());

        logSection.getChildren().add(logView);
        logSection.setVisible(false); // Initially hide the log section

        VBox layout = new VBox(10);
        layout.getChildren().addAll(listSelector, taskInput, addButton, listView, completeButton, deleteButton, showLogsButton, logSection);

        Scene scene = new Scene(layout, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load tasks from the default list when application starts
        loadSelectedList();

        // Save tasks to database when application is about to exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> toDoList.saveTasksToDatabase(listSelector.getValue())));
    }

    private void addTask(String description) {
        if (!description.isEmpty()) {
            toDoList.addTask(listSelector.getValue(), description);
            listView.getItems().add(description);
            taskInput.clear(); // Clear the input field
        }
    }

    private void deleteSelectedTask() {
        String selectedTask = listView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            listView.getItems().remove(selectedTask);
            toDoList.deleteTask(listSelector.getValue(), selectedTask);
        }
    }

    private void markTaskAsCompleted() {
        String selectedTask = listView.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            for (Task task : toDoList.getTasks(listSelector.getValue())) {
                if (task.getDescription().equals(selectedTask)) {
                    task.setCompleted(true);
                    toDoList.updateTaskDescription(listSelector.getValue(), task, selectedTask + " [Completed]");
                    break;
                }
            }
            listView.getItems().set(listView.getSelectionModel().getSelectedIndex(),
                    selectedTask + " [Completed]");
        }
    }

    private void loadSelectedList() {
        listView.getItems().clear();
        toDoList.loadTasksFromDatabase(listSelector.getValue());
        for (Task task : toDoList.getTasks(listSelector.getValue())) {
            listView.getItems().add(task.getDescription());
        }
    }

    private void toggleLogsVisibility() {
        if (logSection.isVisible()) {
            logSection.setVisible(false);
            logView.getItems().clear(); // Optionally clear the logs when hiding
        } else {
            logSection.setVisible(true);
            List<String> logs = toDoList.getLogEntries();
            logView.getItems().setAll(logs); // Load logs when showing
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
