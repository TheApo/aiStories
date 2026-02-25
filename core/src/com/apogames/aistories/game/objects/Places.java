package com.apogames.aistories.game.objects;

import com.apogames.asset.AssetLoader;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lombok.Getter;

@Getter
public enum Places implements EnumInterface {
    WHALESTOMACH("places_whale_stomach", "places_whale_stomach_details", AssetLoader.placesTextureRegion[0]),
    CASTLE("places_castle", "places_castle_details", AssetLoader.placesTextureRegion[1]),
    HOTAIRBALLOON("places_hotairballoon", "places_hotairballoon_details", AssetLoader.placesTextureRegion[2]),
    PINEAPPLE("places_pineapple", "places_pineapple_details", AssetLoader.placesTextureRegion[3]),
    PLANET("places_planet", "places_planet_details", AssetLoader.placesTextureRegion[4]),
    SHELL("places_shell", "places_shell_details", AssetLoader.placesTextureRegion[5]),
    AIRPLANE("places_airplane", "places_airplane_details", AssetLoader.placesTextureRegion[6]),
    SPACESHIP("places_spaceship", "places_spaceship_details", AssetLoader.placesTextureRegion[7]),
    CUP("places_cup", "places_cup_details", AssetLoader.placesTextureRegion[8]),
    HAMMOCK("places_hammock", "places_hammock_details", AssetLoader.placesTextureRegion[9]),
    CAR("places_car", "places_car_details", AssetLoader.placesTextureRegion[10]),
    CAVE("places_cave", "places_cave_details", AssetLoader.placesTextureRegion[11]),
    FOUNTAIN("places_fountain", "places_fountain_details", AssetLoader.placesTextureRegion[12]),
    MOUSEHOLE("places_mousehole", "places_mousehole_details", AssetLoader.placesTextureRegion[13]),
    SUBMARINE("places_submarine", "places_submarine_details", AssetLoader.placesTextureRegion[14]),
    BOOTS("places_boots", "places_boots_details", AssetLoader.placesTextureRegion[15]),
    PIRATESHIP("places_pirateship", "places_pirateship_details", AssetLoader.placesTextureRegion[16]),
    TREEHOUSE("places_treehouse", "places_treehouse_details", AssetLoader.placesTextureRegion[17]),
    CHEESECAVE("places_cheesecave", "places_cheesecave_details", AssetLoader.placesTextureRegion[18]),
    TRAPDOOR("places_trapdoor", "places_trapdoor_details", AssetLoader.placesTextureRegion[19]),
    LIBRARY("places_library", "places_library_details", AssetLoader.placesTextureRegion[20]),
    RAINBOWBRIDGE("places_rainbowbridge", "places_rainbowbridge_details", AssetLoader.placesTextureRegion[21]),
    TRAIN("places_train", "places_train_details", AssetLoader.placesTextureRegion[22]),
    CAMPFIRE("places_campfire", "places_campfire_details", AssetLoader.placesTextureRegion[23]),
    TENT("places_tent", "places_tent_details", AssetLoader.placesTextureRegion[24]),
    GEMSTONE("places_gemstone", "places_gemstone_details", AssetLoader.placesTextureRegion[25]),
    LIGHTHOUSE("places_lighthouse", "places_lighthouse_details", AssetLoader.placesTextureRegion[26]),
    SWEETS("places_sweets", "places_sweets_details", AssetLoader.placesTextureRegion[27]),
    WATERFALL("places_waterfall", "places_waterfall_details", AssetLoader.placesTextureRegion[28]),
    SANDCASTLE("places_sandcastle", "places_sandcastle_details", AssetLoader.placesTextureRegion[29]);

    private final String name;
    private final String details;
    private TextureRegion image;

    Places(final String name, final String details) {
        this(name, details, null);
    }

    Places(final String name, final String details, TextureRegion image) {
        this.name = name;
        this.details = details;
        this.image = image;
    }

    @Override
    public String getEnumName() {
        return "places";
    }

    public static EnumInterface getRandom() {
        return Places.values()[(int)(Places.values().length * Math.random())];
    }

    @Override
    public EnumInterface getNext(int add) {
        int index = 0;
        for (Places places : values()) {
            if (places.getName().equals(this.getName())) {
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
        for (Places places : values()) {
            if (places.getName().equals(name)) {
                return places;
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
            Places places = values()[i];
            places.image = AssetLoader.placesTextureRegion[i];
        }
    }
}
