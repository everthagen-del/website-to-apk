package com.treinverstoringen.app;
import com.treinverstoringen.app.R;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Lees URL uit config.json
        String url = loadUrlFromConfig();
        
        // Als er een URL is, open hem
        if (url != null && !url.isEmpty()) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }
    }
    
    private String loadUrlFromConfig() {
        try {
            InputStream is = getAssets().open("config.json");
            Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
            String jsonString = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
            JSONObject config = new JSONObject(jsonString);
            return config.getString("dashboardUrl");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
