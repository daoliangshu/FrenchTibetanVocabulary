package com.librefra.daoliangshu.tibetanvocabulary;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class VocabularyActivity extends AppCompatActivity {

    private MediaPlayer mp = null;

    private TextView mWordView;
    private TextView mTransView;
    private TextView mPhonView;
    private DBHelper dbHelper = null;
    private ListView listView;
    private int currentLesson = 1;
    TextView mPhonInfoView;
    private View mControlsView;
    private boolean mVisible;
    private ArrayList<HashMap<String, String>> vocList;
    private int curVocIndex = -1;


    //pager
    private static final int NUM_PAGES = 3;
    private ViewPager mPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_vocabulary);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        View mContent = findViewById(R.id.content_view);
        mWordView = (TextView) findViewById(R.id.word_content);
        mTransView = (TextView) findViewById(R.id.trans_content);
        mPhonView = (TextView) findViewById(R.id.phonetic_content);
        String ava = "";
        for (Locale a : Locale.getAvailableLocales()) {
            ava += " " + a.toString();
        }
        Log.i("LOCAL_AVAILABLE: ", ava);

        // Set up the user interaction to manually show or hide the system UI.
        mContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        dbHelper = null;
        try {
            dbHelper = new DBHelper(getApplicationContext());
            vocList = dbHelper.getTransByStartWith("", 1, 1);
            if (vocList != null && vocList.size() > 0) curVocIndex = 0;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        mp = new MediaPlayer();
        Button mSpeakButton = (Button) findViewById(R.id.speack_button);
        mSpeakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dbHelper == null || curVocIndex < 0) return;
                byte[] ba = dbHelper.getAudioBytes(
                        Integer.parseInt(vocList.get(curVocIndex).
                                get(DBHelper.COL_ID)));
                try {
                    File file = new File(getFilesDir() +
                            "/temp_file.wav");


                    FileOutputStream fos = openFileOutput("temp_file.wav", Context.MODE_PRIVATE);
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
                    Log.i("Path: ", getFilesDir().getAbsolutePath());
                    mp.setDataSource(getFilesDir().getAbsolutePath() + "/temp_file.wav");
                    mp.prepare();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });
        Button nextButton = (Button) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curVocIndex >= 0) {
                    curVocIndex = (curVocIndex + 1) % vocList.size();
                    mTransView.setText(vocList.get(curVocIndex).get(DBHelper.COL_TRANS));
                    mWordView.setText(vocList.get(curVocIndex).get(DBHelper.COL_WORD));
                    mPhonView.setText(vocList.get(curVocIndex).get(DBHelper.COL_PHON));
                }
            }
        });
        Button prevButton = (Button) findViewById(R.id.prev_button);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curVocIndex >= 0) {
                    curVocIndex = curVocIndex - 1;
                    if (curVocIndex < 0) curVocIndex = vocList.size() - 1;
                    mTransView.setText(vocList.get(curVocIndex).get(DBHelper.COL_TRANS));
                    mWordView.setText(vocList.get(curVocIndex).get(DBHelper.COL_WORD));
                    mPhonView.setText(vocList.get(curVocIndex).get(DBHelper.COL_PHON));
                }
            }
        });


        //List view
        listView = (ListView) findViewById(R.id.list);
        //Array to show in listView:
        String[] values = new String[]{
                "Leçon 0",
                "Leçon 1",
                "Leçon 2"
        };
        //new Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
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
                String itemValue = (String) listView.getItemAtPosition(itemPosition);
                //Load voc according to lesson selected
                int indexLesson = itemValue.lastIndexOf(" ");
                currentLesson = Integer.parseInt(itemValue.substring(indexLesson).trim());
                Log.i("LessonIndex: ", String.valueOf(currentLesson));
                setVocList(currentLesson);
            }
        });


        //Flipper
        final ViewFlipper flipper = (ViewFlipper) findViewById(R.id.view_flipper);
        Button buttonBack0 = (Button) findViewById(R.id.button_back0);
        Button buttonBack1 = (Button) findViewById(R.id.button_back1);
        buttonBack0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                flipper.setDisplayedChild(1);
            }
        });
        buttonBack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipper.setDisplayedChild(0);
            }
        });

        //View Pager
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter mPagerAdapter = new SlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());


    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    public DBHelper getDb() {
        return dbHelper;
    }

    private void setVocList(int lessonIndex) {
        try {
            curVocIndex = -1;
            if (dbHelper == null)
                dbHelper = new DBHelper(getApplicationContext());
            vocList = dbHelper.getTransByStartWith("", 1, lessonIndex);
            if (vocList != null && vocList.size() > 0) {
                curVocIndex = 0;
                mTransView.setText(vocList.get(curVocIndex).get(DBHelper.COL_TRANS));
                mWordView.setText(vocList.get(curVocIndex).get(DBHelper.COL_WORD));
                mPhonView.setText(vocList.get(curVocIndex).get(DBHelper.COL_PHON));
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    private void hide() {
        mControlsView.setVisibility(View.GONE);
        mVisible = false;
    }

    private void show() {
        mVisible = true;
        mControlsView.setVisibility(View.VISIBLE);
    }

    public void pronounceLetter(String phonOrLetter) {
        if (dbHelper != null) {
            byte[] audioBytes = dbHelper.getLetterAudioBytes(phonOrLetter);
            if (audioBytes != null) {
                StaticUtils.playSound(getApplicationContext(), audioBytes);
            }
        }
    }


    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }


    private class SlidePagerAdapter extends FragmentStatePagerAdapter {
        public SlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            SlidePageFragment res = new SlidePageFragment();
            String htmlAsText;

            if ((htmlAsText = StaticUtils.
                    getHtmlAsString(getApplicationContext(), position)) != null) {
                res.setText(htmlAsText);
            }
            return res;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}





