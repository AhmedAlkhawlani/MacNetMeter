package com.maknoon.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.prefs.Preferences;

public class AlertService {
    private final Preferences prefs = Preferences.userNodeForPackage(AlertService.class);
    private long alertLimitBytes;
    private boolean alertedToday = false;

    public AlertService() {
        this.alertLimitBytes = prefs.getLong("alert_limit", 0);
    }

    public long getAlertLimitBytes() { return alertLimitBytes; }
    public boolean isAlertActive() { return alertLimitBytes > 0; }

    public void checkUsage(long todayBytes) {
        if (alertLimitBytes > 0 && todayBytes >= alertLimitBytes && !alertedToday) {
            sendMacAlertBox("⚠️ تنبيه تجاوز باقة البيانات!",
                    "لقد تجاوز استهلاكك لليوم الحد المحدد (" + formatData(alertLimitBytes) + ")!\\nإجمالي الاستهلاك الحالي: " + formatData(todayBytes));
            alertedToday = true;
        }
    }

    public void resetAlert() {
        alertedToday = false;
    }

    public void setAlertLimitMB(long megabytes) {
        this.alertLimitBytes = megabytes * 1024L * 1024L;
        prefs.putLong("alert_limit", alertLimitBytes);
        this.alertedToday = false;
    }

    public void disableAlert() {
        this.alertLimitBytes = 0;
        prefs.putLong("alert_limit", 0);
        this.alertedToday = false;
    }

    public void promptCustomLimit(Runnable onDone) {
        new Thread(() -> {
            try {
                String script = "text returned of (display dialog \"أدخل الحد اليومي بالميجابايت (MB):\" default answer \"500\" buttons {\"إلغاء\", \"حفظ\"} default button \"حفظ\" with title \"تخصيص استهلاك البيانات\")";
                Process process = new ProcessBuilder("osascript", "-e", script).start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String input = reader.readLine();
                if (input != null && !input.trim().isEmpty()) {
                    long mb = Long.parseLong(input.trim());
                    if (mb > 0) {
                        setAlertLimitMB(mb);
                        onDone.run();
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }

    // تم إصلاح الخطأ: الآن الزر الافتراضي يطابق زر "حسناً" بدقة مع صوت Sosumi
    private void sendMacAlertBox(String title, String message) {
        new Thread(() -> {
            try {
                String script = String.format("display alert \"%s\" message \"%s\" as critical buttons {\"حسناً\"} default button \"حسناً\"", title, message);
                new ProcessBuilder("osascript", "-e", script).start();
                new ProcessBuilder("afplay", "/System/Library/Sounds/Sosumi.aiff").start();
            } catch (Exception ignored) {}
        }).start();
    }

    public static String formatData(long bytes) {
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}