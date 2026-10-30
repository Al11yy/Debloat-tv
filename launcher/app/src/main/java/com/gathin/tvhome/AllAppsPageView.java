package com.gathin.tvhome;

import android.content.Context;
import android.content.pm.ApplicationInfo;
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
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Page 02 — ALL APPS
 * 7-column grid with category filter tabs (All, Video, Music, Utilities)
 * and app counter matching Nothing OS aesthetic.
 */
public class AllAppsPageView extends LinearLayout {

    private Consumer<AppAdapter.Entry> onAppClick;
    private Consumer<AppAdapter.Entry> onAppLongClick;
    private Runnable onBackToHome;

    private List<AppAdapter.Entry> allEntries = new ArrayList<>();
    private String currentCategory = "All";

    private LinearLayout tabsRow;
    private TextView countView;
    private GridLayout gridLayout;
    private ScrollView scrollView;

    private static final ColorMatrixColorFilter GRAYSCALE_FILTER;
    private static final ColorMatrixColorFilter INVERT_FILTER;

    static {
        ColorMatrix gray = new ColorMatrix();
        gray.setSaturation(0f);
        float[] boost = new float[]{
                1.3f, 0f,   0f,   0f, 20f,
                0f,   1.3f, 0f,   0f, 20f,
                0f,   0f,   1.3f, 0f, 20f,
                0f,   0f,   0f,   1f, 0f
        };
        gray.postConcat(new ColorMatrix(boost));
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

    public AllAppsPageView(Context context) {
        super(context);
        init();
    }

    public AllAppsPageView(Context context, AttributeSet attrs) {
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
        addView(NothingUiHelper.createTitleBlock(getContext(), "ALL APPS", "Everything you watch, listen to and open."));

        // 3. Category Filter Tabs Row
        tabsRow = new LinearLayout(getContext());
        tabsRow.setOrientation(HORIZONTAL);
        tabsRow.setGravity(Gravity.CENTER_VERTICAL);
        tabsRow.setPadding((int) (40 * density), 0, (int) (40 * density), (int) (12 * density));

        String[] categories = new String[]{"All", "Video", "Music", "Utilities"};
        for (String cat : categories) {
            TextView tab = createTabPill(cat);
            tabsRow.addView(tab);
        }

        // App counter hint
        countView = new TextView(getContext());
        countView.setTextColor(NothingUiHelper.COLOR_TEXT_DIM);
        countView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        countView.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams countLp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        tabsRow.addView(countView, countLp);

        addView(tabsRow);

        // 4. Scrollable 7-Column Grid
        scrollView = new ScrollView(getContext());
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(false);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        scrollView.setLayoutParams(scrollLp);

        gridLayout = new GridLayout(getContext());
        gridLayout.setColumnCount(7);
        gridLayout.setPadding((int) (40 * density), 0, (int) (40 * density), (int) (16 * density));
        gridLayout.setClipToPadding(false);
        gridLayout.setClipChildren(false);
        scrollView.addView(gridLayout, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addView(scrollView);

        // 5. Footer
        addView(NothingUiHelper.createFooter(getContext(), "02 / APPS"));
    }

    private TextView createTabPill(String cat) {
        float density = getResources().getDisplayMetrics().density;
        TextView pill = new TextView(getContext());
        pill.setText(cat);
        pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        pill.setFocusable(true);
        pill.setClickable(true);
        pill.setGravity(Gravity.CENTER);
        pill.setPadding((int) (16 * density), (int) (6 * density), (int) (16 * density), (int) (6 * density));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, (int) (32 * density));
        lp.setMarginEnd((int) (8 * density));
        pill.setLayoutParams(lp);

        updateTabAppearance(pill, cat.equalsIgnoreCase(currentCategory), false);

        pill.setOnFocusChangeListener((v, hasFocus) -> {
            updateTabAppearance(pill, cat.equalsIgnoreCase(currentCategory), hasFocus);
            if (hasFocus) {
                currentCategory = cat;
                refreshGrid();
                updateAllTabStyles();
            }
        });

        pill.setOnClickListener(v -> {
            currentCategory = cat;
            refreshGrid();
            updateAllTabStyles();
        });

        pill.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (gridLayout.getChildCount() > 0) {
                    gridLayout.getChildAt(0).requestFocus();
                    return true;
                }
            }
            return false;
        });

        return pill;
    }

    private void updateAllTabStyles() {
        for (int i = 0; i < tabsRow.getChildCount(); i++) {
            View v = tabsRow.getChildAt(i);
            if (v instanceof TextView && v != countView) {
                TextView tab = (TextView) v;
                boolean isSelected = tab.getText().toString().equalsIgnoreCase(currentCategory);
                updateTabAppearance(tab, isSelected, tab.hasFocus());
            }
        }
    }

    private void updateTabAppearance(TextView tab, boolean isSelected, boolean hasFocus) {
        float density = getResources().getDisplayMetrics().density;
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(16 * density);

        if (hasFocus) {
            gd.setColor(Color.WHITE);
            gd.setStroke((int) (2 * density), NothingUiHelper.COLOR_ACCENT_RED);
            tab.setTextColor(Color.BLACK);
        } else if (isSelected) {
            gd.setColor(Color.WHITE);
            gd.setStroke(0, Color.TRANSPARENT);
            tab.setTextColor(Color.BLACK);
        } else {
            gd.setColor(NothingUiHelper.COLOR_CARD_DARK);
            gd.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
            tab.setTextColor(NothingUiHelper.COLOR_TEXT_WHITE);
        }
        tab.setBackground(gd);
    }

    public void setEntries(List<AppAdapter.Entry> entries) {
        this.allEntries = entries != null ? entries : Collections.emptyList();
        refreshGrid();
    }

    public void setCallbacks(Consumer<AppAdapter.Entry> onAppClick, Consumer<AppAdapter.Entry> onAppLongClick, Runnable onBack) {
        this.onAppClick = onAppClick;
        this.onAppLongClick = onAppLongClick;
        this.onBackToHome = onBack;
    }

    public void refreshGrid() {
        if (gridLayout == null) return;
        gridLayout.removeAllViews();
        float density = getResources().getDisplayMetrics().density;

        List<AppAdapter.Entry> filtered = filterEntries(allEntries, currentCategory);
        countView.setText(filtered.size() + " apps / Hold OK for options");

        int cardWidth = (int) (114 * density);
        int cardHeight = (int) (105 * density);
        int margin = (int) (5 * density);

        for (int i = 0; i < filtered.size(); i++) {
            final AppAdapter.Entry entry = filtered.get(i);
            final int pos = i;

            FrameLayout card = new FrameLayout(getContext());
            card.setFocusable(true);
            card.setFocusableInTouchMode(true);

            GridLayout.LayoutParams glp = new GridLayout.LayoutParams();
            glp.width = cardWidth;
            glp.height = cardHeight;
            glp.setMargins(margin, margin, margin, margin);
            card.setLayoutParams(glp);

            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(16 * density);
            bg.setColor(NothingUiHelper.COLOR_CARD_DARK);
            bg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
            card.setBackground(bg);

            LinearLayout content = new LinearLayout(getContext());
            content.setOrientation(VERTICAL);
            content.setGravity(Gravity.CENTER);
            card.addView(content, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            ImageView iconView = new ImageView(getContext());
            int iconSize = (int) (42 * density);
            LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(iconSize, iconSize);
            iconView.setLayoutParams(iconLp);
            iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            Drawable icon = null;
            try {
                if (entry.pkg != null) {
                    icon = getContext().getPackageManager().getApplicationIcon(entry.pkg);
                }
            } catch (Exception ignored) {
                icon = entry.icon != null ? entry.icon : entry.banner;
            }
            if (icon == null) icon = getContext().getDrawable(R.drawable.ic_apps);
            iconView.setImageDrawable(icon);
            iconView.setColorFilter(null);
            content.addView(iconView);

            TextView labelView = new TextView(getContext());
            labelView.setText(entry.label != null ? entry.label : "");
            labelView.setTextColor(NothingUiHelper.COLOR_TEXT_WHITE);
            labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            labelView.setSingleLine(true);
            labelView.setEllipsize(TextUtils.TruncateAt.END);
            labelView.setGravity(Gravity.CENTER);
            labelView.setPadding((int) (6 * density), (int) (6 * density), (int) (6 * density), 0);
            content.addView(labelView);

            // Red dot indicator on focus
            View redDot = new View(getContext());
            int dotSize = (int) (6 * density);
            FrameLayout.LayoutParams dotLp = new FrameLayout.LayoutParams(dotSize, dotSize);
            dotLp.gravity = Gravity.TOP | Gravity.END;
            dotLp.topMargin = (int) (8 * density);
            dotLp.rightMargin = (int) (8 * density);
            redDot.setLayoutParams(dotLp);
            GradientDrawable dotGd = new GradientDrawable();
            dotGd.setShape(GradientDrawable.OVAL);
            dotGd.setColor(NothingUiHelper.COLOR_ACCENT_RED);
            redDot.setBackground(dotGd);
            redDot.setVisibility(View.GONE);
            card.addView(redDot);

            // Focus listener
            card.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    bg.setColor(Color.WHITE);
                    bg.setStroke((int) (2 * density), NothingUiHelper.COLOR_ACCENT_RED);
                    labelView.setTextColor(Color.BLACK);
                    iconView.setColorFilter(null);
                    redDot.setVisibility(View.VISIBLE);
                } else {
                    bg.setColor(NothingUiHelper.COLOR_CARD_DARK);
                    bg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
                    labelView.setTextColor(NothingUiHelper.COLOR_TEXT_WHITE);
                    iconView.setColorFilter(null);
                    redDot.setVisibility(View.GONE);
                }
            });

            // Click listener
            card.setOnClickListener(v -> {
                if (onAppClick != null) onAppClick.accept(entry);
            });

            // Long click listener
            card.setOnLongClickListener(v -> {
                if (onAppLongClick != null) {
                    onAppLongClick.accept(entry);
                    return true;
                }
                return false;
            });

            // D-pad Up navigation from first row of grid back to tabs
            card.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
                if (keyCode == KeyEvent.KEYCODE_DPAD_UP && pos < 7) {
                    if (tabsRow.getChildCount() > 0) {
                        tabsRow.getChildAt(0).requestFocus();
                        return true;
                    }
                }
                return false;
            });

            gridLayout.addView(card);
        }
    }

    public void focusFirstElement() {
        if (tabsRow != null && tabsRow.getChildCount() > 0) {
            tabsRow.getChildAt(0).requestFocus();
        } else if (gridLayout != null && gridLayout.getChildCount() > 0) {
            gridLayout.getChildAt(0).requestFocus();
        }
    }

    private List<AppAdapter.Entry> filterEntries(List<AppAdapter.Entry> list, String category) {
        if (category == null || category.equalsIgnoreCase("All")) {
            return list;
        }
        List<AppAdapter.Entry> result = new ArrayList<>();
        String catLower = category.toLowerCase(Locale.ROOT);

        for (AppAdapter.Entry entry : list) {
            if (entry == null || entry.pkg == null) continue;
            String pkg = entry.pkg.toLowerCase(Locale.ROOT);
            String label = entry.label != null ? entry.label.toLowerCase(Locale.ROOT) : "";

            boolean match = false;
            if (catLower.equals("video")) {
                match = pkg.contains("video") || pkg.contains("tv") || pkg.contains("stream")
                        || pkg.contains("youtube") || pkg.contains("netflix") || pkg.contains("disney")
                        || pkg.contains("cinema") || pkg.contains("movie") || pkg.contains("plex")
                        || pkg.contains("kodi") || label.contains("video") || label.contains("tv")
                        || label.contains("movie");
            } else if (catLower.equals("music")) {
                match = pkg.contains("music") || pkg.contains("audio") || pkg.contains("sound")
                        || pkg.contains("spotify") || pkg.contains("radio") || pkg.contains("podcast")
                        || label.contains("music") || label.contains("spotify");
            } else if (catLower.equals("utilities")) {
                match = pkg.contains("settings") || pkg.contains("input") || pkg.contains("share")
                        || pkg.contains("file") || pkg.contains("explorer") || pkg.contains("gallery")
                        || pkg.contains("cast") || pkg.contains("browser") || pkg.contains("clean")
                        || pkg.contains("device") || label.contains("settings") || label.contains("file");
            }

            if (match) {
                result.add(entry);
            }
        }
        return result.isEmpty() ? list : result;
    }
}
