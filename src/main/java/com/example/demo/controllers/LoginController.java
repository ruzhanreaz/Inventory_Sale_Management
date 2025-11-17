package com.example.demo.controllers;

import com.example.demo.dao.UserDAO;
import com.example.demo.models.User;
import com.example.demo.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private Label messageLabel;

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;

    @FXML
    public void handleLogin(ActionEvent actionEvent) {
        String email = emailField.getText();
        String password = passwordField.getText();
        UserDAO userDAO = new UserDAO();
        User user = userDAO.authenticate(email, password);
        if (user != null) {
            messageLabel.setText("Login successful!");
            // Set user type and id in SessionManager
            SessionManager.setUserType(user.getType());
            SessionManager.setUserId(user.getId());
            // Redirect based on user type
            String fxmlPath;
            if ("admin".equalsIgnoreCase(user.getType())) {
                fxmlPath = "/com/example/demo/admin-dashboard-view.fxml";
            } else if ("employee".equalsIgnoreCase(user.getType())) {
                fxmlPath = "/com/example/demo/employee-dashboard-view.fxml";
            } else {
                messageLabel.setText("Unknown user type");
                return;
            }
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent dashboardRoot = loader.load();
                // No need to set userType on controller, use SessionManager everywhere
                Stage stage = (Stage) emailField.getScene().getWindow();
                if ("admin".equalsIgnoreCase(user.getType())) {
                    stage.setTitle("Admin Dashboard");
                } else if ("employee".equalsIgnoreCase(user.getType())) {
                    stage.setTitle("Employee Dashboard");
                }
                stage.setScene(new Scene(dashboardRoot, 1000, 800));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                messageLabel.setText("Failed to load dashboard");
            }
        } else {
            messageLabel.setText("Invalid credentials");
        }
    }
}
