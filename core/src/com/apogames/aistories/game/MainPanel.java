package com.apogames.aistories.game;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.createStory.CreateStory;
import com.apogames.aistories.game.creativeTonie.CreativeTonie;
import com.apogames.aistories.game.customEntity.CustomEntityEditor;
import com.apogames.aistories.game.listenStories.ListenStories;
import com.apogames.aistories.game.main.ChatGPTIO;
import com.apogames.aistories.game.main.Prompt;
import com.apogames.aistories.game.menu.Menu;
import com.apogames.aistories.game.settings.SongSettings;
import com.apogames.aistories.game.settings.SongSettingsScreen;
import com.apogames.aistories.game.settings.StorySettings;
import com.apogames.aistories.game.settings.StorySettingsScreen;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainPanel extends GameScreen {

    private static volatile int keyboardPixelHeight = 0;
    private static volatile float activeInputY = -1;
    private static volatile float activeInputHeight = 0;

    public static void setKeyboardPixelHeight(int height) {
        keyboardPixelHeight = height;
    }

    public static void setActiveInput(float y, float height) {
        activeInputY = y;
        activeInputHeight = height;
    }

    public static void clearActiveInput() {
        activeInputY = -1;
        activeInputHeight = 0;
    }

    private int calculateKeyboardOffset() {
        if (keyboardPixelHeight <= 0 || activeInputY < 0) return 0;
        float scaleY = (float) Constants.GAME_HEIGHT / Gdx.graphics.getHeight();
        float keyboardGameHeight = keyboardPixelHeight * scaleY;
        float keyboardTopY = Constants.GAME_HEIGHT - keyboardGameHeight;
        float inputBottomY = activeInputY + activeInputHeight;
        if (inputBottomY <= keyboardTopY) return 0;
        return (int) (inputBottomY - keyboardTopY + 15);
    }

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

    private static final String[] PROFILE_CATEGORIES = {"characters", "universe", "places", "objectives"};

    private CreateStory createStory;
    private ListenStories listenStory;
    private CreativeTonie creativeTonie;
    private CustomEntityEditor customEntityEditor;
    private Menu menu;
    private StorySettingsScreen storySettingsScreen;
    private SongSettingsScreen songSettingsScreen;

    private StorySettings storySettings;
    private SongSettings songSettings;

    private boolean songMode;

    private CustomEntity customMainEntity;
    private CustomEntity customSupportingEntity;
    private CustomEntity customUniverse;
    private CustomEntity customPlaces;
    private CustomEntity customObjectives;

    private final java.util.Map<String, List<CharacterProfile>> allOverrides = new java.util.HashMap<>();
    private final java.util.Map<String, List<CharacterProfile>> allCustomProfiles = new java.util.HashMap<>();

    private EnumInterface pendingCharacterSelection;
    private int pendingCharacterColumn = -1;

    public MainPanel() {
        super();
        if ((this.getButtons() == null) || (this.getButtons().isEmpty())) {
            ButtonProvider button = new ButtonProvider(this);
            button.init();
        }

        Gdx.graphics.setContinuousRendering(false);

        Localization.getInstance().setLocale(Locale.getDefault());

        this.storySettings = new StorySettings();
        this.storySettings.load();

        this.songSettings = new SongSettings();
        this.songSettings.load();

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
        loadCharacterProfiles();

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
        if (this.storySettingsScreen == null) {
            this.storySettingsScreen = new StorySettingsScreen(this);
        }
        if (this.songSettingsScreen == null) {
            this.songSettingsScreen = new SongSettingsScreen(this);
        }

        this.getPromptObject().updateFromSettings(this.storySettings);

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
        this.songMode = false;
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

    public void changeToListenStoriesForSong() {
        this.listenStory.createNewSong();
        this.changeModel(this.listenStory);
        this.listenStory.setButtonsInsivislbe();
    }

    public void changeToListenStoriesForLyrics() {
        this.listenStory.createNewLyrics();
        this.changeModel(this.listenStory);
        this.listenStory.setButtonsInsivislbe();
    }

    public void changeToCreativeTonie(FileHandle fileHandle) {
        this.creativeTonie.setFileHandle(fileHandle);
        this.changeModel(this.creativeTonie);
    }

    public void changeToCustomEntityEditor(CustomEntity customEntity) {
        this.customEntityEditor.setCustomEntity(customEntity);
        this.changeModel(this.customEntityEditor);
    }

    public void changeToEntityBrowse(EnumInterface currentSelection, java.util.List<EnumInterface> options, CustomEntity customEntity, boolean characterMode, String browseCategory) {
        this.customEntityEditor.setEntityBrowse(currentSelection, options, customEntity, characterMode, browseCategory);
        this.changeModel(this.customEntityEditor);
    }

    public EnumInterface getPendingCharacterSelection() {
        return pendingCharacterSelection;
    }

    public void setPendingCharacterSelection(EnumInterface selection) {
        this.pendingCharacterSelection = selection;
    }

    public int getPendingCharacterColumn() {
        return pendingCharacterColumn;
    }

    public void setPendingCharacterColumn(int column) {
        this.pendingCharacterColumn = column;
    }

    public void clearPendingCharacterSelection() {
        this.pendingCharacterSelection = null;
        this.pendingCharacterColumn = -1;
    }

    public void changeToStorySettings() {
        this.changeModel(this.storySettingsScreen);
    }

    public void changeToCreateSong() {
        this.songMode = true;
        this.changeModel(this.createStory);
    }

    public void changeToSongSettings() {
        this.changeModel(this.songSettingsScreen);
    }

    public StorySettings getStorySettings() {
        return this.storySettings;
    }

    public SongSettings getSongSettings() {
        return this.songSettings;
    }

    public boolean isSongMode() {
        return this.songMode;
    }

    public void setSongMode(boolean songMode) {
        this.songMode = songMode;
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

    public ListenStories getListenStory() {
        return this.listenStory;
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

    // --- Character Profile System ---

    public void loadCharacterProfiles() {
        for (String category : PROFILE_CATEGORIES) {
            loadProfilesForCategory(category);
        }
    }

    private void loadProfilesForCategory(String category) {
        CustomImageManager mgr = getImageManagerFor(category);

        String overridesPrefs = "characters".equals(category) ? "AIStoriesCharacterOverrides" : "AIStoriesOverrides_" + category;
        Preferences prefs = Gdx.app.getPreferences(overridesPrefs);
        int count = prefs.getInteger("override_count", 0);
        List<CharacterProfile> overrides = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CharacterProfile p = new CharacterProfile();
            p.setCategory(category);
            p.setBuiltInName(prefs.getString("override_" + i + "_builtInName", ""));
            p.setDisplayName(prefs.getString("override_" + i + "_name", ""));
            p.setDisplayDetails(prefs.getString("override_" + i + "_details", ""));
            p.setImageIndex(prefs.getInteger("override_" + i + "_imageIndex", 0));
            p.setCustomImageManager(mgr);
            overrides.add(p);
        }
        allOverrides.put(category, overrides);

        String profilesPrefs = "characters".equals(category) ? "AIStoriesSavedCharacters" : "AIStoriesProfiles_" + category;
        prefs = Gdx.app.getPreferences(profilesPrefs);
        count = prefs.getInteger("profile_count", 0);
        List<CharacterProfile> profiles = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CharacterProfile p = new CharacterProfile();
            p.setCategory(category);
            p.setId(prefs.getLong("profile_" + i + "_id", 0));
            p.setDisplayName(prefs.getString("profile_" + i + "_name", ""));
            p.setDisplayDetails(prefs.getString("profile_" + i + "_details", ""));
            p.setImageIndex(prefs.getInteger("profile_" + i + "_imageIndex", 0));
            p.setCustomImageManager(mgr);
            profiles.add(p);
        }
        allCustomProfiles.put(category, profiles);
    }

    public void saveProfilesForCategory(String category) {
        List<CharacterProfile> overrides = allOverrides.getOrDefault(category, java.util.Collections.emptyList());
        String overridesPrefs = "characters".equals(category) ? "AIStoriesCharacterOverrides" : "AIStoriesOverrides_" + category;
        Preferences prefs = Gdx.app.getPreferences(overridesPrefs);
        prefs.clear();
        prefs.putInteger("override_count", overrides.size());
        for (int i = 0; i < overrides.size(); i++) {
            CharacterProfile p = overrides.get(i);
            prefs.putString("override_" + i + "_builtInName", p.getBuiltInName());
            prefs.putString("override_" + i + "_name", p.getDisplayName());
            prefs.putString("override_" + i + "_details", p.getDisplayDetails());
            prefs.putInteger("override_" + i + "_imageIndex", p.getImageIndex());
        }
        prefs.flush();

        List<CharacterProfile> profiles = allCustomProfiles.getOrDefault(category, java.util.Collections.emptyList());
        String profilesPrefs = "characters".equals(category) ? "AIStoriesSavedCharacters" : "AIStoriesProfiles_" + category;
        prefs = Gdx.app.getPreferences(profilesPrefs);
        prefs.clear();
        prefs.putInteger("profile_count", profiles.size());
        for (int i = 0; i < profiles.size(); i++) {
            CharacterProfile p = profiles.get(i);
            prefs.putLong("profile_" + i + "_id", p.getId());
            prefs.putString("profile_" + i + "_name", p.getDisplayName());
            prefs.putString("profile_" + i + "_details", p.getDisplayDetails());
            prefs.putInteger("profile_" + i + "_imageIndex", p.getImageIndex());
        }
        prefs.flush();
    }

    public CharacterProfile getOverrideFor(String category, String builtInName) {
        List<CharacterProfile> overrides = allOverrides.get(category);
        if (overrides == null) return null;
        for (CharacterProfile p : overrides) {
            if (p.getBuiltInName().equals(builtInName)) return p;
        }
        return null;
    }

    public CharacterProfile getOverrideFor(String builtInName) {
        return getOverrideFor("characters", builtInName);
    }

    public List<CharacterProfile> getCustomProfiles(String category) {
        return allCustomProfiles.getOrDefault(category, new ArrayList<>());
    }

    public List<CharacterProfile> getCustomProfiles() {
        return getCustomProfiles("characters");
    }

    public void saveOverride(String category, CharacterProfile override) {
        List<CharacterProfile> overrides = allOverrides.computeIfAbsent(category, k -> new ArrayList<>());
        overrides.removeIf(p -> p.getBuiltInName().equals(override.getBuiltInName()));
        overrides.add(override);
        saveProfilesForCategory(category);
    }

    public void saveOverride(CharacterProfile override) {
        saveOverride("characters", override);
    }

    public void removeOverride(String category, String builtInName) {
        List<CharacterProfile> overrides = allOverrides.get(category);
        if (overrides != null) overrides.removeIf(p -> p.getBuiltInName().equals(builtInName));
        saveProfilesForCategory(category);
    }

    public void removeOverride(String builtInName) {
        removeOverride("characters", builtInName);
    }

    public void addProfile(String category, CharacterProfile profile) {
        List<CharacterProfile> profiles = allCustomProfiles.computeIfAbsent(category, k -> new ArrayList<>());
        profiles.add(profile);
        saveProfilesForCategory(category);
    }

    public void addProfile(CharacterProfile profile) {
        addProfile("characters", profile);
    }

    public void deleteProfile(String category, long id) {
        List<CharacterProfile> profiles = allCustomProfiles.get(category);
        if (profiles != null) profiles.removeIf(p -> p.getId() == id);
        saveProfilesForCategory(category);
    }

    public void deleteProfile(long id) {
        deleteProfile("characters", id);
    }

    public void saveCharacterProfiles() {
        saveProfilesForCategory("characters");
    }

    public List<EnumInterface> buildEffectiveOptions(String category, EnumInterface[] enumValues) {
        List<EnumInterface> options = new ArrayList<>();
        for (EnumInterface e : enumValues) {
            CharacterProfile override = getOverrideFor(category, e.getName());
            if (override != null) {
                options.add(override);
            } else {
                options.add(e);
            }
        }
        options.addAll(getCustomProfiles(category));
        return options;
    }

    public List<EnumInterface> buildEffectiveCharacterOptions() {
        return buildEffectiveOptions("characters", MainCharacter.values());
    }

    public CustomImageManager getImageManagerFor(String category) {
        switch (category) {
            case "characters": return this.customMainEntity.getCustomImageManager();
            case "universe": return this.customUniverse.getCustomImageManager();
            case "places": return this.customPlaces.getCustomImageManager();
            case "objectives": return this.customObjectives.getCustomImageManager();
            default: return null;
        }
    }

    public CustomImageManager getSharedCharacterImageManager() {
        return getImageManagerFor("characters");
    }

    private void changeModel(final ScreenModel model) {
        if (this.model != null) {
            this.model.dispose();
        }

        Gdx.input.setOnscreenKeyboardVisible(false);
        clearActiveInput();

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
            int offsetY = calculateKeyboardOffset();
            if (offsetY != 0) {
                cam.position.y -= offsetY;
                cam.update();
                spriteBatch.setProjectionMatrix(cam.combined);
                getRenderer().setProjectionMatrix(cam.combined);
            }

            model.render();

            if (offsetY != 0) {
                cam.position.y += offsetY;
                cam.update();
                spriteBatch.setProjectionMatrix(cam.combined);
                getRenderer().setProjectionMatrix(cam.combined);
            }

            model.drawOverlay();
        }
        this.spriteBatch.begin();
        this.drawString(String.valueOf(Gdx.graphics.getFramesPerSecond()), 5, 5, Constants.COLOR_PURPLE, AssetLoader.font15, DrawString.BEGIN, false, false);
        this.spriteBatch.end();
    }

    private int fullScreenWidth = 0;
    private int fullScreenHeight = 0;

    @Override
    public void resize(int width, int height) {
        if (fullScreenWidth == 0 || (width >= fullScreenWidth && height >= fullScreenHeight)) {
            fullScreenWidth = width;
            fullScreenHeight = height;
            super.resize(width, height);
        }
    }

    public void renderBackground() {
        this.getRenderer().begin(ShapeType.Filled);
        this.getRenderer().setColor(Constants.COLOR_BACKGROUND[0], Constants.COLOR_BACKGROUND[1], Constants.COLOR_BACKGROUND[2], 1);
        this.getRenderer().rect(0, 0, Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        this.getRenderer().end();
    }
}
