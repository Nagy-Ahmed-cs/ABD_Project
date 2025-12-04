package org.example.adb_project;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Case;
import models.Client;
import repos.ClientRepository;

import java.util.ArrayList;
import java.util.List;



public class ClientPage {
    final ClientRepository clientRepository =new ClientRepository();
    public void open(Stage stage) {
        // Main VBox for client page
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-alignment: center; -fx-background-color: #1e1e1e;");

        Label title = new Label("Client Page");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        // Buttons for Sign In or Log In
        Button signInBtn = new Button("Sign Up");
        Button logInBtn = new Button("Log In");
        signInBtn.getStyleClass().add("menu-button");
        logInBtn.getStyleClass().add("menu-button");
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("dashboard-button");

        backBtn.setOnAction(e -> {
            HelloApplication helloApplication=new HelloApplication();
            helloApplication.start(stage); // pass the same stage
        });

        HBox buttonBox = new HBox(20, signInBtn, logInBtn,backBtn);
        buttonBox.setStyle("-fx-alignment: center;");

        root.getChildren().addAll(title, buttonBox);

        Scene scene;
        if (stage.getScene() == null) {
            scene = new Scene(root, 400, 400);
            stage.setScene(scene);
        } else {
            // Replace current root with new root
            stage.getScene().setRoot(root);
        }

        stage.setTitle("Client Page");
        stage.show();

        // Actions
        signInBtn.setOnAction(e -> showSignInForm(root,stage));
        logInBtn.setOnAction(e -> showLogInForm(root,stage));
    }


    // Show Sign In Form
    private void showSignInForm(VBox root,Stage stage) {
        root.getChildren().clear();

        Label title = new Label("Sign Up - New Client");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Client Name");

        TextField emailField = new TextField();
        emailField.setPromptText("Client Email");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");

        TextField addressField = new TextField();
        addressField.setPromptText("Address");

        Button submitBtn = new Button("Create Account");
        submitBtn.getStyleClass().add("menu-button");
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("dashboard-button");

        backBtn.setOnAction(e -> {
           ClientPage clientPage=new ClientPage();
           clientPage.open(stage);
        });

        submitBtn.setOnAction(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String address = addressField.getText();

            if (!validateClient(name, email, phone, address)) {
                return; // stop saving
            }


            Client client=new Client(name,email,phone,address,new ArrayList<>());
            clientRepository.save(client);
            showAlert("Success", "Client account created:\n" + name + "\n" + email);

            new DashboardPage().open(new Stage(),client);
        });

        root.getChildren().addAll(title, nameField, emailField, phoneField, addressField, submitBtn,backBtn);
    }

    // Show Log In Form
    private void showLogInForm(VBox root,Stage stage) {
        root.getChildren().clear();

        Label title = new Label("Log In - Existing Client");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("Client Email");

        Button submitBtn = new Button("Log In");
        submitBtn.getStyleClass().add("menu-button");
        Hyperlink signInLink = new Hyperlink("Don't have an account? Sign Up");
        signInLink.setStyle("-fx-font-size: 14px; -fx-text-fill: #2d89ef;");
        signInLink.setOnAction(e -> {
            // Open the Sign In page
            showSignInForm(root,stage);

        });
        Button backBtn = new Button("Back");
        backBtn.getStyleClass().add("dashboard-button");

        backBtn.setOnAction(e -> {
            ClientPage clientPage=new ClientPage();
            clientPage.open(stage);
        });

        submitBtn.setOnAction(e -> {
            String email = emailField.getText();
            if(email.isEmpty()) {
                showAlert("Error", "Email is required!");
                return;
            }
            List <Client> clients=clientRepository.findAll();
            boolean flag=false;
            for(Client c:clients){
                //System.out.println(c.getEmail());
                if(c.getEmail().equals(email)){
                    List<Case> cases=clientRepository.getClientCases(c.getId());
                   // System.out.println(cases.get(0).getClientId()+":"+c.getId());

                    new DashboardPage().open(stage,c);
                    flag=true;
                    break;
                    //add client Dashboard
                  }}
                    if(!flag){
                    showAlert("Failed","Enter Valid Email");
                    showLogInForm(root,stage);}





        });

        root.getChildren().addAll(title, emailField, submitBtn,signInLink,backBtn);
    }
    private boolean validateClient(String name, String email, String phone, String address) {

        // 1. Empty fields check
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            showError("All fields are required!");
            return false;
        }

        // 2. Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showError("Invalid email format!");
            return false;
        }
        if (clientRepository.findByEmail(email)!= null) {
            showError("This email is already in use!");
            return false;
        }

        // 3. Validate phone number (Egypt example)
        // Accepts: 010, 011, 012, 015 + 8 digits = 11 digits total
        if (!phone.matches("^(010|011|012|015)[0-9]{8}$")) {
            showError("Invalid phone number! Example: 01012345678");
            return false;
        }

        // 4. Address length check (optional)
        if (address.length() < 4) {
            showError("Address is too short!");
            return false;
        }

        return true;
    }
    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

