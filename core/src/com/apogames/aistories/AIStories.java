package com.apogames.aistories;

import com.apogames.aistories.game.MainPanel;
import com.apogames.aistories.game.main.ChatGPTIO;
import com.apogames.aistories.game.main.ElvenlabIO;
import com.apogames.aistories.game.main.SunoApiIO;
import com.apogames.aistories.game.main.ToniesAPI;
import com.apogames.aistories.game.objects.*;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.Game;
import com.badlogic.gdx.Gdx;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AIStories extends Game {
    @Override
    public void create() {
        loadProperties();
        AssetLoader.load();
        setScreen(new MainPanel());
        if (Constants.IS_HTML) {
            Gdx.graphics.setContinuousRendering(false);
            Gdx.graphics.requestRendering();
        }
    }

    private void loadProperties() {
        if (!Gdx.files.internal("config.properties").exists()) {
            Gdx.app.log("config properties", "config.properties not found - starting without API keys");
            return;
        }
        Properties props = new Properties();
        try (InputStream input = Gdx.files.internal("config.properties").read()) {
            props.load(input);
            ChatGPTIO.API_KEY = props.getProperty("CHATGPT_API_KEY");
            ChatGPTIO.GEMINI_API_KEY = props.getProperty("GEMINI_API_KEY");
            ElvenlabIO.API_KEY = props.getProperty("ELEVENLABS_API_KEY");
            ElvenlabIO.VOICE_ID = props.getProperty("ELEVENLABS_VOICE_ID");
            String modelId = props.getProperty("ELEVENLABS_MODEL_ID");
            if (modelId != null && !modelId.trim().isEmpty()) {
                ElvenlabIO.MODEL_ID = modelId.trim();
            }
            ToniesAPI.USERNAME = props.getProperty("TONIES_USERNAME");
            ToniesAPI.PASSWORD = props.getProperty("TONIES_PASSWORD");
            SunoApiIO.API_KEY = props.getProperty("SUNO_API_KEY");
        } catch (Exception ex) {
            System.out.println("Error loading config.properties: " + ex.getMessage());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        AssetLoader.dispose();
    }

    public void resume() {
        super.resume();
        AssetLoader.load();
        MainCharacter.refreshAll();
        Objectives.refreshAll();
        SupportingCharacter.refreshAll();
        Places.refreshAll();
        Universe.refreshAll();
    }
}
