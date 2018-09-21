package org.andrei.ppreader.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.ReplacementSpan;

public class PPNovelLineSpan extends ReplacementSpan {
    public PPNovelLineSpan(float fontSize, float leftPadding, float rightPadding) {
        m_fontSize = fontSize;
        m_leftPadding = leftPadding;
        m_rightPadding = rightPadding;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
        return 0;
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence charSequence, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        Paint src = getTitlePaint(paint);
        float cw = canvas.getWidth() - m_leftPadding - m_rightPadding;
        String text = charSequence.subSequence(start, end).toString();
        float len = src.measureText(text);
        float space = (float)(cw - len)/(float)(text.length()-1);
        float s = 0;
        for(int i = 0; i < text.length(); i++){
            String c = text.substring(i,i+1);
            canvas.drawText(c,s,y,src);
            s += src.measureText(c);
            if(i!= text.length() - 1){
                s += space;
            }
        }
    }

    private Paint getTitlePaint(Paint src) {
        Paint paint = new Paint(src);
        paint.setTextSize(m_fontSize);
        return paint;
    }

    private float m_fontSize;
    private float m_leftPadding;
    private float m_rightPadding;
}