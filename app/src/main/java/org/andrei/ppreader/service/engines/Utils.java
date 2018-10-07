package org.andrei.ppreader.service.engines;

public class Utils {
    public static String adjustText(final String text){
        StringBuilder space = new StringBuilder();
        char c = 12288;//utf8 space
        space.append(c);
        String txt = text.replaceAll("&nbsp;","");
        txt = txt.replaceAll("<br>","");
        txt = txt.replaceAll(" ","");
        txt = txt.replaceAll(space.toString(),"");
        String arr[] = txt.split("\n");
        StringBuilder builder = new StringBuilder();

        String regex = "^第[0-9一二三四五六七八九十百千]+章.*";
        for(String str: arr){
            if(str.length() == 0 || str.matches(regex)){
                continue;
            }
            builder.append(str);
            builder.append("\n");
        }
        builder.deleteCharAt(builder.length()-1);
        return builder.toString();
    }
}
