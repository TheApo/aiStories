package com.apogames.aistories.game.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorySettings {

    private static final String PREFS_NAME = "AIStoriesSettings";

    public enum StoryType {
        BEDTIME, FRIENDSHIP, DETECTIVE, ADVENTURE, FAIRYTALE
    }

    public enum AgeGroup {
        AGE_0_1, AGE_2_4, AGE_5_7, AGE_8_12, AGE_12_16, AGE_16_PLUS
    }

    public enum StoryLength {
        SHORT(2000, "2-3"),
        MEDIUM(3500, "3-4"),
        LONG(5000, "5");

        @Getter
        private final int charCount;
        @Getter
        private final String chapters;

        StoryLength(int charCount, String chapters) {
            this.charCount = charCount;
            this.chapters = chapters;
        }
    }

    private StoryType storyType = StoryType.BEDTIME;
    private AgeGroup ageGroup = AgeGroup.AGE_8_12;
    private StoryLength storyLength = StoryLength.LONG;
    private String promptTemplate = "";

    public void save() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putString("storyType", storyType.name());
        prefs.putString("ageGroup", ageGroup.name());
        prefs.putString("storyLength", storyLength.name());
        prefs.putString("promptTemplate", promptTemplate);
        prefs.flush();
    }

    public void load() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        try {
            storyType = StoryType.valueOf(prefs.getString("storyType", StoryType.BEDTIME.name()));
        } catch (IllegalArgumentException e) {
            storyType = StoryType.BEDTIME;
        }
        try {
            ageGroup = AgeGroup.valueOf(prefs.getString("ageGroup", AgeGroup.AGE_8_12.name()));
        } catch (IllegalArgumentException e) {
            ageGroup = AgeGroup.AGE_8_12;
        }
        try {
            storyLength = StoryLength.valueOf(prefs.getString("storyLength", StoryLength.LONG.name()));
        } catch (IllegalArgumentException e) {
            storyLength = StoryLength.LONG;
        }
        promptTemplate = prefs.getString("promptTemplate", "");
    }

    public String getAgeLabel() {
        switch (ageGroup) {
            case AGE_0_1: return "0-1";
            case AGE_2_4: return "2-4";
            case AGE_5_7: return "5-7";
            case AGE_8_12: return "8-12";
            case AGE_12_16: return "12-16";
            case AGE_16_PLUS: return "16+";
            default: return "8-12";
        }
    }

    public String getAgeDescription() {
        switch (ageGroup) {
            case AGE_0_1: return "Kinder von 0 bis 1 Jahr";
            case AGE_2_4: return "Kinder von 2 bis 4 Jahren";
            case AGE_5_7: return "Kinder von 5 bis 7 Jahren";
            case AGE_8_12: return "Kinder von 8 bis 12 Jahren";
            case AGE_12_16: return "Jugendliche von 12 bis 16 Jahren";
            case AGE_16_PLUS: return "Jugendliche und Erwachsene ab 16 Jahren";
            default: return "Kinder von 8 bis 12 Jahren";
        }
    }

    public String getComplexityDescription() {
        switch (ageGroup) {
            case AGE_0_1:
                return "in ganz einfachen Worten mit kurzen Saetzen und Wiederholungen";
            case AGE_2_4:
                return "in sehr einfacher Sprache mit kurzen Saetzen und einfachen Reimen";
            case AGE_5_7:
                return "kindgerecht mit kurzen, klaren Saetzen und einfacher direkter Rede";
            case AGE_8_12:
                return "bildhaft, kindgerecht (ohne lange Schachtelsaetze) mit abwechslungsreichen Saetzen und humorvollen Elementen";
            case AGE_12_16:
                return "anspruchsvoll mit komplexeren Satzkonstruktionen, Spannung und emotionaler Tiefe";
            case AGE_16_PLUS:
                return "literarisch anspruchsvoll mit komplexen Handlungsstraengen und vielschichtigen Charakteren";
            default:
                return "bildhaft und kindgerecht";
        }
    }
}
