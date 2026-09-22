package com.maknoon;

import com.maknoon.model.DisplayMode;
import com.maknoon.service.AlertService;
import com.maknoon.service.CaffeinateService;
import com.maknoon.service.CpuMemoryService;
import com.maknoon.service.SystemInfoService;
import com.maknoon.ui.CpuMenuBuilder;
import com.maknoon.ui.CpuWidgetRenderer;
import com.maknoon.ui.MenuBuilder;
import com.maknoon.ui.WidgetRenderer;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

public class App {
    private static final Preferences prefs = Preferences.userNodeForPackage(App.class);
    private static DisplayMode currentMode = DisplayMode.FULL;
    private static long todayBytes = 0;
    private static String savedDate = "";
    private static long previousRx = 0, previousTx = 0;

    public static void main(String[] args) throws Exception {
        System.setProperty("apple.awt.UIElement", "true");
        if (!SystemTray.isSupported()) return;

        loadTodayData();

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        // الخدمات
        CaffeinateService caffeinateService = new CaffeinateService();
        AlertService alertService = new AlertService();
        SystemInfoService sysInfoService = new SystemInfoService(si);
        CpuMemoryService cpuMemoryService = new CpuMemoryService(si);

        SystemTray tray = SystemTray.getSystemTray();

        // 1. أيقونة الشبكة (الوحدة الأولى)
        TrayIcon netWidget = new TrayIcon(WidgetRenderer.render(currentMode, "0 B", "0 B", todayBytes, alertService.getAlertLimitBytes(), false, 0, 0));
        netWidget.setImageAutoSize(false);

        // 2. أيقونة المعالج والرام (الوحدة الثانية المستقلة)
        TrayIcon cpuWidget = new TrayIcon(CpuWidgetRenderer.render(0, cpuMemoryService.getUsedMemory(), cpuMemoryService.getTotalMemory()));
        cpuWidget.setImageAutoSize(false);

        // تفعيل قائمة الشبكة
        Runnable refreshNet = () -> {
            netWidget.setPopupMenu(MenuBuilder.build(caffeinateService, alertService, sysInfoService, currentMode,
                    () -> refreshNet(netWidget, caffeinateService, alertService, sysInfoService),
                    () -> {
                        todayBytes = 0;
                        saveTodayData();
                        alertService.resetAlert();
                        refreshNet(netWidget, caffeinateService, alertService, sysInfoService);
                    },
                    m -> {
                        currentMode = m;
                        refreshNet(netWidget, caffeinateService, alertService, sysInfoService);
                    }
            ));
        };

        refreshNet.run();

        // إضافة الأيقونتين لشريط الماك (الماك سيرتبهم بجانب بعضهم)
        tray.add(netWidget);
        tray.add(cpuWidget);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkMidnightReset(alertService);

                // --- تحديث الشبكة ---
                List<NetworkIF> nets = hal.getNetworkIFs();
                long curRx = 0, curTx = 0;
                for (NetworkIF net : nets) {
                    net.updateAttributes();
                    curRx += net.getBytesRecv();
                    curTx += net.getBytesSent();
                }

                if (previousRx > 0 && previousTx > 0) {
                    long rxSpeed = curRx - previousRx;
                    long txSpeed = curTx - previousTx;
                    todayBytes += (rxSpeed + txSpeed);
                    saveTodayData();

                    alertService.checkUsage(todayBytes);

                    netWidget.setImage(WidgetRenderer.render(
                            currentMode,
                            formatSpeed(rxSpeed),
                            formatSpeed(txSpeed),
                            todayBytes,
                            alertService.getAlertLimitBytes(),
                            caffeinateService.isCaffeinated(),
                            rxSpeed,
                            txSpeed
                    ));
                }
                previousRx = curRx;
                previousTx = curTx;

                // --- تحديث المعالج والرام ---
                double cpu = cpuMemoryService.getCpuUsage();
                long usedMem = cpuMemoryService.getUsedMemory();
                long totalMem = cpuMemoryService.getTotalMemory();

                cpuWidget.setImage(CpuWidgetRenderer.render(cpu, usedMem, totalMem));
                // تحديث قائمة البرامج المستهلكة في الخلفية كل ثانية
                cpuWidget.setPopupMenu(CpuMenuBuilder.build(cpuMemoryService));
            }
        }, 0, 1000);
    }

    private static void refreshNet(TrayIcon widget, CaffeinateService caffeinate, AlertService alerts, SystemInfoService sysInfo) {
        widget.setPopupMenu(MenuBuilder.build(caffeinate, alerts, sysInfo, currentMode,
                () -> refreshNet(widget, caffeinate, alerts, sysInfo),
                () -> {
                    todayBytes = 0;
                    saveTodayData();
                    alerts.resetAlert();
                    widget.setImage(WidgetRenderer.render(currentMode, "0 B", "0 B", 0, alerts.getAlertLimitBytes(), caffeinate.isCaffeinated(), 0, 0));
                },
                m -> {
                    currentMode = m;
                    refreshNet(widget, caffeinate, alerts, sysInfo);
                }
        ));
    }

    private static String formatSpeed(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        return String.format("%.1f %c", bytes / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
    }

    private static void loadTodayData() {
        savedDate = prefs.get("date", LocalDate.now().toString());
        if (savedDate.equals(LocalDate.now().toString())) {
            todayBytes = prefs.getLong("bytes", 0);
        }
    }

    private static void saveTodayData() {
        prefs.put("date", LocalDate.now().toString());
        prefs.putLong("bytes", todayBytes);
    }

    private static void checkMidnightReset(AlertService alertService) {
        String today = LocalDate.now().toString();
        if (!today.equals(savedDate)) {
            todayBytes = 0;
            savedDate = today;
            saveTodayData();
            alertService.resetAlert();
        }
    }
}
//package com.maknoon;
//
//import com.maknoon.model.DisplayMode;
//import com.maknoon.service.AlertService;
//import com.maknoon.service.CaffeinateService;
//import com.maknoon.service.SystemInfoService;
//import com.maknoon.ui.MenuBuilder;
//import com.maknoon.ui.WidgetRenderer;
//import oshi.SystemInfo;
//import oshi.hardware.HardwareAbstractionLayer;
//import oshi.hardware.NetworkIF;
//
//import java.awt.*;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.prefs.Preferences;
//
//public class App {
//    private static final Preferences prefs = Preferences.userNodeForPackage(App.class);
//    private static DisplayMode currentMode = DisplayMode.FULL;
//    private static long todayBytes = 0;
//    private static String savedDate = "";
//    private static long previousRx = 0, previousTx = 0;
//
//    public static void main(String[] args) throws Exception {
//        System.setProperty("apple.awt.UIElement", "true");
//        if (!SystemTray.isSupported()) return;
//
//        loadTodayData();
//
//        SystemInfo si = new SystemInfo();
//        HardwareAbstractionLayer hal = si.getHardware();
//        CaffeinateService caffeinateService = new CaffeinateService();
//        AlertService alertService = new AlertService();
//        SystemInfoService sysInfoService = new SystemInfoService(si);
//
//        SystemTray tray = SystemTray.getSystemTray();
//        TrayIcon widget = new TrayIcon(WidgetRenderer.render(currentMode, "0 B", "0 B", todayBytes, alertService.getAlertLimitBytes(), false, 0, 0));
//        widget.setImageAutoSize(false);
//
//        // تصفير العداد وتحديث الشاشة فوراً
//        Runnable resetTodayAction = () -> {
//            todayBytes = 0;
//            saveTodayData();
//            alertService.resetAlert();
//            refreshUI(widget, caffeinateService, alertService, sysInfoService);
//            widget.setImage(WidgetRenderer.render(currentMode, "0 B", "0 B", 0, alertService.getAlertLimitBytes(), caffeinateService.isCaffeinated(), 0, 0));
//        };
//
//        refreshUI(widget, caffeinateService, alertService, sysInfoService, resetTodayAction);
//        tray.add(widget);
//
//        Timer timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                checkMidnightReset(alertService);
//
//                List<NetworkIF> nets = hal.getNetworkIFs();
//                long curRx = 0, curTx = 0;
//                for (NetworkIF net : nets) {
//                    net.updateAttributes();
//                    curRx += net.getBytesRecv();
//                    curTx += net.getBytesSent();
//                }
//
//                if (previousRx > 0 && previousTx > 0) {
//                    long rxSpeed = curRx - previousRx;
//                    long txSpeed = curTx - previousTx;
//                    todayBytes += (rxSpeed + txSpeed);
//                    saveTodayData();
//
//                    alertService.checkUsage(todayBytes);
//
//                    widget.setImage(WidgetRenderer.render(
//                            currentMode,
//                            formatSpeed(rxSpeed),
//                            formatSpeed(txSpeed),
//                            todayBytes,
//                            alertService.getAlertLimitBytes(),
//                            caffeinateService.isCaffeinated(),
//                            rxSpeed,
//                            txSpeed
//                    ));
//                }
//                previousRx = curRx;
//                previousTx = curTx;
//            }
//        }, 0, 1000);
//    }
//
//    private static void refreshUI(TrayIcon widget, CaffeinateService caffeinate, AlertService alerts, SystemInfoService sysInfo, Runnable onResetToday) {
//        widget.setPopupMenu(MenuBuilder.build(caffeinate, alerts, sysInfo, currentMode,
//                () -> refreshUI(widget, caffeinate, alerts, sysInfo, onResetToday),
//                onResetToday,
//                m -> {
//                    currentMode = m;
//                    refreshUI(widget, caffeinate, alerts, sysInfo, onResetToday);
//                }
//        ));
//    }
//
//    private static void refreshUI(TrayIcon widget, CaffeinateService caffeinate, AlertService alerts, SystemInfoService sysInfo) {
//        refreshUI(widget, caffeinate, alerts, sysInfo, () -> {
//            todayBytes = 0;
//            saveTodayData();
//            alerts.resetAlert();
//            widget.setImage(WidgetRenderer.render(currentMode, "0 B", "0 B", 0, alerts.getAlertLimitBytes(), caffeinate.isCaffeinated(), 0, 0));
//        });
//    }
//
//    private static String formatSpeed(long bytes) {
//        if (bytes < 1024) return bytes + " B";
//        int exp = (int) (Math.log(bytes) / Math.log(1024));
//        return String.format("%.1f %c", bytes / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
//    }
//
//    private static void loadTodayData() {
//        savedDate = prefs.get("date", LocalDate.now().toString());
//        if (savedDate.equals(LocalDate.now().toString())) {
//            todayBytes = prefs.getLong("bytes", 0);
//        }
//    }
//
//    private static void saveTodayData() {
//        prefs.put("date", LocalDate.now().toString());
//        prefs.putLong("bytes", todayBytes);
//    }
//
//    private static void checkMidnightReset(AlertService alertService) {
//        String today = LocalDate.now().toString();
//        if (!today.equals(savedDate)) {
//            todayBytes = 0;
//            savedDate = today;
//            saveTodayData();
//            alertService.resetAlert();
//        }
//    }
//}