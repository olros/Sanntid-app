package com.olros.bussapp;

import java.io.Serializable;
import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

public class Model implements Serializable {

    public enum ItemType {
        list_item, card_item, horizontal_item, top_item;
    }

    private String name;
    private String holdeplass;
    public LatLng location;
    public ItemType type;
    public ArrayList<Horizontal> horizontal;


    public Model(LatLng location, String holdeplass, String name, ItemType type, ArrayList<Horizontal> horizontal) {
        this.location = location;
        this.holdeplass = holdeplass;
        this.name = name;
        this.type = type;
        this.horizontal = horizontal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHoldeplass() {
        return holdeplass;
    }

    public void setHoldeplass(String holdeplass) {
        this.holdeplass = holdeplass;
    }

    public LatLng getLocation() {
        return location;
    }

    public ItemType getType() {
        return type;
    }

    public ArrayList<Horizontal> getHorizontal() {
        return horizontal;
    }
}
