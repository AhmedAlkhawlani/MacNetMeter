package com.maknoon.ui;

import com.maknoon.model.AppNetUsage;
import com.maknoon.model.DisplayMode;
import com.maknoon.service.*;

import java.awt.*;
import java.util.List;
import java.util.Set;

public class MenuBuilder {
    public static PopupMenu build(CaffeinateService caffeinate,
                                  AlertService alerts,
                                  SystemInfoService sysInfo,
                                  AppNetworkService appNetService,
                                  DataSaverService dataSaver,
                                  DisplayMode currentMode,
                                  boolean isMonitoringActive,
                                  boolean isCpuWidgetVisible,
                                  long todayBytes,
                                  long rxSpeed,
                                  long txSpeed,
                                  Runnable onRefresh,
                                  Runnable onResetToday,
                                  java.util.function.Consumer<Boolean> onToggleMonitoring,
                                  java.util.function.Consumer<Boolean> onToggleCpuWidget,
                                  java.util.function.Consumer<DisplayMode> onModeChanged) {
        PopupMenu menu = new PopupMenu();

        // 📌 1. بطاقة معلومات شاملة تظهر كل ما هو مخفي في البار!
        MenuItem header = new MenuItem("── 📊 Live Status Overview ──");
        header.setEnabled(false);
        menu.add(header);

        MenuItem speedInfo = new MenuItem(String.format("⚡ Speeds: ↓ %s | ↑ %s", formatSpeed(rxSpeed), formatSpeed(txSpeed)));
        speedInfo.setEnabled(false);
        menu.add(speedInfo);

        MenuItem todayInfo = new MenuItem("📅 Today Usage: " + WidgetRenderer.formatData(todayBytes));
        todayInfo.setEnabled(false);
        menu.add(todayInfo);

        if (alerts.isAlertActive()) {
            double pct = (double) todayBytes / alerts.getAlertLimitBytes() * 100;
            String limitMsg = (todayBytes >= alerts.getAlertLimitBytes())
                    ? "⚠️ EXCEEDED!"
                    : String.format("%.0f%% of %s", pct, WidgetRenderer.formatData(alerts.getAlertLimitBytes()));
            MenuItem limitInfo = new MenuItem("🔔 Target Limit: " + limitMsg);
            limitInfo.setEnabled(false);
            menu.add(limitInfo);
        }
        menu.addSeparator();

        // 2. تفعيل / إيقاف المراقبة مؤقتاً (Pause / Resume)
        CheckboxMenuItem monitorToggle = new CheckboxMenuItem("🟢 Monitoring Active", isMonitoringActive);
        monitorToggle.addItemListener(e -> onToggleMonitoring.accept(!isMonitoringActive));
        menu.add(monitorToggle);

        // 3. إظهار / إخفاء مراقب المعالج والرام لتوفير المساحة
        CheckboxMenuItem cpuToggle = new CheckboxMenuItem("💻 Show CPU & RAM Monitor", isCpuWidgetVisible);
        cpuToggle.addItemListener(e -> onToggleCpuWidget.accept(!isCpuWidgetVisible));
        menu.add(cpuToggle);
        menu.addSeparator();

        // 4. وضع الهوتسبوت والكافيين
        CheckboxMenuItem dataSaverItem = new CheckboxMenuItem("🛡️ Hotspot Data Saver", dataSaver.isEnabled());
        dataSaverItem.addItemListener(e -> {
            dataSaver.toggle();
            onRefresh.run();
        });
        menu.add(dataSaverItem);

        CheckboxMenuItem caffeinateItem = new CheckboxMenuItem("☕ Keep Mac Awake", caffeinate.isCaffeinated());
        caffeinateItem.addItemListener(e -> {
            caffeinate.toggle();
            onRefresh.run();
        });
        menu.add(caffeinateItem);
        menu.addSeparator();

        // 5. أوضاع العرض في البار
        Menu viewMenu = new Menu("👁 Display Mode");
        for (DisplayMode mode : DisplayMode.values()) {
            CheckboxMenuItem item = new CheckboxMenuItem(mode.getTitle(), mode == currentMode);
            item.addItemListener(e -> onModeChanged.accept(mode));
            viewMenu.add(item);
        }
        menu.add(viewMenu);

        // 6. البرامج اللحظية وقواعد الهوتسبوت
        Menu appsMenu = new Menu("🌐 Live Apps Bandwidth");
        List<AppNetUsage> topApps = appNetService.getTopActiveApps(8);
        if (topApps.isEmpty()) {
            MenuItem emptyItem = new MenuItem("No active apps");
            emptyItem.setEnabled(false);
            appsMenu.add(emptyItem);
        } else {
            for (AppNetUsage app : topApps) {
                boolean isBlocked = dataSaver.isAppBlocked(app.getName());
                String title = String.format("%s%s (↓ %s)", isBlocked ? "🔴 " : "🟢 ", app.getName(), formatSpeed(app.getRxSpeed()));
                Menu appSubMenu = new Menu(title);
                MenuItem infoItem = new MenuItem("Today Total: " + WidgetRenderer.formatData(app.getTodayTotalBytes()));
                infoItem.setEnabled(false);
                appSubMenu.add(infoItem);

                MenuItem toggleBlock = new MenuItem(isBlocked ? "🟢 Allow on Hotspot" : "🔴 Block on Hotspot");
                toggleBlock.addActionListener(e -> {
                    if (isBlocked) dataSaver.unblockApp(app.getName());
                    else dataSaver.blockApp(app.getName());
                    onRefresh.run();
                });
                appSubMenu.add(toggleBlock);
                appsMenu.add(appSubMenu);
            }
        }
        menu.add(appsMenu);

        Menu rulesMenu = new Menu("🛡️ Manage Hotspot Rules");
        Set<String> blocked = dataSaver.getBlockedApps();
        if (blocked.isEmpty()) {
            MenuItem noRules = new MenuItem("No apps blocked yet");
            noRules.setEnabled(false);
            rulesMenu.add(noRules);
        } else {
            for (String appName : blocked) {
                Menu ruleSub = new Menu("🔴 " + appName + " [BLOCKED]");
                MenuItem unblock = new MenuItem("🟢 Unblock (Allow App)");
                unblock.addActionListener(e -> {
                    dataSaver.unblockApp(appName);
                    onRefresh.run();
                });
                ruleSub.add(unblock);
                rulesMenu.add(ruleSub);
            }
        }
        menu.add(rulesMenu);
        menu.addSeparator();

        // 7. التنبيهات وحد البيانات
        Menu alertMenu = new Menu("🔔 Data Limit Alert");
        CheckboxMenuItem disableItem = new CheckboxMenuItem("❌ Disabled", !alerts.isAlertActive());
        disableItem.addItemListener(e -> {
            alerts.disableAlert();
            onRefresh.run();
        });
        alertMenu.add(disableItem);
        long[] presetMB = {10, 500, 1024, 2048, 5120}; // أضفنا 10MB للتجربة السريعة!
        String[] titles = {"10 MB (Test)", "500 MB", "1 GB", "2 GB", "5 GB"};
        for (int i = 0; i < presetMB.length; i++) {
            long bytes = presetMB[i] * 1024 * 1024;
            CheckboxMenuItem presetItem = new CheckboxMenuItem(titles[i], alerts.getAlertLimitBytes() == bytes);
            long mb = presetMB[i];
            presetItem.addItemListener(e -> {
                alerts.setAlertLimitMB(mb);
                onRefresh.run();
            });
            alertMenu.add(presetItem);
        }
        MenuItem customItem = new MenuItem("✏️ Custom Limit (in MB)...");
        customItem.addActionListener(e -> alerts.promptCustomLimit(onRefresh));
        alertMenu.add(customItem);
        menu.add(alertMenu);

        // 8. تصفير العداد والخروج
        MenuItem resetToday = new MenuItem("🔄 Reset Today's Usage");
        resetToday.addActionListener(e -> onResetToday.run());
        menu.add(resetToday);
        menu.addSeparator();

        MenuItem exit = new MenuItem("❌ Quit");
        exit.addActionListener(e -> {
            caffeinate.stop();
            System.exit(0);
        });
        menu.add(exit);

        return menu;
    }

    private static String formatSpeed(long bytes) {
        if (bytes < 1024) return bytes + " B/s";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        return String.format("%.1f %c/s", bytes / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
    }
}