package com.apogames.aistories.game.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameObjectives {

    private EnumInterface mainCharacter;
    private EnumInterface supportingCharacter;
    private EnumInterface objectives;
    private EnumInterface places;
    private EnumInterface universe;

    public GameObjectives() {
        this.shuffle();
    }

    public void shuffle() {
        this.mainCharacter = MainCharacter.getRandom();
        this.supportingCharacter = SupportingCharacter.getRandom();
        this.objectives = Objectives.getRandom();
        this.places = Places.getRandom();
        this.universe = Universe.getRandom();
    }

    public void shuffleWithCustoms(CustomEntity customMain, CustomEntity customSupport,
                                    CustomEntity customUniverse, CustomEntity customPlaces,
                                    CustomEntity customObjectives) {
        this.mainCharacter = pickRandom(MainCharacter.values(), customMain);
        this.supportingCharacter = pickRandom(SupportingCharacter.values(), customSupport);
        this.universe = pickRandom(Universe.values(), customUniverse);
        this.places = pickRandom(Places.values(), customPlaces);
        this.objectives = pickRandom(Objectives.values(), customObjectives);
    }

    private static EnumInterface pickRandom(EnumInterface[] values, CustomEntity custom) {
        int total = values.length + (custom != null ? 1 : 0);
        int index = (int) (total * Math.random());
        return index < values.length ? values[index] : custom;
    }

    public void refresh() {
        if (this.mainCharacter != null) this.mainCharacter.refresh();
        if (this.supportingCharacter != null) this.supportingCharacter.refresh();
        if (this.objectives != null) this.objectives.refresh();
        if (this.places != null) this.places.refresh();
        if (this.universe != null) this.universe.refresh();
    }
}
