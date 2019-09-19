package com.example.loginregistration.store;

import com.example.loginregistration.model.Country;
import com.example.loginregistration.model.State;

import java.util.ArrayList;

public class CountrySrc {
    private Country country;
    ArrayList<State> states = new ArrayList<>();

    public CountrySrc() {
        String[] jamshedpur = {};
        String[] maharastra = {};
        states.add(new State("Jharkhand", jamshedpur));
        states.add(new State("Maharastra", maharastra));
        country = new Country("India", states);
    }

    public Country getCountry() {
        return country;
    }

}
