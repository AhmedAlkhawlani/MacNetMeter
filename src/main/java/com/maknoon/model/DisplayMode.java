package com.maknoon.model;

public enum DisplayMode {
    FULL("Full Dashboard", 175),       // السرعات + استهلاك اليوم
    SPEEDS_ONLY("Speeds Only", 115),   // سرعة الرفع والتحميل فقط
    TODAY_ONLY("Today Usage", 85),     // استهلاك اليوم فقط
    MINIMAL("Minimal Dot", 35);        // أيقونة مدمجة جداً

    private final String title;
    private final int width;

    DisplayMode(String title, int width) {
        this.title = title;
        this.width = width;
    }

    public String getTitle() { return title; }
    public int getWidth() { return width; }
}