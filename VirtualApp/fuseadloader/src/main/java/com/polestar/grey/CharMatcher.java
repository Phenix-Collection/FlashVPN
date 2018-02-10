package com.polestar.grey;

/**
 * Created by guojia on 2018/2/10.
 */

class CharMatcher {
    private final String str;
    public CharMatcher(String str)
    {
        if (null == str) this.str = ""; else
            this.str = str;
    }

    boolean isAnyOf(char c) {
        for (int i = 0; i < this.str.length(); i++) {
            if (c == this.str.charAt(i)) return true;
        }
        return false;
    }

    String trim(String src) {
        if ((null == src) || ("".equals(src))) return "";
        int s = 0;
        while ((s < src.length()) &&
                (isAnyOf(src.charAt(s)))) {
            s++;
        }

        int e = src.length() - 1;
        while ((e > 0) &&
                (isAnyOf(src.charAt(e)))) {
            e--;
        }

        if (s >= 0 && s <= e + 1 && e + 1 <= src.length()) {
            return src.substring(s, e + 1);
        } else {
            return "";
        }
    }

    String removeStart(String src) {
        if ((null == src) || ("".equals(src))) return "";
        int s = 0;
        while ((s < src.length()) &&
                (isAnyOf(src.charAt(s)))) {
            s++;
        }

        return src.substring(s);
    }

//    String removeEnd(String src) {
//        if ((null == src) || ("".equals(src))) return "";
//        int e = src.length() - 1;
//        while ((e > 0) &&
//                (isAnyOf(src.charAt(e)))) {
//            e--;
//        }
//
//        return src.substring(0, e + 1);
//    }
}
