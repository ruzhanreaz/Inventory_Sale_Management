package com.example.demo.patterns.observer;

import com.example.demo.models.Product;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class StockObserver implements ProductObserver {
    @Override
    public void update(Product product) {
        if (product.getQuantity() == 0) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Out of Stock");
                alert.setHeaderText(null);
                alert.setContentText("Warning: Product '" + product.getName() + "' (ID: " + product.getId() + ") is out of stock!");
                alert.showAndWait();
            });
        }
    }
}
