package cc.pachuchi.garagesalemanager;

import it.auties.whatsapp.model.message.standard.ImageMessageSimpleBuilder;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import org.kordamp.ikonli.javafx.FontIcon;
import javafx.scene.paint.Color;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class listItemsViewController {

    private Storage storage;
    private AtomicBoolean isDialogOpen = new AtomicBoolean(true);
    private boolean wasBotEnabled = false; // Track the previous state of the bot

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private TableView<Item> tableView;
    @FXML
    public TableColumn itemDateAddedColumn;
    @FXML
    private TableColumn<Item, String> itemIDColumn;
    @FXML
    private TableColumn<Item, String> itemNameColumn;
    @FXML
    private TableColumn<Item, String> itemDescriptionColumn;
    @FXML
    private TableColumn<Item, Number> itemPriceColumn;
    @FXML
    private TableColumn<Item, Boolean> itemReservedColumn;
    @FXML
    private TableColumn<Item, Boolean> itemSoldColumn;
    @FXML
    private TableColumn itemReservationDateColumn;
    @FXML
    private ImageView itemImageView;
    @FXML
    private Button reserveItemButton;
    @FXML
    private Button detailItemButton;
    @FXML
    private Button whatsAppButton;
    @FXML
    private Button addItemButton;
    @FXML
    private Button editItemButton;
    @FXML
    private Button removeItemButton;
    @FXML
    private Label warningMsg;
    @FXML
    private Label logStatus;
    @FXML
    private Button setSoldButton;
    private ChatBot localBot;

    public void initialize() {
        localBot = ChatBot.getInstance(); // Get the singleton instance
        localBot.initializeWhatsappApi();
        startChecking();

        addItemButton.setOnAction(event -> openAddItemDialog());
        itemDateAddedColumn.setCellValueFactory(new PropertyValueFactory<Item, String>("date"));
        itemReservationDateColumn.setCellValueFactory(new PropertyValueFactory<Item, LocalDate>("reservationDate"));
        itemIDColumn.setCellValueFactory(new PropertyValueFactory<Item, String>("id"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<Item, String>("name"));
        itemDescriptionColumn.setCellValueFactory(new PropertyValueFactory<Item, String>("description"));
        itemPriceColumn.setCellValueFactory(new PropertyValueFactory<Item, Number>("price"));
        itemPriceColumn.setCellFactory(column -> new TableCell<Item, Number>() {
            @Override
            protected void updateItem(Number price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    Item currentItem = getTableView().getItems().get(getIndex());
                    String currency = currentItem.getCurrency(); // Assuming Item has a getCurrency() method
                    setText(price + " (" + currency + ")");
                }
            }
        });

        itemReservedColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isReserved()));
        itemReservedColumn.setCellFactory(col -> new TableCell<Item, Boolean>() {
            @Override
            protected void updateItem(Boolean reserved, boolean empty) {
                super.updateItem(reserved, empty);

                if (empty || reserved == null) {
                    setText(null);
                } else {
                    setText(reserved ? "Yes" : "No");
                }
            }
        });

        itemSoldColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().getSold()));
        itemSoldColumn.setCellFactory(col -> new TableCell<Item, Boolean>() {
            @Override
            protected void updateItem(Boolean sold, boolean empty) {
                super.updateItem(sold, empty);

                if (empty || sold == null) {
                    setText(null);
                } else {
                    setText(sold ? "Yes" : "No");
                }
            }
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                displayItemImage(newValue);
            } else {
                itemImageView.setImage(null); // Optionally, clear or set a default image when no item is selected
            }
        });

        editItemButton.setOnAction(event -> {
            Item selectedItem = tableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                openEditItemDialog(selectedItem);
            } else {
                warningMsg.setText("No item selected.");
                Effects.fadeOutText(warningMsg, 1.5);
            }
        });


        whatsAppButton.setOnAction(event -> openWhatsAppDialog());

        removeItemButton.setOnAction(event -> {
            Item selectedItem = tableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                openRemoveItemDialog(selectedItem);
            } else {
                warningMsg.setText("No item selected.");
                Effects.fadeOutText(warningMsg, 1.5);
            }
        });

        reserveItemButton.setOnAction(event -> {
            Item selectedItem = tableView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                // No item is selected
                warningMsg.setText("No item selected.");
                Effects.fadeOutText(warningMsg, 1.5);
            } else {
                // An item is selected and it's not reserved
                openReserveItemDialog(selectedItem);
            }
        });

        detailItemButton.setOnAction(event -> {
            Item selectedItem = tableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                openDetailItemDialog(selectedItem);
            } else {
                warningMsg.setText("No item selected.");
                Effects.fadeOutText(warningMsg, 1.5);
            }
        });

        setSoldButton.setOnAction(event -> {
            Item selectedItem = tableView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && !selectedItem.getSold()) {
                selectedItem.setSold(true);
                storage.saveItems();
                setSoldButton.setStyle("-fx-icon-color: #bc1919"); // Corrected color code
                setSoldButton.applyCss();
                refreshTableView();
            } else if (selectedItem != null && selectedItem.getSold()) {
                selectedItem.setSold(false);
                setSoldButton.setStyle("-fx-icon-color: #45932b"); // Green for unsold state
                setSoldButton.applyCss();
                storage.saveItems();
                refreshTableView();
            } else {
                warningMsg.setText("No item selected.");
                Effects.fadeOutText(warningMsg, 1.5);
            }
        });
    }




    private void openDetailItemDialog(Item selectedItem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("detailItemDialog.fxml"));
            Parent dialogRoot = loader.load();

            detailItemDialogController dialogController = loader.getController();
            dialogController.setStorage(storage);
            dialogController.setItem(selectedItem);
            Stage dialogStage = new Stage();
            dialogStage.setResizable(false);
            dialogStage.setTitle("Add Item");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            refreshTableView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
        refreshTableView();
    }

    private void refreshTableView() {
        tableView.setItems(storage.getItems()); // Assuming getItems() returns an ObservableList<Item>
        tableView.refresh();
    }

    private void openAddItemDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("addItemDialog.fxml"));
            Parent dialogRoot = loader.load();

            addItemDialogController dialogController = loader.getController();
            dialogController.setStorage(storage);
            Stage dialogStage = new Stage();
            dialogStage.setResizable(false);
            dialogStage.setTitle("Add Item");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            refreshTableView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openEditItemDialog(Item selectedItem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("editItemDialog.fxml"));
            Parent dialogRoot = loader.load();

            editItemDialogController dialogController = loader.getController();
            dialogController.setStorage(storage);
            dialogController.setItem(selectedItem);
            Stage dialogStage = new Stage();
            dialogStage.setResizable(false);
            dialogStage.setTitle("Edit Item");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();
            refreshTableView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openRemoveItemDialog(Item selectedItem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("confirmDialog.fxml"));
            Parent dialogRoot = loader.load();
            confirmDialogController dialogController = loader.getController();

            // Set the message for the confirmation dialog
            dialogController.setDialogMessage("Are you sure you want to delete this item?");

            // Set the action to perform upon confirmation
            dialogController.setConfirmAction(() -> {
                storage.removeItem(selectedItem); // Perform deletion
                refreshTableView(); // Assuming you have a method to refresh the table view
            });

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Confirm Delete");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openReserveItemDialog(Item selectedItem) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("reserveItemDialog.fxml"));
            Parent dialogRoot = loader.load();

            reserveItemDialogController dialogController = loader.getController();
            dialogController.setStorage(storage);
            dialogController.setItem(selectedItem);
            Stage dialogStage = new Stage();
            dialogStage.setResizable(false);
            dialogStage.setTitle("Reserve Item");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();
            refreshTableView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void displayItemImage(Item item) {
        if (item != null && item.getImageData() != null) {
            // Assuming getImageData() returns a byte array and you have a method to convert it to an Image
            Image image = new Image(new ByteArrayInputStream(item.getImageData()));
            itemImageView.setImage(image);
        } else {
            itemImageView.setImage(null); // Clear the image or set a default placeholder image
        }
    }

    private void openWhatsAppDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("whatsAppDialog.fxml"));
            Parent dialogRoot = loader.load();

            whatsAppDialogController whatsAppDialogController = loader.getController();
            whatsAppDialogController.setStorage(storage);
            Stage dialogStage = new Stage();
            dialogStage.setResizable(false);
            dialogStage.setTitle("WhatsApp Settings");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(dialogRoot));
            dialogStage.showAndWait();

            refreshTableView();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateConnectionStatus() {
        FontIcon statusIcon = new FontIcon(); // Create a FontIcon object

        if (localBot.isConnected()) {
            statusIcon.setIconLiteral("fas-circle");  // Set icon to checkmark for connected status
            statusIcon.setIconColor(Color.rgb(69,147,43));   // Set the color to green
            logStatus.setGraphic(statusIcon);       // Set the graphic to the label
        } else {
            statusIcon.setIconLiteral("far-circle");  // Set icon to cross for disconnected status
            statusIcon.setIconColor(Color.rgb(188,25 ,25));     // Set the color to red
            logStatus.setGraphic(statusIcon);       // Set the graphic to the label
        }
    }

    public void startChecking() {
        Thread checkerThread = new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                    updateConnectionStatus();
                    boolean isBotEnabled = localBot.isEnabled();

                    if (isBotEnabled && !wasBotEnabled) {
                        // The bot has just been enabled, start sending messages
                        startSendingMessages();
                        wasBotEnabled = true;
                    } else if (!isBotEnabled && wasBotEnabled) {
                        // The bot has just been disabled
                        wasBotEnabled = false;
                    }
                });
                try {
                    Thread.sleep(1000); // Sleep for 1 second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        checkerThread.setDaemon(true);
        checkerThread.start();
    }

    public void startSendingMessages() {
        Thread messageSenderThread = new Thread(() -> {
            while (true) {
                try {
                    LocalTime currentTime = LocalTime.now();
                    LocalTime startTime = LocalTime.of(localBot.getStartTimeHour(), localBot.getStartTimeMinutes());
                    LocalTime endTime = LocalTime.of(localBot.getEndTimeHour(), localBot.getEndTimeMinutes());

                    if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)) {
                        System.out.println("Within time limits, starting client messaging...");

                        if (!localBot.isConnected()) {
                            System.out.println("Not connected. Attempting to reconnect...");
                            localBot.initializeWhatsappApi(); // Reinitialize the API to attempt reconnection
                        }

                        List<Item> items = storage.getItems();
                        if (items.isEmpty()) {
                            System.out.println("No items available to send.");
                        } else {
                            Random random = new Random();
                            Item randomItem = items.get(random.nextInt(items.size()));

                            // Setup for messaging
                            var chat = localBot.getApi().store()
                                    .findChatByName(localBot.getRecipientName())
                                    .orElseThrow(() -> new NoSuchElementException("Contact doesn't exist!"));

                            var image = new ImageMessageSimpleBuilder()
                                    .media(randomItem.getImageData())
                                    .caption(randomItem.getName() + '\n' + randomItem.getDescription() + '\n' + randomItem.getPrice())
                                    .build();
                            localBot.getApi().sendMessage(chat, image);
                            System.out.println("Sent item: " + randomItem.getName());
                        }

                        // Respect the interval for sending messages
                        Thread.sleep(localBot.getInterval() * 60 * 1000);
                    } else {
                        // If outside the time window, just sleep for a short time before checking again
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Failed to send message.");
                }
            }
        });

        messageSenderThread.setDaemon(true);
        messageSenderThread.start();
    }
}