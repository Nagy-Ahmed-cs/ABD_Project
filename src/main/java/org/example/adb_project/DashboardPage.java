package org.example.adb_project;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Case;
import models.Client;
import org.bson.types.ObjectId;
import repos.CaseRepo;
import repos.ClientRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DashboardPage {

    final ClientRepository clientRepository = new ClientRepository();
    final CaseRepo caseRepo=new CaseRepo();

    public void open(Stage stage, Client client) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // ===== Top: Profile & Exit =====
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(10));
        topBar.setSpacing(200); // space between buttons

        Button profileIcon = new Button("Profile");
        Button exit = new Button("Exit");

        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("dashboard-button");

        backBtn.setOnAction(e -> {
            ClientPage clientPage=new ClientPage();
            clientPage.open(stage);
        });
        profileIcon.setOnMouseClicked(e -> showProfilePopup(stage, client));
        profileIcon.getStyleClass().add("dashboard-button");

        exit.setOnMouseClicked(e -> stage.close());
        exit.getStyleClass().add("exit-button");

        topBar.getChildren().addAll(profileIcon, backBtn,exit);
        root.setTop(topBar);

        // ===== Center: Table of Cases =====
        TableView<Case> caseTable = new TableView<>();
        ObservableList<Case> data = FXCollections.observableArrayList(
                clientRepository.getClientCases(client.getId())
        );
        caseTable.setItems(data);

        TableColumn<Case, String> nameCol = new TableColumn<>("Title");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Case, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new TableCell<Case, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);

                    // Reset style
                    setStyle("-fx-font-weight: bold; -fx-alignment: CENTER;");

                    // Apply color based on status
                    switch (status) {
                        case "Resolved":
                            setTextFill(javafx.scene.paint.Color.GREEN);
                            break;
                        case "In Progress":
                            setTextFill(javafx.scene.paint.Color.ORANGE);
                            break;
                        case "Open":
                            setTextFill(javafx.scene.paint.Color.DARKRED);
                            break;
                        case "Not Taken":
                            setTextFill(javafx.scene.paint.Color.RED);
                            break;
                        case "Under Review":
                            setTextFill(javafx.scene.paint.Color.BLUE);
                            break;
                        default:
                            setTextFill(javafx.scene.paint.Color.BLACK);
                            break;
                    }
                }
            }
        });


        TableColumn<Case, String> createAtCol = new TableColumn<>("Created At");
        createAtCol.setCellValueFactory(new PropertyValueFactory<>("createAt"));

        TableColumn<Case, String> updatedAtCol = new TableColumn<>("Updated At");
        updatedAtCol.setCellValueFactory(new PropertyValueFactory<>("updateAt"));
        TableColumn<Case, Void> deleteCol = new TableColumn<>("  ");
        deleteCol.setCellFactory(column -> new TableCell<>() {
            private final Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
                deleteBtn.setOnAction(e -> {
                    Case c = getTableView().getItems().get(getIndex());

                    // Confirm deletion
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Case");
                    confirm.setHeaderText("Are you sure you want to delete this case?");
                    confirm.setContentText(c.getTitle());

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            // Delete from DB
                            caseRepo.delete(c.getId());

                            // Remove from table
                            getTableView().getItems().remove(c);

                            showAlert("Deleted", "Case deleted successfully!");
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });


        caseTable.getColumns().addAll(nameCol, statusCol, createAtCol, updatedAtCol,deleteCol);
        root.setCenter(caseTable);

        // ===== Bottom: Create Case Button =====
        Button createCaseBtn = new Button("Create New Case");
        createCaseBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        root.setBottom(createCaseBtn);

        createCaseBtn.setOnAction(e -> {
            TextInputDialog dialogTitle = new TextInputDialog();
            dialogTitle.setHeaderText("Enter Case Title:");
            String title = dialogTitle.showAndWait().orElse(null);
            if (title == null) return;

            TextInputDialog dialogPriority = new TextInputDialog();
            dialogPriority.setHeaderText("Enter Case Priority (High/Medium/Low):");
            String priority = dialogPriority.showAndWait().orElse(null);
            if (priority == null) return;

            // Create case
            createCase(title, priority, client.getId());

            // ===== Refresh Table =====
            List<Case> updatedCases = clientRepository.getClientCases(client.getId());
            data.setAll(updatedCases); // updates the TableView
        });

        Scene scene = new Scene(root, 700, 500);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Client Dashboard");
        stage.show();
    }


    // ===== Profile Popup =====
    private void showProfilePopup(Stage owner, Client client) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(owner);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        Label nameLabel = new Label("Name: " + client.getName());
        Label emailLabel = new Label("Email: " + client.getEmail());
        Label phoneLabel = new Label("Phone: " + client.getPhone());
        Label addressLabel = new Label("Address: " + client.getAddress());

        Button updateBtn = new Button("Update Info");


        updateBtn.setOnAction(e -> {
            popup.close();
            showUpdatePopup(owner, client);
        });



        vbox.getChildren().addAll(nameLabel, emailLabel, phoneLabel, addressLabel, updateBtn);

        Scene scene = new Scene(vbox, 300, 250);
        popup.setScene(scene);
        popup.setTitle("Profile Info");
        popup.show();
    }

    // ===== Update Info Popup =====
    private void showUpdatePopup(Stage owner, Client client) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(owner);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        TextField nameField = new TextField(client.getName());
        TextField emailField = new TextField(client.getEmail());
        TextField phoneField = new TextField(client.getPhone());
        TextField addressField = new TextField(client.getAddress());

        Button saveBtn = new Button("Save");

        saveBtn.setOnAction(e -> {

            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String address = addressField.getText();

            if (!validateUpdate(client, name, email, phone, address)) {
                return;
            }

            client.setName(name);
            client.setEmail(email);
            client.setPhone(phone);
            client.setAddress(address);

            clientRepository.update(client);

            showAlert("Success", "Client info updated successfully!");
            popup.close();
        });


        vbox.getChildren().addAll(
                new Label("Name:"), nameField,
                new Label("Email:"), emailField,
                new Label("Phone:"), phoneField,
                new Label("Address:"), addressField,
                saveBtn
        );

        Scene scene = new Scene(vbox, 350, 300);
        popup.setScene(scene);
        popup.setTitle("Update Profile");
        popup.show();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }


    private boolean validateUpdate(Client oldClient, String name, String email, String phone, String address) {

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            showAlert("Error", "All fields are required!");
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showAlert("Error", "Invalid email format!");
            return false;
        }

        Client existing = clientRepository.findByEmail(email);
        if (existing != null && !existing.getId().equals(oldClient.getId())) {
            showAlert("Error", "This email is already used by another client!");
            return false;
        }

        if (!phone.matches("^(010|011|012|015)[0-9]{8}$")) {
            showAlert("Error", "Invalid phone number! Example: 01012345678");
            return false;
        }

        if (address.length() < 4) {
            showAlert("Error", "Address is too short!");
            return false;
        }

        return true;
    }



    private void createCase(String title, String priority, ObjectId clientId) {

        Case c = new Case();
        c.setTitle(title);
        c.setPriority(priority);
        c.setStatus("Not Taken");               // default status
        c.setClientId(clientId);
        c.setEmployeeIds(new ArrayList<>());    // empty list
        c.setCreateAt(Instant.now());
        c.setUpdateAt(Instant.now());

        CaseRepo repo = new CaseRepo();
        repo.save(c);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Case Created Successfully!");
        alert.show();
    }



}