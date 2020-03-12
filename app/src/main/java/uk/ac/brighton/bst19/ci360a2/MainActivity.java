package uk.ac.brighton.bst19.ci360a2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity extends AppCompatActivity{
  private Button textButton, photoButton;
  private EditText imageUrlString;
  ImageView imageView;
  File photoFile;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    imageView = findViewById(R.id.imageView);
    textButton = findViewById(R.id.grabButton);
    photoButton = findViewById(R.id.photoButton);
    imageUrlString = findViewById(R.id.imageUrlField);

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

    imageUrlString.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          getImageFromUrl();
          return true;
        }
        return false;
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

  static final int REQUEST_IMAGE_CAPTURE = 1;

//  Changed to extract the URI to global,
//  then get URI from global, rather than intent
//  https://guides.codepath.com/android/Accessing-the-Camera-and-Stored-Media

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      Bitmap imageBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
      Log.println(Log.ASSERT, "image", imageBitmap.toString());
      imageView.setImageBitmap(imageBitmap);
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

  protected void getImageFromUrl() {
    Picasso.get().load(imageUrlString.getText().toString()).into(imageView);
  }

  protected FirebaseVisionImage captureImage() {
    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
    if(drawable != null) {
      Bitmap bitmap = drawable.getBitmap();
      return FirebaseVisionImage.fromBitmap(bitmap);
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
          @Override
          public void onSuccess(FirebaseVisionText texts) {
            textButton.setEnabled(true);
            Log.i("Result", texts.getText());
            getResult(texts);
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

  private void getResult(FirebaseVisionText texts) {
    String dataToPass = texts.getText();
    Intent intentToPass = new Intent(this, OverviewActivity.class);
    intentToPass.putExtra(Intent.EXTRA_TEXT, dataToPass);
    startActivity(intentToPass);
//    showToast(texts.getText());
  }

}