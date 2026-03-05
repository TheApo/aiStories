package com.apogames.aistories.game.objects;

import com.apogames.aistories.game.customEntity.CustomImageManager;
import com.apogames.asset.AssetLoader;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomEntity implements EnumInterface {

    private String customName;
    private String customDetails;
    private int imageIndex;
    private final String enumName;
    private CustomImageManager customImageManager;

    public CustomEntity(String enumName) {
        this.enumName = enumName;
        this.customName = "Eigener Charakter";
        this.customDetails = "Was macht ihn besonders? Wo kommt er her?";
        this.imageIndex = 0;
    }

    @Override
    public String getEnumName() {
        return this.enumName;
    }

    @Override
    public String getName() {
        return this.enumName + "_custom";
    }

    @Override
    public String getDetails() {
        return this.customDetails;
    }

    public TextureRegion[][] getTextureArrays() {
        switch (this.enumName) {
            case "universe":
                return new TextureRegion[][]{AssetLoader.universeTextureRegion};
            case "places":
                return new TextureRegion[][]{AssetLoader.placesTextureRegion};
            case "objectives":
                return new TextureRegion[][]{AssetLoader.objectivesTextureRegion};
            default:
                return new TextureRegion[][]{AssetLoader.maincharacterTextureRegion, AssetLoader.supportCharacterTextureRegion};
        }
    }

    public int getBuiltInImageCount() {
        int total = 0;
        for (TextureRegion[] arr : getTextureArrays()) {
            total += arr.length;
        }
        return total;
    }

    public int getCustomImageCount() {
        return customImageManager != null ? customImageManager.getCount() : 0;
    }

    public int getTotalImages() {
        return getBuiltInImageCount() + getCustomImageCount();
    }

    public TextureRegion getTextureByIndex(int index) {
        int builtIn = getBuiltInImageCount();
        if (index < builtIn) {
            for (TextureRegion[] arr : getTextureArrays()) {
                if (index < arr.length) {
                    return arr[index];
                }
                index -= arr.length;
            }
            return getTextureArrays()[0][0];
        }
        int customIdx = index - builtIn;
        if (customImageManager != null && customIdx < customImageManager.getCount()) {
            return customImageManager.getTexture(customIdx);
        }
        return getTextureArrays()[0][0];
    }

    public boolean isCustomImage(int index) {
        return index >= getBuiltInImageCount();
    }

    @Override
    public TextureRegion getImage() {
        return getTextureByIndex(this.imageIndex);
    }

    @Override
    public String getDisplayName() {
        return this.customName;
    }

    @Override
    public String getDisplayDetails() {
        return this.customDetails;
    }

    @Override
    public EnumInterface getNext(int add) {
        return this;
    }

    @Override
    public EnumInterface getEnumByName(String name) {
        if (getName().equals(name)) {
            return this;
        }
        switch (this.enumName) {
            case "mainCharacter":
                for (MainCharacter c : MainCharacter.values()) {
                    if (c.getName().equals(name)) return c;
                }
                break;
            case "supportingCharacter":
                for (SupportingCharacter c : SupportingCharacter.values()) {
                    if (c.getName().equals(name)) return c;
                }
                break;
            case "universe":
                for (Universe c : Universe.values()) {
                    if (c.getName().equals(name)) return c;
                }
                break;
            case "places":
                for (Places c : Places.values()) {
                    if (c.getName().equals(name)) return c;
                }
                break;
            case "objectives":
                for (Objectives c : Objectives.values()) {
                    if (c.getName().equals(name)) return c;
                }
                break;
        }
        return this;
    }

    @Override
    public void refresh() {
    }
}
