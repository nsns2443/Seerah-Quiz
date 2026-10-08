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

/**
 * এই অ্যাপটা মূলত আপনার সীরাত কুইজ পোর্টাল ওয়েবসাইটটাকেই একটা নেটিভ Android
 * অ্যাপের "খোলস" (shell)-এর ভেতরে দেখায় — ঠিক যেভাবে আগের LifeManager অ্যাপেও
 * করা হয়েছিল। ওয়েবসাইটে যা পরিবর্তন করবেন, অ্যাপেও সাথে সাথে তা দেখা যাবে —
 * আলাদাভাবে অ্যাপ আপডেট করার প্রয়োজন নেই।
 */
public class MainActivity extends AppCompatActivity {

    // আপনার কুইজ পোর্টালের ডিপ্লয়-করা লিংক। ভবিষ্যতে Apps Script-এ নতুন করে
    // "New deployment" করলে লিংক বদলে যেতে পারে — তখন শুধু এই একটা লাইন বদলে
    // আবার অ্যাপ বিল্ড করলেই অ্যাপ নতুন লিংকে চলবে।
    private static final String SITE_URL =
            "https://script.google.com/macros/s/AKfycbxvaZzSRaXIkXdRK3bWPGemcIp20ZUzN2_IWzCUvbF4CGdbWXhkC0vUvJH5Y0ZL8fMB/exec";

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

                // নিজের সাইটের লিংক (ও Google-এর রিডাইরেক্ট ডোমেইন) ওয়েবভিউর ভেতরেই থাকবে
                if (host.contains("script.google.com") || host.contains("googleusercontent.com")) {
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
                // মূল পেজ লোড করতেই সমস্যা হলে এরর স্ক্রিন দেখানো হবে (ভেতরের কোনো ছোট রিসোর্স
                // ফেইল করলে পুরো স্ক্রিন এরর দেখানো হবে না)
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
