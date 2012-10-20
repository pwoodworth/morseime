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

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

public class HelpActivity extends BasicActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.help);
    Button next = (Button)findViewById(R.id.practicebutton);
    next.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        MorseUtil.switchTo(R.id.practice_activity, HelpActivity.this, view.getContext());
        //        Intent myIntent = new Intent(view.getContext(), PracticeActivity.class);
//        startActivityForResult(myIntent, 0);
      }
    });

    Resources resources = getResources();
    MorseUtil.LinkCreator[] linkCreators = new MorseUtil.LinkCreator[]{
        new MorseUtil.LinkCreator(
            resources.getString(R.string.help_language_and_keyboard_settings_link_text),
            R.string.language_settings_intent_cmpname),
        new MorseUtil.LinkCreator(
            resources.getString(R.string.help_input_method_link_text),
            new MorseUtil.CustomClickableSpan(new View.OnClickListener() {
              public void onClick(View v) {
                ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showInputMethodPicker();
              }
            }))
    };
    MorseUtil.linkify(
        (TextView)findViewById(R.id.help_enable_keyboard_textview),
        resources.getString(R.string.help_enable_keyboard_text),
        linkCreators);
    MorseUtil.linkify(
        (TextView)findViewById(R.id.help_select_input_method_textview),
        resources.getString(R.string.help_select_input_method_text),
        linkCreators);
  }

  @Override
  protected int getOptionsMenuResId() {
    return R.menu.help_options_menu;
  }
}
