package com.apogames.aistories.game.settings;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.MainPanel;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.DrawString;
import com.apogames.backend.SequentiallyThinkingScreenModel;
import com.apogames.common.Localization;
import com.apogames.entity.ApoButton;
import com.apogames.entity.Textfield;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class StorySettingsScreen extends SequentiallyThinkingScreenModel {

    public static final String FUNCTION_BACK = "SETTINGS_QUIT";
    public static final String FUNCTION_CONFIRM = "SETTINGS_CONFIRM";

    private static final int TILE_HEIGHT = 45;
    private static final int TILE_GAP = 8;
    private static final int LABEL_X = 20;
    private static final int TILES_X = 20;
    private static final int SECTION_X = 10;
    private static final int SECTION_W = Constants.GAME_WIDTH - 20;
    private static final int SECTION_GAP = 7;

    private static final int SECTION_1_Y = 78;
    private static final int ROW_Y_TYPE = SECTION_1_Y + 28;
    private static final int SECTION_1_H = 28 + TILE_HEIGHT + 8;

    private static final int SECTION_2_Y = SECTION_1_Y + SECTION_1_H + SECTION_GAP;
    private static final int ROW_Y_AGE = SECTION_2_Y + 28;
    private static final int SECTION_2_H = SECTION_1_H;

    private static final int SECTION_3_Y = SECTION_2_Y + SECTION_2_H + SECTION_GAP;
    private static final int ROW_Y_LENGTH = SECTION_3_Y + 28;
    private static final int SECTION_3_H = SECTION_1_H;

    private static final int SECTION_4_Y = SECTION_3_Y + SECTION_3_H + SECTION_GAP;
    private static final int PROMPT_X = 20;
    private static final int PROMPT_Y = SECTION_4_Y + 28;
    private static final int PROMPT_WIDTH = Constants.GAME_WIDTH - 40;
    private static final int PROMPT_HEIGHT = Constants.GAME_HEIGHT - PROMPT_Y - 110;
    private static final int SECTION_4_H = 28 + PROMPT_HEIGHT + 8;

    private static final int TILE_RADIUS = 5;

    private Textfield promptField;
    private int hoverType = -1;
    private int hoverAge = -1;
    private int hoverLength = -1;

    private StorySettings.StoryType selectedType;
    private StorySettings.AgeGroup selectedAge;
    private StorySettings.StoryLength selectedLength;

    private static final GlyphLayout glyphLayout = new GlyphLayout();

    public StorySettingsScreen(MainPanel game) {
        super(game);
    }

    @Override
    public void init() {
        StorySettings settings = getMainPanel().getStorySettings();
        this.selectedType = settings.getStoryType();
        this.selectedAge = settings.getAgeGroup();
        this.selectedLength = settings.getStoryLength();

        if (this.promptField == null) {
            this.promptField = new Textfield(PROMPT_X, PROMPT_Y, PROMPT_WIDTH, PROMPT_HEIGHT, AssetLoader.font25);
            this.promptField.init();
            this.promptField.setMaxLength(3000);
            this.promptField.setMultiLine(true);
            this.promptField.setFixedFont(true);
            this.promptField.setCurString("");
        }

        String template = settings.getPromptTemplate();
        if (template == null || template.isEmpty()) {
            template = getMainPanel().getPromptObject().buildPromptTemplate(settings);
        }
        this.promptField.setCurString(template);
        this.promptField.setSelect(false);

        getMainPanel().resetSize(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        this.setNeededButtonsVisible();
    }

    @Override
    public void setNeededButtonsVisible() {
        getMainPanel().getButtonByFunction(FUNCTION_BACK).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_CONFIRM).setVisible(true);
    }

    @Override
    public void mouseButtonFunction(String function) {
        super.mouseButtonFunction(function);
        switch (function) {
            case FUNCTION_BACK:
                goBack();
                break;
            case FUNCTION_CONFIRM:
                applyAndSave();
                goBack();
                break;
        }
    }

    private void goBack() {
        if (getMainPanel().isSongMode()) {
            getMainPanel().changeToCreateSong();
        } else {
            getMainPanel().changeToCreateStory();
        }
    }

    private void applyAndSave() {
        StorySettings settings = getMainPanel().getStorySettings();
        settings.setStoryType(selectedType);
        settings.setAgeGroup(selectedAge);
        settings.setStoryLength(selectedLength);
        settings.setPromptTemplate(promptField.getCurString());
        settings.save();
        getMainPanel().getPromptObject().updateFromSettings(settings);
    }

    private void regenerateTemplate() {
        StorySettings tempSettings = new StorySettings();
        tempSettings.setStoryType(selectedType);
        tempSettings.setAgeGroup(selectedAge);
        tempSettings.setStoryLength(selectedLength);
        String template = getMainPanel().getPromptObject().buildPromptTemplate(tempSettings);
        promptField.setCurString(template);
    }

    @Override
    public void mousePressed(int x, int y, boolean isRightButton) {
        if (promptField.mousePressed(x, y)) {
            promptField.setSelect(true);
            return;
        }
        promptField.setSelect(false);
        Gdx.input.setOnscreenKeyboardVisible(false);
        MainPanel.clearActiveInput();

        int typeIndex = getTileIndex(x, y, ROW_Y_TYPE, StorySettings.StoryType.values().length, getTypeTileWidth());
        if (typeIndex >= 0) {
            selectedType = StorySettings.StoryType.values()[typeIndex];
            regenerateTemplate();
            return;
        }
        int ageIndex = getTileIndex(x, y, ROW_Y_AGE, StorySettings.AgeGroup.values().length, getAgeTileWidth());
        if (ageIndex >= 0) {
            selectedAge = StorySettings.AgeGroup.values()[ageIndex];
            regenerateTemplate();
            return;
        }
        int lengthIndex = getTileIndex(x, y, ROW_Y_LENGTH, StorySettings.StoryLength.values().length, getLengthTileWidth());
        if (lengthIndex >= 0) {
            selectedLength = StorySettings.StoryLength.values()[lengthIndex];
            regenerateTemplate();
        }
    }

    @Override
    public void mouseButtonReleased(int mouseX, int mouseY, boolean isRightButton) {
        promptField.mouseReleased(mouseX, mouseY);
    }

    @Override
    public void mouseDragged(int x, int y, boolean isRightButton) {
        promptField.mouseDragged(x, y);
    }

    @Override
    public void mouseMoved(int x, int y) {
        promptField.getMove(x, y);
        hoverType = getTileIndex(x, y, ROW_Y_TYPE, StorySettings.StoryType.values().length, getTypeTileWidth());
        hoverAge = getTileIndex(x, y, ROW_Y_AGE, StorySettings.AgeGroup.values().length, getAgeTileWidth());
        hoverLength = getTileIndex(x, y, ROW_Y_LENGTH, StorySettings.StoryLength.values().length, getLengthTileWidth());
    }

    @Override
    public void keyPressed(int keyCode, char character) {
        super.keyPressed(keyCode, character);
        if (promptField.isSelect()) {
            promptField.keyDown(keyCode);
        }
    }

    @Override
    public void keyCharacterTyped(char character) {
        if (promptField.isSelect()) {
            promptField.addTypedCharacter(character);
        }
    }

    @Override
    public void keyButtonReleased(int keyCode, char character) {
        if (promptField.isSelect()) {
            promptField.keyUp(keyCode);
        } else {
            super.keyButtonReleased(keyCode, character);
        }
    }

    private int getTileIndex(int x, int y, int rowY, int count, int tileWidth) {
        if (y < rowY || y > rowY + TILE_HEIGHT) return -1;
        for (int i = 0; i < count; i++) {
            int tileX = TILES_X + i * (tileWidth + TILE_GAP);
            if (x >= tileX && x <= tileX + tileWidth) {
                return i;
            }
        }
        return -1;
    }

    private int getTypeTileWidth() {
        return (Constants.GAME_WIDTH - 2 * TILES_X - (StorySettings.StoryType.values().length - 1) * TILE_GAP)
                / StorySettings.StoryType.values().length;
    }

    private int getAgeTileWidth() {
        return (Constants.GAME_WIDTH - 2 * TILES_X - (StorySettings.AgeGroup.values().length - 1) * TILE_GAP)
                / StorySettings.AgeGroup.values().length;
    }

    private int getLengthTileWidth() {
        return (Constants.GAME_WIDTH - 2 * TILES_X - (StorySettings.StoryLength.values().length - 1) * TILE_GAP)
                / StorySettings.StoryLength.values().length;
    }

    @Override
    protected void quit() {
        goBack();
    }

    @Override
    public void doThink(float delta) {
        promptField.think(10);
    }

    @Override
    public void render() {
        getMainPanel().spriteBatch.begin();
        getMainPanel().spriteBatch.draw(AssetLoader.backgroundTextureRegion, 0, 0);
        getMainPanel().spriteBatch.end();

        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);

        // Section background panels
        getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
        getMainPanel().getRenderer().setColor(0f, 0f, 0f, 0.4f);
        getMainPanel().getRenderer().roundedRect(SECTION_X, SECTION_1_Y, SECTION_W, SECTION_1_H, 10);
        getMainPanel().getRenderer().roundedRect(SECTION_X, SECTION_2_Y, SECTION_W, SECTION_2_H, 10);
        getMainPanel().getRenderer().roundedRect(SECTION_X, SECTION_3_Y, SECTION_W, SECTION_3_H, 10);
        getMainPanel().getRenderer().roundedRect(SECTION_X, SECTION_4_Y, SECTION_W, SECTION_4_H, 10);
        getMainPanel().getRenderer().end();

        renderTileRow(ROW_Y_TYPE, StorySettings.StoryType.values().length, getTypeTileWidth(),
                selectedType.ordinal(), hoverType);
        renderTileRow(ROW_Y_AGE, StorySettings.AgeGroup.values().length, getAgeTileWidth(),
                selectedAge.ordinal(), hoverAge);
        renderTileRow(ROW_Y_LENGTH, StorySettings.StoryLength.values().length, getLengthTileWidth(),
                selectedLength.ordinal(), hoverLength);

        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        getMainPanel().drawTitle(Localization.getInstance().getCommon().get("settings_title"), Constants.COLOR_WHITE, false);

        drawLabel(Localization.getInstance().getCommon().get("settings_story_type"), SECTION_1_Y + 3);
        drawTileLabels(ROW_Y_TYPE, StorySettings.StoryType.values().length, getTypeTileWidth(), getTypeLabels(), selectedType.ordinal());

        drawLabel(Localization.getInstance().getCommon().get("settings_age_group"), SECTION_2_Y + 3);
        drawTileLabels(ROW_Y_AGE, StorySettings.AgeGroup.values().length, getAgeTileWidth(), getAgeLabels(), selectedAge.ordinal());

        drawLabel(Localization.getInstance().getCommon().get("settings_length"), SECTION_3_Y + 3);
        drawTileLabels(ROW_Y_LENGTH, StorySettings.StoryLength.values().length, getLengthTileWidth(), getLengthLabels(), selectedLength.ordinal());

        drawLabel(Localization.getInstance().getCommon().get("settings_prompt"), SECTION_4_Y + 3);

        getMainPanel().spriteBatch.end();

        promptField.render(getMainPanel(), 0, 0);

        for (ApoButton button : getMainPanel().getButtons()) {
            button.render(getMainPanel());
        }
    }

    private void renderTileRow(int rowY, int count, int tileWidth, int selectedIndex, int hoverIndex) {
        for (int i = 0; i < count; i++) {
            int x = TILES_X + i * (tileWidth + TILE_GAP);

            getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
            if (i == selectedIndex) {
                getMainPanel().getRenderer().setColor(Constants.COLOR_YELLOW[0], Constants.COLOR_YELLOW[1], Constants.COLOR_YELLOW[2], 0.9f);
            } else {
                getMainPanel().getRenderer().setColor(0.3f, 0.3f, 0.3f, 0.7f);
            }
            getMainPanel().getRenderer().roundedRect(x, rowY, tileWidth, TILE_HEIGHT, TILE_RADIUS);
            getMainPanel().getRenderer().end();

            getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Line);
            getMainPanel().getRenderer().setColor(Constants.COLOR_WHITE[0], Constants.COLOR_WHITE[1], Constants.COLOR_WHITE[2], 1f);
            getMainPanel().getRenderer().roundedRectLine(x, rowY, tileWidth, TILE_HEIGHT, TILE_RADIUS);
            getMainPanel().getRenderer().end();
        }
    }

    private void drawLabel(String text, int y) {
        getMainPanel().drawString(text, LABEL_X, y, Constants.COLOR_WHITE, AssetLoader.font20, DrawString.BEGIN, false, false);
    }

    private void drawTileLabels(int rowY, int count, int tileWidth, String[] labels, int selectedIndex) {
        BitmapFont font = AssetLoader.font20;
        for (int i = 0; i < count; i++) {
            int x = TILES_X + i * (tileWidth + TILE_GAP) + tileWidth / 2;
            int y = rowY + TILE_HEIGHT / 2 - 8;
            glyphLayout.setText(font, labels[i]);
            if (glyphLayout.width > tileWidth - 10) {
                font = AssetLoader.font15;
            }
            float[] textColor = (i == selectedIndex) ? Constants.COLOR_BLACK : Constants.COLOR_WHITE;
            getMainPanel().drawString(labels[i], x, y, textColor, font, DrawString.MIDDLE, false, false);
            font = AssetLoader.font20;
        }
    }

    private String[] getTypeLabels() {
        StorySettings.StoryType[] values = StorySettings.StoryType.values();
        String[] labels = new String[values.length];
        String[] keys = {"story_type_bedtime", "story_type_friendship", "story_type_detective", "story_type_adventure", "story_type_fairytale"};
        for (int i = 0; i < values.length; i++) {
            labels[i] = Localization.getInstance().getCommon().get(keys[i]);
        }
        return labels;
    }

    private String[] getAgeLabels() {
        StorySettings.AgeGroup[] values = StorySettings.AgeGroup.values();
        String[] labels = new String[values.length];
        String[] keys = {"age_0_1", "age_2_4", "age_5_7", "age_8_12", "age_12_16", "age_16_plus"};
        for (int i = 0; i < values.length; i++) {
            labels[i] = Localization.getInstance().getCommon().get(keys[i]);
        }
        return labels;
    }

    private String[] getLengthLabels() {
        StorySettings.StoryLength[] values = StorySettings.StoryLength.values();
        String[] labels = new String[values.length];
        String[] keys = {"length_short", "length_medium", "length_long"};
        for (int i = 0; i < values.length; i++) {
            labels[i] = Localization.getInstance().getCommon().get(keys[i]);
        }
        return labels;
    }

    @Override
    public void dispose() {
    }
}
