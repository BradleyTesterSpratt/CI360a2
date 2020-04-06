package uk.ac.brighton.bst19.ci360a2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mashape.unirest.http.exceptions.UnirestException;

public class OverviewActivity extends AppCompatActivity {

  private TextView titleText, scoreText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent passedIntent = getIntent();
    String title = passedIntent.getStringExtra(Intent.EXTRA_TEXT);
    setContentView(R.layout.activity_overview);
    titleText = findViewById(R.id.titleText);
    scoreText = findViewById(R.id.scoreText);

    titleText.setText(title);
  }

}
