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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Nothing OS style Date Widget Card.
 * Displays Day of Week (NDOT), large Day Number (NDOT), and Red Month / Year (NDOT).
 */
public class DateCardView extends View {

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            invalidate();
            handler.postDelayed(this, 60000L); // once per minute is plenty
        }
    };

    public DateCardView(Context context) {
        super(context);
        init();
    }

    public DateCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DateCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.parseColor("#1E1E1E"));
        setOnFocusChangeListener((v, hasFocus) -> invalidate());
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
        if (hasFocus()) {
            Paint focusBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
            focusBorder.setStyle(Paint.Style.STROKE);
            focusBorder.setStrokeWidth(2f * density);
            focusBorder.setColor(Color.WHITE);
            RectF borderRect = new RectF(1f * density, 1f * density, w - 1f * density, h - 1f * density);
            canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, focusBorder);
        }

        Calendar cal = Calendar.getInstance();
        String dayOfWeek = new SimpleDateFormat("EEEE", Locale.US).format(cal.getTime()).toUpperCase();
        String dayNum = new SimpleDateFormat("dd", Locale.US).format(cal.getTime());
        String monthYear = new SimpleDateFormat("MMM / yyyy", Locale.US).format(cal.getTime()).toUpperCase();

        float padX = 16f * density;
        float curY = 22f * density;

        // Day of week: "FRIDAY"
        dotPaint.setColor(Color.parseColor("#888888"));
        NdotHelper.drawText(canvas, dayOfWeek, padX, curY, 0.75f * density, 1.9f * density, 1.4f * density, dotPaint);

        // Large Day number: "30"
        curY += 20f * density;
        dotPaint.setColor(Color.WHITE);
        NdotHelper.drawText(canvas, dayNum, padX, curY, 2.4f * density, 6.2f * density, 4.6f * density, dotPaint);

        // Month / Year in Red: "OCT / 2026"
        curY += 56f * density;
        dotPaint.setColor(Color.parseColor("#FF3B30"));
        NdotHelper.drawText(canvas, monthYear, padX, curY, 0.75f * density, 1.9f * density, 1.4f * density, dotPaint);
    }
}
