package com.treinverstoringen.app;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Heel simpel: open Google
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
        startActivity(browserIntent);
        
        // App mag op de achtergrond blijven
    }
}
