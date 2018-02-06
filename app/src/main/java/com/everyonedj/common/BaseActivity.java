package com.everyonedj.common;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.everyonedj.R;

/**
 * Created by Siddharth on 7/9/2015.
 */
public class BaseActivity extends ActionBarActivity{

    private Toolbar mToolbar;

    protected Toolbar activateToolbar() {
        if(mToolbar == null) {
            mToolbar = (Toolbar) findViewById(R.id.app_bar);
            if(mToolbar != null) {
                setSupportActionBar(mToolbar);
            }
        }
        return mToolbar;
    }

}
