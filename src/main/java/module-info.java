module com.example.conwaylife {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.conwaylife to javafx.fxml;
    exports com.example.conwaylife;
}