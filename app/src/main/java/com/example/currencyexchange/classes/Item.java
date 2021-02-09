package com.example.currencyexchange.classes;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

public class Item implements Parcelable {
    private int imageResource;
    private String name;
    private String currencyName;
    private String exchangeRate;
    private double exchangeNumber;
    private boolean isFav;
    private boolean isBase;

    /**
     * parametricky konštruktor
     * @param mImageResource
     * @param mName
     * @param currencyName
     * @param mExchangeRate
     * @param mExchangeNumber
     * @param mIsFav
     * @param mIsBase
     */
    public Item(int mImageResource, String mName, String currencyName, String mExchangeRate,double mExchangeNumber, boolean mIsFav, boolean mIsBase){
        this.imageResource = mImageResource;
        this.name = mName;
        this.currencyName = currencyName;
        this.exchangeRate = mExchangeRate;
        this.exchangeNumber = mExchangeNumber;
        this.isFav = mIsFav;
        this.isBase = mIsBase;
    }

    /**
     * druhý parametrický konštruktor
     * @param mImageresource
     * @param mName
     */
    public Item(int mImageresource, String mName){
        this.imageResource = mImageresource;
        this.name = mName;
    }

    /**
     * konštruktor, ktorý sa zavolá pri kopírovaní objektu medzi aktivitami
     * @param in
     */
    protected Item(Parcel in) {
        imageResource = in.readInt();
        name = in.readString();
        currencyName = in.readString();
        exchangeRate = in.readString();
        exchangeNumber = in.readDouble();
        isFav = in.readByte() != 0;
        isBase = in.readByte() != 0;
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    /**
     * Porovná dva itemy na základe ich názvu.
     * @return
     */
    public static Comparator<Item> getCompByName() {
        Comparator comp = new Comparator<Item>(){
            @Override
            public int compare(Item s1, Item s2)
            {
                return s1.name.compareTo(s2.name);
            }
        };
        return comp;
    }

    /**
     * Vráti hodnotu obrázka.
     * @return
     */
    public int getImageResource() { return this.imageResource; }

    /**
     * Vráti skratku názvu menovej jednotky.
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     * Vráti názov menovej jednotky.
     * @return
     */
    public String getCurrencyName() { return currencyName; }

    /**
     * Vráti informačný text o menovom kurze.
     * @return
     */
    public String getExchangeRateText() {
        return this.exchangeRate;
    }

    /**
     * Vráti hodnotu ceny menovej jednotky vyjadrenú v druhej menovej jednotke.
     * @return
     */
    public double getExchangeNumber() { return exchangeNumber; }

    /**
     * Vráti true ak item reprezentuje menovú jednotku v ktorej sú vyjadrené ostatné menové jednotky.
     * @return
     */
    public boolean isBase() {
        return this.isBase;
    }

    /**
     * vráti true ak item patrí do obľúbených
     * @return
     */
    public boolean isFav() { return isFav; }

    /**
     * nastaví item ako základný to znamená že všetky ceny menových jednotiek budú vyjadrene v menovej jednotke
     * tohto itemu
     * @param base
     */
    public void setBase(boolean base) {
        isBase = base;
    }

    /**
     * nastavi text menoveho kurzu
     * @param currencyName
     */
    public void setCurrencyName(String currencyName) { this.currencyName = currencyName; }

    /**
     * nastavi obrazok menovej jednotky
     * @param imageResource
     */
    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    /**
     * nastavi hodnotu menoveho kurzu
     * @param exchangeNumber
     */
    public void setExchangeNumber(double exchangeNumber) {
        this.exchangeNumber = exchangeNumber;
    }

    /**
     * nastavi item ako oblubeny
     * @param fav
     */
    public void setFav(boolean fav) {
        isFav = fav;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * pomocou tejto metody sa vlastnosti Itemu mozu prenasat medzi aktivitami
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(imageResource);
        dest.writeString(name);
        dest.writeString(currencyName);
        dest.writeString(exchangeRate);
        dest.writeDouble(exchangeNumber);
        dest.writeByte((byte) (isFav ? 1 : 0));
        dest.writeByte((byte) (isBase ? 1 : 0));
    }
}
