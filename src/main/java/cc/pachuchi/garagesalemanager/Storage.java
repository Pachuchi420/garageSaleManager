package cc.pachuchi.garagesalemanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Storage {
    private ObservableList<Item> itemList = FXCollections.observableArrayList();
    private static final String DATA_FILE = "garageSaleItems.dat";
    private static final String SETTINGS_FILE = "chatbot_settings.properties";

    public Storage() {
        loadItems();
    }

    @SuppressWarnings("unchecked")
    private void loadItems() {
        File dataFile = new File(DATA_FILE);
        if (dataFile.exists() && !dataFile.isDirectory()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
                List<Item> list = (List<Item>) in.readObject();
                itemList.addAll(FXCollections.observableArrayList(list));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveItems() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            // Convert ObservableList to ArrayList for serialization
            out.writeObject(new ArrayList<>(itemList));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Item> getItems() {
        return itemList;
    }

    public void addItem(Item item) {
        itemList.add(item);
        saveItems();
    }

    public void removeItem(Item item) {
        itemList.remove(item);
        saveItems();
    }

    // ChatBot Settings
    public void saveChatBotSettings(ChatBot chatBot) {
        Properties properties = new Properties();

        properties.setProperty("recipientID", chatBot.getRecipientID());
        properties.setProperty("intervalMinutes", String.valueOf(chatBot.getIntervalMinutes()));
        properties.setProperty("startTime", chatBot.getStartTime());
        properties.setProperty("endTime", chatBot.getEndTime());
        properties.setProperty("isActive", String.valueOf(chatBot.isActive()));

        try (OutputStream output = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(output, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ChatBot loadChatBotSettings() {
        Properties properties = new Properties();
        ChatBot chatBot = ChatBot.getInstance();

        File settingsFile = new File(SETTINGS_FILE);
        if (settingsFile.exists() && !settingsFile.isDirectory()) {
            try (InputStream input = new FileInputStream(SETTINGS_FILE)) {
                properties.load(input);

                chatBot.setRecipientID(properties.getProperty("recipientID"));
                chatBot.setIntervalMinutes(Integer.parseInt(properties.getProperty("intervalMinutes", "0")));
                chatBot.setStartTime(properties.getProperty("startTime"));
                chatBot.setEndTime(properties.getProperty("endTime"));
                chatBot.setActive(Boolean.parseBoolean(properties.getProperty("isActive", "false")));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return chatBot;
    }
}
