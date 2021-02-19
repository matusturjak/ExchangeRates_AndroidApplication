package com.example.currencyexchange.classes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Trieda v ktorej sa transformujú JSON dáta a následne sa ukladajú do HashMapov s ktorými ďalej pracujú
 * aktivity
 */
public class JsonParser {

    /**
     * funkcia berie ako parameter JSON dáta, ktoré prechádza a následnej ich ukladá do HashMapu
     * funkcia vracia HashMap v ktorom kľúče reprezentujú meno cenovej meny a hodnoty sú ceny
     * jednotlivých mien vyjadrené v jednej mene.
     * @param response
     * @return
     * @throws JSONException
     */
    public HashMap<String, Double> getLatestRates(JSONArray response) throws JSONException {

        HashMap<String, Double> list = new HashMap<>();
        for (int i = 0; i < response.length(); i++) {
            JSONObject jsonObject = response.getJSONObject(i);
            JSONObject rate = jsonObject.getJSONObject("rate");

            list.put(rate.getString("secondCountry"), rate.getDouble("value"));
        }

        return list;
    }

    public TreeMap<String, Double> getAllTimeRates(JSONArray response) throws JSONException {

        TreeMap<String, Double> treeMap = new TreeMap<>();

        for (int i = 0; i < response.length(); i++){
            JSONObject jsonObject = response.getJSONObject(i);
            JSONObject rate = jsonObject.getJSONObject("rate");
            treeMap.put(jsonObject.getString("date"), rate.getDouble("value"));
        }
        return treeMap;
    }

    /**
     * Vrati aktualnu hodnotu vybrateho menoveho kurzu.
     * @param response
     * @return
     * @throws JSONException
     */
    public Double getLatestRate(JSONObject response) throws JSONException {
        JSONObject currency = response.getJSONObject("rate");
        return currency.getDouble("value");
    }

    public TreeMap<String, Float> getPredictions(JSONArray response) throws JSONException {
        TreeMap<String, Float> treeMap = new TreeMap<>();

        for(int i = 0; i < response.length(); i++) {
            JSONObject jsonObject = response.getJSONObject(i);
            JSONObject rate = jsonObject.getJSONObject("rate");
            treeMap.put(jsonObject.getString("date"), (float) rate.getDouble("value"));
        }

        return treeMap;
    }
}
