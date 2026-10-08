package com.azharul.seerahquiz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // অ্যাপ খুললে এই লিংকটা প্রথমে লোড হবে। ভবিষ্যতে লিংক বদলাতে চাইলে শুধু এই
    // একটা লাইন বদলে আবার অ্যাপ বিল্ড করলেই অ্যাপ নতুন লিংকে চলবে।
    private static final String SITE_URL = "https://seerahquizbd.blogspot.com/";

    private WebView webView;
    private View errorView;
    private boolean pageLoadedOnce = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        errorView = findViewById(R.id.error_view);
        findViewById(R.id.btn_retry).setOnClickListener(v -> reload());

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String host = uri.getHost() == null ? "" : uri.getHost();

                // নিজের সাইটের লিংক (Blogspot পেজ, আর তার ভেতর থেকে যদি কুইজ অংশের
                // জন্য script.google.com-এ যাওয়া হয়, সেটাও) ওয়েবভিউর ভেতরেই থাকবে
                if (host.contains("blogspot.com")
                        || host.contains("blogger.com")
                        || host.contains("script.google.com")
                        || host.contains("googleusercontent.com")) {
                    return false;
                }

                // WhatsApp গ্রুপ, ফোন/ইমেইল লিংক বা অন্য কোনো বাইরের লিংক হলে
                // ফোনের নিজের অ্যাপ (WhatsApp/ডায়ালার/ব্রাউজার) দিয়ে খোলা হবে
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception ignored) {
                    // উপযুক্ত অ্যাপ ফোনে না থাকলে কিছু হবে না, অ্যাপ বন্ধ হয়ে যাবে না
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                pageLoadedOnce = true;
                showWebView();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame() && !pageLoadedOnce) {
                    showErrorView();
                }
            }
        });

        webView.loadUrl(SITE_URL);
    }

    private void reload() {
        errorView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl(SITE_URL);
    }

    private void showWebView() {
        errorView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
    }

    private void showErrorView() {
        webView.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
