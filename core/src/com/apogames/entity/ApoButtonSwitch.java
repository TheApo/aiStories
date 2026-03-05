package com.apogames.entity;

import com.apogames.aistories.Constants;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.DrawString;
import com.apogames.backend.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class ApoButtonSwitch extends ApoButtonColor {

    private String leftLabel;
    private String rightLabel;
    private String singleLabel;

    public ApoButtonSwitch(int x, int y, int width, int height, String function, float[] color, float[] colorBorder) {
        super(x, y, width, height, function, "", color, colorBorder);
    }

    public ApoButtonSwitch(int x, int y, int width, int height, String function, String text, float[] color, float[] colorBorder) {
        super(x, y, width, height, function, text, color, colorBorder);
    }

    public void setLabels(String leftLabel, String rightLabel) {
        this.leftLabel = leftLabel;
        this.rightLabel = rightLabel;
        this.singleLabel = null;
    }

    public void setSingleLabel(String label) {
        this.singleLabel = label;
        this.leftLabel = null;
        this.rightLabel = null;
    }

    public void render(GameScreen screen, int changeX, int changeY) {
        if (!this.isVisible()) return;
        if (this.isOnlyText()) return;

        float bx = this.getX() + changeX;
        float by = this.getY() + changeY;
        float w = getWidth();
        float h = getHeight();

        BitmapFont labelFont = AssetLoader.font20;

        // Single label mode: just background panel + centered text, no track/knob
        if (singleLabel != null) {
            Constants.glyphLayout.setText(labelFont, singleLabel);
            float textW = Constants.glyphLayout.width;
            float panelPad = 20;
            float panelW = textW + 2 * panelPad;
            float panelH = h + 14;
            float panelX = bx + w / 2f - panelW / 2f;
            float panelY = by - 7;

            Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
            screen.getRenderer().begin(ShapeType.Filled);
            screen.getRenderer().setColor(0f, 0f, 0f, 0.55f);
            screen.getRenderer().roundedRect(panelX, panelY, panelW, panelH, panelH / 2f);
            screen.getRenderer().end();
            Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

            screen.spriteBatch.begin();
            screen.drawString(singleLabel, bx + w / 2f, by + h / 2f, Constants.COLOR_YELLOW, labelFont, DrawString.MIDDLE, true, false);
            screen.spriteBatch.end();
            return;
        }

        boolean hasLabels = leftLabel != null && rightLabel != null;
        float radius = h / 2f;
        float knobRadius = radius - 4;

        float leftW = 0;
        float rightW = 0;
        float labelGap = 8;

        if (hasLabels) {
            Constants.glyphLayout.setText(labelFont, leftLabel);
            leftW = Constants.glyphLayout.width;
            Constants.glyphLayout.setText(labelFont, rightLabel);
            rightW = Constants.glyphLayout.width;
        }

        float panelPad = 10;
        float panelX = bx - labelGap - leftW - panelPad;
        float panelW = panelPad + leftW + labelGap + w + labelGap + rightW + panelPad;
        float panelY = by - 7;
        float panelH = h + 14;

        // Background panel behind labels
        if (hasLabels) {
            Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
            screen.getRenderer().begin(ShapeType.Filled);
            screen.getRenderer().setColor(0f, 0f, 0f, 0.55f);
            screen.getRenderer().roundedRect(panelX, panelY, panelW, panelH, panelH / 2f);
            screen.getRenderer().end();
            Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);
        }

        // Track (pill shape, neutral color)
        screen.getRenderer().begin(ShapeType.Filled);
        screen.getRenderer().setColor(0.4f, 0.4f, 0.4f, 1f);
        screen.getRenderer().roundedRect(bx, by, w, h, radius);

        // Knob
        screen.getRenderer().setColor(1f, 1f, 1f, 1f);
        if (!this.isSelect()) {
            screen.getRenderer().circle(bx + radius, by + radius, knobRadius);
        } else {
            screen.getRenderer().circle(bx + w - radius, by + radius, knobRadius);
        }
        screen.getRenderer().end();

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

        // Labels: left ends at switch, right starts at switch
        if (hasLabels) {
            float textY = by + h / 2f;
            float[] leftColor = this.isSelect() ? Constants.COLOR_GREY_BRIGHT : Constants.COLOR_YELLOW;
            float[] rightColor = this.isSelect() ? Constants.COLOR_YELLOW : Constants.COLOR_GREY_BRIGHT;

            screen.spriteBatch.begin();
            screen.drawString(leftLabel, bx - labelGap, textY, leftColor, labelFont, DrawString.END, true, false);
            screen.drawString(rightLabel, bx + w + labelGap, textY, rightColor, labelFont, DrawString.BEGIN, true, false);
            screen.spriteBatch.end();
        }
    }

    /** @deprecated Use setLabels() and let render() handle everything. */
    public void renderText(GameScreen screen, int changeX, int changeY, String llm) {
        // kept for backwards compatibility but no longer needed
    }
}
