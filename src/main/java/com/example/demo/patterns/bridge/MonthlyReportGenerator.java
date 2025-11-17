package com.example.demo.patterns.bridge;

import com.example.demo.models.SaleReportRow;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;

public class MonthlyReportGenerator extends ReportGenerator {
    public MonthlyReportGenerator(ReportExporter exporter) {
        super(exporter);
    }

    @Override
    public void generateReport(List<SaleReportRow> data, String filePath, int year, int month) {
        List<SaleReportRow> filtered = data.stream()
                .filter(row -> row.getDate() != null && row.getDate().getYear() == year && row.getDate().getMonthValue() == month)
                .collect(java.util.stream.Collectors.toList());
        exporter.export(filtered, filePath);
    }
}
