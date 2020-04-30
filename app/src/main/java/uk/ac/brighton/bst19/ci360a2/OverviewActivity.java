package uk.ac.brighton.bst19.ci360a2;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OverviewActivity extends AppCompatActivity {
  private GameData data;
  private TextView titleText, scoreText, genresText;
  private LinearLayout reviewsSourceLayout, reviewsAuthorLayout, reviewsUrlLayout, reviewsScoreLayout;
  private Boolean titleFound = false;

  public OverviewActivity() throws IOException {
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_overview);
    Intent passedIntent = getIntent();
    String title = passedIntent.getStringExtra(Intent.EXTRA_TEXT);
    titleText = findViewById(R.id.titleText);
    scoreText = findViewById(R.id.scoreText);
    genresText = findViewById(R.id.genresText);
    reviewsSourceLayout = findViewById(R.id.reviewsSourceLayout);
    reviewsAuthorLayout = findViewById(R.id.reviewsAuthorLayout);
    reviewsUrlLayout = findViewById(R.id.reviewsUrlLayout);
    reviewsScoreLayout = findViewById(R.id.reviewsScoreLayout);
    try {
      igdbPostRequest(title);
      while(!titleFound) {
        TimeUnit.SECONDS.sleep(1);
      }
      setTexts();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }

  }

  public void igdbPostRequest(final String searchTerm) throws IOException {

    MediaType mediaType = MediaType.parse("text/plain");

    OkHttpClient client = new OkHttpClient();

    RequestBody body = RequestBody.create(mediaType, "search \"" + searchTerm + "\"; fields name, genres, game_modes, player_perspectives, aggregated_rating; where version_parent = null;");

    Request request = new Request.Builder()
      .url("https://api-v3.igdb.com/games/")
      .post(body)
      .header("Content-Type", "text/plain")
      .header("user-key", "49681339a8319428dd737e99fbf9681e")
      .build();

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        String mMessage = e.getMessage().toString();
        Log.w("failure Response", mMessage);
        //call.cancel();
      }

      @RequiresApi(api = Build.VERSION_CODES.N)
      @Override
      public void onResponse(Call call, Response response) throws IOException {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<IGDBResult>>(){}.getType();
        Collection<IGDBResult> results = gson.fromJson(response.body().string(), collectionType);
        IGDBResult igdbResult = results.iterator().next();
        data = igdbResult.parseToGameData();
        gamespotGetRequest(data.name);
      }
    });
  }

  private String encodeString(String string) throws UnsupportedEncodingException {
    return URLEncoder.encode(string, "UTF-8");
  }

  private void gamespotGetRequest(String name) throws UnsupportedEncodingException {
    OkHttpClient client = new OkHttpClient();
    String api_key = "c6141eeedb92dab9632695da584667a95a8f767f";
    String encodedName = encodeString(name);
    Request request = new Request.Builder()
      .url("https://www.gamespot.com/api/reviews/?api_key=" + api_key + "&filter=title:" + encodedName + "&field_list	=authors,score,site_detail_url&format=json")
      .method("GET", null)
      .build();

    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        String mMessage = e.getMessage().toString();
        Log.w("failure Response", mMessage);
      }

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        Gson gson = new Gson();
        GamespotResults results;
        results = gson.fromJson(response.body().string(), GamespotResults.class);
        GamespotResult gamespotResult = results.results.get(0);
        data.addScore(gamespotResult.standardisedScore());
        data.addReview(gamespotResult.generateReview());
        titleFound = true;
      }
    });

  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  private void setTexts() {
    titleText.setText(data.name);
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
    String dataToPass = url;
    Intent intentToPass = new Intent(this, WebViewActivity.class);
    intentToPass.putExtra(Intent.EXTRA_TEXT, dataToPass);
    startActivity(intentToPass);
  }

}
