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

    public long getAlertLimitBytes() {
        return alertLimitBytes;
    }

    public boolean isAlertActive() {
        return alertLimitBytes > 0;
    }

    public void checkUsage(long todayBytes) {
        if (alertLimitBytes > 0 && todayBytes >= alertLimitBytes && !alertedToday) {
            // إظهار نافذة منبثقة رسمية من نظام الماك تثبت في الشاشة
            sendMacAlertBox("⚠️ تنبيه استهلاك البيانات",
                    "لقد تجاوز استهلاكك لليوم الحد المحدد (" + formatData(alertLimitBytes) + ")!\\nالاستهلاك الحالي: " + formatData(todayBytes));
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

    // هذه النافذة لا تختفي أبداً حتى يضغط المستخدم على "حسناً"
    private void sendMacAlertBox(String title, String message) {
        new Thread(() -> {
            try {
                String script = String.format("display alert \"%s\" message \"%s\" as warning buttons {\"حسناً\"} default button \"حفظ\"", title, message);
                new ProcessBuilder("osascript", "-e", script).start();
            } catch (Exception ignored) {}
        }).start();
    }

    private String formatData(long bytes) {
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}
//package com.maknoon.service;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.prefs.Preferences;
//
//public class AlertService {
//    private final Preferences prefs = Preferences.userNodeForPackage(AlertService.class);
//    private long alertLimitBytes;
//    private boolean alertedToday = false;
//
//    public AlertService() {
//        // 0 تعني معطل افتراضياً
//        this.alertLimitBytes = prefs.getLong("alert_limit", 0);
//    }
//
//    public long getAlertLimitBytes() {
//        return alertLimitBytes;
//    }
//
//    public boolean isAlertActive() {
//        return alertLimitBytes > 0;
//    }
//
//    public void checkUsage(long todayBytes) {
//        if (alertLimitBytes > 0 && todayBytes >= alertLimitBytes && !alertedToday) {
//            sendMacNotification("تنبيه استهلاك البيانات", "لقد استهلكت كامل الحد المحدد لليوم!");
//            alertedToday = true;
//        }
//    }
//
//    public void resetAlert() {
//        alertedToday = false;
//    }
//
//    public void setAlertLimitMB(long megabytes) {
//        this.alertLimitBytes = megabytes * 1024L * 1024L;
//        prefs.putLong("alert_limit", alertLimitBytes);
//        this.alertedToday = false;
//    }
//
//    public void disableAlert() {
//        this.alertLimitBytes = 0;
//        prefs.putLong("alert_limit", 0);
//        this.alertedToday = false;
//    }
//
//    // نافذة إدخال أصلية من نظام الماك لكتابة أي رقم تريده بالميجابايت!
//    public void promptCustomLimit(Runnable onDone) {
//        new Thread(() -> {
//            try {
//                String script = "text returned of (display dialog \"أدخل الحد اليومي بالميجابايت (MB):\" default answer \"500\" buttons {\"إلغاء\", \"حفظ\"} default button \"حفظ\" with title \"تخصيص استهلاك البيانات\")";
//                Process process = new ProcessBuilder("osascript", "-e", script).start();
//                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//                String input = reader.readLine();
//                if (input != null && !input.trim().isEmpty()) {
//                    long mb = Long.parseLong(input.trim());
//                    if (mb > 0) {
//                        setAlertLimitMB(mb);
//                        onDone.run();
//                    }
//                }
//            } catch (Exception ignored) {}
//        }).start();
//    }
//
//    private void sendMacNotification(String title, String message) {
//        String script = String.format("display notification \"%s\" with title \"%s\" sound name \"Submarine\"", message, title);
//        try {
//            new ProcessBuilder("osascript", "-e", script).start();
//        } catch (IOException ignored) {}
//    }
//}
////package com.maknoon.service;
////
////import java.io.IOException;
////import java.util.prefs.Preferences;
////
////public class AlertService {
////    private final Preferences prefs = Preferences.userNodeForPackage(AlertService.class);
////    private long alertLimitBytes;
////    private boolean alertedToday = false;
////
////    public AlertService() {
////        // القيمة الافتراضية 1 جيجابايت (0 تعني معطل)
////        this.alertLimitBytes = prefs.getLong("alert_limit", 1024L * 1024 * 1024);
////    }
////
////    public void checkUsage(long todayBytes) {
////        if (alertLimitBytes > 0 && todayBytes >= alertLimitBytes && !alertedToday) {
////            sendMacNotification("تنبيه استهلاك البيانات", "لقد تجاوزت استهلاك " + (alertLimitBytes / (1024 * 1024 * 1024)) + " GB اليوم!");
////            alertedToday = true;
////        }
////    }
////
////    public void resetAlert() {
////        alertedToday = false;
////    }
////
////    public void setAlertLimitGB(int gigabytes) {
////        this.alertLimitBytes = (long) gigabytes * 1024 * 1024 * 1024;
////        prefs.putLong("alert_limit", alertLimitBytes);
////        this.alertedToday = false;
////    }
////
////    private void sendMacNotification(String title, String message) {
////        String script = String.format("display notification \"%s\" with title \"%s\" sound name \"Submarine\"", message, title);
////        try {
////            new ProcessBuilder("osascript", "-e", script).start();
////        } catch (IOException ignored) {}
////    }
////}