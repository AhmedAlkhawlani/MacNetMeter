package com.maknoon.service;

import java.io.IOException;
import java.util.*;
import java.util.prefs.Preferences;

public class DataSaverService {
    private final Preferences prefs = Preferences.userNodeForPackage(DataSaverService.class);
    private final Set<String> blockedApps = new HashSet<>();
    private boolean enabled = false;

    public DataSaverService() {
        // استرجاع قائمة البرامج المحظورة المحفوظة في ذاكرة الماك الدائمة
        String saved = prefs.get("blocked_apps_list", "");
        if (!saved.isEmpty()) {
            blockedApps.addAll(Arrays.asList(saved.split(",")));
        }
    }

    public boolean isEnabled() { return enabled; }
    public void toggle() { this.enabled = !this.enabled; }

    public boolean isAppBlocked(String appName) {
        return blockedApps.contains(appName.toLowerCase());
    }

    public void blockApp(String appName) {
        blockedApps.add(appName.toLowerCase());
        saveToPrefs();
    }

    public void unblockApp(String appName) {
        blockedApps.remove(appName.toLowerCase());
        saveToPrefs();
    }

    public Set<String> getBlockedApps() {
        return Collections.unmodifiableSet(blockedApps);
    }

    // فحص ذكي: إذا كان التطبيق محظوراً ويحاول سحب نت أثناء تفعيل الدرع -> اقتله فوراً!
    public void inspectAndEnforce(String appName, int pid, long speed) {
        if (!enabled) return;

        if (isAppBlocked(appName) && speed > 0) {
            // إغلاق البرنامج فوراً لقطع النت
            ProcessHandle.of(pid).ifPresent(ProcessHandle::destroyForcibly);
            sendMacAlert(appName);
        }
    }

    private void saveToPrefs() {
        prefs.put("blocked_apps_list", String.join(",", blockedApps));
    }

    private void sendMacAlert(String appName) {
        new Thread(() -> {
            try {
                String script = String.format("display notification \"تم إيقاف %s فوراً لمنعه من استهلاك باقة الهوتسبوت!\" with title \"🛡️ جدار الحماية نشط\" sound name \"Basso\"", appName);
                new ProcessBuilder("osascript", "-e", script).start();
            } catch (IOException ignored) {}
        }).start();
    }
}
//package com.maknoon.service;
//
//import java.io.IOException;
//
//public class DataSaverService {
//    private boolean enabled = false;
//    private long lastAlertTime = 0;
//
//    public boolean isEnabled() {
//        return enabled;
//    }
//
//    public void toggle() {
//        this.enabled = !this.enabled;
//    }
//
//    // فحص إذا كان هناك تطبيق يلتهم باقة الهوتسبوت في وضع التوفير
//    public void inspectTraffic(String appName, long speed) {
//        if (!enabled) return;
//
//        // إذا سحب أي تطبيق أكثر من 1 ميجابايت/ثانية في وضع التوفير
//        if (speed > 1024 * 1024 && (System.currentTimeMillis() - lastAlertTime > 10000)) {
//            sendMacAlert(appName);
//            lastAlertTime = System.currentTimeMillis();
//        }
//    }
//
//    private void sendMacAlert(String appName) {
//        new Thread(() -> {
//            try {
//                String script = String.format("display notification \"برنامج %s يستهلك سرعة عالية في وضع التوفير!\" with title \"🛡️ وضع توفير الباقة نشط\" sound name \"Hero\"", appName);
//                new ProcessBuilder("osascript", "-e", script).start();
//            } catch (IOException ignored) {}
//        }).start();
//    }
//}