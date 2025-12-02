package org.example.adb_project;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        // Create buttons
        Button clientBtn = new Button("Client");
        Button employeeBtn = new Button("Employee");

        // Styling
        clientBtn.getStyleClass().add("menu-button");
        employeeBtn.getStyleClass().add("menu-button");

        // Actions
        clientBtn.setOnAction(e -> new ClientPage().open(stage));
        employeeBtn.setOnAction(e -> new EmployeePage().open(stage));

        VBox root = new VBox(20);
        root.getChildren().addAll(clientBtn, employeeBtn);
        root.setStyle("-fx-alignment: center;");

        Scene scene = new Scene(root, 400, 300);

        // Load CSS
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("Main Menu");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}