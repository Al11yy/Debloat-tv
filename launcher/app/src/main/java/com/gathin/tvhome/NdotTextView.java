package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom View that renders text using the Nothing OS dot-matrix style.
 */
public class NdotTextView extends View {

    private String text = "";
    private int textColor = Color.WHITE;
    private float dotRadius = 2.5f;
    private float dotSpacing = 7f;
    private float charSpacing = 6f;
    private boolean centerAlign = false;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public NdotTextView(Context context) {
        super(context);
        init();
    }

    public NdotTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NdotTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(textColor);
        float density = getResources().getDisplayMetrics().density;
        dotRadius = 1.6f * density;
        dotSpacing = 4.2f * density;
        charSpacing = 3.5f * density;
    }

    public void setText(String text) {
        this.text = text != null ? text : "";
        requestLayout();
        invalidate();
    }

    public String getText() {
        return text;
    }

    public void setTextColor(int color) {
        this.textColor = color;
        paint.setColor(color);
        invalidate();
    }

    public void setDotMetrics(float radiusDp, float spacingDp, float charSpacingDp) {
        float d = getResources().getDisplayMetrics().density;
        this.dotRadius = radiusDp * d;
        this.dotSpacing = spacingDp * d;
        this.charSpacing = charSpacingDp * d;
        requestLayout();
        invalidate();
    }

    public void setCenterAlign(boolean center) {
        this.centerAlign = center;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float contentW = NdotHelper.measureWidth(text, dotSpacing, charSpacing) + getPaddingLeft() + getPaddingRight();
        float contentH = NdotHelper.measureHeight(dotSpacing) + getPaddingTop() + getPaddingBottom();

        int w = resolveSize((int) Math.ceil(contentW), widthMeasureSpec);
        int h = resolveSize((int) Math.ceil(contentH), heightMeasureSpec);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (text == null || text.isEmpty()) return;

        float startX = getPaddingLeft();
        if (centerAlign) {
            float textW = NdotHelper.measureWidth(text, dotSpacing, charSpacing);
            startX = (getWidth() - textW) / 2f;
        }
        float startY = getPaddingTop();

        NdotHelper.drawText(canvas, text, startX, startY, dotRadius, dotSpacing, charSpacing, paint);
    }
}
