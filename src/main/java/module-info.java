module com.desarrollo.controlequiposlabg3 {
    requires javafx.controls;
    requires javafx.fxml;

    // Persistencia y conexión con PostgreSQL
    requires java.sql;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires org.postgresql.jdbc;

    exports com.desarrollo.controlequiposlabg3;
    opens com.desarrollo.controlequiposlabg3 to javafx.fxml;
    opens com.desarrollo.controlequiposlabg3.controller to javafx.fxml;

    // Entidades utilizadas por Hibernate y la tabla JavaFX
    opens com.desarrollo.controlequiposlabg3.model
            to org.hibernate.orm.core, javafx.base;
}