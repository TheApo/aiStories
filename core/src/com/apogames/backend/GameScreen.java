/*
 * Copyright (c) 2005-2017 Dirk Aporius <dirk.aporius@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.apogames.backend;

import com.apogames.aistories.Constants;
import com.apogames.asset.AssetLoader;
import com.apogames.entity.ApoButton;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

/**
 * The type Game screen.
 */
public class GameScreen implements Screen, InputProcessor {

    public static boolean headless = false;

    protected final OrthographicCamera cam;
    private final ArrayList<GridPoint2> clickReleasedArray = new ArrayList<GridPoint2>();
    private final ArrayList<GridPoint2> clickDraggedArray = new ArrayList<GridPoint2>();
    private final ArrayList<GridPoint2> clickPressedArray = new ArrayList<GridPoint2>();
    private final ArrayList<GridPoint2> mouseMovedArray = new ArrayList<GridPoint2>();
    private final ArrayList<Integer> keyPressedArray = new ArrayList<Integer>();
    private final ArrayList<Integer> keyReleasedArray = new ArrayList<Integer>();
    private final ArrayList<Character> keyTypedArray = new ArrayList<Character>();
    /**
     * The Sprite batch.
     */
    public SpriteBatch spriteBatch;
    protected ScreenModel model;
    private MyShapeRenderer renderer;
    private FitViewport viewport;
    private int lastViewPortResizeWidth = 800;
    private int lastViewPortResizeHeight = 480;
    private GlyphLayout glyphLayout;
    private ArrayList<ApoButton> buttons;
    private String buttonFunction = null;
    private boolean bRightClick = false;

    /**
     * Instantiates a new Game screen.
     */
    public GameScreen() {
        cam = new OrthographicCamera();

        if (!headless) {
            this.spriteBatch = new SpriteBatch();
            this.glyphLayout = new GlyphLayout();

            resetSize(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
            viewport.update(lastViewPortResizeWidth, lastViewPortResizeHeight);
        }

        Gdx.input.setInputProcessor(this);
        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        this.buttons = new ArrayList<ApoButton>();
    }

    /**
     * Init.
     */
    public void init() {

    }

    public GlyphLayout getGlyphLayout() {
        return glyphLayout;
    }

    /**
     * Gets buttons.
     *
     * @return the buttons
     */
    public ArrayList<ApoButton> getButtons() {
        return buttons;
    }

    /**
     * Sets buttons.
     *
     * @param buttons the buttons
     */
    public void setButtons(ArrayList<ApoButton> buttons) {
        this.buttons = buttons;
    }

    /**
     * Gets button by function.
     *
     * @param function the function
     * @return the button by function
     */
    public ApoButton getButtonByFunction(String function) {
        for (ApoButton apoButton : buttons) {
            if (apoButton.getFunction().equals(function)) {
                return apoButton;
            }
        }
        return null;
    }

    /**
     * Reset size.
     *
     * @param width  the width
     * @param height the height
     */
    public void resetSize(final int width, final int height) {
        cam.setToOrtho(true, width, height);
        viewport = new FitViewport(width, height, cam);
        if (headless) {
            return;
        }
        if (renderer == null) {
            renderer = new MyShapeRenderer();
        }
        applyResize(lastViewPortResizeWidth, lastViewPortResizeHeight);
    }

    /**
     * Gets renderer.
     *
     * @return the renderer
     */
    public MyShapeRenderer getRenderer() {
        return renderer;
    }

    /**
     * Button click sound.
     */
    public void buttonClickSound() {
        //    	if (getButtons()[8].isSelect()) {
        //    		AssetLoader.click.play();
        //    	}
    }

    /**
     * Think.
     *
     * @param delta the delta
     */
    public void think(float delta) {
        checkWindowResize();
        if (!this.mouseMovedArray.isEmpty()) {
            for (GridPoint2 aClickPressedArray : mouseMovedArray) {
                this.model.mouseMoved(aClickPressedArray.x, aClickPressedArray.y);

                if (this.buttons != null) {
                    for (ApoButton button : this.buttons) {
                        if (button.getMove(aClickPressedArray.x, aClickPressedArray.y)) {
                            break;
                        }
                    }
                }
            }
            mouseMovedArray.clear();
        }
        if ((this.buttonFunction != null) && (!this.buttonFunction.isEmpty())) {
            this.model.mouseButtonFunction(this.buttonFunction);
            this.buttonFunction = "";
            clickPressedArray.clear();
            clickDraggedArray.clear();
            clickReleasedArray.clear();
            buttonClickSound();
        }
        if (!this.clickPressedArray.isEmpty()) {
            for (GridPoint2 aClickPressedArray : clickPressedArray) {
                this.model.mousePressed(aClickPressedArray.x, aClickPressedArray.y, bRightClick);
            }
            clickPressedArray.clear();
        }
        if (!this.clickDraggedArray.isEmpty()) {
            for (GridPoint2 aClickDraggedArray : clickDraggedArray) {
                this.model.mouseDragged(aClickDraggedArray.x, aClickDraggedArray.y, bRightClick);
            }
            clickDraggedArray.clear();
        }
        if (!this.clickReleasedArray.isEmpty()) {
            for (GridPoint2 aClickReleasedArray : clickReleasedArray) {
                this.model.mouseButtonReleased(aClickReleasedArray.x, aClickReleasedArray.y, bRightClick);
            }
            clickPressedArray.clear();
            clickDraggedArray.clear();
            clickReleasedArray.clear();
            bRightClick = false;
        }
        if (!this.keyPressedArray.isEmpty()) {
            for (Integer aKeyPressedArray : keyPressedArray) {
                this.model.keyPressed(aKeyPressedArray, (char) (aKeyPressedArray.intValue()));
            }
            keyPressedArray.clear();
        }
        if (!this.keyTypedArray.isEmpty()) {
            for (Character c : keyTypedArray) {
                this.model.keyCharacterTyped(c);
            }
            keyTypedArray.clear();
        }
        if (!this.keyReleasedArray.isEmpty()) {
            for (Integer aKeyReleasedArray : keyReleasedArray) {
                this.model.keyButtonReleased(aKeyReleasedArray, (char) (aKeyReleasedArray.intValue()));
            }
            keyReleasedArray.clear();
        }
    }

    @Override
    public void render(float delta) {
        if (!headless) {
            Gdx.gl.glClearColor(Constants.COLOR_CLEAR[0], Constants.COLOR_CLEAR[1], Constants.COLOR_CLEAR[2], 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        }
    }

    /**
     * Render buttons.
     */
    public void renderButtons() {
        for (ApoButton apoButton : buttons) {
            apoButton.render(this);
        }
    }

    /**
     * Draw string.
     *
     * @param s     the s
     * @param x     the x
     * @param y     the y
     * @param color the color
     */
    public void drawString(String s, int x, int y, float[] color) {
        drawString(s, x, y, color, AssetLoader.font15, true);
    }

    /**
     * Draw string.
     *
     * @param s      the s
     * @param x      the x
     * @param y      the y
     * @param color  the color
     * @param font   the font
     * @param bWidth the b width
     */
    public void drawString(String s, float x, float y, float[] color, BitmapFont font, boolean bWidth) {
        if (bWidth) {
            drawString(s, x, y, color, font, DrawString.MIDDLE, false, true);
        } else {
            drawString(s, x, y, color, font, DrawString.BEGIN, false, true);
        }
    }

    /**
     * Draw string.
     *
     * @param s     the s
     * @param x     the x
     * @param y     the y
     * @param color the color
     * @param font  the font
     * @param width the width
     */
    public void drawString(String s, float x, float y, float[] color, BitmapFont font, DrawString width) {
        drawString(s, x, y, color, font, width, false, true);
    }

    /**
     * Draw string.
     *
     * @param s               the s
     * @param x               the x
     * @param y               the y
     * @param color           the color
     * @param font            the font
     * @param width           the width
     * @param bHeight         the b height
     * @param bNewSpriteBatch the b new sprite batch
     */
    public void drawString(String s, float x, float y, float[] color, BitmapFont font, DrawString width, boolean bHeight, boolean bNewSpriteBatch) {
        if (bNewSpriteBatch) {
            spriteBatch.begin();
        }
        font.setColor(color[0], color[1], color[2], color[3]);

        if (s == null || s.isEmpty()) {
            s = " ";
        }

        glyphLayout.setText(font, s);
        if (bHeight) {
            font.draw(spriteBatch, s, x - glyphLayout.width * width.getDifference(), y - glyphLayout.height * 7 / 10f);
        } else {
            font.draw(spriteBatch, s, x - glyphLayout.width * width.getDifference(), y);
        }
        if (bNewSpriteBatch) {
            spriteBatch.end();
        }
    }

    public void drawTitle(String title, float[] titleColor, boolean bSpriteBatchActive) {
        if (bSpriteBatchActive) {
            spriteBatch.end();
        }

        glyphLayout.setText(AssetLoader.fontTitle, title);
        float bannerWidth = glyphLayout.width + 80;
        float bannerHeight = 60;
        float bannerX = Constants.GAME_WIDTH / 2f - bannerWidth / 2f;
        float bannerY = 10;

        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(0f, 0f, 0f, 0.5f);
        renderer.roundedRect(bannerX, bannerY, bannerWidth, bannerHeight, 12);
        renderer.end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        spriteBatch.begin();
        drawString(title, Constants.GAME_WIDTH / 2f, 20, titleColor, AssetLoader.fontTitle, DrawString.MIDDLE, false, false);
    }

    private void checkWindowResize() {
        if (headless) return;
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        if (w > 0 && h > 0 && (w != lastViewPortResizeWidth || h != lastViewPortResizeHeight)) {
            applyResize(w, h);
        }
    }

    private void applyResize(int width, int height) {
        lastViewPortResizeWidth = width;
        lastViewPortResizeHeight = height;
        viewport.update(width, height);
        renderer.setProjectionMatrix(cam.combined);
        spriteBatch.setProjectionMatrix(cam.combined);
    }

    @Override
    public void resize(int width, int height) {
        if (!headless && width > 0 && height > 0) {
            applyResize(width, height);
        }
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
        AssetLoader.load();
        if (this.model != null) {
            this.model.load();
        }
    }

    @Override
    public void dispose() {
        if (this.model != null) {
            this.model.dispose();
        }
        AssetLoader.dispose();
        spriteBatch.dispose();
        renderer.dispose();
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer > 0) {
            return false;
        }
        Vector3 screenCoords = new Vector3(screenX, screenY, 0);
        viewport.unproject(screenCoords);
        int x = (int) screenCoords.x;
        int y = (int) screenCoords.y;
        boolean bCheck = true;
        if (this.buttons != null) {
            for (ApoButton apoButton : this.buttons) {
                if (apoButton.getReleased(x, y)) {
                    this.buttonFunction = apoButton.getFunction();
                    bCheck = false;
                    break;
                }
            }
        }
        if ((this.model != null) && (bCheck)) {
            this.clickReleasedArray.add(new GridPoint2(x, y));

        }
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer > 0) {
            return false;
        }
        Vector3 screenCoords = new Vector3(screenX, screenY, 0);
        viewport.unproject(screenCoords);
        int x = (int) screenCoords.x;
        int y = (int) screenCoords.y;
        if (this.model != null) {
            this.clickDraggedArray.add(new GridPoint2(x, y));
        }
        return false;
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer > 0) {
            return false;
        }
        Vector3 screenCoords = new Vector3(screenX, screenY, 0);
        viewport.unproject(screenCoords);
        int x = (int) screenCoords.x;
        int y = (int) screenCoords.y;
        if (this.buttons != null) {
            for (ApoButton apoButton : this.buttons) {
                if (apoButton.getPressed(x, y)) {
                    return false;
                }
            }
        }
        if (this.model != null) {
            this.clickPressedArray.add(new GridPoint2(x, y));
            if (button == 1) {
                bRightClick = true;
            }
        }

        return false;
    }

    public boolean keyDown(int keycode) {
        if (this.model != null) {
            keyPressedArray.add(keycode);
            this.model.keyPressed(keycode, (char) (keycode));
        }
        return false;
    }

    public boolean keyUp(int keycode) {
        if (this.model != null) {
            keyReleasedArray.add(keycode);
        }
        return false;
    }

    public boolean keyTyped(char character) {
        if (this.model != null) {
            if ((int) character != 0) {
                keyTypedArray.add(character);
            }
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        Vector3 screenCoords = new Vector3(screenX, screenY, 0);
        viewport.unproject(screenCoords);
        int x = (int) screenCoords.x;
        int y = (int) screenCoords.y;
        if (this.model != null) {
            mouseMovedArray.add(new GridPoint2(x, y));
            //this.model.mouseMoved(x, y);
        }

        /*if (this.buttons != null) {
            for (ApoButton button : this.buttons) {
                if (button.getMove(x, y)) {
                    break;
                }
            }
        }*/

        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (this.model != null) {
            this.model.mouseWheelChanged((int) (amountY));
        }
        return false;
    }

    /**
     * Gets viewport.
     *
     * @return the viewport
     */
    public Viewport getViewport() {
        return viewport;
    }
}
