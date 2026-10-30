package com.gathin.tvhome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Page 07 — SETTINGS (Full Settings Hub)
 * 2-column grid of 8 cards matching Figma design:
 * Network, Picture & sound, Remotes, Apps & storage, Home & widgets, Accessibility, Profiles, System.
 */
public class SettingsHubPageView extends LinearLayout {

    public SettingsHubPageView(Context context) {
        super(context);
        init();
    }

    public SettingsHubPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setBackgroundColor(NothingUiHelper.COLOR_BG_DARK);
        float density = getResources().getDisplayMetrics().density;

        // 1. Header
        addView(NothingUiHelper.createHeader(getContext()));

        // 2. Title Block
        addView(NothingUiHelper.createTitleBlock(getContext(), "SETTINGS", "Make the TV feel like yours."));

        // 3. 2-Column Grid (4 rows)
        GridLayout grid = new GridLayout(getContext());
        grid.setColumnCount(2);
        grid.setRowCount(4);
        grid.setPadding((int) (40 * density), (int) (6 * density), (int) (40 * density), (int) (12 * density));
        grid.setClipChildren(false);
        grid.setClipToPadding(false);

        LinearLayout.LayoutParams gridLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        grid.setLayoutParams(gridLp);

        // Row 1
        grid.addView(createSettingCard(R.drawable.ic_wifi, "Network & internet", "Wi-Fi / Ethernet",
                v -> launchSystemSettings(Settings.ACTION_WIFI_SETTINGS)));
        grid.addView(createSettingCard(R.drawable.ic_picture, "Picture & sound", "Display / audio outputs",
                v -> launchSystemSettings(Settings.ACTION_DISPLAY_SETTINGS)));

        // Row 2
        grid.addView(createSettingCard(R.drawable.ic_settings, "Remotes & accessories", "Bluetooth / device pairing",
                v -> launchSystemSettings(Settings.ACTION_BLUETOOTH_SETTINGS)));
        grid.addView(createSettingCard(R.drawable.ic_apps, "Apps & storage", "Installed apps / space",
                v -> launchSystemSettings(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)));

        // Row 3
        grid.addView(createSettingCard(R.drawable.ic_tune, "Home & widgets", "Layout / appearance",
                v -> {
                    Intent intent = new Intent(getContext(), LauncherSettingsActivity.class);
                    getContext().startActivity(intent);
                }));
        grid.addView(createSettingCard(R.drawable.ic_settings, "Accessibility", "Captions / reading",
                v -> launchSystemSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS)));

        // Row 4
        grid.addView(createSettingCard(R.drawable.ic_settings, "Profiles & restrictions", "Family / parental controls",
                v -> launchSystemSettings(Settings.ACTION_SYNC_SETTINGS)));
        grid.addView(createSettingCard(R.drawable.ic_settings, "System", "Time / updates / about",
                v -> launchSystemSettings(Settings.ACTION_DEVICE_INFO_SETTINGS)));

        addView(grid);

        // 4. Footer
        addView(NothingUiHelper.createFooter(getContext(), "07 / SETTINGS"));
    }

    private FrameLayout createSettingCard(int iconRes, String title, String subtitle, OnClickListener onClick) {
        float density = getResources().getDisplayMetrics().density;
        FrameLayout card = new FrameLayout(getContext());
        card.setFocusable(true);
        card.setFocusableInTouchMode(true);
        card.setClickable(true);

        GridLayout.LayoutParams glp = new GridLayout.LayoutParams();
        glp.width = (int) (420 * density);
        glp.height = (int) (62 * density);
        glp.setMargins((int) (6 * density), (int) (4 * density), (int) (6 * density), (int) (4 * density));
        card.setLayoutParams(glp);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(16 * density);
        bg.setColor(NothingUiHelper.COLOR_CARD_DARK);
        bg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
        card.setBackground(bg);

        LinearLayout inner = new LinearLayout(getContext());
        inner.setOrientation(HORIZONTAL);
        inner.setGravity(Gravity.CENTER_VERTICAL);
        inner.setPadding((int) (16 * density), 0, (int) (16 * density), 0);
        card.addView(inner, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ImageView iv = new ImageView(getContext());
        iv.setImageResource(iconRes);
        iv.setColorFilter(Color.WHITE);
        int iconSize = (int) (24 * density);
        inner.addView(iv, new LinearLayout.LayoutParams(iconSize, iconSize));

        LinearLayout textCol = new LinearLayout(getContext());
        textCol.setOrientation(VERTICAL);
        LinearLayout.LayoutParams tcLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tcLp.setMarginStart((int) (14 * density));
        textCol.setLayoutParams(tcLp);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        textCol.addView(tvTitle);

        TextView tvSub = new TextView(getContext());
        tvSub.setText(subtitle);
        tvSub.setTextColor(NothingUiHelper.COLOR_TEXT_GRAY);
        tvSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        textCol.addView(tvSub);

        inner.addView(textCol);

        card.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                bg.setColor(Color.WHITE);
                bg.setStroke((int) (2 * density), NothingUiHelper.COLOR_ACCENT_RED);
                iv.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN));
                tvTitle.setTextColor(Color.BLACK);
                tvSub.setTextColor(Color.DKGRAY);
            } else {
                bg.setColor(NothingUiHelper.COLOR_CARD_DARK);
                bg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
                iv.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
                tvTitle.setTextColor(Color.WHITE);
                tvSub.setTextColor(NothingUiHelper.COLOR_TEXT_GRAY);
            }
        });

        if (onClick != null) card.setOnClickListener(onClick);
        return card;
    }

    private void launchSystemSettings(String action) {
        try {
            Intent intent = new Intent(action);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        } catch (Exception ignored) {
            try {
                Intent fallback = new Intent(Settings.ACTION_SETTINGS);
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(fallback);
            } catch (Exception ignored2) {}
        }
    }

    public void focusFirstElement() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof GridLayout) {
                GridLayout gl = (GridLayout) child;
                if (gl.getChildCount() > 0) {
                    gl.getChildAt(0).requestFocus();
                    return;
                }
            }
        }
    }
}
