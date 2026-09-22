package com.maknoon.service;

import com.maknoon.model.AppNetUsage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AppNetworkService {
    private final Map<String, AppNetUsage> appsMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lastRxMap = new HashMap<>();
    private final DataSaverService dataSaver;
    @SuppressWarnings("resource")
    private final ScheduledExecutorService executor;

    public AppNetworkService(DataSaverService dataSaver) {
        this.dataSaver = dataSaver;

        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AppNetScanner");
            t.setDaemon(true);
            return t;
        });

        this.executor.scheduleAtFixedRate(this::pollNetworkApps, 0, 2, TimeUnit.SECONDS);
    }

    private void pollNetworkApps() {
        try {
            ProcessBuilder pb = new ProcessBuilder("nettop", "-P", "-L", "1", "-J", "bytes_in,bytes_out");
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                boolean isHeader = true;

                while ((line = reader.readLine()) != null) {
                    if (isHeader) { isHeader = false; continue; }

                    String[] tokens = line.split(",");
                    if (tokens.length >= 3) {
                        String procEntry = tokens[0].trim();
                        long bytesIn = parseLong(tokens[1].trim());

                        int lastDot = procEntry.lastIndexOf('.');
                        if (lastDot > 0) {
                            String appName = procEntry.substring(0, lastDot);
                            int pid = parseInt(procEntry.substring(lastDot + 1));

                            long prevIn = lastRxMap.getOrDefault(procEntry, bytesIn);
                            long deltaIn = Math.max(0, bytesIn - prevIn);

                            lastRxMap.put(procEntry, bytesIn);

                            AppNetUsage usage = appsMap.computeIfAbsent(appName, k -> new AppNetUsage(appName));
                            usage.update(deltaIn / 2, deltaIn);

                            dataSaver.inspectAndEnforce(appName, pid, deltaIn / 2);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public List<AppNetUsage> getTopActiveApps(int limit) {
        return appsMap.values().stream()
                .filter(a -> a.getTodayTotalBytes() > 100 * 1024 || a.getRxSpeed() > 0)
                .sorted((a1, a2) -> Long.compare(a2.getTodayTotalBytes(), a1.getTodayTotalBytes()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return 0; }
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}