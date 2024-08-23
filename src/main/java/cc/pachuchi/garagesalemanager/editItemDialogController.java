package cc.pachuchi.garagesalemanager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;


public class editItemDialogController {
    private Storage storage;

    private byte[] imageData;
    private Item selectedItem;
    @FXML
    private TextField nameField;
    @FXML
    private Label nameWarning;
    @FXML
    private TextArea descriptionField;
    @FXML
    private Label descriptionWarning;
    @FXML
    private TextField priceField;
    @FXML
    private Label priceWarning;
    @FXML
    private RadioButton currencyUSD;
    @FXML
    private RadioButton currencyMXN;
    @FXML
    private RadioButton currencyEUR;
    @FXML
    private Label currencyWarning;
    @FXML
    private Button selectImageButton;
    @FXML
    private Label pathToImage;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    public void setItem(Item item) {
        this.selectedItem = item;
        if (item != null) {
            nameField.setText(item.getName());
            descriptionField.setText(item.getDescription());
            priceField.setText(String.valueOf(item.getPrice()));

            switch (item.getCurrency()) {
                case "USD":
                    currencyUSD.setSelected(true);
                    break;
                case "MXN":
                    currencyMXN.setSelected(true);
                    break;
                case "EUR":
                    currencyEUR.setSelected(true);
                    break;
                default:
                    break;
            }
        }
    }

    public void initialize() {
        selectImageButton.setOnAction(event -> selectImage());
        confirmButton.setOnAction(event -> editItem(selectedItem));
        cancelButton.setOnAction(event-> cancelAction());

    }
    public void setStorage(Storage storage){
        this.storage = storage;
    }

    public void selectImage() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image files (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Image image = new Image(fis);

                // Read and store image data
                imageData = Files.readAllBytes(file.toPath());
            } catch (IOException ex) {
                ex.printStackTrace();
                // Handle error (e.g., show an error dialog)
            }
        }

    }

    public boolean containsNonNumeric(String str) {
        // Check if the string contains at least one letter
        return str.matches(".*[a-zA-Z]+.*");
    }
    public void editItem(Item selectedItem){
        String name = nameField.getText();
        String description = descriptionField.getText();
        String priceAsString = priceField.getText();
        int price = 0;

        if (name.isEmpty()){
            nameWarning.setText("Can't be empty");
            Effects.fadeOutText(nameWarning, 1.5);
            return;
        }

        if (description.isEmpty()){
            descriptionWarning.setText("Can't be empty");
            Effects.fadeOutText(descriptionWarning, 1.5);
            return;
        }

        if (priceAsString.isEmpty()){
            priceWarning.setText("Price cannot be empty");
            Effects.fadeOutText(priceWarning, 1.5);
            return;
        }


        if (!containsNonNumeric(priceAsString)){
            price = Integer.parseInt(priceAsString);
            if (price <= 0){
                priceWarning.setText("Price can only be positive values.");
                Effects.fadeOutText(priceWarning, 1.5);
                return;
            }
        } else {
            priceWarning.setText("Price can only be digits.");
            Effects.fadeOutText(priceWarning, 1.5);
            return;
        }

        String currency = null;
        if (currencyUSD.isSelected()) {
            currency = "USD";
        } else if (currencyMXN.isSelected()) {
            currency = "MXN";
        } else if (currencyEUR.isSelected()) {
            currency = "EUR";
        }

        selectedItem.setName(name);
        selectedItem.setDescription(description);
        selectedItem.setPrice(price);
        selectedItem.setCurrency(currency);
        selectedItem.setImageData(imageData);
        storage.saveItems();
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }

    public void cancelAction(){
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }
}
