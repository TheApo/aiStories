package com.apogames.aistories.game.objects;

import com.apogames.asset.AssetLoader;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lombok.Getter;

@Getter
public enum Objectives implements EnumInterface {
    BOOK("objectives_book", "objectives_book_details", AssetLoader.objectivesTextureRegion[0]),
    MAGICBEAN("objectives_magicbean", "objectives_magicbean_details", AssetLoader.objectivesTextureRegion[1]),
    BICYLE("objectives_bicyle", "objectives_bicyle_details", AssetLoader.objectivesTextureRegion[2]),
    MAGICPOTION("objectives_magicpotion", "objectives_magicpotion_details", AssetLoader.objectivesTextureRegion[3]),
    CAKE("objectives_cake", "objectives_cake_details", AssetLoader.objectivesTextureRegion[4]),
    SWEETS("objectives_sweets", "objectives_sweets_details", AssetLoader.objectivesTextureRegion[5]),
    SMARTPHONE("objectives_smartphone", "objectives_smartphone_details", AssetLoader.objectivesTextureRegion[6]),
    MESSAGEINABOTTLE("objectives_message_in_a_bottle", "objectives_message_in_a_bottle_details", AssetLoader.objectivesTextureRegion[7]),
    MAGICSTICK("objectives_magicstick", "objectives_magicstick_details", AssetLoader.objectivesTextureRegion[8]),
    APPLE("objectives_apple", "objectives_apple_details", AssetLoader.objectivesTextureRegion[9]),
    CUDDLYTOY("objectives_cuddlytoy", "objectives_cuddlytoy_details", AssetLoader.objectivesTextureRegion[10]),
    BROOM("objectives_broom", "objectives_broom_details", AssetLoader.objectivesTextureRegion[11]),
    TEAPOT("objectives_teapot", "objectives_teapot_details", AssetLoader.objectivesTextureRegion[12]),
    SCARECROW("objectives_scarecrow", "objectives_scarecrow_details", AssetLoader.objectivesTextureRegion[13]),
    CROWN("objectives_crown", "objectives_crown_details", AssetLoader.objectivesTextureRegion[14]),
    BALLOON("objectives_balloon", "objectives_balloon_details", AssetLoader.objectivesTextureRegion[15]),
    FLOWER("objectives_flower", "objectives_flower_details", AssetLoader.objectivesTextureRegion[16]),
    MAGICBALL("objectives_magicball", "objectives_magicball_details", AssetLoader.objectivesTextureRegion[17]),
    TREASURE("objectives_treasure", "objectives_treasure_details", AssetLoader.objectivesTextureRegion[18]),
    BAG("objectives_bag", "objectives_bag_details", AssetLoader.objectivesTextureRegion[19]),
    FEATHER("objectives_feather", "objectives_feather_details", AssetLoader.objectivesTextureRegion[20]),
    CLOCK("objectives_clock", "objectives_clock_details", AssetLoader.objectivesTextureRegion[21]),
    CAPE("objectives_cape", "objectives_cape_details", AssetLoader.objectivesTextureRegion[22]),
    STAR("objectives_star", "objectives_star_details", AssetLoader.objectivesTextureRegion[23]),
    SHELL("objectives_shell", "objectives_shell_details", AssetLoader.objectivesTextureRegion[24]),
    DROP("objectives_drop", "objectives_drop_details", AssetLoader.objectivesTextureRegion[25]),
    ROUNDABOUT("objectives_roundabout", "objectives_roundabout_details", AssetLoader.objectivesTextureRegion[26]),
    KEY("objectives_key", "objectives_key_details", AssetLoader.objectivesTextureRegion[27]),
    PICTURE("objectives_picture", "objectives_picture_details", AssetLoader.objectivesTextureRegion[28]),
    COMPASS("objectives_compass", "objectives_compass_details", AssetLoader.objectivesTextureRegion[29]);

    private final String name;
    private final String details;
    private TextureRegion image;

    Objectives(final String name, final String details) {
        this(name, details, null);
    }

    Objectives(final String name, final String details, final TextureRegion image) {
        this.name = name;
        this.details = details;
        this.image = image;
    }

    @Override
    public String getEnumName() {
        return "objectives";
    }

    public static EnumInterface getRandom() {
        return Objectives.values()[(int)(Objectives.values().length * Math.random())];
    }

    @Override
    public EnumInterface getNext(int add) {
        int index = 0;
        for (Objectives objectives : values()) {
            if (objectives.getName().equals(this.getName())) {
                if (index + add >= values().length) {
                    return values()[index + add - values().length];
                } else if (index + add < 0) {
                    return values()[index + add + values().length];
                } else {
                    return values()[index + add];
                }
            }
            index += 1;
        }
        return this;
    }

    @Override
    public EnumInterface getEnumByName(String name) {
        for (Objectives objectives : values()) {
            if (objectives.getName().equals(name)) {
                return objectives;
            }
        }
        return this;
    }

    @Override
    public void refresh() {
        refreshAll();
    }

    @Override
    public TextureRegion getImage() {
        return this.image;
    }

    public static void refreshAll() {
        for (int i = 0; i < values().length; i++) {
            Objectives objectives = values()[i];
            objectives.image = AssetLoader.objectivesTextureRegion[i];
        }
    }
}
