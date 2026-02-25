package com.apogames.aistories.game.customEntity;

import com.apogames.backend.GameProperties;
import com.apogames.backend.SequentiallyThinkingScreenModel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class CustomEntityEditorPreferences extends GameProperties {

    public CustomEntityEditorPreferences(SequentiallyThinkingScreenModel mainPanel) {
        super(mainPanel);
    }

    @Override
    public Preferences getPreferences() {
        return Gdx.app.getPreferences("AIStoriesCustomCharacterPreferences");
    }

    public void writeLevel() {
        getPref().flush();
    }

    public void readLevel() {
    }
}
