package cc.pachuchi.garagesalemanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.prefs.Preferences;

import it.auties.whatsapp.api.QrHandler;
import it.auties.whatsapp.api.WebHistoryLength;
import it.auties.whatsapp.api.Whatsapp;

public class ChatBot {
    private static ChatBot instance;
    private String recipientName;
    private Path qrCodePath;

    private int interval;
    private int startTimeHour;
    private int startTimeMinutes;
    private int endTimeHour;
    private int endTimeMinutes;
    private boolean connected;
    private boolean enabled;

    private static final String PREF_NODE = "cc.pachuchi.garagesalemanager.ChatBot";
    private static final String KEY_RECIPIENT_NAME = "recipientName";
    private static final String KEY_QR_CODE_PATH = "qrCodePath";
    private static final String KEY_INTERVAL = "interval";
    private static final String KEY_START_TIME_HOUR = "startTimeHour";
    private static final String KEY_START_TIME_MINUTES = "startTimeMinutes";
    private static final String KEY_END_TIME_HOUR = "endTimeHour";
    private static final String KEY_END_TIME_MINUTES = "endTimeMinutes";

    private Preferences preferences;
    private Whatsapp api;

    private ChatBot() {
        preferences = Preferences.userRoot().node(PREF_NODE);
        loadState();
    }

    public static ChatBot getInstance() {
        if (instance == null) {
            instance = new ChatBot();
        }
        return instance;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public Path getQrCodePath() {
        return qrCodePath;
    }

    public void setQrCodePath(Path qrCodePath) {
        this.qrCodePath = qrCodePath;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getStartTimeHour() {
        return startTimeHour;
    }

    public void setStartTimeHour(int startTimeHour) {
        this.startTimeHour = startTimeHour;
    }

    public int getStartTimeMinutes() {
        return startTimeMinutes;
    }

    public void setStartTimeMinutes(int startTimeMinutes) {
        this.startTimeMinutes = startTimeMinutes;
    }

    public int getEndTimeHour() {
        return endTimeHour;
    }

    public void setEndTimeHour(int endTimeHour) {
        this.endTimeHour = endTimeHour;
    }

    public int getEndTimeMinutes() {
        return endTimeMinutes;
    }

    public void setEndTimeMinutes(int endTimeMinutes) {
        this.endTimeMinutes = endTimeMinutes;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Whatsapp getApi() {
        return api;
    }

    public void initializeWhatsappApi() {
        // Set the directory to store the QR code image
        Path qrDir = Paths.get(System.getProperty("user.home"), "garageSaleManager");
        try {
            Files.createDirectories(qrDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create directory for QR code", e);
        }

        api = Whatsapp.webBuilder()
                .lastConnection()
                .name("Garage Sale Manager")
                .historyLength(WebHistoryLength.standard())
                .unregistered(QrHandler.toFile(file -> {
                    // Save the QR code to the specified directory
                    qrCodePath = qrDir.resolve("qrcode.png");
                    try {
                        Files.copy(file, qrCodePath, StandardCopyOption.REPLACE_EXISTING);
                        saveState(); // Save the path to Preferences
                        System.out.println("Saved new QR Image");
                    } catch (IOException e) {
                        throw new RuntimeException("Could not save QR code image", e);
                    }
                }))
                .addLoggedInListener(api -> {
                    this.connected = true;
                    System.out.printf("Connected: %s%n", api.store().privacySettings());
                })
                .addDisconnectedListener(reason -> {
                    this.connected = false;
                    System.out.printf("Disconnected: %s%n", reason);
                })
                .addNewChatMessageListener(message -> System.out.printf("New message: %s%n", message.toJson()))
                .connect()
                .join();
    }

    public void saveState() {
        preferences.put(KEY_RECIPIENT_NAME, recipientName != null ? recipientName : "");
        preferences.put(KEY_QR_CODE_PATH, qrCodePath != null ? qrCodePath.toString() : "");
        preferences.putInt(KEY_INTERVAL, interval);
        preferences.putInt(KEY_START_TIME_HOUR, startTimeHour);
        preferences.putInt(KEY_START_TIME_MINUTES, startTimeMinutes);
        preferences.putInt(KEY_END_TIME_HOUR, endTimeHour);
        preferences.putInt(KEY_END_TIME_MINUTES, endTimeMinutes);
    }

    private void loadState() {
        recipientName = preferences.get(KEY_RECIPIENT_NAME, "");
        String pathString = preferences.get(KEY_QR_CODE_PATH, "");
        if (!pathString.isEmpty()) {
            qrCodePath = Paths.get(pathString);
        }
        interval = preferences.getInt(KEY_INTERVAL, 5);
        startTimeHour = preferences.getInt(KEY_START_TIME_HOUR, 0);
        startTimeMinutes = preferences.getInt(KEY_START_TIME_MINUTES, 0);
        endTimeHour = preferences.getInt(KEY_END_TIME_HOUR, 0);
        endTimeMinutes = preferences.getInt(KEY_END_TIME_MINUTES, 0);
    }
}