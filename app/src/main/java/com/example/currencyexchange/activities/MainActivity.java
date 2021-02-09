package com.example.currencyexchange.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.currencyexchange.classes.AdapterClass;
import com.example.currencyexchange.BuildConfig;
import com.example.currencyexchange.classes.CurrencyApi;
import com.example.currencyexchange.classes.Favorites;
import com.example.currencyexchange.classes.Item;
import com.example.currencyexchange.R;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private RequestQueue mQueue;
    private CurrencyApi api;
    private ArrayList<Item> items;
    private ArrayList<Item> allItems;
    private TextView baseCurrency;
    private ImageView baseCurrencyImage;
    private DrawerLayout drawer;
    private NavigationView navigation;

    private RecyclerView mRecyclerView;
    private AdapterClass mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Favorites favorites;
    private boolean fav;

    /**
     * Zavolá sa pri vytvorení aktivity. Inicializujú sa tu atribúty triedy.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initComponents();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null){
            jsonParse("EUR");
        }
        initRecyclerView();
    }

    /**
     * Inicializácia atribútov triedy.
     */
    private void initComponents(){
        this.drawer = findViewById(R.id.drawer_layout);
        this.navigation = findViewById(R.id.nav_view);
        this.navigation.setNavigationItemSelectedListener(this);
        this.api = new CurrencyApi();
        this.baseCurrency = findViewById(R.id.base_currency_text_view);
        this.baseCurrencyImage = findViewById(R.id.base_currency_image_view);
        this.favorites = new Favorites(MainActivity.this );
        this.fav = false;

        mQueue = Volley.newRequestQueue(this);
        this.items = new ArrayList<>();
        this.allItems = new ArrayList<>();
    }

    /**
     * Inicializácia RecyclerView.
     */
    private void initRecyclerView(){
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new AdapterClass(this.items);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new AdapterClass.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                openCompareActivity(position);
            }
        });
    }

    /**
     * metóda, ktorá je zodpovedná za načítanie JSON dát z internetovej adresy a ich následné spracovanie
     * @param base
     */
    private void jsonParse(final String base) {
        if(!this.isNetworkConnected()){
            Toast.makeText(this, "No internet connection",
                    Toast.LENGTH_LONG).show();
            return;
        }
        this.items.clear();
        this.allItems.clear();

        String url = "";
        if(base.equals("EUR")){
            url = "https://api.exchangeratesapi.io/latest?base=EUR";
            items.add(new Item(R.drawable.eur,"EUR","","1 " + base + "= " + 1.0 + " " + "EUR", 1.0, false, true));
            baseCurrency.setText(base);
            allItems.add(new Item(R.drawable.eur,"EUR","","1 " + base + "= " + 1.0 + " " + "EUR", 1.0, false, true));
            baseCurrency.setText(base);
        } else {
            url = "https://api.exchangeratesapi.io/latest?base="+base;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            HashMap<String,String> list = api.getLatestRates(response);
                            DecimalFormat df = new DecimalFormat("####0.0000");

                            for(Map.Entry<String, String> entry : list.entrySet()) {
                                String key = entry.getKey();
                                double value = Double.parseDouble(entry.getValue());

                                Item i = new Item(R.drawable.ic_favorite_border_black_24dp,key,"","1 " + base + " = " + df.format(value) + " " + key, value, false, false);

                                if(key.equals(base)) {
                                    i.setBase(true);
                                    baseCurrency.setText(base);
                                }

                                items.add(i);
                                allItems.add(i);
                            }

                            if(fav) {
                                getFavorites();
                                mAdapter.setItems(items);
                            }

                            setCountryFlags();
                            baseCurrencyImage.setImageResource(getBaseCurrencyFlag(base));
                            Collections.sort(items,Item.getCompByName());
                            Collections.sort(allItems,Item.getCompByName());
                            mAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        this.mQueue.add(request);
    }

    /**
     * Metóda, ktorá sa stará o načítanie dát z databázy.
     */
    private void getFavorites() {
        ArrayList<String> fav = this.favorites.getData();
        ArrayList<Item> newItems = new ArrayList<>();
        for (Item i :
                this.items) {
            if (i.isBase()) {
                newItems.add(i);
                baseCurrency.setText(i.getName());
            }
        }
        for (int i = 0; i < fav.size(); i++) {
            for (int j = 0; j < this.items.size(); j++) {
                if (fav.get(i).equals(this.items.get(j).getName()) && !this.items.get(j).isBase()) {
                    this.items.get(j).setFav(true);
                    newItems.add(this.items.get(j));
                    break;
                }
            }
        }
        this.items.clear();
        this.items = newItems;
    }

    /**
     * Zisťuje, či je zariadenie pripojené k internetu.
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    /**
     * Každému itemu, ktorý reprezentuje daný štát, nastaví štátnu vlajku.
     */
    private void setCountryFlags(){
        for (Item i: this.items) {
            switch(i.getName()){
                case "AUD":
                    i.setImageResource(R.drawable.aud);
                    i.setCurrencyName("Australian dollar");
                    break;
                case "BGN":
                    i.setImageResource(R.drawable.bgn);
                    i.setCurrencyName("Bulgarian lev");
                    break;
                case "BRL":
                    i.setImageResource(R.drawable.brl); //rip
                    i.setCurrencyName("Brazilian real");
                    break;
                case "CAD":
                    i.setImageResource(R.drawable.cad);
                    i.setCurrencyName("Canadian dollar");
                    break;
                case "CHF":
                    i.setImageResource(R.drawable.chf);
                    i.setCurrencyName("Swiss franc");
                    break;
                case "CNY":
                    i.setImageResource(R.drawable.cny);
                    i.setCurrencyName("Chinese yuan renminbi");
                    break;
                case "CZK":
                    i.setImageResource(R.drawable.czk);
                    i.setCurrencyName("Czech koruna");
                    break;
                case "DKK":
                    i.setImageResource(R.drawable.dkk);
                    i.setCurrencyName("Danish krone");
                    break;
                case "EUR":
                    i.setImageResource(R.drawable.eur);
                    i.setCurrencyName("Euro");
                    break;
                case "GBP":
                    i.setImageResource(R.drawable.gbp);
                    i.setCurrencyName("Pound sterling");
                    break;
                case "HKD":
                    i.setImageResource(R.drawable.hkd);
                    i.setCurrencyName("Hong Kong dollar");
                    break;
                case "HRK":
                    i.setImageResource(R.drawable.hrk);
                    i.setCurrencyName("Croatian kuna");
                    break;
                case "HUF":
                    i.setImageResource(R.drawable.huf);
                    i.setCurrencyName("Hungarian forint");
                    break;
                case "IDR":
                    i.setImageResource(R.drawable.idr);
                    i.setCurrencyName("Indonesian rupiah");
                    break;
                case "ILS":
                    i.setImageResource(R.drawable.ils);
                    i.setCurrencyName("Israeli shekel");
                    break;
                case "INR":
                    i.setImageResource(R.drawable.inr);
                    i.setCurrencyName("Indian rupee");
                    break;
                case "ISK":
                    i.setImageResource(R.drawable.isk);
                    i.setCurrencyName("Icelandic krona");
                    break;
                case "JPY":
                    i.setImageResource(R.drawable.jpy);
                    i.setCurrencyName("Japanese yen");
                    break;
                case "KRW":
                    i.setImageResource(R.drawable.krw);
                    i.setCurrencyName("South Korean won");
                    break;
                case "MXN":
                    i.setImageResource(R.drawable.mxn);
                    i.setCurrencyName("Mexican peso");
                    break;
                case "MYR":
                    i.setImageResource(R.drawable.myr);
                    i.setCurrencyName("Malaysian ringgit");
                    break;
                case "NOK":
                    i.setImageResource(R.drawable.nok);
                    i.setCurrencyName("Norwegian krone");
                    break;
                case "NZD":
                    i.setImageResource(R.drawable.nzd);
                    i.setCurrencyName("New Zealand dollar");
                    break;
                case "PHP":
                    i.setImageResource(R.drawable.php);
                    i.setCurrencyName("Philippine peso");
                    break;
                case "PLN":
                    i.setImageResource(R.drawable.pln);
                    i.setCurrencyName("Polish zloty");
                    break;
                case "RON":
                    i.setImageResource(R.drawable.ron);
                    i.setCurrencyName("Romanian leu");
                    break;
                case "RUB":
                    i.setImageResource(R.drawable.rub);
                    i.setCurrencyName("Russian rouble");
                    break;
                case "SEK":
                    i.setImageResource(R.drawable.sek);
                    i.setCurrencyName("Swedish krona");
                    break;
                case "SGD":
                    i.setImageResource(R.drawable.sgd);
                    i.setCurrencyName("Singapore dollar");
                    break;
                case "THB":
                    i.setImageResource(R.drawable.thb);
                    i.setCurrencyName("Thai baht");
                    break;
                case "TRY":
                    i.setImageResource(R.drawable.tryl); //rip
                    i.setCurrencyName("Turkish lira");
                    break;
                case "USD":
                    i.setImageResource(R.drawable.usd);
                    i.setCurrencyName("US dollar");
                    break;
                case "ZAR":
                    i.setImageResource(R.drawable.zar);
                    i.setCurrencyName("South African rand");
                    break;

            }
        }
    }

    /**
     * Vráti hodnotu obrázka toho itemu, ktorý je nastavený ako base.
     * @param base
     * @return
     */
    private int getBaseCurrencyFlag(String base){
        for (Item i: this.items) {
            if(i.getName().equals(base)){
                return i.getImageResource();
            }
        }
        return 0;
    }

    /**
     * Metóda, ktorá spustí Compare aktivitu a pošle jej údaje o vybranom menovom kurze.
     * @param position
     */
    private void openCompareActivity(int position){
        Intent intent = new Intent(this ,Compare.class);
        for (Item i:
                this.items) {
            if(i.isBase()){
                intent.putExtra("base",i);
                break;
            }
        }
        intent.putExtra("choosen",this.items.get(position));
        startActivity(intent);
    }

    /**
     * Metóda, ktorá spustí Prediction aktivitu a pošle jej údaje menových kurzoch.
     */
    private void openPredictActivity(){
        Intent intent = new Intent(this,Prediction.class);
        intent.putExtra("items",this.allItems);
        startActivity(intent);
    }

    /**
     * Metóda, ktorá kontroluje či užívateľ daný item nepridal alebo neodstranil z obľúbených
     * a taktiež kontroluje či užívateľ daný item nezvolil za základný
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case 121:
                String cur = this.mAdapter.getNameOfItem(item.getGroupId());
                jsonParse(cur);
                return true;
            case 122:
                this.items.get(item.getGroupId()).setFav(true);
                this.favorites.insertValue(this.items.get(item.getGroupId()).getName());
                return true;
            case 123:
                this.items.get(item.getGroupId()).setFav(false);
                this.favorites.deleteRow(this.items.get(item.getGroupId()).getName());
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Vytvorí menu v hornej časti obrazovky pomocou ktorého sa vie užívateľ prepínať do obľúbených
     * itemov alebo aktualizovať aktivitu.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item_menu,menu);
        return true;
    }

    /**
     * Metóda, ktorá kontroluje či užívateľ nestlačil voľbu v menu. Ak áno vykoná sa príslušná
     * operácia.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.refresh:
                finish();
                startActivity(getIntent());
                return true;

            case R.id.option_fav:
                this.fav = true;
                this.getFavorites();
                this.mAdapter.setItems(this.items);
                this.mAdapter.notifyDataSetChanged();
                return true;

            case R.id.option_all:
                this.fav = false;
                jsonParse(this.baseCurrency.getText().toString());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Metóda, ktorá sa zavolá ak užívateľ v navigačnom menu sa chce prepnúť do inej aktivity.
     * @param item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.nav_menu:
                startActivity(new Intent(this, MainActivity.class));
                break;
            case R.id.fav_item:
                this.fav = true;
                this.getFavorites();
                this.mAdapter.setItems(this.items);
                this.mAdapter.notifyDataSetChanged();
                break;

            case R.id.nav_predict:
                this.openPredictActivity();
                break;

            case R.id.nav_settings:
                startActivity(new Intent(this ,Informations.class));
                break;
        }
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}
