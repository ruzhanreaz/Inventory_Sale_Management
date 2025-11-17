package com.example.demo.patterns.bridge;

import com.example.demo.models.SaleReportRow;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.DayOfWeek;

public class WeeklyReportGenerator extends ReportGenerator {
    public WeeklyReportGenerator(ReportExporter exporter) {
        super(exporter);
    }
    @Override
    public void generateReport(List<SaleReportRow> data, String filePath, int year, int week) {
        java.time.temporal.WeekFields weekFields = java.time.temporal.WeekFields.ISO;
        List<SaleReportRow> filtered = data.stream()
            .filter(row -> {
                java.time.LocalDate date = row.getDate();
                return date != null && date.getYear() == year && date.get(weekFields.weekOfYear()) == week;
            })
            .collect(java.util.stream.Collectors.toList());
        exporter.export(filtered, filePath);
    }
}
