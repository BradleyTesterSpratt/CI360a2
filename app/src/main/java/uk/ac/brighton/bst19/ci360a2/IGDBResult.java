package uk.ac.brighton.bst19.ci360a2;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;
import java.util.AbstractMap.SimpleEntry;

import static java.util.stream.Collectors.toMap;


@RequiresApi(api = Build.VERSION_CODES.N)
public class IGDBResult {
  @SerializedName("name")
  public String name;
  @SerializedName("genres")
  public ArrayList<String> genres;
  @SerializedName("game_modes")
  public ArrayList<String> game_modes;
  @SerializedName("player_perspectives")
  public ArrayList<String> player_perspectives;
  @SerializedName("aggregated_rating")
  public String aggregated_rating;

  private Map<Integer, String> igdbGenres = Stream.of(
    new SimpleEntry<>(2, "Point and Click"),
    new SimpleEntry<>(4, "Fighting"),
    new SimpleEntry<>(5, "Shooter"),
    new SimpleEntry<>(7, "Music"),
    new SimpleEntry<>(8, "Platform"),
    new SimpleEntry<>(9, "Puzzle"),
    new SimpleEntry<>(10, "Racing"),
    new SimpleEntry<>(11, "Real Time Strategy (RTS)"),
    new SimpleEntry<>(12, "Role-playing (RPG)"),
    new SimpleEntry<>(13, "Simulator"),
    new SimpleEntry<>(14, "Sport"),
    new SimpleEntry<>(15, "Strategy"),
    new SimpleEntry<>(16, "Turn-based strategy (TBS)"),
    new SimpleEntry<>(24, "Tactical"),
    new SimpleEntry<>(25, "Hack and slash/Beat 'em up"),
    new SimpleEntry<>(26, "Quiz/Trivia"),
    new SimpleEntry<>(30, "Pinball"),
    new SimpleEntry<>(31, "Adventure"),
    new SimpleEntry<>(32, "Indie"),
    new SimpleEntry<>(33, "Arcade"),
    new SimpleEntry<>(34, "Visual Novel"))
    .collect(toMap(SimpleEntry::getKey, SimpleEntry::getValue));

  public GameData parseToGameData() {
    ArrayList<String> parsedGenres = new ArrayList<>();
    this.genres.forEach((genre) -> parsedGenres.add(parseGenre(genre)));
    ArrayList<String> parsedPerspectives = new ArrayList<>();
    this.player_perspectives.forEach((perspective) -> parsedPerspectives.add(parsePerspective(perspective)));
    GameData data = new GameData(name, parsedGenres, game_modes, parsedPerspectives, aggregated_rating);
    return data;
  }

  public String parseGenre(String genre) {
    Integer genreInt = Integer.parseInt(genre);
    return igdbGenres.get(genreInt);
  }

  public String parsePerspective(String perspective) {
    switch(perspective) {
      case "1":
        return "First Person";
      case "3":
        return "Third Person";
      default:
        return "Unknown Perspective";
    }
  }
}

