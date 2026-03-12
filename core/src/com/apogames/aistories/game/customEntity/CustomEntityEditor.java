package com.apogames.aistories.game.customEntity;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.MainPanel;
import com.apogames.aistories.game.objects.*;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.DrawString;
import com.apogames.backend.SequentiallyThinkingScreenModel;
import com.apogames.common.Localization;
import com.apogames.entity.ApoButton;
import com.apogames.entity.Textfield;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class CustomEntityEditor extends SequentiallyThinkingScreenModel {

    public static final String FUNCTION_BACK = "CUSTOMEDITOR_QUIT";
    public static final String FUNCTION_CONFIRM = "CUSTOMEDITOR_CONFIRM";
    public static final String FUNCTION_AI_GENERATE = "CUSTOMEDITOR_AI_GENERATE";
    public static final String FUNCTION_MANAGE = "CUSTOMEDITOR_MANAGE";
    public static final String FUNCTION_BACK_TO_GRID = "CUSTOMEDITOR_BACK_TO_GRID";
    public static final String FUNCTION_DO_GENERATE = "CUSTOMEDITOR_DO_GENERATE";
    public static final String FUNCTION_LLM_SWITCH = "CUSTOMEDITOR_LLM_SWITCH";
    public static final String FUNCTION_NEW_PROFILE = "CUSTOMEDITOR_NEW_PROFILE";
    public static final String FUNCTION_RESET = "CUSTOMEDITOR_RESET";
    public static final String FUNCTION_DELETE = "CUSTOMEDITOR_DELETE";
    public static final String FUNCTION_EDIT_PENCIL = "CUSTOMEDITOR_EDIT_PENCIL";

    private static final int PANEL_Y = 90;
    private static final int PANEL_RADIUS = 10;

    private static final int LEFT_PANEL_X = 10;
    private static final int LEFT_PANEL_W = 420;
    private static final int LABEL_Y = 105;
    private static final int NAME_FIELD_Y = 135;
    private static final int DETAILS_LABEL_Y = 195;
    private static final int DETAILS_FIELD_Y = 225;
    private static final int PREVIEW_SIZE = 260;

    private static final int GRID_COLS = 10;
    private static final int GRID_SPRITE_SIZE = 80;
    private static final int GRID_PADDING = 5;
    private static final int GRID_START_X = 450;
    private static final int GRID_START_Y = 140;
    private static final int GRID_VISIBLE_HEIGHT = Constants.GAME_HEIGHT - GRID_START_Y - 20;

    // Style grid in generate mode
    private static final int STYLE_COLS = 4;
    private static final int STYLE_BUTTON_W = 210;
    private static final int STYLE_BUTTON_H = 55;
    private static final int STYLE_ICON_SIZE = 40;
    private static final int STYLE_PADDING = 8;
    private static final int STYLE_START_X = 470;
    private static final int STYLE_START_Y = 170;

    private enum Mode { GRID, GENERATE, MANAGE }
    private enum CharacterEditMode { BUILTIN, PROFILE, NEW }

    private final boolean[] keys = new boolean[256];

    private CustomEntity customEntity;

    // Character profile editing
    private boolean isCharacterMode;
    private CharacterEditMode characterEditMode;
    private String editBuiltInName;
    private CharacterProfile workingProfile;
    private EnumInterface editTarget;

    // Browse mode
    private boolean browseMode;
    private java.util.List<EnumInterface> browseOptions;
    private int browseIndex;
    private String browseCategory;

    private Textfield nameField;
    private Textfield detailsField;
    private Textfield activeField;
    private Textfield imagePromptField;

    private int selectedImageIndex;
    private int totalImages;

    private Mode currentMode = Mode.GRID;
    private int scrollOffset = 0;
    private int selectedStyleIndex = 0;
    private boolean isGenerating = false;
    private String generateError = null;
    private String imageLlm;

    private String getProfileCategory() {
        if ("mainCharacter".equals(browseCategory) || "supportingCharacter".equals(browseCategory)) {
            return "characters";
        }
        return browseCategory;
    }

    public CustomEntityEditor(final MainPanel game) {
        super(game);
        this.totalImages = 60;
    }

    public void setCustomEntity(CustomEntity customEntity) {
        this.customEntity = customEntity;
        this.isCharacterMode = false;
        this.browseMode = false;
        if (customEntity != null) {
            this.totalImages = customEntity.getTotalImages();
        }
    }

    public void setEntityBrowse(EnumInterface currentSelection, java.util.List<EnumInterface> options, CustomEntity customEntity, boolean characterMode, String browseCategory) {
        this.customEntity = customEntity;
        this.isCharacterMode = true;
        this.browseMode = true;
        this.browseCategory = browseCategory;
        this.browseOptions = new java.util.ArrayList<>(options);
        this.totalImages = this.browseOptions.size();
        this.editTarget = currentSelection;
        this.browseIndex = 0;
        for (int i = 0; i < options.size(); i++) {
            if (options.get(i) == currentSelection || options.get(i).getName().equals(currentSelection.getName())) {
                this.browseIndex = i;
                break;
            }
        }
    }

    public void setCharacterEdit(EnumInterface character) {
        this.customEntity = null;
        this.isCharacterMode = true;
        this.editTarget = character;

        if (character instanceof CharacterProfile) {
            CharacterProfile cp = (CharacterProfile) character;
            if (cp.isBuiltInOverride()) {
                this.characterEditMode = CharacterEditMode.BUILTIN;
                this.editBuiltInName = cp.getBuiltInName();
                this.workingProfile = cp;
            } else {
                this.characterEditMode = CharacterEditMode.PROFILE;
                this.workingProfile = cp;
                this.editBuiltInName = null;
            }
        } else {
            this.characterEditMode = CharacterEditMode.BUILTIN;
            this.editBuiltInName = character.getName();
            this.workingProfile = getMainPanel().getOverrideFor(getProfileCategory(), character.getName());
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
            this.detailsField = new Textfield(30, DETAILS_FIELD_Y, 380, 170, AssetLoader.font20);
            this.detailsField.init();
            this.detailsField.setMaxLength(300);
            this.detailsField.setFixedFont(true);
            this.detailsField.setMultiLine(true);
        }
        if (this.imagePromptField == null) {
            int gridWidth = STYLE_COLS * (STYLE_BUTTON_W + STYLE_PADDING) - STYLE_PADDING;
            this.imagePromptField = new Textfield(STYLE_START_X, 400, gridWidth, 70, AssetLoader.font20);
            this.imagePromptField.init();
            this.imagePromptField.setMaxLength(200);
            this.imagePromptField.setFixedFont(true);
            this.imagePromptField.setMultiLine(true);
        }

        if (this.browseMode) {
            updateBrowseDisplay();
        } else if (this.isCharacterMode) {
            initCharacterMode();
        } else if (this.customEntity != null) {
            this.nameField.setCurString(this.customEntity.getCustomName());
            this.nameField.setPosition(this.customEntity.getCustomName().length());
            this.detailsField.setCurString(this.customEntity.getCustomDetails());
            this.detailsField.setPosition(this.customEntity.getCustomDetails().length());
            this.selectedImageIndex = this.customEntity.getImageIndex();
            this.totalImages = this.customEntity.getTotalImages();
        }

        this.nameField.setSelect(false);
        this.detailsField.setSelect(false);
        this.imagePromptField.setSelect(false);
        this.activeField = null;
        this.currentMode = Mode.GRID;
        this.scrollOffset = 0;
        this.selectedStyleIndex = 0;
        this.isGenerating = false;
        this.generateError = null;
        this.imagePromptField.setCurString("");
        this.imageLlm = getCurrentLlm();

        this.getMainPanel().resetSize(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);
        this.setNeededButtonsVisible();
    }

    private void initCharacterMode() {
        this.totalImages = getCharacterTotalImages();
        switch (characterEditMode) {
            case BUILTIN:
                if (workingProfile != null) {
                    nameField.setCurString(workingProfile.getDisplayName());
                    nameField.setPosition(workingProfile.getDisplayName().length());
                    detailsField.setCurString(workingProfile.getDisplayDetails());
                    detailsField.setPosition(workingProfile.getDisplayDetails().length());
                    selectedImageIndex = workingProfile.getImageIndex();
                } else if (editTarget != null) {
                    nameField.setCurString(editTarget.getDisplayName());
                    nameField.setPosition(editTarget.getDisplayName().length());
                    detailsField.setCurString(editTarget.getDisplayDetails());
                    detailsField.setPosition(editTarget.getDisplayDetails().length());
                    selectedImageIndex = findBuiltInImageIndex(editTarget);
                }
                break;
            case PROFILE:
                if (workingProfile != null) {
                    nameField.setCurString(workingProfile.getDisplayName());
                    nameField.setPosition(workingProfile.getDisplayName().length());
                    detailsField.setCurString(workingProfile.getDisplayDetails());
                    detailsField.setPosition(workingProfile.getDisplayDetails().length());
                    selectedImageIndex = workingProfile.getImageIndex();
                }
                break;
            case NEW:
                nameField.setCurString("");
                nameField.setPosition(0);
                detailsField.setCurString("");
                detailsField.setPosition(0);
                selectedImageIndex = 0;
                break;
        }
    }

    private void updateBrowseDisplay() {
        if (browseOptions == null || browseIndex < 0 || browseIndex >= browseOptions.size()) return;
        EnumInterface profile = browseOptions.get(browseIndex);
        nameField.setCurString(profile.getDisplayName());
        nameField.setPosition(profile.getDisplayName().length());
        detailsField.setCurString(profile.getDisplayDetails());
        detailsField.setPosition(profile.getDisplayDetails().length());
    }

    private void enterEditModeFromBrowse() {
        if (browseOptions == null || browseIndex < 0 || browseIndex >= browseOptions.size()) return;
        EnumInterface profile = browseOptions.get(browseIndex);

        setCharacterEdit(profile);
        initCharacterMode();
        browseMode = false;
        scrollOffset = 0;
        setNeededButtonsVisible();
    }

    private int getCharacterTotalImages() {
        String category = getProfileCategory();
        CustomImageManager mgr = getMainPanel().getImageManagerFor(category);
        int builtIn = getBuiltInImageCount(category);
        return builtIn + (mgr != null ? mgr.getCount() : 0);
    }

    private int getBuiltInImageCount(String category) {
        int total = 0;
        for (TextureRegion[] arr : getTextureArraysFor(category)) total += arr.length;
        return total;
    }

    private TextureRegion[][] getTextureArraysFor(String category) {
        switch (category) {
            case "universe": return new TextureRegion[][]{AssetLoader.universeTextureRegion};
            case "places": return new TextureRegion[][]{AssetLoader.placesTextureRegion};
            case "objectives": return new TextureRegion[][]{AssetLoader.objectivesTextureRegion};
            default: return new TextureRegion[][]{AssetLoader.maincharacterTextureRegion, AssetLoader.supportCharacterTextureRegion};
        }
    }

    private TextureRegion getCharacterTextureByIndex(int index) {
        String category = getProfileCategory();
        TextureRegion[][] arrays = getTextureArraysFor(category);
        int offset = 0;
        for (TextureRegion[] arr : arrays) {
            if (index < offset + arr.length) return arr[index - offset];
            offset += arr.length;
        }
        CustomImageManager mgr = getMainPanel().getImageManagerFor(category);
        int customIdx = index - offset;
        if (mgr != null && customIdx >= 0 && customIdx < mgr.getCount()) {
            return mgr.getTexture(customIdx);
        }
        return arrays[0][0];
    }

    private int findBuiltInImageIndex(EnumInterface builtIn) {
        TextureRegion target = builtIn.getImage();
        String category = getProfileCategory();
        TextureRegion[][] arrays = getTextureArraysFor(category);
        int offset = 0;
        for (TextureRegion[] arr : arrays) {
            for (int i = 0; i < arr.length; i++) {
                if (arr[i] == target) return offset + i;
            }
            offset += arr.length;
        }
        return 0;
    }

    @Override
    public void setNeededButtonsVisible() {
        boolean isGridMode = currentMode == Mode.GRID;
        boolean isBrowse = browseMode;

        // X button only in grid mode (generate/manage have their own back button)
        getMainPanel().getButtonByFunction(FUNCTION_BACK).setVisible(isGridMode);
        getMainPanel().getButtonByFunction(FUNCTION_CONFIRM).setVisible(isGridMode);

        // Update confirm button text based on mode
        ApoButton confirmBtn = getMainPanel().getButtonByFunction(FUNCTION_CONFIRM);
        if (isBrowse) {
            confirmBtn.setId("custom_editor_select");
        } else {
            confirmBtn.setId("custom_editor_save");
        }

        boolean hasAI = hasOpenAI() || hasGemini();
        // AI button hidden in browse mode
        getMainPanel().getButtonByFunction(FUNCTION_AI_GENERATE).setVisible(isGridMode && hasAI && !isBrowse);

        // Manage only visible after entering generate/manage (not in grid)
        CustomImageManager catMgr = getMainPanel().getImageManagerFor(getProfileCategory());
        boolean hasCustomImages = catMgr != null && catMgr.getCount() > 0;
        getMainPanel().getButtonByFunction(FUNCTION_MANAGE).setVisible(currentMode != Mode.GRID && hasCustomImages);

        getMainPanel().getButtonByFunction(FUNCTION_BACK_TO_GRID).setVisible(currentMode != Mode.GRID);
        getMainPanel().getButtonByFunction(FUNCTION_DO_GENERATE).setVisible(currentMode == Mode.GENERATE && !isGenerating);

        com.apogames.entity.ApoButtonSwitch llmSwitch = (com.apogames.entity.ApoButtonSwitch) getMainPanel().getButtonByFunction(FUNCTION_LLM_SWITCH);
        boolean showLlm = currentMode == Mode.GENERATE && !isGenerating && (hasOpenAI() || hasGemini());
        llmSwitch.setVisible(showLlm);
        if (showLlm) {
            if (hasOpenAI() && hasGemini()) {
                llmSwitch.setLabels("GPT-5-mini", "Gemini-3");
                llmSwitch.setSelect(imageLlm != null && imageLlm.startsWith("gemini"));
            } else if (hasGemini()) {
                llmSwitch.setSingleLabel("Gemini-3");
            } else {
                llmSwitch.setSingleLabel("GPT-5-mini");
            }
        }

        // Pencil on preview — only visible in browse mode
        getMainPanel().getButtonByFunction(FUNCTION_EDIT_PENCIL).setVisible(isBrowse && isGridMode);

        // Profile buttons (all categories)
        getMainPanel().getButtonByFunction(FUNCTION_NEW_PROFILE).setVisible(isGridMode);
        // Reset: only in edit mode for built-in AND only if modified
        getMainPanel().getButtonByFunction(FUNCTION_RESET).setVisible(isGridMode && !isBrowse
                && characterEditMode == CharacterEditMode.BUILTIN && isBuiltInModified());
        getMainPanel().getButtonByFunction(FUNCTION_DELETE).setVisible(isGridMode && !isBrowse
                && characterEditMode == CharacterEditMode.PROFILE);
    }

    private boolean isBuiltInModified() {
        if (characterEditMode != CharacterEditMode.BUILTIN || editBuiltInName == null) return false;
        EnumInterface original = findOriginalBuiltIn(editBuiltInName);
        if (original == null) return false;
        String currentName = nameField != null ? nameField.getCurString().trim() : "";
        String currentDetails = detailsField != null ? detailsField.getCurString().trim() : "";
        return !currentName.equals(original.getDisplayName()) || !currentDetails.equals(original.getDisplayDetails());
    }

    private EnumInterface findOriginalBuiltIn(String name) {
        for (EnumInterface e : getEnumValuesForCategory()) {
            if (e.getName().equals(name)) return e;
        }
        return null;
    }

    private EnumInterface[] getEnumValuesForCategory() {
        String category = getProfileCategory();
        switch (category) {
            case "universe": return Universe.values();
            case "places": return Places.values();
            case "objectives": return Objectives.values();
            default: return MainCharacter.values();
        }
    }

    private boolean hasOpenAI() {
        return com.apogames.aistories.game.main.ChatGPTIO.API_KEY != null
                && !com.apogames.aistories.game.main.ChatGPTIO.API_KEY.isEmpty()
                && !com.apogames.aistories.game.main.ChatGPTIO.API_KEY.equals("Dein ChatGPT API Key");
    }

    private boolean hasGemini() {
        return com.apogames.aistories.game.main.ChatGPTIO.GEMINI_API_KEY != null
                && !com.apogames.aistories.game.main.ChatGPTIO.GEMINI_API_KEY.isEmpty()
                && !com.apogames.aistories.game.main.ChatGPTIO.GEMINI_API_KEY.equals("Dein Gemini API Key");
    }

    private String getCurrentLlm() {
        return getMainPanel().getChatGPT().getLlm();
    }

    @Override
    public void keyPressed(int keyCode, char character) {
        super.keyPressed(keyCode, character);
        if (keyCode >= 0 && keyCode < keys.length) {
            if (keys[keyCode]) return;
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
        if (currentMode == Mode.GENERATE) {
            this.imagePromptField.getMove(mouseX, mouseY);
        }
    }

    @Override
    public void mousePressed(int x, int y, boolean isRightButton) {
        if (isGenerating) return;
        boolean isBrowse = browseMode;

        // In browse mode, text fields are not editable
        if (!isBrowse) {
            if (this.nameField.mousePressed(x, y)) {
                this.nameField.setSelect(true);
                this.detailsField.setSelect(false);
                this.imagePromptField.setSelect(false);
                this.activeField = this.nameField;
                return;
            }
            if (this.detailsField.mousePressed(x, y)) {
                this.detailsField.setSelect(true);
                this.nameField.setSelect(false);
                this.imagePromptField.setSelect(false);
                this.activeField = this.detailsField;
                return;
            }
            if (currentMode == Mode.GENERATE && this.imagePromptField.mousePressed(x, y)) {
                this.imagePromptField.setSelect(true);
                this.nameField.setSelect(false);
                this.detailsField.setSelect(false);
                this.activeField = this.imagePromptField;
                return;
            }
        }

        if (currentMode == Mode.GRID) {
            int gridIndex = getGridIndexAt(x, y);
            if (isBrowse) {
                // Browse mode: select profile
                if (browseOptions != null && gridIndex >= 0 && gridIndex < browseOptions.size()) {
                    browseIndex = gridIndex;
                    updateBrowseDisplay();
                }
            } else {
                // Edit mode: select image
                if (gridIndex >= 0 && gridIndex < this.totalImages) {
                    this.selectedImageIndex = gridIndex;
                }
            }
        } else if (currentMode == Mode.GENERATE) {
            int styleIdx = getStyleIndexAt(x, y);
            if (styleIdx >= 0 && styleIdx < ImageStyle.values().length) {
                this.selectedStyleIndex = styleIdx;
            }
        } else if (currentMode == Mode.MANAGE) {
            handleManageClick(x, y);
        }

        this.nameField.setSelect(false);
        this.detailsField.setSelect(false);
        this.imagePromptField.setSelect(false);
        this.activeField = null;
        Gdx.input.setOnscreenKeyboardVisible(false);
        MainPanel.clearActiveInput();
    }

    @Override
    public void mouseButtonReleased(int mouseX, int mouseY, boolean isRightButton) {
        this.nameField.mouseReleased(mouseX, mouseY);
        this.detailsField.mouseReleased(mouseX, mouseY);
        if (currentMode == Mode.GENERATE) {
            this.imagePromptField.mouseReleased(mouseX, mouseY);
        }
    }

    @Override
    public void mouseDragged(int x, int y, boolean isRightButton) {
        if (this.activeField != null) {
            this.activeField.mouseDragged(x, y);
        }
    }

    @Override
    public void mouseWheelChanged(int amount) {
        if (currentMode == Mode.GRID || currentMode == Mode.MANAGE) {
            scrollOffset += amount * (GRID_SPRITE_SIZE + GRID_PADDING);
            clampScroll();
        }
    }

    private void clampScroll() {
        int rows;
        if (currentMode == Mode.MANAGE) {
            rows = (getCustomImageCount() + GRID_COLS - 1) / GRID_COLS;
        } else if (browseMode && browseOptions != null) {
            rows = (browseOptions.size() + GRID_COLS - 1) / GRID_COLS;
        } else {
            rows = (totalImages + GRID_COLS - 1) / GRID_COLS;
        }
        int totalHeight = rows * (GRID_SPRITE_SIZE + GRID_PADDING);
        int maxScroll = Math.max(0, totalHeight - GRID_VISIBLE_HEIGHT);
        if (scrollOffset < 0) scrollOffset = 0;
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    private int getCustomImageCount() {
        CustomImageManager mgr = getMainPanel().getImageManagerFor(getProfileCategory());
        return mgr != null ? mgr.getCount() : 0;
    }


    private int getGridIndexAt(int mouseX, int mouseY) {
        int relX = mouseX - GRID_START_X;
        int relY = mouseY - GRID_START_Y + scrollOffset;
        if (relX < 0 || relY < 0) return -1;
        if (mouseY < GRID_START_Y || mouseY > Constants.GAME_HEIGHT - 20) return -1;

        int col = relX / (GRID_SPRITE_SIZE + GRID_PADDING);
        int row = relY / (GRID_SPRITE_SIZE + GRID_PADDING);

        if (col >= GRID_COLS) return -1;
        if (col * (GRID_SPRITE_SIZE + GRID_PADDING) + GRID_SPRITE_SIZE < relX) return -1;
        if (row * (GRID_SPRITE_SIZE + GRID_PADDING) + GRID_SPRITE_SIZE < relY) return -1;

        int index = row * GRID_COLS + col;
        if (index >= this.totalImages) return -1;
        return index;
    }

    private int getStyleIndexAt(int mouseX, int mouseY) {
        int relX = mouseX - STYLE_START_X;
        int relY = mouseY - STYLE_START_Y;
        if (relX < 0 || relY < 0) return -1;

        int col = relX / (STYLE_BUTTON_W + STYLE_PADDING);
        int row = relY / (STYLE_BUTTON_H + STYLE_PADDING);

        if (col >= STYLE_COLS) return -1;
        if (col * (STYLE_BUTTON_W + STYLE_PADDING) + STYLE_BUTTON_W < relX) return -1;
        if (row * (STYLE_BUTTON_H + STYLE_PADDING) + STYLE_BUTTON_H < relY) return -1;

        int index = row * STYLE_COLS + col;
        if (index >= ImageStyle.values().length) return -1;
        return index;
    }

    private void handleManageClick(int mouseX, int mouseY) {
        String category = getProfileCategory();
        CustomImageManager mgr = getMainPanel().getImageManagerFor(category);
        int builtInCount = getBuiltInImageCount(category);
        if (mgr == null) return;

        int relX = mouseX - GRID_START_X;
        int relY = mouseY - GRID_START_Y + scrollOffset;
        if (relX < 0 || relY < 0) return;
        if (mouseY < GRID_START_Y || mouseY > Constants.GAME_HEIGHT - 20) return;

        int col = relX / (GRID_SPRITE_SIZE + GRID_PADDING);
        int row = relY / (GRID_SPRITE_SIZE + GRID_PADDING);
        if (col >= GRID_COLS) return;

        int index = row * GRID_COLS + col;
        int customCount = getCustomImageCount();
        if (index >= customCount) return;

        int spriteX = GRID_START_X + col * (GRID_SPRITE_SIZE + GRID_PADDING);
        int spriteY = GRID_START_Y + row * (GRID_SPRITE_SIZE + GRID_PADDING) - scrollOffset;
        int deleteX = spriteX + GRID_SPRITE_SIZE - 24;
        int deleteY = spriteY;

        if (mouseX >= deleteX && mouseX <= deleteX + 24 && mouseY >= deleteY && mouseY <= deleteY + 24) {
            mgr.deleteImage(index);
            totalImages = getCharacterTotalImages();

            if (selectedImageIndex >= builtInCount + index) {
                if (selectedImageIndex > builtInCount) {
                    selectedImageIndex--;
                } else {
                    selectedImageIndex = 0;
                }
            }
            if (selectedImageIndex >= totalImages && totalImages > 0) {
                selectedImageIndex = totalImages - 1;
            }

            if (getCustomImageCount() == 0) {
                currentMode = Mode.GRID;
                scrollOffset = 0;
            }
            setNeededButtonsVisible();
        }
    }

    private TextureRegion getTextureByIndex(int index) {
        return getCharacterTextureByIndex(index);
    }

    @Override
    public void mouseButtonFunction(String function) {
        if (isGenerating) return;

        super.mouseButtonFunction(function);
        switch (function) {
            case FUNCTION_BACK:
                quit();
                break;
            case FUNCTION_CONFIRM:
                confirm();
                break;
            case FUNCTION_AI_GENERATE:
                currentMode = Mode.GENERATE;
                scrollOffset = 0;
                generateError = null;
                setNeededButtonsVisible();
                break;
            case FUNCTION_MANAGE:
                currentMode = Mode.MANAGE;
                scrollOffset = 0;
                setNeededButtonsVisible();
                break;
            case FUNCTION_BACK_TO_GRID:
                currentMode = Mode.GRID;
                scrollOffset = 0;
                totalImages = getCharacterTotalImages();
                setNeededButtonsVisible();
                break;
            case FUNCTION_DO_GENERATE:
                doGenerate();
                break;
            case FUNCTION_LLM_SWITCH:
                ApoButton llmBtn = getMainPanel().getButtonByFunction(FUNCTION_LLM_SWITCH);
                llmBtn.setSelect(!llmBtn.isSelect());
                if (llmBtn.isSelect()) {
                    imageLlm = com.apogames.aistories.game.main.ChatGPTIO.LLM_MODEL_GEMINI;
                } else {
                    imageLlm = com.apogames.aistories.game.main.ChatGPTIO.LLM_MODEL_MINI;
                }
                break;
            case FUNCTION_NEW_PROFILE:
                switchToNewProfileMode();
                break;
            case FUNCTION_RESET:
                resetBuiltIn();
                break;
            case FUNCTION_DELETE:
                deleteProfile();
                break;
            case FUNCTION_EDIT_PENCIL:
                enterEditModeFromBrowse();
                break;
        }
    }

    private void doGenerate() {
        if (isGenerating) return;

        String name = this.nameField.getCurString().trim();

        String description = this.imagePromptField.getCurString().trim();
        if (description.isEmpty()) {
            description = this.detailsField.getCurString().trim();
        }

        ImageStyle style = ImageStyle.values()[selectedStyleIndex];
        String llm = this.imageLlm;

        final CustomImageManager mgr = getMainPanel().getImageManagerFor(getProfileCategory());

        if (mgr == null) return;

        isGenerating = true;
        generateError = null;
        setNeededButtonsVisible();

        ImageGenerationIO.generateImage(name, description, style, llm, new ImageGenerationIO.ImageCallback() {
            @Override
            public void onSuccess(byte[] pngData) {
                mgr.addImage(pngData);
                totalImages = getCharacterTotalImages();
                selectedImageIndex = totalImages - 1;
                isGenerating = false;
                currentMode = Mode.GRID;
                scrollOffset = 0;
                setNeededButtonsVisible();
            }

            @Override
            public void onError(String message) {
                generateError = message;
                isGenerating = false;
                setNeededButtonsVisible();
            }
        });
    }

    private void confirm() {
        if (browseMode) {
            confirmBrowse();
            return;
        }
        confirmCharacter();
    }

    private void confirmBrowse() {
        if (browseOptions != null && browseIndex >= 0 && browseIndex < browseOptions.size()) {
            getMainPanel().setPendingCharacterSelection(browseOptions.get(browseIndex));
        }
        goBack();
    }

    private void confirmCharacter() {
        MainPanel mp = getMainPanel();
        String category = getProfileCategory();
        CustomImageManager mgr = mp.getImageManagerFor(category);
        String name = nameField.getCurString().trim();
        String details = detailsField.getCurString().trim();
        EnumInterface savedProfile = null;

        switch (characterEditMode) {
            case BUILTIN: {
                CharacterProfile override = mp.getOverrideFor(category, editBuiltInName);
                if (override == null) {
                    override = new CharacterProfile();
                    override.setCategory(category);
                    override.setBuiltInName(editBuiltInName);
                    override.setCustomImageManager(mgr);
                }
                if (name.isEmpty() && editTarget != null) {
                    name = editTarget.getDisplayName();
                }
                override.setDisplayName(name);
                override.setDisplayDetails(details);
                override.setImageIndex(selectedImageIndex);
                mp.saveOverride(category, override);
                savedProfile = override;
                break;
            }
            case PROFILE: {
                if (workingProfile != null) {
                    if (name.isEmpty()) name = workingProfile.getDisplayName();
                    workingProfile.setDisplayName(name);
                    workingProfile.setDisplayDetails(details);
                    workingProfile.setImageIndex(selectedImageIndex);
                    mp.saveProfilesForCategory(category);
                    savedProfile = workingProfile;
                }
                break;
            }
            case NEW: {
                if (name.isEmpty()) return;
                CharacterProfile profile = new CharacterProfile();
                profile.setCategory(category);
                profile.setId(System.currentTimeMillis());
                profile.setDisplayName(name);
                profile.setDisplayDetails(details);
                profile.setImageIndex(selectedImageIndex);
                profile.setCustomImageManager(mgr);
                mp.addProfile(category, profile);
                savedProfile = profile;
                break;
            }
        }

        // Return to browse mode with the saved item selected
        if (savedProfile != null) {
            returnToBrowseWithSelection(savedProfile);
        } else {
            goBack();
        }
    }

    private void returnToBrowseWithSelection(EnumInterface savedProfile) {
        // Rebuild browse options with latest profiles
        EnumInterface[] enumValues = getEnumValuesForCategory();
        String category = getProfileCategory();
        this.browseOptions = new java.util.ArrayList<>(getMainPanel().buildEffectiveOptions(category, enumValues));
        this.totalImages = this.browseOptions.size();
        this.browseMode = true;
        this.browseIndex = 0;
        for (int i = 0; i < browseOptions.size(); i++) {
            if (browseOptions.get(i) == savedProfile || browseOptions.get(i).getName().equals(savedProfile.getName())) {
                this.browseIndex = i;
                break;
            }
        }
        updateBrowseDisplay();
        this.scrollOffset = 0;
        this.currentMode = Mode.GRID;
        setNeededButtonsVisible();
    }

    private void resetBuiltIn() {
        if (characterEditMode == CharacterEditMode.BUILTIN && editBuiltInName != null) {
            getMainPanel().removeOverride(getProfileCategory(), editBuiltInName);
        }
        // Find original built-in and return to browse
        EnumInterface original = findOriginalBuiltIn(editBuiltInName);
        if (original != null) {
            returnToBrowseWithSelection(original);
        } else {
            goBack();
        }
    }

    private void deleteProfile() {
        if (characterEditMode == CharacterEditMode.PROFILE && workingProfile != null) {
            getMainPanel().deleteProfile(getProfileCategory(), workingProfile.getId());
        }
        // Return to browse (first item since deleted item is gone)
        EnumInterface[] enumValues = getEnumValuesForCategory();
        String category = getProfileCategory();
        this.browseOptions = new java.util.ArrayList<>(getMainPanel().buildEffectiveOptions(category, enumValues));
        this.totalImages = this.browseOptions.size();
        this.browseMode = true;
        this.browseIndex = 0;
        updateBrowseDisplay();
        this.scrollOffset = 0;
        this.currentMode = Mode.GRID;
        setNeededButtonsVisible();
    }

    private void switchToNewProfileMode() {
        this.browseMode = false;
        this.characterEditMode = CharacterEditMode.NEW;
        this.workingProfile = null;
        this.editTarget = null;
        this.editBuiltInName = null;
        this.totalImages = getCharacterTotalImages();
        this.nameField.setCurString("");
        this.nameField.setPosition(0);
        this.detailsField.setCurString("");
        this.detailsField.setPosition(0);
        this.selectedImageIndex = 0;
        this.scrollOffset = 0;
        setNeededButtonsVisible();
    }

    @Override
    protected void quit() {
        goBack();
    }

    private void goBack() {
        this.nameField.setSelect(false);
        this.detailsField.setSelect(false);
        this.imagePromptField.setSelect(false);
        if (getMainPanel().isSongMode()) {
            getMainPanel().changeToCreateSong();
        } else {
            getMainPanel().changeToCreateStory();
        }
    }

    @Override
    public void doThink(float delta) {
        if (this.nameField != null) {
            this.nameField.think((int) delta);
        }
        if (this.detailsField != null) {
            this.detailsField.think((int) delta);
        }
        if (this.imagePromptField != null) {
            this.imagePromptField.think((int) delta);
        }
        // Dynamically update reset button visibility based on current field values
        if (!browseMode && characterEditMode == CharacterEditMode.BUILTIN && currentMode == Mode.GRID) {
            getMainPanel().getButtonByFunction(FUNCTION_RESET).setVisible(isBuiltInModified());
        }
    }

    @Override
    public void render() {
        getMainPanel().spriteBatch.begin();
        getMainPanel().spriteBatch.draw(AssetLoader.backgroundTextureRegion, 0, 0);
        getMainPanel().spriteBatch.end();

        // Background panels
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
        getMainPanel().getRenderer().setColor(0f, 0f, 0f, 0.3f);
        getMainPanel().getRenderer().roundedRect(LEFT_PANEL_X, PANEL_Y, LEFT_PANEL_W, Constants.GAME_HEIGHT - PANEL_Y - 10, PANEL_RADIUS);

        int rightPanelX = GRID_START_X - 15;
        int rightPanelW = Constants.GAME_WIDTH - rightPanelX - 10;
        int rightPanelH = Constants.GAME_HEIGHT - PANEL_Y - 10;
        getMainPanel().getRenderer().roundedRect(rightPanelX, PANEL_Y, rightPanelW, rightPanelH, PANEL_RADIUS);
        getMainPanel().getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        // Selection highlight (grid mode) — ShapeRenderer before SpriteBatch
        if (currentMode == Mode.GRID) {
            boolean isBrowse = browseMode;
            int selIdx = isBrowse ? browseIndex : selectedImageIndex;
            getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
            int selCol = selIdx % GRID_COLS;
            int selRow = selIdx / GRID_COLS;
            float selY = GRID_START_Y + selRow * (GRID_SPRITE_SIZE + GRID_PADDING) - scrollOffset;
            if (selY >= GRID_START_Y - GRID_SPRITE_SIZE && selY < Constants.GAME_HEIGHT) {
                getMainPanel().getRenderer().setColor(Constants.COLOR_YELLOW[0], Constants.COLOR_YELLOW[1], Constants.COLOR_YELLOW[2], 1f);
                getMainPanel().getRenderer().rect(
                        GRID_START_X + selCol * (GRID_SPRITE_SIZE + GRID_PADDING) - 3,
                        selY - 3,
                        GRID_SPRITE_SIZE + 6, GRID_SPRITE_SIZE + 6);
            }
            getMainPanel().getRenderer().end();
        }

        // Style button shapes (generate mode) — ShapeRenderer before SpriteBatch
        if (currentMode == Mode.GENERATE) {
            renderStyleButtonShapes();
        }

        // Textfields render (they manage their own ShapeRenderer + SpriteBatch)
        this.nameField.render(getMainPanel(), 0, 0);
        this.detailsField.render(getMainPanel(), 0, 0);
        if (currentMode == Mode.GENERATE) {
            this.imagePromptField.render(getMainPanel(), 0, 0);
        }

        // drawTitle starts SpriteBatch internally — batch must NOT be active
        getMainPanel().drawTitle(getEditorTitle(), Constants.COLOR_WHITE, false);

        // From here SpriteBatch is active (started by drawTitle)
        // Left panel labels
        getMainPanel().drawString(Localization.getInstance().getCommon().get("custom_editor_name"), 30, LABEL_Y, Constants.COLOR_WHITE, AssetLoader.font25, DrawString.BEGIN, false, false);
        getMainPanel().drawString(Localization.getInstance().getCommon().get("custom_editor_details"), 30, DETAILS_LABEL_Y, Constants.COLOR_WHITE, AssetLoader.font25, DrawString.BEGIN, false, false);

        // Preview image — larger
        boolean isBrowseRender = browseMode;
        TextureRegion previewImage;
        if (isBrowseRender && browseOptions != null && browseIndex >= 0 && browseIndex < browseOptions.size()) {
            previewImage = browseOptions.get(browseIndex).getImage();
        } else {
            previewImage = getTextureByIndex(this.selectedImageIndex);
        }
        if (previewImage != null) {
            float previewX = LEFT_PANEL_X + LEFT_PANEL_W / 2f - PREVIEW_SIZE / 2f;
            float previewY = 400;
            getMainPanel().spriteBatch.draw(previewImage, previewX, previewY, PREVIEW_SIZE, PREVIEW_SIZE);
        }

        String previewName = this.nameField.getCurString();
        if (!previewName.isEmpty()) {
            getMainPanel().drawString(previewName, LEFT_PANEL_X + LEFT_PANEL_W / 2f + 1, 400 + PREVIEW_SIZE + 20 + 1, Constants.COLOR_BLACK, AssetLoader.font25, DrawString.MIDDLE, true, false);
            getMainPanel().drawString(previewName, LEFT_PANEL_X + LEFT_PANEL_W / 2f, 400 + PREVIEW_SIZE + 20, Constants.COLOR_WHITE, AssetLoader.font25, DrawString.MIDDLE, true, false);
        }

        // Right panel content (mode-specific, SpriteBatch is active)
        switch (currentMode) {
            case GRID:
                renderGridMode();
                break;
            case GENERATE:
                renderGenerateMode();
                break;
            case MANAGE:
                renderManageMode();
                break;
        }

        getMainPanel().drawString("Version: " + Constants.VERSION, Constants.GAME_WIDTH / 2f, Constants.GAME_HEIGHT - 20f, Constants.COLOR_WHITE, AssetLoader.font15, DrawString.MIDDLE, false, false);

        getMainPanel().spriteBatch.end();

        for (ApoButton button : this.getMainPanel().getButtons()) {
            button.render(this.getMainPanel());
        }

        // Loading overlay (same style as ListenStories)
        if (isGenerating) {
            int overlayW = 600;
            Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
            getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
            getMainPanel().getRenderer().setColor(Constants.COLOR_WHITE[0], Constants.COLOR_WHITE[1], Constants.COLOR_WHITE[2], 0.8f);
            getMainPanel().getRenderer().roundedRect(Constants.GAME_WIDTH / 2f - overlayW / 2f, Constants.GAME_HEIGHT / 2f - 50, overlayW, 100, 10);
            getMainPanel().getRenderer().end();
            Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

            getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Line);
            getMainPanel().getRenderer().setColor(Constants.COLOR_BLACK[0], Constants.COLOR_BLACK[1], Constants.COLOR_BLACK[2], 1f);
            getMainPanel().getRenderer().roundedRectLine(Constants.GAME_WIDTH / 2f - overlayW / 2f, Constants.GAME_HEIGHT / 2f - 50, overlayW, 100, 10);
            getMainPanel().getRenderer().end();

            getMainPanel().spriteBatch.begin();
            getMainPanel().drawString(Localization.getInstance().getCommon().get("custom_editor_generating"),
                    Constants.GAME_WIDTH / 2f, Constants.GAME_HEIGHT / 2f - 20,
                    Constants.COLOR_BLACK, AssetLoader.font40, DrawString.MIDDLE, false, false);
            getMainPanel().spriteBatch.end();
        }
    }

    private void renderGridMode() {
        // Grid label — SpriteBatch is active
        getMainPanel().drawString(Localization.getInstance().getCommon().get("custom_editor_image"), GRID_START_X, LABEL_Y, Constants.COLOR_WHITE, AssetLoader.font25, DrawString.BEGIN, false, false);

        if (browseMode) {
            renderBrowseGrid();
        } else {
            renderGridImages(this.totalImages, false);
        }
    }

    private void renderBrowseGrid() {
        if (browseOptions == null) return;
        int count = browseOptions.size();
        for (int i = 0; i < count; i++) {
            int col = i % GRID_COLS;
            int row = i / GRID_COLS;
            float spriteX = GRID_START_X + col * (GRID_SPRITE_SIZE + GRID_PADDING);
            float spriteY = GRID_START_Y + row * (GRID_SPRITE_SIZE + GRID_PADDING) - scrollOffset;

            if (spriteY > Constants.GAME_HEIGHT || spriteY + GRID_SPRITE_SIZE < GRID_START_Y) continue;

            TextureRegion tex = browseOptions.get(i).getImage();
            if (tex != null) {
                getMainPanel().spriteBatch.draw(tex, spriteX, spriteY, GRID_SPRITE_SIZE, GRID_SPRITE_SIZE);
            }
        }
    }

    private void renderGridImages(int count, boolean isManageMode) {
        // SpriteBatch is active — draw all images
        for (int i = 0; i < count; i++) {
            int col = i % GRID_COLS;
            int row = i / GRID_COLS;
            float spriteX = GRID_START_X + col * (GRID_SPRITE_SIZE + GRID_PADDING);
            float spriteY = GRID_START_Y + row * (GRID_SPRITE_SIZE + GRID_PADDING) - scrollOffset;

            if (spriteY > Constants.GAME_HEIGHT || spriteY + GRID_SPRITE_SIZE < GRID_START_Y) continue;

            TextureRegion tex;
            if (isManageMode) {
                CustomImageManager mgr = getMainPanel().getImageManagerFor(getProfileCategory());
                tex = mgr != null ? mgr.getTexture(i) : null;
            } else {
                tex = getTextureByIndex(i);
            }
            if (tex != null) {
                getMainPanel().spriteBatch.draw(tex, spriteX, spriteY, GRID_SPRITE_SIZE, GRID_SPRITE_SIZE);
            }
        }

        // Delete buttons on manage mode — switch to ShapeRenderer
        if (isManageMode) {
            getMainPanel().spriteBatch.end();
            for (int i = 0; i < count; i++) {
                int col = i % GRID_COLS;
                int row = i / GRID_COLS;
                float spriteX = GRID_START_X + col * (GRID_SPRITE_SIZE + GRID_PADDING);
                float spriteY = GRID_START_Y + row * (GRID_SPRITE_SIZE + GRID_PADDING) - scrollOffset;

                if (spriteY > Constants.GAME_HEIGHT || spriteY + GRID_SPRITE_SIZE < GRID_START_Y) continue;

                getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
                getMainPanel().getRenderer().setColor(0.8f, 0f, 0f, 0.85f);
                getMainPanel().getRenderer().circle(spriteX + GRID_SPRITE_SIZE - 12, spriteY + 12, 12);
                getMainPanel().getRenderer().end();

                Gdx.gl20.glLineWidth(2f);
                getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Line);
                getMainPanel().getRenderer().setColor(1f, 1f, 1f, 1f);
                float cx = spriteX + GRID_SPRITE_SIZE - 12;
                float cy = spriteY + 12;
                getMainPanel().getRenderer().line(cx - 5, cy - 5, cx + 5, cy + 5);
                getMainPanel().getRenderer().line(cx - 5, cy + 5, cx + 5, cy - 5);
                getMainPanel().getRenderer().end();
                Gdx.gl20.glLineWidth(1f);
            }
            getMainPanel().spriteBatch.begin();
        }
    }

    private void renderStyleButtonShapes() {
        ImageStyle[] styles = ImageStyle.values();
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        for (int i = 0; i < styles.length; i++) {
            int col = i % STYLE_COLS;
            int row = i / STYLE_COLS;
            float bx = STYLE_START_X + col * (STYLE_BUTTON_W + STYLE_PADDING);
            float by = STYLE_START_Y + row * (STYLE_BUTTON_H + STYLE_PADDING);

            // Button background
            getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
            if (i == selectedStyleIndex) {
                getMainPanel().getRenderer().setColor(Constants.COLOR_YELLOW[0], Constants.COLOR_YELLOW[1], Constants.COLOR_YELLOW[2], 0.9f);
            } else {
                getMainPanel().getRenderer().setColor(0.3f, 0.3f, 0.3f, 0.7f);
            }
            getMainPanel().getRenderer().roundedRect(bx, by, STYLE_BUTTON_W, STYLE_BUTTON_H, 5);

            // Colored style preview square (left side)
            float iconX = bx + 8;
            float iconY = by + (STYLE_BUTTON_H - STYLE_ICON_SIZE) / 2f;
            getMainPanel().getRenderer().setColor(styles[i].getColorR(), styles[i].getColorG(), styles[i].getColorB(), 1f);
            getMainPanel().getRenderer().rect(iconX, iconY, STYLE_ICON_SIZE, STYLE_ICON_SIZE);
            getMainPanel().getRenderer().end();

            // Icon border + button border
            getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Line);
            getMainPanel().getRenderer().setColor(Constants.COLOR_WHITE[0], Constants.COLOR_WHITE[1], Constants.COLOR_WHITE[2], 1f);
            getMainPanel().getRenderer().roundedRectLine(bx, by, STYLE_BUTTON_W, STYLE_BUTTON_H, 5);
            getMainPanel().getRenderer().rect(iconX, iconY, STYLE_ICON_SIZE, STYLE_ICON_SIZE);
            getMainPanel().getRenderer().end();
        }
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);
    }

    private void renderGenerateMode() {
        // SpriteBatch is active here
        // Title
        getMainPanel().drawString(Localization.getInstance().getCommon().get("custom_editor_ai_title"),
                GRID_START_X, LABEL_Y, Constants.COLOR_WHITE, AssetLoader.font25, DrawString.BEGIN, false, false);

        // Style label
        getMainPanel().drawString(Localization.getInstance().getCommon().get("custom_editor_style"),
                STYLE_START_X, STYLE_START_Y - 25, Constants.COLOR_WHITE, AssetLoader.font20, DrawString.BEGIN, false, false);

        // Style button text (right of colored icon)
        ImageStyle[] styles = ImageStyle.values();
        for (int i = 0; i < styles.length; i++) {
            int col = i % STYLE_COLS;
            int row = i / STYLE_COLS;
            float bx = STYLE_START_X + col * (STYLE_BUTTON_W + STYLE_PADDING);
            float textX = bx + 8 + STYLE_ICON_SIZE + 10;
            float by = STYLE_START_Y + row * (STYLE_BUTTON_H + STYLE_PADDING) + STYLE_BUTTON_H / 2f - 8;

            String styleName = Localization.getInstance().getCommon().get(styles[i].getLocalizationKey());
            float[] textColor = (i == selectedStyleIndex) ? Constants.COLOR_BLACK : Constants.COLOR_WHITE;
            getMainPanel().drawString(styleName, textX, by, textColor, AssetLoader.font20, DrawString.BEGIN, false, false);
        }

        // Image prompt label
        getMainPanel().drawString(Localization.getInstance().getCommon().get("custom_editor_image_prompt"),
                STYLE_START_X, 375, Constants.COLOR_WHITE, AssetLoader.font20, DrawString.BEGIN, false, false);

        // Error message
        if (generateError != null) {
            getMainPanel().drawString(generateError,
                    GRID_START_X + (Constants.GAME_WIDTH - GRID_START_X) / 2f, Constants.GAME_HEIGHT - 120,
                    Constants.COLOR_RED, AssetLoader.font15, DrawString.MIDDLE, true, false);
        }
    }

    private void renderManageMode() {
        getMainPanel().drawString(Localization.getInstance().getCommon().get("custom_editor_manage_title"),
                GRID_START_X, LABEL_Y, Constants.COLOR_WHITE, AssetLoader.font25, DrawString.BEGIN, false, false);

        int customCount = getCustomImageCount();
        if (customCount == 0) {
            getMainPanel().drawString(Localization.getInstance().getCommon().get("custom_editor_no_custom_images"),
                    GRID_START_X + (Constants.GAME_WIDTH - GRID_START_X) / 2f, 300,
                    Constants.COLOR_GREY_BRIGHT, AssetLoader.font20, DrawString.MIDDLE, true, false);
        } else {
            renderGridImages(customCount, true);
        }
    }

    private String getEditorTitle() {
        if (browseMode) {
            if (browseCategory != null) {
                String key = "custom_editor_title_browse_" + browseCategory;
                try {
                    return Localization.getInstance().getCommon().get(key);
                } catch (Exception e) { }
            }
            return Localization.getInstance().getCommon().get("custom_editor_title_browse");
        }
        {
            String key;
            switch (characterEditMode) {
                case BUILTIN: key = "custom_editor_title_builtin"; break;
                case PROFILE: key = "custom_editor_title_profile"; break;
                case NEW: key = "custom_editor_title_new"; break;
                default: key = "custom_editor_title"; break;
            }
            try {
                return Localization.getInstance().getCommon().get(key);
            } catch (Exception e) {
                // fallback
            }
        }
        return Localization.getInstance().getCommon().get("custom_editor_title");
    }

    @Override
    public void dispose() {
        this.nameField.setSelect(false);
        this.detailsField.setSelect(false);
        this.imagePromptField.setSelect(false);
    }
}
