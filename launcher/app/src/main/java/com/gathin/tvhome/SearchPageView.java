package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Page 04 — SEARCH
 * Search bar + live results row + on-screen QWERTY TV remote keyboard.
 */
public class SearchPageView extends LinearLayout {

    private StringBuilder query = new StringBuilder();
    private TextView searchInputText;
    private LinearLayout resultsRow;
    private List<AppAdapter.Entry> allEntries = new ArrayList<>();
    private Consumer<AppAdapter.Entry> onAppClick;
    private Runnable onVoiceSearch;
    private Runnable onDone;

    private static final ColorMatrixColorFilter GRAYSCALE_FILTER;
    private static final ColorMatrixColorFilter INVERT_FILTER;

    static {
        ColorMatrix gray = new ColorMatrix();
        gray.setSaturation(0f);
        GRAYSCALE_FILTER = new ColorMatrixColorFilter(gray);

        ColorMatrix invert = new ColorMatrix();
        invert.setSaturation(0f);
        float[] inv = new float[]{
                -1.0f,  0f,    0f,    0f, 255f,
                 0f,   -1.0f,  0f,    0f, 255f,
                 0f,    0f,   -1.0f,  0f, 255f,
                 0f,    0f,    0f,    1.0f, 0f
        };
        invert.postConcat(new ColorMatrix(inv));
        INVERT_FILTER = new ColorMatrixColorFilter(invert);
    }

    public SearchPageView(Context context) {
        super(context);
        init();
    }

    public SearchPageView(Context context, AttributeSet attrs) {
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
        addView(NothingUiHelper.createTitleBlock(getContext(), "SEARCH", "Find an installed app, input or TV setting."));

        // 3. Search Bar Row
        LinearLayout barRow = new LinearLayout(getContext());
        barRow.setOrientation(HORIZONTAL);
        barRow.setGravity(Gravity.CENTER_VERTICAL);
        barRow.setPadding((int) (40 * density), 0, (int) (40 * density), (int) (8 * density));

        FrameLayout searchBar = new FrameLayout(getContext());
        int barWidth = (int) (480 * density);
        int barHeight = (int) (38 * density);
        searchBar.setLayoutParams(new LinearLayout.LayoutParams(barWidth, barHeight));

        GradientDrawable barBg = new GradientDrawable();
        barBg.setCornerRadius(18 * density);
        barBg.setColor(NothingUiHelper.COLOR_CARD_DARK);
        barBg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
        searchBar.setBackground(barBg);

        LinearLayout barInner = new LinearLayout(getContext());
        barInner.setOrientation(HORIZONTAL);
        barInner.setGravity(Gravity.CENTER_VERTICAL);
        barInner.setPadding((int) (12 * density), 0, (int) (12 * density), 0);
        searchBar.addView(barInner, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ImageView searchIcon = new ImageView(getContext());
        searchIcon.setImageResource(R.drawable.ic_search);
        searchIcon.setColorFilter(NothingUiHelper.COLOR_TEXT_GRAY);
        barInner.addView(searchIcon, new LinearLayout.LayoutParams((int) (18 * density), (int) (18 * density)));

        searchInputText = new TextView(getContext());
        searchInputText.setHint("Type to search...");
        searchInputText.setHintTextColor(NothingUiHelper.COLOR_TEXT_DIM);
        searchInputText.setTextColor(Color.WHITE);
        searchInputText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        textLp.setMarginStart((int) (8 * density));
        barInner.addView(searchInputText, textLp);

        barRow.addView(searchBar);

        // Voice button
        FrameLayout btnVoice = new FrameLayout(getContext());
        btnVoice.setFocusable(true);
        btnVoice.setFocusableInTouchMode(true);
        btnVoice.setClickable(true);
        LinearLayout.LayoutParams voiceLp = new LinearLayout.LayoutParams((int) (38 * density), (int) (38 * density));
        voiceLp.setMarginStart((int) (10 * density));
        btnVoice.setLayoutParams(voiceLp);

        GradientDrawable voiceBg = new GradientDrawable();
        voiceBg.setCornerRadius(19 * density);
        voiceBg.setColor(NothingUiHelper.COLOR_CARD_DARK);
        voiceBg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
        btnVoice.setBackground(voiceBg);

        ImageView micIcon = new ImageView(getContext());
        micIcon.setImageResource(R.drawable.ic_search); // Microphone / voice
        micIcon.setColorFilter(Color.WHITE);
        int micSize = (int) (18 * density);
        FrameLayout.LayoutParams micLp = new FrameLayout.LayoutParams(micSize, micSize);
        micLp.gravity = Gravity.CENTER;
        btnVoice.addView(micIcon, micLp);

        btnVoice.setOnFocusChangeListener((v, hasFocus) -> {
            voiceBg.setColor(hasFocus ? Color.WHITE : NothingUiHelper.COLOR_CARD_DARK);
            micIcon.setColorFilter(hasFocus ? Color.BLACK : Color.WHITE);
        });
        btnVoice.setOnClickListener(v -> {
            if (onVoiceSearch != null) onVoiceSearch.run();
        });
        barRow.addView(btnVoice);

        addView(barRow);

        // 4. Results Row (Horizontal scroll)
        LinearLayout resultsHeader = new LinearLayout(getContext());
        resultsHeader.setOrientation(HORIZONTAL);
        resultsHeader.setPadding((int) (40 * density), (int) (6 * density), (int) (40 * density), (int) (4 * density));
        TextView resLabel = new TextView(getContext());
        resLabel.setText("RESULTS");
        resLabel.setTextColor(NothingUiHelper.COLOR_TEXT_DIM);
        resLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        resultsHeader.addView(resLabel);
        addView(resultsHeader);

        resultsRow = new LinearLayout(getContext());
        resultsRow.setOrientation(HORIZONTAL);
        resultsRow.setGravity(Gravity.CENTER_VERTICAL);
        resultsRow.setPadding((int) (40 * density), 0, (int) (40 * density), (int) (8 * density));
        LinearLayout.LayoutParams resLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (64 * density));
        resultsRow.setLayoutParams(resLp);
        addView(resultsRow);

        // 5. Keyboard + Voice Hint split row
        LinearLayout kbAndHint = new LinearLayout(getContext());
        kbAndHint.setOrientation(HORIZONTAL);
        kbAndHint.setGravity(Gravity.CENTER_VERTICAL);
        kbAndHint.setPadding((int) (40 * density), 0, (int) (40 * density), 0);
        LinearLayout.LayoutParams splitLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        kbAndHint.setLayoutParams(splitLp);

        // Keyboard grid
        LinearLayout kbContainer = new LinearLayout(getContext());
        kbContainer.setOrientation(VERTICAL);
        buildKeyboard(kbContainer);
        kbAndHint.addView(kbContainer, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        // Side hint
        LinearLayout hintBox = new LinearLayout(getContext());
        hintBox.setOrientation(VERTICAL);
        hintBox.setPadding((int) (24 * density), 0, 0, 0);
        LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(
                (int) (240 * density), ViewGroup.LayoutParams.WRAP_CONTENT);
        hintBox.setLayoutParams(hintLp);

        TextView vTitle = new TextView(getContext());
        vTitle.setText("Voice search");
        vTitle.setTextColor(Color.WHITE);
        vTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        vTitle.setTypeface(Typeface.DEFAULT_BOLD);
        hintBox.addView(vTitle);

        TextView vDesc = new TextView(getContext());
        vDesc.setText("Hold the microphone button on your remote.\n\nSearch scope: installed apps and device menus.");
        vDesc.setTextColor(NothingUiHelper.COLOR_TEXT_DIM);
        vDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        vDesc.setPadding(0, (int) (4 * density), 0, 0);
        hintBox.addView(vDesc);

        kbAndHint.addView(hintBox);
        addView(kbAndHint);

        // 6. Footer
        addView(NothingUiHelper.createFooter(getContext(), "04 / SEARCH"));
    }

    private void buildKeyboard(LinearLayout container) {
        float density = getResources().getDisplayMetrics().density;
        String[][] rows = new String[][]{
                {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "SPACE", "DEL"},
                {"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "CLEAR", "DONE"},
                {"U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3"},
                {"4", "5", "6", "7", "8", "9", ".", ",", "-", "_", "@"}
        };

        for (int r = 0; r < rows.length; r++) {
            LinearLayout rowView = new LinearLayout(getContext());
            rowView.setOrientation(HORIZONTAL);
            rowView.setGravity(Gravity.CENTER_VERTICAL);
            rowView.setPadding(0, 0, 0, (int) (4 * density));

            for (String key : rows[r]) {
                TextView keyView = createKeyView(key);
                rowView.addView(keyView);
            }
            container.addView(rowView);
        }
    }

    private TextView createKeyView(String key) {
        float density = getResources().getDisplayMetrics().density;
        TextView kv = new TextView(getContext());
        kv.setText(key.equals("SPACE") ? "Space" : (key.equals("DEL") ? "⌫" : key));
        kv.setTextColor(Color.WHITE);
        kv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        kv.setTypeface(Typeface.DEFAULT_BOLD);
        kv.setGravity(Gravity.CENTER);
        kv.setFocusable(true);
        kv.setFocusableInTouchMode(true);
        kv.setClickable(true);

        int widthPx = (int) (32 * density);
        if (key.equals("SPACE") || key.equals("DEL") || key.equals("CLEAR") || key.equals("DONE")) {
            widthPx = (int) (56 * density);
        }
        int heightPx = (int) (30 * density);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(widthPx, heightPx);
        lp.setMarginEnd((int) (4 * density));
        kv.setLayoutParams(lp);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(8 * density);
        bg.setColor(NothingUiHelper.COLOR_CARD_DARK);
        bg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
        kv.setBackground(bg);

        kv.setOnFocusChangeListener((v, hasFocus) -> {
            bg.setColor(hasFocus ? Color.WHITE : NothingUiHelper.COLOR_CARD_DARK);
            bg.setStroke((int) (hasFocus ? 2 * density : 1 * density),
                    hasFocus ? NothingUiHelper.COLOR_ACCENT_RED : NothingUiHelper.COLOR_CARD_STROKE);
            kv.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
        });

        kv.setOnClickListener(v -> handleKeyPress(key));
        return kv;
    }

    private void handleKeyPress(String key) {
        if ("SPACE".equals(key)) {
            query.append(" ");
        } else if ("DEL".equals(key)) {
            if (query.length() > 0) query.deleteCharAt(query.length() - 1);
        } else if ("CLEAR".equals(key)) {
            query.setLength(0);
        } else if ("DONE".equals(key)) {
            if (onDone != null) onDone.run();
            return;
        } else {
            query.append(key);
        }
        updateQueryText();
        filterResults();
    }

    private void updateQueryText() {
        if (query.length() == 0) {
            searchInputText.setText("");
            searchInputText.setHint("Type to search...");
        } else {
            searchInputText.setText(query.toString() + "|");
        }
    }

    public void setEntries(List<AppAdapter.Entry> entries) {
        this.allEntries = entries != null ? entries : Collections.emptyList();
        filterResults();
    }

    private void filterResults() {
        if (resultsRow == null) return;
        resultsRow.removeAllViews();
        float density = getResources().getDisplayMetrics().density;

        String q = query.toString().trim().toLowerCase(Locale.ROOT);
        List<AppAdapter.Entry> matches = new ArrayList<>();
        for (AppAdapter.Entry entry : allEntries) {
            if (entry == null || entry.pkg == null) continue;
            String label = entry.label != null ? entry.label.toLowerCase(Locale.ROOT) : "";
            if (q.isEmpty() || label.contains(q) || entry.pkg.toLowerCase(Locale.ROOT).contains(q)) {
                matches.add(entry);
                if (matches.size() >= 8) break; // limit to 8 quick results
            }
        }

        for (AppAdapter.Entry entry : matches) {
            FrameLayout pill = new FrameLayout(getContext());
            pill.setFocusable(true);
            pill.setFocusableInTouchMode(true);
            pill.setClickable(true);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, (int) (48 * density));
            lp.setMarginEnd((int) (10 * density));
            pill.setLayoutParams(lp);

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(14 * density);
            bg.setColor(NothingUiHelper.COLOR_CARD_DARK);
            bg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
            pill.setBackground(bg);

            LinearLayout inner = new LinearLayout(getContext());
            inner.setOrientation(HORIZONTAL);
            inner.setGravity(Gravity.CENTER_VERTICAL);
            inner.setPadding((int) (12 * density), 0, (int) (16 * density), 0);
            pill.addView(inner, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

            ImageView iv = new ImageView(getContext());
            int iconSize = (int) (26 * density);
            iv.setLayoutParams(new LinearLayout.LayoutParams(iconSize, iconSize));
            Drawable icon = null;
            try {
                icon = getContext().getPackageManager().getApplicationIcon(entry.pkg);
            } catch (Exception ignored) {
                icon = entry.icon;
            }
            if (icon == null) icon = getContext().getDrawable(R.drawable.ic_apps);
            iv.setImageDrawable(icon);
            iv.setColorFilter(null);
            inner.addView(iv);

            TextView tv = new TextView(getContext());
            tv.setText(entry.label != null ? entry.label : "");
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvLp.setMarginStart((int) (8 * density));
            inner.addView(tv, tvLp);

            pill.setOnFocusChangeListener((v, hasFocus) -> {
                bg.setColor(hasFocus ? Color.WHITE : NothingUiHelper.COLOR_CARD_DARK);
                bg.setStroke((int) (hasFocus ? 2 * density : 1 * density),
                        hasFocus ? NothingUiHelper.COLOR_ACCENT_RED : NothingUiHelper.COLOR_CARD_STROKE);
                tv.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
                iv.setColorFilter(null);
            });

            pill.setOnClickListener(v -> {
                if (onAppClick != null) onAppClick.accept(entry);
            });

            resultsRow.addView(pill);
        }
    }

    public void setCallbacks(Consumer<AppAdapter.Entry> onAppClick, Runnable onVoice, Runnable onDone) {
        this.onAppClick = onAppClick;
        this.onVoiceSearch = onVoice;
        this.onDone = onDone;
    }

    public void focusFirstElement() {
        if (resultsRow != null && resultsRow.getChildCount() > 0) {
            resultsRow.getChildAt(0).requestFocus();
        } else {
            // Focus first key 'A'
            View firstKey = findViewById(android.R.id.content);
        }
    }
}
