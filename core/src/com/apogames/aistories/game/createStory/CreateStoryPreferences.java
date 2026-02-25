package com.apogames.aistories.game.createStory;

import com.apogames.backend.GameProperties;
import com.apogames.backend.SequentiallyThinkingScreenModel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class CreateStoryPreferences extends GameProperties {

    public CreateStoryPreferences(SequentiallyThinkingScreenModel mainPanel) {
        super(mainPanel);
    }

    @Override
    public Preferences getPreferences() {
        return Gdx.app.getPreferences("AIStoriesCreateStoryPreferences");
    }

    public void writeLevel() {

        getPref().flush();
    }

    public void readLevel() {
    }

}
