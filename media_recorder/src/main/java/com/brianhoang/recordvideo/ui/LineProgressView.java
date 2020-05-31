package com.brianhoang.recordvideo.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.brianhoang.recordvideo.R;

public class LineProgressView extends View {

    private Paint paint;
    private float progress;

    public LineProgressView(Context context) {
        super(context);
        init();
    }

    public LineProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        paint.setStrokeWidth(getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.WHITE);
        canvas.drawLine(0, 0, getWidth(), 0, paint);

        paint.setColor(getResources().getColor(R.color.colorPrimaryDark));
        canvas.drawLine(0, 0, getWidth() * progress, 0, paint);

    }

}
