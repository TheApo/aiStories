package com.apogames.aistories.game.listenStories;

import com.apogames.backend.GameProperties;
import com.apogames.backend.SequentiallyThinkingScreenModel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class ListenStoriesPreferences extends GameProperties {

    public ListenStoriesPreferences(SequentiallyThinkingScreenModel mainPanel) {
        super(mainPanel);
    }

    @Override
    public Preferences getPreferences() {
        return Gdx.app.getPreferences("AIStoriesListenStoryPreferences");
    }

    public void writeLevel() {

        getPref().flush();
    }

    public void readLevel() {
    }

}
