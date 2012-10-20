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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.*;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.EnumSet;
import java.util.Random;

/**
 * @author Patrick Woodworth
 */
public class PracticeActivity extends BasicActivity {

  @SuppressWarnings({"ToArrayCallWithZeroLengthArrayArgument"})
  private static final MorseDigraph[] sm_digraphs = EnumSet.allOf(MorseDigraph.class).toArray(new MorseDigraph[0]);

  private final Handler mHandler = new Handler();
  private final Random mRandom = new Random();
  private int mColorBad;
  private int mColorGood;

  private final boolean[] mLettersOn = new boolean[4];
  private boolean mDigitsOn;
  private boolean mSymbolsOn;

  private MorseDigraph mDigraph;
  private PracticeState mState = PracticeState.SHOWING_ANSWER;
  private int mScoreNumerator = 0;
  private int mScoreDenominator = 0;
  private int mAnswerTimeout;

  private TextView mQuestionText;
  private TextView mAnswerText;
  private TextView mScoreText;
  private EditText mAnswerEdit;

  public PracticeActivity() {
  }

  private Runnable m_handlerCallback = new Runnable() {
    public void run() {
      resetQuestion();
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.practice);
    mColorBad = getResources().getColor(R.color.answer_bad);
    mColorGood = getResources().getColor(R.color.answer_good);
    loadPreferences();

    mQuestionText = (TextView)findViewById(R.id.questiontext);
    mAnswerText = (TextView)findViewById(R.id.answertext);
    mScoreText = (TextView)findViewById(R.id.scoretext);
    mAnswerEdit = (EditText)findViewById(R.id.answeredit);

    Button resetButton = (Button)findViewById(R.id.resetbutton);
    resetButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        resetScore();
        resetQuestion();
      }
    });

    mAnswerEdit.setOnKeyListener(new View.OnKeyListener() {
      public boolean onKey(View view, int keyCode, KeyEvent event) {
        LOG.debug("onKey: keyCode = %s ; chars = \"%s\"", keyCode, event.getCharacters());
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
          if (event.getAction() == KeyEvent.ACTION_UP && mState == PracticeState.WAITING_ANSWER)
            checkAnswer();
          return true;
        }
        return false;
      }
    });

    mAnswerEdit.setFilters(new InputFilter[] { new PracticeFilter() });
    
    mHandler.post(m_handlerCallback);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mHandler.removeCallbacks(m_handlerCallback);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    loadPreferences();
    super.onActivityResult(requestCode, resultCode, data);
  }

  private void resetQuestion() {
    mHandler.removeCallbacks(m_handlerCallback);
    mState = PracticeState.WAITING_ANSWER;
    mDigraph = nextDigraph(mDigraph);
    mQuestionText.setText(mDigraph.getDecoding().toUpperCase());
    mAnswerText.setText("");
    mAnswerEdit.setEnabled(true);
    mAnswerEdit.setText("");
  }

  private void checkAnswer() {
    mState = PracticeState.SHOWING_ANSWER;
    mAnswerText.setText(mDigraph.getEncodingPretty());
    String answer = mAnswerEdit.getText().toString();
    if (mDigraph.getEncodingPretty().equals(answer)) {
      adjustScore(1);
      mAnswerText.setTextColor(mColorGood);
    } else {
      adjustScore(0);
      mAnswerText.setTextColor(mColorBad);
    }
    mAnswerEdit.setEnabled(false);
    mHandler.removeCallbacks(m_handlerCallback);
    mHandler.postDelayed(m_handlerCallback, mAnswerTimeout);
  }

  private MorseDigraph nextDigraph(MorseDigraph previous) {
    MorseDigraph retval = null;
    while (retval == null) {
      MorseDigraph digraph = sm_digraphs[mRandom.nextInt(sm_digraphs.length)];
      if (digraph == previous)
        continue;
      int digraphLen = digraph.getEncoding().length();
      if (digraph.isLetter()) {
        if (mLettersOn[digraphLen - 1])
          retval = digraph;
      } else if ((mDigitsOn && digraph.isDigit()) || (mSymbolsOn && digraph.isSymbol())) {
        retval = digraph;
      }
    }
    return retval;
  }

  private void loadPreferences() {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    PrefUtil prefUtil = new PrefUtil(sp, getResources());
    loadPreferences(prefUtil);
  }

  private void loadPreferences(PrefUtil sp) {
    mAnswerTimeout = sp.ANSWER_TIMEOUT.getValue() * 100;
    mLettersOn[0] = sp.PRACTICE_DIGRAPH_LENGTH_1_ON.getValue();
    mLettersOn[1] = sp.PRACTICE_DIGRAPH_LENGTH_2_ON.getValue();
    mLettersOn[2] = sp.PRACTICE_DIGRAPH_LENGTH_3_ON.getValue();
    mLettersOn[3] = sp.PRACTICE_DIGRAPH_LENGTH_4_ON.getValue();
    mDigitsOn = sp.PRACTICE_DIGRAPH_DIGITS_ON.getValue();
    mSymbolsOn = sp.PRACTICE_DIGRAPH_SYMBOLS_ON.getValue();
  }

  private void resetScore() {
    adjustScore(-1);
  }

  private void adjustScore(int changeType) {
    switch (changeType) {
      case -1:
        mScoreNumerator = 0;
        mScoreDenominator = 0;
        break;
      case 1:
        mScoreNumerator++;
        // intentional fallthrough
      case 0:
        mScoreDenominator++;
        break;
    }
    mScoreText.setText("Score: " + mScoreNumerator + "/" + mScoreDenominator);
  }

  @Override
  protected int getOptionsMenuResId() {
    return R.menu.practice_options_menu;
  }


  private enum PracticeState {
    WAITING_ANSWER,
    SHOWING_ANSWER,;
  }

  public class PracticeFilter implements InputFilter {

    private boolean mAppendInvalid;  // whether to append or ignore invalid characters

    /**
     * Default constructor for PracticeFilter doesn't append invalid characters.
     */
    PracticeFilter() {
      this(false);
    }

    /**
     * Base constructor for PracticeFilter
     *
     * @param appendInvalid whether or not to append invalid characters.
     */
    PracticeFilter(boolean appendInvalid) {
      mAppendInvalid = appendInvalid;
    }


    /**
     * This method is called when the buffer is going to replace the
     * range <code>dstart &hellip; dend</code> of <code>dest</code>
     * with the new text from the range <code>start &hellip; end</code>
     * of <code>source</code>.  Returns the CharSequence that we want
     * placed there instead, including an empty string
     * if appropriate, or <code>null</code> to accept the original
     * replacement.  Be careful to not to reject 0-length replacements,
     * as this is what happens when you delete text.
     */
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
      onStart();

      // Scan through beginning characters in dest, calling onInvalidCharacter()
      // for each invalid character.
      for (int i = 0; i < dstart; i++) {
        char c = dest.charAt(i);
        if (!isAllowed(c))
          onInvalidCharacter(c);
      }

      // Scan through changed characters rejecting disallowed chars
      SpannableStringBuilder modification = null;
      int modoff = 0;

      for (int i = start; i < end; i++) {
        char c = source.charAt(i);
        if (isAllowed(c)) {
          // Character allowed.
          modoff++;
        } else {
          if (mAppendInvalid) {
            modoff++;
          } else {
            if (modification == null) {
              modification = new SpannableStringBuilder(source, start, end);
              modoff = i - start;
            }

            modification.delete(modoff, modoff + 1);
          }

          onInvalidCharacter(c);
        }
      }

      // Scan through remaining characters in dest, calling onInvalidCharacter()
      // for each invalid character.
      for (int i = dend; i < dest.length(); i++) {
        char c = dest.charAt(i);
        if (!isAllowed(c))
          onInvalidCharacter(c);
      }

      onStop();

      // Either returns null if we made no changes,
      // or what we wanted to change it to if there were changes.
      return modification;
    }

    /**
     * Called when we start processing filter.
     */
    public void onStart() {

    }

    /**
     * Called whenever we encounter an invalid character.
     *
     * @param c the invalid character
     */
    public void onInvalidCharacter(char c) {

    }

    /**
     * Called when we're done processing filter
     */
    public void onStop() {

    }

    /**
     * Returns whether or not we allow character c.
     * Subclasses must override this method.
     * @param c char to test
     * @return whether the char is allowable
     */
    public boolean isAllowed(char c) {
      if (!mAnswerEdit.isEnabled())
        return false;
      switch (c) {
        case Constants.PRETTY_DIT:
        case Constants.PRETTY_DAH:
          return true;
        default:
          return false;
      }
    }
  }
}
