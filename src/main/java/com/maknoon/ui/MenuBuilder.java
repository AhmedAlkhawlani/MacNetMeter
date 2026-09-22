package com.maknoon.ui;

import com.maknoon.model.DisplayMode;
import com.maknoon.service.AlertService;
import com.maknoon.service.CaffeinateService;
import com.maknoon.service.SystemInfoService;

import java.awt.*;

public class MenuBuilder {
    public static PopupMenu build(CaffeinateService caffeinate,
                                  AlertService alerts,
                                  SystemInfoService sysInfo,
                                  DisplayMode currentMode,
                                  Runnable onRefresh,
                                  Runnable onResetToday,
                                  java.util.function.Consumer<DisplayMode> onModeChanged) {
        PopupMenu menu = new PopupMenu();

        // 1. مدة التشغيل (Uptime)
        MenuItem uptimeItem = new MenuItem("⏱ Uptime: " + sysInfo.getFormattedUptime());
        uptimeItem.setEnabled(false);
        menu.add(uptimeItem);
        menu.addSeparator();

        // 2. تفعيل / إيقاف الكافيين
        CheckboxMenuItem caffeinateItem = new CheckboxMenuItem("☕ Keep Mac Awake", caffeinate.isCaffeinated());
        caffeinateItem.addItemListener(e -> {
            caffeinate.toggle();
            onRefresh.run();
        });
        menu.add(caffeinateItem);
        menu.addSeparator();

        // 3. أوضاع العرض
        Menu viewMenu = new Menu("👁 Display Mode");
        for (DisplayMode mode : DisplayMode.values()) {
            CheckboxMenuItem item = new CheckboxMenuItem(mode.getTitle(), mode == currentMode);
            item.addItemListener(e -> onModeChanged.accept(mode));
            viewMenu.add(item);
        }
        menu.add(viewMenu);

        // 4. خيارات التنبيه وحد البيانات
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

        // 5. زر تصفير الاستهلاك (Reset Today's Usage)
        MenuItem resetToday = new MenuItem("🔄 Reset Today's Usage");
        resetToday.addActionListener(e -> onResetToday.run());
        menu.add(resetToday);
        menu.addSeparator();

        // 6. زر الخروج
        MenuItem exit = new MenuItem("❌ Quit");
        exit.addActionListener(e -> {
            caffeinate.stop();
            System.exit(0);
        });
        menu.add(exit);

        return menu;
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
//                                  java.util.function.Consumer<DisplayMode> onModeChanged) {
//        PopupMenu menu = new PopupMenu();
//
//        // 1. مدة التشغيل (Uptime)
//        MenuItem uptimeItem = new MenuItem("⏱ Uptime: " + sysInfo.getFormattedUptime());
//        uptimeItem.setEnabled(false);
//        menu.add(uptimeItem);
//        menu.addSeparator();
//
//        // 2. زر الكافيين مع علامة الصح ✔ عند التفعيل
//        CheckboxMenuItem caffeinateItem = new CheckboxMenuItem("☕ Keep Mac Awake", caffeinate.isCaffeinated());
//        caffeinateItem.addItemListener(e -> {
//            caffeinate.toggle();
//            onRefresh.run();
//        });
//
//        menu.add(caffeinateItem);
//        menu.addSeparator();
//
//        // 3. اختيار وضع العرض (مع علامة صح أمام الوضع النشط حالياً)
//        Menu viewMenu = new Menu("👁 Display Mode");
//        for (DisplayMode mode : DisplayMode.values()) {
//            CheckboxMenuItem item = new CheckboxMenuItem(mode.getTitle(), mode == currentMode);
//            item.addItemListener(e -> onModeChanged.accept(mode));
//            viewMenu.add(item);
//        }
//        menu.add(viewMenu);
//
//
//        // 4. خيارات التنبيه وحد البيانات
//        Menu alertMenu = new Menu("🔔 Data Limit Alert");
//
//        // خيار التعطيل
//        CheckboxMenuItem disableItem = new CheckboxMenuItem("❌ Disabled", !alerts.isAlertActive());
//        disableItem.addItemListener(e -> {
//            alerts.disableAlert();
//            onRefresh.run();
//        });
//        alertMenu.add(disableItem);
//        alertMenu.addSeparator();
//
//        // خيارات جاهزة (500MB, 1GB, 2GB, 5GB)
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
//        // خيار الإدخال المخصص
//        MenuItem customItem = new MenuItem("✏️ Custom Limit (in MB)...");
//        customItem.addActionListener(e -> alerts.promptCustomLimit(onRefresh));
//        alertMenu.addSeparator();
//        alertMenu.add(customItem);
//
//        menu.add(alertMenu);
//        menu.addSeparator();
//
//        // 5. زر الخروج
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
////package com.maknoon.ui;
////
////import com.maknoon.model.DisplayMode;
////import com.maknoon.service.AlertService;
////import com.maknoon.service.CaffeinateService;
////import com.maknoon.service.SystemInfoService;
////
////import java.awt.*;
////
////public class MenuBuilder {
////    public static PopupMenu build(CaffeinateService caffeinate,
////                                  AlertService alerts,
////                                  SystemInfoService sysInfo,
////                                  Runnable onRefresh,
////                                  java.util.function.Consumer<DisplayMode> onModeChanged) {
////        PopupMenu menu = new PopupMenu();
////
////        // 1. مدة التشغيل (Uptime)
////        MenuItem uptimeItem = new MenuItem("⏱ Uptime: " + sysInfo.getFormattedUptime());
////        uptimeItem.setEnabled(false);
////        menu.add(uptimeItem);
////        menu.addSeparator();
////
////        // 2. تفعيل / إيقاف الكافيين
////        System.out.println("=الكافيين"+caffeinate.isCaffeinated());
////        MenuItem caffeinateItem = new MenuItem(caffeinate.isCaffeinated() ? "☕ Deactivate Caffeinate" : "☕ Keep Mac Awake");
////        caffeinateItem.addActionListener(e -> {
////            caffeinate.toggle();
////            onRefresh.run();
////        });
////        menu.add(caffeinateItem);
////        menu.addSeparator();
////
////        // 3. اختيار وضع العرض (Display Mode)
////        Menu viewMenu = new Menu("👁 Display Mode");
////        for (DisplayMode mode : DisplayMode.values()) {
////            MenuItem item = new MenuItem(mode.getTitle());
////            item.addActionListener(e -> onModeChanged.accept(mode));
////            viewMenu.add(item);
////        }
////        menu.add(viewMenu);
////
////        // 4. خيارات التنبيه عند الاستهلاك
////        Menu alertMenu = new Menu("🔔 Data Limit Alert");
////        int[] limits = {1, 2, 5, 10};
////        for (int gb : limits) {
////            MenuItem limitItem = new MenuItem("Alert at " + gb + " GB");
////            limitItem.addActionListener(e -> alerts.setAlertLimitGB(gb));
////            alertMenu.add(limitItem);
////        }
////        menu.add(alertMenu);
////        menu.addSeparator();
////
////        // 5. زر الخروج
////        MenuItem exit = new MenuItem("❌ Quit");
////        exit.addActionListener(e -> {
////            caffeinate.stop(); // التأكد من إيقاف كافيين قبل الخروج
////            System.exit(0);
////        });
////        menu.add(exit);
////
////        return menu;
////    }
////}