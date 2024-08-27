package cc.pachuchi.garagesalemanager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

public class whatsAppDialogController {
    private Storage storage;
    private Thread qrGenerationThread;
    private Thread statusCheckThread;
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

    private final String id = "5215520815848@c.us";
    private final String msg = "Hello World!";

    public void initialize() {
        storage = new Storage();  // Initialize storage

        loadChatBotSettings();  // Load settings when the dialog is opened

        cancelButton.setOnAction(event -> cancelAction());
        testMsg.setOnAction(event -> {
            try {
                sendTestMessage(msg, id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

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

        // Load and monitor QR Code
        startQRGenerationThread();

        // Start a thread to check the connection status
        startStatusCheckThread();
    }

    private void loadChatBotSettings() {
        ChatBot chatBot = storage.loadChatBotSettings();

        recipientID.setText(chatBot.getRecipientContact());
        intervalMinutes.setText(String.valueOf(chatBot.getIntervalMinutes()));

        // Handle startTime
        String startTime = chatBot.getStartTime();
        if (startTime != null && !startTime.isEmpty()) {
            String[] startTimeParts = startTime.split(":");
            startTimeHour.setValue(startTimeParts[0]);
            startTimeMinute.setValue(startTimeParts[1]);
        } else {
            // Provide default values or handle the case when startTime is null
            startTimeHour.setValue("00");
            startTimeMinute.setValue("00");
        }

        // Handle endTime
        String endTime = chatBot.getEndTime();
        if (endTime != null && !endTime.isEmpty()) {
            String[] endTimeParts = endTime.split(":");
            endTimeHour.setValue(endTimeParts[0]);
            endTimeMinute.setValue(endTimeParts[1]);
        } else {
            // Provide default values or handle the case when endTime is null
            endTimeHour.setValue("00");
            endTimeMinute.setValue("00");
        }

        activeToggleButton.setSelected(chatBot.isActive());

        if (chatBot.isActive()) {
            activeToggleButton.setText("Enabled");
            activeToggleButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        } else {
            activeToggleButton.setText("Disabled");
            activeToggleButton.setStyle("-fx-background-color: #bc1919; -fx-text-fill: white;");
        }
    }

    private void saveChatBotSettings() {
        ChatBot chatBot = ChatBot.getInstance();

        chatBot.setRecipientID(recipientID.getText());
        chatBot.setIntervalMinutes(Integer.parseInt(intervalMinutes.getText()));
        chatBot.setStartTime(startTimeHour.getValue() + ":" + startTimeMinute.getValue());
        chatBot.setEndTime(endTimeHour.getValue() + ":" + endTimeMinute.getValue());
        chatBot.setActive(activeToggleButton.isSelected());

        storage.saveChatBotSettings(chatBot);  // Save settings to storage

        System.out.println("Settings saved to ChatBot:");
        System.out.println("Recipient ID: " + chatBot.getRecipientContact());
        System.out.println("Interval Minutes: " + chatBot.getIntervalMinutes());
        System.out.println("Start Time: " + chatBot.getStartTime());
        System.out.println("End Time: " + chatBot.getEndTime());
        System.out.println("Active: " + chatBot.isActive());
    }

    public void cancelAction() {
        // Indicate that the dialog is closed
        isDialogOpen.set(false);

        // Close the dialog window
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void startQRGenerationThread() {
        qrGenerationThread = new Thread(() -> {
            while (isDialogOpen.get()) {
                try {
                    // Generate QR Code image
                    Image qrCodeImage = generateQRCode();

                    // Update ImageView on the JavaFX Application Thread
                    Platform.runLater(() -> whatsappQRCode.setImage(qrCodeImage));

                    // Wait before regenerating the QR code (if needed)
                    Thread.sleep(5000); // Adjust the delay as necessary
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        qrGenerationThread.setDaemon(true);
        qrGenerationThread.start();
    }

    private Image generateQRCode() {
        File file = new File("whatsapp/qrcode.png");
        if (file.exists()) {
            return new Image(file.toURI().toString());
        } else {
            // Optionally, return a placeholder image if the QR code is not yet available
            return new Image("file:placeholder.png"); // Replace with a valid placeholder image path
        }
    }

    private void startStatusCheckThread() {
        statusCheckThread = new Thread(() -> {
            while (isDialogOpen.get()) {
                try {
                    File statusFile = new File("whatsapp/status.txt");
                    if (statusFile.exists()) {
                        String status = new String(Files.readAllBytes(Paths.get(statusFile.toURI())));
                        if (status.trim().equals("connected")) {
                            Platform.runLater(() -> {
                                logStatus.setText("Connected");
                                logStatus.setStyle("-fx-text-fill: #02a402;");
                            });

                            break;
                        }
                    }
                    Thread.sleep(1000); // Check every second
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        statusCheckThread.setDaemon(true);
        statusCheckThread.start();
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    private void sendTestMessage(String message, String recipientId) throws Exception {
        URL url = new URL("http://127.0.0.1:3000/sendTest");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        String jsonInputString = "{\"customMessage\": \"" + message + "\", \"recipientId\": \"" + recipientId + "\"}";

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = conn.getResponseCode();
        System.out.println("Response code: " + code);
    }
}