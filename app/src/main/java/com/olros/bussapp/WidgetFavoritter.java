package com.olros.bussapp;

public class WidgetFavoritter {
    public String id;
    public String navn;

    public WidgetFavoritter(String id, String navn) {
        this.id = id;
        this.navn = navn;
    }

    public void setData(String c,String d){
        this.id = c;
        this.navn = d;
    }
}
