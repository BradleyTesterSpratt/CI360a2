package uk.ac.brighton.bst19.ci360a2;


import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

/*
 * https://stackoverflow.com/questions/2394935/can-i-underline-text-in-an-android-layout/29092099
 */
public class UnderLineTextView extends androidx.appcompat.widget.AppCompatTextView {

  public UnderLineTextView(Context context) {
    super(context);
    this.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
  }

  public UnderLineTextView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    this.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
  }

}

