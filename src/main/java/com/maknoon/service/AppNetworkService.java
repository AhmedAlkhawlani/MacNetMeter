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

    public AppNetworkService() {
        // تشغيل فاحص الشبكة في الخلفية كل ثانيتين
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "AppNetScanner");
            t.setDaemon(true);
            return t;
        });

        executor.scheduleAtFixedRate(this::pollNetworkApps, 1, 2, TimeUnit.SECONDS);
    }

    private void pollNetworkApps() {
        try {
            // استخدام أداة أبل nettop لجلب استهلاك كل تطبيق (Logging mode)
            ProcessBuilder pb = new ProcessBuilder("nettop", "-P", "-L", "1", "-J", "bytes_in,bytes_out");
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                boolean isHeader = true;

                while ((line = reader.readLine()) != null) {
                    if (isHeader) { // تخطي سطر العناوين
                        isHeader = false;
                        continue;
                    }

                    String[] tokens = line.split(",");
                    if (tokens.length >= 3) {
                        String procEntry = tokens[0].trim(); // مثل Google Chrome.1234
                        long bytesIn = parseLong(tokens[1].trim());
                        long bytesOut = parseLong(tokens[2].trim());

                        // استخراج اسم التطبيق والـ PID
                        int lastDot = procEntry.lastIndexOf('.');
                        if (lastDot > 0) {
                            String appName = procEntry.substring(0, lastDot);
                            int pid = parseInt(procEntry.substring(lastDot + 1));

                            // حساب الفارق اللحظي (السرعة الحالية)
                            long prevIn = lastRxMap.getOrDefault(procEntry, bytesIn);
                            long prevOut = lastTxMap.getOrDefault(procEntry, bytesOut);

                            long rxSpeed = (bytesIn - prevIn) / 2; // مقسوم على الفارق الزمني (2 ثانية)
                            long txSpeed = (bytesOut - prevOut) / 2;

                            lastRxMap.put(procEntry, bytesIn);
                            lastTxMap.put(procEntry, bytesOut);

                            AppNetUsage usage = appsMap.computeIfAbsent(appName, k -> new AppNetUsage(appName, pid));
                            usage.updateSpeeds(Math.max(0, rxSpeed), Math.max(0, txSpeed));
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    // جلب أكثر التطبيقات سحباً للإنترنت حالياً (مرتبة تنازلياً)
    public List<AppNetUsage> getTopActiveApps(int limit) {
        return appsMap.values().stream()
                .filter(a -> (a.getRxSpeed() + a.getTxSpeed()) > 0 || a.getTotalBytes() > 1024 * 1024)
                .sorted((a1, a2) -> Long.compare((a2.getRxSpeed() + a2.getTxSpeed()), (a1.getRxSpeed() + a1.getTxSpeed())))
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