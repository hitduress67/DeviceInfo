package $PKG;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.RoundedBitmapDrawable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DeviceInfoProvider info;
    private LinearLayout container;
    private SwipeRefreshLayout swipeRefresh;
    private TextView lastUpdatedView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        info = new DeviceInfoProvider(this);
        container = findViewById(R.id.infoContainer);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        lastUpdatedView = findViewById(R.id.lastUpdated);
        
        swipeRefresh.setOnRefreshListener(this::refreshInfo);
        
        buildDashboard();
        
        // Auto-refresh every 30 seconds
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshInfo();
                handler.postDelayed(this, 30000);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshInfo();
        handler.postDelayed(refreshRunnable, 30000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable);
    }

    private void refreshInfo() {
        buildDashboard();
        swipeRefresh.setRefreshing(false);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        lastUpdatedView.setText("Last updated: " + sdf.format(new Date()));
    }

    private void buildDashboard() {
        container.removeAllViews();
        
        addSectionHeader("Device");
        addInfoCard("Model", info.getDeviceModel(), "#1565C0");
        addInfoCard("Manufacturer", info.getManufacturer(), "#1565C0");
        addInfoCard("Device Name", info.getDeviceName(), "#1565C0");
        addInfoCard("Android Version", info.getAndroidVersion(), "#1565C0");
        addInfoCard("Build", info.getBuildNumber(), "#1565C0");
        addInfoCard("Security Patch", info.getSecurityPatch(), "#1565C0");
        addInfoCard("Bootloader", info.getBootloader(), "#1565C0");
        
        addSectionHeader("Performance");
        addInfoCard("CPU", info.getCpuInfo(), "#2E7D32");
        addInfoCard("CPU Cores", info.getCpuCores(), "#2E7D32");
        addInfoCard("RAM", info.getRamInfo(), "#2E7D32");
        addInfoCard("RAM Usage", info.getRamPercent(), "#2E7D32");
        
        addSectionHeader("Storage");
        addInfoCard("Internal Storage", info.getStorageInfo(), "#E65100");
        addInfoCard("Storage Used", info.getStoragePercent(), "#E65100");
        
        addSectionHeader("Battery");
        addInfoCard("Level", info.getBatteryLevel(), "#6A1B9A");
        addInfoCard("Health", info.getBatteryHealth(), "#6A1B9A");
        addInfoCard("Temperature", info.getBatteryTemperature(), "#6A1B9A");
        addInfoCard("Status", info.getChargingStatus(), "#6A1B9A");
        
        addSectionHeader("Display");
        addInfoCard("Resolution", info.getScreenResolution(), "#00695C");
        addInfoCard("Density", info.getScreenDensity(), "#00695C");
        addInfoCard("Size", info.getScreenSize() + " inches", "#00695C");
        
        addSectionHeader("Network");
        addInfoCard("Type", info.getNetworkType(), "#37474F");
        addInfoCard("Status", info.getIpAddress(), "#37474F");
        
        addSectionHeader("System");
        addInfoCard("Sensors", info.getSensorCount(), "#5D4037");
        addInfoCard("Uptime", info.getUptime(), "#5D4037");
    }

    private void addSectionHeader(String title) {
        TextView header = new TextView(this);
        header.setText(title);
        header.setTextSize(14);
        header.setTypeface(null, android.graphics.Typeface.BOLD);
        header.setTextColor(Color.parseColor("#90CAF9"));
        header.setPadding(16, 24, 16, 8);
        container.addView(header);
    }

    private void addInfoCard(String label, String value, String colorHex) {
        InfoCard card = new InfoCard(this);
        card.setLabel(label);
        card.setValue(value);
        int color = Color.parseColor(colorHex);
        card.setCardBackgroundColor(adjustAlpha(color, 0.15f));
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 4, 16, 4);
        card.setLayoutParams(params);
        container.addView(card);
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        return Color.argb(alpha, r, g, b);
    }
}
