package com.olros.bussapp;

import java.io.Serializable;

public class Horizontal implements Serializable {

    private String name;
    private String holdeplass;
    private int holdeplass_icon;
    private String intent_activity;


    public Horizontal(String holdeplass, String name, int holdeplass_icon, String intent_activity) {
        this.holdeplass = holdeplass;
        this.name = name;
        this.holdeplass_icon = holdeplass_icon;
        this.intent_activity = intent_activity;
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

    public int getHoldeplass_icon() {
        return holdeplass_icon;
    }

    public void setHoldeplass_icon(int holdeplass_icon) { this.holdeplass_icon = holdeplass_icon; }

    public String getIntent_activity() {
        return intent_activity;
    }

    public void setIntent_activity(String intent_activity) {
        this.intent_activity = intent_activity;
    }
}
