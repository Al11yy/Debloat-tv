package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Nothing OS style Prayer Times Widget Card.
 * Displays Next Prayer countdown, large NDOT time, and table of 6 daily prayer times.
 * Default location: Kecamatan Tanah Sareal, Kota Bogor, West Java, Indonesia.
 */
public class PrayerTimesCardView extends View {

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF rect = new RectF();
    private final RectF pillRect = new RectF();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            invalidate();
            handler.postDelayed(this, 30000L); // check every 30s
        }
    };

    // Default to Kecamatan Tanah Sareal, Bogor, West Java, Indonesia (-6.5650, 106.7900)
    private double latitude = PrayerCalculator.DEFAULT_LATITUDE;
    private double longitude = PrayerCalculator.DEFAULT_LONGITUDE;

    public PrayerTimesCardView(Context context) {
        super(context);
        init();
    }

    public PrayerTimesCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PrayerTimesCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFocusable(true);
        setClickable(true);

        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.parseColor("#1E1E1E"));

        pillPaint.setStyle(Paint.Style.FILL);
        pillPaint.setColor(Color.parseColor("#FF3B30"));

        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);

        subTextPaint.setStyle(Paint.Style.FILL);
        subTextPaint.setColor(Color.parseColor("#999999"));

        dotTextPaint.setStyle(Paint.Style.FILL);
        dotTextPaint.setColor(Color.WHITE);

        setOnFocusChangeListener((v, hasFocus) -> invalidate());
    }

    public void setCoordinates(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.post(ticker);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(ticker);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();
        if (w <= 0 || h <= 0) return;

        float density = getResources().getDisplayMetrics().density;
        float cornerRadius = 18f * density;

        // Draw card background
        rect.set(0, 0, w, h);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint);

        // Hover / Focus border in Nothing Red (#FF3B30)
        if (hasFocus()) {
            Paint focusBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
            focusBorder.setStyle(Paint.Style.STROKE);
            focusBorder.setStrokeWidth(2f * density);
            focusBorder.setColor(Color.parseColor("#FF3B30"));
            RectF borderRect = new RectF(1.5f * density, 1.5f * density, w - 1.5f * density, h - 1.5f * density);
            canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, focusBorder);
        }

        Calendar now = Calendar.getInstance();
        double tzOffset = TimeZone.getDefault().getOffset(now.getTimeInMillis()) / 3600000.0;

        PrayerCalculator.Schedule schedule = PrayerCalculator.compute(now, latitude, longitude, tzOffset);
        PrayerCalculator.NextPrayerInfo next = PrayerCalculator.getNextPrayer(now, latitude, longitude, tzOffset);

        float padX = 20f * density;
        float curY = 22f * density;

        // Header: "PRAYER TIMES" (NDOT style)
        dotTextPaint.setColor(Color.parseColor("#CCCCCC"));
        float headerDotRadius = 0.75f * density;
        float headerDotSpacing = 1.9f * density;
        float headerCharSpacing = 1.3f * density;
        NdotHelper.drawText(canvas, "PRAYER TIMES", padX, curY, headerDotRadius, headerDotSpacing, headerCharSpacing, dotTextPaint);

        // Next Prayer pill: "NEXT FAJR" (or next prayer)
        String pillText = "NEXT " + next.name;
        float pillDotRadius = 0.75f * density;
        float pillDotSpacing = 1.8f * density;
        float pillCharSpacing = 1.2f * density;
        float pillTextW = NdotHelper.measureWidth(pillText, pillDotSpacing, pillCharSpacing);
        float pillW = pillTextW + (12f * density);
        float pillH = 15f * density;
        float pillX = w - padX - pillW;
        float pillY = curY - 2f * density;

        pillRect.set(pillX, pillY, pillX + pillW, pillY + pillH);
        canvas.drawRoundRect(pillRect, pillH / 2f, pillH / 2f, pillPaint);

        dotTextPaint.setColor(Color.WHITE);
        NdotHelper.drawText(canvas, pillText, pillX + (6f * density), pillY + (3f * density),
                pillDotRadius, pillDotSpacing, pillCharSpacing, dotTextPaint);

        // Big Digital Time: next prayer time, e.g. "04:15"
        curY += 22f * density;
        dotTextPaint.setColor(Color.WHITE);
        NdotHelper.drawText(canvas, next.timeFormatted, padX, curY, 2.3f * density, 6.0f * density, 4.6f * density, dotTextPaint);

        // "Tomorrow" or "Today" subtitle
        float timeW = NdotHelper.measureWidth(next.timeFormatted, 6.0f * density, 4.6f * density);
        subTextPaint.setTextSize(12f * density);
        String dayDesc = next.isTomorrow ? "Tomorrow" : "Today";
        canvas.drawText(dayDesc, padX + timeW + (14f * density), curY + (28f * density), subTextPaint);

        // Bottom prayer times table (Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha)
        curY += 56f * density;
        PrayerCalculator.PrayerTime[] all = schedule.all();
        float colWidth = (w - 2 * padX) / (float) all.length;

        textPaint.setTextSize(10.5f * density);
        subTextPaint.setTextSize(12f * density);

        for (int i = 0; i < all.length; i++) {
            PrayerCalculator.PrayerTime p = all[i];
            float cx = padX + i * colWidth;

            // Highlight if this is the active/next prayer
            if (p.name.equalsIgnoreCase(next.name) && !next.isTomorrow) {
                textPaint.setColor(Color.parseColor("#FF3B30"));
                subTextPaint.setColor(Color.parseColor("#FF3B30"));
            } else {
                textPaint.setColor(Color.parseColor("#888888"));
                subTextPaint.setColor(Color.WHITE);
            }

            canvas.drawText(p.name, cx, curY, textPaint);
            canvas.drawText(p.timeString, cx, curY + (16f * density), subTextPaint);
        }
    }
}
