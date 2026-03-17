package com.apogames.aistories.game.listenStories;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WordTimingData {

    @Getter
    private final List<WordTiming> words;

    public WordTimingData(List<WordTiming> words) {
        this.words = words;
    }

    public static WordTimingData fromAlignment(List<String> characters, List<Float> startTimes, List<Float> endTimes) {
        List<WordTiming> words = new ArrayList<>();
        StringBuilder currentWord = new StringBuilder();
        float wordStart = -1;
        float wordEnd = 0;

        for (int i = 0; i < characters.size(); i++) {
            String ch = characters.get(i);
            if (ch.equals(" ") || ch.equals("\n")) {
                if (currentWord.length() > 0) {
                    words.add(new WordTiming(currentWord.toString(), wordStart, wordEnd));
                    currentWord.setLength(0);
                    wordStart = -1;
                }
            } else {
                if (wordStart < 0) {
                    wordStart = startTimes.get(i);
                }
                wordEnd = endTimes.get(i);
                currentWord.append(ch);
            }
        }
        if (currentWord.length() > 0) {
            words.add(new WordTiming(currentWord.toString(), wordStart, wordEnd));
        }

        return new WordTimingData(words);
    }

    public void saveToFile(String jsonPath) {
        try {
            String json = new Gson().toJson(words);
            FileHandle file = Gdx.files.local(jsonPath);
            file.writeString(json, false);
        } catch (Exception e) {
            Gdx.app.log("WordTimingData", "Failed to save: " + e.getMessage());
        }
    }

    public static WordTimingData loadFromFile(String jsonPath) {
        try {
            FileHandle file = Gdx.files.local(jsonPath);
            if (!file.exists()) return null;
            String json = file.readString();
            Type listType = new TypeToken<List<WordTiming>>() {}.getType();
            List<WordTiming> words = new Gson().fromJson(json, listType);
            return new WordTimingData(words);
        } catch (Exception e) {
            Gdx.app.log("WordTimingData", "Failed to load: " + e.getMessage());
            return null;
        }
    }

    public float getDuration() {
        if (words.isEmpty()) return 0f;
        return words.get(words.size() - 1).endTime;
    }

    public int getCurrentWordIndex(float playbackSeconds) {
        List<WordTiming> w = this.words;
        if (w.isEmpty()) return -1;

        // Binary search
        int low = 0, high = w.size() - 1, result = -1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (w.get(mid).startTime <= playbackSeconds) {
                result = mid;
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        if (result >= 0 && playbackSeconds <= w.get(result).endTime) {
            return result;
        }
        return -1;
    }

    @Getter
    public static class WordTiming {
        private final String word;
        private final float startTime;
        private final float endTime;

        public WordTiming(String word, float startTime, float endTime) {
            this.word = word;
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}
