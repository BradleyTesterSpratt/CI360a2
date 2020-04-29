package uk.ac.brighton.bst19.ci360a2;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
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
  private IGDBResult igdbResult;
  private TextView titleText, scoreText, genresText;
  private Boolean titleFound = false;

  public OverviewActivity() throws IOException {
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent passedIntent = getIntent();
    String title = passedIntent.getStringExtra(Intent.EXTRA_TEXT);
    setContentView(R.layout.activity_overview);
    titleText = findViewById(R.id.titleText);
    scoreText = findViewById(R.id.scoreText);
    genresText = findViewById(R.id.genresText);
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

      @Override
      public void onResponse(Call call, Response response) throws IOException {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<Collection<IGDBResult>>(){}.getType();
        Collection<IGDBResult> results = gson.fromJson(response.body().string(), collectionType);
        igdbResult = results.iterator().next();
        titleFound = true;
      }
    });
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public void setTexts() {
    GameData data = igdbResult.parseToGameData();
    titleText.setText(data.name);
    scoreText.setText(data.score());
    genresText.setText(data.genres.toString() + "\n" + data.perspectives);
  }

}
