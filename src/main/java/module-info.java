module com.example.kamieniolom2_pw {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;

    opens com.example.kamieniolom2_pw to javafx.fxml;
    exports com.example.kamieniolom2_pw;
}