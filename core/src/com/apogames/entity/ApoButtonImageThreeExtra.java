package com.apogames.entity;

import com.apogames.asset.AssetLoader;
import com.apogames.backend.GameScreen;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lombok.Getter;

/**
 * The type Apo button image three.
 */
public class ApoButtonImageThreeExtra extends ApoButtonImageThree {

    public enum EXTRA {PLAY, NEXT, PREV, STOP, PAUSE, PLUS, MINUS, UP, DOWN}

    private final int ADD = 20;

    @Getter
    private EXTRA extra = EXTRA.PLAY;

    private float[] polygon = new float[0];

    /**
     * Instantiates a new Apo button image three.
     *
     * @param x        the x
     * @param y        the y
     * @param width    the width
     * @param height   the height
     * @param function the function
     * @param text     the text
     */
    public ApoButtonImageThreeExtra(int x, int y, int width, int height, String function, String text, float[] color) {
        this(x, y, width, height, function, text, 0, 0, AssetLoader.buttonBlancoTextureRegion[0].getRegionWidth(), AssetLoader.buttonBlancoTextureRegion[0].getRegionHeight(), color, null);
    }

    public ApoButtonImageThreeExtra(int x, int y, int width, int height, String function, String text, int assetX, int assetY, int picWidth, int picHeight, float[] color, String id) {
        super(x, y, width, height, function, text, assetX, assetY, picWidth, picHeight, color, id);

        this.setExtra(EXTRA.PLAY);
    }

    public void setExtra(EXTRA extra) {
        this.extra = extra;

        if ((this.extra == EXTRA.PLAY) || (this.extra == EXTRA.NEXT)) {
            this.polygon = new float[6];
            this.polygon[0] = this.getX() + ADD;
            this.polygon[1] = this.getY() + ADD;

            this.polygon[2] = this.getX() + this.getWidth() - ADD;
            this.polygon[3] = this.getY() + this.getHeight()/2;

            this.polygon[4] = this.getX() + ADD;
            this.polygon[5] = this.getY() + this.getHeight() - ADD;
        } else if (this.extra == EXTRA.PREV) {
            this.polygon = new float[6];
            this.polygon[0] = this.getX() + this.getWidth() - ADD;
            this.polygon[1] = this.getY() + ADD;

            this.polygon[2] = this.getX() + ADD;
            this.polygon[3] = this.getY() + this.getHeight()/2;

            this.polygon[4] = this.getX() + this.getWidth() - ADD;
            this.polygon[5] = this.getY() + this.getHeight() - ADD;
        } else if (this.extra == EXTRA.STOP) {
            this.polygon = new float[8];

            this.polygon[0] = this.getX() + ADD;
            this.polygon[1] = this.getY() + ADD;

            this.polygon[2] = this.getX() + ADD;
            this.polygon[3] = this.getY() + this.getHeight() - ADD;

            this.polygon[4] = this.getX() + this.getWidth() - ADD;
            this.polygon[5] = this.getY() + this.getHeight() - ADD;

            this.polygon[6] = this.getX() + this.getWidth() - ADD;
            this.polygon[7] = this.getY() + ADD;
        } else if (this.extra == EXTRA.UP) {
            this.polygon = new float[6];
            this.polygon[0] = this.getX() + this.getWidth() - ADD;
            this.polygon[1] = this.getY() + this.getHeight() - ADD;

            this.polygon[2] = this.getX() + ADD;
            this.polygon[3] = this.getY() + this.getHeight() - ADD;

            this.polygon[4] = this.getX() + this.getWidth()/2;
            this.polygon[5] = this.getY() + ADD;
        } else if (this.extra == EXTRA.DOWN) {
            this.polygon = new float[6];
            this.polygon[0] = this.getX() + this.getWidth() - ADD;
            this.polygon[1] = this.getY() + ADD;

            this.polygon[2] = this.getX() + ADD;
            this.polygon[3] = this.getY() + ADD;

            this.polygon[4] = this.getX() + this.getWidth()/2;
            this.polygon[5] = this.getY() + this.getHeight() - ADD;
        }
    }

    public void render(GameScreen screen, int changeX, int changeY, boolean bShowTextOnly) {
        if (this.isVisible()) {
            screen.spriteBatch.begin();
            renderImage(screen, changeX, changeY);
            screen.spriteBatch.end();

            screen.getRenderer().begin(ShapeRenderer.ShapeType.Filled);
            screen.getRenderer().setColor(getColor()[0], getColor()[1], getColor()[2], getColor()[3]);
            if (this.extra == EXTRA.PAUSE) {
                screen.getRenderer().rect(this.getX() + this.getWidth()/2 - 10, this.getY() + ADD, 5, this.getHeight() - 2 * ADD);
                screen.getRenderer().rect(this.getX() + this.getWidth()/2  + 5, this.getY() + ADD, 5, this.getHeight() - 2 * ADD);
            } else if (this.extra == EXTRA.PLUS) {
                screen.getRenderer().rect(this.getX() + this.getWidth()/2 - 3, this.getY() + ADD, 6, this.getHeight() - 2 * ADD);
                screen.getRenderer().rect(this.getX() + ADD, this.getY() + getHeight()/2 - 3, this.getWidth() - 2 * ADD, 6);
            } else if (this.extra == EXTRA.MINUS) {
                screen.getRenderer().rect(this.getX() + ADD, this.getY() + getHeight()/2 - 3, this.getWidth() - 2 * ADD, 6);
            } else {
                screen.getRenderer().fillpolygon(polygon);
            }
            screen.getRenderer().end();
        }
    }

}
