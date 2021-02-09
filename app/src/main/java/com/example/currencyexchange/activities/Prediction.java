package com.example.currencyexchange.activities;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.currencyexchange.classes.CurrencyApi;
import com.example.currencyexchange.ExponentialSmoothing.DoubleExponentialSmoothing;
import com.example.currencyexchange.ExponentialSmoothing.SingleExponentialSmoothing;
import com.example.currencyexchange.classes.Item;
import com.example.currencyexchange.classes.MyDate;
import com.example.currencyexchange.R;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class Prediction extends DemoBase implements PopupMenu.OnMenuItemClickListener {
    private RequestQueue mQueue;
    private CurrencyApi api;
    private TextView first;
    private TextView second;
    private Button image1;
    private Button image2;
    private Button[] predictions;
    private ArrayList<Float> allRates;
    private ArrayList<Item> arrayListItems;
    private Item right;
    private Item left;
    private boolean change;

    /**
     * Zavola sa pri vytvoreni aktivity.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(this.isNetworkConnected()){
            this.initComponents();
            this.initGraph();
            this.jsonParse();
        } else {
            Toast.makeText(this,"No internet connection",
                    Toast.LENGTH_LONG).show();
            onBackPressed();
        }

    }

    @Override
    protected void saveToGallery() {

    }

    /**
     * Inicializacia atributov triedy.
     */
    private void initComponents(){
        Intent intent = getIntent();

        this.mQueue = Volley.newRequestQueue(this);
        this.first = findViewById(R.id.country1_text_view);
        this.second = findViewById(R.id.country2_text_view);
        this.image1 = findViewById(R.id.country1_button);
        this.image1.setBackgroundResource(R.drawable.eur);
        this.image2 = findViewById(R.id.country2_button);
        this.image2.setBackgroundResource(R.drawable.czk);
        this.change = false;

        this.api = new CurrencyApi();
        this.items = new ArrayList<>();
        this.dates = new ArrayList<>();

        this.arrayListItems = intent.getParcelableArrayListExtra("items");

        this.left = this.arrayListItems.get(8);
        this.right = this.arrayListItems.get(6);

        this.predictions = new Button[3];
        this.predictions[0] = findViewById(R.id.button_5_days);
        this.predictions[1] = findViewById(R.id.button_3_days);
        this.predictions[2] = findViewById(R.id.button_1_day);

        this.allRates = this.getData();
    }

    /**
     * Ziska historicke hodnoty menoveho kurzu z webovej stranky a tie ulozi listov triedy
     * pre dalsiu pracu.
     */
    private void jsonParse(){
        this.items.clear();
        this.dates.clear();
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        cal.add(Calendar.DATE,0);
        String to = getDate(cal);

        cal.add(Calendar.DATE,-15);
        String from = getDate(cal);

        String url = "https://api.exchangeratesapi.io/history?start_at=" + from + "&end_at="+
                to+"&base="+ this.left.getName() + "&symbols="+this.right.getName();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Map<String,String> list = api.getAllTimeRates(response,right.getName());
                            TreeMap<String, String> sorted = new TreeMap<>();
                            sorted.putAll(list);

                            for(Map.Entry<String, String> entry : sorted.entrySet()) {
                                float value = Float.parseFloat(entry.getValue());
                                items.add(value);
                                dates.add(entry.getKey());
                            }

                            setData(items.size());
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
     * Zisťuje, či je zariadenie pripojené k internetu.
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    /**
     * Metoda vytvorena vrati historicke data menoveho kurzu a nasledne tieto data sa pouziju
     * pre vypocet predikcii.
     * @return
     */
    private ArrayList<Float> getData(){
        final ArrayList<Float> al = new ArrayList<>();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String url = "https://api.exchangeratesapi.io/history?start_at=2000-01-01&end_at="+
                simpleDateFormat.format(date)+"&base="+ this.left.getName() + "&symbols="+this.right.getName();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Map<String,String> list = api.getAllTimeRates(response,right.getName());
                            TreeMap<String, String> sorted = new TreeMap<>();
                            sorted.putAll(list);

                            for(Map.Entry<String, String> entry : sorted.entrySet()) {
                                float value = Float.parseFloat(entry.getValue());
                                al.add(value);
                            }
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
        return al;
    }

    /**
     * Vrati datum v podobe stringu.
     * @param cal
     * @return
     */
    public static String getDate(Calendar cal){
        return "" + cal.get(Calendar.YEAR) + "-" +  (cal.get(Calendar.MONTH)+1) + "-" + cal.get(Calendar.DATE);
    }

    /**
     * Po kliknuti na graf sa yobrayi ikona o historickej hodnote takeho menoveho kurzu na ktory
     * uzivatel klikol.
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
     * Vytvori a zobrazi popup menu.
     * @param view
     */
    public void showPopup(View view) {
        PopupMenu popup = new PopupMenu(this,view);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.popup_menu_predict);
        popup.show();
        this.change = view.getId() != R.id.country1_button ? true : false;
    }

    /**
     * Zistuje, ktora polozka v popu menu bola kliknuta.
     * @param item
     * @return
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch(item.getItemId()){
            case R.id.item_aud:
                this.right = this.change == true ? this.arrayListItems.get(0) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(0) : this.left;
                break;
            case R.id.item_cad:
                this.right = this.change == true ? this.arrayListItems.get(3) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(3) : this.left;
                break;
            case R.id.item_chf:
                this.right = this.change == true ? this.arrayListItems.get(4) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(4) : this.left;
                break;
            case R.id.item_czk:
                this.right = this.change == true ? this.arrayListItems.get(6) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(6) : this.left;
                break;
            case R.id.item_dkk:
                this.right = this.change == true ? this.arrayListItems.get(7) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(7) : this.left;
                break;
            case R.id.item_eur:
                this.right = this.change == true ? this.arrayListItems.get(8) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(8) : this.left;
                break;
            case R.id.item_gbp:
                this.right = this.change == true ? this.arrayListItems.get(9) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(9) : this.left;
                break;
            case R.id.item_hkd:
                this.right = this.change == true ? this.arrayListItems.get(10) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(10) : this.left;
                break;
            case R.id.item_huf:
                this.right = this.change == true ? this.arrayListItems.get(12) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(12) : this.left;
                break;
            case R.id.item_jpy:
                this.right = this.change == true ? this.arrayListItems.get(17) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(17) : this.left;
                break;
            case R.id.item_krw:
                this.right = this.change == true ? this.arrayListItems.get(18) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(18) : this.left;
                break;
            case R.id.item_nok:
                this.right = this.change == true ? this.arrayListItems.get(21) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(21) : this.left;
                break;
            case R.id.item_pln:
                this.right = this.change == true ? this.arrayListItems.get(24) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(24) : this.left;
                break;
            case R.id.item_sek:
                this.right = this.change == true ? this.arrayListItems.get(27) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(27) : this.left;
                break;
            case R.id.item_sqd:
                this.right = this.change == true ? this.arrayListItems.get(28) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(28) : this.left;
                break;
            case R.id.item_USD:
                this.right = this.change == true ? this.arrayListItems.get(31) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(31) : this.left;
                break;
            case R.id.item_zar:
                this.right = this.change == true ? this.arrayListItems.get(32) : this.right;
                this.left = this.change == false ? this.arrayListItems.get(32) : this.left;
                break;
        }

        this.updateItems();
        return true;
    }

    /**
     * Vytvori menu aktivity.
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.prediction_menu, menu);
        return true;
    }

    /**
     * Vrati uzivatela spat na uvodnu stranku.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return true;
    }

    /**
     * Nastavi itemy.
     */
    private void updateItems() {
        this.image1.setBackgroundResource(this.left.getImageResource());
        this.image2.setBackgroundResource(this.right.getImageResource());

        this.first.setText(this.left.getName());
        this.second.setText(this.right.getName());

        jsonParse();
        this.allRates = this.getData();
    }

    /**
     * Metoda, ktora sa zavola ak uzivatel chce predikovat data o jeden den dopredu.
     * @param view
     */
    public void predictOneDay(View view) {

        SingleExponentialSmoothing s = new SingleExponentialSmoothing(this.allRates.size(),1);
        DoubleExponentialSmoothing d = new DoubleExponentialSmoothing(this.allRates.size(),1);
        predict(s,d,1,0.6f);
    }

    /**
     * Metoda, ktora sa zavola ak uzivatel chce predikovat data o tri dni dopredu.
     * @param view
     */
    public void predictThreeDays(View view) {
        SingleExponentialSmoothing s = new SingleExponentialSmoothing(this.allRates.size(),3);
        DoubleExponentialSmoothing d = new DoubleExponentialSmoothing(this.allRates.size(),3);
        predict(s,d,3,0.4f);
    }

    /**
     * Metoda, ktora sa zavola ak uzivatel chce predikovat data o pat dni dopredu.
     * @param view
     */
    public void predictFiveDays(View view) {
        SingleExponentialSmoothing s = new SingleExponentialSmoothing(this.allRates.size(),5);
        DoubleExponentialSmoothing d = new DoubleExponentialSmoothing(this.allRates.size(),5);
        predict(s,d,5, 0.3f);
    }

    /**
     * Metoda vypocita predikcie menoveho kurzu a nasledne ich zobrazi v grafe.
     * @param s
     * @param d
     * @param numOfDays
     * @param alpha
     */
    public void predict(SingleExponentialSmoothing s, DoubleExponentialSmoothing d, int numOfDays, float alpha){
        Float[] arr = new Float[this.allRates.size()];
        float[] ses = s.predict(this.allRates.toArray(arr),alpha);
        float[] des = d.predict(this.allRates.toArray(arr),alpha);

        MyDate m = new MyDate();

        if(this.items.size() <= 0 || this.dates.size() <=0)
            return;

        this.removePredictions();

        if(s.getResiduals() > d.getResiduals()){
            for(int i = 0; i < des.length; i++){
                this.items.add(des[i]);
                this.dates.add(m.addDays(this.dates.get(this.dates.size() - 1),1));
            }
        } else {
            for(int i = 0; i < ses.length; i++){
                this.items.add(ses[i]);
                this.dates.add(m.addDays(this.dates.get(this.dates.size() - 1),1));
            }
        }
        setData(this.items.size());
        chart.invalidate();
    }

    /**
     * Pomocna metoda, ktora vymaze vypocitane predikcie.
     */
    public void removePredictions(){
        DecimalFormat df = new DecimalFormat("#.#####");
        if(this.items.size() <= 0 || this.dates.size() <=0 || this.allRates.size() <= 0)
            return;

        while(!df.format(this.items.get(this.items.size() - 1)).equals(df.format(this.allRates.get(this.allRates.size() - 1)))){
            this.items.remove(this.items.size() - 1);
            this.dates.remove(this.dates.size() - 1);
        }
    }
}
