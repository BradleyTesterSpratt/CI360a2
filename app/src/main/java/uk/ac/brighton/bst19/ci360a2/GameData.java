package uk.ac.brighton.bst19.ci360a2;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class GameData {
  protected String name;
  protected ArrayList<String> genres;
  protected ArrayList<String> gameModes;
  protected ArrayList<String> perspectives;
  protected ArrayList<Double> scores;
  protected ArrayList<Review> reviews;

  public GameData(String name, ArrayList<String> genres, ArrayList<String> gameModes, ArrayList<String> perspectives, String score) {
    this.name = name;
    this.genres = genres;
    this.gameModes = gameModes;
    this.perspectives = perspectives;
    this.scores = new ArrayList<Double>();
    this.reviews = new ArrayList<Review>();
    addScore(score);
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public String score() {
    final Double[] totalScores = {0.00};
    this.scores.forEach((score) -> {
      totalScores[0] = totalScores[0] + score;});
    Double average = totalScores[0] /this.scores.size();
    return Math.round(average) + "/100";
  }

  public void addScore(Double score) {
    this.scores.add(score);
  }

  public void addScore(String score) {
    this.scores.add(Double.parseDouble(score));
  }

  public void addGenres(String genre) {
    //check that genre is not already in list
    this.genres.add(genre);
    //or format list to remove duplicates (Use regex? contains?)
  }

  public void addGameMode(String gameMode) {
    //check that genre is not already in list
    this.gameModes.add(gameMode);
    //or format list to remove duplicates (Use regex? contains?)
  }

  public void addPerspective(String perspective) {
    //check that genre is not already in list
    this.perspectives.add(perspective);
    //or format list to remove duplicates (Use regex? contains?)
  }

  public void addReview(Review review) {
    this.reviews.add(review);
  }


}
