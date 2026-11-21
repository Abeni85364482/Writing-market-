package com.abenezer.aritimarket;

import android.app.Activity;
import android.os.Bundle;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.net.http.SslError;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int FILE_PICKER_REQUEST = 4102;
    private static final String HOME = "https://abeni85364482.github.io/Writing-market-/";
    private static final String HOME_HOST = "abeni85364482.github.io";

    private WebView webView;
    private View errorView;
    private android.webkit.ValueCallback<Uri[]> fileCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout root = new FrameLayout(this);
        webView = new WebView(this);
        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(root);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(webView, true);

        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setBackgroundColor(Color.rgb(243, 245, 249));
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(Uri.parse(url));
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                hideError();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && request.isForMainFrame()) {
                    showError();
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel();
                showError();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view,
                    android.webkit.ValueCallback<Uri[]> callback,
                    WebChromeClient.FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;

                Intent intent;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    String[] accepted = params.getAcceptTypes();
                    String mime = "image/*";
                    if (accepted != null && accepted.length > 0 && accepted[0] != null
                            && !accepted[0].trim().isEmpty()) {
                        mime = accepted[0];
                    }
                    intent.setType(mime);
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*"});
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.getMode()
                            == FileChooserParams.MODE_OPEN_MULTIPLE);
                } else {
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                }

                try {
                    startActivityForResult(Intent.createChooser(intent, "Select product photo"),
                            FILE_PICKER_REQUEST);
                    return true;
                } catch (ActivityNotFoundException e) {
                    fileCallback = null;
                    return false;
                }
            }
        });

        if (savedInstanceState == null || webView.restoreState(savedInstanceState) == null) {
            webView.loadUrl(HOME);
        }
    }

    private boolean handleUrl(Uri uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if ("https".equalsIgnoreCase(scheme) && host != null
                && (host.equals(HOME_HOST) || host.equals("supabase.co")
                || host.endsWith(".supabase.co"))) {
            return false;
        }

        try {
            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)
                    || "mailto".equalsIgnoreCase(scheme) || "tel".equalsIgnoreCase(scheme)
                    || "whatsapp".equalsIgnoreCase(scheme)) {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        } catch (ActivityNotFoundException ignored) {
        }
        return true;
    }

    private void showError() {
        if (errorView != null) return;
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(32, 32, 32, 32);
        panel.setBackgroundColor(Color.rgb(243, 245, 249));

        TextView title = new TextView(this);
        title.setText("ARITI MARKET");
        title.setTextColor(Color.rgb(16, 28, 50));
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 12);
        panel.addView(title);

        TextView message = new TextView(this);
        message.setText("Connection problem. Please check your internet and try again.");
        message.setTextColor(Color.rgb(70, 80, 100));
        message.setTextSize(16);
        message.setGravity(Gravity.CENTER);
        message.setPadding(0, 0, 0, 20);
        panel.addView(message);

        Button retry = new Button(this);
        retry.setText("Try again");
        retry.setOnClickListener(v -> {
            hideError();
            webView.loadUrl(HOME);
        });
        panel.addView(retry);

        errorView = panel;
        ((FrameLayout) webView.getParent()).addView(errorView,
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private void hideError() {
        if (errorView != null) {
            ((FrameLayout) errorView.getParent()).removeView(errorView);
            errorView = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST && fileCallback != null) {
            Uri[] result = WebChromeClient.FileChooserParams.parseResult(resultCode, data);
            fileCallback.onReceiveValue(result);
            fileCallback = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (fileCallback != null) {
            fileCallback.onReceiveValue(null);
            fileCallback = null;
        }
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
