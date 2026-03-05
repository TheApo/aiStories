package com.apogames.entity;

import com.apogames.backend.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class ApoButtonMovement extends ApoButtonColor {

    private MOVEMENT movement;

    private float[] polygon = new float[0];

    public ApoButtonMovement(int x, int y, int width, int height, String function, float[] color, float[] colorBorder) {
        super(x, y, width, height, function, "", color, colorBorder);

        this.movement = MOVEMENT.UP;
    }

    public MOVEMENT getMovement() {
        return movement;
    }

    public void setMovement(MOVEMENT movement) {
        this.movement = movement;
    }

    public void render(GameScreen screen, int changeX, int changeY) {
        if (!this.isVisible() || this.isOnlyText()) return;

        float bx = this.getX() + changeX;
        float by = this.getY() + changeY;
        float w = getWidth();
        float h = getHeight();
        float radius = h / 2f;

        if (this.movement == MOVEMENT.DELETE) {
            renderDelete(screen, bx, by, w, h);
            return;
        }

        if (this.movement == MOVEMENT.LEFT || this.movement == MOVEMENT.RIGHT) {
            renderLeftRight(screen, bx, by, w, h);
            return;
        }

        // UP / DOWN: dark pill-shaped button with bold chevron arrow
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        screen.getRenderer().begin(ShapeType.Filled);

        // Darkened column color as background
        float darken = 0.35f;
        float cr = Math.max(0f, getColor()[0] - darken);
        float cg = Math.max(0f, getColor()[1] - darken);
        float cb = Math.max(0f, getColor()[2] - darken);
        float bgAlpha = this.isBOver() ? 1f : 0.85f;
        screen.getRenderer().setColor(cr, cg, cb, bgAlpha);
        screen.getRenderer().roundedRect(bx, by, w, h, radius);

        // Bold chevron arrow in column color
        float cx = bx + w / 2f;
        float cy = by + h / 2f;
        float arrowW = w * 0.32f;
        float arrowH = h * 0.30f;
        float thickness = 5f;

        if (this.movement == MOVEMENT.UP) {
            drawChevron(screen, cx, cy, arrowW, arrowH, thickness, true);
        } else {
            drawChevron(screen, cx, cy, arrowW, arrowH, thickness, false);
        }

        screen.getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        // Border on hover/press
        if (this.isBOver() || this.isBPressed()) {
            Gdx.gl20.glLineWidth(2f);
            screen.getRenderer().begin(ShapeType.Line);
            if (this.isBPressed()) {
                screen.getRenderer().setColor(1f, 0f, 0f, 1f);
            } else {
                screen.getRenderer().setColor(1f, 1f, 0f, 1f);
            }
            screen.getRenderer().roundedRectLine(bx, by, w, h, radius);
            screen.getRenderer().end();
            Gdx.gl20.glLineWidth(1f);
        }
    }

    private void drawChevron(GameScreen screen, float cx, float cy, float halfW, float halfH, float t, boolean up) {
        screen.getRenderer().setColor(1f, 1f, 1f, 1f);
        float tipY = up ? cy - halfH : cy + halfH;
        float baseY = up ? cy + halfH : cy - halfH;

        // Left arm of chevron
        float[] left = new float[]{
                cx - halfW, baseY,
                cx - halfW + t, baseY,
                cx + t / 2f, tipY,
                cx - t / 2f, tipY
        };
        // Right arm of chevron
        float[] right = new float[]{
                cx + halfW, baseY,
                cx + halfW - t, baseY,
                cx + t / 2f, tipY,
                cx - t / 2f, tipY
        };
        screen.getRenderer().fillpolygon(left);
        screen.getRenderer().fillpolygon(right);
    }

    private void renderDelete(GameScreen screen, float bx, float by, float w, float h) {
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        screen.getRenderer().begin(ShapeType.Filled);
        screen.getRenderer().setColor(getColor()[0], getColor()[1], getColor()[2], getColor()[3]);
        screen.getRenderer().roundedRect(bx, by, w, h, 3);

        screen.getRenderer().setColor(getColorBorder()[0], getColorBorder()[1], getColorBorder()[2], 1f);
        float[] rec = new float[]{
                bx + 5, by + 5,
                bx + 6, by + 3,
                bx + w - 5, by + h - 5,
                bx + w - 6, by + h - 3
        };
        float[] rec2 = new float[]{
                bx + w - 5, by + 5,
                bx + w - 6, by + 3,
                bx + 5, by + h - 5,
                bx + 6, by + h - 3
        };
        screen.getRenderer().fillpolygon(rec);
        screen.getRenderer().fillpolygon(rec2);
        screen.getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        Gdx.gl20.glLineWidth(getStroke());
        screen.getRenderer().begin(ShapeType.Line);
        screen.getRenderer().setColor(getColorBorder()[0], getColorBorder()[1], getColorBorder()[2], 1f);
        if (this.isBPressed() || isSelect()) {
            screen.getRenderer().setColor(1f, 0f, 0f, 1f);
        } else if (this.isBOver()) {
            screen.getRenderer().setColor(1f, 1f, 0f, 1f);
        }
        screen.getRenderer().roundedRectLine(bx, by, w, h, 3);
        screen.getRenderer().end();
        Gdx.gl20.glLineWidth(1f);
    }

    private void renderLeftRight(GameScreen screen, float bx, float by, float w, float h) {
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        screen.getRenderer().begin(ShapeType.Filled);
        screen.getRenderer().setColor(getColor()[0], getColor()[1], getColor()[2], getColor()[3]);
        screen.getRenderer().roundedRect(bx, by, w, h, 3);
        screen.getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        Gdx.gl20.glLineWidth(getStroke());
        screen.getRenderer().begin(ShapeType.Line);
        screen.getRenderer().setColor(getColorBorder()[0], getColorBorder()[1], getColorBorder()[2], 1f);

        if (movement == MOVEMENT.RIGHT) {
            float x = bx + w / 4;
            float y = by + h / 4;
            screen.getRenderer().line(x, y, x + w / 2, y + w / 4);
            screen.getRenderer().line(x, y + w / 2, x + w / 2, y + w / 4);
        } else {
            float x = bx + w * 3 / 4;
            float y = by + h / 4;
            screen.getRenderer().line(x, y, x - w / 2, y + w / 4);
            screen.getRenderer().line(x, y + w / 2, x - w / 2, y + w / 4);
        }
        screen.getRenderer().end();

        screen.getRenderer().begin(ShapeType.Line);
        screen.getRenderer().setColor(getColorBorder()[0], getColorBorder()[1], getColorBorder()[2], 1f);
        if (this.isBPressed() || isSelect()) {
            screen.getRenderer().setColor(1f, 0f, 0f, 1f);
        } else if (this.isBOver()) {
            screen.getRenderer().setColor(1f, 1f, 0f, 1f);
        }
        screen.getRenderer().roundedRectLine(bx, by, w, h, 3);
        screen.getRenderer().end();
        Gdx.gl20.glLineWidth(1f);
    }

    public static enum MOVEMENT {LEFT, RIGHT, UP, DOWN, DELETE}
}
