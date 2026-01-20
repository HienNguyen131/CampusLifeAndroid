package com.example.campuslife.item;

import java.util.List;

public class OnboardingItem {
    private int imageRes;
    private String titleMain;
    private String titleHighlight;
    private String description;
    private int accentColor;
    private List<String> bullets;

    public OnboardingItem(int imageRes, String titleMain, String titleHighlight, String description, int accentColor, List<String> bullets) {
        this.imageRes = imageRes;
        this.titleMain = titleMain;
        this.titleHighlight = titleHighlight;
        this.description = description;
        this.accentColor = accentColor;
        this.bullets = bullets;
    }

    public int getImageRes() { return imageRes; }
    public String getTitleMain() { return titleMain; }
    public String getTitleHighlight() { return titleHighlight; }
    public String getDescription() { return description; }
    public int getAccentColor() { return accentColor; }
    public List<String> getBullets() { return bullets; }
}
