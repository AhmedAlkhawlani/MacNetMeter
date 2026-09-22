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
                                  Runnable onRefresh,
                                  Runnable onResetToday,
                                  java.util.function.Consumer<DisplayMode> onModeChanged) {
        PopupMenu menu = new PopupMenu();

        // 1. قسم النشاط اللحظي للبرامج التي تسحب نت الآن
        Menu appsMenu = new Menu("🌐 Live Apps Bandwidth");
        List<AppNetUsage> topApps = appNetService.getTopActiveApps(8);
        if (topApps.isEmpty()) {
            MenuItem emptyItem = new MenuItem("No active apps");
            emptyItem.setEnabled(false);
            appsMenu.add(emptyItem);
        } else {
            for (AppNetUsage app : topApps) {
                boolean isBlocked = dataSaver.isAppBlocked(app.getName());
                String icon = isBlocked ? "🔴 " : "🟢 ";
                String title = String.format("%s%s (↓ %s)", icon, app.getName(), formatSpeed(app.getRxSpeed()));
                Menu appSubMenu = new Menu(title);

                MenuItem infoItem = new MenuItem("Today Total: " + WidgetRenderer.formatData(app.getTodayTotalBytes()));
                infoItem.setEnabled(false);
                appSubMenu.add(infoItem);

                // زر التحويل بين الحظر والسماح
                if (isBlocked) {
                    MenuItem unblockItem = new MenuItem("🟢 Allow on Hotspot");
                    unblockItem.addActionListener(e -> {
                        dataSaver.unblockApp(app.getName());
                        onRefresh.run();
                    });
                    appSubMenu.add(unblockItem);
                } else {
                    MenuItem blockItem = new MenuItem("🔴 Block on Hotspot");
                    blockItem.addActionListener(e -> {
                        dataSaver.blockApp(app.getName());
                        onRefresh.run();
                    });
                    appSubMenu.add(blockItem);
                }

                appsMenu.add(appSubMenu);
            }
        }
        menu.add(appsMenu);

        // 2. القسم الدائم: إدارة قواعد الحظر (لا يختفي أبداً حتى لو أغلقت فايرفوكس!)
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

        // 3. تفعيل / إيقاف وضع توفير الهوتسبوت
        CheckboxMenuItem dataSaverItem = new CheckboxMenuItem("🛡️ Hotspot Data Saver", dataSaver.isEnabled());
        dataSaverItem.addItemListener(e -> {
            dataSaver.toggle();
            onRefresh.run();
        });
        menu.add(dataSaverItem);

        // 4. الكافيين
        CheckboxMenuItem caffeinateItem = new CheckboxMenuItem("☕ Keep Mac Awake", caffeinate.isCaffeinated());
        caffeinateItem.addItemListener(e -> {
            caffeinate.toggle();
            onRefresh.run();
        });
        menu.add(caffeinateItem);
        menu.addSeparator();

        // 5. أوضاع العرض
        Menu viewMenu = new Menu("👁 Display Mode");
        for (DisplayMode mode : DisplayMode.values()) {
            CheckboxMenuItem item = new CheckboxMenuItem(mode.getTitle(), mode == currentMode);
            item.addItemListener(e -> onModeChanged.accept(mode));
            viewMenu.add(item);
        }
        menu.add(viewMenu);

        // 6. التنبيهات
        Menu alertMenu = new Menu("🔔 Data Limit Alert");
        CheckboxMenuItem disableItem = new CheckboxMenuItem("❌ Disabled", !alerts.isAlertActive());
        disableItem.addItemListener(e -> {
            alerts.disableAlert();
            onRefresh.run();
        });
        alertMenu.add(disableItem);
        long[] presetMB = {500, 1024, 2048, 5120};
        String[] titles = {"500 MB", "1 GB", "2 GB", "5 GB"};
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
        menu.addSeparator();

        // 7. تصفير العداد لليوم
        MenuItem resetToday = new MenuItem("🔄 Reset Today's Usage");
        resetToday.addActionListener(e -> onResetToday.run());
        menu.add(resetToday);
        menu.addSeparator();

        // 8. خروج
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
//import com.maknoon.model.AppNetUsage;
//import com.maknoon.model.DisplayMode;
//import com.maknoon.service.*;
//
//import java.awt.*;
//import java.util.List;
//
//public class MenuBuilder {
//    public static PopupMenu build(CaffeinateService caffeinate,
//                                  AlertService alerts,
//                                  SystemInfoService sysInfo,
//                                  AppNetworkService appNetService,
//                                  DataSaverService dataSaver,
//                                  DisplayMode currentMode,
//                                  Runnable onRefresh,
//                                  Runnable onResetToday,
//                                  java.util.function.Consumer<DisplayMode> onModeChanged) {
//        PopupMenu menu = new PopupMenu();
//
//        // 1. قسم التطبيقات واستهلاكها لليوم + إيقافها بنقرة واحدة
//        Menu appsMenu = new Menu("🌐 Apps Bandwidth & Usage");
//        List<AppNetUsage> topApps = appNetService.getTopActiveApps(8);
//        if (topApps.isEmpty()) {
//            MenuItem emptyItem = new MenuItem("Analyzing network traffic...");
//            emptyItem.setEnabled(false);
//            appsMenu.add(emptyItem);
//        } else {
//            for (AppNetUsage app : topApps) {
//                // عنوان التطبيق يوضح السرعة واستهلاك اليوم
//                String title = String.format("%s — Today: %s", app.getName(), WidgetRenderer.formatData(app.getTodayTotalBytes()));
//                Menu appSubMenu = new Menu(title);
//
//                MenuItem liveSpeed = new MenuItem(String.format("Live: ↓ %s | ↑ %s",
//                        formatSpeed(app.getRxSpeed()), formatSpeed(app.getTxSpeed())));
//                liveSpeed.setEnabled(false);
//
//                MenuItem stopItem = new MenuItem("🛑 Stop App (Cut Connection)");
//                stopItem.addActionListener(e -> {
//                    appNetService.stopApp(app.getPid());
//                    onRefresh.run();
//                });
//
//                appSubMenu.add(liveSpeed);
//                appSubMenu.add(stopItem);
//                appsMenu.add(appSubMenu);
//            }
//        }
//        menu.add(appsMenu);
//        menu.addSeparator();
//
//        // 2. وضع توفير باقة الهوتسبوت (Data Saver Mode)
//        CheckboxMenuItem dataSaverItem = new CheckboxMenuItem("🛡️ Hotspot Data Saver", dataSaver.isEnabled());
//        dataSaverItem.addItemListener(e -> {
//            dataSaver.toggle();
//            onRefresh.run();
//        });
//        menu.add(dataSaverItem);
//
//        // 3. منع الماك من النوم (Caffeinate)
//        CheckboxMenuItem caffeinateItem = new CheckboxMenuItem("☕ Keep Mac Awake", caffeinate.isCaffeinated());
//        caffeinateItem.addItemListener(e -> {
//            caffeinate.toggle();
//            onRefresh.run();
//        });
//        menu.add(caffeinateItem);
//        menu.addSeparator();
//
//        // 4. مدة التشغيل (Uptime)
//        MenuItem uptimeItem = new MenuItem("⏱ Uptime: " + sysInfo.getFormattedUptime());
//        uptimeItem.setEnabled(false);
//        menu.add(uptimeItem);
//        menu.addSeparator();
//
//        // 5. أوضاع العرض
//        Menu viewMenu = new Menu("👁 Display Mode");
//        for (DisplayMode mode : DisplayMode.values()) {
//            CheckboxMenuItem item = new CheckboxMenuItem(mode.getTitle(), mode == currentMode);
//            item.addItemListener(e -> onModeChanged.accept(mode));
//            viewMenu.add(item);
//        }
//        menu.add(viewMenu);
//
//        // 6. خيارات التنبيه وحد البيانات
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
//        for (int i = 0; i < presetMB.length; i++) {
//            long bytes = presetMB[i] * 1024 * 1024;
//            CheckboxMenuItem presetItem = new CheckboxMenuItem(titles[i], alerts.getAlertLimitBytes() == bytes);
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
//        // 7. زر تصفير الاستهلاك لليوم
//        MenuItem resetToday = new MenuItem("🔄 Reset Today's Usage");
//        resetToday.addActionListener(e -> onResetToday.run());
//        menu.add(resetToday);
//        menu.addSeparator();
//
//        // 8. زر الخروج
//        MenuItem exit = new MenuItem("❌ Quit");
//        exit.addActionListener(e -> {
//            caffeinate.stop();
//            System.exit(0);
//        });
//        menu.add(exit);
//
//        return menu;
//    }
//
//    private static String formatSpeed(long bytes) {
//        if (bytes < 1024) return bytes + " B/s";
//        int exp = (int) (Math.log(bytes) / Math.log(1024));
//        return String.format("%.1f %c/s", bytes / Math.pow(1024, exp), "KMGTPE".charAt(exp - 1));
//    }
//}