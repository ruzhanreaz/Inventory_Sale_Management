package com.example.demo.controllers;

import com.example.demo.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class DashBoardController {
    @FXML
    public void initialize() {
        // Prevent unauthorized access: only allow admin or employee
        String userType = SessionManager.getUserType();
        if (!"admin".equalsIgnoreCase(userType) && !"employee".equalsIgnoreCase(userType)) {
            System.out.println("Unauthorized access to dashboard");
            // Optionally, redirect to login or show error
        }
    }

    @FXML
    private void handleProductView(ActionEvent event) {
        try {
            String userType = SessionManager.getUserType();
            String fxml;
            if ("employee".equalsIgnoreCase(userType)) {
                fxml = "/com/example/demo/employee-product-view.fxml";
            } else if ("admin".equalsIgnoreCase(userType)) {
                fxml = "/com/example/demo/admin-product-view.fxml";
            } else {
                System.out.println("Unauthorized access to product view");
                return;
            }
            // Mark navigation as coming from dashboard
            com.example.demo.utils.SessionManager.setNavigatedFromDashboard(true);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
            Parent productRoot = fxmlLoader.load();
            Scene productScene = new Scene(productRoot, 1000, 800);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Products");
            stage.setScene(productScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReportView(ActionEvent event) {
        try {
            String userType = SessionManager.getUserType();
            if (!"admin".equalsIgnoreCase(userType)) {
                System.out.println("Unauthorized access to report view");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/demo/report.fxml"));
            Parent reportRoot = fxmlLoader.load();
            Scene reportScene = new Scene(reportRoot, 1000, 800);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Sales Report");
            stage.setScene(reportScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleuserView(ActionEvent event) {
        try {
            String userType = SessionManager.getUserType();
            if (!"admin".equalsIgnoreCase(userType)) {
                System.out.println("Unauthorized access to user view");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/demo/admin-user-view.fxml"));
            Parent userRoot = fxmlLoader.load();
            Scene userScene = new Scene(userRoot, 1000, 800);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Edit User");
            stage.setScene(userScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaleView(ActionEvent event) {
        try {
            // Only employees can create sales; admins could be allowed too if needed
            String userType = SessionManager.getUserType();
            if (!"employee".equalsIgnoreCase(userType) && !"admin".equalsIgnoreCase(userType)) {
                System.out.println("Unauthorized access to sale view");
                return;
            }
            // mark navigation flag
            SessionManager.setNavigatedFromDashboard(true);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/demo/employee-sale-view.fxml"));
            Parent saleRoot = fxmlLoader.load();
            Scene saleScene = new Scene(saleRoot, 1000, 800);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Create Sale");
            stage.setScene(saleScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlelogout(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/demo/login-view.fxml"));
            Parent loginRoot = fxmlLoader.load();
            Scene loginScene = new Scene(loginRoot, 1000, 800);
            // Get the current stage from the event
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Login");
            stage.setScene(loginScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCustomerView(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/demo/add-customer-view.fxml"));
            Parent addCustomerRoot = fxmlLoader.load();
            Scene addCustomerScene = new Scene(addCustomerRoot, 1000, 800);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Add Customer");
            stage.setScene(addCustomerScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
