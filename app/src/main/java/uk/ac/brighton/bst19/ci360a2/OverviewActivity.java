package uk.ac.brighton.bst19.ci360a2;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OverviewActivity extends AppCompatActivity {
  private GameData data;
  private TextView scoreText, genresText;
  private LinearLayout reviewsSourceLayout, reviewsAuthorLayout, reviewsUrlLayout, reviewsScoreLayout;
  private Boolean titleFound = false;
  private OkHttpClient client = new OkHttpClient();
  private Request request;
  private Gson gson = new Gson();

  public OverviewActivity() throws IOException {
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_overview);
    Intent passedIntent = getIntent();
    data = passedIntent.getParcelableExtra("data");
    ActionBar toolbar = getSupportActionBar();
    toolbar.setDisplayHomeAsUpEnabled(true);
    toolbar.setTitle(data.name);
    scoreText = findViewById(R.id.scoreText);
    genresText = findViewById(R.id.genresText);
    reviewsSourceLayout = findViewById(R.id.reviewsSourceLayout);
    reviewsAuthorLayout = findViewById(R.id.reviewsAuthorLayout);
    reviewsUrlLayout = findViewById(R.id.reviewsUrlLayout);
    reviewsScoreLayout = findViewById(R.id.reviewsScoreLayout);
    try {
      gamespotGetRequest(data.name);
      while(!titleFound) {
        TimeUnit.SECONDS.sleep(1);
      }
      setTexts();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

/*
 *  https://stackoverflow.com/questions/33540497/
 *  using-onoptionsitemselected-to-go-up-from-preferenceactivity-with-preferencefrag/34531238
 */
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

  private Request buildGetRequest(String api_key, String url, String filterString, String name, String queryString) throws UnsupportedEncodingException {
    String encodedName = encodeString(name);
    return new Request.Builder()
      .url(url + api_key + filterString + encodedName + queryString)
      .method("GET", null)
      .build();
  }


  private String encodeString(String string) throws UnsupportedEncodingException {
    return URLEncoder.encode(string, "UTF-8");
  }

  private void gamespotGetRequest(String name) throws UnsupportedEncodingException {
    request = buildGetRequest("c6141eeedb92dab9632695da584667a95a8f767f",
      "https://www.gamespot.com/api/reviews/?api_key=",
      "&filter=title:",
      name,
      "&field_list=authors,score,site_detail_url&format=json");

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        String mMessage = e.getMessage().toString();
        Log.w("failure Response", mMessage);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        SearchResults results = gson.fromJson(response.body().string(), SearchResults.class);
        ApiResult gamespotResult = results.results.get(0);
        data.addScore(gamespotResult.standardisedScore(10.0));
        data.addReview(gamespotResult.generateReview("Gamespot", "/10"));
        giantbombSearchGetRequest(name);
      }
    });
  }

    private void giantbombSearchGetRequest(String name) throws UnsupportedEncodingException {
      String api_key = "b069ba20800e3053230a2d9df970131a9e95728b";
      request = buildGetRequest(
        api_key,
        "https://www.giantbomb.com/api/search/?api_key=",
        "&query=",
        name,
        "&resources=game&field_list=api_detail_url&format=json");

      client.newCall(request).enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          String mMessage = e.getMessage().toString();
          Log.w("failure Response", mMessage);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
          SearchResults searchResults = gson.fromJson(response.body().string(), SearchResults.class);
          ApiResult searchResult = searchResults.results.get(0);
          giantbombGameGetRequest(api_key, searchResult.api_detail_url + "?api_key=");
        }
      });
  }

  public void giantbombGameGetRequest(String api_key, String url) throws UnsupportedEncodingException {
    request = buildGetRequest(api_key, url, "&field_list=reviews&format=json", "", "");

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        String mMessage = e.getMessage().toString();
        Log.w("failure Response", mMessage);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        ApiResult searchResults = gson.fromJson(response.body().string(), ApiResult.class);
        ApiResult searchResult = searchResults.results;
        ApiResult reviewResults = searchResult.reviews.get(0);
        giantBombReviewGetRequest(api_key, reviewResults.api_detail_url + "?api_key=");
      }
    });
  }

  public void giantBombReviewGetRequest(String api_key, String url) throws UnsupportedEncodingException {
    request = buildGetRequest(api_key, url, "&format=json", "", "");
    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        String mMessage = e.getMessage().toString();
        Log.w("failure Response", mMessage);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        ApiResult giantBombResults = gson.fromJson(response.body().string(), ApiResult.class);
        ApiResult giantBombResult = giantBombResults.results;
        data.addScore(giantBombResult.standardisedScore(20.0));
        data.addReview(giantBombResult.generateReview("GiantBomb", "/5"));
        titleFound = true;
      }
    });
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  private void setTexts() {
//    titleText.setText(data.name);
    scoreText.setText(data.score());
    genresText.setText(data.genres.toString() + "\n" + data.perspectives);
    data.reviews.forEach(review -> generateReviewPanels(review));
  }

  public void generateReviewPanels(Review review) {
    generateTextView(reviewsSourceLayout, review.source);
    generateTextView(reviewsAuthorLayout, review.author);
    generateTextView(reviewsScoreLayout, review.score);
    generateWebViewButton(reviewsUrlLayout, review.url);
  }

  public void generateTextView(LinearLayout layout, String text) {
    TextView textView = new TextView(this);
    textView.setText(text);
    textView.setLayoutParams(new ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.WRAP_CONTENT,
      ViewGroup.LayoutParams.WRAP_CONTENT));
    layout.addView(textView);
  }

  public void generateWebViewButton(LinearLayout layout, String url) {
    Button button = new Button(this);
    button.setText("Read");
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        visitReview(url);
      }
    });
    layout.addView(button);
  }

  private void visitReview(String url) {
    Intent intentToPass = new Intent(this, WebViewActivity.class);
    Bundle dataToPass = new Bundle();
    dataToPass.putString("url",url);
    dataToPass.putString("title",data.name);
    intentToPass.putExtras(dataToPass);
    startActivity(intentToPass);
  }

}
