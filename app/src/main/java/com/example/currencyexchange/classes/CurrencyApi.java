package com.example.currencyexchange.classes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Trieda v ktorej sa transformujú JSON dáta a následne sa ukladajú do HashMapov s ktorými ďalej pracujú
 * aktivity
 */
public class CurrencyApi {

    /**
     * funkcia berie ako parameter JSON dáta, ktoré prechádza a následnej ich ukladá do HashMapu
     * funkcia vracia HashMap v ktorom kľúče reprezentujú meno cenovej meny a hodnoty sú ceny
     * jednotlivých mien vyjadrené v jednej mene.
     * @param response
     * @return
     * @throws JSONException
     */
    public HashMap<String, String> getLatestRates(JSONObject response) throws JSONException {
        JSONObject currencies = response.getJSONObject("rates");
        Iterator x = currencies.keys();
        JSONArray names = currencies.names();
        JSONArray jsonArray = new JSONArray();

        while (x.hasNext()){
            String key = (String) x.next();
            jsonArray.put(currencies.get(key));
        }

        HashMap<String,String> list = new HashMap<>();

        for (int i = 0; i < jsonArray.length(); i++){
            list.put(names.get(i).toString(),jsonArray.get(i).toString());
        }

        return list;
    }

    /**
     * Funkcia, ktorá  ukladá do HashMapu datumy a prvky menoveho kurzu a nasledne tento HashMap vrati.
     * @param response
     * @param choosen
     * @return
     * @throws JSONException
     */
    public HashMap<String, String> getAllTimeRates(JSONObject response,String choosen) throws JSONException {
        JSONObject currencies = response.getJSONObject("rates");
        Iterator x = currencies.keys();
        JSONArray dates = currencies.names();
        JSONArray jsonArray = new JSONArray();

        HashMap<String,String> list = new HashMap<>();

        while (x.hasNext()){
            String key = (String) x.next();
            JSONObject obj = currencies.getJSONObject(key);
            jsonArray.put(obj.get(choosen));
        }

        if(dates.length() == jsonArray.length()) {
            for (int i = 0; i < jsonArray.length(); i++) {
                list.put(dates.get(i).toString(), jsonArray.get(i).toString());
            }
        }
        return list;
    }

    /**
     * Vrati aktualnu hodnotu vybrateho menoveho kurzu.
     * @param response
     * @param choosen
     * @return
     * @throws JSONException
     */
    public String getLatestRate(JSONObject response, String choosen) throws JSONException {
        JSONObject currency = response.getJSONObject("rates");
        return currency.get(choosen).toString();
    }
}
