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
    @FXML
    private ComboBox<String> itemHour;
    @FXML
    private ComboBox<String> itemMinutes;

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


        // Populate hours
        for (int i = 0; i < 24; i++) {
            String hour = String.format("%02d", i);
            itemHour.getItems().add(hour);
        }

        // Populate minutes
        for (int i = 0; i < 60; i += 5) { // Every 5 minutes
            String minute = String.format("%02d", i);
            itemMinutes.getItems().add(minute);
        }
    }

    private void editReservation() {
        String buyer = itemBuyer.getText();
        String place = itemPlace.getText();
        LocalDate date = itemDate.getValue();
        int itemHourValue = Integer.parseInt(itemHour.getValue());
        int itemMinutesValue = Integer.parseInt(itemMinutes.getValue());

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
        selectedItem.getReservation().setHour(itemHourValue);   // Save the hour
        selectedItem.getReservation().setMinute(itemMinutesValue); // Save the minute

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
        itemDate.setValue(selectedItem.getReservation().getDate());

        // Set the hour and minute in the ComboBox
        if (selectedItem.getReservation().getHour() >= 0) {
            itemHour.setValue(String.format("%02d", selectedItem.getReservation().getHour()));
        }
        if (selectedItem.getReservation().getMinute() >= 0) {
            itemMinutes.setValue(String.format("%02d", selectedItem.getReservation().getMinute()));
        }
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