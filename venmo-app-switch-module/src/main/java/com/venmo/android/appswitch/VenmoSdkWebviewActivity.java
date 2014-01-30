package com.venmo.android.appswitch;

import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class VenmoSdkWebviewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
            getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
            getActionBar().hide();
        } else {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        WebView webview = new WebView(this);
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                setProgress(newProgress *
                            1000); // http://developer.android.com/reference/android/webkit/WebView.html
            }
        });
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        Bundle extras = getIntent().getExtras();
        String url = String.format(
                "https://venmo.com/touch/signup_to_pay?client=android&app_id=%s&app_name=%s&amount=%f&txn=%s&recipients=%s&note=%s",
                extras.getString(TransactionRequest.EXTRA_APP_ID),
                extras.getString(TransactionRequest.EXTRA_APP_NAME),
                extras.getDouble(TransactionRequest.EXTRA_AMOUNTS),
                extras.getString(TransactionRequest.EXTRA_PAY_CHARGE),
                extras.getString(TransactionRequest.EXTRA_TARGETS),
                extras.getString(TransactionRequest.EXTRA_NOTE));
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl(url);
        setContentView(webview);
    }
}
