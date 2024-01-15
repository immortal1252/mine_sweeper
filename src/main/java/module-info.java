module com.spg {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires org.yaml.snakeyaml;
    requires org.apache.logging.log4j;
    requires com.opencsv;
    opens com.spg;
    opens media;
    opens newmedia;
    exports com.spg;
    exports com.spg.bean;
    opens com.spg.bean;
    exports com.spg.dao;
    opens com.spg.dao;
    exports com.spg.service;
    opens com.spg.service;
}