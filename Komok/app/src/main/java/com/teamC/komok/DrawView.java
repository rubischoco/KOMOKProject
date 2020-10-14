package com.teamC.komok;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawView extends View {

    public DrawView(Context context) {
        super(context);

        init(null);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(attrs);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(attrs);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(attrs);
    }

    private void init(@Nullable AttributeSet set) {

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);


    }
}
