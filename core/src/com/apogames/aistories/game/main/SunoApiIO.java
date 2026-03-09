package com.apogames.aistories.game.main;

import com.apogames.common.Localization;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SunoApiIO {

    public static String API_KEY;

    private static final String BASE_URL = "https://api.sunoapi.org";
    private static final String CALLBACK_URL = "https://example.com/callback";
    private static final int POLL_INTERVAL_MS = 20000;
    private static final int MAX_WAIT_MS = 480000;

    private final MainInterface main;
    private final Gson gson = new Gson();
    private String characterHeader = "";
    private String existingFilePrefix = "";
    private String songStyle = "";

    public SunoApiIO(MainInterface main) {
        this.main = main;
    }

    public void setCharacterHeader(String characterHeader) {
        this.characterHeader = characterHeader;
    }

    public void setExistingFilePrefix(String prefix) {
        this.existingFilePrefix = prefix;
    }

    public void generateSongCustom(String lyrics, String style, String title) {
        main.setRunning(Running.CREATE_SONG);
        this.songStyle = style != null ? style : "";

        Map<String, Object> payload = new HashMap<>();
        payload.put("prompt", lyrics);
        payload.put("style", style);
        payload.put("title", title);
        payload.put("customMode", true);
        payload.put("instrumental", false);
        payload.put("model", "V5");
        payload.put("callBackUrl", CALLBACK_URL);

        Gdx.app.log("SunoApiIO", "Custom mode: lyrics=" + lyrics.length() + " chars, style=" + style + ", title=" + title);
        sendGenerateRequest(payload);
    }

    public void generateSong(String prompt) {
        main.setRunning(Running.CREATE_SONG);

        Map<String, Object> payload = new HashMap<>();
        payload.put("prompt", prompt);
        payload.put("customMode", false);
        payload.put("instrumental", false);
        payload.put("model", "V5");
        payload.put("callBackUrl", CALLBACK_URL);

        sendGenerateRequest(payload);
    }

    private void sendGenerateRequest(Map<String, Object> payload) {
        HttpRequestBuilder builder = new HttpRequestBuilder();
        HttpRequest request = builder.newRequest()
                .method(HttpMethods.POST)
                .url(BASE_URL + "/api/v1/generate")
                .timeout(30000)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .content(gson.toJson(payload))
                .build();

        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse httpResponse) {
                try {
                    String body = httpResponse.getResultAsString();
                    Gdx.app.log("SunoApiIO", "Generate response: " + body);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = gson.fromJson(body, Map.class);
                    double code = ((Number) data.get("code")).doubleValue();
                    if (code != 200 || !data.containsKey("data")) {
                        Gdx.app.error("SunoApiIO", "Generate failed: " + body);
                        main.setRunning(Running.NONE);
                        return;
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> innerData = (Map<String, Object>) data.get("data");
                    String taskId = (String) innerData.get("taskId");
                    Gdx.app.log("SunoApiIO", "taskId: " + taskId);
                    startPolling(taskId);
                } catch (Exception e) {
                    Gdx.app.error("SunoApiIO", "Error parsing generate response", e);
                    main.setRunning(Running.NONE);
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("SunoApiIO", "Generate request failed", t);
                main.setRunning(Running.NONE);
            }

            @Override
            public void cancelled() {
                Gdx.app.log("SunoApiIO", "Generate request cancelled");
                main.setRunning(Running.NONE);
            }
        });
    }

    private String buildStatusText(int elapsedSec) {
        String duration = Localization.getInstance().getCommon().get("text_song_duration");
        String wait = Localization.getInstance().getCommon().format("text_song_wait", elapsedSec);
        return duration + " - " + wait;
    }

    private void startPolling(String taskId) {
        new Thread(() -> {
            long start = System.currentTimeMillis();
            String lastStatus = null;

            Gdx.app.postRunnable(() -> main.setStatusText(buildStatusText(0)));

            while (System.currentTimeMillis() - start < MAX_WAIT_MS) {
                try {
                    Thread.sleep(POLL_INTERVAL_MS);
                } catch (InterruptedException e) {
                    break;
                }
                int elapsedSec = (int) ((System.currentTimeMillis() - start) / 1000);
                Gdx.app.postRunnable(() -> main.setStatusText(buildStatusText(elapsedSec)));

                Map<String, Object> details = pollSync(taskId);
                if (details == null) continue;

                String status = String.valueOf(details.get("status"));
                if (!status.equals(lastStatus)) {
                    Gdx.app.log("SunoApiIO", "Status: " + status);
                    lastStatus = status;
                }

                if ("SUCCESS".equals(status)) {
                    handleSuccess(details);
                    return;
                }

                if (isFailedStatus(status)) {
                    Gdx.app.error("SunoApiIO", "Task failed: " + status + " - " + details.get("errorMessage"));
                    Gdx.app.postRunnable(() -> main.setRunning(Running.NONE));
                    return;
                }
            }

            Gdx.app.error("SunoApiIO", "Timeout waiting for task " + taskId);
            Gdx.app.postRunnable(() -> main.setRunning(Running.NONE));
        }).start();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> pollSync(String taskId) {
        try {
            java.net.URL url = new java.net.URL(BASE_URL + "/api/v1/generate/record-info?taskId=" + taskId);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            int responseCode = conn.getResponseCode();
            if (responseCode >= 400) {
                Gdx.app.error("SunoApiIO", "Poll failed: HTTP " + responseCode);
                return null;
            }

            java.io.InputStream is = conn.getInputStream();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            Map<String, Object> data = gson.fromJson(sb.toString(), Map.class);
            double code = ((Number) data.get("code")).doubleValue();
            if (code != 200 || !data.containsKey("data")) {
                return null;
            }
            return (Map<String, Object>) data.get("data");
        } catch (Exception e) {
            Gdx.app.error("SunoApiIO", "Poll error", e);
            return null;
        }
    }

    private boolean isFailedStatus(String status) {
        return "CREATE_TASK_FAILED".equals(status)
                || "GENERATE_AUDIO_FAILED".equals(status)
                || "CALLBACK_EXCEPTION".equals(status)
                || "SENSITIVE_WORD_ERROR".equals(status)
                || "FAILED".equals(status);
    }

    @SuppressWarnings("unchecked")
    private void handleSuccess(Map<String, Object> details) {
        Map<String, Object> response = (Map<String, Object>) details.get("response");
        if (response == null) {
            Gdx.app.postRunnable(() -> main.setRunning(Running.NONE));
            return;
        }

        List<Map<String, Object>> sunoData = (List<Map<String, Object>>) response.get("sunoData");
        if (sunoData == null || sunoData.isEmpty()) {
            Gdx.app.postRunnable(() -> main.setRunning(Running.NONE));
            return;
        }

        String lyrics = null;
        String title = null;
        String tags = null;
        List<String> audioUrls = new ArrayList<>();

        for (Map<String, Object> track : sunoData) {
            String audioUrl = (String) track.get("audioUrl");
            if (audioUrl != null && !audioUrl.isEmpty()) {
                audioUrls.add(audioUrl);
            }
            if (lyrics == null) {
                lyrics = (String) track.get("prompt");
                title = (String) track.get("title");
                tags = (String) track.get("tags");
            }
        }

        if (audioUrls.isEmpty()) {
            Gdx.app.error("SunoApiIO", "No audio URLs in response");
            Gdx.app.postRunnable(() -> main.setRunning(Running.NONE));
            return;
        }

        Gdx.app.log("SunoApiIO", "Got " + audioUrls.size() + " tracks, title: " + title);

        boolean hasExistingFile = existingFilePrefix != null && !existingFilePrefix.isEmpty();
        String filePrefix = hasExistingFile ? existingFilePrefix : buildFilePrefix(title) + "_song";
        String finalLyrics = lyrics != null ? lyrics : "";

        // Download MP3s
        for (int i = 0; i < audioUrls.size(); i++) {
            String suffix = (audioUrls.size() > 1) ? "_v" + (i + 1) : "";
            String mp3Path = Prompt.DIRECTORY + filePrefix + suffix + ".mp3";
            downloadMp3Sync(audioUrls.get(i), mp3Path);
        }

        if (hasExistingFile) {
            // TXT already saved, just reload
            String txtPath = Prompt.DIRECTORY + filePrefix + ".txt";
            Gdx.app.postRunnable(() -> {
                FileHandle txtFile = Gdx.files.local(txtPath);
                main.setFileHandle(txtFile);
                main.setRunning(Running.NONE);
            });
        } else {
            // Save lyrics as txt (with _song marker)
            String txtPath = Prompt.DIRECTORY + filePrefix + ".txt";
            String header = characterHeader.isEmpty() ? "song" : "song;" + characterHeader;
            if (!songStyle.isEmpty()) header += ";" + songStyle;
            String txtContent = header + ListenStories_SEPARATOR + finalLyrics;
            Gdx.app.postRunnable(() -> {
                FileHandle txtFile = Gdx.files.local(txtPath);
                txtFile.writeString(txtContent, false);
                main.setFileHandle(txtFile);
                main.setRunning(Running.NONE);
            });
        }
    }

    private static final String ListenStories_SEPARATOR = ";!;!";

    private String buildFilePrefix(String title) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_");
        String date = now.format(fmt);
        String safeName = (title != null) ? title.replaceAll("[^a-zA-Z0-9äöüÄÖÜß _-]", "").trim().replace(" ", "_") : "Song";
        if (safeName.length() > 60) safeName = safeName.substring(0, 60);
        return date + safeName;
    }

    private void downloadMp3Sync(String audioUrl, String outputPath) {
        try {
            java.net.URL url = new java.net.URL(audioUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            java.io.InputStream is = conn.getInputStream();
            byte[] buffer = new byte[256 * 1024];

            // Write to temp file then move
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            is.close();

            byte[] mp3Data = baos.toByteArray();
            Gdx.app.postRunnable(() -> {
                FileHandle out = Gdx.files.local(outputPath);
                out.writeBytes(mp3Data, false);
                Gdx.app.log("SunoApiIO", "Downloaded: " + outputPath + " (" + mp3Data.length + " bytes)");
            });

            // Small delay to let postRunnable execute
            Thread.sleep(500);
        } catch (Exception e) {
            Gdx.app.error("SunoApiIO", "Download failed: " + audioUrl, e);
        }
    }
}
