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
    private final Map<String, Long> lastTxMap = new HashMap<>();
    private final DataSaverService dataSaver;

    public AppNetworkService(DataSaverService dataSaver) {
        this.dataSaver = dataSaver;

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AppNetScanner");
            t.setDaemon(true);
            return t;
        });

        // تشغيل فوري (0 delay) ثم كل ثانيتين لمنع أي تأخير في القائمة!
        executor.scheduleAtFixedRate(this::pollNetworkApps, 0, 2, TimeUnit.SECONDS);
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
                        long bytesOut = parseLong(tokens[2].trim());

                        int lastDot = procEntry.lastIndexOf('.');
                        if (lastDot > 0) {
                            String appName = procEntry.substring(0, lastDot);
                            int pid = parseInt(procEntry.substring(lastDot + 1));

                            long prevIn = lastRxMap.getOrDefault(procEntry, bytesIn);
                            long prevOut = lastTxMap.getOrDefault(procEntry, bytesOut);

                            long deltaIn = Math.max(0, bytesIn - prevIn);
                            long deltaOut = Math.max(0, bytesOut - prevOut);

                            lastRxMap.put(procEntry, bytesIn);
                            lastTxMap.put(procEntry, bytesOut);

                            AppNetUsage usage = appsMap.computeIfAbsent(appName, k -> new AppNetUsage(appName, pid));
                            usage.update(deltaIn / 2, deltaOut / 2, deltaIn + deltaOut);

                            // فحص وضع التوفير
//                            dataSaver.inspectTraffic(appName, deltaIn / 2);
                            // ابحث عن هذا السطر في AppNetworkService.java وغيّره إلى:
                            dataSaver.inspectAndEnforce(appName, pid, deltaIn / 2);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    // جلب أكثر التطبيقات استهلاكاً (سواء الحالية أو التراكمية لليوم)
    public List<AppNetUsage> getTopActiveApps(int limit) {
        return appsMap.values().stream()
                .filter(a -> a.getTodayTotalBytes() > 100 * 1024 || a.getRxSpeed() > 0)
                .sorted((a1, a2) -> Long.compare(a2.getTodayTotalBytes(), a1.getTodayTotalBytes()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // إيقاف تطبيق فوراً لحماية النت
    public boolean stopApp(int pid) {
        return ProcessHandle.of(pid).map(ProcessHandle::destroyForcibly).orElse(false);
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return 0; }
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }
}