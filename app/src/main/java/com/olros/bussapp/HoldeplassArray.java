package com.olros.bussapp;

import java.util.ArrayList;

public class HoldeplassArray {
    private static ArrayList<Model> arraylist;

    public static ArrayList<Model> getList() {
        return arraylist;
    }

    public static void setList(ArrayList<Model> holdeplasser) {
        arraylist = holdeplasser;
    }
}
