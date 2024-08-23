package cc.pachuchi.garagesalemanager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.time.LocalDate;

public class reserveItemDialogController {

    private Storage storage;
    private Item selectedItem;

    @FXML
    private Label itemName;
    @FXML
    private Label itemID;
    @FXML
    private TextField itemBuyer;
    @FXML
    private TextArea itemPlace;
    @FXML
    private DatePicker itemDate;
    @FXML
    private Label itemBuyerWarning;
    @FXML
    private Label itemPlaceWarning;
    @FXML
    private Label itemDateWarning;
    @FXML
    private Button cancelButton;
    @FXML
    private Button reserveButton;
    @FXML
    private Button undoReservationButton;

    public void initialize() {
        reserveButton.setOnAction(event -> editReservation());
        cancelButton.setOnAction(event -> cancelAction());
        undoReservationButton.setOnAction(event -> undoReservation());

        // Disable earlier dates than the present date in the DatePicker
        itemDate.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #EEEEEE;");
                        }
                    }
                };
            }
        });
    }

    private void editReservation() {
        String buyer = itemBuyer.getText();
        String place = itemPlace.getText();
        LocalDate date = itemDate.getValue();

        if (buyer.isEmpty()) {
            itemBuyerWarning.setText("Can't be empty");
            Effects.fadeOutText(itemBuyerWarning, 1.5);
            return;
        }

        if (place.isEmpty()) {
            itemPlaceWarning.setText("Can't be empty");
            Effects.fadeOutText(itemPlaceWarning, 1.5);
            return;
        }


        selectedItem.getReservation().setReserved(true);
        selectedItem.getReservation().setBuyer(buyer);
        selectedItem.getReservation().setPlace(place);
        selectedItem.getReservation().setDate(date);
        storage.saveItems();
        Stage stage = (Stage) reserveButton.getScene().getWindow();
        stage.close();
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public void setItem(Item selectedItem) {
        this.selectedItem = selectedItem;
        itemName.setText(selectedItem.getName());
        itemID.setText(selectedItem.getId());
        itemBuyer.setText(selectedItem.getReservation().getBuyer());
        itemPlace.setText(selectedItem.getReservation().getPlace());

    }

    public void cancelAction() {
        Stage stage = (Stage) reserveButton.getScene().getWindow();
        stage.close();
    }

    public void undoReservation(){
        selectedItem.getReservation().undoReservation();
        storage.saveItems();
        Stage stage = (Stage) undoReservationButton.getScene().getWindow();
        stage.close();
    }


    public void updateItemDetails(){
        if (selectedItem.getReservation() != null){
            itemName.setText(selectedItem.getName());
        }
    }
}