package com.apogames.aistories.game.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatGPTIO {
    public static final String LLM_MODEL_MINI = "gpt-5.4-mini";
    public static final String LLM_MODEL_GEMINI = "gemini-3-flash-preview";
    public static String API_KEY;
    public static String GEMINI_API_KEY;
    private final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final MainInterface main;
    private final List<Map<String, String>> conversationHistory;
    private final Gson gson = new Gson();
    @Getter
    @Setter
    private String llm;

    // Cache zum Speichern der bereits generierten Bilder (z.B. nach Charakter oder Abschnitt-ID)
    private final Map<String, String> imageCache = new HashMap<>();

    public ChatGPTIO(MainInterface main) {
        this.main = main;
        conversationHistory = new ArrayList<>();
        if (GEMINI_API_KEY != null && !GEMINI_API_KEY.isEmpty() && !GEMINI_API_KEY.equals("Dein Gemini API Key")) {
            this.llm = LLM_MODEL_GEMINI;
        } else {
            this.llm = LLM_MODEL_MINI;
        }
        this.reset();
    }

    private boolean isGeminiModel() {
        return llm != null && llm.startsWith("gemini");
    }

    private void sendMessage(String message) {
        this.main.setRunning(Running.CREATE_STORY);

        // Add the user's message to the conversation history
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        conversationHistory.add(userMessage);

        if (isGeminiModel()) {
            sendGeminiMessage();
        } else {
            sendOpenAIMessage();
        }
    }

    private void sendOpenAIMessage() {
        // Log what we're sending
        for (int i = 0; i < conversationHistory.size(); i++) {
            Map<String, String> msg = conversationHistory.get(i);
            String role = msg.get("role");
            String content = msg.get("content");
            Gdx.app.log("ChatGPT", "Message[" + i + "] role=" + role + " length=" + (content != null ? content.length() : "null"));
            if (content != null && content.length() < 500) {
                Gdx.app.log("ChatGPT", "Message[" + i + "] content: " + content);
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("model", llm);
        params.put("messages", conversationHistory);
        params.put("temperature", 0.7);
        if (llm.startsWith("gpt-5")) {
            params.put("max_completion_tokens", 16384);
        } else {
            params.put("max_tokens", 16384);
        }
        Gdx.app.log("ChatGPT", "Sending to OpenAI: model=" + llm + " messages=" + conversationHistory.size());

        HttpRequestBuilder builder = new HttpRequestBuilder();
        HttpRequest request = builder.newRequest()
                .method(HttpMethods.POST)
                .url(API_URL)
                .timeout(90000)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .content(gson.toJson(params))
                .build();

        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                String responseStr = httpResponse.getResultAsString();
                Gdx.app.log("ChatGPT", "OpenAI HTTP status: " + statusCode + " response: " + responseStr);
                try {
                    Map<String, Object> response = gson.fromJson(responseStr, Map.class);
                    if (response == null) {
                        Gdx.app.error("ChatGPT", "Failed to parse response " + responseStr);
                        main.setRunning(Running.NONE);
                        return;
                    }
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        LinkedTreeMap<String, String> messages = (LinkedTreeMap<String, String>) choices.get(0).get("message");
                        if (messages != null && !messages.isEmpty()) {
                            String content = messages.get("content");
                            Gdx.app.log("ChatGPT", "OpenAI content: " + (content != null ? content.length() + " chars" : "null"));
                            if (content == null || content.trim().isEmpty()) {
                                String refusal = messages.get("refusal");
                                Gdx.app.error("ChatGPT", "OpenAI returned empty content. Refusal: " + refusal);
                                main.setStatusText("Error: GPT refused - " + (refusal != null ? refusal : "unknown"));
                                main.setRunning(Running.NONE);
                                return;
                            }
                            Map<String, String> gptMessage = new HashMap<>();
                            gptMessage.put("role", "assistant");
                            gptMessage.put("content", content);
                            main.setTextForTextArea(content);
                            conversationHistory.add(gptMessage);
                        } else {
                            Gdx.app.error("ChatGPT", "OpenAI message is null/empty. Choice: " + choices.get(0));
                            main.setStatusText("Error: GPT returned no message");
                        }
                    } else {
                        Gdx.app.error("ChatGPT", "OpenAI choices null/empty. Response: " + responseStr);
                        main.setStatusText("Error: GPT returned no choices");
                    }
                } catch (Exception e) {
                    Gdx.app.error("ChatGPT", "Error processing response", e);
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

    private void sendGeminiMessage() {
        // Build Gemini request format
        Map<String, Object> params = new HashMap<>();

        // Convert conversation history to Gemini format
        List<Map<String, Object>> contents = new ArrayList<>();
        String systemText = null;

        for (Map<String, String> msg : conversationHistory) {
            String role = msg.get("role");
            String content = msg.get("content");

            if ("system".equals(role)) {
                systemText = content;
                continue;
            }

            Map<String, Object> geminiMsg = new HashMap<>();
            // Gemini uses "model" instead of "assistant"
            geminiMsg.put("role", "assistant".equals(role) ? "model" : role);

            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> textPart = new HashMap<>();
            textPart.put("text", content);
            parts.add(textPart);
            geminiMsg.put("parts", parts);

            contents.add(geminiMsg);
        }

        params.put("contents", contents);

        // System instruction as separate field
        if (systemText != null) {
            Map<String, Object> systemInstruction = new HashMap<>();
            List<Map<String, String>> sysParts = new ArrayList<>();
            Map<String, String> sysTextPart = new HashMap<>();
            sysTextPart.put("text", systemText);
            sysParts.add(sysTextPart);
            systemInstruction.put("parts", sysParts);
            params.put("systemInstruction", systemInstruction);
        }

        // Generation config
        Map<String, Object> genConfig = new HashMap<>();
        genConfig.put("temperature", 1);
        genConfig.put("maxOutputTokens", 16384);
        params.put("generationConfig", genConfig);

        String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/" + llm + ":generateContent?key=" + GEMINI_API_KEY;

        HttpRequestBuilder builder = new HttpRequestBuilder();
        HttpRequest request = builder.newRequest()
                .method(HttpMethods.POST)
                .url(geminiUrl)
                .timeout(90000)
                .header("Content-Type", "application/json")
                .content(gson.toJson(params))
                .build();

        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse httpResponse) {
                String responseStr = httpResponse.getResultAsString();
                try {
                    Map<String, Object> response = gson.fromJson(responseStr, Map.class);
                    if (response == null) {
                        Gdx.app.error("HTTP", "Failed to parse Gemini response " + responseStr);
                        return;
                    }

                    // Gemini response: candidates[0].content.parts[0].text
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                        Map<String, Object> candidateContent = (Map<String, Object>) candidates.get(0).get("content");
                        if (candidateContent != null) {
                            List<Map<String, String>> parts = (List<Map<String, String>>) candidateContent.get("parts");
                            if (parts != null && !parts.isEmpty()) {
                                String text = parts.get(0).get("text");
                                // Store in OpenAI format for conversation history
                                Map<String, String> assistantMessage = new HashMap<>();
                                assistantMessage.put("role", "assistant");
                                assistantMessage.put("content", text);
                                main.setTextForTextArea(text);
                                conversationHistory.add(assistantMessage);
                            }
                        }
                    }
                } catch (Exception e) {
                    Gdx.app.error("HTTP", "Error processing Gemini response", e);
                }
                main.setRunning(Running.NONE);
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.error("HTTP", "Gemini request failed", t);
                main.setRunning(Running.NONE);
            }

            @Override
            public void cancelled() {
                Gdx.app.log("HTTP", "Gemini request cancelled");
                main.setRunning(Running.NONE);
            }
        });
    }

    public void sendAnotherMessage(String message) {
        sendMessage(message);
    }

    public void reset() {
        this.conversationHistory.clear();

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "system");
        userMessage.put("content", this.main.getPrompt().getSystemSetting());
        conversationHistory.add(userMessage);
    }

    public void resetWithSystemPrompt(String systemPrompt) {
        this.conversationHistory.clear();

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        conversationHistory.add(systemMessage);
    }

}
