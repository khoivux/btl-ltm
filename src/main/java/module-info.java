module btl.ltm {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires kotlin.stdlib;
    requires javafx.media;
    requires javafx.graphics;

    exports client;
    exports client.controller;
    exports model;
    exports server;
    exports server.dao;
    
    // Cho phép JavaFX FXML truy cập vào controller để inject @FXML fields
    opens client.controller to javafx.fxml;
    exports server.controller;
}