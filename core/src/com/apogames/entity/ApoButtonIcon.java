package com.apogames.entity;

import com.apogames.backend.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class ApoButtonIcon extends ApoButton {

    public enum IconType { EDIT, SEARCH }

    private final float[] circleColor;
    private final float[] iconColor;
    private IconType iconType;

    public ApoButtonIcon(int x, int y, int size, String function, float[] circleColor, float[] iconColor, IconType iconType) {
        super(x, y, size, size, function, "");
        this.circleColor = circleColor;
        this.iconColor = iconColor;
        this.iconType = iconType;
        this.setStroke(2);
    }

    public IconType getIconType() {
        return iconType;
    }

    public void setIconType(IconType iconType) {
        this.iconType = iconType;
    }

    @Override
    public void render(GameScreen screen, int changeX, int changeY) {
        if (!this.isVisible()) {
            return;
        }

        float cx = this.getX() + changeX + getWidth() / 2f;
        float cy = this.getY() + changeY + getHeight() / 2f;
        float radius = getWidth() / 2f;

        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        screen.getRenderer().begin(ShapeType.Filled);

        // Shadow
        screen.getRenderer().setColor(0f, 0f, 0f, 0.25f);
        screen.getRenderer().circle(cx + 1, cy + 1, radius);

        // Filled circle background
        screen.getRenderer().setColor(circleColor[0], circleColor[1], circleColor[2], 0.95f);
        screen.getRenderer().circle(cx, cy, radius);

        // Icon
        if (iconType == IconType.SEARCH) {
            drawSearchFilled(screen, cx, cy);
        } else {
            drawPencilFilled(screen, cx, cy);
        }

        screen.getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        // Circle border
        Gdx.gl20.glLineWidth(2f);
        screen.getRenderer().begin(ShapeType.Line);
        if (this.isBPressed()) {
            screen.getRenderer().setColor(1f, 0f, 0f, 1f);
        } else if (this.isBOver()) {
            screen.getRenderer().setColor(1f, 1f, 0f, 1f);
        } else {
            screen.getRenderer().setColor(iconColor[0], iconColor[1], iconColor[2], 0.8f);
        }
        screen.getRenderer().circle(cx, cy, radius);
        screen.getRenderer().end();
        Gdx.gl20.glLineWidth(1f);
    }

    private void drawPencilFilled(GameScreen screen, float cx, float cy) {
        screen.getRenderer().setColor(iconColor[0], iconColor[1], iconColor[2], 1f);

        float s = getWidth() / 48f;

        float bodyX1 = cx - 6 * s;
        float bodyY1 = cy + 6 * s;
        float bodyX2 = cx + 10 * s;
        float bodyY2 = cy - 10 * s;
        screen.getRenderer().rectLine(bodyX1, bodyY1, bodyX2, bodyY2, 8 * s);

        float tipX = cx - 12 * s;
        float tipY = cy + 12 * s;
        float perpX = 3f * s;
        float perpY = 3f * s;
        screen.getRenderer().triangle(
                tipX, tipY,
                bodyX1 - perpX, bodyY1 - perpY,
                bodyX1 + perpX, bodyY1 + perpY
        );

        float eraserX1 = cx + 8 * s;
        float eraserY1 = cy - 8 * s;
        float eraserX2 = cx + 13 * s;
        float eraserY2 = cy - 13 * s;
        screen.getRenderer().rectLine(eraserX1, eraserY1, eraserX2, eraserY2, 8 * s);

        screen.getRenderer().setColor(circleColor[0], circleColor[1], circleColor[2], 0.7f);
        screen.getRenderer().rectLine(
                cx + 8 * s - 3 * s, cy - 8 * s - 3 * s,
                cx + 8 * s + 3 * s, cy - 8 * s + 3 * s,
                1.5f * s
        );
    }

    private void drawSearchFilled(GameScreen screen, float cx, float cy) {
        float s = getWidth() / 48f;

        // Lens center — upper-right (y+ = up in screen coords)
        float lx = cx + 3 * s;
        float ly = cy - 3 * s;
        float outerR = 8 * s;
        float innerR = 5.5f * s;

        // Outer lens ring
        screen.getRenderer().setColor(iconColor[0], iconColor[1], iconColor[2], 1f);
        screen.getRenderer().circle(lx, ly, outerR);

        // Inner hole (background color)
        screen.getRenderer().setColor(circleColor[0], circleColor[1], circleColor[2], 0.95f);
        screen.getRenderer().circle(lx, ly, innerR);

        // Handle — from lower-left edge of lens toward lower-left
        screen.getRenderer().setColor(iconColor[0], iconColor[1], iconColor[2], 1f);
        float hx1 = lx - outerR * 0.6f;
        float hy1 = ly + outerR * 0.6f;
        float hx2 = hx1 - 9 * s;
        float hy2 = hy1 + 9 * s;
        screen.getRenderer().rectLine(hx1, hy1, hx2, hy2, 3.5f * s);
    }

    @Override
    public boolean intersects(float x, float y) {
        float cx = this.getX() + getWidth() / 2f;
        float cy = this.getY() + getHeight() / 2f;
        float radius = getWidth() / 2f;
        float dx = x - cx;
        float dy = y - cy;
        return (dx * dx + dy * dy) <= (radius * radius);
    }
}
