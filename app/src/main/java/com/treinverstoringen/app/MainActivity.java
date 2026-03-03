package com.treinverstoringen.app;
import com.treinverstoringen.app.R;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
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

    // Automatisch verversen
    private Handler refreshHandler = new Handler();
    private Runnable refreshRunnable;
    private static final int REFRESH_INTERVAL = 60000; // 60 seconden

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        // WebView instellingen
        WebSettings webSettings = webView.getSettings();
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus();
        webSettings.setJavaScriptEnabled(true);
        
        // === CACHE INSTELLINGEN AANPASSEN ===
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // NIET uit cache laden
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportZoom(false);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        
        // Cache volledig uitschakelen
        webView.clearCache(true); // Wis cache bij start
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webView.setLayerType(WebView.LAYER_TYPE_HARDWARE, null);
            CookieManager.getInstance().removeAllCookies(null); // Wis cookies
        }

        // WebViewClient met timeout
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);

                timeoutRunnable = new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                        webView.stopLoading();
                    }
                };
                timeoutHandler.postDelayed(timeoutRunnable, 5000);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                timeoutHandler.removeCallbacks(timeoutRunnable);
            }
        });

        // Start automatisch verversen
        startAutoRefresh();

        // Laad de URL
        String url = loadUrlFromConfig();
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        }
    }

    // === NIEUWE METHODE: Cache wissen voor refresh ===
    private void clearCacheAndReload() {
        if (webView != null) {
            // Wis alle caches
            webView.clearCache(true);
            webView.clearHistory();
            
            // Wis cookies (voor Android 5+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().flush();
            }
            
            // Laad de pagina opnieuw
            String url = loadUrlFromConfig();
            if (url != null && !url.isEmpty()) {
                webView.loadUrl(url);
            }
        }
    }

    // Aangepaste auto-refresh met cache wissen
    private void startAutoRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (webView != null) {
                    clearCacheAndReload(); // Wis cache EN herlaad
                }
                refreshHandler.postDelayed(this, REFRESH_INTERVAL);
            }
        };
        refreshHandler.postDelayed(refreshRunnable, REFRESH_INTERVAL);
    }

    // Stop verversen als app op achtergrond gaat
    @Override
    protected void onPause() {
        super.onPause();
        refreshHandler.removeCallbacks(refreshRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAutoRefresh();
    }

    // Afstandsbediening navigatie
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

    // OPTIE 3: Echt afsluiten bij terug-knop
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
                return true;
            } else {
                finishAffinity();
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    // URL uit config.json lezen
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
