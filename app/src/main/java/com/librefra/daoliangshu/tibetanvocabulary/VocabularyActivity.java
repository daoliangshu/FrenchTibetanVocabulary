package com.librefra.daoliangshu.tibetanvocabulary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class VocabularyActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private MediaPlayer mp = null;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContent;
    private TextView mWordView;
    private TextView mTransView;
    private TextView mPhonView;
    private DBHelper dbHelper = null;
    private ListView listView;
    private int currentLesson = 1;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContent.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private ArrayList<HashMap<String, String>> vocList;
    private int curVocIndex = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vocabulary);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContent = findViewById(R.id.content_view);
        mWordView = (TextView)findViewById(R.id.word_content);
        mTransView = (TextView)findViewById(R.id.trans_content);
        mPhonView = (TextView)findViewById(R.id.phonetic_content);
        String ava = "";
        for(Locale a: Locale.getAvailableLocales()){
            ava += " "+ a.toString();
        }
        Log.i("LOCAL_AVAILABLE: ", ava);

        // Set up the user interaction to manually show or hide the system UI.
        mContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.next_button).setOnTouchListener(mDelayHideTouchListener);


        dbHelper = null;
        try{
            dbHelper = new DBHelper(getApplicationContext());
            vocList = dbHelper.getTransByStartWith("", 1, 1);
            if(vocList != null && vocList.size() > 0)curVocIndex = 0;
        } catch(SQLException sqle){
            sqle.printStackTrace();
        }
        mp = new MediaPlayer();
        Button mSpeakButton = (Button)findViewById(R.id.speack_button);
        mSpeakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbHelper == null || curVocIndex < 0)return;
                byte[] ba = dbHelper.getAudioBytes(
                                     Integer.parseInt(vocList.get(curVocIndex).
                                             get(DBHelper.COL_ID)));


                try {
                    File file =  new File(getFilesDir()+
                                            "/temp_file.wav");


                    FileOutputStream fos = openFileOutput("temp_file.wav", Context.MODE_PRIVATE);
                    //FileOutputStream fos = new FileOutputStream(file);
                    fos.write(ba, 0, ba.length);
                    fos.flush();
                    fos.close();
                    mp.reset();
                    mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        public void onPrepared(MediaPlayer mPlayer) {
                            Log.d("SMP","    Mediaplayer ready (preparation done). Starting reproduction");
                            mPlayer.start();
                            Log.d("SMP","    Mediaplayer ready (preparation done). Done!");
                        }
                    });

                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    //mp.setDataSource(fos.getFD());
                    Log.i("Path: ", getFilesDir().getAbsolutePath());
                    mp.setDataSource(getFilesDir().getAbsolutePath()+"/temp_file.wav");
                    mp.prepare();
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }
            }
        });
        Button nextButton = (Button)findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curVocIndex >= 0){
                    curVocIndex = (curVocIndex+1)%vocList.size();
                    mTransView.setText(vocList.get(curVocIndex).get(DBHelper.COL_TRANS));
                    mWordView.setText(vocList.get(curVocIndex).get(DBHelper.COL_WORD));
                    mPhonView.setText(vocList.get(curVocIndex).get(DBHelper.COL_PHON));
                }
            }
        });
        Button prevButton = (Button)findViewById(R.id.prev_button);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(curVocIndex >= 0){
                    curVocIndex = curVocIndex-1;
                    if(curVocIndex < 0)curVocIndex = vocList.size()-1;
                    mTransView.setText(vocList.get(curVocIndex).get(DBHelper.COL_TRANS));
                    mWordView.setText(vocList.get(curVocIndex).get(DBHelper.COL_WORD));
                    mPhonView.setText(vocList.get(curVocIndex).get(DBHelper.COL_PHON));
                }
            }
        });


        //List view
        listView = (ListView)findViewById(R.id.list);
        //Array to show in listView:
        String[] values = new String[]{
                "Leçon 0",
                "Leçon 1",
                "Leçon 2"
        };
        //new Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,
                android.R.id.text1, values);

        //Assign adapter to ListView
        listView.setAdapter(adapter);
        //listener listview click
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Clicked item index
                int itemPosition = position;
                //Clicked item value
                String itemValue = (String)listView.getItemAtPosition(itemPosition);
                //Load voc according to lesson selected
                int indexLesson = itemValue.lastIndexOf(" ");
                currentLesson = Integer.parseInt(itemValue.substring(indexLesson).trim());
                Log.i("LessonIndex: ", String.valueOf(currentLesson));
                setVocList(currentLesson);
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void setVocList(int lessonIndex){
        try{
            curVocIndex = -1;
            if(dbHelper == null)
                dbHelper = new DBHelper(getApplicationContext());
            vocList = dbHelper.getTransByStartWith("", 1, lessonIndex);
            if(vocList != null && vocList.size() > 0){
                curVocIndex = 0;
                mTransView.setText(vocList.get(curVocIndex).get(DBHelper.COL_TRANS));
                mWordView.setText(vocList.get(curVocIndex).get(DBHelper.COL_WORD));
                mPhonView.setText(vocList.get(curVocIndex).get(DBHelper.COL_PHON));
            }

        } catch(SQLException sqle){
            sqle.printStackTrace();
        }
    }

    private void hide() {
        // Hide UI first
        //ActionBar actionBar = getSupportActionBar();
        //if (actionBar != null) {
        //    actionBar.hide();
        //}
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        //mTransView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        //        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
