package com.gathin.tvhome;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Nothing OS App Pill Item View.
 * Unfocused: Compact dark rounded card (80dp) with desaturated monochrome (grayscale) icon.
 * Focused: Expands horizontally (190dp) with solid white background, black inverted icon,
 * black app title, red accent border, and top-right red indicator dot.
 */
public class AppPillItemView extends FrameLayout {

    private ImageView iconView;
    private TextView titleView;
    private View redDot;
    private LinearLayout contentRow;

    private int normalWidthPx;
    private int expandedWidthPx;
    private int heightPx;

    private ValueAnimator widthAnimator;
    private final GradientDrawable bgDrawable = new GradientDrawable();

    private Drawable originalIcon;
    private String label;
    private boolean isSpecialAllApps = false;

    // Filters for Nothing OS aesthetic
    private static final ColorMatrixColorFilter GRAYSCALE_FILTER;
    private static final ColorMatrixColorFilter INVERT_FILTER;

    static {
        // 1. Unfocused: Grayscale murni tanpa boost ekstrem biar ga jadi kotak putih silau
        ColorMatrix grayMatrix = new ColorMatrix();
        grayMatrix.setSaturation(0f);
        float[] dimming = new float[]{
                0.9f, 0f,   0f,   0f, 0f,
                0f,   0.9f, 0f,   0f, 0f,
                0f,   0f,   0.9f, 0f, 0f,
                0f,   0f,   0f,   1f, 0f
        };
        grayMatrix.postConcat(new ColorMatrix(dimming));
        GRAYSCALE_FILTER = new ColorMatrixColorFilter(grayMatrix);

        // 2. Focused: Grayscale + Darken (Otomatis bikin logo apapun jadi gelap di atas background putih)
        ColorMatrix darkenMatrix = new ColorMatrix();
        darkenMatrix.setSaturation(0f);
        float[] darken = new float[]{
                0.25f,  0f,    0f,    0f, 0f,
                 0f,   0.25f,  0f,    0f, 0f,
                 0f,    0f,   0.25f,  0f, 0f,
                 0f,    0f,    0f,    1.0f, 0f
        };
        darkenMatrix.postConcat(new ColorMatrix(darken));
        INVERT_FILTER = new ColorMatrixColorFilter(darkenMatrix);
    }

    public AppPillItemView(Context context) {
        super(context);
        init();
    }

    public AppPillItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AppPillItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
        setClipChildren(false);
        setClipToPadding(false);

        float density = getResources().getDisplayMetrics().density;
        normalWidthPx = (int) (80 * density);
        expandedWidthPx = (int) (190 * density);
        heightPx = (int) (80 * density);

        // Rounded background
        bgDrawable.setCornerRadius(20 * density);
        bgDrawable.setColor(Color.parseColor("#1E1E1E"));
        bgDrawable.setStroke((int) (1 * density), Color.parseColor("#2A2A2A"));
        setBackground(bgDrawable);

        contentRow = new LinearLayout(getContext());
        contentRow.setOrientation(LinearLayout.HORIZONTAL);
        contentRow.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams rowParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentRow.setLayoutParams(rowParams);
        contentRow.setPadding((int) (15 * density), 0, (int) (15 * density), 0);
        addView(contentRow);

        iconView = new ImageView(getContext());
        int iconSize = (int) (50 * density);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        iconView.setLayoutParams(iconParams);
        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int iconPad = (int) (9 * density);
        iconView.setPadding(iconPad, iconPad, iconPad, iconPad);
        contentRow.addView(iconView);

        titleView = new TextView(getContext());
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        titleParams.setMarginStart((int) (12 * density));
        titleView.setLayoutParams(titleParams);
        titleView.setSingleLine(true);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        titleView.setTextColor(Color.BLACK);
        titleView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        titleView.setVisibility(View.GONE);
        contentRow.addView(titleView);

        // Red indicator dot at top-right
        redDot = new View(getContext());
        int dotSize = (int) (6 * density);
        FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(dotSize, dotSize);
        dotParams.gravity = Gravity.TOP | Gravity.END;
        dotParams.topMargin = (int) (8 * density);
        dotParams.rightMargin = (int) (8 * density);
        redDot.setLayoutParams(dotParams);
        GradientDrawable redDotDrawable = new GradientDrawable();
        redDotDrawable.setShape(GradientDrawable.OVAL);
        redDotDrawable.setColor(Color.parseColor("#FF3B30"));
        redDot.setBackground(redDotDrawable);
        redDot.setVisibility(View.GONE);
        addView(redDot);

        setLayoutParams(new ViewGroup.MarginLayoutParams(normalWidthPx, heightPx));

        setOnFocusChangeListener((v, hasFocus) -> applyFocus(hasFocus, true));
    }

    public void setApp(String label, Drawable icon, boolean isAllApps) {
        this.label = label;
        this.originalIcon = icon;
        this.isSpecialAllApps = isAllApps;

        titleView.setText(label != null ? label : "");
        iconView.setImageDrawable(icon);

        applyFocus(hasFocus(), false);
    }

    private void applyFocus(boolean focused, boolean animate) {
        float density = getResources().getDisplayMetrics().density;
        int targetWidth = focused ? expandedWidthPx : normalWidthPx;

        if (widthAnimator != null && widthAnimator.isRunning()) {
            widthAnimator.cancel();
        }

        if (animate) {
            int startWidth = getWidth() > 0 ? getWidth() : (focused ? normalWidthPx : expandedWidthPx);
            widthAnimator = ValueAnimator.ofInt(startWidth, targetWidth);
            widthAnimator.setDuration(160L);
            widthAnimator.setInterpolator(new DecelerateInterpolator());
            widthAnimator.addUpdateListener(anim -> {
                int val = (Integer) anim.getAnimatedValue();
                ViewGroup.LayoutParams lp = getLayoutParams();
                if (lp != null) {
                    lp.width = val;
                    setLayoutParams(lp);
                }
            });
            widthAnimator.start();
        } else {
            ViewGroup.LayoutParams lp = getLayoutParams();
            if (lp != null) {
                lp.width = targetWidth;
                setLayoutParams(lp);
            }
        }

        if (focused) {
            // White card with red accent border
            bgDrawable.setColor(Color.WHITE);
            bgDrawable.setStroke((int) (2 * density), Color.parseColor("#FF3B30"));

            // Black title text
            titleView.setVisibility(View.VISIBLE);
            titleView.setTextColor(Color.BLACK);

            // Red dot indicator
            redDot.setVisibility(View.VISIBLE);

            // App icon in original full color, or black for special All Apps icon
            if (isSpecialAllApps) {
                iconView.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN));
            } else {
                iconView.setColorFilter(null);
            }
        } else {
            // Dark card with subtle border
            bgDrawable.setColor(Color.parseColor("#1E1E1E"));
            bgDrawable.setStroke((int) (1 * density), Color.parseColor("#2A2A2A"));

            titleView.setVisibility(View.GONE);
            redDot.setVisibility(View.GONE);

            // App icon in original full color, or white for special All Apps icon
            if (isSpecialAllApps) {
                iconView.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
            } else {
                iconView.setColorFilter(null);
            }
        }
    }
}
