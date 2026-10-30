package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Common Nothing OS TV UI builder utilities.
 * Ensures consistent design language, typography, headers, footers, and focus states across all pages.
 */
public final class NothingUiHelper {

    public static final int COLOR_BG_DARK = Color.parseColor("#121212");
    public static final int COLOR_CARD_DARK = Color.parseColor("#1E1E1E");
    public static final int COLOR_CARD_STROKE = Color.parseColor("#2A2A2A");
    public static final int COLOR_ACCENT_RED = Color.parseColor("#FF3B30");
    public static final int COLOR_TEXT_WHITE = Color.WHITE;
    public static final int COLOR_TEXT_GRAY = Color.parseColor("#888888");
    public static final int COLOR_TEXT_DIM = Color.parseColor("#555555");
    public static final int COLOR_TEXT_BLACK = Color.BLACK;

    private NothingUiHelper() {}

    /**
     * Builds the standard sub-page header:
     * Left: NOTHING (TV) dot-matrix branding
     * Right: HH:MM time (NDOT) + Wi-Fi icon
     */
    public static LinearLayout createHeader(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding((int) (40 * density), (int) (18 * density), (int) (40 * density), (int) (10 * density));

        // Brand logo
        NdotTextView brand = new NdotTextView(context);
        brand.setDotMetrics(1.1f, 2.8f, 2.2f);
        brand.setText("Ravandy (TV)");
        LinearLayout.LayoutParams brandLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        header.addView(brand, brandLp);

        // Right side: Time + Wi-Fi
        LinearLayout rightBox = new LinearLayout(context);
        rightBox.setOrientation(LinearLayout.HORIZONTAL);
        rightBox.setGravity(Gravity.CENTER_VERTICAL);

        NdotTextView timeView = new NdotTextView(context);
        timeView.setDotMetrics(1.1f, 2.8f, 2.2f);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timeView.setText(sdf.format(new Date()));
        rightBox.addView(timeView);

        ImageView wifiIcon = new ImageView(context);
        wifiIcon.setImageResource(R.drawable.ic_wifi);
        wifiIcon.setColorFilter(COLOR_TEXT_WHITE);
        LinearLayout.LayoutParams wifiLp = new LinearLayout.LayoutParams(
                (int) (18 * density), (int) (18 * density));
        wifiLp.setMarginStart((int) (12 * density));
        rightBox.addView(wifiIcon, wifiLp);

        header.addView(rightBox);
        return header;
    }

    /**
     * Builds the large NDOT page title and subtitle block.
     */
    public static LinearLayout createTitleBlock(Context context, String title, String subtitle) {
        float density = context.getResources().getDisplayMetrics().density;
        LinearLayout block = new LinearLayout(context);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding((int) (40 * density), 0, (int) (40 * density), (int) (14 * density));

        NdotTextView titleView = new NdotTextView(context);
        titleView.setDotMetrics(1.3f, 3.2f, 2.4f);
        titleView.setText(title);
        block.addView(titleView);

        if (subtitle != null && !subtitle.isEmpty()) {
            TextView subView = new TextView(context);
            subView.setText(subtitle);
            subView.setTextColor(COLOR_TEXT_GRAY);
            subView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            subView.setPadding(0, (int) (4 * density), 0, 0);
            block.addView(subView);
        }

        return block;
    }

    /**
     * Builds the standard sub-page footer:
     * Left: D-PAD Navigate   OK Select   BACK Return
     * Right: Page code (e.g. "02 / APPS")
     */
    public static LinearLayout createFooter(Context context, String pageCode) {
        float density = context.getResources().getDisplayMetrics().density;
        LinearLayout footer = new LinearLayout(context);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setPadding((int) (40 * density), (int) (12 * density), (int) (40 * density), (int) (14 * density));

        TextView hints = new TextView(context);
        hints.setText("D-PAD Navigate   OK Select   BACK Return");
        hints.setTextColor(COLOR_TEXT_DIM);
        hints.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        footer.addView(hints, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView code = new TextView(context);
        code.setText(pageCode);
        code.setTextColor(COLOR_TEXT_DIM);
        code.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        footer.addView(code);

        return footer;
    }

    /**
     * Creates a rounded card frame with default Nothing OS styling.
     */
    public static FrameLayout createCard(Context context, int cornerRadiusDp, int bgNormal, int strokeNormal) {
        float density = context.getResources().getDisplayMetrics().density;
        FrameLayout card = new FrameLayout(context);
        card.setFocusable(true);
        card.setFocusableInTouchMode(true);

        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(cornerRadiusDp * density);
        gd.setColor(bgNormal);
        gd.setStroke((int) (1 * density), strokeNormal);
        card.setBackground(gd);
        return card;
    }

    /**
     * Attaches focus listener to invert card colors (white background, red stroke, black children).
     */
    public static void setupCardFocus(FrameLayout card, int cornerRadiusDp, int bgNormal, int strokeNormal) {
        float density = card.getResources().getDisplayMetrics().density;
        card.setOnFocusChangeListener((v, hasFocus) -> {
            GradientDrawable gd = (GradientDrawable) card.getBackground();
            if (gd != null) {
                gd.setColor(hasFocus ? Color.WHITE : bgNormal);
                gd.setStroke(
                        hasFocus ? (int) (2 * density) : (int) (1 * density),
                        hasFocus ? COLOR_ACCENT_RED : strokeNormal
                );
            }
            invertChildren(card, hasFocus);
        });
    }

    private static void invertChildren(ViewGroup parent, boolean focused) {
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(focused ? COLOR_TEXT_BLACK : COLOR_TEXT_WHITE);
            } else if (child instanceof ImageView) {
                ImageView iv = (ImageView) child;
                iv.setColorFilter(focused ? new PorterDuffColorFilter(COLOR_TEXT_BLACK, PorterDuff.Mode.SRC_IN) : null);
            } else if (child instanceof ViewGroup) {
                invertChildren((ViewGroup) child, focused);
            }
        }
    }
}
