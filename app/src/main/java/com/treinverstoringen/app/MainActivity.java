package com.treinverstoringen.app;
import com.treinverstoringen.app.R;

import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private Handler timeoutHandler = new Handler();
    private Runnable timeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar); // Zorg dat dit ID in je layout zit!

        // --- WebView instellingen (zoals je al had) ---
        WebSettings webSettings = webView.getSettings();
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(false);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
        }

        // ===== DE FIX VOOR HET DRAAIENDE LAADRONDJE =====
        webView.setWebViewClient(new WebViewClient() {

            // 1. Laadrondje verschijnt en timer start
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE); // Rondje ZICHTBAAR

                // Stel een time-out in (bijv. 5 seconden)
                timeoutRunnable = new Runnable() {
                    @Override
                    public void run() {
                        // Als de time-out afloopt, verberg het rondje en stop het laden
                        progressBar.setVisibility(View.GONE);
                        webView.stopLoading(); // Stopt als de pagina blijft hangen
                    }
                };
                timeoutHandler.postDelayed(timeoutRunnable, 5000); // 5000ms = 5 sec
            }

            // 2. Laadrondje verdwijnt en timer stopt
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE); // Rondje ONZICHTBAAR
                timeoutHandler.removeCallbacks(timeoutRunnable); // Timer annuleren
            }
        });

        // Laad de URL
        String url = loadUrlFromConfig();
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        }
    }

    // ===== Afstandsbediening navigatie (jouw code) =====
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    // ===== URL uit config.json lezen (jouw code) =====
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
