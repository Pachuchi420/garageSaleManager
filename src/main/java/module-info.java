module cc.pachuchi.garagesalemanager {
    requires javafx.controls;
    requires javafx.fxml;


    opens cc.pachuchi.garagesalemanager to javafx.fxml;
    exports cc.pachuchi.garagesalemanager;
}