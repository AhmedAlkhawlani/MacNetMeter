package com.maknoon.ui;

import com.maknoon.model.AppNetUsage;
import com.maknoon.model.DisplayMode;
import com.maknoon.service.AlertService;
import com.maknoon.service.AppNetworkService;
import com.maknoon.service.CaffeinateService;
import com.maknoon.service.SystemInfoService;

import java.awt.*;
import java.util.List;

public class MenuBuilder {
    public static PopupMenu build(CaffeinateService caffeinate,
                                  AlertService alerts,
                                  SystemInfoService sysInfo,
                                  AppNetworkService appNetService,
                                  DisplayMode currentMode,
                                  Runnable onRefresh,
                                  Runnable onResetToday,
                                  java.util.function.Consumer<DisplayMode> onModeChanged) {
        PopupMenu menu = new PopupMenu();

        // 1. قسم التطبيقات الأكثر استهلاكاً للإنترنت الآن! (جديد 🌟)
        Menu appsMenu = new Menu("🌐 Active Apps Bandwidth");
        List<AppNetUsage> topApps = appNetService.getTopActiveApps(5);
        if (topApps.isEmpty()) {
            MenuItem emptyItem = new MenuItem("No active network traffic");
            emptyItem.setEnabled(false);
            appsMenu.add(emptyItem);
        } else {
            for (AppNetUsage app : topApps) {
                String speedSummary = String.format("%s: ↓ %s | ↑ %s",
                        app.getName(), formatSpeed(app.getRxSpeed()), formatSpeed(app.getTxSpeed()));
                MenuItem appItem = new MenuItem(speedSummary);
                appsMenu.add(appItem);
            }
        }
        menu.add(appsMenu);
        menu.addSeparator();

        // 2. مدة التشغيل (Uptime)
        MenuItem uptimeItem = new MenuItem("⏱ Uptime: " + sysInfo.getFormattedUptime());
        uptimeItem.setEnabled(false);
        menu.add(uptimeItem);
        menu.addSeparator();

        // 3. تفعيل / إيقاف الكافيين
        CheckboxMenuItem caffeinateItem = new CheckboxMenuItem("☕ Keep Mac Awake", caffeinate.isCaffeinated());
        caffeinateItem.addItemListener(e -> {
            caffeinate.toggle();
            onRefresh.run();
        });
        menu.add(caffeinateItem);
        menu.addSeparator();

        // 4. أوضاع العرض
        Menu viewMenu = new Menu("👁 Display Mode");
        for (DisplayMode mode : DisplayMode.values()) {
            CheckboxMenuItem item = new CheckboxMenuItem(mode.getTitle(), mode == currentMode);
            item.addItemListener(e -> onModeChanged.accept(mode));
            viewMenu.add(item);
        }
        menu.add(viewMenu);

        // 5. خيارات التنبيه وحد البيانات
        Menu alertMenu = new Menu("🔔 Data Limit Alert");
        CheckboxMenuItem disableItem = new CheckboxMenuItem("❌ Disabled", !alerts.isAlertActive());
        disableItem.addItemListener(e -> {
            alerts.disableAlert();
            onRefresh.run();
        });
        alertMenu.add(disableItem);
        alertMenu.addSeparator();

        long[] presetMB = {500, 1024, 2048, 5120};
        String[] titles = {"500 MB", "1 GB", "2 GB", "5 GB"};

        for (int i = 0; i < presetMB.length; i++) {
            long bytes = presetMB[i] * 1024 * 1024;
            boolean isSelected = alerts.getAlertLimitBytes() == bytes;
            CheckboxMenuItem presetItem = new CheckboxMenuItem(titles[i], isSelected);
            long mb = presetMB[i];
            presetItem.addItemListener(e -> {
                alerts.setAlertLimitMB(mb);
                onRefresh.run();
            });
            alertMenu.add(presetItem);
        }

        MenuItem customItem = new MenuItem("✏️ Custom Limit (in MB)...");
        customItem.addActionListener(e -> alerts.promptCustomLimit(onRefresh));
        alertMenu.addSeparator();
        alertMenu.add(customItem);
        menu.add(alertMenu);
        menu.addSeparator();

        // 6. زر تصفير الاستهلاك
        MenuItem resetToday = new MenuItem("🔄 Reset Today's Usage");
        resetToday.addActionListener(e -> onResetToday.run());
        menu.add(resetToday);
        menu.addSeparator();

        // 7. زر الخروج
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
//package com.maknoon.ui;
//
//import com.maknoon.model.DisplayMode;
//import com.maknoon.service.AlertService;
//import com.maknoon.service.CaffeinateService;
//import com.maknoon.service.SystemInfoService;
//
//import java.awt.*;
//
//public class MenuBuilder {
//    public static PopupMenu build(CaffeinateService caffeinate,
//                                  AlertService alerts,
//                                  SystemInfoService sysInfo,
//                                  DisplayMode currentMode,
//                                  Runnable onRefresh,
//                                  Runnable onResetToday,
//                                  java.util.function.Consumer<DisplayMode> onModeChanged) {
//        PopupMenu menu = new PopupMenu();
//
//        // 1. مدة التشغيل (Uptime)
//        MenuItem uptimeItem = new MenuItem("⏱ Uptime: " + sysInfo.getFormattedUptime());
//        uptimeItem.setEnabled(false);
//        menu.add(uptimeItem);
//        menu.addSeparator();
//
//        // 2. تفعيل / إيقاف الكافيين
//        CheckboxMenuItem caffeinateItem = new CheckboxMenuItem("☕ Keep Mac Awake", caffeinate.isCaffeinated());
//        caffeinateItem.addItemListener(e -> {
//            caffeinate.toggle();
//            onRefresh.run();
//        });
//        menu.add(caffeinateItem);
//        menu.addSeparator();
//
//        // 3. أوضاع العرض
//        Menu viewMenu = new Menu("👁 Display Mode");
//        for (DisplayMode mode : DisplayMode.values()) {
//            CheckboxMenuItem item = new CheckboxMenuItem(mode.getTitle(), mode == currentMode);
//            item.addItemListener(e -> onModeChanged.accept(mode));
//            viewMenu.add(item);
//        }
//        menu.add(viewMenu);
//
//        // 4. خيارات التنبيه وحد البيانات
//        Menu alertMenu = new Menu("🔔 Data Limit Alert");
//        CheckboxMenuItem disableItem = new CheckboxMenuItem("❌ Disabled", !alerts.isAlertActive());
//        disableItem.addItemListener(e -> {
//            alerts.disableAlert();
//            onRefresh.run();
//        });
//        alertMenu.add(disableItem);
//        alertMenu.addSeparator();
//
//        long[] presetMB = {500, 1024, 2048, 5120};
//        String[] titles = {"500 MB", "1 GB", "2 GB", "5 GB"};
//
//        for (int i = 0; i < presetMB.length; i++) {
//            long bytes = presetMB[i] * 1024 * 1024;
//            boolean isSelected = alerts.getAlertLimitBytes() == bytes;
//            CheckboxMenuItem presetItem = new CheckboxMenuItem(titles[i], isSelected);
//            long mb = presetMB[i];
//            presetItem.addItemListener(e -> {
//                alerts.setAlertLimitMB(mb);
//                onRefresh.run();
//            });
//            alertMenu.add(presetItem);
//        }
//
//        MenuItem customItem = new MenuItem("✏️ Custom Limit (in MB)...");
//        customItem.addActionListener(e -> alerts.promptCustomLimit(onRefresh));
//        alertMenu.addSeparator();
//        alertMenu.add(customItem);
//        menu.add(alertMenu);
//        menu.addSeparator();
//
//        // 5. زر تصفير الاستهلاك (Reset Today's Usage)
//        MenuItem resetToday = new MenuItem("🔄 Reset Today's Usage");
//        resetToday.addActionListener(e -> onResetToday.run());
//        menu.add(resetToday);
//        menu.addSeparator();
//
//        // 6. زر الخروج
//        MenuItem exit = new MenuItem("❌ Quit");
//        exit.addActionListener(e -> {
//            caffeinate.stop();
//            System.exit(0);
//        });
//        menu.add(exit);
//
//        return menu;
//    }
//}
