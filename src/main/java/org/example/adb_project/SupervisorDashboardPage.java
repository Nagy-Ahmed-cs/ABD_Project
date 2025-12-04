package org.example.adb_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Case;
import models.Client;
import models.Employee;
import models.Action;
import repos.CaseRepo;
import repos.ClientRepository;
import repos.EmployeeRepository;
import repos.ActionRepo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class SupervisorDashboardPage {

    private final CaseRepo caseRepository = new CaseRepo();
    private final ClientRepository clientRepository = new ClientRepository();
    private final EmployeeRepository employeeRepository = new EmployeeRepository();
    private final ActionRepo actionRepository = new ActionRepo();

    private VBox mainBox = new VBox(15); // Main content area for dynamic tables

    public void open(Stage stage, Employee supervisor) {

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // ===== TOP BAR =====
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button profileBtn = new Button("Profile");
        Button exitBtn = new Button("Exit");
        Button clientsBtn = new Button("Clients");
        Button employeesBtn = new Button("Employees");
        Button casesBtn = new Button("Cases");

        profileBtn.getStyleClass().add("dashboard-button");
        exitBtn.getStyleClass().add("exit-button");
        clientsBtn.getStyleClass().add("dashboard-button");
        employeesBtn.getStyleClass().add("dashboard-button");
        casesBtn.getStyleClass().add("dashboard-button");
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("dashboard-button");

        backBtn.setOnAction(e -> {
            EmployeePage employeePage=new EmployeePage();
            employeePage.open(stage);
        });

        profileBtn.setOnAction(e -> showProfilePopup(stage, supervisor));
        exitBtn.setOnAction(e -> stage.close());

        topBar.getChildren().addAll(profileBtn, clientsBtn, employeesBtn, casesBtn,backBtn, exitBtn);
        root.setTop(topBar);

        mainBox.setPadding(new Insets(20));
        root.setCenter(mainBox);

        // ===== BUTTON ACTIONS =====
        clientsBtn.setOnAction(e -> showClientsView());
        employeesBtn.setOnAction(e -> showEmployeesView());
        casesBtn.setOnAction(e -> showCasesView());

        // ===== INITIAL VIEW =====
        showClientsView();

        Scene scene = new Scene(root, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Supervisor Dashboard");
        stage.show();
    }

    // ========================================
    // Clients View
    // ========================================
    private void showClientsView() {
        mainBox.getChildren().clear();

        Label title = new Label("Clients Overview");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Search by name or email
        TextField searchField = new TextField();
        searchField.setPromptText("Search by Name or Email");

        TableView<Client> clientTable = new TableView<>();
        TableColumn<Client, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Client, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<Client, String> casesCol = new TableColumn<>("Cases Taken");

        clientTable.getColumns().addAll(nameCol, emailCol, casesCol);

        // Load clients
        ObservableList<Client> clients = FXCollections.observableArrayList(clientRepository.findAll());
        clientTable.setItems(clients);

        // Populate cases for each client
        casesCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                Client client = (Client) getTableRow().getItem();
                List<Case> clientCases = caseRepository.findByClientId(client.getId());
                setText(clientCases.stream()
                        .map(Case::getTitle)
                        .collect(Collectors.joining(", ")));
            }
        });

        // Filter dynamically
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal.toLowerCase();
            clientTable.setItems(FXCollections.observableArrayList(
                    clients.stream()
                            .filter(c -> c.getName().toLowerCase().contains(filter) ||
                                    c.getEmail().toLowerCase().contains(filter))
                            .collect(Collectors.toList())
            ));
        });

        mainBox.getChildren().addAll(title, searchField, clientTable);
    }

    // ========================================
    // Employees View
    // ========================================
    private void showEmployeesView() {
        mainBox.getChildren().clear();

        Label title = new Label("Employees Overview");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search by Department");

        TableView<Employee> empTable = new TableView<>();
        TableColumn<Employee, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Employee, String> deptCol = new TableColumn<>("Department");
        deptCol.setCellValueFactory(new PropertyValueFactory<>("department"));
        TableColumn<Employee, String> actionsCol = new TableColumn<>("Actions Taken");
        TableColumn<Employee, String> casesCol = new TableColumn<>("Cases Assigned");

        empTable.getColumns().addAll(nameCol, deptCol, casesCol, actionsCol);

        ObservableList<Employee> employees = FXCollections.observableArrayList(employeeRepository.findAll());
        empTable.setItems(employees);

        // Cases per employee
        casesCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                Employee emp = (Employee) getTableRow().getItem();
                List<Case> empCases = caseRepository.getEmployeeCases(emp.getId());
                setText(empCases.stream().map(Case::getTitle).collect(Collectors.joining(", ")));
            }
        });

        // Actions per employee
        actionsCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                Employee emp = (Employee) getTableRow().getItem();
                List<Action> actions = actionRepository.findByEmployeeId(emp.getId());
                setText(actions.stream()
                        .map(a -> a.getType() + ": " + a.getDescription())
                        .collect(Collectors.joining("; ")));
            }
        });

        // Filter dynamically by department
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String filter = newVal.toLowerCase();
            empTable.setItems(FXCollections.observableArrayList(
                    employees.stream()
                            .filter(e -> e.getDepartment().toLowerCase().contains(filter))
                            .collect(Collectors.toList())
            ));
        });

        mainBox.getChildren().addAll(title, searchField, empTable);
    }

    // ========================================
    // Cases View
    // ========================================
    private void showCasesView() {
        mainBox.getChildren().clear();

        Label title = new Label("All Cases");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        TextField actionFilter = new TextField();
        actionFilter.setPromptText("Action Type");

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All", "Not Taken", "In Progress", "Under Review", "Open", "Resolved");
        statusFilter.setValue("All");

        ComboBox<String> priorityFilter = new ComboBox<>();
        priorityFilter.getItems().addAll("All", "High", "Medium", "Low");
        priorityFilter.setValue("All");

        DatePicker fromDate = new DatePicker();
        fromDate.setPromptText("From Date");

        DatePicker toDate = new DatePicker();
        toDate.setPromptText("To Date");

        filterBox.getChildren().addAll(new Label("Status:"), statusFilter,
                new Label("Priority:"), priorityFilter,
                new Label("Action Type:"), actionFilter,
                new Label("From:"), fromDate,
                new Label("To:"), toDate);

        TableView<Case> caseTable = new TableView<>();
        TableColumn<Case, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Case, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<Case, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));

        TableColumn<Case, String> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) {
                    setText(null);
                    return;
                }
                Case c = (Case) getTableRow().getItem();
                List<Action> actions = actionRepository.findByCaseId(c.getId());
                setText(actions.stream()
                        .map(a -> a.getType() + ": " + a.getDescription() + " (" +
                                DateTimeFormatter.ofPattern("yyyy-MM-dd").format(a.getTakenAt().atZone(ZoneId.systemDefault())) + ")")
                        .collect(Collectors.joining("; ")));
            }
        });

        ObservableList<Case> cases = FXCollections.observableArrayList(caseRepository.findAll());
        caseTable.setItems(cases);

        caseTable.getColumns().addAll(titleCol, statusCol, priorityCol, actionsCol);

        // Filter logic
        Runnable filterCases = () -> {
            String stFilter = statusFilter.getValue();
            String prFilter = priorityFilter.getValue();
            String actFilter = actionFilter.getText().toLowerCase();
            LocalDate from = fromDate.getValue();
            LocalDate to = toDate.getValue();

            List<Case> filtered = cases.stream().filter(c -> {
                boolean st = stFilter.equals("All") || c.getStatus().equalsIgnoreCase(stFilter);
                boolean pr = prFilter.equals("All") || c.getPriority().equalsIgnoreCase(prFilter);
                boolean act = actFilter.isEmpty() || actionRepository.findByCaseId(c.getId()).stream()
                        .anyMatch(a -> a.getType().toLowerCase().contains(actFilter));

                boolean date = true;
                if (from != null || to != null) {
                    Instant caseTime = c.getCreateAt();
                    LocalDate caseDate = caseTime.atZone(ZoneId.systemDefault()).toLocalDate();
                    if (from != null && caseDate.isBefore(from)) date = false;
                    if (to != null && caseDate.isAfter(to)) date = false;
                }
                return st && pr && act && date;
            }).collect(Collectors.toList());

            caseTable.setItems(FXCollections.observableArrayList(filtered));
        };

        statusFilter.setOnAction(e -> filterCases.run());
        priorityFilter.setOnAction(e -> filterCases.run());
        actionFilter.textProperty().addListener((obs, oldV, newV) -> filterCases.run());
        fromDate.setOnAction(e -> filterCases.run());
        toDate.setOnAction(e -> filterCases.run());

        mainBox.getChildren().addAll(title, filterBox, caseTable);
    }

    private void showProfilePopup(Stage stage, Employee supervisor) {
        // Implement profile popup
    }
}
