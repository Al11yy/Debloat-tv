package com.gathin.tvhome;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.provider.Settings;
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
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.function.Consumer;

/**
 * Page 06 — QUICK SETTINGS
 * 4x2 grid of quick tiles + volume slider + "All settings" shortcut.
 */
public class QuickSettingsPageView extends LinearLayout {

    private AudioManager audioManager;
    private TextView volumeValueText;
    private View volumeProgress;
    private int maxVolume = 100;
    private int currentVolume = 32;

    private TextView sleepSubText;
    private int sleepIndex = 0;
    private final String[] sleepOptions = new String[]{"Off", "15m", "30m", "60m", "120m"};

    private Runnable onOpenAllSettings;
    private Runnable onOpenInputs;

    public QuickSettingsPageView(Context context) {
        super(context);
        init();
    }

    public QuickSettingsPageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setOrientation(VERTICAL);
        setBackgroundColor(NothingUiHelper.COLOR_BG_DARK);
        float density = getResources().getDisplayMetrics().density;
        audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        // 1. Header
        addView(NothingUiHelper.createHeader(getContext()));

        // 2. Title Block
        addView(NothingUiHelper.createTitleBlock(getContext(), "QUICK SETTINGS", "The controls you reach for most."));

        // 3. 4x2 Tiles Grid
        GridLayout grid = new GridLayout(getContext());
        grid.setColumnCount(4);
        grid.setRowCount(2);
        grid.setPadding((int) (40 * density), (int) (6 * density), (int) (40 * density), (int) (8 * density));
        grid.setClipChildren(false);
        grid.setClipToPadding(false);

        LinearLayout.LayoutParams gridLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        grid.setLayoutParams(gridLp);

        // Row 1: Wi-Fi, Bluetooth, Input, Cast
        grid.addView(createTile(R.drawable.ic_wifi, "Wi-Fi", "Connected", v -> {
            try { getContext().startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); } catch (Exception ignored) {}
        }));
        grid.addView(createTile(R.drawable.ic_settings, "Bluetooth", "On", v -> {
            try { getContext().startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS)); } catch (Exception ignored) {}
        }));
        grid.addView(createTile(R.drawable.ic_input, "Input", "HDMI 1", v -> {
            if (onOpenInputs != null) onOpenInputs.run();
        }));
        grid.addView(createTile(R.drawable.ic_screen, "Cast", "Ready", v -> {
            try { getContext().startActivity(new Intent("android.settings.CAST_SETTINGS")); } catch (Exception ignored) {}
        }));

        // Row 2: Picture, Sound, Sleep timer, Power
        grid.addView(createTile(R.drawable.ic_picture, "Picture", "Cinema", v -> {
            try { getContext().startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS)); } catch (Exception ignored) {}
        }));
        grid.addView(createTile(R.drawable.ic_settings, "Sound", "Standard", v -> {
            try { getContext().startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS)); } catch (Exception ignored) {}
        }));

        FrameLayout sleepTile = createTile(R.drawable.ic_settings, "Sleep timer", "Off", v -> cycleSleepTimer());
        sleepSubText = (TextView) ((LinearLayout) sleepTile.getChildAt(0)).getChildAt(2);
        grid.addView(sleepTile);

        grid.addView(createTile(R.drawable.ic_settings, "Power", "Standby", v -> {
            try { getContext().startActivity(new Intent(Intent.ACTION_POWER_USAGE_SUMMARY)); } catch (Exception ignored) {}
        }));

        addView(grid);

        // 4. Volume Row + All Settings
        LinearLayout bottomRow = new LinearLayout(getContext());
        bottomRow.setOrientation(HORIZONTAL);
        bottomRow.setGravity(Gravity.CENTER_VERTICAL);
        bottomRow.setPadding((int) (40 * density), (int) (8 * density), (int) (40 * density), (int) (10 * density));

        // Volume Interactive Container
        FrameLayout volContainer = new FrameLayout(getContext());
        volContainer.setFocusable(true);
        volContainer.setFocusableInTouchMode(true);
        volContainer.setClickable(true);

        int volWidth = (int) (520 * density);
        int volHeight = (int) (44 * density);
        volContainer.setLayoutParams(new LinearLayout.LayoutParams(volWidth, volHeight));

        GradientDrawable volBg = new GradientDrawable();
        volBg.setCornerRadius(14 * density);
        volBg.setColor(NothingUiHelper.COLOR_CARD_DARK);
        volBg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
        volContainer.setBackground(volBg);

        LinearLayout volInner = new LinearLayout(getContext());
        volInner.setOrientation(HORIZONTAL);
        volInner.setGravity(Gravity.CENTER_VERTICAL);
        volInner.setPadding((int) (16 * density), 0, (int) (16 * density), 0);
        volContainer.addView(volInner, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView volLabel = new TextView(getContext());
        volLabel.setText("Volume  🔊");
        volLabel.setTextColor(Color.WHITE);
        volLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        volLabel.setTypeface(Typeface.DEFAULT_BOLD);
        volInner.addView(volLabel);

        // Custom volume track
        FrameLayout barTrack = new FrameLayout(getContext());
        LinearLayout.LayoutParams trackLp = new LinearLayout.LayoutParams(0, (int) (4 * density), 1.0f);
        trackLp.setMargins((int) (14 * density), 0, (int) (14 * density), 0);
        barTrack.setLayoutParams(trackLp);

        GradientDrawable trackBg = new GradientDrawable();
        trackBg.setCornerRadius(2 * density);
        trackBg.setColor(Color.parseColor("#333333"));
        barTrack.setBackground(trackBg);

        volumeProgress = new View(getContext());
        GradientDrawable progBg = new GradientDrawable();
        progBg.setCornerRadius(2 * density);
        progBg.setColor(NothingUiHelper.COLOR_ACCENT_RED);
        volumeProgress.setBackground(progBg);
        barTrack.addView(volumeProgress, new FrameLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT));

        volInner.addView(barTrack);

        volumeValueText = new TextView(getContext());
        volumeValueText.setTextColor(Color.WHITE);
        volumeValueText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        volumeValueText.setTypeface(Typeface.DEFAULT_BOLD);
        volInner.addView(volumeValueText);

        syncSystemVolume();

        volContainer.setOnFocusChangeListener((v, hasFocus) -> {
            volBg.setColor(hasFocus ? Color.WHITE : NothingUiHelper.COLOR_CARD_DARK);
            volBg.setStroke((int) (hasFocus ? 2 * density : 1 * density),
                    hasFocus ? NothingUiHelper.COLOR_ACCENT_RED : NothingUiHelper.COLOR_CARD_STROKE);
            volLabel.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
            volumeValueText.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
        });

        volContainer.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                adjustVolume(-5);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                adjustVolume(5);
                return true;
            }
            return false;
        });

        bottomRow.addView(volContainer);

        // Gap
        View gap = new View(getContext());
        bottomRow.addView(gap, new LinearLayout.LayoutParams(0, 1, 1.0f));

        // "All settings" Button
        FrameLayout btnAllSettings = new FrameLayout(getContext());
        btnAllSettings.setFocusable(true);
        btnAllSettings.setFocusableInTouchMode(true);
        btnAllSettings.setClickable(true);

        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, (int) (40 * density));
        btnAllSettings.setLayoutParams(btnLp);

        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setCornerRadius(20 * density);
        btnBg.setColor(NothingUiHelper.COLOR_CARD_DARK);
        btnBg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
        btnAllSettings.setBackground(btnBg);

        LinearLayout btnInner = new LinearLayout(getContext());
        btnInner.setOrientation(HORIZONTAL);
        btnInner.setGravity(Gravity.CENTER_VERTICAL);
        btnInner.setPadding((int) (16 * density), 0, (int) (18 * density), 0);
        btnAllSettings.addView(btnInner, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ImageView setIcon = new ImageView(getContext());
        setIcon.setImageResource(R.drawable.ic_settings);
        setIcon.setColorFilter(Color.WHITE);
        btnInner.addView(setIcon, new LinearLayout.LayoutParams((int) (16 * density), (int) (16 * density)));

        TextView setLabel = new TextView(getContext());
        setLabel.setText("All settings");
        setLabel.setTextColor(Color.WHITE);
        setLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        setLabel.setTypeface(Typeface.DEFAULT_BOLD);
        setLabel.setPadding((int) (8 * density), 0, 0, 0);
        btnInner.addView(setLabel);

        btnAllSettings.setOnFocusChangeListener((v, hasFocus) -> {
            btnBg.setColor(hasFocus ? Color.WHITE : NothingUiHelper.COLOR_CARD_DARK);
            btnBg.setStroke((int) (hasFocus ? 2 * density : 1 * density),
                    hasFocus ? NothingUiHelper.COLOR_ACCENT_RED : NothingUiHelper.COLOR_CARD_STROKE);
            setLabel.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
            setIcon.setColorFilter(hasFocus ? Color.BLACK : Color.WHITE);
        });

        btnAllSettings.setOnClickListener(v -> {
            if (onOpenAllSettings != null) onOpenAllSettings.run();
        });

        bottomRow.addView(btnAllSettings);
        addView(bottomRow);

        // 5. Footer
        addView(NothingUiHelper.createFooter(getContext(), "06 / QUICK SETTINGS"));
    }

    private FrameLayout createTile(int iconRes, String title, String subtitle, OnClickListener onClick) {
        float density = getResources().getDisplayMetrics().density;
        FrameLayout tile = new FrameLayout(getContext());
        tile.setFocusable(true);
        tile.setFocusableInTouchMode(true);
        tile.setClickable(true);

        GridLayout.LayoutParams glp = new GridLayout.LayoutParams();
        glp.width = (int) (204 * density);
        glp.height = (int) (86 * density);
        glp.setMargins((int) (6 * density), (int) (6 * density), (int) (6 * density), (int) (6 * density));
        tile.setLayoutParams(glp);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(16 * density);
        bg.setColor(NothingUiHelper.COLOR_CARD_DARK);
        bg.setStroke((int) (1 * density), NothingUiHelper.COLOR_CARD_STROKE);
        tile.setBackground(bg);

        LinearLayout inner = new LinearLayout(getContext());
        inner.setOrientation(VERTICAL);
        inner.setGravity(Gravity.CENTER_VERTICAL);
        inner.setPadding((int) (14 * density), 0, (int) (14 * density), 0);
        tile.addView(inner, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ImageView iv = new ImageView(getContext());
        iv.setImageResource(iconRes);
        iv.setColorFilter(Color.WHITE);
        int iconSize = (int) (22 * density);
        inner.addView(iv, new LinearLayout.LayoutParams(iconSize, iconSize));

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setPadding(0, (int) (4 * density), 0, 0);
        inner.addView(tvTitle);

        TextView tvSub = new TextView(getContext());
        tvSub.setText(subtitle);
        tvSub.setTextColor(NothingUiHelper.COLOR_TEXT_GRAY);
        tvSub.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f);
        inner.addView(tvSub);

        tile.setOnFocusChangeListener((v, hasFocus) -> {
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

        if (onClick != null) tile.setOnClickListener(onClick);
        return tile;
    }

    private void cycleSleepTimer() {
        sleepIndex = (sleepIndex + 1) % sleepOptions.length;
        if (sleepSubText != null) sleepSubText.setText(sleepOptions[sleepIndex]);
    }

    private void syncSystemVolume() {
        if (audioManager != null) {
            int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int cur = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (max > 0) {
                currentVolume = (int) (((float) cur / max) * 100);
            }
        }
        updateVolumeUi();
    }

    private void adjustVolume(int delta) {
        currentVolume = Math.max(0, Math.min(100, currentVolume + delta));
        if (audioManager != null) {
            int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int target = (int) (((float) currentVolume / 100f) * max);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0);
        }
        updateVolumeUi();
    }

    private void updateVolumeUi() {
        if (volumeValueText != null) {
            volumeValueText.setText(String.valueOf(currentVolume));
        }
        if (volumeProgress != null) {
            volumeProgress.post(() -> {
                View parent = (View) volumeProgress.getParent();
                if (parent != null) {
                    int w = parent.getWidth();
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) volumeProgress.getLayoutParams();
                    lp.width = (int) (w * (currentVolume / 100f));
                    volumeProgress.setLayoutParams(lp);
                }
            });
        }
    }

    public void setCallbacks(Runnable onOpenAllSettings, Runnable onOpenInputs) {
        this.onOpenAllSettings = onOpenAllSettings;
        this.onOpenInputs = onOpenInputs;
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
