package com.deviceinfo.dashboard;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private DeviceInfoProvider info;
    private LinearLayout container;
    private TextView lastUpdatedView;
    private TextView pullIndicator;
    private Handler handler = new Handler(Looper.getMainLooper());
    private float touchStartY = 0;
    private long lastRefreshTime = 0;
    private static final int SWIPE_THRESHOLD = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        info = new DeviceInfoProvider(this);
        container = findViewById(R.id.infoContainer);
        lastUpdatedView = findViewById(R.id.lastUpdated);
        pullIndicator = findViewById(R.id.pullIndicator);

        // Hide pull indicator when scrolling normally
        ScrollView sv = findViewById(R.id.scrollView);
        sv.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > 10 && pullIndicator.getVisibility() == View.VISIBLE) {
                    pullIndicator.setVisibility(View.GONE);
                }
            }
        });

        buildDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshInfo();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshInfo();
                handler.postDelayed(this, 30000);
            }
        }, 30000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE: {
                ScrollView sv = findViewById(R.id.scrollView);
                if (sv != null && sv.getScrollY() <= 5) {
                    float dy = event.getY() - touchStartY;
                    if (dy > 30) {
                        // Show pull indicator during the gesture
                        pullIndicator.setVisibility(View.VISIBLE);
                        if (dy > SWIPE_THRESHOLD) {
                            pullIndicator.setText("\u2191 Release to refresh");
                            pullIndicator.setTextColor(Color.parseColor("#90CAF9"));
                            long now = System.currentTimeMillis();
                            if (now - lastRefreshTime > 2000) {
                                lastRefreshTime = now;
                                // Show refreshing feedback
                                pullIndicator.setText("\u21bb Refreshing\u2026");
                                refreshInfo();
                                return true;
                            }
                        } else {
                            int pct = (int)((dy / SWIPE_THRESHOLD) * 100);
                            pullIndicator.setText("\u2193 Pull to refresh " + pct + "%");
                            pullIndicator.setTextColor(Color.parseColor("#60FFFFFF"));
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // Hide indicator after a short delay
                pullIndicator.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullIndicator.setVisibility(View.GONE);
                    }
                }, 1200);
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private void refreshInfo() {
        buildDashboard();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        lastUpdatedView.setText("Last updated: " + sdf.format(new Date()));
        
        // Show visual feedback in the pull indicator area
        pullIndicator.setVisibility(View.VISIBLE);
        pullIndicator.setText("\u2713 Refreshed");
        pullIndicator.setTextColor(Color.parseColor("#4CAF50"));
        pullIndicator.postDelayed(new Runnable() {
            @Override
            public void run() {
                pullIndicator.setVisibility(View.GONE);
            }
        }, 1500);
    }

    private void buildDashboard() {
        container.removeAllViews();

        addSectionHeader("Device");
        addInfoCard("Model", info.getDeviceModel());
        addInfoCard("Manufacturer", info.getManufacturer());
        addInfoCard("Device Name", info.getDeviceName());
        addInfoCard("Android Version", info.getAndroidVersion());
        addInfoCard("Build", info.getBuildNumber());
        addInfoCard("Security Patch", info.getSecurityPatch());
        addInfoCard("Bootloader", info.getBootloader());

        addSectionHeader("Performance");
        addInfoCard("CPU", info.getCpuInfo());
        addInfoCard("CPU Cores", info.getCpuCores());
        addInfoCard("RAM", info.getRamInfo());
        addInfoCard("RAM Usage", info.getRamPercent());

        addSectionHeader("Storage");
        addInfoCard("Internal Storage", info.getStorageInfo());
        addInfoCard("Storage Used", info.getStoragePercent());

        addSectionHeader("Battery");
        addInfoCard("Level", info.getBatteryLevel());
        addInfoCard("Health", info.getBatteryHealth());
        addInfoCard("Temperature", info.getBatteryTemperature());
        addInfoCard("Status", info.getChargingStatus());

        addSectionHeader("Display");
        addInfoCard("Resolution", info.getScreenResolution());
        addInfoCard("Density", info.getScreenDensity());
        addInfoCard("Size", info.getScreenSize() + " inches");

        addSectionHeader("Network");
        addInfoCard("Type", info.getNetworkType());
        addInfoCard("Status", info.getConnectionStatus());
        addInfoCard("Local IP", info.getLocalIpAddress());
        addInfoCard("Public IP", info.getPublicIpAddress());

        addSectionHeader("System");
        addInfoCard("Sensors", info.getSensorCount());
        addInfoCard("Uptime", info.getUptime());

        // Footer
        TextView note = new TextView(this);
        note.setText("\u2195 Swipe down or wait 30s to refresh");
        note.setTextSize(11);
        note.setTextColor(Color.parseColor("#40FFFFFF"));
        note.setGravity(android.view.Gravity.CENTER);
        note.setPadding(16, 24, 16, 32);
        container.addView(note);
    }

    private void addSectionHeader(String title) {
        TextView header = new TextView(this);
        header.setText(title);
        header.setTextSize(13);
        header.setTextColor(Color.parseColor("#90CAF9"));
        header.setPadding(24, 24, 24, 8);
        container.addView(header);
    }

    private void addInfoCard(String label, String value) {
        if (value == null || value.isEmpty()) value = "N/A";
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 14, 20, 14);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.parseColor("#1E1E1E"));
        bg.setCornerRadius(12);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 6, 16, 6);
        card.setLayoutParams(params);
        card.setBackground(bg);

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(12);
        labelView.setTextColor(Color.parseColor("#80FFFFFF"));
        card.addView(labelView);

        for (String line : value.split("\n")) {
            if (line.trim().isEmpty()) continue;
            TextView valView = new TextView(this);
            valView.setText(line);
            valView.setTextSize(15);
            valView.setTextColor(Color.WHITE);
            valView.setPadding(0, 2, 0, 0);
            card.addView(valView);
        }

        container.addView(card);
    }
}
