package com.terralink.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.terralink.R;

public class CreditScoreGaugeView extends View {
    private int score = 0;
    private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF arcRect = new RectF();

    public CreditScoreGaugeView(Context context) {
        super(context);
        init();
    }

    public CreditScoreGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CreditScoreGaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(30f);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        needlePaint.setStyle(Paint.Style.FILL);
        needlePaint.setColor(Color.DKGRAY);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(40f);
        textPaint.setFakeBoldText(true);
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.navy_text_primary));
    }

    public void setScore(int score) {
        this.score = Math.max(0, Math.min(100, score));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2 - 40;
        
        arcRect.set(width / 2 - radius, height / 2 - radius, width / 2 + radius, height / 2 + radius);

        // Draw background arc
        arcPaint.setColor(Color.LTGRAY);
        canvas.drawArc(arcRect, 135, 270, false, arcPaint);

        // Draw progress arc
        int color;
        if (score >= 80) color = ContextCompat.getColor(getContext(), R.color.status_green);
        else if (score >= 60) color = ContextCompat.getColor(getContext(), R.color.status_blue);
        else if (score >= 40) color = ContextCompat.getColor(getContext(), R.color.status_amber);
        else color = ContextCompat.getColor(getContext(), R.color.status_red);
        
        arcPaint.setColor(color);
        float sweepAngle = (score / 100f) * 270;
        canvas.drawArc(arcRect, 135, sweepAngle, false, arcPaint);

        // Draw Score Text
        canvas.drawText(String.valueOf(score), width / 2, height / 2 + 15, textPaint);
        
        // Draw needle
        float angle = 135 + sweepAngle;
        float needleLength = radius - 20;
        float needleX = (float) (width / 2 + Math.cos(Math.toRadians(angle)) * needleLength);
        float needleY = (float) (height / 2 + Math.sin(Math.toRadians(angle)) * needleLength);
        
        canvas.drawCircle(width / 2, height / 2, 10f, needlePaint);
        canvas.drawLine(width / 2, height / 2, needleX, needleY, needlePaint);
    }
}
