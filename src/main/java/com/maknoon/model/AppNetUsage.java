package com.maknoon.model;

public class AppNetUsage {
    private final String name;
    private final int pid;
    private long rxSpeed;
    private long txSpeed;
    private long todayTotalBytes; // إجمالي استهلاك التطبيق لليوم

    public AppNetUsage(String name, int pid) {
        this.name = name;
        this.pid = pid;
    }

    public void update(long rxSpeed, long txSpeed, long deltaBytes) {
        this.rxSpeed = rxSpeed;
        this.txSpeed = txSpeed;
        this.todayTotalBytes += deltaBytes;
    }

    public String getName() { return name; }
    public int getPid() { return pid; }
    public long getRxSpeed() { return rxSpeed; }
    public long getTxSpeed() { return txSpeed; }
    public long getTodayTotalBytes() { return todayTotalBytes; }
}