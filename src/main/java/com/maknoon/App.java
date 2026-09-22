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
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

public class App {
    private static final Preferences prefs = Preferences.userNodeForPackage(App.class);

    // استرجاع الإعدادات المحفوظة للأبد
    private static DisplayMode currentMode;
    private static boolean isMonitoringActive;
    private static boolean isCpuWidgetVisible;
    private static long todayBytes = 0;
    private static String savedDate = "";
    private static long previousRx = 0, previousTx = 0;
    private static long currentRxSpeed = 0, currentTxSpeed = 0;

    public static void main(String[] args) throws Exception {
        System.setProperty("apple.awt.UIElement", "true");
        if (!SystemTray.isSupported()) return;

        // تحميل الإعدادات السابقة
        loadAllPreferences();

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        CaffeinateService caffeinateService = new CaffeinateService();
        DataSaverService dataSaverService = new DataSaverService();
        AlertService alertService = new AlertService();
        SystemInfoService sysInfoService = new SystemInfoService(si);
        CpuMemoryService cpuMemoryService = new CpuMemoryService(si);
        AppNetworkService appNetworkService = new AppNetworkService(dataSaverService);

        SystemTray tray = SystemTray.getSystemTray();

        TrayIcon netWidget = new TrayIcon(WidgetRenderer.render(currentMode, "0 B", "0 B", todayBytes, alertService.getAlertLimitBytes(), false, false, !isMonitoringActive, 0, 0));
        netWidget.setImageAutoSize(false);

        TrayIcon cpuWidget = new TrayIcon(CpuWidgetRenderer.render(0, cpuMemoryService.getUsedMemory(), cpuMemoryService.getTotalMemory()));
        cpuWidget.setImageAutoSize(false);

        tray.add(netWidget);
        if (isCpuWidgetVisible) {
            tray.add(cpuWidget);
        }

        // تحديث واجهة القائمة
        Runnable refreshMenuAction = () -> updateMenu(netWidget, caffeinateService, alertService, sysInfoService, appNetworkService, dataSaverService, tray, cpuWidget);
        refreshMenuAction.run();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkMidnightReset(alertService);

                if (isMonitoringActive) {
                    List<NetworkIF> nets = hal.getNetworkIFs();
                    long curRx = 0, curTx = 0;
                    for (NetworkIF net : nets) {
                        net.updateAttributes();
                        curRx += net.getBytesRecv();
                        curTx += net.getBytesSent();
                    }

                    if (previousRx > 0 && previousTx > 0) {
                        currentRxSpeed = curRx - previousRx;
                        currentTxSpeed = curTx - previousTx;
                        todayBytes += (currentRxSpeed + currentTxSpeed);
                        saveTodayData();

                        alertService.checkUsage(todayBytes);

                        netWidget.setImage(WidgetRenderer.render(
                                currentMode,
                                formatSpeed(currentRxSpeed),
                                formatSpeed(currentTxSpeed),
                                todayBytes,
                                alertService.getAlertLimitBytes(),
                                caffeinateService.isCaffeinated(),
                                dataSaverService.isEnabled(),
                                false,
                                currentRxSpeed,
                                currentTxSpeed
                        ));
                    }
                    previousRx = curRx;
                    previousTx = curTx;
                } else {
                    // وضع الخمول والتعطيل
                    netWidget.setImage(WidgetRenderer.render(
                            currentMode, "0 B", "0 B", todayBytes,
                            alertService.getAlertLimitBytes(),
                            caffeinateService.isCaffeinated(),
                            dataSaverService.isEnabled(),
                            true, 0, 0
                    ));
                }

                // تحديث المعالج والرام إذا كان مفعلاً وظاهراً فقط
                if (isCpuWidgetVisible && isMonitoringActive) {
                    double cpu = cpuMemoryService.getCpuUsage();
                    long usedMem = cpuMemoryService.getUsedMemory();
                    long totalMem = cpuMemoryService.getTotalMemory();

                    cpuWidget.setImage(CpuWidgetRenderer.render(cpu, usedMem, totalMem));
                    cpuWidget.setPopupMenu(CpuMenuBuilder.build(cpuMemoryService));
                }

                // تحديث القائمة دورياً
                updateMenu(netWidget, caffeinateService, alertService, sysInfoService, appNetworkService, dataSaverService, tray, cpuWidget);
            }
        }, 0, 1000);
    }

    private static void updateMenu(TrayIcon netWidget, CaffeinateService caffeinate, AlertService alerts,
                                   SystemInfoService sysInfo, AppNetworkService appNet, DataSaverService dataSaver,
                                   SystemTray tray, TrayIcon cpuWidget) {
        netWidget.setPopupMenu(MenuBuilder.build(
                caffeinate, alerts, sysInfo, appNet, dataSaver, currentMode,
                isMonitoringActive, isCpuWidgetVisible, todayBytes, currentRxSpeed, currentTxSpeed,
                () -> updateMenu(netWidget, caffeinate, alerts, sysInfo, appNet, dataSaver, tray, cpuWidget),
                () -> {
                    todayBytes = 0;
                    saveTodayData();
                    alerts.resetAlert();
                    updateMenu(netWidget, caffeinate, alerts, sysInfo, appNet, dataSaver, tray, cpuWidget);
                },
                newMonitoringState -> {
                    isMonitoringActive = newMonitoringState;
                    prefs.putBoolean("monitoring_active", isMonitoringActive);
                    updateMenu(netWidget, caffeinate, alerts, sysInfo, appNet, dataSaver, tray, cpuWidget);
                },
                newCpuVisibleState -> {
                    isCpuWidgetVisible = newCpuVisibleState;
                    prefs.putBoolean("cpu_widget_visible", isCpuWidgetVisible);
                    if (isCpuWidgetVisible) {
                        try { tray.add(cpuWidget); } catch (Exception ignored) {}
                    } else {
                        tray.remove(cpuWidget);
                        notifyUser("تم تعطيل مراقب المعالج", "تم إخفاء وتعطيل مراقب المعالج والرام لتوفير المساحة وموارد الجهاز.");
                    }
                    updateMenu(netWidget, caffeinate, alerts, sysInfo, appNet, dataSaver, tray, cpuWidget);
                },
                newMode -> {
                    currentMode = newMode;
                    prefs.put("display_mode", currentMode.name());
                    updateMenu(netWidget, caffeinate, alerts, sysInfo, appNet, dataSaver, tray, cpuWidget);
                }
        ));
    }

    private static void notifyUser(String title, String message) {
        new Thread(() -> {
            try {
                String script = String.format("display notification \"%s\" with title \"%s\" sound name \"Pop\"", message, title);
                new ProcessBuilder("osascript", "-e", script).start();
            } catch (IOException ignored) {}
        }).start();
    }

    private static String formatSpeed(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        return String.format("%.1f %c", bytes / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
    }

    private static void loadAllPreferences() {
        savedDate = prefs.get("date", LocalDate.now().toString());
        if (savedDate.equals(LocalDate.now().toString())) {
            todayBytes = prefs.getLong("bytes", 0);
        }
        currentMode = DisplayMode.valueOf(prefs.get("display_mode", DisplayMode.FULL.name()));
        isMonitoringActive = prefs.getBoolean("monitoring_active", true);
        isCpuWidgetVisible = prefs.getBoolean("cpu_widget_visible", true);
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