package com.librefra.daoliangshu.tibetanvocabulary;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Spinner;
import android.widget.ViewFlipper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class VocabularyActivity extends AppCompatActivity {

    private MediaPlayer mp = null;
    private MainSlidePageFragment flashCardFragment;
    private SettingSlidePageFragment settingsFragment;


    private DBHelper dbHelper = null;
    private Spinner listView;
    private int currentLesson = 1;
    private boolean mVisible;
    private ArrayList<HashMap<String, String>> vocList;
    private int curVocIndex = -1;


    private ViewFlipper flipper;

    private static final int NUM_MAIN_PAGES = 2;
    private ViewPager mPagerMain;

    //pager info
    private static final int NUM_INFO_PAGES = 8;
    private ViewPager mPagerInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_vocabulary);
        if (!PreferenceManager.getDefaultSharedPreferences(
                //Copy assets phonetic resources in data directory
                getApplicationContext())
                .getBoolean("installed", false)) {
            PreferenceManager.getDefaultSharedPreferences(
                    getApplicationContext())
                    .edit().putBoolean("installed", true).apply();
            DataManager.copyAssetFolder(getAssets(), "phon_info",
                    this.getFilesDir().getPath() + "/phon_info");
            Log.i("PATH__ : ", this.getFilesDir().getPath());
        }
        dbHelper = null;
        try {
            dbHelper = new DBHelper(getApplicationContext());
            vocList = dbHelper.getTransByStartWith("", 1, 1);
            if (vocList != null && vocList.size() > 0) curVocIndex = 0;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        //Load Settings
        DataManager.loadSettings(getFilesDir().getAbsolutePath() +
                "/settings.conf");

        mp = new MediaPlayer();

        //Main flipper
        flipper = (ViewFlipper) findViewById(R.id.view_flipper);

        //View Pagers
        //Flash card + Setting pager
        mPagerMain = (ViewPager) findViewById(R.id.pager_main);
        PagerAdapter mPagerAdapterMain = new SlidePagerAdapter(getSupportFragmentManager(),
                StaticUtils.PAGE_MAIN);
        mPagerMain.setAdapter(mPagerAdapterMain);
        mPagerMain.setPageTransformer(true, new ZoomOutPageTransformer());

        //Info Pager
        mPagerInfo = (ViewPager) findViewById(R.id.pager_info);
        PagerAdapter mPagerAdapter = new SlidePagerAdapter(getSupportFragmentManager(),
                StaticUtils.PAGE_INFO);


        mPagerInfo.setAdapter(mPagerAdapter);
        mPagerInfo.setPageTransformer(true, new ZoomOutPageTransformer());
        mPagerInfo.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            float sumPositionAndPositionOffset;
            float prevPositionOffset;
            int equalCumulate;

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.i("pos", String.format(Locale.ENGLISH,
                        "%d  --  %f --  %d  ", position, positionOffset, positionOffsetPixels));
                if (position + positionOffset <= sumPositionAndPositionOffset && equalCumulate >= 5) {
                    equalCumulate = 0;
                    if (mPagerInfo.getCurrentItem() == 0) {
                        flipper.setDisplayedChild(0); //Return to main view
                    }
                }
                if (mPagerInfo.getCurrentItem() == 0 && positionOffset == prevPositionOffset)
                    equalCumulate += 1;
                else {
                    equalCumulate = 0;
                }
                prevPositionOffset = positionOffset;
                sumPositionAndPositionOffset = position + positionOffset;

                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            public void onPageSelected(int position) {
                equalCumulate = 0;
                super.onPageSelected(position);
            }
        });
    }

    public void enterInfo() {
        ((SlidePagerAdapter) mPagerInfo.getAdapter()).updateFragment();
    }

    public void flipView(int childIndexToDisplay) {
        flipper.setDisplayedChild(childIndexToDisplay);
    }

    public void speak() {
        if (dbHelper == null || curVocIndex < 0) return;
        byte[] ba = dbHelper.getAudioBytes(
                Integer.parseInt(vocList.get(curVocIndex).
                        get(DBHelper.COL_ID)));
        playSound(ba);
    }

    public DBHelper getDb() {
        return dbHelper;
    }

    public void setVocList(int lessonIndex) {
        try {
            curVocIndex = -1;
            if (dbHelper == null)
                dbHelper = new DBHelper(getApplicationContext());
            vocList = dbHelper.getTransByStartWith("", 1, lessonIndex);
            if (vocList != null && vocList.size() > 0) {
                curVocIndex = 0;
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void nextWord() {
        if (curVocIndex >= 0) {
            curVocIndex = (curVocIndex + 1) % vocList.size();
            if (Settings.isAutoSpeak) speak();
        }
        updateInterval();


    }

    public void previousWord() {
        if (curVocIndex >= 0) {
            curVocIndex = curVocIndex - 1;
            if (curVocIndex < 0) curVocIndex = vocList.size() - 1;
            if (Settings.isAutoSpeak) speak();
        }
        updateInterval();
    }


    /**
     * Get the current value of the specifed columm. (name defined in DBHelper)
     * Choices: PHON, WORD, TRANS
     *
     * @param dbColumn: The column for which to ask value
     * @return Value of the column
     */
    public String getCurrent(String dbColumn) {
        if (vocList != null && curVocIndex >= 0 && curVocIndex < vocList.size()) {
            return vocList.get(curVocIndex).get(dbColumn);
        } else {
            return getString(R.string.no_retrieved_value);
        }
    }

    public void pronounceLetter(String phonOrLetter) {
        if (dbHelper != null) {
            byte[] audioBytes = dbHelper.getLetterAudioBytes(phonOrLetter);
            if (audioBytes != null) {

                playSound(audioBytes);
            }
        }
    }

    public void saveSettings() {
        DataManager.saveSettings(getFilesDir().getAbsolutePath() +
                "/settings.conf");
    }

    private void playSound(byte[] ba) {
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


    @Override
    public void onBackPressed() {
        if (flipper.getDisplayedChild() == 1) {
            flipper.setDisplayedChild(0);

        } else {
            if (mPagerMain.getCurrentItem() == 0) {
                // If the user is currently looking at the first step, allow the system to handle the
                // Back button. This calls finish() on this activity and pops the back stack.
                super.onBackPressed();
            } else {
                // Otherwise, select the previous step.
                mPagerMain.setCurrentItem(mPagerMain.getCurrentItem() - 1);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (flashCardFragment != null) {
            flashCardFragment.pauseInterval();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (flashCardFragment != null) {
            flashCardFragment.resumeInterval();
        }

    }


    private class SlidePagerAdapter extends FragmentStatePagerAdapter {
        int mode = 0;
        private HashMap<Integer, Fragment> infoFragments;
        private Fragment mCurrentFragment;

        public SlidePagerAdapter(FragmentManager fm, int mode) {
            super(fm);
            this.mode = mode;
        }

        @Override
        public Fragment getItem(int position) {
            switch (this.mode) {
                case StaticUtils.PAGE_INFO:
                    if (infoFragments == null) infoFragments = new HashMap<>();

                    //if(DataManager.infoFragments.get(position, null) != null){
                    //    return DataManager.infoFragments.get(position);
                    //}
                    InfoSlidePageFragment res = new InfoSlidePageFragment();
                    infoFragments.put(position, res);
                    //res.setText(getHtmlAsString(position));
                    res.setPosition(position);
                    res.setText(StaticUtils.getHtmlAsString(getApplicationContext(), position));
                    return res;
                case StaticUtils.PAGE_MAIN:
                    switch (position) {
                        case 1:
                            return new MainSlidePageFragment();
                        case 0:
                            return new SettingSlidePageFragment();
                        default:
                            return new SettingSlidePageFragment();
                    }
                default:
                    return new SettingSlidePageFragment();
            }
        }

        public void updateFragment() {
            if (infoFragments != null) {
                for (Map.Entry<Integer, Fragment> entry : infoFragments.entrySet()) {
                    ((InfoSlidePageFragment) entry.getValue()).reload();
                }
            }
        }

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (mode == StaticUtils.PAGE_INFO &&
                    getCurrentFragment() != object) {
                mCurrentFragment = ((Fragment) object);
                ((InfoSlidePageFragment) mCurrentFragment).reloadIfDisplayModeChanged();
            }
            super.setPrimaryItem(container, position, object);
        }

        private String getHtmlAsString(int position) {
            String htmlAsText;
            if ((htmlAsText = StaticUtils.
                    getHtmlAsString(getApplicationContext(), position)) != null) {
                return htmlAsText;
            }
            return getString(R.string.no_page_found);
        }

        @Override
        public int getCount() {
            if (this.mode == StaticUtils.PAGE_INFO) {
                return NUM_INFO_PAGES;
            }
            return NUM_MAIN_PAGES;
        }
    }

    /**
     * Ask the flashcard fragment to update the interval between words.
     * Need to be called after interval change, so that it can current schedulled interval.
     */
    public void updateInterval() {
        if (flashCardFragment != null) {
            flashCardFragment.updateInterval();
        }
    }

    public void setFlashCardFragment(MainSlidePageFragment fragment) {
        flashCardFragment = fragment;
    }

    public void setSettingsFragment(SettingSlidePageFragment fragment) {
        settingsFragment = fragment;

    }

    public void updateNightDayMode() {
        int colorResIndex;
        if (Settings.isNightMode) colorResIndex = R.color.dark_blue;
        else colorResIndex = R.color.gray_mid;
        mPagerInfo.setBackgroundColor(StaticUtils.getColor(getApplicationContext(), colorResIndex));
        mPagerMain.setBackgroundColor(StaticUtils.getColor(getApplicationContext(), colorResIndex));
        if (flashCardFragment != null) flashCardFragment.updateNightDayMode();
    }

    public void updateHiddenState() {
        if (flashCardFragment != null)
            flashCardFragment.updateHiddenState();
    }


}





