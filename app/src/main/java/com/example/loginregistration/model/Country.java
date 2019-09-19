package com.example.loginregistration.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Country {

    @SerializedName("CountryName")
    private String CountryName;

    @SerializedName("States")
    private ArrayList<State> States;

    public Country() {}

    public Country(String countryName, ArrayList<State> states) {
        CountryName = countryName;
        this.States = states;
    }

    public String getCountryName() {
        return CountryName;
    }

    public void setCountryName(String countryName) {
        CountryName = countryName;
    }

    public ArrayList<State> getStates() {
        return States;
    }

    public void setStates(ArrayList<State> states) {
        this.States = states;
    }
}
