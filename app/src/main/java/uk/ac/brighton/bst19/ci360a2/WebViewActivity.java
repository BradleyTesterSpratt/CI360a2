package uk.ac.brighton.bst19.ci360a2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity {

  private WebView webView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_web_view);
    Intent passedIntent = getIntent();
    webView = findViewById(R.id.webView);
    webView.setWebViewClient(new WebViewClient());
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setDomStorageEnabled(true);
    webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
    webView.loadUrl(passedIntent.getStringExtra(Intent.EXTRA_TEXT));

  }
}
