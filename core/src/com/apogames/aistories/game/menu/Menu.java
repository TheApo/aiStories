package com.apogames.aistories.game.menu;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.MainPanel;
import com.apogames.aistories.game.main.ChatGPTIO;
import com.apogames.aistories.game.main.SunoApiIO;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.DrawString;
import com.apogames.backend.SequentiallyThinkingScreenModel;
import com.apogames.common.Localization;
import com.apogames.entity.ApoButton;
import com.apogames.entity.ApoButtonDropdown;

import java.util.Locale;

public class Menu extends SequentiallyThinkingScreenModel {

    public static final String FUNCTION_BACK = "MENU_QUIT";

    public static final String FUNCTION_LANGUAGE = "MENU_LANGUAGE";

    public static final String FUNCTION_CREATESTORY = "MENU_CREATESTORY";

    public static final String FUNCTION_ALLMP3S = "MENU_ALLMP3S";

    public static final String FUNCTION_CREATESONG = "MENU_CREATESONG";

    private final boolean[] keys = new boolean[256];

    private boolean isPressed = false;

    private LanguageEnum language = LanguageEnum.DE;

    public Menu(final MainPanel game) {
        super(game);
    }

    public void setNeededButtonsVisible() {
        getMainPanel().getButtonByFunction(FUNCTION_BACK).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_LANGUAGE).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_CREATESTORY).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_ALLMP3S).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_CREATESONG).setVisible(true);

        if (!getMainPanel().isStoryAvailable()) {
            getMainPanel().getButtonByFunction(FUNCTION_ALLMP3S).setVisible(false);
        }

        boolean hasSuno = SunoApiIO.API_KEY != null && !SunoApiIO.API_KEY.isEmpty();
        if (!hasSuno) {
            getMainPanel().getButtonByFunction(FUNCTION_CREATESONG).setVisible(false);
        }
    }

    @Override
    public void init() {
        if (getGameProperties() == null) {
            setGameProperties(new MenuPreferences(this));
            loadProperties();
        }

        this.getMainPanel().resetSize(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);

        this.setNeededButtonsVisible();
        this.setButtonsVisibility();
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
        switch (function) {
            case Menu.FUNCTION_BACK:
                quit();
                break;
            case Menu.FUNCTION_CREATESTORY:
                getMainPanel().changeToCreateStory();
                break;
            case Menu.FUNCTION_ALLMP3S:
                getMainPanel().changeToListenStories();
                break;
            case Menu.FUNCTION_CREATESONG:
                getMainPanel().changeToCreateSong();
                break;
            case Menu.FUNCTION_LANGUAGE:
                ApoButton btn = getMainPanel().getButtonByFunction(FUNCTION_LANGUAGE);
                if (btn instanceof ApoButtonDropdown) {
                    ApoButtonDropdown dropdown = (ApoButtonDropdown) btn;
                    LanguageEnum[] langs = LanguageEnum.values();
                    int idx = dropdown.getSelectedIndex();
                    if (idx >= 0 && idx < langs.length) {
                        this.language = langs[idx];
                        Localization.getInstance().setLocale(this.language.getLocale());
                    }
                }
                break;
        }
    }

    private void setButtonsVisibility() {
        ApoButton btn = getMainPanel().getButtonByFunction(FUNCTION_LANGUAGE);
        if (btn instanceof ApoButtonDropdown) {
            ((ApoButtonDropdown) btn).setSelectedIndex(this.language.ordinal());
        }

        if (Constants.IS_HTML) {
            getMainPanel().getButtonByFunction(Menu.FUNCTION_BACK).setVisible(false);
        }
    }

    @Override
    protected void quit() {
        getMainPanel().quitGame();
    }

    @Override
    public void doThink(float delta) {

    }

    @Override
    public void render() {
        getMainPanel().spriteBatch.begin();
        getMainPanel().spriteBatch.draw(AssetLoader.backgroundTextureRegion, 0, 0);
        getMainPanel().drawString("Version: " + Constants.VERSION, Constants.GAME_WIDTH / 2f, Constants.GAME_HEIGHT - 20f, Constants.COLOR_WHITE, AssetLoader.font15, DrawString.MIDDLE, false, false);
        getMainPanel().drawTitle(Localization.getInstance().getCommon().get("title"), Constants.COLOR_WHITE, true);
        getMainPanel().spriteBatch.end();

        for (ApoButton button : this.getMainPanel().getButtons()) {
            button.render(this.getMainPanel());
        }
    }

    @Override
    public void dispose() {
    }
}
