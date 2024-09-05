package cc.pachuchi.garagesalemanager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

public class whatsAppDialogController {
    private Storage storage;
    private AtomicBoolean isDialogOpen = new AtomicBoolean(true);

    @FXML
    private Button cancelButton;
    @FXML
    private Label logStatus;
    @FXML
    private ImageView whatsappQRCode;
    @FXML
    private Button testMsg;
    @FXML
    private TextField recipientID;
    @FXML
    private TextField intervalMinutes;
    @FXML
    private ComboBox<String> startTimeHour;
    @FXML
    private ComboBox<String> startTimeMinute;
    @FXML
    private ComboBox<String> endTimeHour;
    @FXML
    private ComboBox<String> endTimeMinute;
    @FXML
    private ToggleButton activeToggleButton;
    @FXML
    private Button saveSettings;
    @FXML
    private Button refreshQRButton;
    @FXML
    private Button deleteUserDataButton;
    private ChatBot localBot;


    public void initialize() {
        localBot = ChatBot.getInstance();
        startChecking();

        storage = new Storage();  // Initialize storage
        loadChatBotSettings();

        cancelButton.setOnAction(event -> cancelAction());

        activeToggleButton.setOnAction(event -> {
            if (activeToggleButton.isSelected()) {
                activeToggleButton.setText("Enabled");
                activeToggleButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            } else {
                activeToggleButton.setText("Disabled");
                activeToggleButton.setStyle("-fx-background-color: #bc1919; -fx-text-fill: white;");
            }
        });

        saveSettings.setOnAction(event -> saveChatBotSettings());
        testMsg.setOnAction(actionEvent -> sendTestMsg());
        refreshQRButton.setOnAction(actionEvent -> refreshQR());
        deleteUserDataButton.setOnAction(actionEvent -> deleteUserData());

        // Populate hours
        for (int i = 0; i < 24; i++) {
            String hour = String.format("%02d", i);
            startTimeHour.getItems().add(hour);
            endTimeHour.getItems().add(hour);
        }

        // Populate minutes
        for (int i = 0; i < 60; i += 5) { // Every 5 minutes
            String minute = String.format("%02d", i);
            startTimeMinute.getItems().add(minute);
            endTimeMinute.getItems().add(minute);
        }

        // Start checking for connection status and QR code updates
        startChecking();
    }

    private void deleteUserData() {
        localBot.getApi().logout();
    }

    public void cancelAction() {
        // Indicate that the dialog is closed
        isDialogOpen.set(false);

        // Close the dialog window
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public void saveChatBotSettings() {
        try {
            // Retrieve data from UI components
            String recipient = recipientID.getText();
            int interval = Integer.parseInt(intervalMinutes.getText());
            int startTimeHourValue = Integer.parseInt(startTimeHour.getValue());
            int startTimeMinutesValue = Integer.parseInt(startTimeMinute.getValue());
            int endTimeHourValue = Integer.parseInt(endTimeHour.getValue());
            int endTimeMinutesValue = Integer.parseInt(endTimeMinute.getValue());
            boolean isActive = activeToggleButton.isSelected();

            // Check if the chat exists
            if (localBot.getApi().store().findChatByName(recipient).isPresent()) {
                localBot.setRecipientName(recipient);
                System.out.println("Found recipient '" + recipient + "' with ID: " + localBot.getApi().store().findChatByName(recipient).get());
            } else {
                // If the recipient is not found, throw an exception
                throw new NoSuchElementException("Recipient '" + recipient + "' not found");
            }

            // Set other ChatBot properties
            localBot.setInterval(interval);
            localBot.setStartTimeHour(startTimeHourValue);
            localBot.setStartTimeMinutes(startTimeMinutesValue);
            localBot.setEndTimeHour(endTimeHourValue);
            localBot.setEndTimeMinutes(endTimeMinutesValue);
            localBot.setEnabled(isActive);

            localBot.saveState();

            // Debug/confirmation messages
            System.out.println("Contact saved: " + recipient);
            System.out.println("Interval saved: " + interval + " minutes");
            System.out.println("Start Time saved: " + startTimeHourValue + ":" + startTimeMinutesValue);
            System.out.println("End Time saved: " + endTimeHourValue + ":" + endTimeMinutesValue);
            System.out.println("Chat Bot Active: " + (isActive ? "Yes" : "No"));

        } catch (NumberFormatException e) {
            // Handle case where intervalMinutes is not a valid integer
            System.out.println("Invalid interval or time values.");
            showAlert("Invalid input", "Please enter valid numbers for the interval and time fields.");
        } catch (NoSuchElementException e) {
            // Handle case where the recipient was not found
            System.out.println(e.getMessage());
            showAlert("Recipient Not Found", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }


    public void loadChatBotSettings() {
        try {
            // Load the data from the localBot object
            String recipient = localBot.getRecipientName();
            int interval = localBot.getInterval();
            int startTimeHourValue = localBot.getStartTimeHour();
            int startTimeMinutesValue = localBot.getStartTimeMinutes();
            int endTimeHourValue = localBot.getEndTimeHour();
            int endTimeMinutesValue = localBot.getEndTimeMinutes();
            boolean isActive = localBot.isEnabled();

            // Set the data into UI components
            recipientID.setText(recipient);
            intervalMinutes.setText(String.valueOf(interval));
            startTimeHour.setValue(String.format("%02d", startTimeHourValue));
            startTimeMinute.setValue(String.format("%02d", startTimeMinutesValue));
            endTimeHour.setValue(String.format("%02d", endTimeHourValue));
            endTimeMinute.setValue(String.format("%02d", endTimeMinutesValue));
            activeToggleButton.setSelected(isActive);

            // Update the toggle button text and style based on its state
            if (isActive) {
                activeToggleButton.setText("Enabled");
                activeToggleButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
            } else {
                activeToggleButton.setText("Disabled");
                activeToggleButton.setStyle("-fx-background-color: #bc1919; -fx-text-fill: white;");
            }

            // Debug/confirmation messages
            System.out.println("Settings loaded: ");
            System.out.println("Contact: " + recipient);
            System.out.println("Interval: " + interval + " minutes");
            System.out.println("Start Time: " + startTimeHourValue + ":" + startTimeMinutesValue);
            System.out.println("End Time: " + endTimeHourValue + ":" + endTimeMinutesValue);
            System.out.println("Chat Bot Active: " + (isActive ? "Yes" : "No"));

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Load Error", "Failed to load chatbot settings. Please check the logs for more details.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateConnectionStatus() {
        if (localBot.isConnected()) {
            logStatus.setText("Connected");
            logStatus.setStyle("-fx-text-fill: green;");
            refreshQRButton.setDisable(true);
        } else {
            logStatus.setText("Disconnected");
            logStatus.setStyle("-fx-text-fill: red;");
            refreshQRButton.setDisable(false);
        }
    }

    private void setQrCodeImageView() {
        Path qrCodePath = localBot.getQrCodePath();
        if (qrCodePath != null && Files.exists(qrCodePath)) {
            if (localBot.isConnected()) {
                whatsappQRCode.setImage(null);
            } else {
                Image qrImage = new Image(qrCodePath.toUri().toString());
                whatsappQRCode.setImage(qrImage);
            }
        }
    }

    public void startChecking() {
        Thread checkerThread = new Thread(() -> {
            while (isDialogOpen.get()) {
                Platform.runLater(() -> {
                    updateConnectionStatus();
                    setQrCodeImageView();
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

    private void sendTestMsg() {
        var chat = localBot.getApi().store()
                .findChatByName(localBot.getRecipientName())
                .orElseThrow(() -> new NoSuchElementException("Chat not found..."));
        localBot.getApi().sendMessage(chat, "Test message for Garage Sale Manager");

    }


    private void refreshQR() {
        if (!localBot.isConnected()) {
            localBot.initializeWhatsappApi(); // This will generate a new QR code and update the path
            setQrCodeImageView(); // Refresh the ImageView with the new QR code
        } else {
            showAlert("Cannot Refresh QR Code", "You are already connected.");
        }
    }



}