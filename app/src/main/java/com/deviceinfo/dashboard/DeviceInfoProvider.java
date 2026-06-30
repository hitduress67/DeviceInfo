package $PKG;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class DeviceInfoProvider {

    private final Context context;

    public DeviceInfoProvider(Context context) {
        this.context = context;
    }

    // ── Device ──
    public String getDeviceModel() {
        return Build.MODEL;
    }

    public String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public String getDeviceName() {
        String name = Settings.Global.getString(context.getContentResolver(), "device_name");
        return name != null ? name : Build.DEVICE;
    }

    public String getAndroidVersion() {
        return Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")";
    }

    public String getBuildNumber() {
        return Build.DISPLAY;
    }

    public String getSecurityPatch() {
        return Build.VERSION.SECURITY_PATCH;
    }

    public String getBootloader() {
        return Build.BOOTLOADER;
    }

    // ── CPU ──
    public String getCpuInfo() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Processor") || line.contains("Hardware") || line.contains("model name")) {
                    sb.append(line.trim()).append("\n");
                }
            }
            br.close();
            String result = sb.toString().trim();
            return result.isEmpty() ? Build.HARDWARE : result;
        } catch (Exception e) {
            return Build.HARDWARE;
        }
    }

    public String getCpuCores() {
        return String.valueOf(Runtime.getRuntime().availableProcessors());
    }

    // ── Memory ──
    public String getRamInfo() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        long totalMem = mi.totalMem;
        long availMem = mi.availMem;
        long usedMem = totalMem - availMem;
        return formatBytes(usedMem) + " / " + formatBytes(totalMem) + " used";
    }

    public String getRamPercent() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        long used = mi.totalMem - mi.availMem;
        int pct = (int) (used * 100 / mi.totalMem);
        return pct + "%";
    }

    // ── Storage ──
    public String getStorageInfo() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availBlocks = stat.getAvailableBlocksLong();
        long total = totalBlocks * blockSize;
        long avail = availBlocks * blockSize;
        long used = total - avail;
        return formatBytes(used) + " / " + formatBytes(total) + " used";
    }

    public String getStoragePercent() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availBlocks = stat.getAvailableBlocksLong();
        long total = totalBlocks * blockSize;
        long avail = availBlocks * blockSize;
        int pct = (int) ((total - avail) * 100 / total);
        return pct + "%";
    }

    // ── Battery ──
    public String getBatteryLevel() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        if (batteryStatus == null) return "N/A";
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level == -1 || scale == -1) return "N/A";
        return (level * 100 / scale) + "%";
    }

    public String getBatteryHealth() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        if (batteryStatus == null) return "N/A";
        int health = batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
        switch (health) {
            case BatteryManager.BATTERY_HEALTH_GOOD: return "Good";
            case BatteryManager.BATTERY_HEALTH_OVERHEAT: return "Overheat";
            case BatteryManager.BATTERY_HEALTH_DEAD: return "Dead";
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE: return "Over Voltage";
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE: return "Failure";
            default: return "Unknown";
        }
    }

    public String getBatteryTemperature() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        if (batteryStatus == null) return "N/A";
        int temp = batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(temp / 10.0) + "°C";
    }

    public String getChargingStatus() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        if (batteryStatus == null) return "N/A";
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        switch (status) {
            case BatteryManager.BATTERY_STATUS_CHARGING: return "Charging";
            case BatteryManager.BATTERY_STATUS_DISCHARGING: return "Discharging";
            case BatteryManager.BATTERY_STATUS_FULL: return "Full";
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING: return "Not Charging";
            default: return "Unknown";
        }
    }

    // ── Display ──
    public String getScreenResolution() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        return dm.widthPixels + " × " + dm.heightPixels + " px";
    }

    public String getScreenDensity() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        switch (dm.densityDpi) {
            case DisplayMetrics.DENSITY_LOW: return "LDPI (" + dm.densityDpi + ")";
            case DisplayMetrics.DENSITY_MEDIUM: return "MDPI (" + dm.densityDpi + ")";
            case DisplayMetrics.DENSITY_HIGH: return "HDPI (" + dm.densityDpi + ")";
            case DisplayMetrics.DENSITY_XHIGH: return "XHDPI (" + dm.densityDpi + ")";
            case DisplayMetrics.DENSITY_XXHIGH: return "XXHDPI (" + dm.densityDpi + ")";
            case DisplayMetrics.DENSITY_XXXHIGH: return "XXXHDPI (" + dm.densityDpi + ")";
            default: return dm.densityDpi + " dpi";
        }
    }

    public float getScreenSize() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        return (float) Math.round(Math.sqrt(x + y) * 10) / 10;
    }

    // ── Network ──
    public String getNetworkType() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null) return "Disconnected";
        return activeNetwork.getTypeName() + " (" + activeNetwork.getSubtypeName() + ")";
    }

    public String getIpAddress() {
        // Simplified - just shows connection status
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return "Connected";
        }
        return "Disconnected";
    }

    // ── Sensors ──
    public String getSensorCount() {
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
        return String.valueOf(sensors.size());
    }

    // ── System ──
    public String getUptime() {
        long uptime = System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime();
        long seconds = (android.os.SystemClock.elapsedRealtime() / 1000);
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long mins = (seconds % 3600) / 60;
        if (days > 0) return days + "d " + hours + "h " + mins + "m";
        if (hours > 0) return hours + "h " + mins + "m";
        return mins + "m";
    }

    // ── Helpers ──
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024, exp), pre);
    }
}
