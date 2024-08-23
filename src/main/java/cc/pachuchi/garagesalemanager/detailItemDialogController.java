package cc.pachuchi.garagesalemanager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class detailItemDialogController {

    private Storage storage;
    private Item selectedItem;

    @FXML
    private Button cancelButton;
    @FXML
    private Label itemName;
    @FXML
    private Label itemID;
    @FXML
    private Label itemDescription;
    @FXML
    private Label itemPrice;
    @FXML
    private Label itemCurrency;
    @FXML
    private Label itemDateAdded;
    @FXML
    private Label itemBuyerLabel;
    @FXML
    private Label itemPlaceLabel;
    @FXML
    private Label itemDateReservation;
    @FXML
    private Label itemTimeReservation;
    @FXML
    private Label itemReserved;
    @FXML
    private Label itemSold;




    public void initialize() {
        cancelButton.setOnAction(event-> cancelAction());

    }



    public void setStorage(Storage storage){
        this.storage = storage;
    }

    public void setItem(Item selectedItem) {
        this.selectedItem = selectedItem;
        updateItemDetails();

    }

    private void updateItemDetails() {
        if (selectedItem != null) {
            itemName.setText(selectedItem.getName());
            itemID.setText(selectedItem.getId());
            itemDescription.setText(selectedItem.getDescription());
            itemPrice.setText(Integer.toString(selectedItem.getPrice()));
            itemCurrency.setText(selectedItem.getCurrency());
            itemDateAdded.setText(selectedItem.getDate().toString());

            // Check if the reservation is null or if reservation details are null
            Reservation reservation = selectedItem.getReservation();
            boolean reserved = reservation.getReserved();
            if (!reserved) {
                String buyer = "N/A";
                String place = "N/A";
                String dateReservation = "N/A";
                itemBuyerLabel.setText(buyer);
                itemPlaceLabel.setText(place);
                itemDateReservation.setText(dateReservation);
                itemReserved.setText("No");
            } else {
                itemBuyerLabel.setText(reservation.getBuyer());
                itemPlaceLabel.setText(reservation.getPlace());
                itemDateReservation.setText(String.valueOf(reservation.getDate()));
                itemReserved.setText("Yes");
            }

            // Check and display if the item is sold
            String sold = null;
            if (!selectedItem.getSold()) {
                sold = "No";
            } else {
                sold = "Yes";
            }
            itemSold.setText(sold);
        }
    }


    public void cancelAction(){
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
