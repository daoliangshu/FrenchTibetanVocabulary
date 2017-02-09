package com.librefra.daoliangshu.tibetanvocabulary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by daoliangshu on 2/5/17.
 */

public class SettingSlidePageFragment extends Fragment {

    private VocabularyActivity parentActivity;
    private Spinner lessonSpinner;
    private Spinner modeSpinner;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_slide_settings, container, false);

        //List view
        parentActivity = (VocabularyActivity) getActivity();
        lessonSpinner = (Spinner) rootView.findViewById(R.id.lesson_list);
        //Array to show in lessonSpinner:
        final String[] values = getResources().getStringArray(R.array.lesson_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_dropdown_item_1line,
                android.R.id.text1, values) {
            @Override
            public boolean isEnabled(int position) {
                return super.isEnabled(position);
            }

            @Override
            public boolean areAllItemsEnabled() {
                return super.areAllItemsEnabled();
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    Context mContext = this.getContext();
                    LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.support_simple_spinner_dropdown_item, null);
                }

                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                tv.setText(values[position]);
                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.colorBlueLight));
                tv.setBackgroundResource(R.drawable.button);
                return v;
            }
        };
        lessonSpinner.setAdapter(adapter);
        //listener listview click
        lessonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Clicked item index

                //Clicked item value
                String itemValue = (String) lessonSpinner.getItemAtPosition(position);
                //Load voc according to lesson selected
                int indexLesson = itemValue.lastIndexOf(" ");
                Settings.curLesson = Integer.parseInt(itemValue.substring(indexLesson).trim());
                Log.i("LessonIndex: ", String.valueOf(Settings.curLesson));
                if (parentActivity != null)
                    parentActivity.setVocList(Settings.curLesson);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //mode
        modeSpinner = (Spinner) rootView.findViewById(R.id.mode_list);
        //Array to show in lessonSpinner:
        final String[] values2 = getResources().getStringArray(R.array.switch_mode_list);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_dropdown_item_1line,
                android.R.id.text1, values2) {
            @Override
            public boolean isEnabled(int position) {
                return super.isEnabled(position);
            }

            @Override
            public boolean areAllItemsEnabled() {
                return super.areAllItemsEnabled();
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    Context mContext = this.getContext();
                    LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.support_simple_spinner_dropdown_item, null);
                }

                TextView tv = (TextView) v.findViewById(android.R.id.text1);
                tv.setText(values2[position]);
                tv.setTextColor(ContextCompat.getColor(getContext(), R.color.colorBlueLight));
                tv.setBackgroundResource(R.drawable.button);
                return v;
            }
        };
        modeSpinner.setAdapter(adapter2);
        //listener listview click
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Clicked item index

                //Clicked item value
                String itemValue = (String) modeSpinner.getItemAtPosition(position);
                switch (position) {
                    case StaticUtils.MODE_MANUAL:
                        Settings.curInterval = 9000000;
                        break;
                    case StaticUtils.MODE_3_SEC:
                        Settings.curInterval = 3000;
                        break;
                    case StaticUtils.MODE_5_SEC:
                        Settings.curInterval = 5000;
                        break;
                    case StaticUtils.MODE_10_SEC:
                        Settings.curInterval = 10000;
                        break;
                    case StaticUtils.MODE_15_SEC:
                        Settings.curInterval = 15000;
                        break;
                    case StaticUtils.MODE_30_SEC:
                        Settings.curInterval = 30000;
                        break;

                }
                parentActivity.updateInterval();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        ((CheckBox) rootView.findViewById(R.id.auto_speak_check)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.isAutoSpeak = isChecked;
            }
        });

        loadSettings(rootView);

        final int hideCheckResId[] = {R.id.hide_word_check, R.id.hide_trans_check, R.id.hide_pron_check};
        for (int i = 0; i < hideCheckResId.length; i++) {
            CheckBox cb = (CheckBox) rootView.findViewById(hideCheckResId[i]);
            final int resId = hideCheckResId[i];
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    switch (resId) {
                        case R.id.hide_word_check:
                            Settings.hideWord = isChecked;
                            break;
                        case R.id.hide_trans_check:
                            Settings.hideTrans = isChecked;
                            break;
                        case R.id.hide_pron_check:
                            Settings.hidePron = isChecked;
                    }
                    parentActivity.updateHiddenState();
                }
            });
        }


        final Switch dayNightSwitch = (Switch) rootView.findViewById(R.id.switch_day_night);
        dayNightSwitch.setChecked(false);
        dayNightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.isNightMode = isChecked;
                if (isChecked) {
                    dayNightSwitch.setText(getString(R.string.night_active));
                } else {
                    dayNightSwitch.setText(getString(R.string.day_active));
                }
                if (parentActivity != null) parentActivity.updateNightDayMode();
                updateNightDayMode();
            }
        });

        ((Button) rootView.findViewById(R.id.save_config_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataManager.saveSettings(getActivity().getFilesDir().getAbsolutePath() +
                        "/settings.conf");
            }
        });


        return rootView;
    }

    public void loadSettings(ViewGroup rootView) {
        ((CheckBox) rootView.
                findViewById(R.id.hide_pron_check)).setChecked(Settings.hidePron);
        ((CheckBox) rootView.
                findViewById(R.id.hide_trans_check)).setChecked(Settings.hideTrans);

        ((CheckBox) rootView.
                findViewById(R.id.hide_word_check)).setChecked(Settings.hideWord);
        ((Switch) rootView.
                findViewById(R.id.switch_day_night)).setChecked(Settings.isNightMode);

    }

    public void updateNightDayMode() {
        int textColor = Settings.isNightMode ? R.color.colorTextDay1 : R.color.colorTextNight1;
        ((CheckBox) getActivity().findViewById(R.id.hide_pron_check)).
                setTextColor(StaticUtils.getColor(getContext(), textColor));
        ((CheckBox) getActivity().findViewById(R.id.hide_word_check)).
                setTextColor(StaticUtils.getColor(getContext(), textColor));
        ((CheckBox) getActivity().findViewById(R.id.hide_trans_check)).
                setTextColor(StaticUtils.getColor(getContext(), textColor));
        ((CheckBox) getActivity().findViewById(R.id.auto_speak_check)).
                setTextColor(StaticUtils.getColor(getContext(), textColor));
        ((Switch) getActivity().findViewById(R.id.switch_day_night)).
                setTextColor(StaticUtils.getColor(getContext(), textColor));
        ((TextView) getActivity().findViewById(R.id.label_lesson_list)).
                setTextColor(StaticUtils.getColor(getContext(), textColor));
        ((TextView) getActivity().findViewById(R.id.label_mode_list)).
                setTextColor(StaticUtils.getColor(getContext(), textColor));

        int styles[] = {R.drawable.button, R.drawable.button2};
        int colors[] = {R.color.colorBlueLight, R.color.dark_dark_blue};
        (getActivity().findViewById(R.id.save_config_button)).
                setBackgroundResource(Settings.isNightMode ? styles[0] : styles[1]);
        ((Button) getActivity().findViewById(R.id.save_config_button)).
                setTextColor(ContextCompat.getColor(getContext(),
                        Settings.isNightMode ? colors[0] : colors[1]));
    }

}
