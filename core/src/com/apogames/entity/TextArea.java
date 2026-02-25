package com.apogames.entity;

import com.apogames.aistories.Constants;
import com.apogames.backend.DrawString;
import com.apogames.backend.GameScreen;
import com.badlogic.gdx.Gdx;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class TextArea extends ApoButton {

    private ArrayList<String> myText = new ArrayList<>();

    @Getter
    @Setter
    private int add = 20;

    /**
     * Instantiates a new Apo button.
     *
     * @param x        the x
     * @param y        the y
     * @param width    the width
     * @param height   the height
     * @param function the function
     * @param text     the text
     */
    public TextArea(int x, int y, int width, int height, String function, String text) {
        super(x, y, width, height, function, text);
    }

    public void setText(String text) {
        this.myText.clear();

//        text = text.replace("\n\n", "\n");

        super.setText(text);

        boolean end = false;
        String currentString = "";
        String leftString = text;
        int startIndex = 0;
        int firstIndex = 0;
        int lastNextSpace = 0;
        while (!end) {
            int nextSpace = text.indexOf(" ", startIndex);
            int nextLineBreak = text.indexOf("\n", startIndex);
            if (nextSpace == -1) {
                this.myText.add(leftString.substring(firstIndex));
                end = true;
            } else {
                boolean skip = false;
                if (nextLineBreak > 0) {
                    currentString = leftString.substring(firstIndex, nextLineBreak);
                    Constants.glyphLayout.setText(getFont(), currentString);

                    if (Constants.glyphLayout.width < this.getWidth() - 20) {
                        myText.add(leftString.substring(firstIndex, nextLineBreak));
                        firstIndex = nextLineBreak + 1;
                        startIndex = firstIndex;
                        skip = true;
                    }
                }

                if (!skip) {
                    currentString = leftString.substring(firstIndex, nextSpace);
                    if (getFont() == null) {
                        return;
                    }
                    if (currentString == null) {
                        currentString = "";
                    }
                    Constants.glyphLayout.setText(getFont(), currentString);

                    if (Constants.glyphLayout.width > this.getWidth() - 20) {
                        myText.add(leftString.substring(firstIndex, lastNextSpace));
                        firstIndex = lastNextSpace + 1;
                    } else {
                        lastNextSpace = nextSpace;
                        startIndex = nextSpace + 1;
                    }
                }
            }
        }
    }

    public final ArrayList<String> getMyText() {
        return myText;
    }

    public int getRows() {
        return (int)(this.getHeight() / this.add);
    }
    
    public void renderFilled(GameScreen screen, int changeX, int changeY) {
        //screen.getRenderer().begin(ShapeRenderer.ShapeType.Filled);
        screen.getRenderer().setColor(1f, 1f, 1f, 1f);
        screen.getRenderer().rect((this.getX() + changeX), (this.getY() + changeY), (this.getWidth()), (this.getHeight()));
        //screen.getRenderer().end();
    }

    public void renderLine(GameScreen screen, int changeX, int changeY) {
        //screen.getRenderer().begin(ShapeRenderer.ShapeType.Line);
        screen.getRenderer().setColor(0f, 0f, 0f, 1f);
        Gdx.gl20.glLineWidth(2f);
        screen.getRenderer().rect((this.getX() + changeX), (this.getY() + changeY), (this.getWidth() - 1), (this.getHeight() - 1));

        Gdx.gl20.glLineWidth(1f);
        //screen.getRenderer().end();
    }

    public void renderSprite(GameScreen screen, int changeX, int changeY) {
        this.renderSprite(screen, changeX, changeY, 0);
    }

    public void renderSprite(GameScreen screen, int changeX, int changeY, int start) {
        if (this.getText() != null) {
            int addY = 0;
            int count = 0;
            int rows = getRows();
            for (int i = start; i < start + rows && i < this.myText.size(); i++) {
                screen.drawString(this.myText.get(i), this.getX() + changeX + 10, this.getY() + changeY + 5 + count * add + addY, Constants.COLOR_BLACK, super.getFont(), DrawString.BEGIN, false, false);
                count += 1;
            }
//            for (String text : this.myText) {
//                if (count >= start) {
//                    screen.drawString(text, this.getX() + changeX + 10, this.getY() + changeY + 5 + count * add + addY, Constants.COLOR_BLACK, super.getFont(), DrawString.BEGIN, false, false);
//                }
//                count += 1;
//
//                if (count > rows) {
//                    break;
//                }
//            }
        }
    }
}
