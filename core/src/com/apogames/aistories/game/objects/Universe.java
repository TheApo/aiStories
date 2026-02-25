package com.apogames.aistories.game.objects;

import com.apogames.asset.AssetLoader;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lombok.Getter;

@Getter
public enum Universe implements EnumInterface {
    PLAYGROUND("universe_playground", "universe_playground_details", AssetLoader.universeTextureRegion[0]),
    FORREST("universe_forrest", "universe_forrest_details", AssetLoader.universeTextureRegion[1]),
    WATERFALL("universe_waterfall", "universe_waterfall_details", AssetLoader.universeTextureRegion[2]),
    VILLAGE("universe_village", "universe_village_details", AssetLoader.universeTextureRegion[3]),
    CIRCUS("universe_circus", "universe_circus_details", AssetLoader.universeTextureRegion[4]),
    SPACE("universe_space", "universe_space_details", AssetLoader.universeTextureRegion[5]),
    MOUNTAINS("universe_mountains", "universe_mountains_details", AssetLoader.universeTextureRegion[6]),
    DESERT("universe_desert", "universe_desert_details", AssetLoader.universeTextureRegion[7]),
    JUNGLE("universe_jungle", "universe_jungle_details", AssetLoader.universeTextureRegion[8]),
    LEGO("universe_lego", "universe_lego_details", AssetLoader.universeTextureRegion[9]),
    KNIGHTWORLD("universe_knightworld", "universe_knightworld_details", AssetLoader.universeTextureRegion[10]),
    SHIP("universe_ship", "universe_ship_details", AssetLoader.universeTextureRegion[11]),
    THUNDERSTORM("universe_thunderstorm", "universe_thunderstorm_details", AssetLoader.universeTextureRegion[12]),
    ISLE("universe_isle", "universe_isle_details", AssetLoader.universeTextureRegion[13]),
    POOL("universe_pool", "universe_pool_details", AssetLoader.universeTextureRegion[14]),
    SNOW("universe_snow", "universe_snow_details", AssetLoader.universeTextureRegion[15]),
    FARMYARD("universe_farmyard", "universe_farmyard_details", AssetLoader.universeTextureRegion[16]),
    ZOO("universe_zoo", "universe_zoo_details", AssetLoader.universeTextureRegion[17]),
    UNDERWATER("universe_underwater", "universe_underwater_details", AssetLoader.universeTextureRegion[18]),
    BOOK("universe_book", "universe_book_details", AssetLoader.universeTextureRegion[19]),
    MUSIC("universe_music", "universe_music_details", AssetLoader.universeTextureRegion[20]),
    CUDDLYTOYS("universe_cuddlytoys", "universe_cuddlytoys_details", AssetLoader.universeTextureRegion[21]),
    CHILDROOM("universe_childroom", "universe_childroom_details", AssetLoader.universeTextureRegion[22]),
    FLYINGCARPET("universe_flyingcarpet", "universe_flyingcarpet_details", AssetLoader.universeTextureRegion[23]),
    RAINBOW("universe_rainbow", "universe_rainbow_details", AssetLoader.universeTextureRegion[24]),
    CLOCK("universe_clock", "universe_clock_details", AssetLoader.universeTextureRegion[25]),
    BED("universe_bed", "universe_bed_details", AssetLoader.universeTextureRegion[26]),
    FAIR("universe_fair", "universe_fair_details", AssetLoader.universeTextureRegion[27]),
    WHALE("universe_whale", "universe_whale_details", AssetLoader.universeTextureRegion[28]),
    SHELF("universe_shelf", "universe_shelf_details", AssetLoader.universeTextureRegion[29]);

    private final String name;
    private final String details;
    private TextureRegion image;

    Universe(final String name, final String details) {
        this(name, details, null);
    }

    Universe(final String name, final String details, TextureRegion image) {
        this.name = name;
        this.details = details;
        this.image = image;
    }

    @Override
    public String getEnumName() {
        return "universe";
    }

    public static EnumInterface getRandom() {
        return Universe.values()[(int)(Universe.values().length * Math.random())];
    }

    @Override
    public EnumInterface getNext(int add) {
        int index = 0;
        for (Universe universe : values()) {
            if (universe.getName().equals(this.getName())) {
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
        for (Universe universe : values()) {
            if (universe.getName().equals(name)) {
                return universe;
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
            Universe universe = values()[i];
            universe.image = AssetLoader.universeTextureRegion[i];
        }
    }
}
