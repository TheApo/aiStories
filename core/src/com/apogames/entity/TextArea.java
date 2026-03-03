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

        super.setText(text);

        if (getFont() == null) {
            return;
        }

        float maxWidth = this.getWidth() - 20;
        String[] paragraphs = text.split("\n", -1);
        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                myText.add("");
                continue;
            }
            Constants.glyphLayout.setText(getFont(), paragraph);
            if (Constants.glyphLayout.width <= maxWidth) {
                myText.add(paragraph);
                continue;
            }
            String[] words = paragraph.split(" ", -1);
            StringBuilder current = new StringBuilder();
            for (String word : words) {
                if (current.length() == 0) {
                    current.append(word);
                } else {
                    String test = current + " " + word;
                    Constants.glyphLayout.setText(getFont(), test);
                    if (Constants.glyphLayout.width > maxWidth) {
                        myText.add(current.toString());
                        current = new StringBuilder(word);
                    } else {
                        current = new StringBuilder(test);
                    }
                }
            }
            if (current.length() > 0) {
                myText.add(current.toString());
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
