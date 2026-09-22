package com.maknoon.ui;

import com.maknoon.model.DisplayMode;

import java.awt.*;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;

public class WidgetRenderer {

    private static final Font FONT = new Font(".AppleSystemUIFont", Font.BOLD, 12);

    public static Image render(DisplayMode mode, String down, String up, long todayBytes,
                               long targetLimitBytes, boolean isCaffeinated, boolean isDataSaver,
                               boolean isPaused, long rxSpeed, long txSpeed) {

        String downText = isPaused ? "↓ 0 B" : "↓ " + down;
        String upText = isPaused ? "↑ 0 B" : "↑ " + up;
        String sepText = "│";
        String usageText = isPaused ? "⏸ Paused" : formatData(todayBytes);
        String limitText = (!isPaused && targetLimitBytes > 0) ? " / " + formatData(targetLimitBytes) : "";

        BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gDummy = dummy.createGraphics();
        gDummy.setFont(FONT);
        FontMetrics fm = gDummy.getFontMetrics();

        // مساحة الأيقونات المتجهة (18 بكسل لكل أيقونة نشطة)
        int wIcons = (isCaffeinated ? 18 : 0) + (isDataSaver ? 18 : 0);
        int wDown = fm.stringWidth(downText);
        int wUp = fm.stringWidth(upText);
        int wSep = fm.stringWidth(sepText);
        int wUsage = fm.stringWidth(usageText + limitText);
        gDummy.dispose();

        int spacing = 10;
        int totalWidth;

        switch (mode) {
            case FULL -> totalWidth = 6 + wIcons + wDown + spacing + wUp + spacing + wSep + spacing + wUsage + 6;
            case SPEEDS_ONLY -> totalWidth = 6 + wIcons + wDown + spacing + wUp + 6;
            case TODAY_ONLY -> totalWidth = 6 + wIcons + fm.stringWidth("📊 ") + wUsage + 6;
            case MINIMAL -> totalWidth = 26 + wIcons;
            default -> totalWidth = 150;
        }

        int height = 22;

        BufferedImage img1x = drawCanvas(totalWidth, height, 1, mode, downText, upText, sepText,
                usageText, limitText, isCaffeinated, isDataSaver, isPaused,
                rxSpeed, txSpeed, todayBytes, targetLimitBytes, spacing);
        BufferedImage img3x = drawCanvas(totalWidth, height, 3, mode, downText, upText, sepText,
                usageText, limitText, isCaffeinated, isDataSaver, isPaused,
                rxSpeed, txSpeed, todayBytes, targetLimitBytes, spacing);

        return new BaseMultiResolutionImage(img1x, img3x);
    }

    private static BufferedImage drawCanvas(int logicalWidth, int logicalHeight, int scale,
                                            DisplayMode mode, String downText, String upText, String sepText,
                                            String usageText, String limitText,
                                            boolean isCaffeinated, boolean isDataSaver, boolean isPaused,
                                            long rxSpeed, long txSpeed,
                                            long todayBytes, long targetLimitBytes, int spacing) {

        BufferedImage image = new BufferedImage(logicalWidth * scale, logicalHeight * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.scale(scale, scale);

        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, logicalWidth, logicalHeight);

        g2d.setFont(FONT);
        FontMetrics fm = g2d.getFontMetrics(FONT);
        int y = ((logicalHeight - fm.getHeight()) / 2) + fm.getAscent();

        int curX = 6;

        // ☕ 1. رسم فنجان القهوة كفيكتور احترافي وظاهر بوضوح!
        if (isCaffeinated) {
            drawCoffeeVector(g2d, curX, y);
            curX += 18;
        }

        // 🛡️ 2. رسم درع الحماية كفيكتور أزرق نيون لامع!
        if (isDataSaver) {
            drawShieldVector(g2d, curX, y);
            curX += 18;
        }

        Color dimColor = new Color(255, 255, 255, 60);

        switch (mode) {
            case FULL -> {
                g2d.setColor(isPaused ? dimColor : new Color(0, 245, 255));
                g2d.drawString(downText, curX, y);
                curX += fm.stringWidth(downText) + spacing;

                g2d.setColor(isPaused ? dimColor : new Color(255, 50, 150));
                g2d.drawString(upText, curX, y);
                curX += fm.stringWidth(upText) + spacing;

                g2d.setColor(new Color(255, 255, 255, 40));
                g2d.drawString(sepText, curX, y);
                curX += fm.stringWidth(sepText) + spacing;

                if (isPaused) {
                    g2d.setColor(dimColor);
                    g2d.drawString(usageText, curX, y);
                } else {
                    drawUsageText(g2d, curX, y, usageText, limitText, todayBytes, targetLimitBytes, fm);
                }
            }
            case SPEEDS_ONLY -> {
                g2d.setColor(isPaused ? dimColor : new Color(0, 245, 255));
                g2d.drawString(downText, curX, y);
                curX += fm.stringWidth(downText) + spacing;

                g2d.setColor(isPaused ? dimColor : new Color(255, 50, 150));
                g2d.drawString(upText, curX, y);
            }
            case TODAY_ONLY -> {
                g2d.setColor(isPaused ? dimColor : Color.WHITE);
                g2d.drawString("📊 ", curX, y);
                curX += fm.stringWidth("📊 ");
                if (isPaused) {
                    g2d.setColor(dimColor);
                    g2d.drawString(usageText, curX, y);
                } else {
                    drawUsageText(g2d, curX, y, usageText, limitText, todayBytes, targetLimitBytes, fm);
                }
            }
            case MINIMAL -> {
                g2d.setColor(isPaused ? dimColor : (rxSpeed > 0 || txSpeed > 0 ? Color.GREEN : Color.GRAY));
                g2d.drawString("●", curX + 4, y);
            }
        }

        g2d.dispose();
        return image;
    }

    // دالة رسم فنجان القهوة بالبكسلات (Vector Cup)
    private static void drawCoffeeVector(Graphics2D g, int x, int y) {
        g.setColor(new Color(255, 185, 60)); // لون عنبري ذهبي دافئ

        // جسم الفنجان
        g.fillRoundRect(x + 1, y - 9, 8, 8, 2, 2);

        // يد الفنجان
        g.setStroke(new java.awt.BasicStroke(1.2f));
        g.drawArc(x + 7, y - 8, 4, 5, 270, 180);

        // خط البخار المتصاعد
        g.drawLine(x + 3, y - 11, x + 4, y - 13);
        g.drawLine(x + 6, y - 11, x + 7, y - 13);
    }

    // دالة رسم درع الحماية بالبكسلات (Vector Shield)
    private static void drawShieldVector(Graphics2D g, int x, int y) {
        g.setColor(new Color(0, 245, 255)); // أزرق نيون
        int[] px = {x + 1, x + 9, x + 9, x + 5, x + 1};
        int[] py = {y - 12, y - 12, y - 7, y - 2, y - 7};
        g.drawPolygon(px, py, 5);

        // نقطة مضيئة في منتصف الدرع
        g.fillRect(x + 4, y - 8, 3, 3);
    }

    private static void drawUsageText(Graphics2D g2d, int x, int y, String usageText, String limitText,
                                      long todayBytes, long targetLimitBytes, FontMetrics fm) {
        if (targetLimitBytes <= 0) {
            g2d.setColor(Color.WHITE);
            g2d.drawString(usageText, x, y);
            return;
        }

        double ratio = (double) todayBytes / targetLimitBytes;
        if (ratio >= 1.0) {
            g2d.setColor(new Color(255, 65, 65));
        } else if (ratio >= 0.8) {
            g2d.setColor(new Color(255, 185, 0));
        } else {
            g2d.setColor(Color.WHITE);
        }

        g2d.drawString(usageText, x, y);

        int offset = fm.stringWidth(usageText);
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.drawString(limitText, x + offset, y);
    }

    public static String formatData(long bytes) {
        if (bytes < 1024 * 1024) return String.format("%.0f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}