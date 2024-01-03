module com.spg {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires org.yaml.snakeyaml;
    requires org.apache.logging.log4j;

    opens com.spg;
    opens media;
    exports com.spg;
}