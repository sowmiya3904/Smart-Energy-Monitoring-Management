package com.datamint.energy.android;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.*;

/** Small helpers so screens can be built in code with the same look as the web dashboard. */
public class Ui {
    public static final int BLUE = 0xFF2F7BFF, GREEN = 0xFF22D36B, ORANGE = 0xFFFF9F1C, PURPLE = 0xFF8B5CF6,
            RED = 0xFFFF4D4F, YELLOW = 0xFFFFC83D, TEXT = 0xFFE8EEFC, MUTED = 0xFF8FA3CC, BORDER = 0xFF17306A, CARD = 0xFF0A1A3A;

    public static int dp(Context c, float v) { return Math.round(v * c.getResources().getDisplayMetrics().density); }

    public static GradientDrawable round(Context c, int color, float radiusDp) {
        GradientDrawable g = new GradientDrawable(); g.setColor(color); g.setCornerRadius(dp(c, radiusDp)); return g;
    }

    public static TextView text(Context c, String s, float sp, int color, boolean bold) {
        TextView t = new TextView(c); t.setText(s); t.setTextSize(sp); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    public static LinearLayout card(Context c) {
        LinearLayout l = new LinearLayout(c); l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundResource(R.drawable.bg_card); l.setPadding(dp(c, 14), dp(c, 12), dp(c, 14), dp(c, 12));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.setMargins(dp(c, 4), dp(c, 6), dp(c, 4), dp(c, 6));
        l.setLayoutParams(lp); return l;
    }

    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); return l;
    }

    public static TextView tag(Context c, String s, int color) {
        TextView t = text(c, s, 11, color, true);
        t.setPadding(dp(c, 10), dp(c, 4), dp(c, 10), dp(c, 4));
        t.setBackground(round(c, (color & 0x00FFFFFF) | 0x33000000, 8)); return t;
    }

    public static Button button(Context c, String label, int color) {
        Button b = new Button(c); b.setText(label); b.setAllCaps(false); b.setTextSize(12); b.setTextColor(Color.WHITE);
        b.setBackground(round(c, color, 10)); b.setMinHeight(0); b.setMinimumHeight(0); b.setMinWidth(0); b.setMinimumWidth(0);
        b.setPadding(dp(c, 14), dp(c, 8), dp(c, 14), dp(c, 8));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2); lp.setMargins(dp(c, 3), dp(c, 4), dp(c, 3), dp(c, 4));
        b.setLayoutParams(lp); return b;
    }

    public static LinearLayout.LayoutParams weight(float w) { return new LinearLayout.LayoutParams(0, -2, w); }
}
