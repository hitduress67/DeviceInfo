package com.deviceinfo.dashboard;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        info = new DeviceInfoProvider(this);
        container = findViewById(R.id.infoContainer);
        lastUpdatedView = findViewById(R.id.lastUpdated);
        
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

    private void refreshInfo() {
        buildDashboard();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        lastUpdatedView.setText("Last updated: " + sdf.format(new Date()));
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
        addInfoCard("Status", info.getIpAddress());
        
        addSectionHeader("System");
        addInfoCard("Sensors", info.getSensorCount());
        addInfoCard("Uptime", info.getUptime());
    }

    private void addSectionHeader(String title) {
        TextView header = new TextView(this);
        header.setText(title);
        header.setTextSize(14);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setTextColor(Color.parseColor("#90CAF9"));
        header.setPadding(32, 32, 16, 8);
        container.addView(header);
    }

    private void addInfoCard(String label, String value) {
        InfoCard card = new InfoCard(this);
        card.setLabel(label);
        card.setValue(value);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 4, 16, 4);
        card.setLayoutParams(params);
        container.addView(card);
    }
}
