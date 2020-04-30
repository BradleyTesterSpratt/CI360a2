package uk.ac.brighton.bst19.ci360a2;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GamespotResults {
  @SerializedName("results")
  public ArrayList<GamespotResult> results;
}

class GamespotResult {
  @SerializedName("authors")
  public String authors;
  @SerializedName("score")
  public String score;
  @SerializedName("site_detail_url")
  public String site_detail_url;

  public Double standardisedScore() {
    return Double.parseDouble(score) * 10;
  }

  public Review generateReview() {
    return new Review("Gamespot", site_detail_url, authors, score+"/10");
  }
}
