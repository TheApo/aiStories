package com.apogames.aistories.game.customEntity;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.MainPanel;
import com.apogames.aistories.game.objects.CustomEntity;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.DrawString;
import com.apogames.backend.SequentiallyThinkingScreenModel;
import com.apogames.common.Localization;
import com.apogames.entity.Textfield;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class CustomEntityEditor extends SequentiallyThinkingScreenModel {

    public static final String FUNCTION_BACK = "CUSTOMEDITOR_QUIT";
    public static final String FUNCTION_CONFIRM = "CUSTOMEDITOR_CONFIRM";

    private static final int PANEL_Y = 90;
    private static final int PANEL_RADIUS = 10;

    private static final int LEFT_PANEL_X = 10;
    private static final int LEFT_PANEL_W = 420;
    private static final int LABEL_Y = 105;
    private static final int NAME_FIELD_Y = 135;
    private static final int DETAILS_LABEL_Y = 195;
    private static final int DETAILS_FIELD_Y = 225;
    private static final int PREVIEW_SIZE = 140;

    private static final int GRID_COLS = 10;
    private static final int GRID_SPRITE_SIZE = 80;
    private static final int GRID_PADDING = 5;
    private static final int GRID_START_X = 450;
    private static final int GRID_START_Y = 140;

    private final boolean[] keys = new boolean[256];

    private CustomEntity customEntity;

    private Textfield nameField;
    private Textfield detailsField;
    private Textfield activeField;

    private int selectedImageIndex;
    private int totalImages;

    public CustomEntityEditor(final MainPanel game) {
        super(game);
        this.totalImages = 60;
    }

    public void setCustomEntity(CustomEntity customEntity) {
        this.customEntity = customEntity;
        if (customEntity != null) {
            this.totalImages = customEntity.getTotalImages();
        }
    }

    @Override
    public void init() {
        if (getGameProperties() == null) {
            setGameProperties(new CustomEntityEditorPreferences(this));
            loadProperties();
        }

        if (this.nameField == null) {
            this.nameField = new Textfield(30, NAME_FIELD_Y, 380, 35, AssetLoader.font20);
            this.nameField.init();
            this.nameField.setMaxLength(30);
            this.nameField.setFixedFont(true);
        }
        if (this.detailsField == null) {
            this.detailsField = new Textfield(30, DETAILS_FIELD_Y, 380, 120, AssetLoader.font20);
            this.detailsField.init();
            this.detailsField.setMaxLength(300);
            this.detailsField.setFixedFont(true);
            this.detailsField.setMultiLine(true);
        }

        if (this.customEntity != null) {
            this.nameField.setCurString(this.customEntity.getCustomName());
            this.nameField.setPosition(this.customEntity.getCustomName().length());
            this.detailsField.setCurString(this.customEntity.getCustomDetails());
            this.detailsField.setPosition(this.customEntity.getCustomDetails().length());
            this.selectedImageIndex = this.customEntity.getImageIndex();
        }

        this.nameField.setSelect(false);
        this.detailsField.setSelect(false);
        this.activeField = null;

        this.getMainPanel().resetSize(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        this.setNeededButtonsVisible();
    }

    @Override
    public void setNeededButtonsVisible() {
        getMainPanel().getButtonByFunction(FUNCTION_BACK).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_CONFIRM).setVisible(true);
    }

    @Override
    public void keyPressed(int keyCode, char character) {
        super.keyPressed(keyCode, character);
        if (keyCode >= 0 && keyCode < keys.length) {
            if (keys[keyCode]) {
                return; // GameScreen calls keyPressed twice per key event — skip duplicate
            }
            keys[keyCode] = true;
        }
        if (this.activeField != null) {
            this.activeField.handleNavigationKey(keyCode);
        }
    }

    @Override
    public void keyButtonReleased(int keyCode, char character) {
        if (keyCode >= 0 && keyCode < keys.length && keys[keyCode]) {
            keys[keyCode] = false;
            if (this.activeField != null) {
                return;
            }
        }
        if (this.activeField != null) {
            if (character == 8 || character == 127 || character == 10 || character == 13
                    || (!Character.isISOControl(character) && Character.isDefined(character))) {
                this.activeField.addTypedCharacter(character);
                return;
            }
        }
        super.keyButtonReleased(keyCode, character);
    }

    @Override
    public void mouseMoved(int mouseX, int mouseY) {
        this.nameField.getMove(mouseX, mouseY);
        this.detailsField.getMove(mouseX, mouseY);
    }

    @Override
    public void mousePressed(int x, int y, boolean isRightButton) {
        if (this.nameField.mousePressed(x, y)) {
            this.nameField.setSelect(true);
            this.detailsField.setSelect(false);
            this.activeField = this.nameField;
            showKeyboardIfAndroid();
            return;
        }
        if (this.detailsField.mousePressed(x, y)) {
            this.detailsField.setSelect(true);
            this.nameField.setSelect(false);
            this.activeField = this.detailsField;
            showKeyboardIfAndroid();
            return;
        }

        int gridIndex = getGridIndexAt(x, y);
        if (gridIndex >= 0 && gridIndex < this.totalImages) {
            this.selectedImageIndex = gridIndex;
        }

        this.nameField.setSelect(false);
        this.detailsField.setSelect(false);
        this.activeField = null;
        hideKeyboardIfAndroid();
    }

    @Override
    public void mouseButtonReleased(int mouseX, int mouseY, boolean isRightButton) {
        this.nameField.mouseReleased(mouseX, mouseY);
        this.detailsField.mouseReleased(mouseX, mouseY);
    }

    @Override
    public void mouseDragged(int x, int y, boolean isRightButton) {
        if (this.activeField != null) {
            this.activeField.mouseDragged(x, y);
        }
    }

    private void showKeyboardIfAndroid() {
        if (Constants.IS_ANDROID) {
            Gdx.input.setOnscreenKeyboardVisible(true);
        }
    }

    private void hideKeyboardIfAndroid() {
        if (Constants.IS_ANDROID) {
            Gdx.input.setOnscreenKeyboardVisible(false);
        }
    }

    private int getGridIndexAt(int mouseX, int mouseY) {
        int relX = mouseX - GRID_START_X;
        int relY = mouseY - GRID_START_Y;
        if (relX < 0 || relY < 0) return -1;

        int col = relX / (GRID_SPRITE_SIZE + GRID_PADDING);
        int row = relY / (GRID_SPRITE_SIZE + GRID_PADDING);

        if (col >= GRID_COLS) return -1;
        if (col * (GRID_SPRITE_SIZE + GRID_PADDING) + GRID_SPRITE_SIZE < relX) return -1;
        if (row * (GRID_SPRITE_SIZE + GRID_PADDING) + GRID_SPRITE_SIZE < relY) return -1;

        int index = row * GRID_COLS + col;
        if (index >= this.totalImages) return -1;
        return index;
    }

    private TextureRegion getTextureByIndex(int index) {
        if (this.customEntity != null) {
            return this.customEntity.getTextureByIndex(index);
        }
        return AssetLoader.maincharacterTextureRegion[0];
    }

    @Override
    public void mouseButtonFunction(String function) {
        super.mouseButtonFunction(function);
        switch (function) {
            case FUNCTION_BACK:
                quit();
                break;
            case FUNCTION_CONFIRM:
                confirm();
                break;
        }
    }

    private void confirm() {
        if (this.customEntity != null) {
            String name = this.nameField.getCurString().trim();
            if (name.isEmpty()) {
                name = Localization.getInstance().getCommon().get(this.customEntity.getName());
            }
            this.customEntity.setCustomName(name);
            this.customEntity.setCustomDetails(this.detailsField.getCurString().trim());
            this.customEntity.setImageIndex(this.selectedImageIndex);

            getMainPanel().saveCustomEntityPreferences();
        }
        hideKeyboardIfAndroid();
        getMainPanel().changeToCreateStory();
    }

    @Override
    protected void quit() {
        hideKeyboardIfAndroid();
        getMainPanel().changeToCreateStory();
    }

    @Override
    public void doThink(float delta) {
        if (this.nameField != null) {
            this.nameField.think((int) delta);
        }
        if (this.detailsField != null) {
            this.detailsField.think((int) delta);
        }
    }

    @Override
    public void render() {
        getMainPanel().spriteBatch.begin();
        getMainPanel().spriteBatch.draw(AssetLoader.backgroundTextureRegion, 0, 0);
        getMainPanel().spriteBatch.end();

        int gridRows = (this.totalImages + GRID_COLS - 1) / GRID_COLS;

        // Rounded background panels
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
        getMainPanel().getRenderer().setColor(0f, 0f, 0f, 0.3f);
        getMainPanel().getRenderer().roundedRect(LEFT_PANEL_X, PANEL_Y, LEFT_PANEL_W, 580, PANEL_RADIUS);
        int rightPanelX = GRID_START_X - 15;
        int rightPanelW = GRID_COLS * (GRID_SPRITE_SIZE + GRID_PADDING) + 25;
        int rightPanelH = (GRID_START_Y - PANEL_Y) + gridRows * (GRID_SPRITE_SIZE + GRID_PADDING) + 15;
        getMainPanel().getRenderer().roundedRect(rightPanelX, PANEL_Y, rightPanelW, rightPanelH, PANEL_RADIUS);
        getMainPanel().getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        // Selection highlight for selected image
        getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
        int selCol = this.selectedImageIndex % GRID_COLS;
        int selRow = this.selectedImageIndex / GRID_COLS;
        getMainPanel().getRenderer().setColor(Constants.COLOR_YELLOW[0], Constants.COLOR_YELLOW[1], Constants.COLOR_YELLOW[2], 1f);
        getMainPanel().getRenderer().rect(
                GRID_START_X + selCol * (GRID_SPRITE_SIZE + GRID_PADDING) - 3,
                GRID_START_Y + selRow * (GRID_SPRITE_SIZE + GRID_PADDING) - 3,
                GRID_SPRITE_SIZE + 6, GRID_SPRITE_SIZE + 6);
        getMainPanel().getRenderer().end();

        this.nameField.render(getMainPanel(), 0, 0);
        this.detailsField.render(getMainPanel(), 0, 0);

        getMainPanel().drawTitle(getEditorTitle(), Constants.COLOR_WHITE, false);

        // Labels — "Name:" and "Bild auswählen:" at same height
        getMainPanel().drawString(Localization.getInstance().getCommon().get("custom_editor_name"), 30, LABEL_Y, Constants.COLOR_WHITE, AssetLoader.font25, DrawString.BEGIN, false, false);
        getMainPanel().drawString(Localization.getInstance().getCommon().get("custom_editor_details"), 30, DETAILS_LABEL_Y, Constants.COLOR_WHITE, AssetLoader.font25, DrawString.BEGIN, false, false);
        getMainPanel().drawString(Localization.getInstance().getCommon().get("custom_editor_image"), GRID_START_X, LABEL_Y, Constants.COLOR_WHITE, AssetLoader.font25, DrawString.BEGIN, false, false);

        // Preview
        TextureRegion previewImage = getTextureByIndex(this.selectedImageIndex);
        if (previewImage != null) {
            float previewX = LEFT_PANEL_X + LEFT_PANEL_W / 2f - PREVIEW_SIZE / 2f;
            getMainPanel().spriteBatch.draw(previewImage, previewX, 370, PREVIEW_SIZE, PREVIEW_SIZE);
        }

        String previewName = this.nameField.getCurString();
        if (!previewName.isEmpty()) {
            getMainPanel().drawString(previewName, LEFT_PANEL_X + LEFT_PANEL_W / 2f, 525, Constants.COLOR_WHITE, AssetLoader.font25, DrawString.MIDDLE, true, false);
        }

        // Grid images
        for (int i = 0; i < this.totalImages; i++) {
            int col = i % GRID_COLS;
            int row = i / GRID_COLS;
            TextureRegion tex = getTextureByIndex(i);
            if (tex != null) {
                float spriteX = GRID_START_X + col * (GRID_SPRITE_SIZE + GRID_PADDING);
                float spriteY = GRID_START_Y + row * (GRID_SPRITE_SIZE + GRID_PADDING);
                getMainPanel().spriteBatch.draw(tex, spriteX, spriteY, GRID_SPRITE_SIZE, GRID_SPRITE_SIZE);
            }
        }

        getMainPanel().drawString("Version: " + Constants.VERSION, Constants.GAME_WIDTH / 2f, Constants.GAME_HEIGHT - 20f, Constants.COLOR_WHITE, AssetLoader.font15, DrawString.MIDDLE, false, false);

        getMainPanel().spriteBatch.end();

        for (com.apogames.entity.ApoButton button : this.getMainPanel().getButtons()) {
            button.render(this.getMainPanel());
        }
    }

    private String getEditorTitle() {
        if (this.customEntity != null) {
            String key = "custom_editor_title_" + this.customEntity.getEnumName();
            try {
                return Localization.getInstance().getCommon().get(key);
            } catch (Exception e) {
                // fallback to generic title
            }
        }
        return Localization.getInstance().getCommon().get("custom_editor_title");
    }

    @Override
    public void dispose() {
        hideKeyboardIfAndroid();
    }
}
