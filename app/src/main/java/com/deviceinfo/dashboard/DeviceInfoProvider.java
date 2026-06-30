package com.deviceinfo.dashboard;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DeviceInfoProvider {

    private final Context context;

    // Known SoC / processor name mappings
    private static final Map<String, String> SOC_NAMES = new HashMap<>();
    static {
        SOC_NAMES.put("exynos", "Samsung Exynos");
        SOC_NAMES.put("s5e", "Samsung Exynos");
        SOC_NAMES.put("samsungexynos", "Samsung Exynos");
        SOC_NAMES.put("smd", "Qualcomm Snapdragon");
        SOC_NAMES.put("kryo", "Qualcomm Kryo");
        SOC_NAMES.put("qcom", "Qualcomm Snapdragon");
        SOC_NAMES.put("qualcomm", "Qualcomm Snapdragon");
        SOC_NAMES.put("mt", "MediaTek Dimensity");
        SOC_NAMES.put("mediatek", "MediaTek");
        SOC_NAMES.put("kirin", "HiSilicon Kirin");
        SOC_NAMES.put("hisi", "HiSilicon");
        SOC_NAMES.put("apple", "Apple");
        SOC_NAMES.put("tensor", "Google Tensor");
        SOC_NAMES.put("rockchip", "Rockchip");
        SOC_NAMES.put("spreadtrum", "Unisoc Spreadtrum");
        // Specific model mappings by hardware string
        SOC_NAMES.put("s5e9925", "Samsung Exynos 2200");
        SOC_NAMES.put("s5e9935", "Samsung Exynos 2200");
        SOC_NAMES.put("s5e8825", "Samsung Exynos 1280");
        SOC_NAMES.put("s5e8535", "Samsung Exynos 1380");
        SOC_NAMES.put("exynos2200", "Samsung Exynos 2200");
        SOC_NAMES.put("exynos2100", "Samsung Exynos 2100");
        SOC_NAMES.put("sm8450", "Qualcomm Snapdragon 8 Gen 1");
        SOC_NAMES.put("sm8475", "Qualcomm Snapdragon 8+ Gen 1");
        SOC_NAMES.put("sm8550", "Qualcomm Snapdragon 8 Gen 2");
        SOC_NAMES.put("sm8560", "Qualcomm Snapdragon 8 Gen 2");
        SOC_NAMES.put("sm8575", "Qualcomm Snapdragon 8+ Gen 2");
        SOC_NAMES.put("sm8650", "Qualcomm Snapdragon 8 Gen 3");
        SOC_NAMES.put("sm8675", "Qualcomm Snapdragon 8+ Gen 3");
        SOC_NAMES.put("sm8750", "Qualcomm Snapdragon 8 Elite");
        SOC_NAMES.put("sm7325", "Qualcomm Snapdragon 778G");
        SOC_NAMES.put("sm8350", "Qualcomm Snapdragon 888");
        SOC_NAMES.put("mt6983", "MediaTek Dimensity 9000");
        SOC_NAMES.put("mt6985", "MediaTek Dimensity 9200");
        SOC_NAMES.put("mt6989", "MediaTek Dimensity 9300");
        SOC_NAMES.put("mt6991", "MediaTek Dimensity 9400");
        SOC_NAMES.put("mt6893", "MediaTek Dimensity 1200");
        SOC_NAMES.put("mt6895", "MediaTek Dimensity 8100");
        SOC_NAMES.put("mt6877", "MediaTek Dimensity 7050");
        SOC_NAMES.put("kirin9000", "HiSilicon Kirin 9000");
        SOC_NAMES.put("kirin9010", "HiSilicon Kirin 9010");
        SOC_NAMES.put("kirin990", "HiSilicon Kirin 990");
    }

    // Human-readable sensor type names
    private static final Map<Integer, String> SENSOR_TYPES = new HashMap<>();
    static {
        SENSOR_TYPES.put(Sensor.TYPE_ACCELEROMETER, "Accelerometer");
        SENSOR_TYPES.put(Sensor.TYPE_MAGNETIC_FIELD, "Magnetometer");
        SENSOR_TYPES.put(Sensor.TYPE_GYROSCOPE, "Gyroscope");
        SENSOR_TYPES.put(Sensor.TYPE_LIGHT, "Light");
        SENSOR_TYPES.put(Sensor.TYPE_PRESSURE, "Barometer");
        SENSOR_TYPES.put(Sensor.TYPE_PROXIMITY, "Proximity");
        SENSOR_TYPES.put(Sensor.TYPE_GRAVITY, "Gravity");
        SENSOR_TYPES.put(Sensor.TYPE_LINEAR_ACCELERATION, "Linear Accel");
        SENSOR_TYPES.put(Sensor.TYPE_ROTATION_VECTOR, "Rotation Vector");
        SENSOR_TYPES.put(Sensor.TYPE_RELATIVE_HUMIDITY, "Humidity");
        SENSOR_TYPES.put(Sensor.TYPE_AMBIENT_TEMPERATURE, "Ambient Temp");
        SENSOR_TYPES.put(Sensor.TYPE_STEP_COUNTER, "Step Counter");
        SENSOR_TYPES.put(Sensor.TYPE_STEP_DETECTOR, "Step Detector");
        SENSOR_TYPES.put(Sensor.TYPE_HEART_RATE, "Heart Rate");
        SENSOR_TYPES.put(Sensor.TYPE_HEART_BEAT, "Heart Beat");
        SENSOR_TYPES.put(Sensor.TYPE_SIGNIFICANT_MOTION, "Significant Motion");
        SENSOR_TYPES.put(Sensor.TYPE_GAME_ROTATION_VECTOR, "Game Rotation");
        SENSOR_TYPES.put(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, "Geo Rotation");
        SENSOR_TYPES.put(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, "Magnetometer (Uncal)");
        SENSOR_TYPES.put(Sensor.TYPE_GYROSCOPE_UNCALIBRATED, "Gyroscope (Uncal)");
        SENSOR_TYPES.put(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED, "Accelerometer (Uncal)");
        SENSOR_TYPES.put(Sensor.TYPE_HINGE_ANGLE, "Hinge Angle");
        SENSOR_TYPES.put(Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT, "Off-Body Detect");
        SENSOR_TYPES.put(Sensor.TYPE_MOTION_DETECT, "Motion Detect");
        SENSOR_TYPES.put(Sensor.TYPE_STATIONARY_DETECT, "Stationary Detect");
        SENSOR_TYPES.put(Sensor.TYPE_POSE_6DOF, "6-DoF Pose");
    }

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
        String patch = Build.VERSION.SECURITY_PATCH;
        return patch != null && !patch.isEmpty() ? patch : "N/A";
    }

    public String getBootloader() {
        return Build.BOOTLOADER;
    }

    // ── CPU / SoC ──
    public String getCpuInfo() {
        StringBuilder result = new StringBuilder();
        
        // 1. Get raw identifier from /proc/cpuinfo
        String rawHardware = Build.HARDWARE;
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("Hardware")) {
                    rawHardware = line.substring(line.indexOf(':') + 1).trim();
                }
                if (line.contains("Processor")) {
                    String proc = line.substring(line.indexOf(':') + 1).trim();
                    if (!proc.isEmpty()) {
                        result.append(proc).append("\n");
                    }
                }
            }
            br.close();
        } catch (Exception ignored) {}

        // 2. Map to known SoC name
        String hwLower = rawHardware.toLowerCase(Locale.US);
        String knownName = null;
        
        // Check exact match first
        if (SOC_NAMES.containsKey(hwLower)) {
            knownName = SOC_NAMES.get(hwLower);
        } else {
            // Check partial matches (e.g., "s5e9925" matches "s5e" prefix)
            for (Map.Entry<String, String> entry : SOC_NAMES.entrySet()) {
                if (hwLower.contains(entry.getKey()) || entry.getKey().contains(hwLower)) {
                    knownName = entry.getValue();
                    break;
                }
            }
        }

        // 3. Build display string
        StringBuilder display = new StringBuilder();
        if (knownName != null) {
            display.append(knownName);
            // Include the raw identifier after the known name for reference
            if (!rawHardware.equalsIgnoreCase(knownName.replaceAll("[^a-zA-Z0-9]", ""))) {
                display.append("\n(").append(rawHardware).append(")");
            }
        } else if (result.length() > 0) {
            display.append(result.toString().trim());
        } else {
            display.append(rawHardware);
        }

        return display.toString().trim();
    }

    public String getCpuCores() {
        int cores = Runtime.getRuntime().availableProcessors();
        String arch = System.getProperty("os.arch", "?");
        return cores + " cores (" + arch + ")";
    }

    // ── Memory ──
    public String getRamInfo() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        long totalMem = mi.totalMem;
        long availMem = mi.availMem;
        long usedMem = totalMem - availMem;
        return formatBytes(usedMem) + " / " + formatBytes(totalMem);
    }

    public String getRamPercent() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        long used = mi.totalMem - mi.availMem;
        int pct = (int) (used * 100 / mi.totalMem);
        return pct + "% used";
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
        return formatBytes(used) + " / " + formatBytes(total);
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
        return pct + "% used";
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
            case BatteryManager.BATTERY_HEALTH_COLD: return "Cold";
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

    public String getScreenSize() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
        double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
        return String.format(Locale.US, "%.1f inches", Math.round(Math.sqrt(x + y) * 10) / 10.0);
    }

    // ── Network (fixed) ──
    public String getNetworkType() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return "N/A";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network activeNetwork = cm.getActiveNetwork();
                if (activeNetwork == null) return "Disconnected";

                NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
                if (caps == null) return "Disconnected";

                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (wm != null) {
                        WifiInfo wifiInfo = wm.getConnectionInfo();
                        if (wifiInfo != null) {
                            String ssid = wifiInfo.getSSID();
                            if (ssid == null || ssid.equals("<unknown ssid>")) ssid = "Connected";
                            else ssid = ssid.replace("\"", "");
                            int freq = wifiInfo.getFrequency();
                            int rssi = wifiInfo.getRssi();
                            String signalQuality;
                            if (rssi >= -50) signalQuality = "Excellent";
                            else if (rssi >= -60) signalQuality = "Good";
                            else if (rssi >= -70) signalQuality = "Fair";
                            else signalQuality = "Weak";
                            return "WiFi · " + ssid + "\n" + freq + " MHz (" + signalQuality + ")";
                        }
                    }
                    return "WiFi";
                } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    String netType = "Cellular";
                    try {
                        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                        if (tm != null) {
                            int type = tm.getDataNetworkType();
                            switch (type) {
                                case TelephonyManager.NETWORK_TYPE_NR: netType = "5G"; break;
                                case TelephonyManager.NETWORK_TYPE_LTE: netType = "4G LTE"; break;
                                case TelephonyManager.NETWORK_TYPE_UMTS:
                                case TelephonyManager.NETWORK_TYPE_HSPA:
                                case TelephonyManager.NETWORK_TYPE_HSPAP: netType = "3G"; break;
                                case TelephonyManager.NETWORK_TYPE_EDGE:
                                case TelephonyManager.NETWORK_TYPE_GPRS: netType = "2G"; break;
                            }
                            String carrier = tm.getNetworkOperatorName();
                            if (carrier != null && !carrier.isEmpty()) {
                                return netType + " · " + carrier;
                            }
                        }
                    } catch (SecurityException ignored) {
                        // No phone permission - just show generic
                    }
                    return netType;
                } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return "Ethernet";
                } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    return "VPN";
                }
                return "Connected";
            } else {
                @SuppressWarnings("deprecation")
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork == null) return "Disconnected";
                return activeNetwork.getTypeName();
            }
        } catch (Exception e) {
            return "Network info unavailable";
        }
    }

    public String getConnectionStatus() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return "N/A";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Network activeNetwork = cm.getActiveNetwork();
                if (activeNetwork == null) return "Disconnected";
                NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
                if (caps == null) return "Disconnected";
                if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    return "Connected";
                }
                return "No Internet";
            } else {
                @SuppressWarnings("deprecation")
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null && activeNetwork.isConnected()) return "Connected";
                return "Disconnected";
            }
        } catch (Exception e) {
            return "N/A";
        }
    }

    // ── Sensors (improved) ──
    public String getSensorSummary() {
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
        
        if (sensors.isEmpty()) return "None detected";
        
        // Count by category
        int motion = 0, position = 0, environmental = 0, biomedical = 0, other = 0;
        StringBuilder notable = new StringBuilder();
        
        for (Sensor s : sensors) {
            String name = s.getName();
            String typeName = SENSOR_TYPES.getOrDefault(s.getType(), "Other");
            
            // Categorize
            if (typeName.contains("Accel") || typeName.contains("Gyro") || typeName.contains("Gravity") ||
                typeName.contains("Linear") || typeName.contains("Step") || typeName.contains("Motion") ||
                typeName.contains("Stationary") || typeName.contains("Heart") || typeName.contains("Significant")) {
                motion++;
            } else if (typeName.contains("Magnet") || typeName.contains("Rotation") || typeName.contains("Pose") ||
                       typeName.contains("Hinge") || typeName.contains("Off-Body") || typeName.contains("Geo") ||
                       typeName.contains("Game")) {
                position++;
            } else if (typeName.contains("Light") || typeName.contains("Barometer") || typeName.contains("Pressure") ||
                       typeName.contains("Humidity") || typeName.contains("Temp") || typeName.contains("Proximity")) {
                environmental++;
            } else {
                other++;
            }
            
            // Build notable sensor list (first few)
            if (notable.length() == 0) {
                // Include and deduplicate
                if (!name.toLowerCase().contains("virtual") && !name.contains("3pp")) {
                    notable.append(name);
                }
            }
        }
        
        StringBuilder result = new StringBuilder();
        result.append(sensors.size()).append(" total");
        if (motion > 0) result.append(" · ").append(motion).append(" motion");
        if (position > 0) result.append(" · ").append(position).append(" position");
        if (environmental > 0) result.append(" · ").append(environmental).append(" env");
        
        return result.toString();
    }
    
    public String getSensorCount() {
        return getSensorSummary();
    }

    // ── System ──
    public String getUptime() {
        long seconds = (android.os.SystemClock.elapsedRealtime() / 1000);
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long mins = (seconds % 3600) / 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0 || days > 0) sb.append(hours).append("h ");
        sb.append(mins).append("m");
        return sb.toString();
    }

    // ── Helpers ──
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "B";
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024, exp), pre);
    }
}
