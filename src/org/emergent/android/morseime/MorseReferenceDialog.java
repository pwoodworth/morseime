/*
 * Copyright (c) 2010 Patrick Woodworth
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.emergent.android.morseime;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import java.util.EnumSet;

/**
 * @author Patrick Woodworth
 */
public class MorseReferenceDialog extends Dialog {

  private static final boolean USE_IMAGE = false;

  @SuppressWarnings({"ToArrayCallWithZeroLengthArrayArgument"})
  private static final MorseDigraph[] sm_digraphs = EnumSet.allOf(MorseDigraph.class).toArray(new MorseDigraph[0]);

  private int mColumnCount = 2;
  private int mTextColor;

  public MorseReferenceDialog(Context context) {
    super(context);
  }

  public MorseReferenceDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
    super(context, cancelable, cancelListener);
  }

  public MorseReferenceDialog(Context context, int theme) {
    super(context, theme);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.reference_dialog);

    Resources resources = getContext().getResources();
    setTitle(resources.getString(R.string.morse_code_reference));
    mTextColor = resources.getColor(R.color.refline_text);
    if (USE_IMAGE) {
      ScrollView scrollView = (ScrollView)findViewById(R.id.scroll_root);
      scrollView.setVisibility(View.GONE);
      ImageView image = (ImageView)findViewById(R.id.image);
      image.setVisibility(View.VISIBLE);
      image.setImageResource(R.drawable.morseref);

      final String refCardUrl = resources.getString(R.string.ref_card_link_uri);
      image.setOnClickListener(new View.OnClickListener() {
        public void onClick(View view) {
//          MorseReferenceDialog.this.cancel();
          MorseUtil.openUri(view.getContext(), refCardUrl);
        }
      });
    } else {
      TableLayout layout = (TableLayout)findViewById(R.id.table_layout);
      addRefRows(layout);
    }

//    Button closeButton = (Button)dialog.findViewById(R.id.close_button);
//    closeButton.setOnClickListener(new View.OnClickListener() {
//      public void onClick(View view) {
//        dialog.cancel();
//      }
//    });
  }

  private void addRefRows(TableLayout layout) {
    int rowCount = 0;
    rowCount += addRefRows(layout, 0, 25, mColumnCount, rowCount % 2 != 0);
    rowCount += addEmptyRow(layout, rowCount % 2 != 0);
    rowCount += addRefRows(layout, 26, 35, mColumnCount, rowCount % 2 != 0);
    rowCount += addEmptyRow(layout, rowCount % 2 != 0);
    rowCount += addRefRows(layout, 36, sm_digraphs.length - 1, mColumnCount, rowCount % 2 != 0);
  }

  private int addEmptyRow(TableLayout layout, boolean oddFirst) {
    addRefRow(layout, new MorseDigraph[mColumnCount], oddFirst);
    return 1;
  }

  private int addRefRows(TableLayout layout, int beginIdx, int endIdx, int columnCount, boolean oddFirst) {
    int digraphCount = (endIdx - beginIdx) + 1;
    int rowCount = digraphCount / columnCount;
    if (digraphCount % columnCount != 0)
      rowCount++;

    MorseDigraph digraphs[] = new MorseDigraph[columnCount];
    for (int ii = 0; ii < rowCount; ii++) {
      for (int jj = 0; jj < columnCount; jj++) {
        int digraphOffset = beginIdx + (ii) + (jj * rowCount);
        if (digraphOffset < sm_digraphs.length) {
          digraphs[jj] = sm_digraphs[digraphOffset];
        } else {
          digraphs[jj] = null;
        }
      }
      addRefRow(layout, digraphs, (ii % 2 == 1) ^ oddFirst);
    }
    return rowCount;
  }

  private void addRefRow(TableLayout layout, MorseDigraph[] digraphs, boolean oddRow) {
    Context context = layout.getContext();

    TableRow trow = new TableRow(context);
    trow.setGravity(Gravity.CENTER);

    for (MorseDigraph digraph : digraphs) {
      trow.addView(createTextView(context, digraph != null ? digraph.getDecoding().toUpperCase() : null, oddRow));
      trow.addView(createTextView(context, digraph != null ? digraph.getEncodingPretty() : null, oddRow));
      oddRow = !oddRow;
    }

    layout.addView(trow, new TableRow.LayoutParams(
        TableRow.LayoutParams.FILL_PARENT,
        TableRow.LayoutParams.WRAP_CONTENT));
  }

  private TextView createTextView(Context context, String text, boolean oddRow) {
    TextView encTextView = new TextView(context);
    if (text != null) {
//      encTextView.setText(text);
      encTextView.setText(text, TextView.BufferType.SPANNABLE);
      encTextView.setTextColor(mTextColor);
      encTextView.setBackgroundResource(oddRow ? R.color.refline_odd : R.color.refline_even);

//      // Get the EditText's internal text storage
//      Spannable str = (Spannable)encTextView.getText();
//      // Create our span sections, and assign a format to each.
//      str.setSpan(new TypefaceSpan("monospace"), 0, str.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    encTextView.setGravity(Gravity.LEFT);
    encTextView.setPadding(
        encTextView.getPaddingLeft() + 5,
        encTextView.getPaddingTop(),
        encTextView.getPaddingRight() + 5,
        encTextView.getPaddingBottom()
    );
    return encTextView;
  }

}
