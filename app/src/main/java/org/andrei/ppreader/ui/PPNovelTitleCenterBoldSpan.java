package org.andrei.ppreader.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.style.ReplacementSpan;

public class PPNovelTitleCenterBoldSpan extends ReplacementSpan {

    public PPNovelTitleCenterBoldSpan(float fontSize,float padding){
        m_fontSize = fontSize;
        m_padding = padding;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence charSequence, int i, int i1, @Nullable Paint.FontMetricsInt fontMetricsInt) {
        Paint src = getTitlePaint(paint);
        String text = charSequence.subSequence(i, i1).toString();
        return (int)src.measureText(text);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence charSequence, int i, int i1, float v, int i2, int i3, int i4, @NonNull Paint paint) {
        Paint src = getTitlePaint(paint);
        String text = charSequence.subSequence(i, i1).toString();
        String newText = autoChangeTextByCanvasWidth(text,src,canvas);
        int len = (int)src.measureText(newText);
        int w = canvas.getWidth();
        int x = (w - len)/2 - (int)m_padding + 10 ;
        canvas.drawText(newText,x,i3,src);
    }

    private Paint getTitlePaint(Paint src){
        Paint paint = new Paint(src);
        paint.setTextSize(m_fontSize);
        paint.setFakeBoldText(true);
        return paint;
    }

    private String autoChangeTextByCanvasWidth(final String text, Paint paint, Canvas canvas){
        String tx = text.replaceAll("\n", "");
        float len = paint.measureText(tx);
        float tailLen = paint.measureText("…");
        float w = canvas.getWidth() - 20 - m_padding;
        if(len <= w){
            return tx;
        }
        StringBuilder sbNewText = new StringBuilder();
        float txLen = tailLen;
        for (int i = 0; i != tx.length(); ++i){
            char ch = tx.charAt(i);
            txLen += paint.measureText(String.valueOf(ch));
            if(txLen <= w ){
                sbNewText.append(ch);
            }
            else{
                break;
            }
        }
        sbNewText.append("…");
        return sbNewText.toString();
    }

    private float m_fontSize;
    private float m_padding;
}
