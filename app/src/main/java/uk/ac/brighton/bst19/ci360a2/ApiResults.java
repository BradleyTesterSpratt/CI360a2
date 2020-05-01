package uk.ac.brighton.bst19.ci360a2;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

class SearchResults {
  @SerializedName("results")
  public ArrayList<ApiResult> results;
}

class ApiResult {
  @SerializedName("reviewer")
  public String reviewer;
  @SerializedName("authors")
  public String authors;
  @SerializedName("score")
  public String score;
  @SerializedName("site_detail_url")
  public String site_detail_url;
  @SerializedName("api_detail_url")
  public String api_detail_url;
  @SerializedName("reviews")
  public ArrayList<ApiResult> reviews;
  @SerializedName("results")
  public ApiResult results;


  public Double standardisedScore(Double multiplier) {
    return Double.parseDouble(score) * multiplier;
  }

  public Review generateReview(String source, String scorePresentation) {
    String author = authors != null? authors : reviewer;
    return new Review(source, site_detail_url, author, score + scorePresentation);
  }
}
