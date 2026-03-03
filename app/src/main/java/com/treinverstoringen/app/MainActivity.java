package com.treinverstoringen.app;
import com.treinverstoringen.app.R;

import android.os.Bundle;
import android.os.Build;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();

        // ===== TV-OPTIMALISATIES =====
        // 1. Navigatie met afstandsbediening inschakelen
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus();

        // 2. JavaScript (altijd nodig)
        webSettings.setJavaScriptEnabled(true);

        // 3. Snelle caching
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setDomStorageEnabled(true);

        // 4. Belangrijk voor TV: schaal de website goed
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(false); // Uit voor TV

        // 5. HTML5 video/fullscreen ondersteuning
        webSettings.setMediaPlaybackRequiresUserGesture(false); // Video automatisch starten

        // 6. Hardware versnelling (voor soepele ervaring)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
        }

        // 7. Aangepaste WebViewClient om links in de WebView te houden
        webView.setWebViewClient(new WebViewClient());

        // Laad de URL uit config.json
        String url = loadUrlFromConfig();
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        }
    }

    // ===== BELANGRIJK VOOR TV: Afstandsbediening =====
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // DPAD-knoppen doorsturen naar WebView
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP ||
            keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
            keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
            keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ||
            keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
            keyCode == KeyEvent.KEYCODE_ENTER) {
            
            if (webView != null) {
                webView.onKeyDown(keyCode, event);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // Terug-knop: ga terug in webgeschiedenis of sluit app
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
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
