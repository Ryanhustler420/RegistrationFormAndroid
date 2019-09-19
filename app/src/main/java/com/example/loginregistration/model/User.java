package com.example.loginregistration.model;

public class User {
    private String imageUrl;
    private String name;
    private String gender;
    private String address;
    private String dob;
    private Coordinates lastLocation;
    private String phone;
    private String country;
    private String state;
    private String city;
    private String currentCity;
    private String lastCity;
    private String email;
    private int role;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String imageUrl, String name, String gender, String address, String dob, Coordinates lastLocation, String phone, String country, String state, String city, String currentCity, String lastCity, String email, int role) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.gender = gender;
        this.address = address;
        this.dob = dob;
        this.lastLocation = lastLocation;
        this.phone = phone;
        this.country = country;
        this.state = state;
        this.city = city;
        this.currentCity = currentCity;
        this.lastCity = lastCity;
        this.email = email;
        this.role = role;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public Coordinates getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Coordinates lastLocation) {
        this.lastLocation = lastLocation;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(String currentCity) {
        this.currentCity = currentCity;
    }

    public String getLastCity() {
        return lastCity;
    }

    public void setLastCity(String lastCity) {
        this.lastCity = lastCity;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
