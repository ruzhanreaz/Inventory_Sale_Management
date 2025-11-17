package com.example.demo.patterns.bridge;

import com.example.demo.models.SaleReportRow;
import java.util.List;

public abstract class ReportGenerator {
    protected ReportExporter exporter;
    public ReportGenerator(ReportExporter exporter) {
        this.exporter = exporter;
    }
    public abstract void generateReport(List<SaleReportRow> data, String filePath, int year, int period);
}