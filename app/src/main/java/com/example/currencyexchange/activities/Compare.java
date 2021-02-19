package com.example.currencyexchange.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.currencyexchange.classes.JsonParser;
import com.example.currencyexchange.classes.Item;
import com.example.currencyexchange.classes.MyDate;
import com.example.currencyexchange.R;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.text.DecimalFormat;

/**
 * Aktivita Compare
 */
public class Compare extends DemoBase implements OnChartValueSelectedListener {
    private RequestQueue mQueue;
    private JsonParser api;
    private Item base;
    private Item choosen;
    private EditText baseCountryEdit;
    private EditText choosenCountryEdit;
    private TextView baseCountryText;
    private TextView choosenCountryText;
    private ImageView baseImage;
    private ImageView choosenImage;
    private DecimalFormat df;
    private Button[] zoom;

    /**
     * zavolá sa pri vytvorení aktivity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_compare);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(savedInstanceState == null){
            initComponents();
            setTitle(this.base.getName() + " -> " + this.choosen.getName());
            initGraph();
            jsonParse(30);
        }

        this.baseCountryEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try{
                    double number = Double.parseDouble(baseCountryEdit.getText()+"")*choosen.getExchangeNumber();
                    choosenCountryEdit.setText(df.format(number) + " " + choosen.getName());
                } catch (NumberFormatException e){
                    //Toast.makeText(this,"nezadali ste číslo", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * Inicializácia atribútov.
     */
    private void initComponents(){
        this.df = new DecimalFormat("####0.000");

        Intent intent = getIntent();

        this.api = new JsonParser();
        this.base = intent.getParcelableExtra("base");
        this.choosen = intent.getParcelableExtra("choosen");
        this.mQueue = Volley.newRequestQueue(this);

        this.baseCountryEdit = findViewById(R.id.country1_edit_text);
        this.choosenCountryEdit = findViewById(R.id.country2_edit_text);
        this.baseCountryText = findViewById(R.id.country1_text_view);
        this.choosenCountryText = findViewById(R.id.country2_text_view);
        this.baseImage = findViewById(R.id.country1_image_view);
        this.choosenImage = findViewById(R.id.country2_image_view);

        this.baseCountryEdit.setHint("1" + this.base.getName());
        this.choosenCountryEdit.setHint(df.format(this.choosen.getExchangeNumber()) + " " + this.choosen.getName());

        this.baseCountryText.setText(this.base.getCurrencyName());
        this.choosenCountryText.setText(this.choosen.getCurrencyName());

        this.baseImage.setImageResource(this.base.getImageResource());
        this.choosenImage.setImageResource(this.choosen.getImageResource());

        this.items = new ArrayList<>();
        this.dates = new ArrayList<>();
        this.zoom = new Button[6];
        this.zoom[0] = findViewById(R.id.button_all);
        this.zoom[1] = findViewById(R.id.button_ten_y);
        this.zoom[2] = findViewById(R.id.button_five_y);
        this.zoom[3] = findViewById(R.id.button_one_y);
        this.zoom[4] = findViewById(R.id.button_six_m);
        this.zoom[5] = findViewById(R.id.button_one_m);
    }

    /**
     * metóda, ktorá počíta cenu jednej menovej jednotky v druhej menovej jednotke.
     * @param view
     */
    public void calculateCurrency(View view) {
        try{
            double number = Double.parseDouble(this.baseCountryEdit.getText()+"")*this.choosen.getExchangeNumber();
            this.choosenCountryEdit.setText(this.df.format(number) + " " + this.choosen.getName());
        } catch (NumberFormatException e){
            Toast.makeText(this,"nezadali ste číslo",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void jsonParse(int numOfDays) {
        this.items.clear();
        this.dates.clear();

        String url = "http://192.168.0.161:8080/rates/" + this.base.getName() + "-" + this.choosen.getName() + "/"+numOfDays;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            Map<String, Double> map = api.getAllTimeRates(response);

                            for(Map.Entry<String, Double> entry : map.entrySet()) {
                                Float value = Float.parseFloat(entry.getValue() + "");
                                items.add(value);
                                dates.add(entry.getKey());
                            }

                            setData();
                            chart.invalidate();
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
     * vytvorí menu, v ktorom sú dve tlačidlá a to pre návrat na úvodnú stránku a pre uloženie
     * obrázka grafu do galérie
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.line, menu);
        return true;
    }

    /**
     * kontroluje, či užívateľ stlačil tlačidlo pre návrat na úvodnú stránku ale stlačil tlačidlo
     * pre uloženie obrázka grafu do galérie
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.back_to_home:
                onBackPressed();
                break;
            case R.id.actionSave: {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    saveToGallery();
                } else {
                    requestStoragePermission(chart);
                }
                break;
            }
        }
        return true;
    }

    /**
     * Ulozi obrazok grafu do galerie.
     */
    @Override
    protected void saveToGallery() {
        saveToGallery(chart, "LineChartTime");
    }

    /**
     * metóda, ktorá po kliknutí na určitú hodnotu v grafe zobrazí informácie
     * @param e
     * @param h
     */
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        MyDate d = new MyDate();
        Toast.makeText(this,d.addDays("1970-01-01",(int)e.getX()) + "\n"+e.getY(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected() {
    }

    /**
     * Prehodi menove jednotky.
     * @param view
     */
    public void swapCurrency(View view) {
        Item newBase = this.base;
        this.base = this.choosen;
        this.choosen = newBase;

        String url = "http://192.168.0.161:8080/latest/" + this.base.getName() + "/" + this.choosen.getName();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            double value = api.getLatestRate(response);

                            choosen.setExchangeNumber(value);
                            baseCountryEdit.setHint("1" + base.getName());
                            choosenCountryEdit.setHint(df.format(choosen.getExchangeNumber()) + " " + choosen.getName());

                            baseCountryText.setText(base.getCurrencyName());
                            choosenCountryText.setText(choosen.getCurrencyName());

                            baseImage.setImageResource(base.getImageResource());
                            choosenImage.setImageResource(choosen.getImageResource());
                            jsonParse(30);

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
     * Metóda, ktorá po kliknutí vykreslí stanovený počet dát, ktorý užívateľ zadáva stlačením
     * konkrétneho tlačidla.
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void showGraph(View view) {
        switch(view.getId()){
            case R.id.button_all:
                jsonParse(Integer.MAX_VALUE);
                break;
            case R.id.button_ten_y:
                jsonParse(3650);
                break;
            case R.id.button_five_y:
                jsonParse(365*5);
                break;
            case R.id.button_one_y:
                jsonParse(365);
                break;
            case R.id.button_six_m:
                jsonParse(180);
                break;
            case R.id.button_one_m:
                jsonParse(30);
                break;
        }
    }
}
