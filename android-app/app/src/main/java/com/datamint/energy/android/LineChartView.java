package com.datamint.energy.android;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/** Line + area chart with a dashed target (average) line and x-axis labels. */
public class LineChartView extends View {
    private float[] values = new float[0]; private String[] labels = new String[0];
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG), fill = new Paint(Paint.ANTI_ALIAS_FLAG),
            avg = new Paint(Paint.ANTI_ALIAS_FLAG), dot = new Paint(Paint.ANTI_ALIAS_FLAG), txt = new Paint(Paint.ANTI_ALIAS_FLAG);

    public LineChartView(Context c) { super(c); init(); }
    public LineChartView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        line.setColor(0xFF2F7BFF); line.setStyle(Paint.Style.STROKE); line.setStrokeWidth(6f);
        fill.setColor(0x442F7BFF); fill.setStyle(Paint.Style.FILL);
        avg.setColor(0xFF22D36B); avg.setStyle(Paint.Style.STROKE); avg.setStrokeWidth(3f); avg.setPathEffect(new DashPathEffect(new float[]{18, 12}, 0));
        dot.setColor(0xFF2F7BFF);
        txt.setColor(0xFF8FA3CC); txt.setTextAlign(Paint.Align.CENTER); txt.setTextSize(11 * getResources().getDisplayMetrics().scaledDensity);
    }

    public void setValues(float[] v, String[] l) { values = v; labels = l; invalidate(); }

    @Override protected void onDraw(Canvas c) {
        if (values.length < 2) return;
        float w = getWidth(), h = getHeight(), pad = 16f, bottom = txt.getTextSize() + 20f, max = 0, sum = 0;
        for (float v : values) { max = Math.max(max, v); sum += v; }
        max *= 1.1f;
        float ph = h - bottom - pad;
        Path p = new Path(), area = new Path();
        for (int i = 0; i < values.length; i++) {
            float x = pad + i * (w - 2 * pad) / (values.length - 1), y = pad + ph - values[i] / max * ph;
            if (i == 0) { p.moveTo(x, y); area.moveTo(x, pad + ph); area.lineTo(x, y); } else { p.lineTo(x, y); area.lineTo(x, y); }
        }
        area.lineTo(w - pad, pad + ph); area.close();
        c.drawPath(area, fill); c.drawPath(p, line);
        float ay = pad + ph - (sum / values.length) / max * ph;
        c.drawLine(pad, ay, w - pad, ay, avg);
        int step = Math.max(1, (int) Math.ceil(values.length / 5.0));
        for (int i = 0; i < values.length; i++) {
            float x = pad + i * (w - 2 * pad) / (values.length - 1);
            c.drawCircle(x, pad + ph - values[i] / max * ph, 6f, dot);
            if (i < labels.length && i % step == 0) c.drawText(labels[i], x, h - 6f, txt);
        }
    }
}
