module com.example.projet_semestre6 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires itextpdf;


    opens com.example.projet_semestre6 to javafx.fxml;
    exports com.example.projet_semestre6;
}