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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author Patrick Woodworth
 */
public abstract class BasicActivity extends Activity {

  protected static final int PRACTICE_SETTINGS_REQ_CODE = 0;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    int optResId = getOptionsMenuResId();
    if (optResId != 0) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(optResId, menu);
    }
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (MorseUtil.switchTo(item.getItemId(), this))
      return true;

    switch (item.getItemId()) {
      case R.id.reference_card:
        showRefCard();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onOptionsMenuClosed(Menu menu) {
    super.onOptionsMenuClosed(menu);
  }

  protected int getOptionsMenuResId() {
    return 0;
  }

  private void showRefCard() {
    MorseUtil.showRefCard(this, null);
  }
}
