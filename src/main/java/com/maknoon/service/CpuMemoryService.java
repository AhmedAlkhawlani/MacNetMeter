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

    public double getCpuUsage() {
        double cpu = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        prevTicks = processor.getSystemCpuLoadTicks();
        return Math.max(0, Math.min(cpu, 100));
    }

    public long getUsedMemory() {
        return memory.getTotal() - memory.getAvailable();
    }

    public long getTotalMemory() {
        return memory.getTotal();
    }

    public List<OSProcess> getTopCpuProcesses(int limit) {
        return os.getProcesses(OperatingSystem.ProcessFiltering.ALL_PROCESSES,
                OperatingSystem.ProcessSorting.CPU_DESC, limit);
    }

    public List<OSProcess> getTopMemoryProcesses(int limit) {
        return os.getProcesses(OperatingSystem.ProcessFiltering.ALL_PROCESSES,
                OperatingSystem.ProcessSorting.RSS_DESC, limit);
    }

    // تم تغييرها إلى void لأننا لا نستخدم القيمة المرجعة
    public void killProcess(int pid) {
        ProcessHandle.of(pid).ifPresent(ProcessHandle::destroyForcibly);
    }

    public void purgeRam() {
        try {
            new ProcessBuilder("purge").start();
        } catch (Exception ignored) {}
    }
}