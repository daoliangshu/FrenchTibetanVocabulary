package com.librefra.daoliangshu.tibetanvocabulary;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by daoliangshu on 2/4/17.
 * Tool class providing static methods.
 */

public class StaticUtils {

    public static final int PAGE_INFO = 0;
    public static final int PAGE_MAIN = 1;

    public static final int MODE_MANUAL = 0;
    public static final int MODE_3_SEC = 1;
    public static final int MODE_5_SEC = 2;
    public static final int MODE_10_SEC = 3;
    public static final int MODE_15_SEC = 4;
    public static final int MODE_30_SEC = 5;



    public static String getHtmlAsString(Context context, int pageIndex) {
        String pages[] = {
                "alphabet",
                "prefix_simple",
                "souscrites",
                "suscrites",
                "prefix_composed",
                "empilees",
                "suffixes1",
                "epellation"};
        if (DataManager.getHtmlFile(pageIndex, Settings.isNightMode) == null) {
            try {
                FileInputStream is = new FileInputStream(new File(
                        context.getFilesDir().toString() + "/phon_info/" + pages[pageIndex] + ".html"));
                BufferedReader in =
                        new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String text = "";
                String temp;
                while ((temp = in.readLine()) != null) {
                    Log.i("Line", temp);
                    text += new String(temp.getBytes(), "UTF-8");
                }
                in.close();
                //insert method to retrieve sound on button
                text = text.replace("%pron;", "class=\"pron\" type=\"button\"  onclick=\"pron.performClick(this.value);\"");
                text = text.replace("%em;", "<span class=\"big1\">");
                text = text.replace("%/em;", "</span>");
                text = text.replace("%em2;", "<div class=\"myh1\">");
                text = text.replace("%/em2;", "</div>");
                text = text.replace("%s;", "<span class=\"small\">");
                text = text.replace("%/s;", "</span>");
                if (text.contains("%common_style;")) {
                    String style;
                    if (Settings.isNightMode) style = getStyle(context, "common_style_dark");
                    else style = getStyle(context, "common_style");
                    if (style != null) {
                        return text.replace("%common_style;", style);
                    }
                }
                DataManager.putHtmlFile(text, pageIndex, Settings.isNightMode);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return DataManager.getHtmlFile(pageIndex, Settings.isNightMode);

    }

    public static String getStyle(Context context, String styleFileName) {
        try {
            FileInputStream is = new FileInputStream(new File(
                    context.getFilesDir().toString() + "/phon_info/" + styleFileName + ".css"));
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String text = "";
            String temp;
            while ((temp = in.readLine()) != null) {
                Log.i("Line", temp);
                text += new String(temp.getBytes(), "UTF-8");
            }
            in.close();
            return text;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getColor(Context context, int resId) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.
                    getColor(context, resId);
        } else {
            return context.getResources().getColor(resId);
        }
    }
}
