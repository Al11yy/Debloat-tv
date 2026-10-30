package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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

import java.util.function.Consumer;

/**
 * Page 05 — INPUTS
 * 3x2 grid of source cards: Live TV, HDMI 1-3, USB drive, Cast
 * with status indicators (CONNECTED, READY, UNAVAILABLE).
 */
public class InputsPageView extends LinearLayout {

    private Consumer<Integer> onInputSelect;

    public InputsPageView(Context context) {
        super(context);
        init();
    }

    public InputsPageView(Context context, AttributeSet attrs) {
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
        addView(NothingUiHelper.createTitleBlock(getContext(), "INPUTS", "Switch sources without leaving the couch."));

        // 3. 3x2 Grid
        GridLayout grid = new GridLayout(getContext());
        grid.setColumnCount(3);
        grid.setRowCount(2);
        grid.setPadding((int) (40 * density), (int) (8 * density), (int) (40 * density), (int) (12 * density));
        grid.setClipChildren(false);
        grid.setClipToPadding(false);

        LinearLayout.LayoutParams gridLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        grid.setLayoutParams(gridLp);

        // Sources:
        // 0: Live TV
        // 1: HDMI 1
        // 2: HDMI 2
        // 3: HDMI 3
        // 4: USB Drive
        // 5: Cast
        grid.addView(createSourceCard(0, R.drawable.ic_livetv, "Live TV", "Antenna / digital channels", "READY", Color.parseColor("#34C759")));
        grid.addView(createSourceCard(1, R.drawable.ic_input, "HDMI 1", "Game console", "CONNECTED", NothingUiHelper.COLOR_ACCENT_RED));
        grid.addView(createSourceCard(2, R.drawable.ic_input, "HDMI 2", "No signal", "UNAVAILABLE", NothingUiHelper.COLOR_TEXT_DIM));
        grid.addView(createSourceCard(3, R.drawable.ic_input, "HDMI 3", "No signal", "UNAVAILABLE", NothingUiHelper.COLOR_TEXT_DIM));
        grid.addView(createSourceCard(4, R.drawable.ic_media, "USB drive", "Media storage", "CONNECTED", NothingUiHelper.COLOR_TEXT_GRAY));
        grid.addView(createSourceCard(5, R.drawable.ic_screen, "Cast", "From your phone", "READY", Color.parseColor("#34C759")));

        addView(grid);

        // Bottom hint
        TextView hint = new TextView(getContext());
        hint.setText("Hold OK on an HDMI input to rename it or change its icon.");
        hint.setTextColor(NothingUiHelper.COLOR_TEXT_DIM);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        hint.setPadding((int) (40 * density), 0, (int) (40 * density), (int) (6 * density));
        addView(hint);

        // 4. Footer
        addView(NothingUiHelper.createFooter(getContext(), "05 / INPUTS"));
    }

    private FrameLayout createSourceCard(int index, int iconRes, String title, String subtitle, String statusText, int statusColor) {
        float density = getResources().getDisplayMetrics().density;
        FrameLayout card = new FrameLayout(getContext());
        card.setFocusable(true);
        card.setFocusableInTouchMode(true);
        card.setClickable(true);

        GridLayout.LayoutParams glp = new GridLayout.LayoutParams();
        glp.width = (int) (274 * density);
        glp.height = (int) (88 * density);
        glp.setMargins((int) (6 * density), (int) (6 * density), (int) (6 * density), (int) (6 * density));
        card.setLayoutParams(glp);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(18 * density);
        bg.setColor(NothingUiHelper.COLOR_CARD_DARK);
        bg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
        card.setBackground(bg);

        LinearLayout inner = new LinearLayout(getContext());
        inner.setOrientation(HORIZONTAL);
        inner.setGravity(Gravity.CENTER_VERTICAL);
        inner.setPadding((int) (14 * density), 0, (int) (14 * density), 0);
        card.addView(inner, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Icon
        ImageView iv = new ImageView(getContext());
        iv.setImageResource(iconRes);
        iv.setColorFilter(Color.WHITE);
        int iconSize = (int) (28 * density);
        inner.addView(iv, new LinearLayout.LayoutParams(iconSize, iconSize));

        // Texts
        LinearLayout textCol = new LinearLayout(getContext());
        textCol.setOrientation(VERTICAL);
        LinearLayout.LayoutParams tcLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tcLp.setMarginStart((int) (12 * density));
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
        tvSub.setPadding(0, (int) (2 * density), 0, 0);
        textCol.addView(tvSub);

        inner.addView(textCol);

        // Status badge
        TextView statusBadge = new TextView(getContext());
        statusBadge.setText(statusText);
        statusBadge.setTextColor(statusColor);
        statusBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9f);
        statusBadge.setTypeface(Typeface.DEFAULT_BOLD);
        statusBadge.setGravity(Gravity.CENTER);
        statusBadge.setPadding((int) (8 * density), (int) (3 * density), (int) (8 * density), (int) (3 * density));

        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(10 * density);
        badgeBg.setColor(Color.parseColor("#252525"));
        statusBadge.setBackground(badgeBg);
        inner.addView(statusBadge);

        // Focus styling
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

        card.setOnClickListener(v -> {
            if (onInputSelect != null) onInputSelect.accept(index);
        });

        return card;
    }

    public void setOnInputSelect(Consumer<Integer> callback) {
        this.onInputSelect = callback;
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
