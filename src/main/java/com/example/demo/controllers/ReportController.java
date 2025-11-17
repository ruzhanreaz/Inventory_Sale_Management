package com.example.demo.controllers;

import com.example.demo.dao.OrderDAO;
import com.example.demo.models.SaleReportRow;
import com.example.demo.patterns.strategy.*;
import com.example.demo.patterns.bridge.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ReportController {
    @FXML
    private TableView<SaleReportRow> salesTable;
    @FXML
    private Button generateReportButton;
    @FXML
    private TableColumn<SaleReportRow, String> productColumn;
    @FXML
    private TableColumn<SaleReportRow, String> categoryColumn;
    @FXML
    private TableColumn<SaleReportRow, Integer> quantityColumn;
    @FXML
    private TableColumn<SaleReportRow, Double> priceColumn;
    @FXML
    private TableColumn<SaleReportRow, String> dateColumn;

    private final OrderDAO orderDAO = new OrderDAO();
    private final ObservableList<SaleReportRow> reportData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        productColumn.setCellValueFactory(new PropertyValueFactory<>("product"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("profit"));

        priceColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(String.format(java.util.Locale.US, "%.2f", item));
                }
            }
        });
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                return new javafx.beans.property.SimpleStringProperty("");
            }
        });
        loadReportData();
    }

    private void loadReportData() {
        reportData.setAll(orderDAO.getSalesReportRows());
        salesTable.setItems(reportData);
        System.out.println("Loaded report rows: " + reportData.size()); // Debug output
    }

    @FXML
    private void onGenerateReport() {
        java.util.List<String> reportTypes = java.util.Arrays.asList("Weekly", "Monthly");
        java.util.List<String> formats = java.util.Arrays.asList("CSV", "PDF");
        javafx.scene.control.ChoiceDialog<String> typeDialog = new javafx.scene.control.ChoiceDialog<>("Weekly", reportTypes);
        typeDialog.setTitle("Report Type");
        typeDialog.setHeaderText("Select report type:");
        typeDialog.setContentText("Type:");
        java.util.Optional<String> typeResult = typeDialog.showAndWait();
        if (typeResult.isEmpty()) return;
        String reportType = typeResult.get();

        javafx.scene.control.ChoiceDialog<String> formatDialog = new javafx.scene.control.ChoiceDialog<>("CSV", formats);
        formatDialog.setTitle("Report Format");
        formatDialog.setHeaderText("Select report format:");
        formatDialog.setContentText("Format:");
        java.util.Optional<String> formatResult = formatDialog.showAndWait();
        if (formatResult.isEmpty()) return;
        String format = formatResult.get();

        // Prompt for year
        int currentYear = java.time.LocalDate.now().getYear();
        javafx.scene.control.TextInputDialog yearDialog = new javafx.scene.control.TextInputDialog(String.valueOf(currentYear));
        yearDialog.setTitle("Select Year");
        yearDialog.setHeaderText("Enter the year for the report:");
        yearDialog.setContentText("Year:");
        java.util.Optional<String> yearResult = yearDialog.showAndWait();
        if (yearResult.isEmpty()) return;
        int year;
        try {
            year = Integer.parseInt(yearResult.get());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Invalid year entered.");
            return;
        }

        int period;
        if (reportType.equals("Monthly")) {
            // Prompt for month
            javafx.scene.control.ChoiceDialog<Integer> monthDialog = new javafx.scene.control.ChoiceDialog<>(java.time.LocalDate.now().getMonthValue(), java.util.stream.IntStream.rangeClosed(1, 12).boxed().toList());
            monthDialog.setTitle("Select Month");
            monthDialog.setHeaderText("Select the month for the report:");
            monthDialog.setContentText("Month:");
            java.util.Optional<Integer> monthResult = monthDialog.showAndWait();
            if (monthResult.isEmpty()) return;
            period = monthResult.get();
        } else {
            // Prompt for week
            int maxWeek = java.time.LocalDate.of(year, 12, 28).get(java.time.temporal.WeekFields.ISO.weekOfYear());
            javafx.scene.control.ChoiceDialog<Integer> weekDialog = new javafx.scene.control.ChoiceDialog<>(1, java.util.stream.IntStream.rangeClosed(1, maxWeek).boxed().toList());
            weekDialog.setTitle("Select Week");
            weekDialog.setHeaderText("Select the week number for the report:");
            weekDialog.setContentText("Week:");
            java.util.Optional<Integer> weekResult = weekDialog.showAndWait();
            if (weekResult.isEmpty()) return;
            period = weekResult.get();
        }

        ReportExporter exporter = format.equals("CSV") ? new CsvReportExporter() : new PdfReportExporter();
        ReportGenerator generator = reportType.equals("Weekly") ? new WeeklyReportGenerator(exporter) : new MonthlyReportGenerator(exporter);

        String userHome = System.getProperty("user.home");
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String ext = format.toLowerCase();
        String fileName = String.format("%s_report_%s.%s", reportType.toLowerCase(), timestamp, ext);
        String filePath = java.nio.file.Paths.get(userHome, "Desktop", fileName).toString();

        try {
            generator.generateReport(reportData, filePath, year, period);
            showAlert(Alert.AlertType.INFORMATION, "Report Generated", String.format("%s %s report saved to:\n%s", reportType, format, filePath));
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Report Error", "Failed to generate report: " + e.getMessage());
        }
    }

    @FXML
    private void onBack(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/demo/admin-dashboard-view.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 800));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the admin dashboard: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
