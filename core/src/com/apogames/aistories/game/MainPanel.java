package com.apogames.aistories.game;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.createStory.CreateStory;
import com.apogames.aistories.game.creativeTonie.CreativeTonie;
import com.apogames.aistories.game.customEntity.CustomEntityEditor;
import com.apogames.aistories.game.listenStories.ListenStories;
import com.apogames.aistories.game.main.ChatGPTIO;
import com.apogames.aistories.game.main.Prompt;
import com.apogames.aistories.game.menu.Menu;
import com.apogames.aistories.game.customEntity.CustomImageManager;
import com.apogames.aistories.game.objects.*;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.DrawString;
import com.apogames.backend.GameScreen;
import com.apogames.backend.ScreenModel;
import com.apogames.common.Localization;
import com.apogames.entity.TextArea;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import java.util.Locale;

public class MainPanel extends GameScreen {

    private static final String PREFS_NAME = "AIStoriesCustomEntityPreferences";
    private static final String PREF_CUSTOM_MAIN_NAME = "custom_main_name";
    private static final String PREF_CUSTOM_MAIN_DETAILS = "custom_main_details";
    private static final String PREF_CUSTOM_MAIN_IMAGE = "custom_main_imageIndex";
    private static final String PREF_CUSTOM_SUPPORT_NAME = "custom_support_name";
    private static final String PREF_CUSTOM_SUPPORT_DETAILS = "custom_support_details";
    private static final String PREF_CUSTOM_SUPPORT_IMAGE = "custom_support_imageIndex";
    private static final String PREF_CUSTOM_UNIVERSE_NAME = "custom_universe_name";
    private static final String PREF_CUSTOM_UNIVERSE_DETAILS = "custom_universe_details";
    private static final String PREF_CUSTOM_UNIVERSE_IMAGE = "custom_universe_imageIndex";
    private static final String PREF_CUSTOM_PLACES_NAME = "custom_places_name";
    private static final String PREF_CUSTOM_PLACES_DETAILS = "custom_places_details";
    private static final String PREF_CUSTOM_PLACES_IMAGE = "custom_places_imageIndex";
    private static final String PREF_CUSTOM_OBJECTIVES_NAME = "custom_objectives_name";
    private static final String PREF_CUSTOM_OBJECTIVES_DETAILS = "custom_objectives_details";
    private static final String PREF_CUSTOM_OBJECTIVES_IMAGE = "custom_objectives_imageIndex";

    private CreateStory createStory;
    private ListenStories listenStory;
    private CreativeTonie creativeTonie;
    private CustomEntityEditor customEntityEditor;
    private Menu menu;

    private CustomEntity customMainEntity;
    private CustomEntity customSupportingEntity;
    private CustomEntity customUniverse;
    private CustomEntity customPlaces;
    private CustomEntity customObjectives;

    public MainPanel() {
        super();
        if ((this.getButtons() == null) || (this.getButtons().isEmpty())) {
            ButtonProvider button = new ButtonProvider(this);
            button.init();
        }

        Gdx.graphics.setContinuousRendering(false);

        Localization.getInstance().setLocale(Locale.getDefault());

        FileHandle dirHandle = Gdx.files.local(Prompt.DIRECTORY);

        if (!dirHandle.exists()) {
            dirHandle.mkdirs();
        }

        // Create custom characters and load from preferences
        this.customMainEntity = new CustomEntity("mainCharacter");
        this.customSupportingEntity = new CustomEntity("supportingCharacter");
        this.customUniverse = new CustomEntity("universe");
        this.customPlaces = new CustomEntity("places");
        this.customObjectives = new CustomEntity("objectives");

        CustomImageManager sharedCharacterImages = new CustomImageManager("characters");
        this.customMainEntity.setCustomImageManager(sharedCharacterImages);
        this.customSupportingEntity.setCustomImageManager(sharedCharacterImages);
        this.customUniverse.setCustomImageManager(new CustomImageManager("universe"));
        this.customPlaces.setCustomImageManager(new CustomImageManager("places"));
        this.customObjectives.setCustomImageManager(new CustomImageManager("objectives"));

        loadCustomEntityPreferences();

        if (this.createStory == null) {
            this.createStory = new CreateStory(this);
        }
        if (this.listenStory == null) {
            this.listenStory = new ListenStories(this);
        }
        if (this.menu == null) {
            this.menu = new Menu(this);
        }
        if (this.creativeTonie == null) {
            this.creativeTonie = new CreativeTonie(this);
        }
        if (this.customEntityEditor == null) {
            this.customEntityEditor = new CustomEntityEditor(this);
        }

        this.createStory.init();
        this.listenStory.init();

        this.changeToMenu();
    }

    public CustomEntity getCustomMainEntity() {
        return this.customMainEntity;
    }

    public CustomEntity getCustomSupportingEntity() {
        return this.customSupportingEntity;
    }

    public CustomEntity getCustomUniverse() {
        return this.customUniverse;
    }

    public CustomEntity getCustomPlaces() {
        return this.customPlaces;
    }

    public CustomEntity getCustomObjectives() {
        return this.customObjectives;
    }

    public boolean isStoryAvailable() {
        return this.listenStory.isStoryAvailable();
    }

    public void changeToMenu() {
        this.changeModel(this.menu);
    }

    public void changeToCreateStory() {
        this.changeModel(this.createStory);
    }

    public void changeToListenStories() {
        changeToListenStories(false);
    }

    public void changeToListenStories(boolean createNewStory) {
        if (createNewStory) {
            this.listenStory.createNewStory();
        }
        this.changeModel(this.listenStory);
        if (createNewStory) {
            this.listenStory.setButtonsInsivislbe();
        }
    }

    public void changeToCreativeTonie(FileHandle fileHandle) {
        this.creativeTonie.setFileHandle(fileHandle);
        this.changeModel(this.creativeTonie);
    }

    public void changeToCustomEntityEditor(CustomEntity customEntity) {
        this.customEntityEditor.setCustomEntity(customEntity);
        this.changeModel(this.customEntityEditor);
    }

    public void reInit() {
        MainCharacter.refreshAll();
        SupportingCharacter.refreshAll();
        Universe.refreshAll();
        Places.refreshAll();
        Objectives.refreshAll();

        this.getPromptObject().getGameObjectives().refresh();
    }

    /**
     * Quit game.
     */
    public final void quitGame() {
        this.saveProperties();
        Gdx.app.exit();
    }

    /**
     * Update level chooser.
     */
    public void saveProperties() {
    }

    public ChatGPTIO getChatGPT() {
        return this.listenStory.getChatGPT();
    }

    public Prompt getPromptObject() {
        return this.listenStory.getPrompt();
    }

    public TextArea getTextArea() {
        return this.listenStory.getTextArea();
    }

    public void loadCustomEntityPreferences() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);

        loadCustomPrefs(prefs, this.customMainEntity, PREF_CUSTOM_MAIN_NAME, PREF_CUSTOM_MAIN_DETAILS, PREF_CUSTOM_MAIN_IMAGE);
        loadCustomPrefs(prefs, this.customSupportingEntity, PREF_CUSTOM_SUPPORT_NAME, PREF_CUSTOM_SUPPORT_DETAILS, PREF_CUSTOM_SUPPORT_IMAGE);
        loadCustomPrefs(prefs, this.customUniverse, PREF_CUSTOM_UNIVERSE_NAME, PREF_CUSTOM_UNIVERSE_DETAILS, PREF_CUSTOM_UNIVERSE_IMAGE);
        loadCustomPrefs(prefs, this.customPlaces, PREF_CUSTOM_PLACES_NAME, PREF_CUSTOM_PLACES_DETAILS, PREF_CUSTOM_PLACES_IMAGE);
        loadCustomPrefs(prefs, this.customObjectives, PREF_CUSTOM_OBJECTIVES_NAME, PREF_CUSTOM_OBJECTIVES_DETAILS, PREF_CUSTOM_OBJECTIVES_IMAGE);
    }

    private void loadCustomPrefs(Preferences prefs, CustomEntity custom, String nameKey, String detailsKey, String imageKey) {
        String defaultName = Localization.getInstance().getCommon().get(custom.getName());
        String defaultDetails = Localization.getInstance().getCommon().get(custom.getName() + "_details");
        custom.setCustomName(prefs.getString(nameKey, defaultName));
        custom.setCustomDetails(prefs.getString(detailsKey, defaultDetails));
        custom.setImageIndex(prefs.getInteger(imageKey, 0));
    }

    public void saveCustomEntityPreferences() {
        Preferences prefs = Gdx.app.getPreferences(PREFS_NAME);
        saveCustomPrefs(prefs, this.customMainEntity, PREF_CUSTOM_MAIN_NAME, PREF_CUSTOM_MAIN_DETAILS, PREF_CUSTOM_MAIN_IMAGE);
        saveCustomPrefs(prefs, this.customSupportingEntity, PREF_CUSTOM_SUPPORT_NAME, PREF_CUSTOM_SUPPORT_DETAILS, PREF_CUSTOM_SUPPORT_IMAGE);
        saveCustomPrefs(prefs, this.customUniverse, PREF_CUSTOM_UNIVERSE_NAME, PREF_CUSTOM_UNIVERSE_DETAILS, PREF_CUSTOM_UNIVERSE_IMAGE);
        saveCustomPrefs(prefs, this.customPlaces, PREF_CUSTOM_PLACES_NAME, PREF_CUSTOM_PLACES_DETAILS, PREF_CUSTOM_PLACES_IMAGE);
        saveCustomPrefs(prefs, this.customObjectives, PREF_CUSTOM_OBJECTIVES_NAME, PREF_CUSTOM_OBJECTIVES_DETAILS, PREF_CUSTOM_OBJECTIVES_IMAGE);
        prefs.flush();
    }

    private void saveCustomPrefs(Preferences prefs, CustomEntity custom, String nameKey, String detailsKey, String imageKey) {
        prefs.putString(nameKey, custom.getCustomName());
        prefs.putString(detailsKey, custom.getCustomDetails());
        prefs.putInteger(imageKey, custom.getImageIndex());
    }

    private void changeModel(final ScreenModel model) {
        if (this.model != null) {
            this.model.dispose();
        }

        this.model = model;

        this.setButtonsInvisible();
        this.model.setNeededButtonsVisible();
        this.model.init();
    }

    public final void setButtonsInvisible() {
        for (int i = 0; i < this.getButtons().size(); i++) {
            this.getButtons().get(i).setVisible(false);
        }
    }

    public void think(final float delta) {
        super.think(delta);
        if (model != null) model.think(delta);
    }

    public void render(float delta) {
        super.render(delta);

        if (model != null) {
            model.render();
            model.drawOverlay();
        }
        this.spriteBatch.begin();
        this.drawString(String.valueOf(Gdx.graphics.getFramesPerSecond()), 5, 5, Constants.COLOR_PURPLE, AssetLoader.font15, DrawString.BEGIN, false, false);
        this.spriteBatch.end();
    }

    public void renderBackground() {
        this.getRenderer().begin(ShapeType.Filled);
        this.getRenderer().setColor(Constants.COLOR_BACKGROUND[0], Constants.COLOR_BACKGROUND[1], Constants.COLOR_BACKGROUND[2], 1);
        this.getRenderer().rect(0, 0, Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        this.getRenderer().end();
    }
}
