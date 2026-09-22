package com.maknoon.model;

public enum DisplayMode {
    FULL("Full Dashboard"),
    SPEEDS_ONLY("Speeds Only"),
    TODAY_ONLY("Today Usage"),
    MINIMAL("Minimal Dot");

    private final String title;

    DisplayMode(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}