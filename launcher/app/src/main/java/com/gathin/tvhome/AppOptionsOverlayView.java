package com.gathin.tvhome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.function.Consumer;

/**
 * Page 03 — APP OPTIONS (Long-press overlay)
 * Left: App preview card (icon, name, installed status)
 * Right: 5 actions (Open, Pinned to Home, Move, App info, Hide)
 */
public class AppOptionsOverlayView extends LinearLayout {

    private AppAdapter.Entry currentEntry;
    private boolean isPinned = true;

    private ImageView previewIcon;
    private TextView previewTitle;
    private TextView previewSubtitle;

    private TextView btnOpenText;
    private TextView pinStatusText;
    private LinearLayout actionsList;

    private Consumer<AppAdapter.Entry> onLaunch;
    private Consumer<AppAdapter.Entry> onTogglePin;
    private Consumer<AppAdapter.Entry> onMove;
    private Consumer<AppAdapter.Entry> onHide;
    private Runnable onDismiss;

    private static final ColorMatrixColorFilter GRAYSCALE_FILTER;
    static {
        ColorMatrix gray = new ColorMatrix();
        gray.setSaturation(0f);
        GRAYSCALE_FILTER = new ColorMatrixColorFilter(gray);
    }

    public AppOptionsOverlayView(Context context) {
        super(context);
        init();
    }

    public AppOptionsOverlayView(Context context, AttributeSet attrs) {
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
        addView(NothingUiHelper.createTitleBlock(getContext(), "APP OPTIONS", "One place to launch, pin or manage an app."));

        // 3. Two-Column Content
        LinearLayout columns = new LinearLayout(getContext());
        columns.setOrientation(HORIZONTAL);
        columns.setGravity(Gravity.CENTER_VERTICAL);
        columns.setPadding((int) (40 * density), 0, (int) (40 * density), 0);
        LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        columns.setLayoutParams(colLp);

        // Left Preview Card
        FrameLayout previewCard = new FrameLayout(getContext());
        int prevWidth = (int) (210 * density);
        int prevHeight = (int) (250 * density);
        LinearLayout.LayoutParams prevLp = new LinearLayout.LayoutParams(prevWidth, prevHeight);
        prevLp.setMarginEnd((int) (32 * density));
        previewCard.setLayoutParams(prevLp);

        GradientDrawable prevBg = new GradientDrawable();
        prevBg.setCornerRadius(20 * density);
        prevBg.setColor(NothingUiHelper.COLOR_CARD_DARK);
        prevBg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
        previewCard.setBackground(prevBg);

        LinearLayout prevContent = new LinearLayout(getContext());
        prevContent.setOrientation(VERTICAL);
        prevContent.setGravity(Gravity.CENTER);
        previewCard.addView(prevContent, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        previewIcon = new ImageView(getContext());
        int iconSize = (int) (64 * density);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(iconSize, iconSize);
        previewIcon.setLayoutParams(iconLp);
        previewIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        previewIcon.setColorFilter(null);
        prevContent.addView(previewIcon);

        previewTitle = new TextView(getContext());
        previewTitle.setTextColor(Color.WHITE);
        previewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        previewTitle.setTypeface(Typeface.DEFAULT_BOLD);
        previewTitle.setPadding(0, (int) (14 * density), 0, (int) (4 * density));
        prevContent.addView(previewTitle);

        previewSubtitle = new TextView(getContext());
        previewSubtitle.setText("Streaming / installed");
        previewSubtitle.setTextColor(NothingUiHelper.COLOR_TEXT_GRAY);
        previewSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        prevContent.addView(previewSubtitle);

        columns.addView(previewCard);

        // Right Action Rows
        actionsList = new LinearLayout(getContext());
        actionsList.setOrientation(VERTICAL);
        actionsList.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams actionsLp = new LinearLayout.LayoutParams(
                (int) (460 * density), ViewGroup.LayoutParams.WRAP_CONTENT);
        actionsList.setLayoutParams(actionsLp);

        // Action 1: Open
        FrameLayout rowOpen = createActionRow("▶", "Open App", null);
        btnOpenText = (TextView) ((LinearLayout) rowOpen.getChildAt(0)).getChildAt(1);
        rowOpen.setOnClickListener(v -> {
            if (onLaunch != null && currentEntry != null) onLaunch.accept(currentEntry);
        });
        actionsList.addView(rowOpen);

        // Action 2: Pinned to Home
        FrameLayout rowPin = createActionRow("⊕", "Pinned to Home", "On");
        pinStatusText = (TextView) ((LinearLayout) rowPin.getChildAt(0)).getChildAt(2);
        rowPin.setOnClickListener(v -> {
            isPinned = !isPinned;
            pinStatusText.setText(isPinned ? "On" : "Off");
            if (onTogglePin != null && currentEntry != null) onTogglePin.accept(currentEntry);
        });
        actionsList.addView(rowPin);

        // Action 3: Move in app row
        FrameLayout rowMove = createActionRow("↔", "Move in app row", null);
        rowMove.setOnClickListener(v -> {
            if (onMove != null && currentEntry != null) onMove.accept(currentEntry);
        });
        actionsList.addView(rowMove);

        // Action 4: App information
        FrameLayout rowInfo = createActionRow("ⓘ", "App information", "Storage and details");
        rowInfo.setOnClickListener(v -> {
            if (currentEntry != null && currentEntry.pkg != null) {
                try {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + currentEntry.pkg));
                    getContext().startActivity(intent);
                } catch (Exception ignored) {}
            }
        });
        actionsList.addView(rowInfo);

        // Action 5: Hide from Home
        FrameLayout rowHide = createActionRow("⊞", "Hide from Home", "Keep app installed");
        rowHide.setOnClickListener(v -> {
            if (onHide != null && currentEntry != null) onHide.accept(currentEntry);
        });
        actionsList.addView(rowHide);

        columns.addView(actionsList);
        addView(columns);

        // Bottom note
        TextView note = new TextView(getContext());
        note.setText("Opening an app takes you to its own interface.");
        note.setTextColor(NothingUiHelper.COLOR_TEXT_DIM);
        note.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        note.setPadding((int) (40 * density), (int) (6 * density), (int) (40 * density), 0);
        addView(note);

        // 4. Footer
        addView(NothingUiHelper.createFooter(getContext(), "03 / APP OPTIONS"));
    }

    private FrameLayout createActionRow(String iconText, String title, String rightHint) {
        float density = getResources().getDisplayMetrics().density;
        FrameLayout row = new FrameLayout(getContext());
        row.setFocusable(true);
        row.setFocusableInTouchMode(true);
        row.setClickable(true);

        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (44 * density));
        rowLp.setMargins(0, 0, 0, (int) (8 * density));
        row.setLayoutParams(rowLp);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(14 * density);
        bg.setColor(NothingUiHelper.COLOR_CARD_DARK);
        bg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
        row.setBackground(bg);

        LinearLayout inner = new LinearLayout(getContext());
        inner.setOrientation(HORIZONTAL);
        inner.setGravity(Gravity.CENTER_VERTICAL);
        inner.setPadding((int) (16 * density), 0, (int) (16 * density), 0);
        row.addView(inner, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Icon
        TextView iv = new TextView(getContext());
        iv.setText(iconText);
        iv.setTextColor(Color.WHITE);
        iv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        inner.addView(iv);

        // Title
        TextView tv = new TextView(getContext());
        tv.setText(title);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tvLp.setMarginStart((int) (14 * density));
        inner.addView(tv, tvLp);

        // Right hint
        TextView rh = new TextView(getContext());
        rh.setText(rightHint != null ? rightHint : "");
        rh.setTextColor(NothingUiHelper.COLOR_TEXT_GRAY);
        rh.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        inner.addView(rh);

        // Focus styling
        row.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                bg.setColor(Color.WHITE);
                bg.setStroke((int) (2 * density), NothingUiHelper.COLOR_ACCENT_RED);
                iv.setTextColor(Color.BLACK);
                tv.setTextColor(Color.BLACK);
                rh.setTextColor(Color.DKGRAY);
            } else {
                bg.setColor(NothingUiHelper.COLOR_CARD_DARK);
                bg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
                iv.setTextColor(Color.WHITE);
                tv.setTextColor(Color.WHITE);
                rh.setTextColor(NothingUiHelper.COLOR_TEXT_GRAY);
            }
        });

        return row;
    }

    public void setEntry(AppAdapter.Entry entry, boolean isPinned) {
        this.currentEntry = entry;
        this.isPinned = isPinned;

        if (entry != null) {
            previewTitle.setText(entry.label != null ? entry.label : "");
            btnOpenText.setText("Open " + (entry.label != null ? entry.label : "App"));
            pinStatusText.setText(isPinned ? "On" : "Off");

            Drawable icon = null;
            try {
                if (entry.pkg != null) {
                    icon = getContext().getPackageManager().getApplicationIcon(entry.pkg);
                }
            } catch (Exception ignored) {
                icon = entry.icon != null ? entry.icon : entry.banner;
            }
            if (icon == null) icon = getContext().getDrawable(R.drawable.ic_apps);
            previewIcon.setImageDrawable(icon);
        }
    }

    public void setCallbacks(Consumer<AppAdapter.Entry> onLaunch, Consumer<AppAdapter.Entry> onTogglePin,
                             Consumer<AppAdapter.Entry> onMove, Consumer<AppAdapter.Entry> onHide,
                             Runnable onDismiss) {
        this.onLaunch = onLaunch;
        this.onTogglePin = onTogglePin;
        this.onMove = onMove;
        this.onHide = onHide;
        this.onDismiss = onDismiss;
    }

    public void focusFirstElement() {
        if (actionsList != null && actionsList.getChildCount() > 0) {
            actionsList.getChildAt(0).requestFocus();
        }
    }
}
