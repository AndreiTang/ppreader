package org.andrei.ppreader.ui;

import android.graphics.Paint;
import android.widget.TextView;

public class Utils {

    /**
     * ASCII表中可见字符从!开始，偏移位值为33(Decimal)
     */
    static final char DBC_CHAR_START = 33; // 半角!
    /**
     * ASCII表中可见字符到~结束，偏移位值为126(Decimal)
     */
    static final char DBC_CHAR_END = 126; // 半角~
    /**
     * 全角对应于ASCII表的可见字符从！开始，偏移值为65281
     */
    static final char SBC_CHAR_START = 65281; // 全角！
    /**
     * 全角对应于ASCII表的可见字符到～结束，偏移值为65374
     */
    static final char SBC_CHAR_END = 65374; // 全角～
    /**
     * ASCII表中除空格外的可见字符与对应的全角字符的相对偏移
     */
    static final int CONVERT_STEP = 65248; // 全角半角转换间隔
    /**
     * 全角空格的值，它没有遵从与ASCII的相对偏移，必须单独处理
     */
    static final char SBC_SPACE = 12288; // 全角空格 12288
    /**
     * 半角空格的值，在ASCII中为32(Decimal)
     */
    static final char DBC_SPACE = ' '; // 半角空格

    public static String half2full(String src) {
        if (src == null) {
            return src;
        }
        StringBuilder buf = new StringBuilder(src.length());
        char[] ca = src.toCharArray();
        for (int i = 0; i < ca.length; i++) {
            if (ca[i] == DBC_SPACE) { // 如果是半角空格，直接用全角空格替代
                buf.append(SBC_SPACE);
            } else if ((ca[i] >= DBC_CHAR_START) && (ca[i] <= DBC_CHAR_END)) { // 字符是!到~之间的可见字符
                buf.append((char) (ca[i] + CONVERT_STEP));
            } else { // 不对空格以及ascii表中其他可见字符之外的字符做任何处理
                buf.append(ca[i]);
            }
        }
        return buf.toString();
    }

    public static String autoSplitText(final TextView tv,final String rawText) {
        final Paint tvPaint = tv.getPaint(); //paint，包含字体等信息
        final float tvSpace = tvPaint.measureText(String.valueOf("，")) + 5;
        final float tvWidth = tv.getWidth() - tv.getPaddingLeft() - tv.getPaddingRight() - tvSpace; //控件可用宽度
        final float height = tv.getHeight();
        final String flags = "，。：”’！？·,.:'\"?!`";

        String [] rawTextLines = rawText.replaceAll("\r", "").split("\n");
        StringBuilder sbNewText = new StringBuilder();
        for (String rawTextLine : rawTextLines) {
            rawTextLine = rawTextLine.replaceAll("\\s*","");
            if(rawTextLine.length() > 0){
                rawTextLine = "        " + rawTextLine;
//                if(sbNewText.length() > 0){
//                    rawTextLine = "        " + rawTextLine;
//                }
//                else{
//                    //the title remain space.And there are two \n at the tail.
//                    rawTextLine = tmp;
//                    rawTextLine = rawTextLine.trim();
//                    rawTextLine+= '\r';
//                }
            }
            else{
                continue;
            }
            if (tvPaint.measureText(rawTextLine) <= tvWidth) {
                sbNewText.append(rawTextLine);
            } else {
                //如果整行宽度超过控件可用宽度，则按字符测量，在超过可用宽度的前一个字符处手动换行
                float lineWidth = 0;
                for (int cnt = 0; cnt != rawTextLine.length(); ++cnt) {
                    char ch = rawTextLine.charAt(cnt);
                    lineWidth += tvPaint.measureText(String.valueOf(ch));
                    if (lineWidth <= tvWidth) {
                        sbNewText.append(ch);
                    } else {
                        if(flags.indexOf(ch) !=-1 ){
                            sbNewText.append(ch);
                            if((cnt + 1)<  rawTextLine.length()&&rawTextLine.charAt(cnt+1) == '\n'){
                                ++cnt;
                            }
                        }
                        else{
                            --cnt;
                        }
                        sbNewText.append("\n");
                        lineWidth = 0;
                    }
                }
            }
            sbNewText.append("\n");
        }

        if (!rawText.endsWith("\n")) {
            sbNewText.deleteCharAt(sbNewText.length() - 1);
        }
        String strText = sbNewText.toString();
        strText = strText.replaceAll("\n\n","\n");
        return strText;
    }



}
