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
        finish();
    }

    private void openInBrowser(String urlString) {
        Uri uri = Uri.parse(urlString);

        Intent baseIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));
        Intent targetIntent = new Intent(Intent.ACTION_VIEW, uri);
        targetIntent.addCategory(Intent.CATEGORY_BROWSABLE);
        targetIntent.setSelector(baseIntent);

        try {
            startActivity(targetIntent);
            Log.i(TAG, "Browser intent succesfully sent for URL: " + urlString);
        } catch (Exception e) {
            Log.e(TAG, "Kon geen app vinden om de URL te openen.", e);
            try {
                Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(fallbackIntent);
            } catch (Exception ex) {
                Log.e(TAG, "Zelfs de fallback intent is mislukt.", ex);
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
