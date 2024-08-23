package cc.pachuchi.garagesalemanager;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public class Item implements Serializable {
    private String id;
    private String name;
    private String description;
    private byte[] imageData;
    private int price;
    private String currency;
    private final LocalDateTime date;
    private  LocalDate reservationDate;
    private Boolean sold;



    private Reservation reservation = new Reservation(null, null, null, false);






    public Item(String name, String description, byte[] imageData, int price, String currency) {
        this.id = makeUniqueID();
        this.name = name;
        this.description = description;
        this.imageData = imageData;
        this.price= price;
        this.currency = currency;
        this.date = LocalDateTime.now();
        this.sold = false;
        this.reservationDate = null;
    }

    private String makeUniqueID(){
        return UUID.randomUUID().toString();
    }

    // Setters & Getters


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public Image getImage() {
        if (imageData != null) {
            return new Image(new ByteArrayInputStream(imageData));
        }
        return null; // Return null or a default image
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Boolean getSold() {
        return this.sold;
    }

    public void setSold(Boolean sold) {
        this.sold = sold;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public LocalDate getReservationDate() {
        return reservation.getDate();
    }

    public Reservation getReservation(){return reservation;}

    public Boolean checkReservation(){
        return reservation.getReserved();
    }


    public void undoReservation(){

    }
}
