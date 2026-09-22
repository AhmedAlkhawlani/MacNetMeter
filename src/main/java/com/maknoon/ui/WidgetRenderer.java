package com.maknoon.ui;

import com.maknoon.model.DisplayMode;

import java.awt.*;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;

public class WidgetRenderer {

    private static final Font FONT = new Font(".AppleSystemUIFont", Font.BOLD, 12);

    public static Image render(DisplayMode mode, String down, String up, long todayBytes,
                               long targetLimitBytes, boolean isCaffeinated, boolean isDataSaver,
                               long rxSpeed, long txSpeed) {

        String downText = "↓ " + down;
        String upText = "↑ " + up;
        String sepText = "│";
        String usageText = formatData(todayBytes);
        String limitText = (targetLimitBytes > 0) ? " / " + formatData(targetLimitBytes) : "";

        BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gDummy = dummy.createGraphics();
        gDummy.setFont(FONT);
        FontMetrics fm = gDummy.getFontMetrics();

        int wIcons = (isCaffeinated ? 20 : 0) + (isDataSaver ? 20 : 0);
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
                usageText, limitText, isCaffeinated, isDataSaver, rxSpeed, txSpeed,
                todayBytes, targetLimitBytes, spacing);
        BufferedImage img3x = drawCanvas(totalWidth, height, 3, mode, downText, upText, sepText,
                usageText, limitText, isCaffeinated, isDataSaver, rxSpeed, txSpeed,
                todayBytes, targetLimitBytes, spacing);

        return new BaseMultiResolutionImage(img1x, img3x);
    }

    private static BufferedImage drawCanvas(int logicalWidth, int logicalHeight, int scale,
                                            DisplayMode mode, String downText, String upText, String sepText,
                                            String usageText, String limitText,
                                            boolean isCaffeinated, boolean isDataSaver,
                                            long rxSpeed, long txSpeed,
                                            long todayBytes, long targetLimitBytes, int spacing) {

        BufferedImage image = new BufferedImage(logicalWidth * scale, logicalHeight * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.scale(scale, scale);

        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, logicalWidth, logicalHeight);

        g2d.setFont(FONT);
        FontMetrics fm = g2d.getFontMetrics(FONT);
        int y = ((logicalHeight - fm.getHeight()) / 2) + fm.getAscent();

        int curX = 6;

        if (isCaffeinated) {
            g2d.drawString("☕", curX, y);
            curX += 20;
        }

        if (isDataSaver) {
            g2d.drawString("🛡️", curX, y);
            curX += 20;
        }

        switch (mode) {
            case FULL -> {
                g2d.setColor(new Color(0, 245, 255));
                g2d.drawString(downText, curX, y);
                curX += fm.stringWidth(downText) + spacing;

                g2d.setColor(new Color(255, 50, 150));
                g2d.drawString(upText, curX, y);
                curX += fm.stringWidth(upText) + spacing;

                g2d.setColor(new Color(255, 255, 255, 60));
                g2d.drawString(sepText, curX, y);
                curX += fm.stringWidth(sepText) + spacing;

                drawUsageText(g2d, curX, y, usageText, limitText, todayBytes, targetLimitBytes, fm);
            }
            case SPEEDS_ONLY -> {
                g2d.setColor(new Color(0, 245, 255));
                g2d.drawString(downText, curX, y);
                curX += fm.stringWidth(downText) + spacing;

                g2d.setColor(new Color(255, 50, 150));
                g2d.drawString(upText, curX, y);
            }
            case TODAY_ONLY -> {
                g2d.setColor(Color.WHITE);
                g2d.drawString("📊 ", curX, y);
                curX += fm.stringWidth("📊 ");
                drawUsageText(g2d, curX, y, usageText, limitText, todayBytes, targetLimitBytes, fm);
            }
            case MINIMAL -> {
                g2d.setColor(rxSpeed > 0 || txSpeed > 0 ? Color.GREEN : Color.GRAY);
                g2d.drawString("●", curX + 4, y);
            }
        }

        g2d.dispose();
        return image;
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