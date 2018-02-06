package com.everyonedj.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Siddharth on 10/8/2015.
 */

public class TextViewPlus extends TextView {

    private static final String TAG = "TextView";

    public TextViewPlus(Context context) {
        super(context);
    }

    public TextViewPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public TextViewPlus(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        //  TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.TextViewPlus);
        //String customFont = a.getString(R.styleable.TextViewPlus_customFont);
        //  setCustomFont(ctx);
        Typeface tf = TypeFaceProvider.getTypeFace(ctx, "FuturaLT-Book");
        setTypeface(tf);
        //  a.recycle();
    }

   /* public boolean setCustomFont(Context ctx) {
        Typeface tf = null;
        try {
            tf = Typeface.createFromAsset(ctx.getAssets(), "fonts/FuturaLT-Book.ttf");
        } catch (Exception e) {

            return false;
        }

        setTypeface(tf);
        return true;
    }*/



}
