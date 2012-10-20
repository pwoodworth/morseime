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
import android.app.Dialog;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/** @author Patrick Woodworth */
class MorseUtil {

  private static final int DIT_UNITS = 1;
  private static final int DAH_UNITS = 3;
  private static final int CHAR_SEP_UNITS = 3;
  private static final int WORD_SEP_UNITS = 7;
  private static final int WPM_MILLIS_EQUATION_NUMERATOR = 1200;
  private static final int BAD_REQ_CODE = -1;

  private MorseUtil() {
    // no instantiation
  }

  public static long calcDitMillis(int wpm) {
    return calcDitMillis(wpm, DIT_UNITS);
  }

  public static long calcDahMillis(int wpm) {
    return calcDitMillis(wpm, DAH_UNITS);
  }

  public static long calcCharSepMillis(int wpm) {
    return calcDitMillis(wpm, MorseUtil.CHAR_SEP_UNITS);
  }

  public static long calcWordSepMillis(int wpm) {
    return calcDitMillis(wpm, MorseUtil.WORD_SEP_UNITS);
  }

  public static void openUri(Context context, String uri) {
    Intent i = new Intent(Intent.ACTION_VIEW);
    i.setData(Uri.parse(uri));
    context.startActivity(i);
  }

  public static boolean switchTo(int dest, View view) {
    return switchTo(dest, null, view.getContext(), BAD_REQ_CODE);
  }

  public static boolean switchTo(int dest, Activity activity) {
    return switchTo(dest, activity, BAD_REQ_CODE);
  }

  public static boolean switchTo(int dest, Activity activity, int reqCode) {
    return switchTo(dest, activity, activity, reqCode);
  }

  public static boolean switchTo(int dest, Activity activity, Context context) {
    return switchTo(dest, activity, context, BAD_REQ_CODE);
  }

  public static boolean switchTo(int itemId, Activity activity, Context context, int reqCode) {
    Class clazz = null;
    Intent intent = null;
    switch (itemId) {
      case R.string.language_settings_intent_cmpname:
        try {
          intent = new Intent();
          intent.setAction(Intent.ACTION_MAIN);
          ComponentName com = new ComponentName("com.android.settings", "com.android.settings.LanguageSettings");
          intent.setComponent(com);
        } catch (Exception ignored) {
        }
        break;
      case R.id.morse_settings:
        clazz = MorseSettings.class;
        break;
      case R.id.practice_settings:
        clazz = PracticeSettings.class;
        break;
      case R.id.help_activity:
        clazz = HelpActivity.class;
        break;
      case R.id.practice_activity:
        clazz = PracticeActivity.class;
        break;
    }

    if (intent == null) {
      if (clazz == null)
        return false;
      intent = new Intent(context, clazz);
    }
    if (reqCode != BAD_REQ_CODE) {
      if (activity != null) {
        activity.startActivityForResult(intent, reqCode);
      } else {
        return false;
      }
    } else {
      context.startActivity(intent);
      if (activity != null) {
        activity.finish();
      }
    }
    return true;
  }

  public static void showRefCard(Context context, View view) {
    final Dialog dialog = new MorseReferenceDialog(context, true, new DialogInterface.OnCancelListener() {
      public void onCancel(DialogInterface dialogInterface) {
      }
    });
    Window window = dialog.getWindow();
    WindowManager.LayoutParams lp = window.getAttributes();
    if (view != null)
      lp.token = view.getWindowToken();
    lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
    window.setAttributes(lp);
    window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    dialog.show();
  }

  private static long calcDitMillis(int wpm, int units) {
    return (WPM_MILLIS_EQUATION_NUMERATOR * units) / wpm;
  }

  public static int getVersionCode(ContextWrapper contextWrapper) {
    int versionNo = 0;
    PackageInfo pInfo = null;
    try {
      pInfo = contextWrapper.getPackageManager().getPackageInfo(
          PrefUtil.class.getPackage().getName(), PackageManager.GET_META_DATA);
    } catch (PackageManager.NameNotFoundException ignored) {
      pInfo = null;
    }
    if (pInfo != null)
      versionNo = pInfo.versionCode;
    return versionNo;
  }

  public static Spannable linkify(TextView textView, String text, LinkCreator... linkCreators) {
    Spannable str = linkifyCreateSpannable(textView, text);
    return linkify(textView, str, text, linkCreators);
  }

  public static Spannable linkify(TextView textView, Spannable str, String text, LinkCreator... linkCreators) {
    for (LinkCreator linkCreator : linkCreators) {
      addLinkSpan(str, text, linkCreator.getLinkText(), linkCreator.getClickableSpan());
    }
    linkifyPost(textView);
    return str;
  }

  private static Spannable linkifyCreateSpannable(TextView textView, String text) {
    textView.setText(text, TextView.BufferType.SPANNABLE);
    return (Spannable)textView.getText();
  }

  private static void linkifyPost(TextView textView) {
    if (!textView.getLinksClickable())
      textView.setLinksClickable(true);
    MovementMethod m = textView.getMovementMethod();
    if ((m == null) || !(m instanceof LinkMovementMethod)) {
      if (textView.getLinksClickable()) {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
      }
    }
  }

  private static void addLinkSpan(Spannable spannable, String text, String substr, ClickableSpan clickable) {
    int spanStart = 0;
    int spanEnd = spannable.length() - 1;
    if (substr != null) {
      if (text.indexOf(substr) < 0)
        return;
      spanStart = text.indexOf(substr);
      spanEnd = spanStart + substr.length();
    }

    if (spanEnd - spanStart < 0) // todo should this be < 1 ?
        return;

    // Create our span sections, and assign a format to each.
    spannable.setSpan(clickable, spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
  }

  public static class LinkCreator {

    private final ClickableSpan mClickableSpan;
    private final String mLinkText;

    public LinkCreator(String linkText, String linkUrl) {
      this(linkText, new URLSpan(linkUrl));
    }

    public LinkCreator(String linkText, final int linkResId) {
      this(linkText, new CustomClickableSpan(new View.OnClickListener() {
        public void onClick(View v) {
          boolean success = false;
          try {
            success = switchTo(linkResId, v);
            LOG.debug("MorseUtil.switchTo(R.id.language_settings,v): %s", success);
          } catch (Exception e) {
            LOG.error(e, "MorseUtil.switchTo(R.id.language_settings,v)");
          }
        }
      }));
    }

    public LinkCreator(String linkText, CustomClickableSpan clickableSpan) {
      this(clickableSpan, linkText);
    }

    public LinkCreator(String linkText, URLSpan clickableSpan) {
      this(clickableSpan, linkText);
    }

    private LinkCreator(ClickableSpan clickableSpan, String linkText) {
      mClickableSpan = clickableSpan;
      mLinkText = linkText;
    }

    public ClickableSpan getClickableSpan() {
      return mClickableSpan;
    }

    public String getLinkText() {
      return mLinkText;
    }
  }

  public static class CustomClickableSpan extends ClickableSpan {

    private final View.OnClickListener mListener;

    public CustomClickableSpan(View.OnClickListener listener) {
      if (listener == null)
        throw new NullPointerException("listener was null");
      mListener = listener;
    }

    @Override
    public void onClick(View widget) {
      mListener.onClick(widget);
    }
  }
}
