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

/**
 * @author Patrick Woodworth
 */
class Constants {

  public static final char XML_NBSP = '\u00A0';
  public static final char PRETTY_DIT = '\u2022';
  public static final char PRETTY_DAH = '\u2014';
  public static final char PRETTY_CHARSEP = '\u25A0';
  public static final char PRETTY_WORDSEP = '\u25A1';

  public static final int KEYCODE_DIT = -102;
  public static final int KEYCODE_DAH = -103;
  public static final int KEYCODE_CHARSEP = -104;
  public static final int KEYCODE_WORDSEP = -105;
  public static final int KEYCODE_DIRECT_DIT = PRETTY_DIT;
  public static final int KEYCODE_DIRECT_DAH = PRETTY_DAH;
  public static final int KEYCODE_DIRECT_CHARSEP = PRETTY_CHARSEP;
  public static final int KEYCODE_DIRECT_WORDSEP = PRETTY_WORDSEP;
  public static final int KEYCODE_ENTER = 10;
  public static final int LOOP_MILLIS = 50;

  private Constants() {
    // no instantiation
  }
}
