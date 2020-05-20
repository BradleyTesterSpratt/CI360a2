package uk.ac.brighton.bst19.ci360a2;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity {

  private WebView webView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_web_view);
    Intent passedIntent = getIntent();
    Bundle data = passedIntent.getExtras();
    ActionBar toolbar = getSupportActionBar();
    toolbar.setDisplayHomeAsUpEnabled(true);
    toolbar.setTitle(data.getString("title"));
    webView = findViewById(R.id.webView);
    webView.setWebViewClient(new WebViewClient());
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setDomStorageEnabled(true);
    webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
    webView.loadUrl(data.getString("url"));
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
