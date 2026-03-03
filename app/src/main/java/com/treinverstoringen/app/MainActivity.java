cd ~/website-to-apk

# Vervang MainActivity.java door de robuuste versie met een selector intent
tee app/src/main/java/com/treinverstoringen/app/MainActivity.java << 'EOF'
package com.treinverstoringen.app;
import com.treinverstoringen.app.R;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String url = loadUrlFromConfig();

        if (url != null && !url.isEmpty()) {
            openInBrowser(url);
        } else {
            Log.e(TAG, "URL in config.json is leeg of niet gevonden.");
        }
        finish(); // Sluit de app nadat de intent is verzonden
    }

    private void openInBrowser(String urlString) {
        Uri uri = Uri.parse(urlString);

        // Stap 1: Maak een basis intent voor een lege http link.
        // Deze intent heeft geen echte data, alleen een leeg http schema.
        // Hiermee vinden we alle browsers, ongeacht deep links.
        Intent baseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));

        // Stap 2: Maak de uiteindelijke intent die de echte URL bevat.
        Intent targetIntent = new Intent(Intent.ACTION_VIEW, uri);
        targetIntent.addCategory(Intent.CATEGORY_BROWSABLE);

        // Stap 3: Koppel de baseIntent als 'selector' voor de targetIntent.
        // Dit vertelt Android: "Zoek een app die de baseIntent aankan
        // (dus een browser) en gebruik die om de targetIntent uit te voeren."
        targetIntent.setSelector(baseIntent);

        try {
            startActivity(targetIntent);
            Log.i(TAG, "Browser intent succesfully sent for URL: " + urlString);
        } catch (Exception e) {
            // Fallback voor het zeldzame geval dat er echt geen browser is.
            Log.e(TAG, "Kon geen app vinden om de URL te openen. Valt terug op standaard intent.", e);
            try {
                // Probeer het op de oude, simpele manier. Dit zal waarschijnlijk de deep link triggeren,
                // maar we moeten het proberen als de eerste methode faalt.
                Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(fallbackIntent);
            } catch (Exception ex) {
                Log.e(TAG, "Zelfs de fallback intent is mislukt. Er is waarschijnlijk geen browser.", ex);
            }
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
EOF
