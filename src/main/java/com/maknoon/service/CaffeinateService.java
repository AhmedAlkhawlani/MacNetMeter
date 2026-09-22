package com.maknoon.service;

public class CaffeinateService {
    private Process caffeinateProcess;

    public boolean isCaffeinated() {
        return caffeinateProcess != null && caffeinateProcess.isAlive();
    }

    public void toggle() {
        if (isCaffeinated()) {
            stop();
        } else {
            start();
        }
    }

    public void start() {
        try {
            stop();
            caffeinateProcess = new ProcessBuilder("caffeinate", "-d", "-i", "-m").start();
        } catch (Exception ignored) {
            // تجاهل الخطأ في حال لم يدعم النظام الأمر
        }
    }

    public void stop() {
        if (caffeinateProcess != null) {
            caffeinateProcess.destroyForcibly();
            caffeinateProcess = null;
        }
    }
}