package com.apogames.aistories.game.settings;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.MainPanel;
import com.apogames.aistories.game.main.SongPrompt;
import com.apogames.aistories.game.objects.GameObjectives;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.DrawString;
import com.apogames.backend.SequentiallyThinkingScreenModel;
import com.apogames.common.Localization;
import com.apogames.entity.ApoButton;
import com.apogames.entity.ApoButtonCheckIcon;
import com.apogames.entity.Textfield;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SongSettingsScreen extends SequentiallyThinkingScreenModel {

    public static final String FUNCTION_BACK = "SONGSETTINGS_QUIT";
    public static final String FUNCTION_CONFIRM = "SONGSETTINGS_CONFIRM";

    private static final int TILE_HEIGHT = 45;
    private static final int TILE_GAP = 8;
    private static final int LABEL_X = 20;
    private static final int TILES_X = 20;
    private static final int SECTION_X = 10;
    private static final int SECTION_W = Constants.GAME_WIDTH - 20;
    private static final int SECTION_GAP = 7;

    private static final int SECTION_1_Y = 78;
    private static final int ROW_Y_STYLE = SECTION_1_Y + 28;
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
    private static final int PROMPT_HEIGHT = 160;
    private static final int SECTION_4_H = 28 + PROMPT_HEIGHT + 8;

    private static final int SECTION_5_Y = SECTION_4_Y + SECTION_4_H + SECTION_GAP;
    private static final int SECTION_5_H = Constants.GAME_HEIGHT - SECTION_5_Y - 100;

    private static final int TOGGLE_SIZE = 48;
    private static final int TOGGLE_X = TILES_X;
    private static final int OBJ_FIELD_X = TOGGLE_X + TOGGLE_SIZE + TILE_GAP;
    private static final int OBJ_FIELD_Y = SECTION_5_Y + 28;
    private static final int OBJ_FIELD_W = SECTION_X + SECTION_W - OBJ_FIELD_X - 10;
    private static final int OBJ_FIELD_H = SECTION_5_H - 33;
    private static final int TOGGLE_Y = OBJ_FIELD_Y + (OBJ_FIELD_H - TOGGLE_SIZE) / 2;

    private static final int TILE_RADIUS = 5;

    private final boolean[] keys = new boolean[256];
    private Textfield promptField;
    private Textfield objectivesField;
    private ApoButtonCheckIcon toggleButton;
    private int hoverStyle = -1;
    private int hoverAge = -1;
    private int hoverLength = -1;

    private SongSettings.MusicStyle selectedStyle;
    private StorySettings.AgeGroup selectedAge;
    private SongSettings.SongLength selectedLength;
    private int totalPromptLength = 0;

    private static final GlyphLayout glyphLayout = new GlyphLayout();

    public SongSettingsScreen(MainPanel game) {
        super(game);
    }

    @Override
    public void init() {
        SongSettings settings = getMainPanel().getSongSettings();
        this.selectedStyle = settings.getMusicStyle();
        this.selectedAge = settings.getAgeGroup();
        this.selectedLength = settings.getSongLength();

        if (this.toggleButton == null) {
            this.toggleButton = new ApoButtonCheckIcon(TOGGLE_X, TOGGLE_Y, TOGGLE_SIZE,
                    "TOGGLE_OBJECTIVES", Constants.COLOR_SIDE_CHARACTER, Constants.COLOR_BLACK);
        }
        this.toggleButton.setChecked(settings.isIncludeObjectives());

        if (this.promptField == null) {
            this.promptField = new Textfield(PROMPT_X, PROMPT_Y, PROMPT_WIDTH, PROMPT_HEIGHT, AssetLoader.font25);
            this.promptField.init();
            this.promptField.setMaxLength(500);
            this.promptField.setMultiLine(true);
            this.promptField.setFixedFont(true);
            this.promptField.setCurString("");
        }

        if (this.objectivesField == null) {
            this.objectivesField = new Textfield(OBJ_FIELD_X, OBJ_FIELD_Y, OBJ_FIELD_W, OBJ_FIELD_H, AssetLoader.font20);
            this.objectivesField.init();
            this.objectivesField.setMaxLength(500);
            this.objectivesField.setMultiLine(true);
            this.objectivesField.setFixedFont(true);
        }

        String header = settings.getPromptTemplate();
        if (header == null || header.isEmpty()) {
            header = SongPrompt.buildCompactTemplate(settings);
        }
        this.promptField.setCurString(header);
        this.promptField.setSelect(false);

        updateObjectivesPreview();

        getMainPanel().resetSize(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        this.setNeededButtonsVisible();
    }

    private void updateObjectivesPreview() {
        String header = promptField.getCurString();
        GameObjectives objectives = getMainPanel().getPromptObject().getGameObjectives();

        if (toggleButton.isChecked()) {
            SongSettings tempSettings = new SongSettings();
            tempSettings.setMusicStyle(selectedStyle);
            tempSettings.setAgeGroup(selectedAge);
            tempSettings.setSongLength(selectedLength);
            tempSettings.setPromptTemplate(header);
            tempSettings.setIncludeObjectives(true);

            String fullPrompt = SongPrompt.buildSunoPrompt(tempSettings, objectives);
            this.totalPromptLength = fullPrompt.length();

            if (fullPrompt.length() > header.length()) {
                objectivesField.setCurString(fullPrompt.substring(header.length()).trim());
            } else {
                objectivesField.setCurString("");
            }
        } else {
            this.totalPromptLength = header.length();
            objectivesField.setCurString(Localization.getInstance().getCommon().get("song_objectives_off"));
        }
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
                getMainPanel().changeToCreateSong();
                break;
            case FUNCTION_CONFIRM:
                applyAndSave();
                getMainPanel().changeToCreateSong();
                break;
        }
    }

    private void applyAndSave() {
        SongSettings settings = getMainPanel().getSongSettings();
        settings.setMusicStyle(selectedStyle);
        settings.setAgeGroup(selectedAge);
        settings.setSongLength(selectedLength);
        settings.setPromptTemplate(promptField.getCurString());
        settings.setIncludeObjectives(toggleButton.isChecked());
        settings.save();
    }

    private void regenerateTemplate() {
        SongSettings tempSettings = new SongSettings();
        tempSettings.setMusicStyle(selectedStyle);
        tempSettings.setAgeGroup(selectedAge);
        tempSettings.setSongLength(selectedLength);
        String header = SongPrompt.buildCompactTemplate(tempSettings);
        promptField.setCurString(header);
        updateObjectivesPreview();
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

        // Toggle objectives button
        if (toggleButton.intersects(x, y)) {
            toggleButton.toggle();
            updateObjectivesPreview();
            return;
        }

        int styleIndex = getTileIndex(x, y, ROW_Y_STYLE, SongSettings.MusicStyle.values().length, getStyleTileWidth());
        if (styleIndex >= 0) {
            selectedStyle = SongSettings.MusicStyle.values()[styleIndex];
            regenerateTemplate();
            return;
        }
        int ageIndex = getTileIndex(x, y, ROW_Y_AGE, StorySettings.AgeGroup.values().length, getAgeTileWidth());
        if (ageIndex >= 0) {
            selectedAge = StorySettings.AgeGroup.values()[ageIndex];
            regenerateTemplate();
            return;
        }
        int lengthIndex = getTileIndex(x, y, ROW_Y_LENGTH, SongSettings.SongLength.values().length, getLengthTileWidth());
        if (lengthIndex >= 0) {
            selectedLength = SongSettings.SongLength.values()[lengthIndex];
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
        toggleButton.setBOver(toggleButton.intersects(x, y));
        hoverStyle = getTileIndex(x, y, ROW_Y_STYLE, SongSettings.MusicStyle.values().length, getStyleTileWidth());
        hoverAge = getTileIndex(x, y, ROW_Y_AGE, StorySettings.AgeGroup.values().length, getAgeTileWidth());
        hoverLength = getTileIndex(x, y, ROW_Y_LENGTH, SongSettings.SongLength.values().length, getLengthTileWidth());
    }

    @Override
    public void keyPressed(int keyCode, char character) {
        super.keyPressed(keyCode, character);
        keys[keyCode] = true;
        if (promptField.isSelect()) {
            promptField.keyDown(keyCode);
        }
    }

    @Override
    public void keyButtonReleased(int keyCode, char character) {
        if (keyCode >= 0 && keyCode < keys.length && !keys[keyCode]) {
            if (promptField.isSelect()) {
                promptField.addTypedCharacter(character);
                updateObjectivesPreview();
            }
            return;
        }
        super.keyButtonReleased(keyCode, character);
        if (keyCode >= 0 && keyCode < keys.length) {
            keys[keyCode] = false;
        }
        if (promptField.isSelect()) {
            promptField.keyUp(keyCode);
            updateObjectivesPreview();
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

    private int getStyleTileWidth() {
        return (Constants.GAME_WIDTH - 2 * TILES_X - (SongSettings.MusicStyle.values().length - 1) * TILE_GAP)
                / SongSettings.MusicStyle.values().length;
    }

    private int getAgeTileWidth() {
        return (Constants.GAME_WIDTH - 2 * TILES_X - (StorySettings.AgeGroup.values().length - 1) * TILE_GAP)
                / StorySettings.AgeGroup.values().length;
    }

    private int getLengthTileWidth() {
        return (Constants.GAME_WIDTH - 2 * TILES_X - (SongSettings.SongLength.values().length - 1) * TILE_GAP)
                / SongSettings.SongLength.values().length;
    }

    @Override
    protected void quit() {
        getMainPanel().changeToCreateSong();
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

        getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
        getMainPanel().getRenderer().setColor(0f, 0f, 0f, 0.4f);
        getMainPanel().getRenderer().roundedRect(SECTION_X, SECTION_1_Y, SECTION_W, SECTION_1_H, 10);
        getMainPanel().getRenderer().roundedRect(SECTION_X, SECTION_2_Y, SECTION_W, SECTION_2_H, 10);
        getMainPanel().getRenderer().roundedRect(SECTION_X, SECTION_3_Y, SECTION_W, SECTION_3_H, 10);
        getMainPanel().getRenderer().roundedRect(SECTION_X, SECTION_4_Y, SECTION_W, SECTION_4_H, 10);
        getMainPanel().getRenderer().roundedRect(SECTION_X, SECTION_5_Y, SECTION_W, SECTION_5_H, 10);
        getMainPanel().getRenderer().end();

        renderTileRow(ROW_Y_STYLE, SongSettings.MusicStyle.values().length, getStyleTileWidth(),
                selectedStyle.ordinal(), hoverStyle);
        renderTileRow(ROW_Y_AGE, StorySettings.AgeGroup.values().length, getAgeTileWidth(),
                selectedAge.ordinal(), hoverAge);
        renderTileRow(ROW_Y_LENGTH, SongSettings.SongLength.values().length, getLengthTileWidth(),
                selectedLength.ordinal(), hoverLength);

        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        getMainPanel().drawTitle(Localization.getInstance().getCommon().get("song_settings_title"), Constants.COLOR_WHITE, false);

        drawLabel(Localization.getInstance().getCommon().get("song_music_style"), SECTION_1_Y + 3);
        drawTileLabels(ROW_Y_STYLE, SongSettings.MusicStyle.values().length, getStyleTileWidth(), getStyleLabels(), selectedStyle.ordinal());

        drawLabel(Localization.getInstance().getCommon().get("settings_age_group"), SECTION_2_Y + 3);
        drawTileLabels(ROW_Y_AGE, StorySettings.AgeGroup.values().length, getAgeTileWidth(), getAgeLabels(), selectedAge.ordinal());

        drawLabel(Localization.getInstance().getCommon().get("song_length"), SECTION_3_Y + 3);
        drawTileLabels(ROW_Y_LENGTH, SongSettings.SongLength.values().length, getLengthTileWidth(), getLengthLabels(), selectedLength.ordinal());

        drawLabel(Localization.getInstance().getCommon().get("settings_prompt"), SECTION_4_Y + 3);

        // Section 5: Story-Elemente label + counter
        drawLabel(Localization.getInstance().getCommon().get("song_objectives_title"), SECTION_5_Y + 3);
        float[] counterColor = totalPromptLength > 500 ? Constants.COLOR_RED : Constants.COLOR_WHITE;
        String counter = totalPromptLength + " / 500";
        getMainPanel().drawString(counter, SECTION_X + SECTION_W - LABEL_X, SECTION_5_Y + 3,
                counterColor, AssetLoader.font20, DrawString.END, false, false);

        getMainPanel().spriteBatch.end();

        promptField.render(getMainPanel(), 0, 0);
        toggleButton.render(getMainPanel(), 0, 0);
        objectivesField.render(getMainPanel(), 0, 0);

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

    private String[] getStyleLabels() {
        SongSettings.MusicStyle[] values = SongSettings.MusicStyle.values();
        String[] labels = new String[values.length];
        String[] keys = {"music_lullaby", "music_pop", "music_rock", "music_eighties",
                "music_hiphop", "music_piano", "music_electronic", "music_musical"};
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
        SongSettings.SongLength[] values = SongSettings.SongLength.values();
        String[] labels = new String[values.length];
        String[] keys = {"song_length_short", "song_length_medium", "song_length_long"};
        for (int i = 0; i < values.length; i++) {
            labels[i] = Localization.getInstance().getCommon().get(keys[i]);
        }
        return labels;
    }

    @Override
    public void dispose() {
    }
}
