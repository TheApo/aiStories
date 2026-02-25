package com.apogames.entity;

import com.apogames.backend.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class ApoButtonMovement extends ApoButtonColor {

    private final int ADD = 15;

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

        if (this.movement == MOVEMENT.UP) {
            this.polygon = new float[6];
            this.polygon[0] = this.getX() + this.getWidth() - ADD;
            this.polygon[1] = this.getY() + this.getHeight() - ADD;

            this.polygon[2] = this.getX() + ADD;
            this.polygon[3] = this.getY() + this.getHeight() - ADD;

            this.polygon[4] = this.getX() + this.getWidth()/2;
            this.polygon[5] = this.getY() + ADD;
        } else if (this.movement == MOVEMENT.DOWN) {
            this.polygon = new float[6];
            this.polygon[0] = this.getX() + this.getWidth() - ADD;
            this.polygon[1] = this.getY() + ADD;

            this.polygon[2] = this.getX() + ADD;
            this.polygon[3] = this.getY() + ADD;

            this.polygon[4] = this.getX() + this.getWidth()/2;
            this.polygon[5] = this.getY() + this.getHeight() - ADD;
        }
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
                Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
                screen.getRenderer().begin(ShapeType.Filled);
                screen.getRenderer().setColor(getColor()[0], getColor()[1], getColor()[2], getColor()[3]);
                screen.getRenderer().roundedRect(this.getX() + rem + changeX, this.getY() + rem + changeY, getWidth(), getHeight(), 3);

                if (movement.equals(MOVEMENT.DELETE)) {
                    float x = this.getX() + rem + changeX;
                    float y = this.getY() + rem + changeY;
                    screen.getRenderer().setColor(getColorBorder()[0], getColorBorder()[1], getColorBorder()[2], 1f);

                    float[] rec = new float[] {
                            x + 5, y + 5,
                            x + 6, y + 3,
                            x + getWidth() - 5, y + getHeight() - 5,
                            x + getWidth() - 6, y + getHeight() - 3
                    };
                    float[] rec2 = new float[] {
                            x + getWidth() - 5, y + 5,
                            x + getWidth() - 6, y + 3,
                            x + 5, y + getHeight() - 5,
                            x + 6, y + getHeight() - 3
                    };

                    screen.getRenderer().fillpolygon(rec);
                    screen.getRenderer().fillpolygon(rec2);
                }

                screen.getRenderer().end();
                Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);


                if (this.movement == MOVEMENT.UP || this.movement == MOVEMENT.DOWN) {
                    screen.getRenderer().begin(ShapeType.Filled);
                    screen.getRenderer().setColor(getColorBorder()[0], getColorBorder()[1], getColorBorder()[2], 1f);
                    if (movement.equals(MOVEMENT.UP)) {
                        screen.getRenderer().fillpolygon(polygon);
                    } else {
                        screen.getRenderer().fillpolygon(polygon);
                    }
                    screen.getRenderer().end();
                } else {
                    Gdx.gl20.glLineWidth(getStroke());
                    screen.getRenderer().begin(ShapeType.Line);
                    screen.getRenderer().setColor(getColorBorder()[0], getColorBorder()[1], getColorBorder()[2], 1f);

                    if (movement.equals(MOVEMENT.RIGHT)) {
                        float x = this.getX() + rem + changeX + getWidth() / 4;
                        float y = this.getY() + rem + changeY + getHeight() / 4;

                        screen.getRenderer().line(x, y, x + getWidth() / 2, y + getWidth() / 4);
                        screen.getRenderer().line(x, y + getWidth() / 2, x + getWidth() / 2, y + getWidth() / 4);
                    } else if (movement.equals(MOVEMENT.LEFT)) {
                        float x = this.getX() + rem + changeX + getWidth() * 3 / 4;
                        float y = this.getY() + rem + changeY + getHeight() / 4;

                        screen.getRenderer().line(x, y, x - getWidth() / 2, y + getWidth() / 4);
                        screen.getRenderer().line(x, y + getWidth() / 2, x - getWidth() / 2, y + getWidth() / 4);
                    }
                    screen.getRenderer().end();
                }

                Gdx.gl20.glLineWidth(getStroke());
                screen.getRenderer().begin(ShapeType.Line);
                screen.getRenderer().setColor(getColorBorder()[0], getColorBorder()[1], getColorBorder()[2], 1f);
                if ((this.isBPressed()) || (isSelect())) {
                    screen.getRenderer().setColor(255f / 255.0f, 0f / 255.0f, 0f / 255.0f, 1f);
                } else if (this.isBOver()) {
                    screen.getRenderer().setColor(255f / 255.0f, 255f / 255.0f, 0f / 255.0f, 1f);
                }
                screen.getRenderer().roundedRectLine(this.getX() + rem + changeX, this.getY() + rem + changeY, getWidth(), getHeight(), 3);
                screen.getRenderer().end();
                Gdx.gl20.glLineWidth(1f);
            }
        }
    }

    public static enum MOVEMENT {LEFT, RIGHT, UP, DOWN, DELETE}
}
