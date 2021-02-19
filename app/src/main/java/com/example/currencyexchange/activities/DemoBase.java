package com.example.currencyexchange.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.currencyexchange.R;
import com.example.currencyexchange.classes.MyDate;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.Chart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class DemoBase extends AppCompatActivity implements OnChartValueSelectedListener,ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_STORAGE = 0;
    protected LineChart chart;
    protected float max;
    protected float min;
    protected ArrayList<Float> items;
    protected ArrayList<String> dates;

    /**
     * metóda v ktorej sa inicializuje graf
     */
    protected void initGraph(){
        chart = findViewById(R.id.chart1);

        chart.getDescription().setEnabled(false);

        chart.setTouchEnabled(true);

        chart.setDragDecelerationFrictionCoef(0.9f);

        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);

        chart.setBackgroundColor(Color.rgb(239,242,245));
        chart.setViewPortOffsets(20f, 0f, 20f, 0f);

        chart.setOnChartValueSelectedListener(this);

        Legend l = chart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {

            private final SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM ''yy", Locale.ENGLISH);

            @Override
            public String getFormattedValue(float value) {

                long millis = TimeUnit.DAYS.toMillis((long) value);
                return mFormat.format(new Date(millis));
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinimum((float) (this.min - 0.05));
        leftAxis.setAxisMaximum((float) (this.max + 0.05));
        leftAxis.setYOffset(-9f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setGranularity(0.01f);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    protected void setData() {
        if(this.items.size() == 0)
            return;

        ArrayList<Entry> values = new ArrayList<>(this.items.size());

        MyDate myDate = new MyDate();

        this.max = 0;
        this.min = 1000;

        for (int i = 0; i < this.items.size(); i++) {
            this.min = this.items.get(i) < this.min ? this.items.get(i) : this.min;
            this.max = this.items.get(i) > this.max ? this.items.get(i) : this.max;
            values.add(new Entry(myDate.daysBetween("1970-01-01",this.dates.get(i)), this.items.get(i)));
        }

        chart.getAxisLeft().setAxisMaximum((float) (this.max + this.max*0.02));
        chart.getAxisLeft().setAxisMinimum((float) (this.min - this.min*0.02));

        LineDataSet set1 = new LineDataSet(values, "DataSet 1");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setValueTextColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(1.5f);
        set1.setDrawCircles(false);
        set1.setDrawValues(false);

        if(this instanceof Prediction){
            set1.setDrawCircles(true);
        } else {
            set1.setDrawCircles(false);
        }
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(false);
        set1.setDrawFilled(true);

        LineData data = new LineData(set1);
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(9f);


        chart.setData(data);
    }

    /**
     * Metóda, ktorá vykreslí v grafe hodnoty menového kurzu za posledných numOfDays dní.
     * @param numOfDays
     */
    protected void setData(int numOfDays) {
        if(this.items.size() == 0)
            return;
        ArrayList<Entry> values = new ArrayList<>();

        MyDate myDate = new MyDate();

        int start = myDate.daysBetween("1970-01-01",
                myDate.addDays((this.dates.get(this.dates.size()-1)),-numOfDays));

        int from = start;

        int counter = this.items.size() - numOfDays;

        while(myDate.getDayNumber(this.dates.get(counter)) <= from)
            counter++;

        this.max = 0;
        this.min = 1000;

        while(from <= myDate.getDayNumber(this.dates.get(this.dates.size() - 1))) {
            if(this.items.size() > 0) {
                if(myDate.getDayNumber(this.dates.get(counter)) == from) {
                    this.max = this.items.get(counter) > this.max ? this.items.get(counter) : this.max;
                    this.min = this.items.get(counter) < this.min ? this.items.get(counter) : this.min;
                    values.add(new Entry(from + 1, this.items.get(counter)));
                    counter++;
                }
            }
            from++;
        }

        chart.getAxisLeft().setAxisMaximum((float) (this.max + this.max*0.02));
        chart.getAxisLeft().setAxisMinimum((float) (this.min - this.min*0.02));

        LineDataSet set1 = new LineDataSet(values, "DataSet 1");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setValueTextColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(1.5f);
        set1.setDrawCircles(false);
        set1.setDrawValues(false);

        if(this instanceof Prediction){
            set1.setDrawCircles(true);
        } else {
            set1.setDrawCircles(false);
        }
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(false);
        set1.setDrawFilled(true);

        LineData data = new LineData(set1);
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(9f);

        chart.setData(data);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.move_left_in_activity, R.anim.move_right_out_activity);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveToGallery();
            } else {
                Toast.makeText(getApplicationContext(), "Saving FAILED!", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    protected void requestStoragePermission(View view) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(view, "Write permission is required to save image to gallery", Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(DemoBase.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
                        }
                    }).show();
        } else {
            Toast.makeText(getApplicationContext(), "Permission Required!", Toast.LENGTH_SHORT)
                    .show();
            ActivityCompat.requestPermissions(DemoBase.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_STORAGE);
        }
    }

    protected void saveToGallery(Chart chart, String name) {
        if (chart.saveToGallery(name + "_" + System.currentTimeMillis(), 70))
            Toast.makeText(getApplicationContext(), "Saving SUCCESSFUL!",
                    Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(getApplicationContext(), "Saving FAILED!", Toast.LENGTH_SHORT)
                    .show();
    }

    protected abstract void saveToGallery();
}
