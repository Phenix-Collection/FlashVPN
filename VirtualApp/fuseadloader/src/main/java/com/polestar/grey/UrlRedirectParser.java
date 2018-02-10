package com.polestar.grey;

/**
 * Created by guojia on 2016/11/7.
 */

public class UrlRedirectParser {
    private static final CharMatcher charMatcher = new CharMatcher(" '\"");

    public static String checkMetaOrJsRedirect(String content)
    {
        if ((null != content) && (content.length() != 0)) {
            String redirect = checkMetaRedirect(content);
            if (null == redirect) {
                redirect = checkJsRedirect(content, "location.replace");
            }
            if (null == redirect) {
                redirect = checkJsRedirect(content, "window.location");
            }

            return redirect;
        }
        return null;
    }

    private static String checkMetaRedirect(String content)
    {
        int s = content.indexOf("<meta");
        int step = 0;
        if (-1 == s) s = content.indexOf("<META");
        if (-1 != s) {
            s = content.indexOf("URL=", s + 5);
            if (-1 == s) s = content.indexOf("url=");
            step++;
            if (-1 != s) {
                int e = content.indexOf('"', s + 4);
                if (-1 == e) {
                    e = content.indexOf('\'', s + 4);
                }
                step++;
                if (-1 != e) {
                    String url = content.substring(s + 4, e);
                    return xmlElementDecode(charMatcher.trim(url));
                }
            }
        }
        return null;
    }

    private static String checkJsRedirect(String content, String key) {
        int s = content.indexOf(key);
        if (-1 == s) s = content.indexOf(key.toUpperCase());
        if (-1 != s) {
            s += key.length();
            char quote = '\'';
            int s1 = content.indexOf('\'', s);
            int s2 = content.indexOf('"', s);
            if ((-1 != s1) && (-1 != s2)) {
                s = s1 < s2 ? s1 : s2;
                quote = s1 < s2 ? '\'' : '"';
            } else if (-1 == s2) {
                s = s1;
                quote = '\'';
            } else {
                s = s2;
                quote = '"';
            }
            if (-1 != s) {
                int e = content.indexOf(quote, s + 1);
                if (-1 != e) {
                    return charMatcher.trim(content.substring(s, e));
                }
            }
        }
        return null;
    }

    private static String xmlElementDecode(String elem)
    {
        if ((null != elem) && (elem.length() != 0)) {
            return elem.replaceAll("&amp;", "&");
        }
        return null;
    }
}

