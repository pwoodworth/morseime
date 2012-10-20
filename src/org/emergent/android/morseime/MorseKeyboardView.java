
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

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author Patrick Woodworth
 */
public class MorseKeyboardView extends KeyboardView {

  static final int KEYCODE_OPTIONS = -100;
  static final int KEYCODE_SHIFT_LONGPRESS = -101;

  private Keyboard mPhoneKeyboard;

  public MorseKeyboardView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public MorseKeyboardView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void setPhoneKeyboard(Keyboard phoneKeyboard) {
    mPhoneKeyboard = phoneKeyboard;
  }

  private LastKey mLastKey = LastKey.NOKEY;

  @Override
  public boolean onTouchEvent(MotionEvent me) {
    String actionName = "UNKOWN";
    switch (me.getAction()) {
      case MotionEvent.ACTION_UP:
        actionName = "UP";
        break;
      case MotionEvent.ACTION_DOWN:
        actionName = "DOWN";
        break;
      case MotionEvent.ACTION_MOVE:
        actionName = "MOVE";
        break;
    }
    String keyName = "none";
    for (Key key : getKeyboard().getKeys()) {
      if (key.isInside((int)me.getX(), (int)me.getY())) {
        keyName = "" + key.codes[0];
        LastKey newKey = LastKey.NOKEY;
        if (key.codes[0] == -102) {
          newKey = LastKey.DITKEY;
        } else if (key.codes[0] == -103) {
          newKey = LastKey.DAHKEY;
        }
        if (newKey != mLastKey) {
          LOG.debug("MorseKeyboardView.onTouchEvent : theval = %s ; x = %s ; y = %s ; key = %s",
              actionName, me.getX(), me.getY(), keyName );
          mLastKey = newKey;
        }
      }
    }
    return super.onTouchEvent(me);
  }

  @Override
  protected boolean onLongPress(Key key) {
    if (key.codes[0] == Keyboard.KEYCODE_MODE_CHANGE) {
      getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
      return true;
    } else if (key.codes[0] == Keyboard.KEYCODE_SHIFT) {
      getOnKeyboardActionListener().onKey(KEYCODE_SHIFT_LONGPRESS, null);
      invalidateAllKeys();
      return true;
    } else if (key.codes[0] == '0' && getKeyboard() == mPhoneKeyboard) {
      // Long pressing on 0 in phone number keypad gives you a '+'.
      getOnKeyboardActionListener().onKey('+', null);
      return true;
    } else {
      return super.onLongPress(key);
    }
  }

  enum LastKey {
    NOKEY,
    DITKEY,
    DAHKEY,
    ;
  }
}
