package com.apogames.aistories.game.listenStories;

import com.apogames.entity.ApoEntity;

import java.util.Random;

public class DecorativeWaveform extends ApoEntity {

    public static final int BAR_COUNT = 120;

    private final float[] heights = new float[BAR_COUNT];

    public DecorativeWaveform(float x, float y, float width, float height, String seed) {
        super(x, y, width, height);
        long s = seed == null ? 0L : seed.hashCode();
        Random r = new Random(s);
        for (int i = 0; i < BAR_COUNT; i++) {
            float t = (float) i / (BAR_COUNT - 1);
            float env = 0.45f + 0.5f * (float) Math.sin(Math.PI * t);
            float slow = 0.6f + 0.4f * (float) Math.sin(t * 6.28f * (1.0f + r.nextFloat() * 2.0f) + r.nextFloat() * 6.28f);
            float noise = 0.55f + 0.45f * r.nextFloat();
            float v = env * slow * noise;
            if (v < 0.12f) v = 0.12f;
            if (v > 1.0f) v = 1.0f;
            heights[i] = v;
        }
    }

    public float getBarHeight(int index) {
        return heights[index];
    }

    public void setHeights(float[] newHeights) {
        if (newHeights == null) return;
        int n = Math.min(newHeights.length, heights.length);

        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            float v = newHeights[i];
            if (v < 0f) v = 0f;
            if (v > 1f) v = 1f;
            if (v < min) min = v;
            if (v > max) max = v;
        }

        boolean stretch = min > 0.2f && max > min;
        float range = max - min;

        for (int i = 0; i < n; i++) {
            float v = newHeights[i];
            if (v < 0f) v = 0f;
            if (v > 1f) v = 1f;
            if (stretch) {
                v = 0.2f + (v - min) / range * 0.8f;
            }
            if (v < 0.06f) v = 0.06f;
            if (v > 1.0f) v = 1.0f;
            heights[i] = v;
        }
    }

    public float xForPosition(float position, float duration) {
        if (duration <= 0f) return getX();
        float ratio = position / duration;
        if (ratio < 0f) ratio = 0f;
        if (ratio > 1f) ratio = 1f;
        return getX() + ratio * getWidth();
    }

    public float positionForX(float mouseX, float duration) {
        float ratio = (mouseX - getX()) / getWidth();
        if (ratio < 0f) ratio = 0f;
        if (ratio > 1f) ratio = 1f;
        return ratio * duration;
    }
}
