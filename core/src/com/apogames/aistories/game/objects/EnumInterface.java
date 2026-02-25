package com.apogames.aistories.game.objects;

import com.apogames.common.Localization;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public interface EnumInterface {

    String getEnumName();
    String getName();
    String getDetails();
    TextureRegion getImage();

    EnumInterface getNext(int add);
    EnumInterface getEnumByName(String name);

    void refresh();

    default String getDisplayName() {
        return Localization.getInstance().getCommon().get(getName());
    }

    default String getDisplayDetails() {
        return Localization.getInstance().getCommon().get(getDetails());
    }
}
