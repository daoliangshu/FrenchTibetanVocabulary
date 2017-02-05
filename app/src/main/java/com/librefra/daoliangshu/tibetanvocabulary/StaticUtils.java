package com.librefra.daoliangshu.tibetanvocabulary;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by daoliangshu on 2/4/17.
 * Tool class providing static methods.
 */

public class StaticUtils {
    public static String getHtmlAsString(Context context, int pageIndex) {
        String pages[] = {"prefix_simple",
                "souscrites",
                "prefix_composed"};

        try {
            InputStream is = context.getAssets().open("phon_info/" + pages[pageIndex]);
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

    public static void playSound(Context context, byte[] ba) {
        MediaPlayer mp = new MediaPlayer();
        try {
            File file = new File(context.getFilesDir() +
                    "/temp_file.wav");


            FileOutputStream fos = context.openFileOutput("temp_file.wav", Context.MODE_PRIVATE);
            //FileOutputStream fos = new FileOutputStream(file);
            fos.write(ba, 0, ba.length);
            fos.flush();
            fos.close();
            mp.reset();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mPlayer) {
                    Log.d("SMP", "    Mediaplayer ready (preparation done). Starting reproduction");
                    mPlayer.start();
                    Log.d("SMP", "    Mediaplayer ready (preparation done). Done!");

                }

            });

            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //mp.setDataSource(fos.getFD());
            Log.i("Path: ", context.getFilesDir().getAbsolutePath());
            mp.setDataSource(context.getFilesDir().getAbsolutePath() + "/temp_file.wav");
            mp.prepare();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
