package com.maknoon.service;

import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;

import java.time.LocalDate;
import java.util.List;
import java.util.prefs.Preferences;

public class MacSystemNetworkService {
    private final Preferences prefs = Preferences.userNodeForPackage(MacSystemNetworkService.class);
    private final HardwareAbstractionLayer hal;

    private long baselineRx = 0;
    private long baselineTx = 0;
    private long rebootOffsetRx = 0;
    private long rebootOffsetTx = 0;
    private long lastHwRx = 0;
    private long lastHwTx = 0;
    private String savedDate = "";

    public MacSystemNetworkService(HardwareAbstractionLayer hal) {
        this.hal = hal;
        loadBaselines();
    }

    // تحديث وقراءة استهلاك كرت الشبكة الحقيقي
    public void update() {
        String today = LocalDate.now().toString();
        long[] currentHw = getRawHardwareTotals();
        long curRx = currentHw[0];
        long curTx = currentHw[1];

        // 1. إذا بدأ يوم جديد، نحدد نقطة الصفر لليوم الجديد
        if (!today.equals(savedDate)) {
            savedDate = today;
            baselineRx = curRx;
            baselineTx = curTx;
            rebootOffsetRx = 0;
            rebootOffsetTx = 0;
            saveBaselines();
        }

        // 2. إذا تم إعادة تشغيل الماك أثناء اليوم، نحسب ما تم استهلاكه قبل الإعادة
        if (curRx < lastHwRx) {
            rebootOffsetRx += Math.max(0, lastHwRx - baselineRx);
            baselineRx = 0;
            saveBaselines();
        }
        if (curTx < lastHwTx) {
            rebootOffsetTx += Math.max(0, lastHwTx - baselineTx);
            baselineTx = 0;
            saveBaselines();
        }

        lastHwRx = curRx;
        lastHwTx = curTx;
    }

    public long getMacTodayDownload() {
        return Math.max(0, (lastHwRx - baselineRx) + rebootOffsetRx);
    }

    public long getMacTodayUpload() {
        return Math.max(0, (lastHwTx - baselineTx) + rebootOffsetTx);
    }

    public long getMacTodayTotal() {
        return getMacTodayDownload() + getMacTodayUpload();
    }

    // قراءة العدادات التراكمية من كروت الشبكة الحقيقية في الماك
    private long[] getRawHardwareTotals() {
        long rx = 0, tx = 0;
        List<NetworkIF> nets = hal.getNetworkIFs();
        for (NetworkIF net : nets) {
            // استثناء كروت الوهمية والـ loopback
            if (!net.getName().startsWith("lo") && !net.getName().startsWith("bridge")) {
                net.updateAttributes();
                rx += net.getBytesRecv();
                tx += net.getBytesSent();
            }
        }
        return new long[]{rx, tx};
    }

    private void loadBaselines() {
        savedDate = prefs.get("mac_net_date", "");
        baselineRx = prefs.getLong("mac_net_base_rx", 0);
        baselineTx = prefs.getLong("mac_net_base_tx", 0);
        rebootOffsetRx = prefs.getLong("mac_net_offset_rx", 0);
        rebootOffsetTx = prefs.getLong("mac_net_offset_tx", 0);

        long[] currentHw = getRawHardwareTotals();
        lastHwRx = currentHw[0];
        lastHwTx = currentHw[1];

        // إذا كانت أول مرة تشغيل على الإطلاق
        if (savedDate.isEmpty()) {
            savedDate = LocalDate.now().toString();
            baselineRx = lastHwRx;
            baselineTx = lastHwTx;
            saveBaselines();
        }
    }

    private void saveBaselines() {
        prefs.put("mac_net_date", savedDate);
        prefs.putLong("mac_net_base_rx", baselineRx);
        prefs.putLong("mac_net_base_tx", baselineTx);
        prefs.putLong("mac_net_offset_rx", rebootOffsetRx);
        prefs.putLong("mac_net_offset_tx", rebootOffsetTx);
    }
}