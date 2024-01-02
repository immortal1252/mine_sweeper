module com.spg {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires org.yaml.snakeyaml;


    opens com.spg;
    opens media;
    exports com.spg;
}