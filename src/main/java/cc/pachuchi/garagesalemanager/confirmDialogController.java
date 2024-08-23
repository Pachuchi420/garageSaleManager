package cc.pachuchi.garagesalemanager;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.fxml.FXML;

public class confirmDialogController {
    private ConfirmAction confirmAction;
    private Storage storage;

    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label dialogMsg;

    public void initialize() {
        cancelButton.setOnAction(event -> closeDialog());
        confirmButton.setOnAction(event -> {
            if (confirmAction != null) {
                confirmAction.perform();
            }
            closeDialog();
        });
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public void setConfirmAction(ConfirmAction confirmAction) {
        this.confirmAction = confirmAction;
    }

    public void setDialogMessage(String message) {
        dialogMsg.setText(message);
    }

    private void closeDialog() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }
}
