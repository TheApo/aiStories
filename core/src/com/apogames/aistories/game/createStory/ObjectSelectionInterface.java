package com.apogames.aistories.game.createStory;

import com.apogames.backend.GameScreen;

public interface ObjectSelectionInterface {

    void mouseMoved(int mouseX, int mouseY);

    void mouseButtonReleased(int mouseX, int mouseY, boolean isRightButton);

    void mousePressed(int x, int y, boolean isRightButton);

    void mouseButtonFunction(String function);

    void renderFilled(GameScreen screen, int changeX, int changeY);

    void renderLine(GameScreen screen, int changeX, int changeY);

    void renderSprite(GameScreen screen, int changeX, int changeY);

}
