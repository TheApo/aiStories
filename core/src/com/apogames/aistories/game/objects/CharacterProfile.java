package com.apogames.aistories.game.objects;

import com.apogames.aistories.game.customEntity.CustomImageManager;
import com.apogames.asset.AssetLoader;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class CharacterProfile implements EnumInterface {

    private String builtInName;
    private long id;
    private String displayName;
    private String displayDetails;
    private int imageIndex;
    private String category = "characters";
    private CustomImageManager customImageManager;

    public boolean isBuiltInOverride() {
        return builtInName != null;
    }

    public String getBuiltInName() { return builtInName; }
    public void setBuiltInName(String builtInName) { this.builtInName = builtInName; }
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    @Override public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    @Override public String getDisplayDetails() { return displayDetails; }
    public void setDisplayDetails(String displayDetails) { this.displayDetails = displayDetails; }
    public int getImageIndex() { return imageIndex; }
    public void setImageIndex(int imageIndex) { this.imageIndex = imageIndex; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public CustomImageManager getCustomImageManager() { return customImageManager; }
    public void setCustomImageManager(CustomImageManager mgr) { this.customImageManager = mgr; }

    @Override
    public String getEnumName() {
        if ("characters".equals(category)) return "mainCharacter";
        return category;
    }

    @Override
    public String getName() {
        return builtInName != null ? builtInName : "profile_" + id;
    }

    @Override
    public String getDetails() {
        return displayDetails;
    }

    @Override
    public TextureRegion getImage() {
        return resolveTexture(imageIndex);
    }

    @Override
    public EnumInterface getNext(int add) {
        return this;
    }

    @Override
    public EnumInterface getEnumByName(String name) {
        if (getName().equals(name)) return this;
        return this;
    }

    @Override
    public void refresh() {}

    public TextureRegion resolveTexture(int index) {
        TextureRegion[][] arrays = getTextureArrays();
        int offset = 0;
        for (TextureRegion[] arr : arrays) {
            if (index < offset + arr.length) return arr[index - offset];
            offset += arr.length;
        }
        int customIdx = index - offset;
        if (customImageManager != null && customIdx >= 0 && customIdx < customImageManager.getCount()) {
            return customImageManager.getTexture(customIdx);
        }
        return arrays.length > 0 && arrays[0].length > 0 ? arrays[0][0] : null;
    }

    private TextureRegion[][] getTextureArrays() {
        switch (category) {
            case "universe": return new TextureRegion[][]{AssetLoader.universeTextureRegion};
            case "places": return new TextureRegion[][]{AssetLoader.placesTextureRegion};
            case "objectives": return new TextureRegion[][]{AssetLoader.objectivesTextureRegion};
            default: return new TextureRegion[][]{AssetLoader.maincharacterTextureRegion, AssetLoader.supportCharacterTextureRegion};
        }
    }

    public int getBuiltInImageCount() {
        int total = 0;
        for (TextureRegion[] arr : getTextureArrays()) total += arr.length;
        return total;
    }

    public int getTotalImages() {
        int total = getBuiltInImageCount();
        if (customImageManager != null) total += customImageManager.getCount();
        return total;
    }
}
