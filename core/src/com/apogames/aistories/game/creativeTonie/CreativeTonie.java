package com.apogames.aistories.game.creativeTonie;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.MainPanel;
import com.apogames.aistories.game.main.MainInterface;
import com.apogames.aistories.game.main.Prompt;
import com.apogames.aistories.game.main.Running;
import com.apogames.aistories.game.main.ToniesAPI;
import com.apogames.aistories.game.main.tonie.Chapter;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.DrawString;
import com.apogames.backend.SequentiallyThinkingScreenModel;
import com.apogames.common.Localization;
import com.apogames.entity.ApoButton;
import com.apogames.entity.ApoButtonTab;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;

public class CreativeTonie extends SequentiallyThinkingScreenModel implements MainInterface {

    public static final String FUNCTION_BACK = "CREATIVETONIE_QUIT";
    public static final String FUNCTION_UPLOAD = "CREATIVETONIE_UPLOAD";
    public static final String FUNCTION_DELETE = "CREATIVETONIE_DELETE_";
    public static final String FUNCTION_CREATIVE = "CREATIVETONIE_CREATIVE_";

    private final int width = 700;
    private final int height = 550;

    private final boolean[] keys = new boolean[256];

    private boolean isPressed = false;

    private ToniesAPI toniesAPI = null;

    private FileHandle fileHandle;

    private int currentTonieID = 1;

    private final ArrayList<ArrayList<ChapterTile>> chapterTiles = new ArrayList<>();

    public CreativeTonie(final MainPanel game) {
        super(game);
    }

    public void setNeededButtonsVisible() {
        getMainPanel().getButtonByFunction(FUNCTION_BACK).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_UPLOAD).setVisible(true);

        for (ApoButton button : getMainPanel().getButtons()) {
            if (button.getFunction().startsWith(FUNCTION_CREATIVE)) {
                button.setVisible(true);
            }
        }

        if (this.chapterTiles.size() > this.currentTonieID) {
            for (ChapterTile tile : this.chapterTiles.get(currentTonieID)) {
                tile.getButton().setVisible(true);
            }
        }
    }

    @Override
    public void init() {
        if (getGameProperties() == null) {
            setGameProperties(new CreativeToniePreferences(this));
            loadProperties();
        }

        if (this.toniesAPI == null) {
            this.toniesAPI = new ToniesAPI();
        }

        this.receiveEverything();

        this.deleteAndCreateCreativeToniesButtons();

        this.getMainPanel().resetSize(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);

        this.setNeededButtonsVisible();
    }

    private void receiveEverything() {
        this.toniesAPI.connect();
        this.toniesAPI.receiveEverything();

        for (int i = getMainPanel().getButtons().size() - 1; i >= 0; i--) {
            ApoButton button = getMainPanel().getButtons().get(i);
            if (button.getFunction().startsWith(FUNCTION_DELETE)) {
                getMainPanel().getButtons().remove(button);
            }
        }
        this.chapterTiles.clear();
        for (int i = 0; i < this.toniesAPI.getCreativeTonies().size(); i++) {
            int x = Constants.GAME_WIDTH/2 - width/2 + 5;
            int width = this.width - 10;
            int height = 30;
            int y = 180;
            this.chapterTiles.add(new ArrayList<>());
            for (Chapter chapter : this.toniesAPI.getTitles().get(i)) {
                this.chapterTiles.get(i).add(new ChapterTile(getMainPanel(), x, y, width, height, chapter));
                y += 30;
            }
        }
    }

    private void deleteAndCreateCreativeToniesButtons() {
        for (int i = 0; i < this.toniesAPI.getCreativeTonies().size(); i++) {
            String function = FUNCTION_CREATIVE + i;
            ApoButton buttonByFunction = this.getMainPanel().getButtonByFunction(function);
            if (buttonByFunction != null) {
                this.getMainPanel().getButtons().remove(buttonByFunction);
            }

            int buttonWidth = width / this.toniesAPI.getCreativeTonies().size();
            int buttonHeight = 50;
            int buttonX = Constants.GAME_WIDTH/2 - width/2 + i * buttonWidth;
            int buttonY = 80;
            ApoButtonTab button = new ApoButtonTab(buttonX, buttonY, buttonWidth, buttonHeight, function, this.toniesAPI.getCreativeTonies().get(i).getName(), Constants.COLOR_WHITE, Constants.COLOR_BLACK);
            button.setStroke(1);
            button.setFont(AssetLoader.font40);
            getMainPanel().getButtons().add(button);

            if (this.currentTonieID == i) {
                button.setSelect(true);
            }
        }
    }

    @Override
    public void setFileHandle(FileHandle file) {
        this.fileHandle = file;
    }

    @Override
    public void setRunning(Running running) {

    }

    @Override
    public void setTextForTextArea(String text) {

    }

    public void uploadToTonieBox(FileHandle fileHandle) {
        this.toniesAPI.disconnect();

        if (fileHandle == null) {
            return;
        }
        toniesAPI.uploadOnTonie(fileHandle.file(), this.currentTonieID);

        this.receiveEverything();

        resetAllButtonVisibility();

        Gdx.graphics.requestRendering();
    }

    @Override
    public void onImageReceived(String sectionIdentifier, String imageUrl) {

    }

    private void delete(String title, String function) {
        this.toniesAPI.deleteByTitle(this.currentTonieID, title);
        getMainPanel().getButtons().remove(getMainPanel().getButtonByFunction(function));

        this.toniesAPI.disconnect();
        receiveEverything();

        Gdx.graphics.requestRendering();
    }

    @Override
    public Prompt getPrompt() {
        return null;
    }

    @Override
    public void keyPressed(int keyCode, char character) {
        super.keyPressed(keyCode, character);

        keys[keyCode] = true;
    }

    @Override
    public void keyButtonReleased(int keyCode, char character) {
        super.keyButtonReleased(keyCode, character);

        keys[keyCode] = false;
    }

    public void mouseMoved(int mouseX, int mouseY) {
    }

    public void mouseButtonReleased(int mouseX, int mouseY, boolean isRightButton) {
        this.isPressed = false;
    }

    public void mousePressed(int x, int y, boolean isRightButton) {
        if (isRightButton && !this.isPressed) {
            this.isPressed = true;
        }
    }

    public void mouseDragged(int x, int y, boolean isRightButton) {
        if (isRightButton) {
            if (!this.isPressed) {
                this.mousePressed(x, y, isRightButton);
            }
        }
    }

    @Override
    public void mouseButtonFunction(String function) {
        super.mouseButtonFunction(function);

        if (function.startsWith(FUNCTION_DELETE)) {
            for (ChapterTile tile : this.chapterTiles.get(this.currentTonieID)) {
                if (tile.getButton().getFunction().equals(function)) {
                    delete(tile.getChapter().getTitle(), function);
                    break;
                }
            }
            resetAllButtonVisibility();
            return;
        }
        if (function.startsWith(FUNCTION_CREATIVE)) {
            for (ApoButton button : getMainPanel().getButtons()) {
                if (button.getFunction().startsWith(FUNCTION_CREATIVE)) {
                    button.setSelect(false);
                }
            }
            ApoButton buttonByFunction = getMainPanel().getButtonByFunction(function);
            buttonByFunction.setSelect(true);
            this.currentTonieID = Integer.parseInt(function.substring(FUNCTION_CREATIVE.length()));
            resetAllButtonVisibility();
        }

        switch (function) {
            case CreativeTonie.FUNCTION_BACK:
                quit();
                break;
            case CreativeTonie.FUNCTION_UPLOAD:
                uploadToTonieBox(this.fileHandle);
                break;
        }
    }

    private void resetAllButtonVisibility() {
        for (ApoButton button : getMainPanel().getButtons()) {
            button.setVisible(false);
        }
        setNeededButtonsVisible();
    }

    @Override
    protected void quit() {
        this.toniesAPI.disconnect();
        getMainPanel().changeToListenStories();
    }

    @Override
    public void doThink(float delta) {

    }

    @Override
    public void render() {
        getMainPanel().spriteBatch.begin();

        getMainPanel().spriteBatch.draw(AssetLoader.backgroundTextureRegion, 0, 0);

        getMainPanel().spriteBatch.end();

        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
        getMainPanel().getRenderer().setColor(Constants.COLOR_WHITE[0], Constants.COLOR_WHITE[1], Constants.COLOR_WHITE[2], 0.6f);
        getMainPanel().getRenderer().roundedRect(Constants.GAME_WIDTH/2f - width/2f, 80, width, height, 10);
        getMainPanel().getRenderer().roundedRect(Constants.GAME_WIDTH/2f - width/2f, Constants.GAME_HEIGHT - 140, width, 130, 10);
        getMainPanel().getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Line);

        getMainPanel().getRenderer().end();

        getMainPanel().drawTitle(Localization.getInstance().getCommon().get("tonie"), Constants.COLOR_WHITE, false);

        float time = 0;

        getMainPanel().drawString("Titel", Constants.GAME_WIDTH / 2f, 150, Constants.COLOR_BLACK, AssetLoader.font20, DrawString.MIDDLE, false, false);
        int index = 0;
        for (Chapter chapter : this.toniesAPI.getTitles().get(this.currentTonieID)) {
            getMainPanel().drawString((index+1) + ": " + chapter.getTitle(), Constants.GAME_WIDTH / 2f - width/2f + 30, 180 + index * 30, Constants.COLOR_BLACK, AssetLoader.font15, DrawString.BEGIN, false, false);
            getMainPanel().drawString( getTimeForSeconds((int)(chapter.getSeconds()) + 1), Constants.GAME_WIDTH / 2f + width/2f - 30, 180 + index * 30, Constants.COLOR_BLACK, AssetLoader.font15, DrawString.END, false, false);
            time += chapter.getSeconds();
            index += 1;
        }

        getMainPanel().drawString( getTimeForSeconds((int)(time)) + " / 90:00", Constants.GAME_WIDTH / 2f + width/2f - 30, 150, Constants.COLOR_BLACK, AssetLoader.font20, DrawString.END, false, false);

        getMainPanel().spriteBatch.draw(AssetLoader.tonieTextureRegion, Constants.GAME_WIDTH/7f - AssetLoader.tonieTextureRegion.getRegionWidth()/2f, Constants.GAME_HEIGHT/2f - AssetLoader.tonieTextureRegion.getRegionHeight()/2f);
        getMainPanel().spriteBatch.draw(AssetLoader.tonieTextureRegion, Constants.GAME_WIDTH*6/7f - AssetLoader.tonieTextureRegion.getRegionWidth()/2f, Constants.GAME_HEIGHT/2f - AssetLoader.tonieTextureRegion.getRegionHeight()/2f);

        getMainPanel().drawString("Version: " + Constants.VERSION, Constants.GAME_WIDTH / 2f, Constants.GAME_HEIGHT - 20f, Constants.COLOR_WHITE, AssetLoader.font15, DrawString.MIDDLE, false, false);

        String name = this.fileHandle.file().getName();
        getMainPanel().drawString("Titel: " + name, Constants.GAME_WIDTH / 2f, Constants.GAME_HEIGHT - 120f, Constants.COLOR_BLACK, AssetLoader.font20, DrawString.MIDDLE, false, false);

        getMainPanel().spriteBatch.end();

        for (ApoButton button : this.getMainPanel().getButtons()) {
            button.render(this.getMainPanel());
        }
    }

    private String getTimeForSeconds(int seconds) {
        String result = "";
        int minutes = seconds / 60;
        int second = seconds % 60;
        if (minutes < 10) {
            result = "0" + minutes;
        } else {
            result = minutes + "";
        }
        result += ":";
        if (second < 10) {
            result += "0" + second;
        } else {
            result += second;
        }
        return result;
    }

    @Override
    public void dispose() {
    }
}
