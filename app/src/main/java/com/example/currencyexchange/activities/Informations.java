package com.example.currencyexchange.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.currencyexchange.R;

/**
 * trieda reprezentujúca aktivitu, ktorá slúži užívateľovi na to aby poskytol v prípade chýb v aplikácii
 * feedback
 */
public class Informations extends AppCompatActivity {

    private EditText message;

    /**
     * zavolá sa pri vytvorení aktivity
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informations);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        this.message = findViewById(R.id.pripomienky_edit_text);
    }

    /**
     * metóda, ktorá umožní užívateľovi odoslať mail s pripomienkami na môj email
     * @param view
     */
    public void sendEmail(View view) {
        String recipientList = "matus.turjak81@gmail.com";
        String[] recipient = recipientList.split(",");
        String subject = "CurrencyExchange application";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL,recipient);
        intent.putExtra(Intent.EXTRA_SUBJECT,subject);
        intent.putExtra(Intent.EXTRA_TEXT,this.message.getText().toString());
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent,"Choose an email client"));
    }

    /**
     * vytvorí menu, ktoré obsahuje tlačidlo pre návrat na úvodnú stránku
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.informations_menu,menu);
        return true;
    }

    /**
     * kontroluje, či užívateľ klikol na tlačidlo pomocou ktorého sa dostane na úvodnú aktivitu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return true;
    }
}
