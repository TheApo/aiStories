package com.apogames.aistories.game.main;

public enum Running {
    NONE(""),
    CREATE_STORY("text_creating_text"),
    CREATE_AUDIO("text_creating_audio"),
    CREATE_SONG("text_creating_song"),
    CREATE_LYRICS("text_creating_lyrics");

    private final String id;

    Running(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
