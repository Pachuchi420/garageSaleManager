package cc.pachuchi.garagesalemanager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ProgramLauncher extends Application {
    private Storage storage = new Storage();
    private Process scriptProcess;
    private ChatBot chatBot = ChatBot.getInstance();
    private Thread messageSendingThread;
    private boolean isMessageSendingThreadRunning = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            setDisconnected();
            runWhatsAppScript();

            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("listItemsView.fxml")));
            Parent root = loader.load();

            listItemsViewController controller = loader.getController();
            controller.setStorage(storage);

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Garage Sale Manager");
            stage.setResizable(true);
            stage.show();

            startMessageCheckingThread();

            stage.setOnCloseRequest(event -> {
                stopWhatsAppScript();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        stopWhatsAppScript();
        stopMessageSendingThread();
        Platform.exit();
    }

    private void runWhatsAppScript() {
        Thread scriptThread = new Thread(() -> {
            try {
                File scriptFile = new File("whatsapp/main.js").getCanonicalFile();

                if (!scriptFile.exists()) {
                    System.out.println("Script file not found: " + scriptFile.getAbsolutePath());
                    return;
                }

                ProcessBuilder processBuilder = new ProcessBuilder("node", scriptFile.getAbsolutePath());
                processBuilder.directory(scriptFile.getParentFile());

                scriptProcess = processBuilder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(scriptProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("Node.js: " + line);
                    }
                }

                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(scriptProcess.getErrorStream()))) {
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        System.err.println(errorLine);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        scriptThread.setDaemon(true);
        scriptThread.start();
    }

    private void stopWhatsAppScript() {
        if (scriptProcess != null && scriptProcess.isAlive()) {
            scriptProcess.destroy();
            try {
                scriptProcess.waitFor();
                System.out.println("Node.js script terminated.");
            } catch (InterruptedException e) {
                System.err.println("Failed to wait for Node.js script termination.");
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void setDisconnected() {
        try {
            File scriptFile = new File("whatsapp/status.txt").getCanonicalFile();
            FileWriter writer = new FileWriter(scriptFile);
            writer.write("disconnected");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startMessageCheckingThread() {
        Thread checkingThread = new Thread(() -> {
            while (true) {
                if (chatBot.isActive() && isConnected()) {
                    if (!isMessageSendingThreadRunning) {
                        startMessageSendingThread();
                    }
                } else {
                    stopMessageSendingThread();
                    System.out.println("ChatBot is not active or WhatsApp is not connected.");
                }
                try {
                    Thread.sleep(1000); // Sleep for 1 second between checks
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        checkingThread.setDaemon(true);
        checkingThread.start();
    }

    private boolean isConnected() {
        try {
            String status = new String(Files.readAllBytes(Paths.get("whatsapp/status.txt")));
            return status.trim().equalsIgnoreCase("connected");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void startMessageSendingThread() {
        messageSendingThread = new Thread(() -> {
            isMessageSendingThreadRunning = true;
            while (true) {
                if (!chatBot.isActive() || !isConnected() || !chatBot.isWithinTimeFrame()) {
                    break;
                }
                try {
                    sendRandomItemMessage(chatBot.getRecipientContact());
                    Thread.sleep(chatBot.getIntervalInMilliseconds());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            isMessageSendingThreadRunning = false;
        });

        messageSendingThread.setDaemon(true);
        messageSendingThread.start();
    }

    public void stopMessageSendingThread() {
        if (messageSendingThread != null && messageSendingThread.isAlive()) {
            messageSendingThread.interrupt();
            isMessageSendingThreadRunning = false;
        }
    }

    private void sendRandomItemMessage(String recipientId) throws Exception {
        List<Item> items = storage.getItems();
        Random rand = new Random();
        if (items.size() > 0) {
            boolean sent = false;
            int attempts = 0;
            while (!sent && attempts < items.size()) {
                int randomIndex = rand.nextInt(items.size());
                Item randomItem = items.get(randomIndex);

                // Check if the item is not reserved, not sold, and hasn't been uploaded today
                if (!randomItem.isReserved() && !randomItem.getSold() && !randomItem.uploadedToday()) {
                    String base64Image = Base64.getEncoder().encodeToString(randomItem.getImageData());

                    String jsonInputString = "{"
                            + "\"name\": \"" + randomItem.getName() + "\", "
                            + "\"description\": \"" + randomItem.getDescription() + "\", "
                            + "\"price\": \"" + randomItem.getPrice() + " " + randomItem.getCurrency() + "\", "
                            + "\"imageData\": \"" + base64Image + "\", "
                            + "\"imageMimeType\": \"" + "image/png" + "\", "
                            + "\"imageFilename\": \"" + "itemImage.png" + "\", "
                            + "\"recipientId\": \"" + recipientId + "\""
                            + "}";

                    sendItemMessage(jsonInputString, recipientId);
                    randomItem.setUploadedDate(); // Set the upload date to today
                    sent = true; // Message sent successfully, exit the loop
                } else {
                    attempts++;
                }
            }

            if (!sent) {
                System.out.println("No available items to send.");
            }
        } else {
            System.out.println("No items available to send.");
        }
    }

    private void sendItemMessage(String jsonInputString, String recipientId) throws Exception {
        URL url = new URL("http://127.0.0.1:3000/sendItem");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = conn.getResponseCode();
        System.out.println("Response code: " + code);
    }
}