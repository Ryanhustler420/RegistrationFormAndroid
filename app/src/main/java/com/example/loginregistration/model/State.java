package com.example.loginregistration.model;

import com.google.gson.annotations.SerializedName;

public class State {
    @SerializedName("StateName")
    private String StateName;

    @SerializedName("Cities")
    private String[] Cities;

    public State() {
    }

    public State(String stateName, String[] cities) {
        StateName = stateName;
        this.Cities = cities;
    }

    public String getStateName() {
        return StateName;
    }

    public void setStateName(String stateName) {
        StateName = stateName;
    }

    public String[] getCities() {
        return Cities;
    }

    public void setCities(String[] cities) {
        this.Cities = cities;
    }
}
