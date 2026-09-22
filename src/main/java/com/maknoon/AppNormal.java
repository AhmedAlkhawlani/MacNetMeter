package com.maknoon;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AppNormal {
    private static long previousRxBytes = 0;
    private static long previousTxBytes = 0;

    public static void main(String[] args) throws Exception {
        // إخفاء أيقونة التطبيق من شريط Dock السفلي
        System.setProperty("apple.awt.UIElement", "true");

        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        SystemTray tray = SystemTray.getSystemTray();

        // إنشاء أيقونتين منفصلتين (واحدة للتحميل وواحدة للرفع)
        TrayIcon downloadIcon = new TrayIcon(createSingleLineImage("↓", "0 B"));
        TrayIcon uploadIcon = new TrayIcon(createSingleLineImage("↑", "0 B"));

        downloadIcon.setImageAutoSize(true);
        uploadIcon.setImageAutoSize(true);

        // إضافة الأيقونتين لشريط الماك (الماك سيرتبهم بجانب بعضهما تلقائياً)
        tray.add(downloadIcon);
        tray.add(uploadIcon);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
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

                    // تحديث الأيقونتين كل ثانية
                    downloadIcon.setImage(createSingleLineImage("↓", formatSpeed(rxSpeed)));
                    uploadIcon.setImage(createSingleLineImage("↑", formatSpeed(txSpeed)));
                }

                previousRxBytes = currentRx;
                previousTxBytes = currentTx;
            }
        }, 0, 1000);
    }

    private static String formatSpeed(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %c", bytes / Math.pow(1024, exp), pre);
    }


    private static Image createSingleLineImage(String arrow, String speed) {
        int logicalWidth = 60; // العرض
        int logicalHeight = 22; // ارتفاع شريط الماك
        int scale = 3; // عدنا للتكبير بـ 3 أضعاف لأنه الأفضل لشاشات الريتنا!

        BufferedImage image = new BufferedImage(logicalWidth * scale, logicalHeight * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // عدنا لتقنية التنعيم العادية لأنها الأفضل مع الخلفيات الشفافة
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.scale(scale, scale);

        // خلفية شفافة
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, logicalWidth, logicalHeight);

        g2d.setColor(Color.WHITE);

        // عدنا للخط العريض (BOLD) لأنه الأوضح في الجافا، مع مقاس 12 أو 13
        Font font = new Font(".AppleSystemUIFont", Font.BOLD, 15);
        g2d.setFont(font);

        // ضبط المحاذاة ليكون النص في المنتصف ليوازي البطارية
        FontMetrics fm = g2d.getFontMetrics(font);
        int y = ((logicalHeight - fm.getHeight()) / 2) + fm.getAscent();

        g2d.drawString(arrow + " " + speed, 2, y);

        g2d.dispose();
        return image;
    }
}
