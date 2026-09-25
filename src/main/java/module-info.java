module com.project.lexerjava {

    requires javafx.controls;
    requires javafx.fxml;

    opens com.project.lexerjava to javafx.fxml;

    exports com.project.lexerjava;
}