package com.apogames.aistories.game.listenStories;

public class PageTurnAnimation {

    public enum Direction { NONE, FORWARD, BACKWARD }

    private static final float DURATION_MS = 800f;

    private Direction direction = Direction.NONE;
    private float elapsed = 0f;

    public boolean isAnimating() {
        return direction != Direction.NONE;
    }

    public void start(Direction dir) {
        this.direction = dir;
        this.elapsed = 0f;
    }

    public boolean update(float deltaMs) {
        if (!isAnimating()) return false;
        elapsed += deltaMs;
        if (elapsed >= DURATION_MS) {
            direction = Direction.NONE;
            elapsed = 0f;
            return true;
        }
        return false;
    }

    public float getProgress() {
        float t = Math.min(elapsed / DURATION_MS, 1f);
        return t * t * (3f - 2f * t);
    }

    public Direction getDirection() {
        return direction;
    }
}
