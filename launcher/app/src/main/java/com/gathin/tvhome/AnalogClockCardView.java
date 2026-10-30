package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;

/**
 * Nothing OS style Analog Clock Widget.
 * Dark circular face, 12 dot hour markers (12 o'clock is red), smooth white hands, red center dot.
 */
public class AnalogClockCardView extends View {

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            invalidate();
            handler.postDelayed(this, 10000L); // every 10 seconds is plenty and battery friendly
        }
    };

    private boolean isAttached = false;

    public AnalogClockCardView(Context context) {
        super(context);
        init();
    }

    public AnalogClockCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnalogClockCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.parseColor("#202020"));

        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(Color.parseColor("#80FFFFFF"));

        redPaint.setStyle(Paint.Style.FILL);
        redPaint.setColor(Color.parseColor("#FF3B30"));

        handPaint.setStyle(Paint.Style.STROKE);
        handPaint.setStrokeCap(Paint.Cap.ROUND);
        handPaint.setColor(Color.WHITE);

        setOnFocusChangeListener((v, hasFocus) -> invalidate());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttached = true;
        handler.post(ticker);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttached = false;
        handler.removeCallbacks(ticker);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float cx = w / 2f;
        float cy = h / 2f;
        float radius = Math.min(cx, cy) - 2f;
        float density = getResources().getDisplayMetrics().density;

        // Draw dark circular clock face
        canvas.drawCircle(cx, cy, radius, bgPaint);
        if (hasFocus()) {
            Paint focusBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
            focusBorder.setStyle(Paint.Style.STROKE);
            focusBorder.setStrokeWidth(2f * density);
            focusBorder.setColor(Color.WHITE);
            canvas.drawCircle(cx, cy, radius - 1f * density, focusBorder);
        }

        // Draw 12 dots around dial
        float dotTrackRadius = radius * 0.82f;
        float dotRadius = 2.2f * density;
        float redDotRadius = 2.8f * density;

        for (int i = 0; i < 12; i++) {
            double angleRad = Math.toRadians((i * 30) - 90);
            float dx = cx + (float) (dotTrackRadius * Math.cos(angleRad));
            float dy = cy + (float) (dotTrackRadius * Math.sin(angleRad));

            if (i == 0) {
                // 12 o'clock is red accent dot
                canvas.drawCircle(dx, dy, redDotRadius, redPaint);
            } else {
                canvas.drawCircle(dx, dy, dotRadius, dotPaint);
            }
        }

        // Get current time
        Calendar cal = Calendar.getInstance();
        int hours = cal.get(Calendar.HOUR);
        int minutes = cal.get(Calendar.MINUTE);
        int seconds = cal.get(Calendar.SECOND);

        float minRatio = (minutes + seconds / 60f) / 60f;
        float hourRatio = (hours + minRatio) / 12f;

        // Hour hand
        float hourAngleRad = (float) Math.toRadians((hourRatio * 360f) - 90);
        float hourHandLen = radius * 0.48f;
        handPaint.setStrokeWidth(4.5f * density);
        float hx = cx + (float) (hourHandLen * Math.cos(hourAngleRad));
        float hy = cy + (float) (hourHandLen * Math.sin(hourAngleRad));
        canvas.drawLine(cx, cy, hx, hy, handPaint);

        // Minute hand
        float minAngleRad = (float) Math.toRadians((minRatio * 360f) - 90);
        float minHandLen = radius * 0.72f;
        handPaint.setStrokeWidth(3.0f * density);
        float mx = cx + (float) (minHandLen * Math.cos(minAngleRad));
        float my = cy + (float) (minHandLen * Math.sin(minAngleRad));
        canvas.drawLine(cx, cy, mx, my, handPaint);

        // Center pivot (red circle)
        canvas.drawCircle(cx, cy, 3.8f * density, redPaint);
    }
}
