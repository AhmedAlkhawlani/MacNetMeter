package com.maknoon.ui;

import java.awt.*;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;

public class CpuWidgetRenderer {

    private static final Font FONT = new Font(".AppleSystemUIFont", Font.BOLD, 12);

    public static Image render(double cpuUsage, long usedMemory, long totalMemory) {
        String cpuStr = String.format("⚡ %.0f%%", cpuUsage);
        String memStr = String.format("🧠 %.1fG", usedMemory / (1024.0 * 1024.0 * 1024.0));
        String sep = "│";

        // قياس العرض السائل برمجياً لمنع التداخل
        BufferedImage dummy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gDummy = dummy.createGraphics();
        gDummy.setFont(FONT);
        FontMetrics fm = gDummy.getFontMetrics();

        int spacing = 8;
        int totalWidth = 6 + fm.stringWidth(cpuStr) + spacing + fm.stringWidth(sep) + spacing + fm.stringWidth(memStr) + 6;
        gDummy.dispose();

        int height = 22;

        BufferedImage img1x = draw(totalWidth, height, 1, cpuStr, memStr, sep, cpuUsage, (double) usedMemory / totalMemory, spacing);
        BufferedImage img3x = draw(totalWidth, height, 3, cpuStr, memStr, sep, cpuUsage, (double) usedMemory / totalMemory, spacing);

        return new BaseMultiResolutionImage(img1x, img3x);
    }

    private static BufferedImage draw(int width, int height, int scale, String cpuStr, String memStr, String sep,
                                      double cpuUsage, double memRatio, int spacing) {
        BufferedImage image = new BufferedImage(width * scale, height * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.scale(scale, scale);

        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, width, height);

        g2d.setFont(FONT);
        FontMetrics fm = g2d.getFontMetrics(FONT);
        int y = ((height - fm.getHeight()) / 2) + fm.getAscent();

        int curX = 6;

        // 1. رسم المعالج (أخضر عادي، برتقالي إذا ضغطت عليه، أحمر لو كاد ينفجر!)
        if (cpuUsage > 80) g2d.setColor(new Color(255, 65, 65));
        else if (cpuUsage > 50) g2d.setColor(new Color(255, 185, 0));
        else g2d.setColor(new Color(0, 245, 255)); // سماوي هادئ

        g2d.drawString(cpuStr, curX, y);
        curX += fm.stringWidth(cpuStr) + spacing;

        // 2. الفاصل
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.drawString(sep, curX, y);
        curX += fm.stringWidth(sep) + spacing;

        // 3. رسم الرام
        if (memRatio > 0.85) g2d.setColor(new Color(255, 65, 65));
        else if (memRatio > 0.70) g2d.setColor(new Color(255, 185, 0));
        else g2d.setColor(new Color(180, 140, 255)); // بنفسجي تقني فخم للرام

        g2d.drawString(memStr, curX, y);

        g2d.dispose();
        return image;
    }
}