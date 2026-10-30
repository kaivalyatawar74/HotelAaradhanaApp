package com.hotelbill.app;

import android.app.Activity;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, android.webkit.WebResourceRequest r) {
                android.net.Uri u = r.getUrl();
                if ("rawbt".equals(u.getScheme())) {
                    try {
                        startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, u));
                    } catch (Exception e) {
                        android.widget.Toast.makeText(MainActivity.this, "Pehle RawBT app install karo", android.widget.Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
                return false;
            }
        });
        web.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void print() {
                runOnUiThread(() -> {
                    PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                    pm.print("Hotel_Bill", web.createPrintDocumentAdapter("Hotel_Bill"),
                            new PrintAttributes.Builder().build());
                });
            }
        }, "AndroidPrint");
        web.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
