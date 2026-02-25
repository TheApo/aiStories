package com.apogames.entity;

import com.apogames.backend.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * The type Apo button color.
 */
public class ApoButtonTab extends ApoButtonColor {

    /**
     * Instantiates a new Apo button color.
     *
     * @param x           the x
     * @param y           the y
     * @param width       the width
     * @param height      the height
     * @param function    the function
     * @param color       the color
     * @param colorBorder the color border
     */
    public ApoButtonTab(int x, int y, int width, int height, String function, float[] color, float[] colorBorder) {
        this(x, y, width, height, function, "", color, colorBorder);
    }

    /**
     * Instantiates a new Apo button color.
     *
     * @param x           the x
     * @param y           the y
     * @param width       the width
     * @param height      the height
     * @param function    the function
     * @param text        the text
     * @param color       the color
     * @param colorBorder the color border
     */
    public ApoButtonTab(int x, int y, int width, int height, String function, String text, float[] color, float[] colorBorder) {
        super(x, y, width, height, function, text, color, colorBorder);
    }

    public ApoButtonTab(int x, int y, int width, int height, String function, String text, float[] color, float[] colorBorder, String id) {
        super(x, y, width, height, function, text, color, colorBorder, id);
    }

    /**
     * malt den Button an die Stelle getX() + changeX und getY() + changeY hin
     *
     * @param changeX: Verschiebung in x-Richtung
     * @param changeY: Verschiebung in y-Richtung
     */
    public void render(GameScreen screen, int changeX, int changeY) {
        if (this.isVisible()) {
            if (!this.isOnlyText()) {
                int rem = 0;
                if (getStroke() > 1) {
                    rem = getStroke() / 2;
                }
                if (getColor()[3] < 1f) {
                    Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
                }
                if (!this.isSelect()) {
                    screen.getRenderer().begin(ShapeType.Filled);
                    screen.getRenderer().setColor(getColor()[0], getColor()[1], getColor()[2], getColor()[3]);
                    screen.getRenderer().roundedRect(this.getX() + rem + changeX, this.getY() + rem + changeY, getWidth(), getHeight(), 3);
                    screen.getRenderer().rect(this.getX() + rem + changeX, this.getY() + rem + changeY + getHeight() - 4, getWidth(), 4);
                    screen.getRenderer().end();
                    if (getColor()[3] < 1f) {
                        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);
                    }

                    Gdx.gl20.glLineWidth(getStroke());
                    screen.getRenderer().begin(ShapeType.Line);
                    screen.getRenderer().setColor(getColorBorder()[0], getColorBorder()[1], getColorBorder()[2], 1f);
                    if ((this.isBPressed()) || (isSelect())) {
                        screen.getRenderer().setColor(1.0f, 0f / 255.0f, 0f / 255.0f, 1f);
                    } else if (this.isBOver()) {
                        screen.getRenderer().setColor(1.0f, 1.0f, 0f / 255.0f, 1f);
                    }
                    screen.getRenderer().roundedRectLine(this.getX() + rem + changeX, this.getY() + rem + changeY, getWidth(), getHeight(), 3);
                    screen.getRenderer().end();
                    Gdx.gl20.glLineWidth(1f);


                    screen.getRenderer().begin(ShapeType.Filled);
                    screen.getRenderer().setColor(getColor()[0], getColor()[1], getColor()[2], getColor()[3]);
                    screen.getRenderer().rect(this.getX() + rem + changeX, this.getY() + rem + changeY + getHeight() - 4, getWidth(), 4);
                    screen.getRenderer().end();
                    if (getColor()[3] < 1f) {
                        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);
                    }

                    Gdx.gl20.glLineWidth(getStroke());
                    screen.getRenderer().begin(ShapeType.Line);
                    screen.getRenderer().setColor(getColorBorder()[0], getColorBorder()[1], getColorBorder()[2], 1f);
                    if ((this.isBPressed()) || (isSelect())) {
                        screen.getRenderer().setColor(1.0f, 0f / 255.0f, 0f / 255.0f, 1f);
                    } else if (this.isBOver()) {
                        screen.getRenderer().setColor(1.0f, 1.0f, 0f / 255.0f, 1f);
                    }

                    screen.getRenderer().line(this.getX() + rem + changeX, this.getY() + rem + changeY + getHeight() - 4, this.getX() + rem + changeX, this.getY() + rem + changeY + getHeight());
                    screen.getRenderer().line(this.getX() + rem + changeX + getWidth() - 1, this.getY() + rem + changeY + getHeight() - 4, this.getX() + rem + changeX + getWidth() - 1, this.getY() + rem + changeY + getHeight());
                    if (!this.isSelect()) {
                        screen.getRenderer().line(this.getX() + rem + changeX, this.getY() + rem + changeY + getHeight(), this.getX() + rem + changeX + getWidth(), this.getY() + rem + changeY + getHeight());
                    }
                    screen.getRenderer().end();
                    Gdx.gl20.glLineWidth(1f);
                }

                if (this.isSolved()) {
                    if (getSolvedImage() != null) {
                        screen.spriteBatch.begin();
                        screen.spriteBatch.enableBlending();
                        renderSolvedImage(screen, changeX, changeY);
                        screen.spriteBatch.end();
                        changeY -= getHeight() / 6f;
                    }
                }

                if (getImage() != null) {
                    screen.spriteBatch.begin();
                    screen.spriteBatch.enableBlending();
                    renderImage(screen, changeX, changeY);
                    screen.spriteBatch.end();
                } else {
                    if (isSolved()) {
                        drawString(screen, changeX, changeY - 9, getColorBorderSolved());
                    } else {
                        drawString(screen, changeX, changeY - 2, getColorBorder());
                    }
                }
            }
        }
    }
}
