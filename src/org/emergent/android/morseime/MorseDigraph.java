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

import java.util.EnumSet;

/**
 * @author Patrick Woodworth
 */
public enum MorseDigraph {

  LETTER_A("a", ".-"),
  LETTER_B("b", "-..."),
  LETTER_C("c", "-.-."),
  LETTER_D("d", "-.."),
  LETTER_E("e", "."),
  LETTER_F("f", "..-."),
  LETTER_G("g", "--."),
  LETTER_H("h", "...."),
  LETTER_I("i", ".."),
  LETTER_J("j", ".---"),
  LETTER_K("k", "-.-"),
  LETTER_L("l", ".-.."),
  LETTER_M("m", "--"),
  LETTER_N("n", "-."),
  LETTER_O("o", "---"),
  LETTER_P("p", ".--."),
  LETTER_Q("q", "--.-"),
  LETTER_R("r", ".-."),
  LETTER_S("s", "..."),
  LETTER_T("t", "-"),
  LETTER_U("u", "..-"),
  LETTER_V("v", "...-"),
  LETTER_W("w", ".--"),
  LETTER_X("x", "-..-"),
  LETTER_Y("y", "-.--"),
  LETTER_Z("z", "--.."),
  DIGIT_0("0", "-----"),
  DIGIT_1("1", ".----"),
  DIGIT_2("2", "..---"),
  DIGIT_3("3", "...--"),
  DIGIT_4("4", "....-"),
  DIGIT_5("5", "....."),
  DIGIT_6("6", "-...."),
  DIGIT_7("7", "--..."),
  DIGIT_8("8", "---.."),
  DIGIT_9("9", "----."),
  PERIOD(".", ".-.-.-"),
  COMMA(",", "--..--"),
  QUESTION_MARK("?", "..--.."),
  APOSTROPHE("\"", ".----."),
  EXCLAMATION_MARK("!", "-.-.--"),
  SLASH("/", "-..-."),
  PAREN_OPEN("(", "	-.--."),
  PAREN_CLOSE(")", "-.--.-"),
  AMPERSAND("&", ".-..."),
  COLON(":", "---..."),
  SEMICOLON(";", "-.-.-."),
  DOUBLE_DASH("-", "-...-"), // todo correct this
  PLUS("+", ".-.-."),
  MINUS("-", "-....-"),
  UNDERSCORE("_", "..--.-"),
  QUOTATION_MARK("\"", ".-..-."),
  DOLLAR_SIGN("$", "...-..-"),
  AT_SIGN("@", ".--.-."),
  ;

  private final String mDecoding;
  private final String mEncoding;

  MorseDigraph(String decoding, String encoding) {
    mDecoding = decoding;
    mEncoding = encoding;
  }

  public char getChar() {
    return mDecoding.charAt(0);
  }

  public String getDecoding() {
    return mDecoding;
  }

  public String getEncoding() {
    return mEncoding;
  }

  public String getEncodingPretty() {
    return mEncoding.replace('.', Constants.PRETTY_DIT).replace('-', Constants.PRETTY_DAH);
  }

  public boolean isLetter() {
    char c = getChar();
    return c >= 'a' && c <= 'z';
  }

  public boolean isDigit() {
    char c = getChar();
    return c >= '0' && c <= '9';
  }

  public boolean isAlphaNumeric() {
    return isLetter() || isDigit();
  }

  public boolean isSymbol() {
    return !isAlphaNumeric();
  }

  @Override
  public String toString() {
    return getDecoding();
  }

  public static MorseDigraph valueOfEncoding(String encoding) {
    for (MorseDigraph digraph : EnumSet.allOf(MorseDigraph.class)) {
      if (digraph.mEncoding.equals(encoding))
        return digraph;
    }
    throw new IllegalArgumentException("Invalid digraph encoding \"" + encoding + "\"");
  }
}
