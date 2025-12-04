package org.example.adb_project;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Case;
import models.Employee;
import org.bson.types.ObjectId;
import repos.EmployeeRepository;

import java.util.ArrayList;

public class EmployeePage {

    private final EmployeeRepository employeeRepository = new EmployeeRepository();

    public void open(Stage stage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-alignment: center; -fx-background-color: #1e1e1e;");

        Label title = new Label("Employee Page");
        title.getStyleClass().add("title");

        Button signUpBtn = new Button("Sign Up");
        Button logInBtn = new Button("Log In");
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("dashboard-button");

        backBtn.setOnAction(e -> {
            HelloApplication helloApplication=new HelloApplication();
            helloApplication.start(stage);
        });

        signUpBtn.getStyleClass().add("menu-button");
        logInBtn.getStyleClass().add("menu-button");

        HBox buttonBox = new HBox(20, signUpBtn, logInBtn,backBtn);
        buttonBox.setStyle("-fx-alignment: center;");

        root.getChildren().addAll(title, buttonBox);

        Scene scene = new Scene(root, 400, 400);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Employee Page");
        stage.show();

        // ===== Actions =====
        signUpBtn.setOnAction(e -> showSignUpForm(stage));
        logInBtn.setOnAction(e -> showLogInForm(stage));
    }

    // ===== Log In Form =====
    private void showLogInForm(Stage stage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-alignment: center;");

        Label lbl = new Label("Employee Log In");
        lbl.getStyleClass().add("title");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter email");

        Button loginBtn = new Button("Log In");
        loginBtn.getStyleClass().add("menu-button");
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("dashboard-button");

        backBtn.setOnAction(e -> {
            EmployeePage employeePage=new EmployeePage();
            employeePage.open(stage);
        });

        root.getChildren().addAll(lbl, emailField, loginBtn,backBtn);

        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (!validateEmail(email)) return;

            Employee emp = employeeRepository.findByEmail(email);
            if (emp != null) {
               // Alert alert = new Alert(Alert.AlertType.INFORMATION, "Login Successful!");
                //alert.showAndWait();

                // Open different dashboards based on position
                if ("Employee".equalsIgnoreCase(emp.getPosition())) {
                    EmployeeDashboardPage dashboard = new EmployeeDashboardPage();
                    dashboard.open(stage, emp); // pass the same stage
                } else if ("Supervisor".equalsIgnoreCase(emp.getPosition())) {
                    SupervisorDashboardPage dashboard = new SupervisorDashboardPage();
                    dashboard.open(stage, emp); // pass the same stage
                }

            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Email not found!");
                alert.show();
            }
        });


        stage.getScene().setRoot(root);
    }

    // ===== Sign Up Form =====
    private void showSignUpForm(Stage stage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-alignment: center;");

        Label lbl = new Label("Employee Sign Up");
        lbl.getStyleClass().add("title");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField departmentField = new TextField();
        departmentField.setPromptText("Department");

        // Position: Radio buttons
        ToggleGroup positionGroup = new ToggleGroup();
        RadioButton empBtn = new RadioButton("Employee");
        empBtn.setStyle("-fx-text-fill: white;");
        empBtn.setToggleGroup(positionGroup);
        empBtn.setSelected(true);

        RadioButton supBtn = new RadioButton("Supervisor");
        supBtn.setStyle("-fx-text-fill: white;");
        supBtn.setToggleGroup(positionGroup);

        HBox positionBox = new HBox(20, empBtn, supBtn);

        Button signUpBtn = new Button("Sign Up");
        signUpBtn.getStyleClass().add("menu-button");
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("dashboard-button");

        backBtn.setOnAction(e -> {
            EmployeePage employeePage=new EmployeePage();
            employeePage.open(stage); // pass the same stage
        });

        root.getChildren().addAll(lbl, nameField, emailField, departmentField, positionBox, signUpBtn,backBtn);

        signUpBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String department = departmentField.getText().trim();
            RadioButton selectedPos = (RadioButton) positionGroup.getSelectedToggle();
            String position = selectedPos.getText();

            if (!validateEmployeeInput(name, email, department)) return;
            if (employeeRepository.findByEmail(email)!= null) {
                showError("This email is already in use!");
                return ;
            }

            Employee emp = new Employee(name, email, position,new ArrayList<ObjectId>(), department);
            employeeRepository.save(emp);

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Employee created successfully!");
            alert.show();

            showLogInForm(stage);
        });

        stage.getScene().setRoot(root);
    }

    // ===== Validation =====
    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            showError("Email cannot be empty!");
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showError("Invalid email format!");
            return false;
        }


        return true;
    }

    private boolean validateEmployeeInput(String name, String email, String department) {
        if (name.isEmpty() || email.isEmpty() || department.isEmpty()) {
            showError("All fields are required!");
            return false;
        }
        if (!validateEmail(email)) return false;

        if (department.length() < 2) {
            showError("Department name too short!");
            return false;
        }
        return true;
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
