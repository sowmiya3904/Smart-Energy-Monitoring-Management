package com.datamint.energy.android;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/** Energy distribution donut. */
public class DonutView extends View {
    private float[] pct = new float[0]; private int[] cols = new int[0];
    private final Paint arc = new Paint(Paint.ANTI_ALIAS_FLAG), big = new Paint(Paint.ANTI_ALIAS_FLAG), small = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DonutView(Context c) { super(c); init(); }
    public DonutView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        arc.setStyle(Paint.Style.STROKE);
        big.setColor(0xFFE8EEFC); big.setTextAlign(Paint.Align.CENTER); big.setTypeface(Typeface.DEFAULT_BOLD);
        small.setColor(0xFF8FA3CC); small.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(float[] p, int[] c) { pct = p; cols = c; invalidate(); }

    @Override protected void onDraw(Canvas c) {
        float size = Math.min(getWidth(), getHeight()), stroke = size * 0.16f, pad = stroke / 2 + 4;
        RectF r = new RectF(pad, pad, size - pad, size - pad);
        arc.setStrokeWidth(stroke);
        float start = -90f;
        for (int i = 0; i < pct.length; i++) {
            float sweep = pct[i] / 100f * 360f;
            arc.setColor(cols[i]); c.drawArc(r, start, Math.max(0, sweep - 1.5f), false, arc); start += sweep;
        }
        big.setTextSize(size * 0.15f); small.setTextSize(size * 0.08f);
        c.drawText("100%", size / 2, size / 2 + size * 0.03f, big);
        c.drawText("Total Usage", size / 2, size / 2 + size * 0.13f, small);
    }
}
