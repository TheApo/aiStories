package com.apogames.aistories.game.creativeTonie;

import com.apogames.backend.GameProperties;
import com.apogames.backend.SequentiallyThinkingScreenModel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class CreativeToniePreferences extends GameProperties {

    public CreativeToniePreferences(SequentiallyThinkingScreenModel mainPanel) {
        super(mainPanel);
    }

    @Override
    public Preferences getPreferences() {
        return Gdx.app.getPreferences("AIStoriesCreativeToniePreferences");
    }

    public void writeLevel() {

        getPref().flush();
    }

    public void readLevel() {
    }

}
