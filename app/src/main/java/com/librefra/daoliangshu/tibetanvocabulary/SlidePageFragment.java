package com.librefra.daoliangshu.tibetanvocabulary;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Created by daoliangshu on 2/3/17.
 */

public class SlidePageFragment extends Fragment {
    private String txt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_slide_info_page, container, false);

        Spanned sp = Html.fromHtml(txt);
        ((WebView) rootView.findViewById(R.id.fragment_page)).
                loadDataWithBaseURL(null, txt, "text/html", "utf-8", null);
        return rootView;
    }

    public void setText(String text) {
        this.txt = text;
    }


}

