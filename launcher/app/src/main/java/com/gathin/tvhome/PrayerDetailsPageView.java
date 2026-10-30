package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Color;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Page 08: Detailed Prayer Times & Qibla Page (Nothing OS aesthetic).
 * Full daily schedule for Kecamatan Tanah Sareal, Bogor, Jawa Barat.
 */
public class PrayerDetailsPageView extends FrameLayout {

    private View firstFocusable;
    private Runnable onBackAction;

    public PrayerDetailsPageView(Context context) {
        super(context);
        init();
    }

    public PrayerDetailsPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PrayerDetailsPageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        float density = getResources().getDisplayMetrics().density;
        setBackgroundColor(NothingUiHelper.COLOR_BG_DARK);

        LinearLayout root = new LinearLayout(getContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(root);

        // 1. Header (RAVANDY (TV) + Time + Wi-Fi)
        root.addView(NothingUiHelper.createHeader(getContext()));

        // 2. Title Block
        root.addView(NothingUiHelper.createTitleBlock(getContext(), "PRAYER SCHEDULE",
                "Kecamatan Tanah Sareal, Kota Bogor, Jawa Barat (KEMENAG RI Standard)"));

        // 3. Main Content: 2-Column Layout (Left Hero Card + Right 8 Schedule Tiles)
        LinearLayout contentRow = new LinearLayout(getContext());
        contentRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        contentParams.setMargins((int) (40 * density), (int) (8 * density), (int) (40 * density), (int) (8 * density));
        contentRow.setLayoutParams(contentParams);
        root.addView(contentRow);

        Calendar now = Calendar.getInstance();
        double lat = PrayerCalculator.DEFAULT_LATITUDE;
        double lon = PrayerCalculator.DEFAULT_LONGITUDE;
        double tz = TimeZone.getDefault().getOffset(now.getTimeInMillis()) / 3600000.0;

        PrayerCalculator.Schedule schedule = PrayerCalculator.compute(now, lat, lon, tz);
        PrayerCalculator.NextPrayerInfo next = PrayerCalculator.getNextPrayer(now, lat, lon, tz);
        String countdown = PrayerCalculator.getRemainingTime(now, lat, lon, tz);
        double qibla = PrayerCalculator.computeQibla(lat, lon);

        // LEFT COLUMN: Next Prayer Hero Card & Location Specs (Width: 320dp)
        LinearLayout leftCol = new LinearLayout(getContext());
        leftCol.setOrientation(LinearLayout.VERTICAL);
        leftCol.setLayoutParams(new LinearLayout.LayoutParams((int) (320 * density), ViewGroup.LayoutParams.MATCH_PARENT));

        // Hero Card
        FrameLayout heroCard = new FrameLayout(getContext());
        heroCard.setFocusable(true);
        heroCard.setClickable(true);
        LinearLayout.LayoutParams heroParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        heroCard.setLayoutParams(heroParams);

        GradientDrawable heroBg = new GradientDrawable();
        heroBg.setCornerRadius(18 * density);
        heroBg.setColor(NothingUiHelper.COLOR_CARD_DARK);
        heroBg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
        heroCard.setBackground(heroBg);
        heroCard.setPadding((int) (20 * density), (int) (18 * density), (int) (20 * density), (int) (18 * density));

        LinearLayout heroContent = new LinearLayout(getContext());
        heroContent.setOrientation(LinearLayout.VERTICAL);
        heroCard.addView(heroContent, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Top Row: Dot matrix "NEXT PRAYER" + Red Pill
        LinearLayout heroTop = new LinearLayout(getContext());
        heroTop.setOrientation(LinearLayout.HORIZONTAL);
        heroTop.setGravity(Gravity.CENTER_VERTICAL);
        heroContent.addView(heroTop);

        NdotTextView nextLabel = new NdotTextView(getContext());
        nextLabel.setDotMetrics(0.85f, 2.0f, 1.6f);
        nextLabel.setText("NEXT PRAYER");
        heroTop.addView(nextLabel, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView nextPill = new TextView(getContext());
        nextPill.setText("NEXT " + next.name);
        nextPill.setTextColor(Color.WHITE);
        nextPill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        nextPill.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setCornerRadius(12 * density);
        pillBg.setColor(NothingUiHelper.COLOR_ACCENT_RED);
        nextPill.setBackground(pillBg);
        nextPill.setPadding((int) (8 * density), (int) (2 * density), (int) (8 * density), (int) (2 * density));
        heroTop.addView(nextPill);

        // Big Digital Time in NDOT font
        NdotTextView bigTime = new NdotTextView(getContext());
        bigTime.setDotMetrics(2.4f, 6.2f, 4.8f);
        bigTime.setText(next.timeFormatted);
        LinearLayout.LayoutParams bigTimeLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bigTimeLp.topMargin = (int) (14 * density);
        heroContent.addView(bigTime, bigTimeLp);

        // Countdown text: "Starts in 7h 12m • Today"
        TextView countdownView = new TextView(getContext());
        countdownView.setText("Starts in " + countdown + " • " + (next.isTomorrow ? "Tomorrow" : "Today"));
        countdownView.setTextColor(NothingUiHelper.COLOR_ACCENT_RED);
        countdownView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        countdownView.setTypeface(Typeface.DEFAULT_BOLD);
        countdownView.setPadding(0, (int) (8 * density), 0, (int) (12 * density));
        heroContent.addView(countdownView);

        // Subtle divider line
        View divider = new View(getContext());
        divider.setBackgroundColor(Color.parseColor("#2C2C2C"));
        heroContent.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (1 * density)));

        // Location & Spec Details
        LinearLayout specBox = new LinearLayout(getContext());
        specBox.setOrientation(LinearLayout.VERTICAL);
        specBox.setPadding(0, (int) (12 * density), 0, 0);
        heroContent.addView(specBox);

        addSpecRow(specBox, density, "District", "Kecamatan Tanah Sareal");
        addSpecRow(specBox, density, "City", "Kota Bogor, Jawa Barat");
        addSpecRow(specBox, density, "Coordinates", String.format(Locale.US, "%.4f° S, %.4f° E", Math.abs(lat), lon));
        addSpecRow(specBox, density, "Qibla Direction", String.format(Locale.US, "%.1f° NW (Arah Kiblat)", qibla));
        addSpecRow(specBox, density, "Calc Standard", "KEMENAG RI / SIHAT (UTC+7)");

        heroCard.setOnFocusChangeListener((v, hasFocus) -> {
            heroBg.setColor(hasFocus ? Color.WHITE : NothingUiHelper.COLOR_CARD_DARK);
            heroBg.setStroke((int) ((hasFocus ? 2 : 1) * density),
                    hasFocus ? NothingUiHelper.COLOR_ACCENT_RED : NothingUiHelper.COLOR_CARD_STROKE);
            nextLabel.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
            bigTime.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
        });

        leftCol.addView(heroCard);
        contentRow.addView(leftCol);

        // RIGHT COLUMN: 4x2 Grid of 8 Detailed Prayer Cards
        GridLayout grid = new GridLayout(getContext());
        grid.setColumnCount(4);
        grid.setRowCount(2);
        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        gridParams.setMarginStart((int) (16 * density));
        grid.setLayoutParams(gridParams);
        contentRow.addView(grid);

        PrayerCalculator.PrayerTime[] detailed = new PrayerCalculator.PrayerTime[]{
                schedule.imsak,
                schedule.fajr,
                schedule.sunrise,
                schedule.dhuha,
                schedule.dhuhr,
                schedule.asr,
                schedule.maghrib,
                schedule.isha
        };

        for (int i = 0; i < detailed.length; i++) {
            PrayerCalculator.PrayerTime pt = detailed[i];
            boolean isNextPrayer = pt.name.equalsIgnoreCase(next.name) && !next.isTomorrow;

            View card = createPrayerTile(density, pt, isNextPrayer);
            if (i == 0) firstFocusable = card;

            GridLayout.LayoutParams glp = new GridLayout.LayoutParams();
            glp.width = 0;
            glp.height = 0;
            glp.columnSpec = GridLayout.spec(i % 4, 1.0f);
            glp.rowSpec = GridLayout.spec(i / 4, 1.0f);
            glp.setMargins((int) (6 * density), (int) (6 * density), (int) (6 * density), (int) (6 * density));
            card.setLayoutParams(glp);

            grid.addView(card);
        }

        // 4. Footer (08 / PRAYER SCHEDULE)
        root.addView(NothingUiHelper.createFooter(getContext(), "08 / PRAYER SCHEDULE"));
    }

    private void addSpecRow(LinearLayout container, float density, String label, String value) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, (int) (2 * density), 0, (int) (2 * density));

        TextView lbl = new TextView(getContext());
        lbl.setText(label);
        lbl.setTextColor(NothingUiHelper.COLOR_TEXT_DIM);
        lbl.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        row.addView(lbl, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        TextView val = new TextView(getContext());
        val.setText(value);
        val.setTextColor(NothingUiHelper.COLOR_TEXT_WHITE);
        val.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        val.setTypeface(Typeface.DEFAULT_BOLD);
        row.addView(val);

        container.addView(row);
    }

    private View createPrayerTile(float density, PrayerCalculator.PrayerTime pt, boolean isNext) {
        FrameLayout tile = new FrameLayout(getContext());
        tile.setFocusable(true);
        tile.setClickable(true);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(14 * density);
        bg.setColor(NothingUiHelper.COLOR_CARD_DARK);
        bg.setStroke((int) ((isNext ? 2 : 1) * density),
                isNext ? NothingUiHelper.COLOR_ACCENT_RED : NothingUiHelper.COLOR_CARD_STROKE);
        tile.setBackground(bg);
        tile.setPadding((int) (14 * density), (int) (12 * density), (int) (14 * density), (int) (12 * density));

        LinearLayout content = new LinearLayout(getContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        tile.addView(content, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Top line: Name (Indonesian) + Red Indicator if active
        LinearLayout topRow = new LinearLayout(getContext());
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        content.addView(topRow);

        TextView nameView = new TextView(getContext());
        nameView.setText(pt.indonesianName);
        nameView.setTextColor(isNext ? NothingUiHelper.COLOR_ACCENT_RED : NothingUiHelper.COLOR_TEXT_GRAY);
        nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        nameView.setTypeface(Typeface.DEFAULT_BOLD);
        topRow.addView(nameView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        if (isNext) {
            View dot = new View(getContext());
            int dotSize = (int) (7 * density);
            GradientDrawable dotGd = new GradientDrawable();
            dotGd.setShape(GradientDrawable.OVAL);
            dotGd.setColor(NothingUiHelper.COLOR_ACCENT_RED);
            dot.setBackground(dotGd);
            topRow.addView(dot, new LinearLayout.LayoutParams(dotSize, dotSize));
        }

        // Time: "04:15"
        TextView timeView = new TextView(getContext());
        timeView.setText(pt.timeString);
        timeView.setTextColor(isNext ? NothingUiHelper.COLOR_ACCENT_RED : Color.WHITE);
        timeView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f);
        timeView.setTypeface(Typeface.DEFAULT_BOLD);
        timeView.setPadding(0, (int) (4 * density), 0, (int) (2 * density));
        content.addView(timeView);

        // Subtitle / English Name
        TextView descView = new TextView(getContext());
        descView.setText(pt.name);
        descView.setTextColor(NothingUiHelper.COLOR_TEXT_DIM);
        descView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        content.addView(descView);

        tile.setOnFocusChangeListener((v, hasFocus) -> {
            bg.setColor(hasFocus ? Color.WHITE : NothingUiHelper.COLOR_CARD_DARK);
            bg.setStroke((int) (2 * density),
                    hasFocus ? NothingUiHelper.COLOR_ACCENT_RED : (isNext ? NothingUiHelper.COLOR_ACCENT_RED : NothingUiHelper.COLOR_CARD_STROKE));
            nameView.setTextColor(hasFocus ? Color.BLACK : (isNext ? NothingUiHelper.COLOR_ACCENT_RED : NothingUiHelper.COLOR_TEXT_GRAY));
            timeView.setTextColor(hasFocus ? Color.BLACK : (isNext ? NothingUiHelper.COLOR_ACCENT_RED : Color.WHITE));
            descView.setTextColor(hasFocus ? Color.DKGRAY : NothingUiHelper.COLOR_TEXT_DIM);
        });

        tile.setOnClickListener(v -> {
            if (onBackAction != null) onBackAction.run();
        });

        return tile;
    }

    public void setOnBackAction(Runnable action) {
        this.onBackAction = action;
    }

    public void focusFirstElement() {
        if (firstFocusable != null) {
            firstFocusable.requestFocus();
        }
    }
}
