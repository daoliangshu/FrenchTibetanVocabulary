package com.librefra.daoliangshu.tibetanvocabulary;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Created by daoliangshu on 2/3/17.
 */

public class SlidePageFragment extends Fragment {
    private String txt;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_slide_info_page, container, false);


        // First set the text to display in the adapter (that is before the call of onCreateView),
        // The text is then displayed here
        Spanned sp = Html.fromHtml(txt);
        WebView webView = (WebView) rootView.findViewById(R.id.fragment_page);
        webView.setWebViewClient(new MyWebViewClient(getContext()));
        // ((VocabularyActivity)getActivity()).getDb()));
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void performClick(String textToPonounce) {
                ((VocabularyActivity) getActivity()).pronounceLetter(textToPonounce);
            }
        }, "pron");
        webView.loadDataWithBaseURL(null, txt, "text/html", "utf-8", null);
        return rootView;
    }

    public void setText(String text) {
        this.txt = text;
    }


}

