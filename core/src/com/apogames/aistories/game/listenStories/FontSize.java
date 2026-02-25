package com.apogames.aistories.game.listenStories;

import com.apogames.asset.AssetLoader;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import lombok.Getter;

@Getter
public enum FontSize {

    FONT_15(AssetLoader.font15, 20),
    FONT_20(AssetLoader.font20, 25),
    FONT_25(AssetLoader.font25, 30),
    FONT_30(AssetLoader.font30, 35),
    FONT_40(AssetLoader.font40, 45);

    private BitmapFont font;

    private final int add;

    FontSize(BitmapFont font, int add) {
        this.font = font;
        this.add = add;
    }

    public FontSize getNext(int add) {
        for (int i = 0; i < FontSize.values().length; i++) {
            FontSize fontSize = FontSize.values()[i];
            if (fontSize.equals(this)) {
                if (i + add >= FontSize.values().length) {
                    return FontSize.values()[i + add - FontSize.values().length];
                } else if (i + add < 0) {
                    return FontSize.values()[i + add + FontSize.values().length];
                } else {
                    return FontSize.values()[i + add];
                }
            }
        }
        return this;
    }

    public static void refreshAll() {
        for (FontSize e : values()) {
            switch (e) {
                case FONT_15:
                    e.font = AssetLoader.font15;
                    break;
                case FONT_20:
                    e.font = AssetLoader.font20;
                    break;
                case FONT_25:
                    e.font = AssetLoader.font25;
                    break;
                case FONT_30:
                    e.font = AssetLoader.font30;
                    break;
                case FONT_40:
                    e.font = AssetLoader.font40;
                    break;
            }
        }
    }
}
