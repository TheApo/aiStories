package com.apogames.aistories.game.createStory;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.MainPanel;
import com.apogames.aistories.game.main.ChatGPTIO;
import com.apogames.aistories.game.main.SongPrompt;
import com.apogames.aistories.game.main.SunoApiIO;
import com.apogames.aistories.game.objects.*;
import com.apogames.aistories.game.settings.SongSettings;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.DrawString;
import com.apogames.backend.SequentiallyThinkingScreenModel;
import com.apogames.common.Localization;
import com.apogames.entity.ApoButton;
import com.apogames.entity.ApoButtonSwitch;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;

public class CreateStory extends SequentiallyThinkingScreenModel {

    public static final String FUNCTION_BACK = "CREATESTORY_QUIT";

    public static final String FUNCTION_LLM = "CREATESTORY_LLM";

    public static final String FUNCTION_GENERATE_TEXT = "CREATESTORY_START";
    public static final String FUNCTION_NEWPROMPT = "CREATESTORY_NEWPROMPT";
    public static final String FUNCTION_SETTINGS = "CREATESTORY_SETTINGS";

    private final boolean[] keys = new boolean[256];

    private boolean isPressed = false;


    private ArrayList<ObjectSelection> objectSelection;

    public CreateStory(final MainPanel game) {
        super(game);
    }

    public void setNeededButtonsVisible() {
        getMainPanel().getButtonByFunction(FUNCTION_BACK).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_LLM).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_GENERATE_TEXT).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_NEWPROMPT).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_SETTINGS).setVisible(true);

        if (getMainPanel().isSongMode()) {
            // Song mode: hide LLM switch, change generate button text
            getMainPanel().getButtonByFunction(FUNCTION_LLM).setVisible(false);
            getMainPanel().getButtonByFunction(FUNCTION_GENERATE_TEXT).setId("button_generate_song");
        } else {
            getMainPanel().getButtonByFunction(FUNCTION_GENERATE_TEXT).setId("button_start");
            boolean hasOpenAI = ChatGPTIO.API_KEY != null && !ChatGPTIO.API_KEY.isEmpty() && !ChatGPTIO.API_KEY.equals("Dein ChatGPT API Key");
            boolean hasGemini = ChatGPTIO.GEMINI_API_KEY != null && !ChatGPTIO.GEMINI_API_KEY.isEmpty() && !ChatGPTIO.GEMINI_API_KEY.equals("Dein Gemini API Key");
            ApoButtonSwitch llmButton = (ApoButtonSwitch) getMainPanel().getButtonByFunction(FUNCTION_LLM);
            if (!hasOpenAI && !hasGemini) {
                getMainPanel().getButtonByFunction(FUNCTION_GENERATE_TEXT).setVisible(false);
                llmButton.setVisible(false);
            } else if (hasOpenAI && hasGemini) {
                llmButton.setLabels("GPT-5-mini", "Gemini-3");
                llmButton.setSelect(this.getMainPanel().getChatGPT().getLlm().startsWith("gemini"));
            } else {
                if (hasGemini) {
                    this.getMainPanel().getChatGPT().setLlm(ChatGPTIO.LLM_MODEL_GEMINI);
                    llmButton.setSingleLabel("Gemini-3");
                } else {
                    this.getMainPanel().getChatGPT().setLlm(ChatGPTIO.LLM_MODEL_MINI);
                    llmButton.setSingleLabel("GPT-5-mini");
                }
            }
        }

        this.createObjectSelection();
        for (ObjectSelection selection : this.objectSelection) {
            selection.setNeededButtonsVisible();
        }
    }

    @Override
    public void init() {
        if (getGameProperties() == null) {
            setGameProperties(new CreateStoryPreferences(this));
            loadProperties();
        }

        this.createObjectSelection();

        this.getMainPanel().resetSize(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);

        this.setNeededButtonsVisible();
    }

    private void createObjectSelection() {
        if (this.objectSelection == null) {
            this.objectSelection = new ArrayList<>();

            // MainCharacter with custom character support
            this.objectSelection.add(new ObjectSelection(this.getMainPanel(), 10, 85, 250, 600,
                    this.getMainPanel().getPromptObject().getGameObjectives().getMainCharacter(),
                    Constants.COLOR_MAIN_CHARACTER, MainCharacter.values(), this.getMainPanel().getCustomMainEntity()));

            // SupportingCharacter with custom character support
            this.objectSelection.add(new ObjectSelection(this.getMainPanel(), 10 + 270, 85, 250, 600,
                    this.getMainPanel().getPromptObject().getGameObjectives().getSupportingCharacter(),
                    Constants.COLOR_SIDE_CHARACTER, SupportingCharacter.values(), this.getMainPanel().getCustomSupportingEntity()));

            // Universe, Places, Objectives - with custom support
            this.objectSelection.add(new ObjectSelection(this.getMainPanel(), 10 + 270 * 2, 85, 250, 600,
                    this.getMainPanel().getPromptObject().getGameObjectives().getUniverse(),
                    Constants.COLOR_UNIVERSE, Universe.values(), this.getMainPanel().getCustomUniverse()));
            this.objectSelection.add(new ObjectSelection(this.getMainPanel(), 10 + 270 * 3, 85, 250, 600,
                    this.getMainPanel().getPromptObject().getGameObjectives().getPlaces(),
                    Constants.COLOR_PLACES, Places.values(), this.getMainPanel().getCustomPlaces()));
            this.objectSelection.add(new ObjectSelection(this.getMainPanel(), 10 + 270 * 4, 85, 250, 600,
                    this.getMainPanel().getPromptObject().getGameObjectives().getObjectives(),
                    Constants.COLOR_OBJECTIVES, Objectives.values(), this.getMainPanel().getCustomObjectives()));
        }
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
        for (ObjectSelection selection : this.objectSelection) {
            selection.mouseButtonFunction(function);
        }

        // Check for customize or jump-to-custom button clicks
        for (ObjectSelection selection : this.objectSelection) {
            if (selection.getCustomEntity() != null) {
                if (function.equals(selection.getJumpCustomFunctionId())) {
                    getMainPanel().changeToCustomEntityEditor(selection.getCustomEntity());
                    return;
                }
            }
        }

        switch (function) {
            case CreateStory.FUNCTION_BACK:
                quit();
                break;
            case CreateStory.FUNCTION_LLM:
                ApoButton buttonByFunction = getMainPanel().getButtonByFunction(FUNCTION_LLM);
                buttonByFunction.setSelect(!buttonByFunction.isSelect());
                if (buttonByFunction.isSelect()) {
                    this.getMainPanel().getChatGPT().setLlm(ChatGPTIO.LLM_MODEL_GEMINI);
                } else {
                    this.getMainPanel().getChatGPT().setLlm(ChatGPTIO.LLM_MODEL_MINI);
                }
                break;
            case CreateStory.FUNCTION_GENERATE_TEXT:
                if (getMainPanel().isSongMode()) {
                    startSong();
                } else {
                    start();
                    getMainPanel().changeToListenStories(true);
                }
                break;
            case CreateStory.FUNCTION_NEWPROMPT:
                shufflePrompt();
                break;
            case CreateStory.FUNCTION_SETTINGS:
                if (getMainPanel().isSongMode()) {
                    getMainPanel().changeToSongSettings();
                } else {
                    getMainPanel().changeToStorySettings();
                }
                break;
        }
    }

    public void shufflePrompt() {
        MainPanel main = this.getMainPanel();
        main.getPromptObject().getGameObjectives().shuffleWithCustoms(
                main.getCustomMainEntity(), main.getCustomSupportingEntity(),
                main.getCustomUniverse(), main.getCustomPlaces(), main.getCustomObjectives());
        main.getPromptObject().setUpPrompt();
        main.getTextArea().setText(main.getPromptObject().getPrompt());

        this.objectSelection.get(0).setGameObjective(main.getPromptObject().getGameObjectives().getMainCharacter());
        this.objectSelection.get(1).setGameObjective(main.getPromptObject().getGameObjectives().getSupportingCharacter());
        this.objectSelection.get(2).setGameObjective(main.getPromptObject().getGameObjectives().getUniverse());
        this.objectSelection.get(3).setGameObjective(main.getPromptObject().getGameObjectives().getPlaces());
        this.objectSelection.get(4).setGameObjective(main.getPromptObject().getGameObjectives().getObjectives());
    }

    private void applySelectedObjectives() {
        GameObjectives go = this.getMainPanel().getPromptObject().getGameObjectives();
        go.setMainCharacter(this.objectSelection.get(0).isEnabled() ? this.objectSelection.get(0).getGameObjective() : null);
        go.setSupportingCharacter(this.objectSelection.get(1).isEnabled() ? this.objectSelection.get(1).getGameObjective() : null);
        go.setUniverse(this.objectSelection.get(2).isEnabled() ? this.objectSelection.get(2).getGameObjective() : null);
        go.setPlaces(this.objectSelection.get(3).isEnabled() ? this.objectSelection.get(3).getGameObjective() : null);
        go.setObjectives(this.objectSelection.get(4).isEnabled() ? this.objectSelection.get(4).getGameObjective() : null);
    }

    private void startSong() {
        Gdx.app.log("create Song", "create Song begin");
        applySelectedObjectives();

        GameObjectives go = getMainPanel().getPromptObject().getGameObjectives();
        SongSettings songSettings = getMainPanel().getSongSettings();

        String sunoPrompt = SongPrompt.buildSunoPrompt(songSettings, go);

        this.getMainPanel().getTextArea().setText(sunoPrompt);

        SunoApiIO sunoApi = new SunoApiIO(getMainPanel().getListenStory());
        String header = (go.getMainCharacter() != null ? go.getMainCharacter().getName() : "") + ";"
                + (go.getSupportingCharacter() != null ? go.getSupportingCharacter().getName() : "") + ";"
                + (go.getUniverse() != null ? go.getUniverse().getName() : "") + ";"
                + (go.getPlaces() != null ? go.getPlaces().getName() : "") + ";"
                + (go.getObjectives() != null ? go.getObjectives().getName() : "");
        sunoApi.setCharacterHeader(header);

        getMainPanel().changeToListenStoriesForSong();

        Gdx.app.log("SunoPrompt", "Length: " + sunoPrompt.length() + " chars");
        sunoApi.generateSong(sunoPrompt);
        Gdx.app.log("create Song", "create Song end");
    }

    private void start() {
        Gdx.app.log("create Story", "create Story begin");
        applySelectedObjectives();
        this.getMainPanel().getPromptObject().setUpPrompt();
        this.getMainPanel().getTextArea().setText(this.getMainPanel().getPromptObject().getPrompt());
        this.getMainPanel().getChatGPT().reset();
        this.getMainPanel().getChatGPT().sendAnotherMessage(this.getMainPanel().getPromptObject().getPrompt());
        Gdx.app.log("create Story", "create Story end");
    }

    @Override
    protected void quit() {
        getMainPanel().setSongMode(false);
        getMainPanel().changeToMenu();
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
        for (ObjectSelection selection : this.objectSelection) {
            selection.renderFilled(getMainPanel(), 0, 0);
        }
        getMainPanel().getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Line);
        for (ObjectSelection selection : this.objectSelection) {
            selection.renderLine(getMainPanel(), 0, 0);
        }
        getMainPanel().getRenderer().end();

        String titleKey = getMainPanel().isSongMode() ? "song_title" : "create_story_title";
        getMainPanel().drawTitle(Localization.getInstance().getCommon().get(titleKey), Constants.COLOR_WHITE, false);

        for (ObjectSelection selection : this.objectSelection) {
            selection.renderSprite(getMainPanel(), 0, 0);
        }

        getMainPanel().drawString("Version: " + Constants.VERSION, Constants.GAME_WIDTH / 2f, Constants.GAME_HEIGHT - 20f, Constants.COLOR_WHITE, AssetLoader.font15, DrawString.MIDDLE, false, false);

        getMainPanel().spriteBatch.end();

        for (ApoButton button : this.getMainPanel().getButtons()) {
            button.render(this.getMainPanel());
        }
    }

    @Override
    public void dispose() {
    }
}
