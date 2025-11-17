package com.example.demo.controllers;

import com.example.demo.dao.UserDAO;
import com.example.demo.models.User;
import com.example.demo.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.util.regex.Pattern;

public class UserController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private Button addButton;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;

    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private final UserDAO userDAO = new UserDAO();
    private User selectedUser = null;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    @FXML
    public void initialize() {
        // Only allow admin
        if (!"admin".equalsIgnoreCase(SessionManager.getUserType())) {
            if (statusLabel != null) statusLabel.setText("Unauthorized access");
            setFieldsDisabled(true);
            return;
        }
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        loadUsers();
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> onUserSelected(newSel));
    }

    private void loadUsers() {
        userList.setAll(userDAO.getAllUsers());
        userTable.setItems(userList);
    }

    private void onUserSelected(User user) {
        selectedUser = user;
        if (user != null) {
            nameField.setText(user.getName());
            emailField.setText(user.getEmail());
            roleComboBox.setValue(user.getType());
            passwordField.setText(""); // Do not show password
        } else {
            clearForm();
        }
    }

    @FXML
    private void handleSaveUser() {
        if (selectedUser == null) {
            statusLabel.setText("Select a user to edit");
            return;
        }
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();
        String password = passwordField.getText();
        if (name.isEmpty() || email.isEmpty() || role == null) {
            statusLabel.setText("Name, email, and role are required");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            statusLabel.setText("Enter a valid email address");
            return;
        }
        User user = new User(selectedUser.getId(), name, email, role, password.isEmpty() ? selectedUser.getPassword() : password);
        boolean success = userDAO.updateUser(user);
        statusLabel.setText(success ? "User updated" : "Update failed");
        loadUsers();
    }

    @FXML
    private void handleDeleteUser() {
        if (selectedUser == null) {
            statusLabel.setText("Select a user to delete");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Deletion");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete user '" + selectedUser.getEmail() + "'? This action cannot be undone.");
        ButtonType result = confirm.showAndWait().orElse(ButtonType.CANCEL);
        if (result != ButtonType.OK) return;

        boolean success = userDAO.deleteUserByEmail(selectedUser.getEmail());
        statusLabel.setText(success ? "User deleted" : "Delete failed");
        loadUsers();
        onUserSelected(null);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/demo/admin-dashboard-view.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 800));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            statusLabel.setText("Failed to return to dashboard");
        }
    }

    @FXML
    private void handleAddUser() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();
        String password = passwordField.getText();
        if (name.isEmpty() || email.isEmpty() || role == null || password.isEmpty()) {
            statusLabel.setText("All fields are required");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            statusLabel.setText("Enter a valid email address");
            return;
        }
        // Prevent duplicate email
        if (userDAO.findByEmail(email) != null) {
            statusLabel.setText("A user with this email already exists");
            return;
        }
        User user = new User(name, email, role, password);
        boolean success = userDAO.addUser(user);
        statusLabel.setText(success ? "User added" : "Add failed");
        loadUsers();
        clearForm();
    }

    private void clearForm() {
        nameField.clear();
        emailField.clear();
        roleComboBox.setValue(null);
        passwordField.clear();
        userTable.getSelectionModel().clearSelection();
        selectedUser = null;
    }

    private void setFieldsDisabled(boolean disabled) {
        nameField.setDisable(disabled);
        emailField.setDisable(disabled);
        roleComboBox.setDisable(disabled);
        passwordField.setDisable(disabled);
        saveButton.setDisable(disabled);
        deleteButton.setDisable(disabled);
        backButton.setDisable(disabled);
    }
}
