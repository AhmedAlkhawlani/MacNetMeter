package com.maknoon.model;

public class AppNetUsage {
    private final String name;
    private long rxSpeed;
    private long todayTotalBytes;

    public AppNetUsage(String name) {
        this.name = name;
    }

    public void update(long rxSpeed, long deltaBytes) {
        this.rxSpeed = rxSpeed;
        this.todayTotalBytes += deltaBytes;
    }

    public String getName() { return name; }
    public long getRxSpeed() { return rxSpeed; }
    public long getTodayTotalBytes() { return todayTotalBytes; }
}