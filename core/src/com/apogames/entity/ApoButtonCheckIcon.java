package com.apogames.entity;

import com.apogames.backend.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class ApoButtonCheckIcon extends ApoButton {

    private final float[] circleColor;
    private final float[] iconColor;
    private boolean checked;

    public ApoButtonCheckIcon(int x, int y, int size, String function, float[] circleColor, float[] iconColor) {
        super(x, y, size, size, function, "");
        this.circleColor = circleColor;
        this.iconColor = iconColor;
        this.checked = true;
        this.setStroke(2);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void toggle() {
        this.checked = !this.checked;
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

        // Circle background — same as edit button
        screen.getRenderer().setColor(circleColor[0], circleColor[1], circleColor[2], 0.95f);
        screen.getRenderer().circle(cx, cy, radius);

        // Checkmark when checked
        if (checked) {
            drawCheckmark(screen, cx, cy);
        }

        screen.getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        // Circle border — same hover/press behavior as edit button
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

    private void drawCheckmark(GameScreen screen, float cx, float cy) {
        screen.getRenderer().setColor(iconColor[0], iconColor[1], iconColor[2], 1f);
        float s = getWidth() / 36f;
        // Checkmark: short stroke down-right, then long stroke up-right
        screen.getRenderer().rectLine(cx - 8 * s, cy - 1 * s, cx - 2 * s, cy + 7 * s, 3.5f * s);
        screen.getRenderer().rectLine(cx - 2 * s, cy + 7 * s, cx + 10 * s, cy - 7 * s, 3.5f * s);
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
