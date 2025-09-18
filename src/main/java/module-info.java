module com.example.bloodcellanalysis {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.bloodcellanalysis to javafx.fxml;
    exports com.example.bloodcellanalysis;
}