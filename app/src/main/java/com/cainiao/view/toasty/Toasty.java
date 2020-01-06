package com.cainiao.view.toasty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cainiao.R;


/**
 * This file is part of Toasty.
 *
 * Toasty is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Toasty is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Toasty.  If not, see <http://www.gnu.org/licenses/>.
 */

@SuppressLint("InflateParams")
public class Toasty {


  private static Toast mToast;
  public static boolean isCenter = false;

  @ColorInt
  private static int DEFAULT_TEXT_COLOR = Color.parseColor(ToasyDefaultConfig.COLOR_DEFAULT_TEXT);
  @ColorInt
  private static int ERROR_COLOR = Color.parseColor(ToasyDefaultConfig.COLOR_ERROR);
  @ColorInt
  private static int INFO_COLOR = Color.parseColor(ToasyDefaultConfig.COLOR_INFO);
  @ColorInt
  private static int SUCCESS_COLOR = Color.parseColor(ToasyDefaultConfig.COLOR_SUCCESS);
  @ColorInt
  private static int WARNING_COLOR = Color.parseColor(ToasyDefaultConfig.COLOR_WARING);
  @ColorInt
  private static int NORMAL_COLOR = Color.parseColor(ToasyDefaultConfig.COLOR_NORMAL);


  private static final Typeface LOADED_TOAST_TYPEFACE = Typeface.create("sans-serif-condensed", Typeface.NORMAL);
  private static Typeface currentTypeface = LOADED_TOAST_TYPEFACE;
  private static int textSize = 16; // in SP

  private static boolean tintIcon = true;

  private Toasty() {
    // avoiding instantiation
  }

  @CheckResult
  public static Toast normal(@NonNull Context context, @NonNull CharSequence message) {
    return normal(context, message, Toast.LENGTH_SHORT, null, false);
  }

  @CheckResult
  public static Toast normal(@NonNull Context context, @NonNull CharSequence message, Drawable icon) {
    return normal(context, message, Toast.LENGTH_SHORT, icon, true);
  }

  @CheckResult
  public static Toast normal(@NonNull Context context, @NonNull CharSequence message, int duration) {
    return normal(context, message, duration, null, false);
  }

  @CheckResult
  public static Toast normal(@NonNull Context context, @NonNull CharSequence message, int duration,
                             Drawable icon) {
    return normal(context, message, duration, icon, true);
  }

  @CheckResult
  public static Toast normal(@NonNull Context context, @NonNull CharSequence message, int duration,
                             Drawable icon, boolean withIcon) {
    return custom(context, message, icon, NORMAL_COLOR, duration, withIcon, true);
  }

  @CheckResult
  public static Toast warning(@NonNull Context context, @NonNull CharSequence message) {
    return warning(context, message, Toast.LENGTH_SHORT, true);
  }

  @CheckResult
  public static Toast warning(@NonNull Context context, @NonNull CharSequence message, int duration) {
    return warning(context, message, duration, true);
  }

  @CheckResult
  public static Toast warning(@NonNull Context context, @NonNull CharSequence message, int duration, boolean withIcon) {
    return custom(context, message, ToastyUtils.getDrawable(context, R.drawable.ic_error_outline_white_48dp),
            WARNING_COLOR, duration, withIcon, true);
  }

  @CheckResult
  public static Toast info(@NonNull Context context, @NonNull CharSequence message) {
    return info(context, message, Toast.LENGTH_SHORT, true);
  }

  @CheckResult
  public static Toast info(@NonNull Context context, @NonNull CharSequence message, int duration) {
    return info(context, message, duration, true);
  }

  @CheckResult
  public static Toast info(@NonNull Context context, @NonNull CharSequence message, int duration, boolean withIcon) {
    return custom(context, message, ToastyUtils.getDrawable(context, R.drawable.ic_info_outline_white_48dp),
            INFO_COLOR, duration, withIcon, true);
  }

  @CheckResult
  public static Toast success(@NonNull Context context, @NonNull CharSequence message) {
    return success(context, message, Toast.LENGTH_SHORT, true);
  }

  @CheckResult
  public static Toast success(@NonNull Context context, @NonNull CharSequence message, int duration) {
    return success(context, message, duration, true);
  }

  @CheckResult
  public static Toast success(@NonNull Context context, @NonNull CharSequence message, int duration, boolean withIcon) {
    return custom(context, message, ToastyUtils.getDrawable(context, R.drawable.ic_check_white_48dp),
            SUCCESS_COLOR, duration, withIcon, true);
  }

  @CheckResult
  public static Toast error(@NonNull Context context, @NonNull CharSequence message) {
    return error(context, message, Toast.LENGTH_SHORT, true);
  }

  @CheckResult
  public static Toast error(@NonNull Context context, @NonNull CharSequence message, int duration) {
    return error(context, message, duration, true);
  }

  @CheckResult
  public static Toast error(@NonNull Context context, @NonNull CharSequence message, int duration, boolean withIcon) {
    return custom(context, message, ToastyUtils.getDrawable(context, R.drawable.ic_clear_white_48dp),
            ERROR_COLOR, duration, withIcon, true);
  }

  @CheckResult
  public static Toast custom(@NonNull Context context, @NonNull CharSequence message, Drawable icon,
                             int duration, boolean withIcon) {
    return custom(context, message, icon, -1, duration, withIcon, false);
  }

  @CheckResult
  public static Toast custom(@NonNull Context context, @NonNull CharSequence message, @DrawableRes int iconRes,
                             @ColorInt int tintColor, int duration,
                             boolean withIcon, boolean shouldTint) {
    return custom(context, message, ToastyUtils.getDrawable(context, iconRes),
            tintColor, duration, withIcon, shouldTint);
  }

  @CheckResult
  public static Toast custom(@NonNull Context context, @NonNull CharSequence message, Drawable icon,
                             @ColorInt int tintColor, int duration,
                             boolean withIcon, boolean shouldTint) {

//        if (mToast == null){
    mToast = new Toast(context);
    ToastyUtils.hook(mToast);
//        }

    final View toastLayout = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
            .inflate(R.layout.layout_toast, null);
    final ImageView toastIcon = toastLayout.findViewById(R.id.toast_icon);
    final TextView toastTextView = toastLayout.findViewById(R.id.toast_text);
    Drawable drawableFrame;

    if (shouldTint)
      drawableFrame = ToastyUtils.tint9PatchDrawableFrame(context, tintColor);
    else
      drawableFrame = ToastyUtils.getDrawable(context, R.drawable.toast_frame);
    ToastyUtils.setBackground(toastLayout, drawableFrame);

    if (withIcon) {
      if (icon == null)
        throw new IllegalArgumentException("Avoid passing 'icon' as null if 'withIcon' is set to true");
      if (tintIcon)
        icon = ToastyUtils.tintIcon(icon, DEFAULT_TEXT_COLOR);
      ToastyUtils.setBackground(toastIcon, icon);
    } else {
      toastIcon.setVisibility(View.GONE);
    }

    toastTextView.setTextColor(DEFAULT_TEXT_COLOR);
    toastTextView.setText(message);

    toastTextView.setTypeface(currentTypeface);
    toastTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    if(isCenter){
      mToast.setGravity(Gravity.CENTER,0,0);
    }

    mToast.setView(toastLayout);
    mToast.setDuration(duration);

    return mToast;
  }

}