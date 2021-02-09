package com.example.currencyexchange.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Trieda, ktorá slúži na ukladanie, odtránenie obľúbených menových kurzov z databázy
 */
public class Favorites extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "currencies.db";
    private static final String TABLE_NAME = "currency";
    private static final String COLUMN_NAME_CUR = "cur_name";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Favorites.TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Favorites.COLUMN_NAME_CUR + " TEXT);";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Favorites.TABLE_NAME;

    /**
     * konštruktor triedy
     * @param context
     */
    public Favorites(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * vytvorí tabuľku v ktorej budú ukladané dáta
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * zmaže tabuľku
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * vráti meno databázy
     * @return
     */
    public static String getDatabaseNam() {
        return DATABASE_NAME;
    }

    /**
     * vráti meno stĺpca
     * @return
     */
    public static String getColumnNameCur() {
        return COLUMN_NAME_CUR;
    }

    /**
     * pridá riadok s dátami do tabuľky
     * @param curr
     * @return
     */
    public boolean insertValue(String curr){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_CUR,curr);

        long result = db.insert(TABLE_NAME,null,values);

        return result > -1 ? true : false;
    }

    /**
     * odstráni riadok z tabuľky
     * @param curr
     * @return
     */
    public int deleteRow(String curr){
        SQLiteDatabase db = this.getWritableDatabase();
        String where = COLUMN_NAME_CUR + " LIKE ?";
        String[] nameOfCurrency = {curr};
        int deletedRows = db.delete(TABLE_NAME,where,nameOfCurrency);

        return deletedRows;
    }

    /**
     * vráti všetky riadky, ktoré sa nachádzajú v tabuľke
     * @return
     */
    public ArrayList<String> getData(){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        ArrayList<String> itemIds = new ArrayList<>();
        while(cursor.moveToNext()) {
            String itemId = cursor.getString(
                    cursor.getColumnIndex(COLUMN_NAME_CUR));
            itemIds.add(itemId);
        }
        cursor.close();
        return itemIds;
    }
}
