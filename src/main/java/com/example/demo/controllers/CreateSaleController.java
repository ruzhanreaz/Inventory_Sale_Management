package com.example.demo.controllers;

import com.example.demo.dao.CustomerDAO;
import com.example.demo.dao.OrderDAO;
import com.example.demo.dao.ProductDAO;
import com.example.demo.models.Customer;
import com.example.demo.models.Product;
import com.example.demo.models.SaleReportRow;
import com.example.demo.utils.SessionManager;
import com.example.demo.patterns.observer.ProductSubject;
import com.example.demo.patterns.observer.ExpiryObserver;
import com.example.demo.patterns.observer.StockObserver;
import com.example.demo.patterns.memento.SaleDraftCaretaker;
import com.example.demo.patterns.memento.SaleDraftMemento;
import com.example.demo.patterns.memento.SaleDraftOriginator;
import com.example.demo.patterns.strategy.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CreateSaleController {
    // Customer section
    @FXML private TextField phoneField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private Button searchCustomerButton;

    // Product selection fields
    @FXML private TextField productIdField;
    @FXML private TextField quantityField;
    @FXML private Button addProductButton;

    // Product list table (left side)
    @FXML private TableView<Product> productListTable;
    @FXML private TableColumn<Product, Integer> listProductIdColumn;
    @FXML private TableColumn<Product, String> listProductNameColumn;
    @FXML private TableColumn<Product, Double> listProductPriceColumn;
    @FXML private TableColumn<Product, Integer> listProductStockColumn;

    // Cart table (right side)
    @FXML private TableView<CartItem> productsTable;
    @FXML private TableColumn<CartItem, Integer> productIdColumn;
    @FXML private TableColumn<CartItem, String> productNameColumn;
    @FXML private TableColumn<CartItem, Integer> quantityColumn;
    @FXML private TableColumn<CartItem, Double> priceColumn;
    @FXML private TableColumn<CartItem, Double> totalColumn;
    @FXML private TableColumn<CartItem, Void> removeColumn;

    // Drafts
    @FXML private ListView<String> draftsListView;
    @FXML private Button draftButton;
    @FXML private Button restoreButton;
    @FXML private Button restoreSelectedDraftButton;

    // Footer
    @FXML private Label totalLabel;
    @FXML private Button completeSaleButton;
    @FXML private Button backButton;

    private final ProductDAO productDAO = new ProductDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    private Integer selectedCustomerId = null;

    private final DecimalFormat moneyFmt = new DecimalFormat("$0.00");
    private final ProductSubject productSubject = new ProductSubject();
    private final SaleDraftOriginator draftOriginator = new SaleDraftOriginator();
    private final SaleDraftCaretaker caretaker = new SaleDraftCaretaker();
    private final SaleDraftOriginator originator = new SaleDraftOriginator();

    // Add this method to refresh the drafts list
    private void refreshDraftsList() {
        ObservableList<String> draftPhones = FXCollections.observableArrayList(
                com.example.demo.patterns.memento.SaleDraftCaretaker.getAllDraftPhones()
        );
        draftsListView.setItems(draftPhones);
    }

    @FXML
    public void initialize() {
        // Access control: only from dashboard
        String userType = SessionManager.getUserType();
        if (!"admin".equalsIgnoreCase(userType) && !"employee".equalsIgnoreCase(userType)) {
            System.out.println("Unauthorized access to sale view");
        }
        if (!SessionManager.isNavigatedFromDashboard()) {
            try {
                String fxml = "employee".equalsIgnoreCase(userType) ? "/com/example/demo/employee-dashboard-view.fxml" : "/com/example/demo/admin-dashboard-view.fxml";
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
                Scene dashboardScene = new Scene(fxmlLoader.load(), 1000, 800);
                Stage stage = (Stage) totalLabel.getScene().getWindow();
                stage.setTitle("Dashboard");
                stage.setScene(dashboardScene);
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }
        SessionManager.setNavigatedFromDashboard(false);

        // Product table setup
        listProductIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        listProductNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        listProductPriceColumn.setCellValueFactory(new PropertyValueFactory<>("salePrice"));
        listProductStockColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        productListTable.setItems(productList);

        // Cart table setup
        productIdColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getProduct().getId()).asObject());
        productNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getProduct().getName()));
        priceColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getProduct().getSalePrice()).asObject());
        // Custom cell factory for quantity column with + and - buttons
        quantityColumn.setCellFactory(col -> new TableCell<CartItem, Integer>() {
            private final HBox box = new HBox(4);
            private final Button minus = new Button("-");
            private final Label qtyLabel = new Label();
            private final Button plus = new Button("+");
            {
                box.getChildren().addAll(minus, qtyLabel, plus);
                minus.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        getTableView().refresh();
                        updateTotal();
                    }
                });
                plus.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    if (item.getQuantity() < item.getMaxAvailable()) {
                        item.setQuantity(item.getQuantity() + 1);
                        getTableView().refresh();
                        updateTotal();
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Out of Stock", "Cannot add more. Only " + item.getMaxAvailable() + " in stock.");
                    }
                });
            }
            @Override
            protected void updateItem(Integer qty, boolean empty) {
                super.updateItem(qty, empty);
                if (empty || qty == null) {
                    setGraphic(null);
                } else {
                    qtyLabel.setText(String.valueOf(qty));
                    setGraphic(box);
                }
            }
        });
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        // Remove button column
        removeColumn.setCellFactory(col -> new TableCell<CartItem, Void>() {
            private final Button removeBtn = new Button("Remove");
            {
                removeBtn.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    cartItems.remove(item);
                    getTableView().refresh();
                    updateTotal();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : removeBtn);
            }
        });
        totalColumn.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().getLineTotal()).asObject());
        totalColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : moneyFmt.format(item));
            }
        });
        productsTable.setItems(cartItems);

        productSubject.addObserver(new ExpiryObserver());
        productSubject.addObserver(new StockObserver());

        loadProducts();
        updateTotal();
        initializeProductSearch();
        initializeDragAndDrop();
        draftButton.setOnAction(this::onDraftButton);
        refreshDraftsList();
    }

    private void loadProducts() {
        productList.setAll(productDAO.getAllProducts());
    }

    private void addToCart(Product p, int qty) {
        productSubject.notifyObservers(p);
        Optional<CartItem> existing = cartItems.stream().filter(ci -> ci.getProduct().getId() == p.getId()).findFirst();
        if (existing.isPresent()) {
            CartItem ci = existing.get();
            int newQty = Math.min(ci.getMaxAvailable(), ci.getQuantity() + qty);
            ci.setQuantity(newQty);
        } else {
            cartItems.add(new CartItem(p, qty));
        }
        productsTable.refresh();
        updateTotal();
    }

    private void updateTotal() {
        double total = cartItems.stream().mapToDouble(CartItem::getLineTotal).sum();
        totalLabel.setText(moneyFmt.format(total));
    }

    @FXML
    private void handleClearCart(ActionEvent event) {
        cartItems.clear();
        productsTable.refresh();
        updateTotal();
    }

    @FXML
    private void handleCompleteSale(ActionEvent event) {
        if (selectedCustomerId == null) {
            showAlert(Alert.AlertType.WARNING, "Select Customer", "Please find a customer by phone before completing the sale.");
            return;
        }
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", "Please add at least one product to the cart.");
            return;
        }
        int userId = SessionManager.getUserId();
        if (userId <= 0) {
            showAlert(Alert.AlertType.ERROR, "User Not Set", "Logged-in user not found. Please log in again.");
            return;
        }
        // Build a map productId -> quantity
        Map<Integer, Integer> items = new HashMap<>();
        for (CartItem ci : cartItems) {
            items.put(ci.getProduct().getId(), ci.getQuantity());
        }
        try {
            int orderId = orderDAO.createOrder(selectedCustomerId, userId, items);
            if (orderId > 0) {
                // Show choice dialog FIRST (Strategy pattern)
                ChoiceDialog<String> dialog = new ChoiceDialog<>("Download", java.util.Arrays.asList("Download", "Print", "Email"));
                dialog.setTitle("Choose Action");
                dialog.setHeaderText("Select what to do after checkout:");
                dialog.setContentText("Action:");
                java.util.Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String choice = result.get();
                    ActionContext context = new ActionContext();
                    switch (choice) {
                        case "Download":
                            context.setStrategy(new DownloadStrategy());
                            break;
                        case "Print":
                            context.setStrategy(new PrintStrategy());
                            break;
                        case "Email":
                            context.setStrategy(new EmailStrategy());
                            break;
                    }
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    java.io.PrintStream ps = new java.io.PrintStream(baos);
                    java.io.PrintStream old = System.out;
                    System.setOut(ps);
                    context.executeStrategy();
                    System.out.flush();
                    System.setOut(old);
                    String message = baos.toString().trim();
                    showAlert(javafx.scene.control.Alert.AlertType.INFORMATION, choice + " Success", message);
                }

                // Generate invoice PDF
                try {
                    generateInvoicePDF(orderId, selectedCustomerId, cartItems);
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Invoice Error", "Could not generate invoice PDF: " + e.getMessage());
                }
                // Remove draft for this customer (if exists)
                String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
                if (!phone.isEmpty()) {
                    SaleDraftCaretaker.removeDraft(phone);
                    refreshDraftsList();
                }
                // Reset state
                cartItems.clear();
                productsTable.refresh();
                updateTotal();
                // Reload products to reflect new stock
                loadProducts();

                showAlert(Alert.AlertType.INFORMATION, "Sale Completed", "Order #" + orderId + " has been created.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Sale Failed", "Could not create the order. Please try again.");
            }
        } catch (IllegalStateException ex) {
            showAlert(Alert.AlertType.WARNING, "Stock Error", ex.getMessage());
            // Reload products to refresh stock
            loadProducts();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + ex.getMessage());
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            String userType = SessionManager.getUserType();
            String fxml = "employee".equalsIgnoreCase(userType) ? "/com/example/demo/employee-dashboard-view.fxml" : "/com/example/demo/admin-dashboard-view.fxml";
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
            Scene dashboardScene = new Scene(fxmlLoader.load(), 1000, 800);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Dashboard");
            stage.setScene(dashboardScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add event handler methods for FXML
    @FXML
    private void onSearchCustomer(ActionEvent event) {
        handleFindCustomer(event);
    }

    @FXML
    private void onAddProduct(ActionEvent event) {
        Product selected = productListTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Product Selected", "Please select a product from the list.");
            return;
        }
        addToCart(selected, 1);
    }

    @FXML
    private void onCompleteSale(ActionEvent event) {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", "Please add products to the cart before completing the sale.");
            return;
        }
        // Ensure a customer is selected before checkout
        if (selectedCustomerId == null) {
            showAlert(Alert.AlertType.WARNING, "Select Customer", "Please find a customer by phone before completing the sale.");
            return;
        }

        List<SaleReportRow> saleReportRows = new ArrayList<>(); // Collect sale report data

        // Prepare order details only; DO NOT mutate product quantities here
        Map<Integer, Integer> orderDetails = new HashMap<>(); // productId -> quantity
        for (CartItem item : cartItems) {
            Product product = item.getProduct();

            // Add to sale report rows
            saleReportRows.add(new SaleReportRow(
                product.getName(),
                item.getQuantity(),
                item.getLineTotal(),
                java.time.LocalDate.now()
            ));

            // Add to order details
            orderDetails.put(product.getId(), item.getQuantity());
        }

        // Insert order into the database
        int userId = SessionManager.getUserId();
        if (userId <= 0) {
            showAlert(Alert.AlertType.ERROR, "User Not Set", "Logged-in user not found. Please log in again.");
            return;
        }
        int customerId = selectedCustomerId != null ? selectedCustomerId : 0;
        try {
            int orderId = orderDAO.createOrder(customerId, userId, orderDetails);
            if (orderId > 0) {
                // Strategy choice dialog FIRST
                ChoiceDialog<String> dialog = new ChoiceDialog<>("Download", java.util.Arrays.asList("Download", "Print", "Email"));
                dialog.setTitle("Choose Action");
                dialog.setHeaderText("Select what to do after checkout:");
                dialog.setContentText("Action:");
                java.util.Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String choice = result.get();
                    ActionContext context = new ActionContext();
                    switch (choice) {
                        case "Download":
                            context.setStrategy(new DownloadStrategy());
                            break;
                        case "Print":
                            context.setStrategy(new PrintStrategy());
                            break;
                        case "Email":
                            context.setStrategy(new EmailStrategy());
                            break;
                    }
                    // Capture the message
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    java.io.PrintStream ps = new java.io.PrintStream(baos);
                    java.io.PrintStream old = System.out;
                    System.setOut(ps);
                    context.executeStrategy();
                    System.out.flush();
                    System.setOut(old);
                    String message = baos.toString().trim();
                    showAlert(javafx.scene.control.Alert.AlertType.INFORMATION, choice + " Success", message);
                }

                // Generate invoice PDF
                try {
                    generateInvoicePDF(orderId, customerId, cartItems);
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Invoice Error", "Could not generate invoice PDF: " + e.getMessage());
                }

                // Clear the cart and refresh the product list
                cartItems.clear();
                productsTable.refresh();
                updateTotal();
                loadProducts(); // Refresh the product list

                // Save the sale report rows (example: to a database or file)
                saveSaleReport(saleReportRows);

                // Remove draft for this customer (if exists)
                String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
                if (!phone.isEmpty()) {
                    SaleDraftCaretaker.removeDraft(phone);
                    refreshDraftsList();
                }

                showAlert(Alert.AlertType.INFORMATION, "Sale Completed", "Order #" + orderId + " has been created.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Sale Failed", "Could not create the order. Please try again.");
                return;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Insufficient Stock", e.getMessage());
            loadProducts(); // Refresh the product list
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while saving the order: " + e.getMessage());
        }
    }

    private void saveSaleReport(List<SaleReportRow> saleReportRows) {
        // Example implementation: Save to a database or file
        for (SaleReportRow row : saleReportRows) {
            System.out.println("Saving sale report row: " + row);
            // Add logic to save to a database or file
        }
    }

    @FXML
    private void onDraftButton(ActionEvent event) {
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        if (phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Phone Required", "Please enter a phone number to save a draft.");
            return;
        }
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Cart", "Cannot save an empty cart as draft.");
            return;
        }
        Customer customer = customerDAO.findByPhone(phone);
        if (customer == null) {
            showAlert(Alert.AlertType.WARNING, "Customer Not Found", "Please search and select a valid customer before saving a draft.");
            return;
        }
        java.util.List<Product> products = new java.util.ArrayList<>();
        for (CartItem ci : cartItems) {
            for (int i = 0; i < ci.getQuantity(); i++) {
                products.add(ci.getProduct());
            }
        }
        draftOriginator.setState(products, customer);
        SaleDraftMemento memento = draftOriginator.saveToMemento();
        com.example.demo.patterns.memento.SaleDraftCaretaker.saveDraft(phone, memento);
        showAlert(Alert.AlertType.INFORMATION, "Draft Saved", "Draft saved for customer: " + customer.getName());
        refreshDraftsList();
    }

    @FXML
    private void handleFindCustomer(ActionEvent event) {
        String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
        if (phone.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Phone Required", "Please enter a phone number.");
            return;
        }
        var customer = customerDAO.findByPhone(phone);
        if (customer == null) {
            selectedCustomerId = null;
            nameField.setText("");
            emailField.setText("");
            showAlert(Alert.AlertType.INFORMATION, "Not Found", "No customer found for this phone. Please add the customer first.");
        } else {
            selectedCustomerId = customer.getId();
            nameField.setText(customer.getName());
            emailField.setText(customer.getEmail());
            // Check for draft
            SaleDraftMemento draft = SaleDraftCaretaker.getDraft(phone);
            if (draft != null) {
                draftOriginator.restoreFromMemento(draft);
                cartItems.clear();
                for (Product p : draftOriginator.getCartItems()) {
                    addToCart(p, 1);
                }
                showAlert(Alert.AlertType.INFORMATION, "Draft Restored", "Draft restored for this customer.");
                refreshDraftsList();
            }
        }
    }

    private int safeSpinnerValue(Spinner<Integer> spinner, int defVal) {
        try {
            return spinner.getValue();
        } catch (Exception e) {
            return defVal;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void initializeProductSearch() {
        productIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                productListTable.setItems(productList);
                return;
            }
            String search = newVal.trim().toLowerCase();
            ObservableList<Product> filtered = FXCollections.observableArrayList();
            for (Product p : productList) {
                if (String.valueOf(p.getId()).equals(search)
                        || p.getName().toLowerCase().contains(search)
                        || (p.getCategory() != null && p.getCategory().toLowerCase().contains(search))) {
                    filtered.add(p);
                }
            }
            productListTable.setItems(filtered);
        });
    }

    @FXML
    private void initializeDragAndDrop() {
        // Drag detected on product list row
        productListTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Product product = row.getItem();
                    Dragboard db = row.startDragAndDrop(TransferMode.COPY);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(String.valueOf(product.getId()));
                    db.setContent(content);
                    event.consume();
                }
            });
            return row;
        });

        // Drag over cart table
        productsTable.setOnDragOver(event -> {
            if (event.getGestureSource() != productsTable && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        // Drop on cart table
        productsTable.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String idStr = db.getString();
                try {
                    int productId = Integer.parseInt(idStr);
                    Product product = productList.stream().filter(p -> p.getId() == productId).findFirst().orElse(null);
                    if (product != null) {
                        if (product.getQuantity() > 0) {
                            addToCart(product, 1);
                            success = true;
                        } else {
                            showAlert(Alert.AlertType.WARNING, "Out of Stock", "Cannot add product '" + product.getName() + "' to cart. It is out of stock.");
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void onRestoreDraft(ActionEvent actionEvent) {
    }

    public static class CartItem {
        private final Product product;
        private final IntegerProperty quantity = new SimpleIntegerProperty(1);

        public CartItem(Product product, int qty) {
            this.product = product;
            setQuantity(qty);
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int q) { this.quantity.set(Math.max(1, Math.min(q, getMaxAvailable()))); }
        public IntegerProperty quantityProperty() { return quantity; }
        public double getLineTotal() { return product.getSalePrice() * getQuantity(); }
        public int getMaxAvailable() { return Math.max(0, product.getQuantity()); }
    }

    private void generateInvoicePDF(int orderId, int customerId, List<CartItem> cartItems) throws Exception {
        // Fetch customer info (assuming you have a method or DAO for this)
        Customer customer = customerDAO.getCustomerById(customerId);
        String fileName = "Invoice_" + orderId + ".pdf";
        String filePath = System.getProperty("user.home") + java.io.File.separator + fileName;
        com.lowagie.text.Document document = new com.lowagie.text.Document();
        com.lowagie.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(filePath));
        document.open();
        document.add(new com.lowagie.text.Paragraph("INVOICE"));
        document.add(new com.lowagie.text.Paragraph("Order ID: " + orderId));
        document.add(new com.lowagie.text.Paragraph("Customer: " + (customer != null ? customer.getName() : "Unknown")));
        document.add(new com.lowagie.text.Paragraph("Email: " + (customer != null ? customer.getEmail() : "")));
        document.add(new com.lowagie.text.Paragraph("----------------------------------------"));
        com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(4);
        table.addCell("Product");
        table.addCell("Quantity");
        table.addCell("Price");
        table.addCell("Total");
        double grandTotal = 0;
        for (CartItem item : cartItems) {
            table.addCell(item.getProduct().getName());
            table.addCell(String.valueOf(item.getQuantity()));
            table.addCell(String.format("%.2f", item.getProduct().getSalePrice()));
            double total = item.getQuantity() * item.getProduct().getSalePrice();
            table.addCell(String.format("%.2f", total));
            grandTotal += total;
        }
        document.add(table);
        document.add(new com.lowagie.text.Paragraph("----------------------------------------"));
        document.add(new com.lowagie.text.Paragraph("Total: $" + String.format("%.2f", grandTotal)));
        document.close();
        // Open the PDF
        java.awt.Desktop.getDesktop().open(new java.io.File(filePath));
    }


    // Method to reduce product quantity when selected
    private final Set<Integer> reducedProductIds = new HashSet<>(); // Track products with reduced quantities

    private void reduceProductQuantity(Product product, int quantity) {
        if (reducedProductIds.contains(product.getId())) {
            return; // Skip if the product's quantity has already been reduced
        }
        reducedProductIds.add(product.getId());
        List<Product> productList = new ArrayList<>();
        productList.add(product);
        originator.setState(productList, null); // Assuming no customer is needed here
        SaleDraftCaretaker.saveDraft("temp", originator.saveToMemento()); // Using saveDraft instead of saveState
        product.setQuantity(product.getQuantity() - quantity);
    }

    // Method to restore product quantity when navigating back
    private void undoProductReduction() {
        SaleDraftMemento memento = SaleDraftCaretaker.getDraft("temp"); // Using getDraft instead of getLastSavedState
        if (memento != null) {
            originator.restoreFromMemento(memento); // Using restoreFromMemento instead of restore
        }
    }

    // Call this method when a product is selected
    public void onProductSelected(Product product, int quantity) {
        // reduceProductQuantity(product, quantity); // Removed to prevent double reduction
    }

    // Call this method when navigating back
    public void onBackPressed() {
        undoProductReduction();
        reducedProductIds.clear(); // Clear the record when navigating back
    }

    // Call this method on checkout to finalize changes
    public void onCheckout() {
        SaleDraftCaretaker.clearAllDrafts(); // Clear all saved drafts as the sale is finalized
    }
}
