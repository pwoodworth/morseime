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
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * @author Patrick Woodworth
 */
public class FrontDoor extends Activity {

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    final Intent thisIntent = getIntent();
    final String action = thisIntent.getAction();
    if (action == null || action.equals(Intent.ACTION_MAIN)) {
      PrefUtil prefUtil = new PrefUtil(PreferenceManager.getDefaultSharedPreferences(this), getResources());
      Intent i = null;
      int newVersion = MorseUtil.getVersionCode(this);
      int oldVersion = prefUtil.handleUpgrade(newVersion);
      if (oldVersion != newVersion) {
        LOG.debug("Upgraded from: %s to %s", oldVersion, newVersion);
        i = new Intent(getApplicationContext(), HelpActivity.class);
      } else {
        i = new Intent(getApplicationContext(), PracticeActivity.class);
      }
      // When launched from debugger, action is null.
      i.setAction(action);
      startActivity(i);
    }
    finish();
  }
}