package com.maknoon.ui;

import com.maknoon.service.CpuMemoryService;
import oshi.software.os.OSProcess;

import java.awt.*;
import java.util.List;

public class CpuMenuBuilder {

    public static PopupMenu build(CpuMemoryService service) {
        PopupMenu menu = new PopupMenu();

        // عنوان ملخص
        MenuItem header = new MenuItem("💻 System Activity Monitor");
        header.setEnabled(false);
        menu.add(header);
        menu.addSeparator();

        // 1. قسم البرامج الأكثر استهلاكاً للمعالج (Top 5 CPU)
        Menu cpuMenu = new Menu("🔥 Top CPU Consumers");
        List<OSProcess> topCpu = service.getTopCpuProcesses(5);
        for (OSProcess p : topCpu) {
            double cpuPercent = 100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime();
            String title = String.format("%s (%.1f%%)", p.getName(), cpuPercent);
            Menu procMenu = new Menu(title);

            MenuItem killItem = new MenuItem("❌ Force Kill (PID: " + p.getProcessID() + ")");
            killItem.addActionListener(e -> service.killProcess(p.getProcessID()));

            procMenu.add(killItem);
            cpuMenu.add(procMenu);
        }
        menu.add(cpuMenu);

        // 2. قسم البرامج الأكثر استهلاكاً للرام (Top 5 RAM)
        Menu memMenu = new Menu("🧠 Top RAM Consumers");
        List<OSProcess> topMem = service.getTopMemoryProcesses(5);
        for (OSProcess p : topMem) {
            double ramMB = p.getResidentSetSize() / (1024.0 * 1024.0);
            String title = String.format("%s (%.0f MB)", p.getName(), ramMB);
            Menu procMenu = new Menu(title);

            MenuItem killItem = new MenuItem("❌ Force Kill (PID: " + p.getProcessID() + ")");
            killItem.addActionListener(e -> service.killProcess(p.getProcessID()));

            procMenu.add(killItem);
            memMenu.add(procMenu);
        }
        menu.add(memMenu);

        menu.addSeparator();

        // 3. تنظيف الرام
        MenuItem purgeItem = new MenuItem("🧹 Purge Inactive RAM");
        purgeItem.addActionListener(e -> service.purgeRam());
        menu.add(purgeItem);

        return menu;
    }
}