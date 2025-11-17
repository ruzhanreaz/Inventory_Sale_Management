package com.example.demo.patterns.bridge;

import com.example.demo.models.SaleReportRow;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

public class PdfReportExporter implements ReportExporter {
    @Override
    public void export(List<SaleReportRow> data, String filePath) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            document.add(new Paragraph("Sales Report"));
            // Table with Product, Category, Quantity, Profit, Date
            PdfPTable table = new PdfPTable(5);
            table.addCell("Product");
            table.addCell("Category");
            table.addCell("Quantity");
            table.addCell("Profit");
            table.addCell("Date");
            for (SaleReportRow row : data) {
                table.addCell(row.getProduct());
                table.addCell(row.getCategory());
                table.addCell(String.valueOf(row.getQuantity()));
                table.addCell(String.format(Locale.US, "%.2f", row.getProfit()));
                table.addCell(row.getDate() != null ? row.getDate().toString() : "");
            }
            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Summary by Product and Category"));
            PdfPTable summaryTable = new PdfPTable(4);
            summaryTable.addCell("Product");
            summaryTable.addCell("Category");
            summaryTable.addCell("Total Quantity");
            summaryTable.addCell("Total Profit");
            java.util.Map<String, double[]> summary = new java.util.HashMap<>();
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
                summaryTable.addCell(product);
                summaryTable.addCell(category);
                summaryTable.addCell(String.valueOf((int)arr[0]));
                summaryTable.addCell(String.format(Locale.US, "%.2f", arr[1]));
            }
            summaryTable.addCell("Total");
            summaryTable.addCell("");
            summaryTable.addCell(String.valueOf(grandTotalQuantity));
            summaryTable.addCell(String.format(Locale.US, "%.2f", grandTotalProfit));
            document.add(summaryTable);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export PDF: " + e.getMessage(), e);
        } finally {
            document.close();
        }
    }
}
