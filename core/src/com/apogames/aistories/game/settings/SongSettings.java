package com.apogames.aistories.game.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SongSettings {

    private static final String PREFS_NAME = "AIStoriesSongSettings";

    public enum SongGenerationMode {
        SUNO_ONLY, GPT_SUNO, GEMINI_SUNO
    }

    public enum MusicStyle {
        POP, ROCK, COUNTRY, HIPHOP, LULLABY, PIANO, ELECTRONIC, MUSICAL
    }

    public enum SongLength {
        SHORT("~2 min"), MEDIUM("~3 min"), LONG("~4 min");

        @Getter
        private final String label;

        SongLength(String label) {
            this.label = label;
        }
    }

    private MusicStyle musicStyle = MusicStyle.POP;
    private StorySettings.AgeGroup ageGroup = StorySettings.AgeGroup.AGE_8_12;
    private SongLength songLength = SongLength.MEDIUM;
    private String promptTemplate = "";
    private boolean includeObjectives = true;
    private SongGenerationMode generationMode = SongGenerationMode.SUNO_ONLY;

    public void save() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        prefs.putString("musicStyle", musicStyle.name());
        prefs.putString("ageGroup", ageGroup.name());
        prefs.putString("songLength", songLength.name());
        prefs.putString("promptTemplate", promptTemplate);
        prefs.putBoolean("includeObjectives", includeObjectives);
        prefs.putString("generationMode", generationMode.name());
        prefs.flush();
    }

    public void load() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        try {
            musicStyle = MusicStyle.valueOf(prefs.getString("musicStyle", MusicStyle.POP.name()));
        } catch (IllegalArgumentException e) {
            musicStyle = MusicStyle.POP;
        }
        try {
            ageGroup = StorySettings.AgeGroup.valueOf(prefs.getString("ageGroup", StorySettings.AgeGroup.AGE_8_12.name()));
        } catch (IllegalArgumentException e) {
            ageGroup = StorySettings.AgeGroup.AGE_8_12;
        }
        try {
            songLength = SongLength.valueOf(prefs.getString("songLength", SongLength.MEDIUM.name()));
        } catch (IllegalArgumentException e) {
            songLength = SongLength.MEDIUM;
        }
        promptTemplate = prefs.getString("promptTemplate", "");
        includeObjectives = prefs.getBoolean("includeObjectives", true);
        try {
            generationMode = SongGenerationMode.valueOf(prefs.getString("generationMode", SongGenerationMode.SUNO_ONLY.name()));
        } catch (IllegalArgumentException e) {
            generationMode = SongGenerationMode.SUNO_ONLY;
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

    public String getLengthDescription() {
        switch (songLength) {
            case SHORT: return "kurzes Lied (~2 Minuten)";
            case MEDIUM: return "mittellanges Lied (~3 Minuten)";
            case LONG: return "langes Lied (~4 Minuten)";
            default: return "mittellanges Lied (~3 Minuten)";
        }
    }
}
