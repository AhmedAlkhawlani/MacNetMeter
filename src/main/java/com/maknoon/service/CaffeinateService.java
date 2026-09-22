package com.maknoon.service;

import java.io.IOException;

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
            stop(); // إيقاف أي عملية سابقة أولاً
            caffeinateProcess = new ProcessBuilder("caffeinate", "-d", "-i", "-m").start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (caffeinateProcess != null) {
            caffeinateProcess.destroyForcibly(); // إيقاف إجباري فوري
            caffeinateProcess = null;
        }
    }
}
//package com.maknoon.service;
//
//import java.io.IOException;
//
//public class CaffeinateService {
//    private Process caffeinateProcess;
//
//    public boolean isCaffeinated() {
//        return caffeinateProcess != null && caffeinateProcess.isAlive();
//    }
//
//    public void toggle() {
//        if (isCaffeinated()) {
//            stop();
//        } else {
//            start();
//        }
//    }
//
//    private void start() {
//        try {
//            // -d: يمنع الشاشة من الإغلاق | -i: يمنع النظام من الخمول
//            caffeinateProcess = new ProcessBuilder("caffeinate", "-d", "-i").start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void stop() {
//        if (caffeinateProcess != null) {
//            caffeinateProcess.destroy();
//            caffeinateProcess = null;
//        }
//    }
//}