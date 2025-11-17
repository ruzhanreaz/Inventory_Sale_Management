package com.example.demo.controllers;

import com.example.demo.dao.ProductDAO;
import com.example.demo.models.Product;
import com.example.demo.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductController {
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Double> purchasePriceColumn;
    @FXML private TableColumn<Product, Double> salePriceColumn;
    @FXML private TableColumn<Product, Integer> quantityColumn;
    @FXML private TableColumn<Product, LocalDate> dateAddedColumn;
    @FXML private TableColumn<Product, LocalDate> expiryDateColumn;
    @FXML private TableColumn<Product, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button saveButton;
    @FXML private Button backButton;
    @FXML private TextField categoryField;
    @FXML private TextField nameField;
    @FXML private TextField purchasePriceField;
    @FXML private TextField salePriceField;
    @FXML private TextField quantityField;
    @FXML private DatePicker dateAddedPicker;
    @FXML private DatePicker expiryDatePicker;

    private final ProductDAO productDAO = new ProductDAO();
    private ObservableList<Product> productList;
    private final Set<Product> editedProducts = new HashSet<>();
    private Product currentSearchedProduct = null;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        purchasePriceColumn.setCellValueFactory(new PropertyValueFactory<>("purchasePrice"));
        salePriceColumn.setCellValueFactory(new PropertyValueFactory<>("salePrice"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        dateAddedColumn.setCellValueFactory(new PropertyValueFactory<>("dateAdded"));
        expiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
        // Format LocalDate as String for display
        dateAddedColumn.setCellFactory(col -> new TableCell<Product, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });
        expiryDateColumn.setCellFactory(col -> new TableCell<Product, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.toString());
            }
        });

        productTable.setEditable(true);
        quantityColumn.setCellFactory(col -> {
            TableCell<Product, Integer> cell = new TableCell<>() {
                private final Button plusButton = new Button("+");
                private final Button minusButton = new Button("-");
                private final TextField quantityField = new TextField();
                private final HBox box = new HBox(5, minusButton, quantityField, plusButton);
                {
                    plusButton.setOnAction(e -> {
                        Product product = getTableView().getItems().get(getIndex());
                        product.setQuantity(product.getQuantity() + 1);
                        quantityField.setText(String.valueOf(product.getQuantity()));
                        editedProducts.add(product);
                    });
                    minusButton.setOnAction(e -> {
                        Product product = getTableView().getItems().get(getIndex());
                        if (product.getQuantity() > 0) {
                            product.setQuantity(product.getQuantity() - 1);
                            quantityField.setText(String.valueOf(product.getQuantity()));
                            editedProducts.add(product);
                        }
                    });
                    quantityField.setPrefWidth(40);
                    quantityField.setOnAction(e -> {
                        Product product = getTableView().getItems().get(getIndex());
                        try {
                            int newQty = Integer.parseInt(quantityField.getText());
                            if (newQty >= 0) {
                                product.setQuantity(newQty);
                                editedProducts.add(product);
                            } else {
                                quantityField.setText(String.valueOf(product.getQuantity()));
                            }
                        } catch (NumberFormatException ex) {
                            quantityField.setText(String.valueOf(product.getQuantity()));
                        }
                    });
                    box.setStyle("-fx-alignment: center;");
                }
                @Override
                protected void updateItem(Integer quantity, boolean empty) {
                    super.updateItem(quantity, empty);
                    if (empty || getIndex() >= getTableView().getItems().size()) {
                        setGraphic(null);
                    } else {
                        Product product = getTableView().getItems().get(getIndex());
                        quantityField.setText(String.valueOf(product.getQuantity()));
                        setGraphic(box);
                    }
                }
            };
            return cell;
        });
        loadProducts();
        addDeleteButtonToTable();

        // Role-based access check
        String userType = SessionManager.getUserType();
        if (!"admin".equalsIgnoreCase(userType) && !"employee".equalsIgnoreCase(userType)) {
            System.out.println("Unauthorized access to product view");
            // Optionally, redirect to login or show error
        }
        // Enforce navigation only from dashboard
        if (!SessionManager.isNavigatedFromDashboard()) {
            System.out.println("Direct access to product view is not allowed. Redirecting to dashboard.");
            try {
                String fxml;
                if ("employee".equalsIgnoreCase(userType)) {
                    fxml = "/com/example/demo/employee-dashboard-view.fxml";
                } else {
                    fxml = "/com/example/demo/admin-dashboard-view.fxml";
                }
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
                Scene dashboardScene = new Scene(fxmlLoader.load(), 1000, 800);
                Stage stage = (Stage) productTable.getScene().getWindow();
                stage.setTitle("Dashboard");
                stage.setScene(dashboardScene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        // Reset flag after successful entry
        SessionManager.setNavigatedFromDashboard(false);
    }

    private void loadProducts() {
        List<Product> products = productDAO.getAllProducts();
        productList = FXCollections.observableArrayList(products);
        productTable.setItems(productList);
        productTable.refresh(); // Force TableView to update
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            productTable.setItems(productList);
            clearProductFields();
            currentSearchedProduct = null;
            return;
        }
        Product foundProduct = null;
        try {
            int id = Integer.parseInt(keyword);
            foundProduct = productDAO.findProductById(id);
        } catch (NumberFormatException e) {
            foundProduct = productDAO.findProductByName(keyword);
        }
        if (foundProduct != null) {
            fillProductFields(foundProduct);
            currentSearchedProduct = foundProduct;
        } else {
            clearProductFields();
            currentSearchedProduct = null;
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No product found with the given ID or name.");
            alert.showAndWait();
        }
    }

    private void fillProductFields(Product product) {
        categoryField.setText(product.getCategory());
        nameField.setText(product.getName());
        purchasePriceField.setText(String.valueOf(product.getPurchasePrice()));
        salePriceField.setText(String.valueOf(product.getSalePrice()));
        quantityField.setText(String.valueOf(product.getQuantity()));
        dateAddedPicker.setValue(product.getDateAdded());
        expiryDatePicker.setValue(product.getExpiryDate());
    }

    private void clearProductFields() {
        categoryField.clear();
        nameField.clear();
        purchasePriceField.clear();
        salePriceField.clear();
        quantityField.clear();
        dateAddedPicker.setValue(null);
        expiryDatePicker.setValue(null);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (currentSearchedProduct != null) {
            // Update the product with edited values
            currentSearchedProduct.setCategory(categoryField.getText());
            currentSearchedProduct.setName(nameField.getText());
            try {
                currentSearchedProduct.setPurchasePrice(Double.parseDouble(purchasePriceField.getText()));
            } catch (NumberFormatException e) {}
            try {
                currentSearchedProduct.setSalePrice(Double.parseDouble(salePriceField.getText()));
            } catch (NumberFormatException e) {}
            try {
                currentSearchedProduct.setQuantity(Integer.parseInt(quantityField.getText()));
            } catch (NumberFormatException e) {}
            currentSearchedProduct.setDateAdded(dateAddedPicker.getValue());
            currentSearchedProduct.setExpiryDate(expiryDatePicker.getValue());
            productDAO.updateProduct(currentSearchedProduct);
            loadProducts(); // Ensure list is refreshed
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Product updated successfully.");
            alert.showAndWait();
            currentSearchedProduct = null;
            clearProductFields();
        } else {
            for (Product product : editedProducts) {
                productDAO.updateProduct(product);
            }
            editedProducts.clear();
            loadProducts(); // Ensure list is refreshed
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Changes saved to database.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            String userType = SessionManager.getUserType();
            String fxml;
            if ("employee".equalsIgnoreCase(userType)) {
                fxml = "/com/example/demo/employee-dashboard-view.fxml";
            } else if ("admin".equalsIgnoreCase(userType)) {
                fxml = "/com/example/demo/admin-dashboard-view.fxml";
            } else {
                System.out.println("Unauthorized access to dashboard");
                return;
            }
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
            Scene dashboardScene = new Scene(fxmlLoader.load(), 1000, 800);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            String title = "Dashboard";
            if ("employee".equalsIgnoreCase(userType)) {
                title = "Employee Dashboard";
            } else if ("admin".equalsIgnoreCase(userType)) {
                title = "Admin Dashboard";
            }
            stage.setTitle(title);
            stage.setScene(dashboardScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddProduct(ActionEvent event) {
        String category = categoryField.getText();
        String name = nameField.getText();
        String purchasePriceStr = purchasePriceField.getText();
        String salePriceStr = salePriceField.getText();
        String quantityStr = quantityField.getText();
        LocalDate dateAdded = dateAddedPicker.getValue();
        LocalDate expiryDate = expiryDatePicker.getValue();

        // Basic validation
        if (category.isEmpty() || name.isEmpty() || purchasePriceStr.isEmpty() || salePriceStr.isEmpty() || quantityStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please fill in all product details.");
            return;
        }
        double purchasePrice, salePrice;
        int quantity;
        try {
            purchasePrice = Double.parseDouble(purchasePriceStr);
            salePrice = Double.parseDouble(salePriceStr);
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid number format for price or quantity.");
            return;
        }
        Product product = new Product(0, category, name, purchasePrice, salePrice, quantity, dateAdded, expiryDate);
        System.out.println("[DEBUG] Attempting to add product: " + product.getName());
        boolean added = productDAO.addProduct(product);
        if (added) {
            showAlert(Alert.AlertType.INFORMATION, "Product added successfully.");
            loadProducts(); // Ensure list is refreshed
            clearProductInputFields();
        } else {
            showAlert(Alert.AlertType.ERROR, "Failed to add product. Please check your input or database connection.");
        }
    }

    private void clearProductInputFields() {
        categoryField.clear();
        nameField.clear();
        purchasePriceField.clear();
        salePriceField.clear();
        quantityField.clear();
        dateAddedPicker.setValue(null);
        expiryDatePicker.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message);
        alert.showAndWait();
    }

    private void addDeleteButtonToTable() {
        TableColumn<Product, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("Delete");
            {
                deleteButton.setOnAction(e -> {
                    Product product = getTableView().getItems().get(getIndex());
                    handleDeleteProduct(product);
                });
                deleteButton.getStyleClass().add("button-red");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });
        productTable.getColumns().add(deleteCol);
    }
    private void handleDeleteProduct(Product product) {
        productDAO.deleteProduct(product.getId());
        showAlert(Alert.AlertType.INFORMATION, "Product deleted successfully.");
        loadProducts(); // Ensure list is refreshed
    }
}
