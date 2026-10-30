package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Nothing OS Status Shortcuts Column:
 * 1. Top Card: HDMI 1 / Active input shortcut (82dp)
 * 2. Bottom Left: Wi-Fi Connection status (75dp)
 * 3. Bottom Right: Sleep Timer shortcut (75dp, crescent moon icon)
 */
public class StatusCardView extends LinearLayout {

    private FrameLayout hdmiCard;
    private FrameLayout wifiCard;
    private FrameLayout sleepCard;

    private TextView hdmiTitle;
    private TextView hdmiSub;
    private ImageView hdmiIcon;

    private TextView wifiText;
    private ImageView wifiIcon;

    private TextView sleepLabel;
    private TextView sleepText;
    private ImageView sleepIcon;

    public StatusCardView(Context context) {
        super(context);
        init();
    }

    public StatusCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatusCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        float density = getResources().getDisplayMetrics().density;

        // 1. HDMI Top Card (82dp)
        hdmiCard = createCard(Color.parseColor("#1E1E1E"), (int) (82 * density), 16 * density);
        hdmiCard.setFocusable(true);
        hdmiCard.setClickable(true);

        LinearLayout hdmiContent = new LinearLayout(getContext());
        hdmiContent.setOrientation(HORIZONTAL);
        hdmiContent.setGravity(Gravity.CENTER_VERTICAL);
        hdmiContent.setPadding((int) (14 * density), 0, (int) (14 * density), 0);
        hdmiCard.addView(hdmiContent, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        hdmiIcon = new ImageView(getContext());
        hdmiIcon.setImageResource(R.drawable.ic_input);
        hdmiIcon.setColorFilter(Color.WHITE);
        int iconSize = (int) (26 * density);
        hdmiContent.addView(hdmiIcon, new LinearLayout.LayoutParams(iconSize, iconSize));

        LinearLayout hdmiTexts = new LinearLayout(getContext());
        hdmiTexts.setOrientation(VERTICAL);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textLp.setMarginStart((int) (12 * density));
        hdmiTexts.setLayoutParams(textLp);

        hdmiTitle = new TextView(getContext());
        hdmiTitle.setText("HDMI 1");
        hdmiTitle.setTextColor(Color.WHITE);
        hdmiTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        hdmiTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        hdmiTexts.addView(hdmiTitle);

        hdmiSub = new TextView(getContext());
        hdmiSub.setText("Game console");
        hdmiSub.setTextColor(Color.parseColor("#888888"));
        hdmiSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        hdmiTexts.addView(hdmiSub);

        hdmiContent.addView(hdmiTexts);

        // Red status dot
        View redDot = new View(getContext());
        int dotSize = (int) (6 * density);
        FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(dotSize, dotSize);
        dotParams.gravity = Gravity.TOP | Gravity.END;
        dotParams.topMargin = (int) (10 * density);
        dotParams.rightMargin = (int) (10 * density);
        redDot.setLayoutParams(dotParams);
        GradientDrawable dotDrawable = new GradientDrawable();
        dotDrawable.setShape(GradientDrawable.OVAL);
        dotDrawable.setColor(Color.parseColor("#FF3B30"));
        redDot.setBackground(dotDrawable);
        hdmiCard.addView(redDot);

        addView(hdmiCard);

        // Gap between top and bottom cards (8dp)
        View gap = new View(getContext());
        addView(gap, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (8 * density)));

        // 2. Bottom Row: Wi-Fi + Sleep Timer (75dp)
        LinearLayout bottomRow = new LinearLayout(getContext());
        bottomRow.setOrientation(HORIZONTAL);
        LinearLayout.LayoutParams bottomRowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (75 * density));
        bottomRow.setLayoutParams(bottomRowLp);

        // Wi-Fi Card (86dp width)
        wifiCard = createCard(Color.parseColor("#1E1E1E"), ViewGroup.LayoutParams.MATCH_PARENT, 14 * density);
        wifiCard.setFocusable(true);
        wifiCard.setClickable(true);
        LinearLayout.LayoutParams wifiLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        wifiLp.setMarginEnd((int) (4 * density));
        wifiCard.setLayoutParams(wifiLp);

        LinearLayout wifiContent = new LinearLayout(getContext());
        wifiContent.setOrientation(VERTICAL);
        wifiContent.setGravity(Gravity.CENTER);
        wifiContent.setPadding((int) (6 * density), (int) (6 * density), (int) (6 * density), (int) (6 * density));
        wifiCard.addView(wifiContent, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        wifiIcon = new ImageView(getContext());
        wifiIcon.setImageResource(R.drawable.ic_wifi);
        wifiIcon.setColorFilter(Color.WHITE);
        int wifiIconSize = (int) (22 * density);
        wifiContent.addView(wifiIcon, new LinearLayout.LayoutParams(wifiIconSize, wifiIconSize));

        wifiText = new TextView(getContext());
        wifiText.setText("Connected");
        wifiText.setTextColor(Color.WHITE);
        wifiText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        wifiText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        wifiText.setGravity(Gravity.CENTER);
        wifiText.setSingleLine(true);
        wifiText.setEllipsize(TextUtils.TruncateAt.END);
        wifiText.setPadding(0, (int) (5 * density), 0, 0);
        wifiContent.addView(wifiText);

        bottomRow.addView(wifiCard);

        // Sleep Timer Card (86dp width, Red accent background)
        sleepCard = createCard(Color.parseColor("#FF3B30"), ViewGroup.LayoutParams.MATCH_PARENT, 14 * density);
        sleepCard.setFocusable(true);
        sleepCard.setClickable(true);
        LinearLayout.LayoutParams sleepLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        sleepLp.setMarginStart((int) (4 * density));
        sleepCard.setLayoutParams(sleepLp);

        LinearLayout sleepContent = new LinearLayout(getContext());
        sleepContent.setOrientation(VERTICAL);
        sleepContent.setGravity(Gravity.CENTER);
        sleepContent.setPadding((int) (6 * density), (int) (6 * density), (int) (6 * density), (int) (6 * density));
        sleepCard.addView(sleepContent, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        sleepIcon = new ImageView(getContext());
        sleepIcon.setImageResource(R.drawable.ic_moon);
        sleepIcon.setColorFilter(Color.WHITE);
        int sleepIconSize = (int) (19 * density);
        sleepContent.addView(sleepIcon, new LinearLayout.LayoutParams(sleepIconSize, sleepIconSize));

        sleepLabel = new TextView(getContext());
        sleepLabel.setText("Sleep timer");
        sleepLabel.setTextColor(Color.WHITE);
        sleepLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
        sleepLabel.setGravity(Gravity.CENTER);
        sleepLabel.setSingleLine(true);
        sleepLabel.setPadding(0, (int) (3 * density), 0, 0);
        sleepContent.addView(sleepLabel);

        sleepText = new TextView(getContext());
        sleepText.setText("OFF");
        sleepText.setTextColor(Color.WHITE);
        sleepText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
        sleepText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        sleepText.setGravity(Gravity.CENTER);
        sleepText.setSingleLine(true);
        sleepText.setPadding(0, (int) (1 * density), 0, 0);
        sleepContent.addView(sleepText);

        bottomRow.addView(sleepCard);
        addView(bottomRow);

        // Setup explicit focus listeners
        setupHdmiCardFocus();
        setupWifiCardFocus();
        setupSleepCardFocus();
    }

    private FrameLayout createCard(int color, int heightPx, float cornerRadiusPx) {
        FrameLayout card = new FrameLayout(getContext());
        card.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightPx));
        float density = getResources().getDisplayMetrics().density;
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(cornerRadiusPx);
        gd.setColor(color);
        gd.setStroke((int) (1 * density), Color.parseColor("#2A2A2A"));
        card.setBackground(gd);
        return card;
    }

    private void setupHdmiCardFocus() {
        float density = getResources().getDisplayMetrics().density;
        hdmiCard.setOnFocusChangeListener((v, hasFocus) -> {
            GradientDrawable gd = (GradientDrawable) hdmiCard.getBackground();
            if (gd != null) {
                gd.setColor(hasFocus ? Color.WHITE : Color.parseColor("#1E1E1E"));
                gd.setStroke(
                        hasFocus ? (int) (2 * density) : (int) (1 * density),
                        hasFocus ? Color.parseColor("#FF3B30") : Color.parseColor("#2A2A2A")
                );
            }
            if (hdmiTitle != null) {
                hdmiTitle.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
            }
            if (hdmiSub != null) {
                hdmiSub.setTextColor(hasFocus ? Color.parseColor("#333333") : Color.parseColor("#888888"));
            }
            if (hdmiIcon != null) {
                hdmiIcon.setColorFilter(hasFocus ? Color.BLACK : Color.WHITE);
            }
        });
    }

    private void setupWifiCardFocus() {
        float density = getResources().getDisplayMetrics().density;
        wifiCard.setOnFocusChangeListener((v, hasFocus) -> {
            GradientDrawable gd = (GradientDrawable) wifiCard.getBackground();
            if (gd != null) {
                gd.setColor(hasFocus ? Color.WHITE : Color.parseColor("#1E1E1E"));
                gd.setStroke(
                        hasFocus ? (int) (2 * density) : (int) (1 * density),
                        hasFocus ? Color.parseColor("#FF3B30") : Color.parseColor("#2A2A2A")
                );
            }
            if (wifiText != null) {
                wifiText.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
            }
            if (wifiIcon != null) {
                wifiIcon.setColorFilter(hasFocus ? Color.BLACK : Color.WHITE);
            }
        });
    }

    private void setupSleepCardFocus() {
        float density = getResources().getDisplayMetrics().density;
        int normalBg = Color.parseColor("#FF3B30");
        sleepCard.setOnFocusChangeListener((v, hasFocus) -> {
            GradientDrawable gd = (GradientDrawable) sleepCard.getBackground();
            if (gd != null) {
                gd.setColor(hasFocus ? Color.WHITE : normalBg);
                gd.setStroke(
                        hasFocus ? (int) (2 * density) : 0,
                        hasFocus ? Color.parseColor("#FF3B30") : Color.TRANSPARENT
                );
            }
            if (sleepLabel != null) {
                sleepLabel.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
            }
            if (sleepText != null) {
                sleepText.setTextColor(hasFocus ? Color.parseColor("#FF3B30") : Color.WHITE);
            }
            if (sleepIcon != null) {
                sleepIcon.setColorFilter(hasFocus ? Color.BLACK : Color.WHITE);
            }
        });
    }

    public FrameLayout getHdmiCard() { return hdmiCard; }
    public FrameLayout getWifiCard() { return wifiCard; }
    public FrameLayout getSleepCard() { return sleepCard; }

    public void setWifiStatus(String status) {
        if (wifiText != null && status != null) wifiText.setText(status);
    }
}
