package uk.ac.brighton.bst19.ci360a2;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class GameData implements Parcelable {
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
    this.scores = new ArrayList<>();
    this.reviews = new ArrayList<>();
    addScore(score);
  }

  protected GameData(Parcel in) {
    name = in.readString();
    genres = in.createStringArrayList();
    gameModes = in.createStringArrayList();
    perspectives = in.createStringArrayList();
  }

  public static final Creator<GameData> CREATOR = new Creator<GameData>() {
    @Override
    public GameData createFromParcel(Parcel in) {
      return new GameData(in);
    }

    @Override
    public GameData[] newArray(int size) {
      return new GameData[size];
    }
  };

  @RequiresApi(api = Build.VERSION_CODES.N)
  public String score() {
    final Double[] totalScores = {0.00};
    this.scores.forEach((score) -> {
      totalScores[0] = totalScores[0] + score;});
    Double average = totalScores[0] /this.scores.size();
    return Math.round(average) + "/100";
  }

  public void addScore(Double score) {
    if(scores == null) {
      this.scores = new ArrayList<>();
    }
    this.scores.add(score);
  }

  public void addScore(String score) {
    if(scores == null) {
      this.scores = new ArrayList<>();
    }
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
    if(reviews == null) {
      this.reviews = new ArrayList<Review>();
    }
    this.reviews.add(review);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
    dest.writeStringList(genres);
    dest.writeStringList(gameModes);
    dest.writeStringList(perspectives);
  }
}
