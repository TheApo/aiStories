package com.apogames.entity;

import com.apogames.aistories.Constants;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.DrawString;
import com.apogames.backend.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class ApoButtonDropdown extends ApoButtonImageThree {

    private final DropdownOption[] options;
    private int selectedIndex = 0;
    private boolean popupOpen = false;
    private int hoveredIndex = -1;

    private static final int ITEM_HEIGHT = 56;
    private static final int ITEM_GAP = 4;
    private static final int POPUP_PADDING = 6;
    private static final int POPUP_WIDTH = 160;

    public ApoButtonDropdown(int x, int y, int width, int height, String function, String text,
                             int assetX, int assetY, int picWidth, int picHeight,
                             float[] color, String id, DropdownOption[] options) {
        super(x, y, width, height, function, text, assetX, assetY, picWidth, picHeight, color, id);
        this.options = options;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < options.length) {
            this.selectedIndex = index;
            setId(options[index].getSelectedLabel());
        }
    }

    public DropdownOption getSelectedOption() {
        return options[selectedIndex];
    }

    @Override
    public boolean getPressed(int x, int y) {
        if (popupOpen && isVisible()) {
            setBPressed(true);
            return true;
        }
        return super.getPressed(x, y);
    }

    @Override
    public boolean getReleased(int x, int y) {
        if (!isBPressed() || !isVisible()) return false;

        if (popupOpen) {
            int idx = getPopupItemAt(x, y);
            boolean itemSelected = idx >= 0;
            if (itemSelected) {
                selectedIndex = idx;
                setId(options[idx].getSelectedLabel());
            }
            popupOpen = false;
            hoveredIndex = -1;
            setBPressed(false);
            setBOver(false);
            Gdx.graphics.requestRendering();
            return itemSelected;
        }

        if (intersects(x, y)) {
            popupOpen = true;
            setBPressed(false);
            setBOver(false);
            Gdx.graphics.requestRendering();
            return false;
        }

        setBPressed(false);
        return false;
    }

    @Override
    public boolean getMove(int x, int y) {
        if (popupOpen && isVisible()) {
            int newHovered = getPopupItemAt(x, y);
            if (newHovered != hoveredIndex) {
                hoveredIndex = newHovered;
                Gdx.graphics.requestRendering();
            }
            return true;
        }
        hoveredIndex = -1;
        return super.getMove(x, y);
    }

    @Override
    public void render(GameScreen screen, int changeX, int changeY, boolean bShowTextOnly) {
        super.render(screen, changeX, changeY, bShowTextOnly);

        if (popupOpen && isVisible()) {
            renderPopup(screen, changeX, changeY);
        }
    }

    private void renderPopup(GameScreen screen, int changeX, int changeY) {
        int itemStep = ITEM_HEIGHT + ITEM_GAP;
        int totalHeight = options.length * itemStep - ITEM_GAP + 2 * POPUP_PADDING;
        int popupX = (int) getX() + changeX;
        int popupY = (int) getY() + changeY - totalHeight;

        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        screen.getRenderer().begin(ShapeRenderer.ShapeType.Filled);

        // Panel background (matching switch/settings style)
        screen.getRenderer().setColor(0f, 0f, 0f, 0.55f);
        screen.getRenderer().roundedRect(popupX, popupY, POPUP_WIDTH, totalHeight, 10);

        // Item tiles
        for (int i = 0; i < options.length; i++) {
            int itemY = popupY + POPUP_PADDING + i * itemStep;

            if (i == selectedIndex) {
                // Selected: yellow fill (matching settings tiles)
                screen.getRenderer().setColor(Constants.COLOR_YELLOW[0], Constants.COLOR_YELLOW[1], Constants.COLOR_YELLOW[2], 0.9f);
                screen.getRenderer().roundedRect(popupX + 4, itemY, POPUP_WIDTH - 8, ITEM_HEIGHT, 5);
            } else if (i == hoveredIndex) {
                // Hovered: lighter fill
                screen.getRenderer().setColor(Constants.COLOR_YELLOW[0], Constants.COLOR_YELLOW[1], Constants.COLOR_YELLOW[2], 0.4f);
                screen.getRenderer().roundedRect(popupX + 4, itemY, POPUP_WIDTH - 8, ITEM_HEIGHT, 5);
            } else {
                // Default: dark gray tile (matching settings tiles)
                screen.getRenderer().setColor(0.3f, 0.3f, 0.3f, 0.7f);
                screen.getRenderer().roundedRect(popupX + 4, itemY, POPUP_WIDTH - 8, ITEM_HEIGHT, 5);
            }
        }

        screen.getRenderer().end();

        // Item borders
        screen.getRenderer().begin(ShapeRenderer.ShapeType.Line);
        screen.getRenderer().setColor(Constants.COLOR_WHITE[0], Constants.COLOR_WHITE[1], Constants.COLOR_WHITE[2], 0.6f);
        for (int i = 0; i < options.length; i++) {
            int itemY = popupY + POPUP_PADDING + i * itemStep;
            screen.getRenderer().roundedRectLine(popupX + 4, itemY, POPUP_WIDTH - 8, ITEM_HEIGHT, 5);
        }
        screen.getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        // Text labels
        screen.spriteBatch.begin();
        for (int i = 0; i < options.length; i++) {
            int itemY = popupY + POPUP_PADDING + i * itemStep;
            float[] color;
            if (i == selectedIndex || i == hoveredIndex) {
                color = Constants.COLOR_BLACK;
            } else {
                color = Constants.COLOR_WHITE;
            }
            screen.drawString(options[i].getDropdownLabel(), popupX + POPUP_WIDTH / 2f, itemY + ITEM_HEIGHT / 2 - 12,
                    color, AssetLoader.font25, DrawString.MIDDLE, false, false);
        }
        screen.spriteBatch.end();
    }

    private int getPopupItemAt(int x, int y) {
        int itemStep = ITEM_HEIGHT + ITEM_GAP;
        int totalHeight = options.length * itemStep - ITEM_GAP + 2 * POPUP_PADDING;
        int popupX = (int) getX();
        int popupY = (int) getY() - totalHeight;

        if (x < popupX || x > popupX + POPUP_WIDTH) return -1;
        int relY = y - popupY - POPUP_PADDING;
        if (relY < 0) return -1;
        int index = relY / itemStep;
        int inItem = relY % itemStep;
        if (inItem > ITEM_HEIGHT) return -1;
        if (index < 0 || index >= options.length) return -1;
        return index;
    }
}
