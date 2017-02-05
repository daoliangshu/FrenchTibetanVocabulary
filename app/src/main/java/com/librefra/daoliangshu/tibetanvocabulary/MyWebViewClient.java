package com.librefra.daoliangshu.tibetanvocabulary;

import android.content.Context;
import android.webkit.WebViewClient;

/**
 * Created by daoliangshu on 2/4/17.
 */

public class MyWebViewClient extends WebViewClient {
    private final Context context;
    private DBHelper dbHelper = null;

    public MyWebViewClient(Context context) {
        this.context = context;
        // this.dbHelper = myDbHelper;
    }

}
