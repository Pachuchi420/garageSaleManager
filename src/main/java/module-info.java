module cc.pachuchi.garagesalemanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.prefs;
    requires it.auties.cobalt;


    opens cc.pachuchi.garagesalemanager to javafx.fxml;
    exports cc.pachuchi.garagesalemanager;
}