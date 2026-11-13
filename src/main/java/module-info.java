module com.desarrollo.controlequiposlabg3 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.desarrollo.controlequiposlabg3 to javafx.fxml;
    exports com.desarrollo.controlequiposlabg3;
}