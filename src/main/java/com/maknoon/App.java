package com.maknoon;

import com.maknoon.model.DisplayMode;
import com.maknoon.service.*;
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
    private static DisplayMode currentMode = DisplayMode.SPEEDS_ONLY;
    private static long todayBytes = 0;
    private static String savedDate = "";
    private static long previousRx = 0, previousTx = 0;

    public static void main(String[] args) throws Exception {
        System.setProperty("apple.awt.UIElement", "true");
        if (!SystemTray.isSupported()) return;

        loadTodayData();

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        // 1. تشغيل الخدمات
        CaffeinateService caffeinateService = new CaffeinateService();
        DataSaverService dataSaverService = new DataSaverService(); // خدمة توفير الباقة 🛡️
        AlertService alertService = new AlertService();
        SystemInfoService sysInfoService = new SystemInfoService(si);
        CpuMemoryService cpuMemoryService = new CpuMemoryService(si);
        AppNetworkService appNetworkService = new AppNetworkService(dataSaverService);

        SystemTray tray = SystemTray.getSystemTray();

        TrayIcon netWidget = new TrayIcon(WidgetRenderer.render(currentMode, "0 B", "0 B", todayBytes, alertService.getAlertLimitBytes(), false, false, 0, 0));
        netWidget.setImageAutoSize(false);

        TrayIcon cpuWidget = new TrayIcon(CpuWidgetRenderer.render(0, cpuMemoryService.getUsedMemory(), cpuMemoryService.getTotalMemory()));
        cpuWidget.setImageAutoSize(false);

        Runnable resetTodayAction = () -> {
            todayBytes = 0;
            saveTodayData();
            alertService.resetAlert();
            refreshNet(netWidget, caffeinateService, alertService, sysInfoService, appNetworkService, dataSaverService, null);
        };

        refreshNet(netWidget, caffeinateService, alertService, sysInfoService, appNetworkService, dataSaverService, resetTodayAction);

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
                            dataSaverService.isEnabled(),
                            rxSpeed,
                            txSpeed
                    ));
                }
                previousRx = curRx;
                previousTx = curTx;

                // تحديث قائمة الشبكة
                refreshNet(netWidget, caffeinateService, alertService, sysInfoService, appNetworkService, dataSaverService, resetTodayAction);

                // --- تحديث المعالج والرام ---
                double cpu = cpuMemoryService.getCpuUsage();
                long usedMem = cpuMemoryService.getUsedMemory();
                long totalMem = cpuMemoryService.getTotalMemory();

                cpuWidget.setImage(CpuWidgetRenderer.render(cpu, usedMem, totalMem));
                cpuWidget.setPopupMenu(CpuMenuBuilder.build(cpuMemoryService));
            }
        }, 0, 1000);
    }

    private static void refreshNet(TrayIcon widget, CaffeinateService caffeinate, AlertService alerts,
                                   SystemInfoService sysInfo, AppNetworkService appNet, DataSaverService dataSaver, Runnable onResetToday) {
        widget.setPopupMenu(MenuBuilder.build(caffeinate, alerts, sysInfo, appNet, dataSaver, currentMode,
                () -> refreshNet(widget, caffeinate, alerts, sysInfo, appNet, dataSaver, onResetToday),
                onResetToday,
                m -> {
                    currentMode = m;
                    refreshNet(widget, caffeinate, alerts, sysInfo, appNet, dataSaver, onResetToday);
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