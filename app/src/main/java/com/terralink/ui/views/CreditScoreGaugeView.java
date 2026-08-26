package com.terralink.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CreditScoreGaugeView extends View {
    public CreditScoreGaugeView(Context context) {
        super(context);
    }

    public CreditScoreGaugeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CreditScoreGaugeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScore(int score) {
        // Implementation for drawing the gauge
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Drawing logic
    }
}
