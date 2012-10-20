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
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

/**
 * @author Patrick Woodworth
 */
public class MorseIME extends LatinIME {

  private DitDahHandlerCallback m_ditDahHandlerCallback = new DitDahHandlerCallback();

  private boolean mDirectMode = false;
  private boolean m_forceVisible = true;
  private boolean m_autoSpace = false;
  private int m_wpm = 10;

  private ToneGenerator mToneGenerator;

  @Override
  public boolean onEvaluateInputViewShown() {
    return m_forceVisible || super.onEvaluateInputViewShown();
  }

  @Override
  public void onStartInputView(EditorInfo attribute, boolean restarting) {
    super.onStartInputView(attribute, restarting);
    if ("org.emergent.android.morseime:DIRECT".equals(attribute.privateImeOptions)) {
      mDirectMode = true;
    } else {
      mDirectMode = false;
    }
  }

  @Override
  public void onFinishInput() {
    super.onFinishInput();
    m_ditDahHandlerCallback.reset();
  }

  @Override
  public void onKey(int primaryCode, int[] keyCodes) {
    if (primaryCode == Constants.KEYCODE_DIT || primaryCode == Constants.KEYCODE_DAH) {
      m_ditDahHandlerCallback.recordDitOrDah(primaryCode == Constants.KEYCODE_DIT);
      if (mDirectMode) {
        primaryCode = primaryCode == Constants.KEYCODE_DIT ? Constants.KEYCODE_DIRECT_DIT : Constants.KEYCODE_DIRECT_DAH;
      } else {
        return;
      }
    } else {
      m_ditDahHandlerCallback.reset();
    }
    super.onKey(primaryCode, keyCodes);
  }

  protected void playKeyClickPostMuteCheck(int primaryCode) {
    long toneDuration = 0;
    switch (primaryCode) {
      case Constants.KEYCODE_DIT:
        toneDuration = MorseUtil.calcDitMillis(m_wpm);
        break;
      case Constants.KEYCODE_DAH:
        toneDuration = MorseUtil.calcDahMillis(m_wpm);
        break;
      default:
        super.playKeyClickPostMuteCheck(primaryCode);
        return;
    }
    if (toneDuration > 0 && mToneGenerator != null)
        mToneGenerator.startTone(ToneGenerator.TONE_DTMF_5, (int)toneDuration);
  }

  @Override
  protected void loadSettings(PrefUtil sp) {
    super.loadSettings(sp);
    m_forceVisible = sp.FORCE_VISIBLE.getValue();
    m_autoSpace = sp.AUTO_SPACE_ON.getValue();
    m_wpm = sp.WORDS_PER_MINUTE.getValue();
    try {
      int beepVolume = sp.BEEP_VOLUME.getValue();
      mToneGenerator = beepVolume <= 0
          ? null
          : new ToneGenerator(AudioManager.STREAM_SYSTEM,beepVolume);
    } catch (Throwable e) {
      LOG.error(e, "failed to instantiate tone generator");
    }
  }

  protected void showOptionsMenu() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(true);
    builder.setIcon(R.drawable.ic_dialog_keyboard);
    builder.setNegativeButton(android.R.string.cancel, null);
    builder.setItems(new CharSequence[]{
        getString(R.string.morse_code_reference),
        getString(R.string.morse_ime_settings),
        getString(R.string.inputMethod),
    },
        new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface di, int position) {
            di.dismiss();
            switch (position) {
              case 0:
                showRefCard();
                break;
              case 1:
                launchSettings();
                break;
              case 2:
                ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();
                break;
            }
          }
        });
    builder.setTitle(getResources().getString(R.string.morse_ime_name));
    mOptionsDialog = builder.create();
    Window window = mOptionsDialog.getWindow();
    WindowManager.LayoutParams lp = window.getAttributes();
    lp.token = getInputView().getWindowToken();
    lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
    window.setAttributes(lp);
    window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    mOptionsDialog.show();
  }

  private void showRefCard() {
    MorseUtil.showRefCard(this, getInputView());
  }

  /**
   * Helper to send a key down / key up pair to the current editor.
   * @param keyEventCode the key code
   */
  private void keyDownUp(int keyEventCode) {
    getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
    getCurrentInputConnection().sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
  }

  /**
   * Helper to send a character to the editor as raw key events.
   * @param keyCode the key code
   */
  private void sendKey(int keyCode) {
    switch (keyCode) {
      case '\n':
        keyDownUp(KeyEvent.KEYCODE_ENTER);
        break;
      default:
        if (keyCode >= '0' && keyCode <= '9') {
          keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
        } else {
          getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
        }
        break;
    }
  }

  public class DitDahHandlerCallback implements Runnable {

    private final StringBuilder m_ditDahBuf = new StringBuilder();

    private long m_lastDitOrDah = 0;

    private boolean m_registered = false;

    public DitDahHandlerCallback() {
    }

    public void run() {
      long curTime = System.currentTimeMillis();
      try {
        synchronized (m_ditDahBuf) {
          long elapsed = curTime - m_lastDitOrDah;
          if (elapsed < MorseUtil.calcCharSepMillis(m_wpm))
            return;

          if (m_ditDahBuf.length() == 0) {
            if (m_lastDitOrDah == 0)
              return;
            if (!m_autoSpace || elapsed < MorseUtil.calcWordSepMillis(m_wpm))
              return;
            m_lastDitOrDah = 0;
            sendKey(mDirectMode ? '\n' : ' ');
          } else {
            String encoding = m_ditDahBuf.toString();
            m_ditDahBuf.setLength(0);
            if (mDirectMode) {
              sendKey('\n');
            } else {
              try {
                MorseDigraph digraph = MorseDigraph.valueOfEncoding(encoding);
                char theChar = digraph.getChar();
                if (mKeyboardSwitcher.mInputView.getKeyboard().isShifted())
                  theChar = Character.toUpperCase(theChar);
                sendKey(theChar);
              } catch (IllegalArgumentException e) {
                LOG.warn(e, e.getMessage());
              }
            }
          }
        }
      } finally {
        mHandler.postDelayed(this, Constants.LOOP_MILLIS);
      }
    }

    public void reset() {
      synchronized (m_ditDahBuf) {
        mHandler.removeCallbacks(this);
        m_lastDitOrDah = 0;
        m_ditDahBuf.setLength(0);
        m_registered = false;
      }
    }

    public void recordDitOrDah(boolean isDit) {
      synchronized (m_ditDahBuf) {
        m_lastDitOrDah = System.currentTimeMillis();
        m_ditDahBuf.append(isDit ? '.' : '-');
        if (!m_registered) {
          mHandler.removeCallbacks(this);
          mHandler.postDelayed(this, Constants.LOOP_MILLIS);
          m_registered = true;
        }
      }
    }
  }
}
