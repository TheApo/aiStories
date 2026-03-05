package com.apogames.aistories.game.createStory;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.MainPanel;
import com.apogames.aistories.game.objects.CustomEntity;
import com.apogames.aistories.game.objects.EnumInterface;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.DrawString;
import com.apogames.backend.GameScreen;
import com.apogames.common.Localization;
import com.apogames.entity.ApoButtonEditIcon;
import com.apogames.entity.ApoButtonMovement;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectSelection implements ObjectSelectionInterface {

    public static final float[] BACKGROUND_COLOR = Constants.COLOR_WHITE;
    public static final float[] BACKGROUND_BORDER_COLOR = Constants.COLOR_BLACK;

    private final static String UP = "UP";
    private final static String DOWN = "DOWN";
    private final static String JUMPCUSTOM = "JUMPCUSTOM";

    @Getter
    private final int x;
    @Getter
    private final int y;
    @Getter
    private final int width;
    @Getter
    private final int height;

    private final String id;

    private final float[] background_color;
    @Getter
    @Setter
    private EnumInterface gameObjective;

    private final ApoButtonMovement upButton;
    private final ApoButtonMovement downButton;
    private ApoButtonEditIcon jumpCustomButton;
    @Getter
    private CustomEntity customEntity;

    private final List<EnumInterface> allOptions;
    private int currentIndex;

    public ObjectSelection(MainPanel main, int x, int y, int width, int height, final EnumInterface gameObjective) {
        this(main, x, y, width, height, gameObjective, BACKGROUND_COLOR, null, null);
    }

    public ObjectSelection(MainPanel main, int x, int y, int width, int height, final EnumInterface gameObjective, float[] color) {
        this(main, x, y, width, height, gameObjective, color, null, null);
    }

    public ObjectSelection(MainPanel main, int x, int y, int width, int height, final EnumInterface gameObjective, float[] color, EnumInterface[] enumValues, CustomEntity customEntity) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.background_color = color;

        this.gameObjective = gameObjective;

        this.id = this.gameObjective.getEnumName();

        // Build options list
        this.allOptions = new ArrayList<>();
        if (enumValues != null) {
            Collections.addAll(this.allOptions, enumValues);
        }
        if (customEntity != null) {
            this.allOptions.add(customEntity);
        }

        // Find current index
        this.currentIndex = 0;
        for (int i = 0; i < this.allOptions.size(); i++) {
            if (this.allOptions.get(i) == gameObjective || this.allOptions.get(i).getName().equals(gameObjective.getName())) {
                this.currentIndex = i;
                break;
            }
        }

        String function = this.id + "_" + UP;
        int buttonWidth = 128;
        int buttonHeight = 64;
        int buttonX = x + width / 2 - buttonWidth / 2;
        int buttonY = y + 5;
        this.upButton = new ApoButtonMovement(buttonX, buttonY, buttonWidth, buttonHeight, function, BACKGROUND_COLOR, Constants.COLOR_GREY);
        this.upButton.setStroke(1);
        this.upButton.setMovement(ApoButtonMovement.MOVEMENT.UP);
        this.upButton.setFont(AssetLoader.font40);
        main.getButtons().add(this.upButton);

        function = this.id + "_" + DOWN;
        buttonY = y + height - buttonHeight - 5;
        this.downButton = new ApoButtonMovement(buttonX, buttonY, buttonWidth, buttonHeight, function, BACKGROUND_COLOR, Constants.COLOR_GREY);
        this.downButton.setStroke(1);
        this.downButton.setMovement(ApoButtonMovement.MOVEMENT.DOWN);
        this.downButton.setFont(AssetLoader.font40);
        main.getButtons().add(this.downButton);

        if (customEntity != null) {
            this.customEntity = customEntity;

            function = this.id + "_" + JUMPCUSTOM;
            int editSize = 48;
            // Position on bottom-right corner of the character image (200x200, centered in column)
            int imageRight = x + width / 2 + 100;
            int imageBottom = y + height / 2 + 100 - 50;
            int editX = imageRight - editSize + 8;
            int editY = imageBottom - editSize + 8;
            this.jumpCustomButton = new ApoButtonEditIcon(editX, editY, editSize, function, this.background_color, Constants.COLOR_BLACK);
            this.jumpCustomButton.setVisible(false);
            main.getButtons().add(this.jumpCustomButton);
        }
    }

    public void setNeededButtonsVisible() {
        this.upButton.setVisible(true);
        this.downButton.setVisible(true);
        if (this.jumpCustomButton != null) {
            this.jumpCustomButton.setVisible(true);
        }
    }

    @Override
    public void mouseMoved(int mouseX, int mouseY) {

    }

    @Override
    public void mouseButtonReleased(int mouseX, int mouseY, boolean isRightButton) {

    }

    @Override
    public void mousePressed(int x, int y, boolean isRightButton) {

    }

    public String getJumpCustomFunctionId() {
        return this.id + "_" + JUMPCUSTOM;
    }

    @Override
    public void mouseButtonFunction(String function) {
        if (function.equals(this.id + "_" + UP)) {
            if (!this.allOptions.isEmpty()) {
                this.currentIndex = (this.currentIndex - 1 + this.allOptions.size()) % this.allOptions.size();
                this.gameObjective = this.allOptions.get(this.currentIndex);
            } else {
                this.gameObjective = this.gameObjective.getNext(-1);
            }

        } else if (function.equals(this.id + "_" + DOWN)) {
            if (!this.allOptions.isEmpty()) {
                this.currentIndex = (this.currentIndex + 1) % this.allOptions.size();
                this.gameObjective = this.allOptions.get(this.currentIndex);
            } else {
                this.gameObjective = this.gameObjective.getNext(+1);
            }

        } else if (function.equals(this.id + "_" + JUMPCUSTOM)) {
            // Jump to the custom option
            if (this.customEntity != null) {
                for (int i = 0; i < this.allOptions.size(); i++) {
                    if (this.allOptions.get(i) == this.customEntity) {
                        this.currentIndex = i;
                        this.gameObjective = this.customEntity;
                        break;
                    }
                }
    
            }
        }
    }

    @Override
    public void renderFilled(GameScreen screen, int changeX, int changeY) {
        renderFilled(screen, this.background_color, changeX, changeY);
    }

    public void renderFilled(GameScreen screen, float[] color, int changeX, int changeY) {
        screen.getRenderer().setColor(color[0], color[1], color[2], 0.6f);
        screen.getRenderer().roundedRect(this.x + changeX, this.y + changeY, this.width, this.height, 10);

        if (this.gameObjective.getImage() != null) {
            screen.getRenderer().setColor(color[0], color[1], color[2], 1f);
            screen.getRenderer().rect(this.x + changeX + this.width / 2f - this.gameObjective.getImage().getRegionWidth() / 2f - 2, this.y + changeY + this.height / 2f - this.gameObjective.getImage().getRegionHeight() / 2f - 50 - 2, this.gameObjective.getImage().getRegionWidth() + 4, this.gameObjective.getImage().getRegionHeight() + 4);
        }
    }

    @Override
    public void renderLine(GameScreen screen, int changeX, int changeY) {
        screen.getRenderer().setColor(BACKGROUND_BORDER_COLOR[0], BACKGROUND_BORDER_COLOR[1], BACKGROUND_BORDER_COLOR[2], 1f);
        screen.getRenderer().roundedRectLine(this.x + changeX, this.y + changeY, this.width, this.height, 10);
    }

    @Override
    public void renderSprite(GameScreen screen, int changeX, int changeY) {
        String categoryName = Localization.getInstance().getCommon().get(this.gameObjective.getEnumName());

        BitmapFont font = AssetLoader.font30;
        screen.getGlyphLayout().setText(font, categoryName);
        if (screen.getGlyphLayout().width > this.width - 20) {
            font = AssetLoader.font25;
        }

        screen.drawString(categoryName, this.x + changeX + this.width / 2f, this.y + changeY + 110, BACKGROUND_BORDER_COLOR, font, DrawString.MIDDLE, true, false);

        // Use getDisplayName() for the character name (supports custom characters)
        String displayName = this.gameObjective.getDisplayName();
        font = AssetLoader.font25;
        screen.getGlyphLayout().setText(font, displayName);
        if (screen.getGlyphLayout().width > this.width - 20) {
            font = AssetLoader.font20;
        }
        screen.drawString(displayName, this.x + changeX + this.width / 2f, this.y + changeY + this.height - 220, BACKGROUND_BORDER_COLOR, font, DrawString.MIDDLE, true, false);

        // Use getDisplayDetails() for the details (supports custom characters)
        String details = this.gameObjective.getDisplayDetails();
        List<String> wrappedLines = wrapText(details, AssetLoader.font15, this.width - 20, screen);
        int lineCount = wrappedLines.size();
        int startY = 0;
        if (lineCount > 3) {
            startY -= 15 * (lineCount - 3);
        }
        for (int i = 0; i < lineCount; i++) {
            screen.drawString(wrappedLines.get(i), this.x + changeX + this.width / 2f, this.y + changeY + startY + this.height - 140 + i * 15, BACKGROUND_BORDER_COLOR, AssetLoader.font15, DrawString.MIDDLE, true, false);
        }

        if (this.gameObjective.getImage() != null) {
            screen.spriteBatch.draw(this.gameObjective.getImage(), this.x + changeX + this.width / 2f - this.gameObjective.getImage().getRegionWidth() / 2f, this.y + changeY + this.height / 2f - this.gameObjective.getImage().getRegionHeight() / 2f - 50);
        }
    }

    private List<String> wrapText(String text, BitmapFont font, float maxWidth, GameScreen screen) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n")) {
            String[] words = paragraph.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String test = line.length() == 0 ? word : line + " " + word;
                screen.getGlyphLayout().setText(font, test);
                if (screen.getGlyphLayout().width > maxWidth && line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(test);
                }
            }
            if (line.length() > 0) {
                lines.add(line.toString());
            }
        }
        return lines;
    }
}
