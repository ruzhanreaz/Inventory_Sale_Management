module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;
    requires java.naming;
    requires java.desktop;

    requires jbcrypt;
    requires com.github.librepdf.openpdf;

    opens com.example.demo to javafx.fxml;
    opens com.example.demo.models to javafx.base;
    exports com.example.demo;
    exports com.example.demo.controllers;

    opens com.example.demo.controllers to javafx.fxml;
}