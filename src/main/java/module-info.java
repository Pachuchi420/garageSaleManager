module cc.pachuchi.garagesalemanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.prefs;
    requires it.auties.cobalt;
    requires org.kordamp.ikonli.javafx;


    opens cc.pachuchi.garagesalemanager to javafx.fxml;
    exports cc.pachuchi.garagesalemanager;
}