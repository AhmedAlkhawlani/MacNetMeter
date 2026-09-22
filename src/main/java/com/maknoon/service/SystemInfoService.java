package com.maknoon.service;

import oshi.SystemInfo;

public class SystemInfoService {
    private final SystemInfo systemInfo;

    public SystemInfoService(SystemInfo systemInfo) {
        this.systemInfo = systemInfo;
    }

    public String getFormattedUptime() {
        long uptimeSeconds = systemInfo.getOperatingSystem().getSystemUptime();
        long days = uptimeSeconds / (24 * 3600);
        long hours = (uptimeSeconds % (24 * 3600)) / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;

        if (days > 0) return String.format("%d days, %d hrs", days, hours);
        return String.format("%d hrs, %d mins", hours, minutes);
    }
}