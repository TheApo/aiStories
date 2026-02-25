package com.apogames.aistories.game.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

        final String API_URL = "https://api.elevenlabs.io/v1/text-to-speech/" + VOICE_ID+"?optimize_streaming_latency=1&output_format=mp3_44100_192";

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
                try (InputStream is = httpResponse.getResultAsStream()) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[16384];

                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }

                    buffer.flush();
                    saveMp3(buffer.toByteArray(), fileName);
                } catch (IOException e) {
                    Gdx.app.error("HTTP", "Error reading response", e);
                }

                main.setRunning(Running.NONE);
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("HTTP", "Request Failed", t);
                main.setRunning(Running.NONE);
            }

            @Override
            public void cancelled() {
                Gdx.app.log("HTTP", "Request Cancelled");
                main.setRunning(Running.NONE);
            }
        });
    }

    private void saveMp3(byte[] mp3Bytes, String filename) {
        try {
            FileHandle file = Gdx.files.local(filename);
            file.writeBytes(mp3Bytes, false);
            Gdx.app.log("Save MP3", "MP3 file saved successfully!");
            main.setFileHandle(file);
        } catch (Exception e) {
            Gdx.app.log("Save MP3", "Failed to save MP3 file: " + e.getMessage());
        }
    }

    public void sendTextToSpeech(String message, String filename) {
        sendMessage(message, filename);
    }

}
