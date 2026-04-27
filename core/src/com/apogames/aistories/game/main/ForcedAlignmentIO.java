package com.apogames.aistories.game.main;

import com.apogames.aistories.game.listenStories.WordTimingData;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ForcedAlignmentIO {

    private static final String ALIGN_URL = "https://api.elevenlabs.io/v1/forced-alignment";
    private static final String CHARSET = "UTF-8";

    private final Gson gson = new Gson();

    public void alignAsync(FileHandle mp3, String lyrics, Runnable onJsonSaved) {
        new Thread(() -> {
            try {
                boolean saved = align(mp3, lyrics);
                if (saved && onJsonSaved != null) {
                    Gdx.app.postRunnable(onJsonSaved);
                }
            } catch (Exception e) {
                Gdx.app.error("ForcedAlignmentIO", "Alignment failed for " + mp3.name(), e);
            }
        }, "forced-alignment-" + mp3.name()).start();
    }

    private boolean align(FileHandle mp3, String lyrics) throws Exception {
        if (ElvenlabIO.API_KEY == null || ElvenlabIO.API_KEY.isEmpty()) {
            Gdx.app.log("ForcedAlignmentIO", "No ElevenLabs API key - skipping alignment");
            return false;
        }
        if (!mp3.exists()) {
            Gdx.app.error("ForcedAlignmentIO", "MP3 not found: " + mp3.path());
            return false;
        }

        String cleanText = cleanLyrics(lyrics);
        if (cleanText.isEmpty()) {
            Gdx.app.log("ForcedAlignmentIO", "Empty lyrics - skipping alignment for " + mp3.name());
            return false;
        }

        String boundary = "----aistories" + System.currentTimeMillis();
        byte[] body = buildMultipart(boundary, cleanText, mp3);

        HttpURLConnection conn = (HttpURLConnection) new URL(ALIGN_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(180000);
        conn.setRequestProperty("xi-api-key", ElvenlabIO.API_KEY);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("Accept", "application/json");
        conn.setFixedLengthStreamingMode(body.length);

        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.write(body);
        }

        int code = conn.getResponseCode();
        if (code >= 400) {
            String err = readStream(conn.getErrorStream());
            Gdx.app.error("ForcedAlignmentIO", "HTTP " + code + " for " + mp3.name() + ": " + err);
            return false;
        }

        String response = readStream(conn.getInputStream());
        return parseAndSave(response, mp3);
    }

    @SuppressWarnings("unchecked")
    private boolean parseAndSave(String responseBody, FileHandle mp3) {
        Map<String, Object> response = gson.fromJson(responseBody, Map.class);
        List<Map<String, Object>> wordsRaw = (List<Map<String, Object>>) response.get("words");
        if (wordsRaw == null || wordsRaw.isEmpty()) {
            Gdx.app.log("ForcedAlignmentIO", "No words in response for " + mp3.name());
            return false;
        }

        List<WordTimingData.WordTiming> words = new ArrayList<>(wordsRaw.size());
        for (Map<String, Object> w : wordsRaw) {
            String text = (String) w.get("text");
            if (text == null || text.trim().isEmpty()) continue;
            Object startObj = w.get("start");
            Object endObj = w.get("end");
            if (!(startObj instanceof Number) || !(endObj instanceof Number)) continue;
            float start = ((Number) startObj).floatValue();
            float end = ((Number) endObj).floatValue();
            words.add(new WordTimingData.WordTiming(text, start, end));
        }

        if (words.isEmpty()) {
            Gdx.app.log("ForcedAlignmentIO", "No usable word timings for " + mp3.name());
            return false;
        }

        String jsonPath = mp3.path().replace(".mp3", ".json");
        new WordTimingData(words).saveToFile(jsonPath);
        Gdx.app.log("ForcedAlignmentIO", "Saved " + words.size() + " word timings -> " + jsonPath);
        return true;
    }

    private byte[] buildMultipart(String boundary, String text, FileHandle mp3) throws Exception {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        String lineEnd = "\r\n";
        String twoHyphens = "--";

        body.write((twoHyphens + boundary + lineEnd).getBytes(CHARSET));
        body.write(("Content-Disposition: form-data; name=\"text\"" + lineEnd).getBytes(CHARSET));
        body.write(("Content-Type: text/plain; charset=UTF-8" + lineEnd + lineEnd).getBytes(CHARSET));
        body.write(text.getBytes(CHARSET));
        body.write(lineEnd.getBytes(CHARSET));

        body.write((twoHyphens + boundary + lineEnd).getBytes(CHARSET));
        body.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + mp3.name() + "\"" + lineEnd).getBytes(CHARSET));
        body.write(("Content-Type: audio/mpeg" + lineEnd + lineEnd).getBytes(CHARSET));
        body.write(mp3.readBytes());
        body.write(lineEnd.getBytes(CHARSET));

        body.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes(CHARSET));
        return body.toByteArray();
    }

    private String readStream(InputStream is) throws Exception {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, CHARSET))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    private String cleanLyrics(String raw) {
        if (raw == null) return "";
        String cleaned = raw.replaceAll("\\[[^\\]]*\\]", " ");
        cleaned = cleaned.replaceAll("[\\t ]+", " ");
        cleaned = cleaned.replaceAll("(?m)^[ \\t]+", "");
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n");
        return cleaned.trim();
    }
}
