package com.maknoon;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.awt.*;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.prefs.Preferences;

public class AppFinal {
    private static long previousRxBytes = 0;
    private static long previousTxBytes = 0;

    private static final Preferences prefs = Preferences.userNodeForPackage(AppFinal.class);
    private static long todayBytes = 0;
    private static String savedDate = "";

    private static volatile int currentTheme = 1;

    public static void main(String[] args) throws Exception {
        System.setProperty("apple.awt.UIElement", "true");

        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        loadTodayData();

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        SystemTray tray = SystemTray.getSystemTray();

        TrayIcon unifiedWidget = new TrayIcon(createCrispWidget("0 B", "0 B", todayBytes, 0, 0));

        // السر هنا: جعلناها false لكي لا يعصر الماك العرض ويجعله مربعاً!
        unifiedWidget.setImageAutoSize(false);

        unifiedWidget.setPopupMenu(createMenu(() -> {
            unifiedWidget.setImage(createCrispWidget("0 B", "0 B", todayBytes, 0, 0));
        }));

        tray.add(unifiedWidget);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkDayReset();

                List<NetworkIF> networkIFs = hal.getNetworkIFs();
                long currentRx = 0;
                long currentTx = 0;

                for (NetworkIF net : networkIFs) {
                    net.updateAttributes();
                    currentRx += net.getBytesRecv();
                    currentTx += net.getBytesSent();
                }

                if (previousRxBytes > 0 && previousTxBytes > 0) {
                    long rxSpeed = currentRx - previousRxBytes;
                    long txSpeed = currentTx - previousTxBytes;

                    todayBytes += (rxSpeed + txSpeed);
                    saveTodayData();

                    unifiedWidget.setImage(createCrispWidget(
                            formatSpeed(rxSpeed),
                            formatSpeed(txSpeed),
                            todayBytes,
                            rxSpeed,
                            txSpeed
                    ));

                    unifiedWidget.setToolTip("Today's Total Usage: " + formatData(todayBytes));
                }

                previousRxBytes = currentRx;
                previousTxBytes = currentTx;
            }
        }, 0, 1000);
    }

    // تقنية دمج دقة 1x مع 3x لمنع الضغط وللحصول على حجم طبيعي وخط حاد
    private static Image createCrispWidget(String down, String up, long todayTotal, long rxSpeed, long txSpeed) {
        int width = 175; // عرض واسع ومريح جداً للأرقام
        int height = 22; // ارتفاع شريط الماك

        BufferedImage img1x = renderWidget(width, height, 1, down, up, todayTotal, rxSpeed, txSpeed);
        BufferedImage img3x = renderWidget(width, height, 3, down, up, todayTotal, rxSpeed, txSpeed);

        // الماك سيعرف أن العرض 175 وسيستخدم نسخة الـ 3x لشاشات الريتنا تلقائياً
        return new BaseMultiResolutionImage(img1x, img3x);
    }

    private static BufferedImage renderWidget(int logicalWidth, int logicalHeight, int scale,
                                              String down, String up, long todayTotal,
                                              long rxSpeed, long txSpeed) {
        BufferedImage image = new BufferedImage(logicalWidth * scale, logicalHeight * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.scale(scale, scale);

        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, logicalWidth, logicalHeight);

        // خط النظام مقاس 12 واضح وطبيعي تماماً
        Font font = new Font(".AppleSystemUIFont", Font.BOLD, 12);
        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics(font);
        int y = ((logicalHeight - fm.getHeight()) / 2) + fm.getAscent();

        // 1. سرعة التحميل (مساحة واسعة من البداية)
        g2d.setColor(determineColor(rxSpeed, true));
        g2d.drawString("↓ " + down, 5, y);

        // 2. سرعة الرفع (تبدأ بعد التحميل بمساحة كافية)
        g2d.setColor(determineColor(txSpeed, false));
        g2d.drawString("↑ " + up, 65, y);

        // 3. فاصل أنيق
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.drawString("│", 120, y);

        // 4. استهلاك اليوم
        g2d.setColor(new Color(220, 220, 220));
        g2d.drawString(formatData(todayTotal), 130, y);

        g2d.dispose();
        return image;
    }

    private static String formatSpeed(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %c", bytes / Math.pow(1024, exp), pre);
    }

    private static String formatData(long bytes) {
        if (bytes < 1024 * 1024) return String.format("%.0f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private static Color determineColor(long bytes, boolean isDownload) {
        if (bytes == 0) return new Color(255, 255, 255, 100);

        switch (currentTheme) {
            case 1: // نيون سايبربانك
                return isDownload ? new Color(0, 245, 255) : new Color(255, 50, 150);
            case 2: // تفاعلي
                if (bytes < 500 * 1024) return new Color(140, 255, 140);
                if (bytes < 5 * 1024 * 1024) return new Color(255, 215, 0);
                return new Color(255, 75, 75);
            case 0: // أبل كلاسيك
            default:
                return Color.WHITE;
        }
    }

    private static void loadTodayData() {
        savedDate = prefs.get("saved_date", LocalDate.now().toString());
        if (savedDate.equals(LocalDate.now().toString())) {
            todayBytes = prefs.getLong("today_bytes", 0);
        } else {
            todayBytes = 0;
            saveTodayData();
        }
    }

    private static void saveTodayData() {
        prefs.put("saved_date", LocalDate.now().toString());
        prefs.putLong("today_bytes", todayBytes);
    }

    private static void checkDayReset() {
        String today = LocalDate.now().toString();
        if (!today.equals(savedDate)) {
            todayBytes = 0;
            savedDate = today;
            saveTodayData();
        }
    }

    private static PopupMenu createMenu(Runnable onThemeChanged) {
        PopupMenu menu = new PopupMenu();

        MenuItem header = new MenuItem("📊 Daily Network Monitor");
        header.setEnabled(false);

        Menu themesMenu = new Menu("🎨 Themes");
        MenuItem themeNeon = new MenuItem("Neon Cyberpunk");
        MenuItem themeTraffic = new MenuItem("Traffic Adaptive");
        MenuItem themeApple = new MenuItem("Apple Minimalist");

        themeNeon.addActionListener(e -> { currentTheme = 1; onThemeChanged.run(); });
        themeTraffic.addActionListener(e -> { currentTheme = 2; onThemeChanged.run(); });
        themeApple.addActionListener(e -> { currentTheme = 0; onThemeChanged.run(); });

        themesMenu.add(themeNeon);
        themesMenu.add(themeTraffic);
        themesMenu.add(themeApple);

        MenuItem resetToday = new MenuItem("🔄 Reset Today's Usage");
        resetToday.addActionListener(e -> {
            todayBytes = 0;
            saveTodayData();
            onThemeChanged.run();
        });

        MenuItem exit = new MenuItem("❌ Quit");
        exit.addActionListener(e -> System.exit(0));

        menu.add(header);
        menu.addSeparator();
        menu.add(themesMenu);
        menu.add(resetToday);
        menu.addSeparator();
        menu.add(exit);

        return menu;
    }
}
//package com.maknoon;
//
//import oshi.SystemInfo;
//import oshi.hardware.HardwareAbstractionLayer;
//import oshi.hardware.NetworkIF;
//
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.prefs.Preferences;
//
//public class App {
//    private static long previousRxBytes = 0;
//    private static long previousTxBytes = 0;
//
//    // حفظ استهلاك اليوم في ذاكرة النظام (حتى لا تضيع إذا أغلقت البرنامج)
//    private static final Preferences prefs = Preferences.userNodeForPackage(App.class);
//    private static long todayBytes = 0;
//    private static String savedDate = "";
//
//    // الثيمات: 1: نيون سايبربانك، 2: ألوان تفاعلية، 0: أبل كلاسيك
//    private static volatile int currentTheme = 1;
//
//    public static void main(String[] args) throws Exception {
//        System.setProperty("apple.awt.UIElement", "true");
//
//        if (!SystemTray.isSupported()) {
//            System.out.println("SystemTray is not supported");
//            return;
//        }
//
//        // استرجاع بيانات اليوم المخزنة مسبقاً
//        loadTodayData();
//
//        SystemInfo si = new SystemInfo();
//        HardwareAbstractionLayer hal = si.getHardware();
//        SystemTray tray = SystemTray.getSystemTray();
//
//        // أيقونة واحدة موحدة تشبه ويدجت التاريخ
//        TrayIcon unifiedWidget = new TrayIcon(createWidgetImage("0 B", "0 B", todayBytes, 0, 0));
//        unifiedWidget.setImageAutoSize(true);
//
//        // قائمة التحكم للويدجت
//        unifiedWidget.setPopupMenu(createMenu(() -> {
//            unifiedWidget.setImage(createWidgetImage("0 B", "0 B", todayBytes, 0, 0));
//        }));
//
//        tray.add(unifiedWidget);
//
//        Timer timer = new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                // فحص إذا بدأ يوم جديد لتصفير العداد تلقائياً
//                checkDayReset();
//
//                List<NetworkIF> networkIFs = hal.getNetworkIFs();
//                long currentRx = 0;
//                long currentTx = 0;
//
//                for (NetworkIF net : networkIFs) {
//                    net.updateAttributes();
//                    currentRx += net.getBytesRecv();
//                    currentTx += net.getBytesSent();
//                }
//
//                if (previousRxBytes > 0 && previousTxBytes > 0) {
//                    long rxSpeed = currentRx - previousRxBytes;
//                    long txSpeed = currentTx - previousTxBytes;
//
//                    // إضافة الاستهلاك لليوم الحالي وحفظه
//                    todayBytes += (rxSpeed + txSpeed);
//                    saveTodayData();
//
//                    // تحديث الويدجت الموحد بالكامل
//                    unifiedWidget.setImage(createWidgetImage(
//                            formatSpeed(rxSpeed),
//                            formatSpeed(txSpeed),
//                            todayBytes,
//                            rxSpeed,
//                            txSpeed
//                    ));
//
//                    unifiedWidget.setToolTip("Today's Total Usage: " + formatData(todayBytes));
//                }
//
//                previousRxBytes = currentRx;
//                previousTxBytes = currentTx;
//            }
//        }, 0, 1000);
//    }
//
//    // دالة رسم الويدجت الموحد (العريض)
//    private static Image createWidgetImage(String down, String up, long todayTotal, long rxSpeed, long txSpeed) {
//        int logicalWidth = 150; // عرض ممتاز يشبه التاريخ في الماك
//        int logicalHeight = 22;
//        int scale = 3; // أقصى دقة لريتنا الماك
//
//        BufferedImage image = new BufferedImage(logicalWidth * scale, logicalHeight * scale, BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g2d = image.createGraphics();
//
//        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
//        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g2d.scale(scale, scale);
//
//        // خلفية شفافة
//        g2d.setColor(new Color(0, 0, 0, 0));
//        g2d.fillRect(0, 0, logicalWidth, logicalHeight);
//
//        Font font = new Font(".AppleSystemUIFont", Font.BOLD, 11);
//        g2d.setFont(font);
//
//        FontMetrics fm = g2d.getFontMetrics(font);
//        int y = ((logicalHeight - fm.getHeight()) / 2) + fm.getAscent();
//
//        // 1. رسم سرعة التحميل (يسار)
//        g2d.setColor(determineColor(rxSpeed, true));
//        g2d.drawString("↓" + down, 2, y);
//
//        // 2. رسم سرعة الرفع (وسط)
//        g2d.setColor(determineColor(txSpeed, false));
//        g2d.drawString("↑" + up, 55, y);
//
//        // 3. رسم فاصل أنيق
//        g2d.setColor(new Color(255, 255, 255, 60));
//        g2d.drawString("│", 102, y);
//
//        // 4. رسم استهلاك اليوم (يمين)
//        g2d.setColor(new Color(220, 220, 220)); // لون رمادي فاتح مريح للعين
//        g2d.drawString(formatData(todayTotal), 110, y);
//
//        g2d.dispose();
//        return image;
//    }
//
//    // تحويل السرعات اللحظية
//    private static String formatSpeed(long bytes) {
//        if (bytes < 1024) return bytes + "B";
//        int exp = (int) (Math.log(bytes) / Math.log(1024));
//        char pre = "KMGTPE".charAt(exp - 1);
//        return String.format("%.1f%c", bytes / Math.pow(1024, exp), pre);
//    }
//
//    // تحويل إجمالي استهلاك اليوم (يتحول لـ MB أو GB بشكل جميل)
//    private static String formatData(long bytes) {
//        if (bytes < 1024 * 1024) return String.format("%.0f KB", bytes / 1024.0);
//        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
//        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
//    }
//
//    private static Color determineColor(long bytes, boolean isDownload) {
//        if (bytes == 0) return new Color(255, 255, 255, 100);
//
//        switch (currentTheme) {
//            case 1: // نيون سايبربانك
//                return isDownload ? new Color(0, 245, 255) : new Color(255, 50, 150);
//            case 2: // تفاعلي حسب السرعة
//                if (bytes < 500 * 1024) return new Color(140, 255, 140);
//                if (bytes < 5 * 1024 * 1024) return new Color(255, 215, 0);
//                return new Color(255, 75, 75);
//            case 0: // أبل كلاسيك
//            default:
//                return Color.WHITE;
//        }
//    }
//
//    // حفظ واسترجاع بيانات اليوم تلقائياً
//    private static void loadTodayData() {
//        savedDate = prefs.get("saved_date", LocalDate.now().toString());
//        if (savedDate.equals(LocalDate.now().toString())) {
//            todayBytes = prefs.getLong("today_bytes", 0);
//        } else {
//            todayBytes = 0; // يوم جديد
//            saveTodayData();
//        }
//    }
//
//    private static void saveTodayData() {
//        prefs.put("saved_date", LocalDate.now().toString());
//        prefs.putLong("today_bytes", todayBytes);
//    }
//
//    private static void checkDayReset() {
//        String today = LocalDate.now().toString();
//        if (!today.equals(savedDate)) {
//            todayBytes = 0;
//            savedDate = today;
//            saveTodayData();
//        }
//    }
//
//    // قائمة الخيارات بالزر الأيمن
//    private static PopupMenu createMenu(Runnable onThemeChanged) {
//        PopupMenu menu = new PopupMenu();
//
//        MenuItem header = new MenuItem("📊 Daily Network Monitor");
//        header.setEnabled(false);
//
//        Menu themesMenu = new Menu("🎨 Themes");
//        MenuItem themeNeon = new MenuItem("Neon Cyberpunk");
//        MenuItem themeTraffic = new MenuItem("Traffic Adaptive");
//        MenuItem themeApple = new MenuItem("Apple Minimalist");
//
//        themeNeon.addActionListener(e -> { currentTheme = 1; onThemeChanged.run(); });
//        themeTraffic.addActionListener(e -> { currentTheme = 2; onThemeChanged.run(); });
//        themeApple.addActionListener(e -> { currentTheme = 0; onThemeChanged.run(); });
//
//        themesMenu.add(themeNeon);
//        themesMenu.add(themeTraffic);
//        themesMenu.add(themeApple);
//
//        MenuItem resetToday = new MenuItem("🔄 Reset Today's Usage");
//        resetToday.addActionListener(e -> {
//            todayBytes = 0;
//            saveTodayData();
//            onThemeChanged.run();
//        });
//
//        MenuItem exit = new MenuItem("❌ Quit");
//        exit.addActionListener(e -> System.exit(0));
//
//        menu.add(header);
//        menu.addSeparator();
//        menu.add(themesMenu);
//        menu.add(resetToday);
//        menu.addSeparator();
//        menu.add(exit);
//
//        return menu;
//    }
//}
////package com.maknoon;
////
////import oshi.SystemInfo;
////import oshi.hardware.HardwareAbstractionLayer;
////import oshi.hardware.NetworkIF;
////
////import java.awt.*;
////import java.awt.image.BufferedImage;
////import java.util.List;
////import java.util.Timer;
////import java.util.TimerTask;
////
////public class App {
////    private static long previousRxBytes = 0;
////    private static long previousTxBytes = 0;
////
////    // إجمالي الاستهلاك منذ تشغيل التطبيق (مع volatile لضمان التزامن)
////    private static volatile long totalDownloaded = 0;
////    private static volatile long totalUploaded = 0;
////
////    // خيارات الثيمات: 0: Apple Clean, 1: Cyberpunk Neon, 2: Traffic Light
////    private static volatile int currentTheme = 1;
////
////    public static void main(String[] args) throws Exception {
////        System.setProperty("apple.awt.UIElement", "true");
////
////        if (!SystemTray.isSupported()) {
////            System.out.println("SystemTray is not supported");
////            return;
////        }
////
////        SystemInfo si = new SystemInfo();
////        HardwareAbstractionLayer hal = si.getHardware();
////        SystemTray tray = SystemTray.getSystemTray();
////
////        TrayIcon downloadIcon = new TrayIcon(createSingleLineImage("↓", "0 B", 0, true));
////        TrayIcon uploadIcon = new TrayIcon(createSingleLineImage("↑", "0 B", 0, false));
////
////        downloadIcon.setImageAutoSize(true);
////        uploadIcon.setImageAutoSize(true);
////
////        // وظيفة التحديث الفوري عند تغيير الثيم
////        Runnable updateCallback = () -> {
////            downloadIcon.setImage(createSingleLineImage("↓", "0 B", 0, true));
////            uploadIcon.setImage(createSingleLineImage("↑", "0 B", 0, false));
////        };
////
////        // الحل هنا: إنشاء نسختين منفصلتين من القائمة لكل أيقونة
////        downloadIcon.setPopupMenu(createMenu(updateCallback));
////        uploadIcon.setPopupMenu(createMenu(updateCallback));
////
////        tray.add(downloadIcon);
////        tray.add(uploadIcon);
////
////        Timer timer = new Timer();
////        timer.scheduleAtFixedRate(new TimerTask() {
////            @Override
////            public void run() {
////                List<NetworkIF> networkIFs = hal.getNetworkIFs();
////                long currentRx = 0;
////                long currentTx = 0;
////
////                for (NetworkIF net : networkIFs) {
////                    net.updateAttributes();
////                    currentRx += net.getBytesRecv();
////                    currentTx += net.getBytesSent();
////                }
////
////                if (previousRxBytes > 0 && previousTxBytes > 0) {
////                    long rxSpeed = currentRx - previousRxBytes;
////                    long txSpeed = currentTx - previousTxBytes;
////
////                    totalDownloaded += rxSpeed;
////                    totalUploaded += txSpeed;
////
////                    // تحديث الأيقونات بالسرعة والألوان
////                    downloadIcon.setImage(createSingleLineImage("↓", formatSpeed(rxSpeed), rxSpeed, true));
////                    uploadIcon.setImage(createSingleLineImage("↑", formatSpeed(txSpeed), txSpeed, false));
////
////                    // نص التلميح عند وضع الماوس على الأيقونة
////                    downloadIcon.setToolTip("Session Download: " + formatSpeed(totalDownloaded));
////                    uploadIcon.setToolTip("Session Upload: " + formatSpeed(totalUploaded));
////                }
////
////                previousRxBytes = currentRx;
////                previousTxBytes = currentTx;
////            }
////        }, 0, 1000);
////    }
////
////    private static String formatSpeed(long bytes) {
////        if (bytes < 1024) return bytes + " B";
////        int exp = (int) (Math.log(bytes) / Math.log(1024));
////        char pre = "KMGTPE".charAt(exp - 1);
////        return String.format("%.1f%c", bytes / Math.pow(1024, exp), pre);
////    }
////
////    private static Image createSingleLineImage(String arrow, String speed, long speedBytes, boolean isDownload) {
////        int logicalWidth = 65;
////        int logicalHeight = 22;
////        int scale = 3;
////
////        BufferedImage image = new BufferedImage(logicalWidth * scale, logicalHeight * scale, BufferedImage.TYPE_INT_ARGB);
////        Graphics2D g2d = image.createGraphics();
////
////        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
////        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
////        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
////        g2d.scale(scale, scale);
////
////        g2d.setColor(new Color(0, 0, 0, 0));
////        g2d.fillRect(0, 0, logicalWidth, logicalHeight);
////
////        Color textColor = determineColor(speedBytes, isDownload);
////        g2d.setColor(textColor);
////
////        Font font = new Font(".AppleSystemUIFont", Font.BOLD, 12);
////        g2d.setFont(font);
////
////        FontMetrics fm = g2d.getFontMetrics(font);
////        int y = ((logicalHeight - fm.getHeight()) / 2) + fm.getAscent();
////
////        g2d.drawString(arrow + " " + speed, 2, y);
////        g2d.dispose();
////        return image;
////    }
////
////    private static Color determineColor(long bytes, boolean isDownload) {
////        if (bytes == 0) {
////            return new Color(255, 255, 255, 90); // لون أبيض شبه شفاف وهادئ لوضع الخمول
////        }
////
////        switch (currentTheme) {
////            case 1: // ثيم Cyberpunk Neon
////                return isDownload ? new Color(0, 245, 255) : new Color(255, 50, 150);
////
////            case 2: // ثيم إشارات المرور التفاعلي (Traffic Light)
////                if (bytes < 500 * 1024) return new Color(140, 255, 140);
////                if (bytes < 5 * 1024 * 1024) return new Color(255, 215, 0);
////                return new Color(255, 75, 75);
////
////            case 0: // ثيم Apple الكلاسيكي
////            default:
////                return Color.WHITE;
////        }
////    }
////
////    private static PopupMenu createMenu(Runnable onThemeChanged) {
////        PopupMenu menu = new PopupMenu();
////
////        Menu themesMenu = new Menu("🎨 Select Theme");
////        MenuItem themeApple = new MenuItem("Apple Minimalist");
////        MenuItem themeCyber = new MenuItem("Neon Cyberpunk");
////        MenuItem themeTraffic = new MenuItem("Dynamic Traffic");
////
////        themeApple.addActionListener(e -> { currentTheme = 0; onThemeChanged.run(); });
////        themeCyber.addActionListener(e -> { currentTheme = 1; onThemeChanged.run(); });
////        themeTraffic.addActionListener(e -> { currentTheme = 2; onThemeChanged.run(); });
////
////        themesMenu.add(themeApple);
////        themesMenu.add(themeCyber);
////        themesMenu.add(themeTraffic);
////
////        MenuItem resetStats = new MenuItem("🔄 Reset Session Usage");
////        resetStats.addActionListener(e -> {
////            totalDownloaded = 0;
////            totalUploaded = 0;
////        });
////
////        MenuItem exit = new MenuItem("❌ Quit NetMeter");
////        exit.addActionListener(e -> System.exit(0));
////
////        menu.add(themesMenu);
////        menu.add(resetStats);
////        menu.addSeparator();
////        menu.add(exit);
////
////        return menu;
////    }
////}