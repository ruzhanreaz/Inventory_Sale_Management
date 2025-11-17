package com.example.demo.patterns.bridge;

import com.example.demo.models.SaleReportRow;
import java.util.List;

public interface ReportExporter {
    void export(List<SaleReportRow> data, String filePath);
}

