package com.maknoon.service;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.List;

public class CpuMemoryService {
    private final CentralProcessor processor;
    private final GlobalMemory memory;
    private final OperatingSystem os;
    private long[] prevTicks;

    public CpuMemoryService(SystemInfo si) {
        this.processor = si.getHardware().getProcessor();
        this.memory = si.getHardware().getMemory();
        this.os = si.getOperatingSystem();
        this.prevTicks = processor.getSystemCpuLoadTicks();
    }

    // جلب نسبة استهلاك المعالج الحالية
    public double getCpuUsage() {
        double cpu = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = processor.getSystemCpuLoadTicks();
        return Math.max(0, Math.min(cpu, 100)); // محصورة بين 0 و 100
    }

    // جلب الرام المستهلك
    public long getUsedMemory() {
        return memory.getTotal() - memory.getAvailable();
    }

    public long getTotalMemory() {
        return memory.getTotal();
    }

    public double getMemoryUsagePercentage() {
        return ((double) getUsedMemory() / getTotalMemory()) * 100;
    }

    // جلب أكثر البرامج استهلاكاً للمعالج
    public List<OSProcess> getTopCpuProcesses(int limit) {
        return os.getProcesses(OperatingSystem.ProcessFiltering.ALL_PROCESSES,
                OperatingSystem.ProcessSorting.CPU_DESC, limit);
    }

    // جلب أكثر البرامج استهلاكاً للرام
    // جلب أكثر البرامج استهلاكاً للرام باستخدام RSS_DESC
    public List<OSProcess> getTopMemoryProcesses(int limit) {
        return os.getProcesses(OperatingSystem.ProcessFiltering.ALL_PROCESSES,
                OperatingSystem.ProcessSorting.RSS_DESC, limit);
    }

    // إنهاء تطبيق بالقوة بضغطة زر
    public boolean killProcess(int pid) {
        return ProcessHandle.of(pid).map(ProcessHandle::destroyForcibly).orElse(false);
    }

    // تنظيف كاش الرام في الماك
    public void purgeRam() {
        try {
            new ProcessBuilder("purge").start();
        } catch (Exception ignored) {}
    }
}