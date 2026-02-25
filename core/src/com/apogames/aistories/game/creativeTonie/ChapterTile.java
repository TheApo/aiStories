package com.apogames.aistories.game.creativeTonie;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.MainPanel;
import com.apogames.aistories.game.main.tonie.Chapter;
import com.apogames.asset.AssetLoader;
import com.apogames.entity.ApoButton;
import com.apogames.entity.ApoButtonMovement;
import lombok.Getter;
import lombok.Setter;

public class ChapterTile {

    public static final float[] BACKGROUND_COLOR = Constants.COLOR_WHITE;
    public static final float[] BACKGROUND_BORDER_COLOR = Constants.COLOR_BLACK;

    @Getter
    private final int x;
    @Getter
    private final int y;
    @Getter
    private final int width;
    @Getter
    private final int height;
    @Getter
    @Setter
    private Chapter chapter;

    private final ApoButtonMovement button;

    public ChapterTile(MainPanel main, int x, int y, int width, int height, Chapter chapter) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.chapter = chapter;

        String function = CreativeTonie.FUNCTION_DELETE + this.chapter.getId();

        ApoButton buttonByFunction = main.getButtonByFunction(function);
        if (buttonByFunction != null) {
            main.getButtons().remove(buttonByFunction);
        }

        int buttonWidth = 20;
        int buttonHeight = 20;
        int buttonX = x + 1;
        int buttonY = y - 5;
        this.button = new ApoButtonMovement(buttonX, buttonY, buttonWidth, buttonHeight, function, BACKGROUND_COLOR, BACKGROUND_BORDER_COLOR);
        this.button.setStroke(1);
        this.button.setMovement(ApoButtonMovement.MOVEMENT.DELETE);
        this.button.setFont(AssetLoader.font40);
        main.getButtons().add(this.button);
    }

    public String getId() {
        return this.chapter.getId();
    }

    public ApoButtonMovement getButton() {
        return button;
    }
}
