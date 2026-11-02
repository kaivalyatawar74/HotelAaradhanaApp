package com.hotelbill.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.speech.RecognizerIntent;
import java.util.ArrayList;
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
                if ("rawbt".equals(u.getScheme()) || "whatsapp".equals(u.getScheme()) || "wa.me".equals(u.getHost())) {
                    try {
                        startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, u));
                    } catch (Exception e) {
                        android.widget.Toast.makeText(MainActivity.this, "Pehle ye app install karo (RawBT ya WhatsApp)", android.widget.Toast.LENGTH_LONG).show();
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
        web.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void start(String lang) {
                runOnUiThread(() -> {
                    try {
                        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
                        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Boliye: do chai ek samosa");
                        startActivityForResult(i, 77);
                    } catch (Exception e) {
                        web.evaluateJavascript("toast('Is phone mein awaaz ki service nahi hai')", null);
                    }
                });
            }
        }, "AndroidVoice");
        web.loadUrl("file:///android_asset/index.html");
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 77 && res == RESULT_OK && data != null) {
            ArrayList<String> r = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (r != null && !r.isEmpty()) {
                String t = r.get(0).replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ");
                web.evaluateJavascript("applyVoice('" + t + "')", null);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
