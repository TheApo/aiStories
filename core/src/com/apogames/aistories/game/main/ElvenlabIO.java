package com.apogames.aistories.game.main;

import com.apogames.aistories.game.listenStories.WordTimingData;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.google.gson.Gson;

import java.util.*;

public class ElvenlabIO {
    public static String VOICE_ID;
    public static String API_KEY;
    public static String MODEL_ID = "eleven_multilingual_v2";

    private final MainInterface main;
    private final Gson gson = new Gson();

    public ElvenlabIO(MainInterface main) {
        this.main = main;
    }

    private void sendMessage(String text, final String fileName) {
        this.main.setRunning(Running.CREATE_AUDIO);

        Map<String, String> voiceSettings = new HashMap<>();
        voiceSettings.put("stability", "0.5");
        voiceSettings.put("similarity_boost", "0.5");

        Map<String, Object> params = new HashMap<>();
        params.put("model_id", MODEL_ID);
        params.put("text", text);
        params.put("voice_settings", voiceSettings);

        final String API_URL = "https://api.elevenlabs.io/v1/text-to-speech/" + VOICE_ID
                + "/with-timestamps?output_format=mp3_44100_192";

        HttpRequestBuilder builder = new HttpRequestBuilder();
        HttpRequest request = builder.newRequest()
                .method(HttpMethods.POST)
                .url(API_URL)
                .timeout(90000)
                .header("xi-api-key", API_KEY)
                .header("Content-Type", "application/json")
                .content(gson.toJson(params))
                .build();

        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse httpResponse) {
                try {
                    String responseBody = httpResponse.getResultAsString();
                    parseTimestampResponse(responseBody, fileName);
                } catch (Exception e) {
                    Gdx.app.error("ElvenlabIO", "Error reading response", e);
                }
                main.setRunning(Running.NONE);
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("ElvenlabIO", "Request Failed", t);
                main.setRunning(Running.NONE);
            }

            @Override
            public void cancelled() {
                Gdx.app.log("ElvenlabIO", "Request Cancelled");
                main.setRunning(Running.NONE);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void parseTimestampResponse(String responseBody, String fileName) {
        Map<String, Object> response = gson.fromJson(responseBody, Map.class);

        // 1. Decode and save MP3 (without signaling yet)
        String audioBase64 = (String) response.get("audio_base64");
        byte[] mp3Bytes = Base64.getDecoder().decode(audioBase64);
        FileHandle mp3File = Gdx.files.local(fileName);
        mp3File.writeBytes(mp3Bytes, false);
        Gdx.app.log("ElvenlabIO", "MP3 file saved successfully!");

        // 2. Parse alignment and save JSON
        Map<String, Object> alignment = (Map<String, Object>) response.get("alignment");
        if (alignment != null) {
            List<String> characters = (List<String>) alignment.get("characters");
            List<Double> startTimesDouble = (List<Double>) alignment.get("character_start_times_seconds");
            List<Double> endTimesDouble = (List<Double>) alignment.get("character_end_times_seconds");

            List<Float> startTimes = new ArrayList<>(startTimesDouble.size());
            List<Float> endTimes = new ArrayList<>(endTimesDouble.size());
            for (int i = 0; i < startTimesDouble.size(); i++) {
                startTimes.add(startTimesDouble.get(i).floatValue());
                endTimes.add(endTimesDouble.get(i).floatValue());
            }

            WordTimingData timingData = WordTimingData.fromAlignment(characters, startTimes, endTimes);
            String jsonPath = fileName.replace(".mp3", ".json");
            timingData.saveToFile(jsonPath);
            Gdx.app.log("ElvenlabIO", "JSON timing file saved (" + timingData.getWords().size() + " words)");
        }

        // 3. Signal AFTER both files are saved
        main.setFileHandle(mp3File);
    }

    public void sendTextToSpeech(String message, String filename) {
        sendMessage(message, filename);
    }
}
