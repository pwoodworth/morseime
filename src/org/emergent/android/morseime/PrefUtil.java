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

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;

/** @author Patrick Woodworth */
class PrefUtil {

  /** @since 1.1 */
  public final IntPrefWrapper PREF_VERSION = new IntPrefWrapper("pref_version", 0);

  /** @since 1.0 */
  public final IntPrefWrapper WORDS_PER_MINUTE = new IntPrefWrapper("words_per_minute", 10);

  /** @since 1.0 */
  public final BooleanPrefWrapper FORCE_VISIBLE = new BooleanPrefWrapper("force_visible", true);

  /** @since 1.0 */
  public final BooleanPrefWrapper AUTO_SPACE_ON = new BooleanPrefWrapper("auto_space_on", false);

  /** @since 1.0 */
  public final BooleanPrefWrapper VIBRATE_ON = new BooleanPrefWrapper("vibrate_on", false);

  /** @since 1.0 */
  public final BooleanPrefWrapper SOUND_ON = new BooleanPrefWrapper("sound_on", false);

  /** @since 1.0 */
  public final BooleanPrefWrapper AUTO_CAP = new BooleanPrefWrapper("auto_cap", true);

  /** @since 1.0 */
  public final BooleanPrefWrapper QUICK_FIXES = new BooleanPrefWrapper("quick_fixes", true);

  /** @since 1.0 */
  public final BooleanPrefWrapper SHOW_SUGGESTIONS = new BooleanPrefWrapper("show_suggestions", true);

  /** @since 1.0 */
  public final BooleanPrefWrapper AUTO_COMPLETE = new BooleanPrefWrapper("auto_complete", false, R.bool.enable_autocorrect);

  /** @since 1.1 */
  public final IntPrefWrapper BEEP_VOLUME = new IntPrefWrapper("beep_volume", 0);

  /** @since 1.1 */
  public final IntPrefWrapper ANSWER_TIMEOUT = new IntPrefWrapper("practice_answer_timeout", 40);

  /** @since 1.1 */
  public final BooleanPrefWrapper PRACTICE_DIGRAPH_LENGTH_1_ON = new BooleanPrefWrapper("practice_letters_1_on", true);

  /** @since 1.1 */
  public final BooleanPrefWrapper PRACTICE_DIGRAPH_LENGTH_2_ON = new BooleanPrefWrapper("practice_letters_2_on", true);

  /** @since 1.1 */
  public final BooleanPrefWrapper PRACTICE_DIGRAPH_LENGTH_3_ON = new BooleanPrefWrapper("practice_letters_3_on", true);

  /** @since 1.1 */
  public final BooleanPrefWrapper PRACTICE_DIGRAPH_LENGTH_4_ON = new BooleanPrefWrapper("practice_letters_4_on", true);

  /** @since 1.1 */
  public final BooleanPrefWrapper PRACTICE_DIGRAPH_DIGITS_ON = new BooleanPrefWrapper("practice_digits_on", true);

  /** @since 1.1 */
  public final BooleanPrefWrapper PRACTICE_DIGRAPH_SYMBOLS_ON = new BooleanPrefWrapper("practice_symbols_on", true);

  private final SharedPreferences mSharedPrefs;
  private final Resources mResources;
  private SharedPreferences.OnSharedPreferenceChangeListener mListener;

  public PrefUtil(SharedPreferences sp, Resources res) {
    mSharedPrefs = sp;
    mResources = res;
  }

  public synchronized void startChangeListening() {
    if (mListener == null) {
      mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        }
      };
      mSharedPrefs.registerOnSharedPreferenceChangeListener(mListener);
    }
  }

  public synchronized void stopChangeListening() {
    if (mListener != null) {
      mSharedPrefs.unregisterOnSharedPreferenceChangeListener(mListener);
      mListener = null;
    }
  }


  public int handleUpgrade(int newVersion) {
    int oldVersion = PREF_VERSION.getValue();
    if (oldVersion != newVersion) {
      // do stuff
      PREF_VERSION.setValue(newVersion);
    }
    return oldVersion;
  }

  public class PrefWrapper {

    public final String mKey;
    public final int mResId;

    public PrefWrapper(String key) {
      this(key, 0);
    }

    public PrefWrapper(String key, int resId) {
      mKey = key;
      mResId = resId;
    }

    public String getKey() {
      return mKey;
    }
  }

  public class BooleanPrefWrapper extends PrefWrapper {

    public final boolean mDefault;

    public BooleanPrefWrapper(String key, boolean def) {
      super(key);
      mDefault = def;
    }

    public BooleanPrefWrapper(String key, boolean def, int resId) {
      super(key, resId);
      mDefault = def;
    }

    public boolean getDefault() {
      if (mResources != null && mResId != 0)
        return mResources.getBoolean(mResId);
      return mDefault;
    }

    public boolean getValue() {
      return mSharedPrefs.getBoolean(getKey(), getDefault());
    }

    public boolean setValue(boolean val) {
      SharedPreferences.Editor edit = mSharedPrefs.edit();
      edit.putBoolean(getKey(), val);
      return edit.commit();
    }
  }

  public class IntPrefWrapper extends PrefWrapper {

    public final int mDefault;

    public IntPrefWrapper(String key, int def) {
      super(key);
      mDefault = def;
    }

    public IntPrefWrapper(String key, int def, int resId) {
      super(key, resId);
      mDefault = def;
    }

    public int getDefault() {
      if (mResources != null && mResId != 0)
        return mResources.getInteger(mResId);
      return mDefault;
    }

    public int getValue() {
      return mSharedPrefs.getInt(getKey(), getDefault());
    }

    public boolean setValue(int val) {
      SharedPreferences.Editor edit = mSharedPrefs.edit();
      edit.putInt(getKey(), val);
      return edit.commit();
    }
  }
}
