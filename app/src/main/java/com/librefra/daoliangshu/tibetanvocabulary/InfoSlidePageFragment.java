package com.librefra.daoliangshu.tibetanvocabulary;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by daoliangshu on 2/3/17.
 */

public class InfoSlidePageFragment extends Fragment {
    private String txt;
    private int position;
    private ViewGroup rootView;
    private boolean isNightMode = Settings.isNightMode;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView != null) return rootView;
        rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_slide_info_page, container, false);

        assignText(rootView);
        Log.i("fragmentInfo", "Created " + this.toString());
        // First set the text to display in the adapter (that is before the call of onCreateView),
        // The text is then displayed here
        if (txt == null) txt = getString(R.string.no_retrieved_value);
        return rootView;
    }

    public void reload() {
        if (getContext() != null) {
            setText(StaticUtils.getHtmlAsString(getContext(), position));
            if (rootView != null) assignText(rootView);
        }
    }

    private void assignText(ViewGroup rootView) {
        if (txt == null || txt.equals("")) {
            txt = "";
        }
        WebView webView = (WebView) rootView.findViewById(R.id.fragment_page);
        webView.setWebViewClient(new MyWebViewClient(getContext()));

        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void performClick(String textToPonounce) {
                ((VocabularyActivity) getActivity()).pronounceLetter(textToPonounce);
            }
        }, "pron");
        webView.loadDataWithBaseURL(null, txt, "text/html", "utf-8", null);

    }

    public void setText(String text) {
        this.txt = text;
    }

    public void setPosition(int pos) {
        this.position = pos;
    }

    public void reloadIfDisplayModeChanged() {
        if (isNightMode != Settings.isNightMode) {
            txt = StaticUtils.getHtmlAsString(getActivity().getApplicationContext(), position);
            assignText(rootView);
            isNightMode = Settings.isNightMode;
        }
    }


}

