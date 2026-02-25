package com.apogames.aistories.game.objects;

import com.apogames.asset.AssetLoader;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import lombok.Getter;

@Getter
public enum SupportingCharacter implements EnumInterface {
    APE("supportingCharacter_ape", "supportingCharacter_ape_details", AssetLoader.supportCharacterTextureRegion[0]),
    ELEPHANT("supportingCharacter_elephant", "supportingCharacter_elephant_details", AssetLoader.supportCharacterTextureRegion[1]),
    GOOSE("supportingCharacter_goose", "supportingCharacter_goose_details", AssetLoader.supportCharacterTextureRegion[2]),
    DOG("supportingCharacter_dog", "supportingCharacter_dog_details", AssetLoader.supportCharacterTextureRegion[3]),
    BIRD("supportingCharacter_bird", "supportingCharacter_bird_details", AssetLoader.supportCharacterTextureRegion[4]),
    CLOWN("supportingCharacter_clown", "supportingCharacter_clown_details", AssetLoader.supportCharacterTextureRegion[5]),
    GIANT("supportingCharacter_giant", "supportingCharacter_giant_details", AssetLoader.supportCharacterTextureRegion[6]),
    FROG("supportingCharacter_frog", "supportingCharacter_frog_details", AssetLoader.supportCharacterTextureRegion[7]),
    GRANDMA("supportingCharacter_grandma", "supportingCharacter_grandma_details", AssetLoader.supportCharacterTextureRegion[8]),
    CRIMINAL("supportingCharacter_criminal", "supportingCharacter_criminal_details", AssetLoader.supportCharacterTextureRegion[9]),
    BUTTERFLY("supportingCharacter_butterfly", "supportingCharacter_butterfly_details", AssetLoader.supportCharacterTextureRegion[10]),
    LADYBUG("supportingCharacter_ladybug", "supportingCharacter_ladybug_details", AssetLoader.supportCharacterTextureRegion[11]),
    FIREFIGHTER("supportingCharacter_firefighter", "supportingCharacter_firefighter_details", AssetLoader.supportCharacterTextureRegion[12]),
    POLICE("supportingCharacter_police", "supportingCharacter_police_details", AssetLoader.supportCharacterTextureRegion[13]),
    BAT("supportingCharacter_bat", "supportingCharacter_bat_details", AssetLoader.supportCharacterTextureRegion[14]),
    GRANDPA("supportingCharacter_grandpa", "supportingCharacter_grandpa_details", AssetLoader.supportCharacterTextureRegion[15]),
    MOLE("supportingCharacter_mole", "supportingCharacter_mole_details", AssetLoader.supportCharacterTextureRegion[16]),
    CAT("supportingCharacter_cat", "supportingCharacter_cat_details", AssetLoader.supportCharacterTextureRegion[17]),
    WORKER("supportingCharacter_worker", "supportingCharacter_worker_details", AssetLoader.supportCharacterTextureRegion[18]),
    EARTHWORM("supportingCharacter_earthworm", "supportingCharacter_earthworm_details", AssetLoader.supportCharacterTextureRegion[19]),
    SQUIRREL("supportingCharacter_squirrel", "supportingCharacter_squirrel_details", AssetLoader.supportCharacterTextureRegion[20]),
    FISH("supportingCharacter_fish", "supportingCharacter_fish_details", AssetLoader.supportCharacterTextureRegion[21]),
    DINOSAUR("supportingCharacter_dinosaur", "supportingCharacter_dinosaur_details", AssetLoader.supportCharacterTextureRegion[22]),
    ROBOT("supportingCharacter_robot", "supportingCharacter_robot_details", AssetLoader.supportCharacterTextureRegion[23]),
    GOBLIN("supportingCharacter_goblin", "supportingCharacter_goblin_details", AssetLoader.supportCharacterTextureRegion[24]),
    CLOCK("supportingCharacter_clock", "supportingCharacter_clock_details", AssetLoader.supportCharacterTextureRegion[25]),
    FLYINGBAT("supportingCharacter_flyingbat", "supportingCharacter_flyingbat_details", AssetLoader.supportCharacterTextureRegion[26]),
    UNICORN("supportingCharacter_unicorn", "supportingCharacter_unicorn_details", AssetLoader.supportCharacterTextureRegion[27]),
    PARROT("supportingCharacter_parrot", "supportingCharacter_parrot_details", AssetLoader.supportCharacterTextureRegion[28]),
    MIRROR("supportingCharacter_mirror", "supportingCharacter_mirror_details", AssetLoader.supportCharacterTextureRegion[29]),
    LION("mainCharacter_lion", "mainCharacter_lion_details", AssetLoader.maincharacterTextureRegion[0]),
    OWL("mainCharacter_owl", "mainCharacter_owl_details", AssetLoader.maincharacterTextureRegion[1]),
    KING("mainCharacter_king", "mainCharacter_king_details", AssetLoader.maincharacterTextureRegion[2]),
    PRINCE("mainCharacter_prince", "mainCharacter_prince_details", AssetLoader.maincharacterTextureRegion[3]),
    PRINCESS("mainCharacter_princess", "mainCharacter_princess_details", AssetLoader.maincharacterTextureRegion[4]),
    KNIGHT("mainCharacter_knight", "mainCharacter_knight_details", AssetLoader.maincharacterTextureRegion[5]),
    MONSTER("mainCharacter_monster", "mainCharacter_monster_details", AssetLoader.maincharacterTextureRegion[6]),
    HERO_FEMALE("mainCharacter_hero_female", "mainCharacter_hero_female_details", AssetLoader.maincharacterTextureRegion[7]),
    HERO("mainCharacter_hero", "mainCharacter_hero_details", AssetLoader.maincharacterTextureRegion[8]),
    WIZARD("mainCharacter_wizard", "mainCharacter_wizard_details", AssetLoader.maincharacterTextureRegion[9]),
    WITCH("mainCharacter_witch", "mainCharacter_witch_details", AssetLoader.maincharacterTextureRegion[10]),
    ALIEN("mainCharacter_alien", "mainCharacter_alien_details", AssetLoader.maincharacterTextureRegion[11]),
    WOLF("mainCharacter_wolf", "mainCharacter_wolf_details", AssetLoader.maincharacterTextureRegion[12]),
    PIRAT("mainCharacter_pirat", "mainCharacter_pirat_details", AssetLoader.maincharacterTextureRegion[13]),
    FOX("mainCharacter_fox", "mainCharacter_fox_details", AssetLoader.maincharacterTextureRegion[14]),
    DRAGON("mainCharacter_dragon", "mainCharacter_dragon_details", AssetLoader.maincharacterTextureRegion[15]),
    MAIN_ROBOT("mainCharacter_robot", "mainCharacter_robot_details", AssetLoader.maincharacterTextureRegion[16]),
    MAIN_UNICORN("mainCharacter_unicorn", "mainCharacter_unicorn_details", AssetLoader.maincharacterTextureRegion[17]),
    FARMER("mainCharacter_farmer", "mainCharacter_farmer_details", AssetLoader.maincharacterTextureRegion[18]),
    HARRY("mainCharacter_harry", "mainCharacter_harry_details", AssetLoader.maincharacterTextureRegion[19]),
    HERMINE("mainCharacter_hermine", "mainCharacter_hermine_details", AssetLoader.maincharacterTextureRegion[20]),
    RON("mainCharacter_ron", "mainCharacter_ron_details", AssetLoader.maincharacterTextureRegion[21]),
    ELSA("mainCharacter_elsa", "mainCharacter_elsa_details", AssetLoader.maincharacterTextureRegion[22]),
    ANNA("mainCharacter_anna", "mainCharacter_anna_details", AssetLoader.maincharacterTextureRegion[23]),
    RACOON("mainCharacter_racoon", "mainCharacter_racoon_details", AssetLoader.maincharacterTextureRegion[24]),
    ROBOT_ALIEN("mainCharacter_robotalien", "mainCharacter_robotalien_details", AssetLoader.maincharacterTextureRegion[25]),
    MAIN_FIREFIGHTER("mainCharacter_firefighter", "mainCharacter_firefighter_details", AssetLoader.maincharacterTextureRegion[26]),
    MAIN_POLICE("mainCharacter_police", "mainCharacter_police_details", AssetLoader.maincharacterTextureRegion[27]),
    SCIENTIST("mainCharacter_scientist", "mainCharacter_scientist_details", AssetLoader.maincharacterTextureRegion[28]),
    TEACHER("mainCharacter_teacher", "mainCharacter_teacher_details", AssetLoader.maincharacterTextureRegion[29]);

    private final String name;
    private final String details;
    private TextureRegion image;

    SupportingCharacter(final String name, final String details) {
        this(name, details, null);
    }

    SupportingCharacter(final String name, final String details, final TextureRegion image) {
        this.name = name;
        this.details = details;
        this.image = image;
    }

    @Override
    public String getEnumName() {
        return "supportingCharacter";
    }

    public static EnumInterface getRandom() {
        return SupportingCharacter.values()[(int)(SupportingCharacter.values().length * Math.random())];
    }

    @Override
    public EnumInterface getNext(int add) {
        int index = 0;
        for (SupportingCharacter character : values()) {
            if (character.getName().equals(this.getName())) {
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
        for (SupportingCharacter character : values()) {
            if (character.getName().equals(name)) {
                return character;
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
            SupportingCharacter supportingCharacter = values()[i];
            if (i >= AssetLoader.supportCharacterTextureRegion.length) {
                supportingCharacter.image = AssetLoader.maincharacterTextureRegion[i-AssetLoader.supportCharacterTextureRegion.length];
            } else {
                supportingCharacter.image = AssetLoader.supportCharacterTextureRegion[i];
            }
        }
    }
}
