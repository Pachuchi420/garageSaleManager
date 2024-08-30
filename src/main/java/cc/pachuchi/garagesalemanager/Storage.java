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

}
