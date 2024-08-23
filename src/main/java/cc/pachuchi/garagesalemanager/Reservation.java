package cc.pachuchi.garagesalemanager;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation implements Serializable {
    private String buyer;
    private String place;
    private LocalDate date;
    private Boolean reserved;


    public Reservation(String buyer, String place, LocalDate date, Boolean reserved){
        this.buyer = buyer;
        this.place = place;
        this.date = date;
        this.reserved = reserved;
    }

    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setDate(LocalDate date){this.date = date;}
    public LocalDate getDate() {
        return date;
    }

    public Boolean getReserved() {
        return reserved;
    }

    public void setReserved(Boolean reserved) {
        this.reserved = reserved;
    }

    public void undoReservation() {

        this.buyer = null;
        this.place = null;
        this.date = null;
        this.reserved = false;
    }

}
