package com.example.demo.patterns.observer;

import com.example.demo.models.Product;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.time.LocalDate;

public class ExpiryObserver implements ProductObserver {
    @Override
    public void update(Product product) {
        LocalDate expiry = product.getExpiryDate();
        if (expiry != null && expiry.isBefore(LocalDate.now())) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Product Expired");
                alert.setHeaderText(null);
                alert.setContentText("Warning: Product '" + product.getName() + "' (ID: " + product.getId() + ") is expired!");
                alert.showAndWait();
            });
        }
    }
}

