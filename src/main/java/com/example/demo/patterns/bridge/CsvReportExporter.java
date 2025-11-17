package com.example.demo.patterns.bridge;

import com.example.demo.models.SaleReportRow;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class CsvReportExporter implements ReportExporter {
    @Override
    public void export(List<SaleReportRow> data, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Product,Category,Quantity,Profit,Date\n");
            for (SaleReportRow row : data) {
                writer.write(String.format(Locale.US, "%s,%s,%d,%.2f,%s\n",
                        row.getProduct(),
                        row.getCategory(),
                        row.getQuantity(),
                        row.getProfit(),
                        row.getDate() != null ? row.getDate().toString() : ""
                ));
            }
            writer.write("\nSummary by Product and Category\n");
            writer.write("Product,Category,Total Quantity,Total Profit\n");
            Map<String, double[]> summary = new HashMap<>();
            double grandTotalProfit = 0;
            int grandTotalQuantity = 0;
            for (SaleReportRow row : data) {
                String key = row.getProduct() + "||" + row.getCategory();
                summary.putIfAbsent(key, new double[]{0, 0});
                double[] arr = summary.get(key);
                arr[0] += row.getQuantity();
                arr[1] += row.getProfit();
                grandTotalQuantity += row.getQuantity();
                grandTotalProfit += row.getProfit();
            }
            for (String key : summary.keySet()) {
                double[] arr = summary.get(key);
                String[] parts = key.split("\\\\|\\\\|", 2);
                String product = parts.length > 0 ? parts[0] : "";
                String category = parts.length > 1 ? parts[1] : "";
                writer.write(String.format(Locale.US, "%s,%s,%d,%.2f\n", product, category, (int)arr[0], arr[1]));
            }
            writer.write(String.format(Locale.US, "Total,%s,%d,%.2f\n", "", grandTotalQuantity, grandTotalProfit));
        } catch (IOException e) {
            throw new RuntimeException("Failed to export CSV: " + e.getMessage(), e);
        }
    }
}
