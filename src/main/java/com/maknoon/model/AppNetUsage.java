package com.maknoon.model;

public class AppNetUsage {
    private final String name;
    private final int pid;
    private long rxSpeed; // سرعة التنزيل
    private long txSpeed; // سرعة الرفع
    private long totalBytes; // إجمالي ما سحبه التطبيق

    public AppNetUsage(String name, int pid) {
        this.name = name;
        this.pid = pid;
    }

    public void updateSpeeds(long rxSpeed, long txSpeed) {
        this.rxSpeed = rxSpeed;
        this.txSpeed = txSpeed;
        this.totalBytes += (rxSpeed + txSpeed);
    }

    public String getName() { return name; }
    public int getPid() { return pid; }
    public long getRxSpeed() { return rxSpeed; }
    public long getTxSpeed() { return txSpeed; }
    public long getTotalBytes() { return totalBytes; }
}