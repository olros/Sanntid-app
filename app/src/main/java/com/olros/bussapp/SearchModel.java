package com.olros.bussapp;

public class SearchModel {
    private String name, locality, id, avstand;

    public SearchModel(String name, String locality, String id, String avstand){
        this.name = name;
        this.locality = locality;
        this.id = id;
        this.avstand = avstand;
    }

    public String getName() {return name;}
    public String getLocality() {return locality;}
    public String getId() {return id;}
    public String getAvstand() {return avstand;}

    public void setName(String name) {this.name = name;}
    public void setLocality(String name) {this.locality = locality;}
    public void setId(String name) {this.id = id;}
    public void setAvstand(String name) {this.avstand = avstand;}
}
