package com.example.mymusicplayer.bean;

import android.content.Context;
import android.util.AttributeSet;
public class MarqueTextView extends androidx.appcompat.widget.AppCompatTextView {
    public MarqueTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public MarqueTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MarqueTextView(Context context) {
        super(context);
    }
    @Override
    public boolean isFocused() {

        return true;
    }
}