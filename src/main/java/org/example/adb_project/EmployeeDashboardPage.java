package org.example.adb_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Case;
import models.Employee;
import models.Action;
import repos.CaseRepo;
import repos.EmployeeRepository;
import repos.ActionRepo;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class EmployeeDashboardPage {

    private final CaseRepo caseRepository = new CaseRepo();
    private final ActionRepo actionRepository = new ActionRepo();
    private final EmployeeRepository employeeRepository = new EmployeeRepository();

    private final ObservableList<String> PRIORITIES =
            FXCollections.observableArrayList("High", "Medium", "Low");

    private final ObservableList<String> STATUSES =
            FXCollections.observableArrayList("Not Taken", "In Progress", "Under Review", "Open", "Resolved");

    private final DateTimeFormatter ACTION_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void open(Stage stage, Employee employee) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(16));
        root.getStyleClass().add("dashboard-root"); // use css for dark theme

        // ===== TOP BAR =====
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(12);
        topBar.setStyle("-fx-alignment: center-right; -fx-padding: 12 6;");

        Button profileBtn = new Button("Profile");
        Button exitBtn = new Button("Exit");
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("dashboard-button");

        backBtn.setOnAction(e -> {
            EmployeePage employeePage=new EmployeePage();
            employeePage.open(stage);
        });

        profileBtn.getStyleClass().addAll("btn", "btn-primary");
        exitBtn.getStyleClass().addAll("btn", "btn-danger");

        profileBtn.setOnAction(e -> showProfilePopup(stage, employee));
        exitBtn.setOnAction(e -> stage.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label title = new Label("Employee Dashboard");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold;");
        title.getStyleClass().add("page-title");

        topBar.getChildren().addAll(title, spacer, profileBtn,backBtn, exitBtn);
        root.setTop(topBar);

        // ===== TABLES & DATA =====
        VBox centerBox = new VBox(12);

        TableView<Case> myCasesTable = createCaseTable(employee);
        TableView<Case> availableCasesTable = createCaseTable(employee);

        ObservableList<Case> myCasesData =
                FXCollections.observableArrayList(caseRepository.getEmployeeCases(employee.getId()));
        ObservableList<Case> availableCasesData =
                FXCollections.observableArrayList(
                        caseRepository.findAll().stream()
                                .filter(c -> !c.getStatus().equalsIgnoreCase("Resolved") &&
                                        (c.getEmployeeIds() == null ||
                                                !c.getEmployeeIds().contains(employee.getId())))
                                .toList()
                );

        myCasesTable.setItems(myCasesData);
        availableCasesTable.setItems(availableCasesData);

        centerBox.getChildren().add(myCasesTable);
        root.setCenter(centerBox);

        // ===== BOTTOM CONTROLS =====
        HBox bottom = new HBox(12);
        bottom.setPadding(new Insets(10));

        ComboBox<String> tableSelector = new ComboBox<>();
        tableSelector.getItems().addAll("My Cases", "Available Cases");
        tableSelector.setValue("My Cases");
        tableSelector.getStyleClass().add("combo");

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("All");
        statusFilter.getItems().addAll(STATUSES);
        statusFilter.setValue("All");
        statusFilter.getStyleClass().add("combo");

        ComboBox<String> priorityFilter = new ComboBox<>();
        priorityFilter.getItems().addAll("All");
        priorityFilter.getItems().addAll(PRIORITIES);
        priorityFilter.setValue("All");
        priorityFilter.getStyleClass().add("combo");

        Button refreshBtn = new Button("Refresh");
        refreshBtn.getStyleClass().addAll("btn", "btn-outline");
        refreshBtn.setOnAction(e -> refreshTables(employee, myCasesTable, availableCasesTable));

        bottom.getChildren().addAll(
                new Label("Table:"), tableSelector,
                new Label("Status:"), statusFilter,
                new Label("Priority:"), priorityFilter,
                refreshBtn
        );
        root.setBottom(bottom);

        // ===== TABLE SWITCH =====
        tableSelector.setOnAction(e -> {
            centerBox.getChildren().clear();
            if (tableSelector.getValue().equals("My Cases")) {
                refreshTables(employee, myCasesTable, availableCasesTable);
                centerBox.getChildren().add(myCasesTable);

            } else {
                refreshTables(employee, myCasesTable, availableCasesTable);
                centerBox.getChildren().add(availableCasesTable);
            }
            // apply current filters
            applyFilters(tableSelector, statusFilter, priorityFilter, myCasesData, availableCasesData, myCasesTable, availableCasesTable);
        });

        // ===== FILTER HANDLERS =====
        Runnable filterTables = () -> applyFilters(tableSelector, statusFilter, priorityFilter, myCasesData, availableCasesData, myCasesTable, availableCasesTable);
        statusFilter.setOnAction(e -> filterTables.run());
        priorityFilter.setOnAction(e -> filterTables.run());

        // ===== AVAILABLE CASES: take case with action type + description =====
        availableCasesTable.setRowFactory(tv -> {
            TableRow<Case> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Case selectedCase = row.getItem();

                    // ask for action type (text)
                    TextInputDialog typeDialog = new TextInputDialog();
                    typeDialog.setTitle("Action Type");
                    typeDialog.setHeaderText("Enter Action Type (e.g. CALL_CLIENT):");
                    Optional<String> actionType = typeDialog.showAndWait();
                    if (actionType.isEmpty() || actionType.get().trim().isEmpty()) return;

                    // ask for description
                    TextInputDialog descDialog = new TextInputDialog();
                    descDialog.setTitle("Action Description");
                    descDialog.setHeaderText("Enter Action Description:");
                    Optional<String> description = descDialog.showAndWait();
                    if (description.isEmpty() || description.get().trim().isEmpty()) return;

                    // assign employee if not assigned
                    if (selectedCase.getEmployeeIds() == null) selectedCase.setEmployeeIds(new ArrayList<>());
                    if (!selectedCase.getEmployeeIds().contains(employee.getId())) {
                        selectedCase.getEmployeeIds().add(employee.getId());
                    }
                    selectedCase.setStatus("In Progress");
                    selectedCase.setUpdateAt(Instant.now());
                    caseRepository.updateCase(selectedCase);

                    // save action
                    Action action = new Action(
                            actionType.get().trim(),
                            description.get().trim(),
                            Instant.now(),
                            selectedCase.getId()
                    );
                    actionRepository.save(action);

                    refreshTables(employee, myCasesTable, availableCasesTable);
                }
            });
            return row;
        });

        // editable columns for myCases
        myCasesTable.setEditable(true);
        statusColEditable(myCasesTable,availableCasesTable ,employee);
        priorityColEditable(myCasesTable,availableCasesTable ,employee);

        // Style sheet (dark theme)
        Scene scene = new Scene(root, 1100, 600);
        // ensure you have /style.css in resources (dark theme). If not, inline styles still present.
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Employee Dashboard");
        stage.show();
    }

    // ========================= helper to apply filters =========================
    private void applyFilters(ComboBox<String> tableSelector,
                              ComboBox<String> statusFilter,
                              ComboBox<String> priorityFilter,
                              ObservableList<Case> myCasesData,
                              ObservableList<Case> availableCasesData,
                              TableView<Case> myCasesTable,
                              TableView<Case> availableCasesTable) {

        String selectedTable = tableSelector.getValue();
        String statusVal = statusFilter.getValue();
        String priorityVal = priorityFilter.getValue();

        ObservableList<Case> source = selectedTable.equals("My Cases") ? myCasesData : availableCasesData;
        TableView<Case> targetTable = selectedTable.equals("My Cases") ? myCasesTable : availableCasesTable;

        List<Case> filtered = source.stream()
                .filter(c -> (statusVal.equals("All") || (c.getStatus() != null && c.getStatus().equalsIgnoreCase(statusVal))))
                .filter(c -> (priorityVal.equals("All") || (c.getPriority() != null && c.getPriority().equalsIgnoreCase(priorityVal))))
                .toList();

        targetTable.setItems(FXCollections.observableArrayList(filtered));
    }

    // ========================= create table with action dropdown + add action button =========================
    private TableView<Case> createCaseTable(Employee employee) {
        TableView<Case> table = new TableView<>();
        table.getStyleClass().add("case-table");

        // Title
        TableColumn<Case, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setMinWidth(100);
        titleCol.setMaxWidth(160);


        // Status (text color only)
        TableColumn<Case, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setMinWidth(100);
        statusCol.setMaxWidth(160);

        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(status);
                setStyle("-fx-font-weight: bold; -fx-alignment: center;");
                switch (status) {
                    case "High" -> setStyle("-fx-text-fill: red; -fx-font-weight:bold; -fx-alignment:center;");
                    case "Medium" -> setStyle("-fx-text-fill: orange; -fx-font-weight:bold; -fx-alignment:center;");
                    case "Low" -> setStyle("-fx-text-fill: green; -fx-font-weight:bold; -fx-alignment:center;");
                    case "Not Taken" -> setStyle("-fx-text-fill: gray; -fx-font-weight:bold; -fx-alignment:center;");
                    case "In Progress" -> setStyle("-fx-text-fill: #00bfff; -fx-font-weight:bold; -fx-alignment:center;");
                    case "Under Review" -> setStyle("-fx-text-fill: gold; -fx-font-weight:bold; -fx-alignment:center;");
                    case "Open" -> setStyle("-fx-text-fill: #00ff7f; -fx-font-weight:bold; -fx-alignment:center;");
                    case "Resolved" -> setStyle("-fx-text-fill: #aaaaaa; -fx-font-weight:bold; -fx-alignment:center;");
                }
            }
        });

        // Priority (colored text)
        TableColumn<Case, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(new PropertyValueFactory<>("priority"));
        priorityCol.setMinWidth(100);
        priorityCol.setMaxWidth(160);
        priorityCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String pr, boolean empty) {
                super.updateItem(pr, empty);
                if (empty || pr == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(pr);
                setStyle("-fx-font-weight: bold; -fx-alignment: center-left;");
                switch (pr) {
                    case "High" -> setStyle("-fx-text-fill: red; -fx-font-weight:bold;");
                    case "Medium" -> setStyle("-fx-text-fill: orange;");
                    case "Low" -> setStyle("-fx-text-fill: green;");

                    default -> setTextFill(javafx.scene.paint.Color.web("#ecf0f1"));
                }
            }
        });

        // Actions column: dropdown + add button in same cell, hide add button if case is closed/resolved
        TableColumn<Case, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setMinWidth(300);
        actionCol.setMaxWidth(400);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final HBox container = new HBox(8);
            private final ComboBox<String> actionDrop = new ComboBox<>();
            private final Button addActionBtn = new Button("Add Action");

            {
                container.setPadding(new Insets(4));
                actionDrop.setPrefWidth(200);
                actionDrop.getStyleClass().add("combo");
                addActionBtn.getStyleClass().addAll("btn", "btn-primary-sm");

                addActionBtn.setOnAction(e -> {
                    Case c = getTableView().getItems().get(getIndex());
                    showAddActionFlow(c);
                });
            }

            private void refreshActionDropdown(Case c) {
                List<Action> actions = actionRepository.findByCaseId(c.getId());
                actionDrop.setItems(FXCollections.observableArrayList(
                        actions.stream()
                                .map(a -> "[" + a.getType() + "] - " + a.getDescription() + " | " +
                                        ACTION_FMT.format(a.getTakenAt().atZone(ZoneId.systemDefault())))
                                .toList()
                ));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Case c = getTableView().getItems().get(getIndex());
                refreshActionDropdown(c);
                // Only show addActionBtn if status is not Resolved or Closed
                String status = c.getStatus();
                if (status != null && (status.equalsIgnoreCase("Resolved") || status.equalsIgnoreCase("Closed"))) {
                    container.getChildren().setAll(actionDrop);
                } else {
                    container.getChildren().setAll(actionDrop, addActionBtn);
                }
                setGraphic(container);
            }
        });

        // CreatedAt / UpdatedAt columns (string display)
        TableColumn<Case, String> createdAtCol = new TableColumn<>("Created At");
        createdAtCol.setCellValueFactory(new PropertyValueFactory<>("createAt"));
        createdAtCol.setMinWidth(100);
        createdAtCol.setMaxWidth(160);

        TableColumn<Case, String> updatedAtCol = new TableColumn<>("Updated At");
        updatedAtCol.setCellValueFactory(new PropertyValueFactory<>("updateAt"));
        updatedAtCol.setMinWidth(100);
        updatedAtCol.setMaxWidth(160);

        table.getColumns().addAll(titleCol, statusCol, priorityCol, actionCol, createdAtCol, updatedAtCol);

        // Table styling (dark)
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getStyleClass().add("dark-table");

        return table;
    }

    // ========================= status editable (ComboBox) =========================
    private void statusColEditable(TableView<Case> table,TableView<Case> availableCasesTable, Employee employee) {
        TableColumn<Case, String> statusCol = (TableColumn<Case, String>)
                table.getColumns().stream()
                        .filter(c -> c.getText().equals("Status"))
                        .findFirst().orElse(null);

        if (statusCol == null) return;

        statusCol.setCellFactory(ComboBoxTableCell.forTableColumn(STATUSES));

        statusCol.setOnEditCommit(event -> {
            Case c = event.getRowValue();

            // Check permissions
            if (c.getEmployeeIds() == null || !c.getEmployeeIds().contains(employee.getId())) {
                Alert a = new Alert(Alert.AlertType.WARNING, "You can update only your assigned cases.");
                a.showAndWait();
                return;
            }

            // Update status and timestamp
            c.setStatus(event.getNewValue());
            c.setUpdateAt(Instant.now());
            caseRepository.updateCase(c);

            // Refresh tables so filtering works
            refreshTables(employee, table, availableCasesTable);
        });

    }

    // ========================= priority editable =========================
    private void priorityColEditable(TableView<Case> table, TableView<Case> availableCasesTable,Employee employee) {
        TableColumn<Case, String> prCol = (TableColumn<Case, String>)
                table.getColumns().stream()
                        .filter(c -> c.getText().equals("Priority"))
                        .findFirst().orElse(null);

        if (prCol == null) return;

        prCol.setCellFactory(ComboBoxTableCell.forTableColumn(PRIORITIES));

        prCol.setOnEditCommit(event -> {
            Case c = event.getRowValue();
            if (c.getEmployeeIds() == null || !c.getEmployeeIds().contains(employee.getId())) {
                Alert a = new Alert(Alert.AlertType.WARNING, "You can update only your assigned cases.");
                a.showAndWait();
                return;
            }
            // Update the case
            c.setPriority(event.getNewValue());
            c.setUpdateAt(Instant.now());
            caseRepository.updateCase(c);

            // Refresh tables so filtering works
            refreshTables(employee,table, availableCasesTable);
        });

    }

    // ========================= show add action flow (ask type then desc) =========================
    private void showAddActionFlow(Case c) {
        // 1) ask for action type (text)
        TextInputDialog typeDialog = new TextInputDialog();
        typeDialog.setTitle("Action Type");
        typeDialog.setHeaderText("Enter Action Type (e.g. CALL_CLIENT, UPDATE_STATUS):");
        Optional<String> actionType = typeDialog.showAndWait();
        if (actionType.isEmpty() || actionType.get().trim().isEmpty()) return;

        // 2) ask for description
        TextInputDialog descDialog = new TextInputDialog();
        descDialog.setTitle("Action Description");
        descDialog.setHeaderText("Enter Action Description:");
        Optional<String> description = descDialog.showAndWait();
        if (description.isEmpty() || description.get().trim().isEmpty()) return;

        // 3) ensure case has employee assigned? (we won't change assignment here)
        if (c.getEmployeeIds() == null) c.setEmployeeIds(new ArrayList<>());

        // 4) add action record
        Action action = new Action(actionType.get().trim(), description.get().trim(), Instant.now(), c.getId());
        actionRepository.save(action);

        // 5) update case's updateAt (not changing assignment or status here)
        c.setUpdateAt(Instant.now());
        caseRepository.updateCase(c);
    }

    // ========================= refresh tables =========================
    private void refreshTables(Employee employee, TableView<Case> myCasesTable, TableView<Case> availableCasesTable) {
        myCasesTable.setItems(
                FXCollections.observableArrayList(caseRepository.getEmployeeCases(employee.getId()))
        );

        availableCasesTable.setItems(
                FXCollections.observableArrayList(
                        caseRepository.findAll().stream()
                                .filter(c -> !c.getStatus().equalsIgnoreCase("Resolved") &&
                                        (c.getEmployeeIds() == null ||
                                                !c.getEmployeeIds().contains(employee.getId())))
                                .toList()
                )
        );
    }

    // ========================= profile popup (unchanged, minor styling) =========================
    private void showProfilePopup(Stage stage, Employee employee) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Profile");
        dialogStage.initOwner(stage);
        dialogStage.initModality(Modality.APPLICATION_MODAL); // Blocks main window

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField(employee.getName());
        TextField emailField = new TextField(employee.getEmail());
        TextField departmentField = new TextField(employee.getDepartment());

        Button updateBtn = new Button("Update Info");
        Button deleteBtn = new Button("Delete Account");

        updateBtn.getStyleClass().addAll("btn", "btn-primary-sm");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger-sm");

        grid.addRow(0, new Label("Name:"), nameField);
        grid.addRow(1, new Label("Email:"), emailField);
        grid.addRow(2, new Label("Department:"), departmentField);
        grid.addRow(3, updateBtn, deleteBtn);

        updateBtn.setOnAction(e -> {
            employee.setName(nameField.getText());
            employee.setEmail(emailField.getText());
            employee.setDepartment(departmentField.getText());
            employeeRepository.updateEmployeeInfo(employee);
            dialogStage.close();
        });

        deleteBtn.setOnAction(e -> {
            employeeRepository.delete(employee.getId());
            stage.close();
            dialogStage.close();
        });
        Scene scene = new Scene(grid);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
}
