package com.deviceinfo.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class InfoCard extends FrameLayout {
    
    private TextView labelView;
    private TextView valueView;
    
    public InfoCard(Context context) {
        super(context);
        init(context);
    }
    
    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_info_card, this, true);
        labelView = findViewById(R.id.infoLabel);
        valueView = findViewById(R.id.infoValue);
    }
    
    public void setLabel(String label) {
        labelView.setText(label);
    }
    
    public void setValue(String value) {
        valueView.setText(value);
    }
}
