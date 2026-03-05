package com.apogames.aistories.game.customEntity;

import com.apogames.aistories.game.main.ChatGPTIO;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Base64Coder;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageGenerationIO {

    public interface ImageCallback {
        void onSuccess(byte[] pngData);
        void onError(String message);
    }

    private static final Gson gson = new Gson();

    public static void generateImage(String name, String description, ImageStyle style, String llm, ImageCallback callback) {
        String prompt = buildPrompt(name, description, style);
        Gdx.app.log("ImageGen", "Generating with " + llm + ": " + prompt);

        ImageCallback loggingCallback = new ImageCallback() {
            @Override
            public void onSuccess(byte[] pngData) {
                Gdx.app.log("ImageGen", "Image generated successfully, size: " + pngData.length + " bytes");
                callback.onSuccess(pngData);
            }

            @Override
            public void onError(String message) {
                Gdx.app.error("ImageGen", "Image generation failed: " + message);
                callback.onError(message);
            }
        };

        if (llm != null && llm.startsWith("gemini")) {
            generateWithGemini(prompt, loggingCallback);
        } else {
            generateWithOpenAI(prompt, loggingCallback);
        }
    }

    private static String buildPrompt(String name, String description, ImageStyle style) {
        StringBuilder sb = new StringBuilder();
        sb.append("Create a small square illustration for a children's story book. ");
        sb.append("Subject: ").append(name);
        if (description != null && !description.isEmpty()) {
            sb.append(" - ").append(description);
        }
        sb.append(". ");
        sb.append("Art style: ").append(style.getPromptFragment()).append(". ");
        sb.append("The image should be friendly, child-appropriate, and have a simple clear composition on a plain background.");
        return sb.toString();
    }

    private static void generateWithOpenAI(String prompt, ImageCallback callback) {
        if (ChatGPTIO.API_KEY == null || ChatGPTIO.API_KEY.isEmpty()) {
            callback.onError("OpenAI API key not configured");
            return;
        }

        Map<String, Object> params = new HashMap<>();
        params.put("model", "gpt-image-1-mini");
        params.put("prompt", prompt);
        params.put("n", 1);
        params.put("size", "1024x1024");
        params.put("quality", "low");

        HttpRequestBuilder builder = new HttpRequestBuilder();
        HttpRequest request = builder.newRequest()
                .method(HttpMethods.POST)
                .url("https://api.openai.com/v1/images/generations")
                .timeout(120000)
                .header("Authorization", "Bearer " + ChatGPTIO.API_KEY)
                .header("Content-Type", "application/json")
                .content(gson.toJson(params))
                .build();

        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse httpResponse) {
                try {
                    String responseStr = httpResponse.getResultAsString();
                    Gdx.app.log("ImageGen", "OpenAI response length: " + responseStr.length());

                    Map<String, Object> response = gson.fromJson(responseStr, Map.class);
                    if (response == null || response.containsKey("error")) {
                        String errorMsg = response != null ? response.get("error").toString() : "Unknown error";
                        Gdx.app.postRunnable(() -> callback.onError("OpenAI: " + errorMsg));
                        return;
                    }

                    List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                    if (data != null && !data.isEmpty()) {
                        String b64 = (String) data.get(0).get("b64_json");
                        if (b64 != null) {
                            byte[] imageBytes = Base64Coder.decode(b64);
                            Gdx.app.log("ImageGen", "OpenAI image received, bytes: " + imageBytes.length);
                            Gdx.app.postRunnable(() -> callback.onSuccess(imageBytes));
                            return;
                        }
                    }
                    Gdx.app.error("ImageGen", "No image data in OpenAI response: " + responseStr);
                    Gdx.app.postRunnable(() -> callback.onError("No image data in response"));
                } catch (Exception e) {
                    Gdx.app.error("ImageGen", "Error parsing OpenAI response", e);
                    Gdx.app.postRunnable(() -> callback.onError("Parse error: " + e.getMessage()));
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("ImageGen", "OpenAI request failed", t);
                Gdx.app.postRunnable(() -> callback.onError("Request failed: " + t.getMessage()));
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> callback.onError("Request cancelled"));
            }
        });
    }

    private static void generateWithGemini(String prompt, ImageCallback callback) {
        if (ChatGPTIO.GEMINI_API_KEY == null || ChatGPTIO.GEMINI_API_KEY.isEmpty()) {
            callback.onError("Gemini API key not configured");
            return;
        }

        String model = "gemini-3.1-flash-image-preview";
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + ChatGPTIO.GEMINI_API_KEY;

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(textPart);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", parts);

        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(content);

        List<String> modalities = new ArrayList<>();
        modalities.add("image");
        modalities.add("text");

        Map<String, Object> genConfig = new HashMap<>();
        genConfig.put("responseModalities", modalities);

        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);
        body.put("generationConfig", genConfig);

        HttpRequestBuilder builder = new HttpRequestBuilder();
        HttpRequest request = builder.newRequest()
                .method(HttpMethods.POST)
                .url(url)
                .timeout(120000)
                .header("Content-Type", "application/json")
                .content(gson.toJson(body))
                .build();

        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse httpResponse) {
                try {
                    String responseStr = httpResponse.getResultAsString();
                    Gdx.app.log("ImageGen", "Gemini response length: " + responseStr.length());

                    Map<String, Object> response = gson.fromJson(responseStr, Map.class);
                    if (response == null || response.containsKey("error")) {
                        String errorMsg = response != null ? response.get("error").toString() : "Unknown error";
                        Gdx.app.postRunnable(() -> callback.onError("Gemini: " + errorMsg));
                        return;
                    }

                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                        Map<String, Object> contentResp = (Map<String, Object>) candidates.get(0).get("content");
                        if (contentResp != null) {
                            List<Map<String, Object>> partsResp = (List<Map<String, Object>>) contentResp.get("parts");
                            if (partsResp != null) {
                                for (Map<String, Object> part : partsResp) {
                                    Map<String, Object> inlineData = (Map<String, Object>) part.get("inlineData");
                                    if (inlineData != null) {
                                        String b64 = (String) inlineData.get("data");
                                        if (b64 != null) {
                                            byte[] imageBytes = Base64Coder.decode(b64);
                                            Gdx.app.postRunnable(() -> callback.onSuccess(imageBytes));
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Gdx.app.postRunnable(() -> callback.onError("No image data in Gemini response"));
                } catch (Exception e) {
                    Gdx.app.error("ImageGen", "Error parsing Gemini response", e);
                    Gdx.app.postRunnable(() -> callback.onError("Parse error: " + e.getMessage()));
                }
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("ImageGen", "Gemini request failed", t);
                Gdx.app.postRunnable(() -> callback.onError("Request failed: " + t.getMessage()));
            }

            @Override
            public void cancelled() {
                Gdx.app.postRunnable(() -> callback.onError("Request cancelled"));
            }
        });
    }
}
