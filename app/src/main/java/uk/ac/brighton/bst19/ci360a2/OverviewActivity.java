package uk.ac.brighton.bst19.ci360a2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

public class OverviewActivity extends AppCompatActivity {

  private TextView titleText, scoreText;

  public OverviewActivity() {
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent passedIntent = getIntent();
    String title = passedIntent.getStringExtra(Intent.EXTRA_TEXT);
    setContentView(R.layout.activity_overview);
    titleText = findViewById(R.id.titleText);
    scoreText = findViewById(R.id.scoreText);
    HttpResponse<String> response = null;
    try {
      response = restCall("title");
    } catch (UnirestException e) {
      e.printStackTrace();
    }

    titleText.setText(title);
    Log.println(Log.ASSERT, "response", response.toString());
  }

  public HttpResponse<String> restCall(String searchText) throws UnirestException {
    return Unirest.post("https://api-v3.igdb.com/games/")
      .header("user-key", "49681339a8319428dd737e99fbf9681e")
      .body("search " + searchText + "; fields name, storyline; where version_parent = null;")
      .asString();
  }
}
