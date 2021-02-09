package com.example.currencyexchange.classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Trieda je vytvorena ako pomocka pri praci  s datumami.
 */
public class MyDate {

    private SimpleDateFormat sdf;

    /**
     * Konstruktor triedy.
     */
    public MyDate(){
        this.sdf = new SimpleDateFormat("yyyy-MM-dd");
    }

    /**
     * Vrati pocet dni medzi dvoma datumami.
     * @param s1
     * @param s2
     * @return
     */
    public int daysBetween(String s1, String s2){
        try {
            Date date1 = this.sdf.parse(s1);
            Date date2 = this.sdf.parse(s2);
            long diff = date2.getTime() - date1.getTime();
            int days = (int)(diff / (1000*60*60*24));

            return days;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Prida ku datumu pocet dni a novy datum vrati.
     * @param s1
     * @param numOfDays
     * @return
     */
    public String addDays(String s1, int numOfDays) {
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(s1));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        c.add(Calendar.DAY_OF_MONTH, numOfDays);
        String newDate = sdf.format(c.getTime());
        return newDate;
    }

    /**
     * Vrati pocet dni medzi datumom v parametri a datumom 1970-01-01.
     * @param d1
     * @return
     */
    public int getDayNumber(String d1){
        return this.daysBetween("1970-01-01",d1);
    }
}
