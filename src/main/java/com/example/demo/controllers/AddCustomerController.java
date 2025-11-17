package com.example.demo.controllers;

import com.example.demo.dao.CustomerDAO;
import com.example.demo.models.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class AddCustomerController {
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;

    // Controls for table/search
    @FXML private TextField searchField;
    @FXML private Button refreshButton;
    @FXML private Button saveButton;
    @FXML private Button backButton; // added to match FXML
    @FXML private Button deleteButton; // added for delete functionality

    // Table and columns
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, Integer> idColumn;
    @FXML private TableColumn<Customer, String> nameColumn;
    @FXML private TableColumn<Customer, String> phoneColumn;
    @FXML private TableColumn<Customer, String> emailColumn;

    private ObservableList<Customer> masterData = FXCollections.observableArrayList();
    private FilteredList<Customer> filteredData;
    private Integer selectedCustomerId = null; // null when creating new

    @FXML
    private void initialize() {
        // Configure columns to use Customer getters
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Load initial data
        masterData.setAll(CustomerDAO.getAllCustomers());

        // Set up filtered & sorted lists for searching
        filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<Customer> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(customerTable.comparatorProperty());
        customerTable.setItems(sortedData);

        // Search listener
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                String lower = newVal == null ? "" : newVal.trim().toLowerCase();
                filteredData.setPredicate(c -> {
                    if (lower.isEmpty()) return true;
                    return (c.getName() != null && c.getName().toLowerCase().contains(lower))
                            || (c.getEmail() != null && c.getEmail().toLowerCase().contains(lower))
                            || (c.getPhone() != null && c.getPhone().toLowerCase().contains(lower));
                });
            });
        }

        // Selection listener to load selected customer into form for editing
        customerTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedCustomerId = newSel.getId();
                nameField.setText(newSel.getName());
                phoneField.setText(newSel.getPhone());
                emailField.setText(newSel.getEmail());
                if (saveButton != null) saveButton.setText("Update");
                statusLabel.setText("");
            }
        });

        // Refresh button (defensive check)
        if (refreshButton != null) {
            refreshButton.setOnAction(e -> refreshCustomerTable());
        }
    }

    private void refreshCustomerTable() {
        List<Customer> customers = CustomerDAO.getAllCustomers();
        masterData.setAll(customers);
        // clear selection and form
        customerTable.getSelectionModel().clearSelection();
        selectedCustomerId = null;
        if (saveButton != null) saveButton.setText("Save");
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        statusLabel.setText("");
    }

    @FXML
    private void handleAddCustomer() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        String email = emailField.getText() == null ? "" : emailField.getText().trim();

        // Basic validation
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            statusLabel.setText("All fields are required.");
            return;
        }

        boolean success;
        if (selectedCustomerId == null) {
            // Create new
            success = CustomerDAO.addCustomer(name, phone, email);
            if (success) {
                statusLabel.setText("Customer added successfully.");
            } else {
                statusLabel.setText("Failed to add customer.");
            }
        } else {
            // Update existing
            success = CustomerDAO.updateCustomer(selectedCustomerId, name, phone, email);
            if (success) {
                statusLabel.setText("Customer updated successfully.");
            } else {
                statusLabel.setText("Failed to update customer.");
            }
        }

        if (success) {
            // refresh table and reset form
            refreshCustomerTable();
        }
    }

    @FXML
    private void handleRefresh() {
        refreshCustomerTable();
    }

    @FXML
    private void handleDeleteCustomer() {
        // Check if a customer is selected
        if (selectedCustomerId == null) {
            statusLabel.setText("Please select a customer to delete.");
            return;
        }

        // Show confirmation dialog (using JavaFX Alert)
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete Customer");
        alert.setContentText("Are you sure you want to delete this customer? This action cannot be undone.");

        java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
            // User confirmed deletion
            boolean success = CustomerDAO.deleteCustomer(selectedCustomerId);
            if (success) {
                statusLabel.setText("Customer deleted successfully.");
                refreshCustomerTable(); // Refresh table and clear form
            } else {
                statusLabel.setText("Failed to delete customer.");
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo/employee-dashboard-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setTitle("Employee Dashboard");
            stage.setScene(new Scene(root, 1000, 800));
        } catch (IOException e) {
            statusLabel.setText("Failed to go back.");
        }
    }
}
