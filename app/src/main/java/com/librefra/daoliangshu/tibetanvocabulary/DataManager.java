package com.librefra.daoliangshu.tibetanvocabulary;

import android.content.res.AssetManager;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daoliangshu on 2/7/17.
 */

public class DataManager {

    private static class SparseHtml {
        SparseArray<String> text;

        public SparseHtml() {
            text = new SparseArray<>();
        }

        public void put(int index, String value) {
            text.put(index, value);
        }

        public String get(int index) {
            return text.get(index, null);
        }
    }

    public static String getHtmlFile(int index, boolean isNightMode) {
        if (infoHtmlPreload == null) {
            infoHtmlPreload = new SparseHtml[2];
            infoHtmlPreload[0] = new SparseHtml();
            infoHtmlPreload[1] = new SparseHtml();
        }
        return infoHtmlPreload[isNightMode ? 1 : 0].get(index);
    }

    public static void putHtmlFile(String htmlText, int index, boolean isNightMode) {
        if (infoHtmlPreload == null) {
            infoHtmlPreload = new SparseHtml[2];
            infoHtmlPreload[0] = new SparseHtml();
            infoHtmlPreload[1] = new SparseHtml();
        }
        infoHtmlPreload[isNightMode ? 1 : 0].put(index, htmlText);
    }

    public static SparseHtml infoHtmlPreload[];
    public static SparseArray<InfoSlidePageFragment> infoFragments;

    public static boolean copyAssetFolder(AssetManager assetManager,
                                          String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            for (int i = 0; i < files.length; i++) {
                Log.i("file in assets : ", files[i]);
            }
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains(".")) {
                    Log.i("creating_file: ", toPath + "/" + file);
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                } else {
                    Log.i("creating_folder: ", toPath + "/" + file);
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public static boolean loadSettings(String path) {
        File settingFile = new File(path);
        if (settingFile.exists()) {
            try {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                new FileInputStream(settingFile)));
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.contains("//")) {
                        String res[] = line.split("//");
                        assignSetting(res[0].trim(), res[1].trim());
                    }
                }
                in.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return false;
            }

        }
        return true;
    }

    public static boolean saveSettings(String path) {
        File settingFile = new File(path);
        try {
            if (settingFile.exists() || settingFile.createNewFile()) {

                BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(settingFile)));
                HashMap<String, String> saveBundle = new HashMap<>();
                saveBundle.put("hidePron", Settings.hidePron ? "1" : "0");
                saveBundle.put("hideWord", Settings.hideWord ? "1" : "0");
                saveBundle.put("hideTrans", Settings.hideTrans ? "1" : "0");
                saveBundle.put("autoSpeak", Settings.isAutoSpeak ? "1" : "0");
                saveBundle.put("nightMode", Settings.isNightMode ? "1" : "0");
                for (Map.Entry<String, String> entry : saveBundle.entrySet()) {
                    String line = entry.getKey() + " // " + entry.getValue();
                    out.write(line);
                    out.newLine();
                }
                out.flush();
                out.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
        return true;
    }


    private static void assignSetting(String key, String value) {
        if (key.equals("hidePron")) {
            if (Integer.valueOf(value) == 0) Settings.hidePron = false;
            else Settings.hidePron = true;
        } else if (key.equals("hideWord")) {
            if (Integer.valueOf(value) == 0) Settings.hideWord = false;
            else Settings.hideWord = true;
        } else if (key.equals("hideTrans")) {
            if (Integer.valueOf(value) == 0) Settings.hideTrans = false;
            else Settings.hideTrans = true;
        } else if (key.equals("autoSpeak")) {
            if (Integer.valueOf(value) == 0) Settings.isAutoSpeak = false;
            else Settings.isAutoSpeak = true;
        } else if (key.equals("nightMode")) {
            if (Integer.valueOf(value) == 0) Settings.isNightMode = false;
            else Settings.isNightMode = true;
        }
    }

}
