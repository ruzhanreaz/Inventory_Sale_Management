package com.example.demo.patterns.command;

import com.example.demo.utils.DBConnector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupDatabaseCommand implements Command {

    private final String databasePath;
    private final String backupDirectory;

    public BackupDatabaseCommand(String databasePath, String backupDirectory) {
        this.databasePath = databasePath;
        this.backupDirectory = backupDirectory;
    }

    @Override
    public void execute() {
        try {
            // Generate a timestamped backup file name
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path source = Path.of(databasePath);
            Path target = Path.of(backupDirectory, "backup_" + timestamp + ".db");

            // Perform the file copy
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Database backup created at: " + target);
        } catch (IOException e) {
            System.err.println("Failed to backup the database: " + e.getMessage());
        }
    }
}
