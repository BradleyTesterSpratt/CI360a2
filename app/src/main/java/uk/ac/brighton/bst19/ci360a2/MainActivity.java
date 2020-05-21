package uk.ac.brighton.bst19.ci360a2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity{
  private Button textButton, photoButton, galleryButton;
  private Boolean titleFound = false;
  private GameData gameData;
  private FirebaseVisionText titleTexts;
  ImageView imageView;
  File photoFile;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    imageView = findViewById(R.id.imageView);
    textButton = findViewById(R.id.grabButton);
    photoButton = findViewById(R.id.photoButton);
    galleryButton = findViewById(R.id.galleryButton);

    textButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        grabText();
      }
    });

    photoButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dispatchTakePictureIntent();
      }
    });

    galleryButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        selectPhoto();
      }
    });
  }

  static final int REQUEST_TAKE_PHOTO = 1;

  private void dispatchTakePictureIntent() {
    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    // Ensure that there's a camera activity to handle the intent
    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
      // Create the File where the photo should go
      try {
        photoFile = createImageFile();
      } catch (IOException ex) {
        // Error occurred while creating the File
      }
      // Continue only if the File was successfully created
      if (photoFile != null) {
        Uri photoURI = FileProvider.getUriForFile(this,
          "uk.ac.brighton.bst19.ci360a2.fileprovider",
          photoFile);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
      }
    }
  }

  public static final int PICK_IMAGE = 2;

  private void selectPhoto() {
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
  }


  //  Changed to extract the URI to global,
  //  then get URI from global, rather than intent
  //  https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
      Bitmap imageBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
      imageView.setImageBitmap(imageBitmap);
    }
    if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
      Uri uri = data.getData();
      imageView.setImageURI(uri);
    }
  }


  private File createImageFile() throws IOException {
    File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
    if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
    }
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String imageFileName = "JPEG_" + timeStamp + "_";
    File file = new File(mediaStorageDir.getPath() + File.separator + imageFileName);
    return file;
  }

  protected FirebaseVisionImage captureImage() {
    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
    if(drawable != null) {
      Bitmap bitmap = drawable.getBitmap();
      if (bitmap != null) {
        return FirebaseVisionImage.fromBitmap(bitmap);
      }
      return null;
    } else {
      return null;
    }
  }

  protected void grabText() {
    FirebaseVisionImage capturedImage = captureImage();
    if(capturedImage != null) {
      FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
      textButton.setEnabled(false);
      Task<FirebaseVisionText> task = recognizer.processImage(capturedImage);
      task.addOnSuccessListener(
        new OnSuccessListener<FirebaseVisionText>() {
          @RequiresApi(api = Build.VERSION_CODES.O)
          @Override
          public void onSuccess(FirebaseVisionText texts) {
            textButton.setEnabled(true);
            Log.i("Result", texts.getText());
            titleTexts = texts;
            try {
              getResult();
            } catch (IOException e) {
              e.printStackTrace();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        })
        .addOnFailureListener(
          new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              // Task failed with an exception
              textButton.setEnabled(true);
              e.printStackTrace();
            }
          });
    } else {
      showToast("Image Needed");
    }
  }

  private void showToast(String message) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private void getResult() throws IOException, InterruptedException {
    //reset it to false, otherwise it never waits for the response if called a second time
    titleFound = false;
    int attempts = 0;
    igdbPostRequest(titleTexts.getText());
    while(!titleFound && attempts < 3) {
      TimeUnit.SECONDS.sleep(1);
      attempts++;
    }
    if(titleFound) {
      startOverviewActivity(gameData);
    } else {
      showToast("Game Not Found");
    }
  }

  private void startOverviewActivity(GameData dataToPass) {
    Intent intentToPass = new Intent(this, OverviewActivity.class);
    intentToPass.putExtra("data", dataToPass);
    startActivity(intentToPass);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private void igdbPostRequest(String searchTerm) throws IOException {
    searchTerm = searchTerm.replaceAll("\n", " ");
    searchTerm = searchTerm.replaceAll("[^a-zA-Z0-9,' -]+", "");
    MediaType mediaType = MediaType.parse("text/plain");
    // prevent reviews for non-released games
    String currentDate = Long.toString(Instant.now().getEpochSecond());
    RequestBody body = RequestBody.create(
      mediaType,
      "search \"" +
        searchTerm +
        "\"; fields name, genres, game_modes, player_perspectives, aggregated_rating; " +
        // exclude platform 39 (mobile doesnt have a game box)
        // exclude where version_parent and parent_game exist as we only want the main game
        // first release date before now to only show games that are currently released
        "where version_parent = null & parent_game = null & first_release_date <= " + currentDate + " & platforms != 39  ;");
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
      .url("https://api-v3.igdb.com/games/")
      .post(body)
      .header("Content-Type", "text/plain")
      .header("user-key", "49681339a8319428dd737e99fbf9681e")
      .build();

    String finalSearchTerm = searchTerm;
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
        Type collectionType = new TypeToken<Collection<IGDBResult>>() {
        }.getType();
        Gson gson = new Gson();
        Collection<IGDBResult> results = gson.fromJson(response.body().string(), collectionType);
        if (!results.isEmpty()) {
          IGDBResult igdbResult = results.iterator().next();
          gameData = igdbResult.parseToGameData();
          titleFound = true;
        }
      }
    });
  }
}