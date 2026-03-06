package com.apogames.entity;

import com.apogames.asset.AssetLoader;
import com.apogames.backend.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.GridPoint2;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Textfield extends ApoButton {

    private static GlyphLayout glyphLayout = new GlyphLayout();
    private final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,6}$");
    private final float[] COLOR_BLACK = new float[]{0f, 0f, 0f, 1f};
    private final float[] COLOR_RED = new float[]{1f, 0f, 0f, 1f};
    private final float[] MOUSE_OVER_COLOR = new float[]{255 / 255f, 255 / 255f, 100 / 255f, 1f};
    private final float[] SELECTED_COLOR = new float[]{0 / 255f, 150 / 255f, 255 / 255f, 1f};
    private final int MAX_LENGTH = 100;
    private final int TIME_LINE = 700;
    private String curString;
    private int position;
    private int time;
    private boolean bLineOn;
    private BitmapFont myFont;
    private boolean bEnabled;
    private int maxLength;
    private GridPoint2 selectedPosition;
    private boolean bCorrectString;
    private boolean fixedFont;
    private boolean multiLine;

    public Textfield(float x, float y, float width, float height, BitmapFont myFont) {
        super((int) x, (int) y, (int) width, (int) height, "", "");

        this.myFont = myFont;
        this.bEnabled = true;
    }

    public void init() {
        super.init();
        this.curString = "dirk.aporius@gmail.com";
        this.position = this.curString.length();
        this.bLineOn = true;
        this.bEnabled = true;
        this.time = 0;
        if (this.myFont == null) {
            this.myFont = AssetLoader.font25;
        }
        if (this.maxLength <= 0) {
            this.maxLength = this.MAX_LENGTH;
        }
        this.selectedPosition = new GridPoint2(-1, -1);
        this.bCorrectString = true;
    }

    public boolean isCorrectString() {
        return bCorrectString;
    }

    public void setCorrectString(boolean bCorrectString) {
        this.bCorrectString = bCorrectString;
    }

    public boolean isStringAValidEMailAdress() {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(this.curString);
        return matcher.find();
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (this.maxLength <= 0) {
            this.maxLength = this.MAX_LENGTH;
        }
    }

    public boolean isBEnabled() {
        return this.bEnabled;
    }

    public void setBEnabled(boolean bEnabled) {
        this.bEnabled = bEnabled;
    }

    public BitmapFont getFont() {
        return this.myFont;
    }

    public void setFont(BitmapFont myFont) {
        this.myFont = myFont;
    }

    public void setFixedFont(boolean fixedFont) {
        this.fixedFont = fixedFont;
    }

    public void setMultiLine(boolean multiLine) {
        this.multiLine = multiLine;
    }

    public boolean mouseDragged(int x, int y) {
        if (!this.bEnabled) {
            return false;
        }
        if (this.selectedPosition.x < 0) {
            this.selectedPosition.x = this.getPosition();
        }
        if (this.selectedPosition.x >= 0) {
            if (this.getRec().contains(x, y)) {
                int position = this.getThisPosition(x, y);
                if (position != -1) {
                    this.setSelectedPosition(position);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mousePressed(int x, int y) {
        if (!this.bEnabled) {
            return false;
        }
        if (this.getPressed(x, y)) {
            if (this.selectedPosition.y >= 0) {
                this.selectedPosition.x = -1;
                this.selectedPosition.y = -1;
            }
            int position = this.getThisPosition(x, y);
            if (position != -1) {
                this.selectedPosition.x = position;
            }
            return true;
        } else {
            this.selectedPosition.x = -1;
            this.selectedPosition.y = -1;
        }
        return false;
    }

    public boolean mouseReleased(int x, int y) {
        if (!this.bEnabled) {
            return false;
        }
        if (super.getReleased(x, y)) {
            int position = this.getThisPosition(x, y);
            if (position != -1) {
                this.setPosition(position);
                this.setSelectedPosition(position);
            }
            return true;
        } else {
            this.selectedPosition.x = -1;
            this.selectedPosition.y = -1;
        }
        return false;
    }

    public void nextSelectedPosition(int position) {
        if (this.selectedPosition.x < 0) {
            this.selectedPosition.x = this.getPosition();
        }
        if (position > this.curString.length()) {
            position = this.curString.length();
        } else if (position < 0) {
            position = 0;
        }
        this.setSelectedPosition(position);
    }

    private void setSelectedPosition(int position) {
        if (this.selectedPosition.x != position) {
            this.setPosition(position);
            if (this.selectedPosition.x > position) {
                if (this.selectedPosition.y < this.selectedPosition.x) {
                    this.selectedPosition.y = this.selectedPosition.x;
                }
                this.selectedPosition.x = position;
            } else {
                this.selectedPosition.y = this.getPosition();
            }
        }
    }

    private int getThisPosition(int x, int y) {
        if (this.multiLine) {
            return getThisPositionMultiLine(x, y);
        }
        String s = this.curString;
        glyphLayout.setText(myFont, s);
        float w = glyphLayout.width;
        if (x > w + 5 + this.getX()) {
            return s.length();
        } else if (x < this.getX() + 5) {
            return 0;
        } else {
            for (int i = 0; i < this.curString.length(); i++) {
                String old = this.curString.substring(0, i);
                String next = this.curString.substring(0, i + 1);
                glyphLayout.setText(myFont, old);
                float wOld = glyphLayout.width;
                glyphLayout.setText(myFont, next);
                float wNext = glyphLayout.width;
                if ((x > wOld + 5 + this.getX()) && (x < wNext + 5 + this.getX())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getThisPositionMultiLine(int x, int y) {
        List<String> lineTexts = new ArrayList<>();
        int[] lineStarts = wrapText(lineTexts);

        float lineHeight = getMultiLineHeight();
        float textStartY = this.getY() + 5;

        int clickLine = (int) ((y - textStartY) / lineHeight);
        if (clickLine < 0) clickLine = 0;
        if (clickLine >= lineTexts.size()) clickLine = lineTexts.size() - 1;

        String line = lineTexts.get(clickLine);
        int lineStart = lineStarts[clickLine];

        if (line.isEmpty()) {
            return lineStart;
        }

        float fieldX = this.getX() + 5;
        if (x < fieldX) {
            return lineStart;
        }

        glyphLayout.setText(myFont, line);
        if (x > fieldX + glyphLayout.width) {
            return lineStart + line.length();
        }

        for (int i = 0; i < line.length(); i++) {
            glyphLayout.setText(myFont, line.substring(0, i));
            float wOld = glyphLayout.width;
            glyphLayout.setText(myFont, line.substring(0, i + 1));
            float wNext = glyphLayout.width;
            if (x < fieldX + (wOld + wNext) / 2f) {
                return lineStart + i;
            }
        }
        return lineStart + line.length();
    }

    public String getCurString() {
        return this.curString;
    }

    public void setCurString(String curString) {
        this.curString = curString;
        if (!this.isSelect()) {
            this.position = this.curString.length();
        }
        if (!this.fixedFont) {
            this.myFont = this.getCorrectFontforFunction();
        }
    }

    public String getSelectedString() {
        String selected = null;
        if ((this.selectedPosition.x >= 0) && (this.selectedPosition.y >= 0)) {
            return this.curString.substring(this.selectedPosition.x, this.selectedPosition.y);
        }
        return selected;
    }

    public void deleteSelectedString() {
        String s = this.getSelectedString();
        if ((s != null) && (s.length() > 0)) {
            this.setPosition(this.selectedPosition.x);
            this.curString = this.curString.substring(0, this.selectedPosition.x) + this.curString.substring(this.selectedPosition.y);
            this.selectedPosition.x = -1;
            this.selectedPosition.y = -1;
        }
    }

    public void removeCurStringAndSetCurString(String curString) {
        if (!curString.equals(this.curString)) {
            this.position = 0;
            this.setCurString(curString);
        }
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
        if (this.position < 0) {
            this.position = 0;
        } else if (this.position > this.curString.length()) {
            this.position = this.curString.length();
        }
        this.showLine();
    }

    /**
     * Handles navigation keys (LEFT, RIGHT, HOME, END, UP, DOWN).
     * Returns true if the key was handled.
     */
    public boolean handleNavigationKey(int keyCode) {
        switch (keyCode) {
            case Input.Keys.LEFT:
                setPosition(position - 1);
                clearSelection();
                return true;
            case Input.Keys.RIGHT:
                setPosition(position + 1);
                clearSelection();
                return true;
            case Input.Keys.HOME:
                navigateHome();
                clearSelection();
                return true;
            case Input.Keys.END:
                navigateEnd();
                clearSelection();
                return true;
            case Input.Keys.UP:
                navigateUp();
                clearSelection();
                return true;
            case Input.Keys.DOWN:
                navigateDown();
                clearSelection();
                return true;
            default:
                return false;
        }
    }

    private void clearSelection() {
        this.selectedPosition.x = -1;
        this.selectedPosition.y = -1;
    }

    private void navigateHome() {
        if (!multiLine) {
            setPosition(0);
            return;
        }
        List<String> lineTexts = new ArrayList<>();
        int[] lineStarts = wrapText(lineTexts);
        int line = findCursorLine(lineStarts);
        setPosition(lineStarts[line]);
    }

    private void navigateEnd() {
        if (!multiLine) {
            setPosition(curString.length());
            return;
        }
        List<String> lineTexts = new ArrayList<>();
        int[] lineStarts = wrapText(lineTexts);
        int line = findCursorLine(lineStarts);
        setPosition(lineStarts[line] + lineTexts.get(line).length());
    }

    private void navigateUp() {
        if (!multiLine) {
            setPosition(0);
            return;
        }
        List<String> lineTexts = new ArrayList<>();
        int[] lineStarts = wrapText(lineTexts);
        int line = findCursorLine(lineStarts);
        if (line == 0) {
            setPosition(0);
            return;
        }
        int col = position - lineStarts[line];
        float pixelX = getPixelXForCol(lineTexts.get(line), col);
        int targetCol = getColForPixelX(lineTexts.get(line - 1), pixelX);
        setPosition(lineStarts[line - 1] + targetCol);
    }

    private void navigateDown() {
        if (!multiLine) {
            setPosition(curString.length());
            return;
        }
        List<String> lineTexts = new ArrayList<>();
        int[] lineStarts = wrapText(lineTexts);
        int line = findCursorLine(lineStarts);
        if (line >= lineTexts.size() - 1) {
            setPosition(curString.length());
            return;
        }
        int col = position - lineStarts[line];
        float pixelX = getPixelXForCol(lineTexts.get(line), col);
        int targetCol = getColForPixelX(lineTexts.get(line + 1), pixelX);
        setPosition(lineStarts[line + 1] + targetCol);
    }

    private int findCursorLine(int[] lineStarts) {
        for (int i = lineStarts.length - 1; i >= 0; i--) {
            if (position >= lineStarts[i]) {
                return i;
            }
        }
        return 0;
    }

    private float getPixelXForCol(String lineText, int col) {
        col = Math.min(col, lineText.length());
        if (col <= 0) return 0;
        glyphLayout.setText(myFont, lineText.substring(0, col));
        return glyphLayout.width;
    }

    private int getColForPixelX(String lineText, float targetX) {
        if (lineText.isEmpty()) return 0;
        for (int i = 1; i <= lineText.length(); i++) {
            glyphLayout.setText(myFont, lineText.substring(0, i));
            if (glyphLayout.width >= targetX) {
                float right = glyphLayout.width;
                float left = 0;
                if (i > 1) {
                    glyphLayout.setText(myFont, lineText.substring(0, i - 1));
                    left = glyphLayout.width;
                }
                return (targetX - left < right - targetX) ? i - 1 : i;
            }
        }
        return lineText.length();
    }

    private void deleteTextBackspace() {
        if (!this.bEnabled) {
            return;
        }
        if ((this.curString.length() > 0) && (this.position != 0)) {
            this.curString = this.curString.substring(0, position - 1) + this.curString.substring(this.position, this.curString.length());
            this.setPosition(this.getPosition() - 1);
        }
        this.showLine();
    }

    private void deleteTextDelete() {
        if (!this.bEnabled) {
            return;
        }
        if (this.curString.length() != this.position) {
            this.curString = this.curString.substring(0, this.getPosition()) + this.curString.substring(this.getPosition() + 1, this.curString.length());
        }
        this.showLine();
    }

    public void addCharacter(int button, char character) {
        if (!this.bEnabled) {
            return;
        }
        if (this.isSelect()) {
            if (button == 67) {
                String s = this.getSelectedString();
                if ((s != null) && (s.length() > 0)) {
                    this.deleteSelectedString();
                } else {
                    this.deleteTextBackspace();
                }
                this.selectedPosition.x = -1;
                this.selectedPosition.y = -1;
            } else if (button == 112) {
                String s = this.getSelectedString();
                if ((s != null) && (s.length() > 0)) {
                    this.deleteSelectedString();
                } else {
                    this.deleteTextDelete();
                }
                this.selectedPosition.x = -1;
                this.selectedPosition.y = -1;
            } else if (button == 2) {
                this.curString = "";
                this.setPosition(0);
                this.selectedPosition.x = -1;
                this.selectedPosition.y = -1;
            } else if (button == 3) {
                this.setPosition(0);
                this.selectedPosition.x = -1;
                this.selectedPosition.y = -1;
            } else if (button == 132) {
                this.setPosition(curString.length());
                this.selectedPosition.x = -1;
                this.selectedPosition.y = -1;
            } else if (button == 21) {
                this.setPosition(this.getPosition() - 1);
                this.selectedPosition.x = -1;
                this.selectedPosition.y = -1;
            } else if (button == 22) {
                this.setPosition(this.getPosition() + 1);
                this.selectedPosition.x = -1;
                this.selectedPosition.y = -1;
            } else if (this.curString.length() < this.maxLength) {
                if (((button >= 29) && (button <= 54)) || (button == 56) || (button == Input.Keys.AT)) {
                    this.curString = this.curString.substring(0, this.position) + Input.Keys.toString(button).toLowerCase() + this.curString.substring(this.position, this.curString.length());
                    this.position += 1;
                    this.showLine();
                    this.selectedPosition.x = -1;
                    this.selectedPosition.y = -1;
                }
            } else {
                return;
            }
            this.myFont = this.getCorrectFontforFunction();
        }
    }

    public void addTypedCharacter(char character) {
        if (!this.bEnabled || !this.isSelect()) {
            return;
        }
        if (character == 8) {
            String s = this.getSelectedString();
            if ((s != null) && (s.length() > 0)) {
                this.deleteSelectedString();
            } else {
                this.deleteTextBackspace();
            }
            this.selectedPosition.x = -1;
            this.selectedPosition.y = -1;
        } else if (character == 127) {
            String s = this.getSelectedString();
            if ((s != null) && (s.length() > 0)) {
                this.deleteSelectedString();
            } else {
                this.deleteTextDelete();
            }
            this.selectedPosition.x = -1;
            this.selectedPosition.y = -1;
        } else if (this.multiLine && (character == 10 || character == 13)) {
            if (this.curString.length() < this.maxLength) {
                String s = this.getSelectedString();
                if ((s != null) && (s.length() > 0)) {
                    this.deleteSelectedString();
                }
                this.curString = this.curString.substring(0, this.position) + '\n' + this.curString.substring(this.position);
                this.position += 1;
                this.showLine();
                this.selectedPosition.x = -1;
                this.selectedPosition.y = -1;
            }
        } else if (character >= 32 && this.curString.length() < this.maxLength) {
            String s = this.getSelectedString();
            if ((s != null) && (s.length() > 0)) {
                this.deleteSelectedString();
            }
            this.curString = this.curString.substring(0, this.position) + character + this.curString.substring(this.position);
            this.position += 1;
            this.showLine();
            this.selectedPosition.x = -1;
            this.selectedPosition.y = -1;
        }
        if (!this.fixedFont) {
            this.myFont = this.getCorrectFontforFunction();
        }
    }

    private void showLine() {
        if (this.isSelect()) {
            this.bLineOn = true;
            this.time = 0;
        }
    }

    public void think(int delta) {
        this.time += delta;
        if (this.time > this.TIME_LINE) {
            this.time = 0;
            this.bLineOn = !this.bLineOn;
        }
    }

    public void render(GameScreen screen, int changeX, int changeY) {
        try {
            // Background
            screen.getRenderer().begin(ShapeType.Filled);
            screen.getRenderer().setColor(1f, 1f, 1f, 1f);
            if (!this.bEnabled) {
                screen.getRenderer().setColor(0.5f, 0.5f, 0.5f, 1f);
            }
            screen.getRenderer().rect((this.getX() + changeX), (this.getY() + changeY), (this.getWidth()), (this.getHeight()));
            screen.getRenderer().end();

            // Border
            screen.getRenderer().begin(ShapeType.Line);
            if ((this.isBOver()) && (this.bEnabled)) {
                screen.getRenderer().setColor(MOUSE_OVER_COLOR[0], MOUSE_OVER_COLOR[1], MOUSE_OVER_COLOR[2], 1f);
                Gdx.gl20.glLineWidth(2f);
            } else if ((!this.isSelect()) || (!this.bEnabled)) {
                screen.getRenderer().setColor(0f, 0f, 0f, 1f);
            } else {
                screen.getRenderer().setColor(SELECTED_COLOR[0], SELECTED_COLOR[1], SELECTED_COLOR[2], 1f);
                Gdx.gl20.glLineWidth(2f);
            }
            screen.getRenderer().rect((this.getX() + changeX), (this.getY() + changeY), (this.getWidth() - 1), (this.getHeight() - 1));
            screen.getRenderer().end();

            Gdx.gl20.glLineWidth(1f);

            if (this.curString != null) {
                if (this.multiLine) {
                    renderMultiLine(screen, changeX, changeY);
                } else {
                    renderSingleLine(screen, changeX, changeY);
                }
            }
        } catch (Exception ex) {
        }
    }

    private void renderSingleLine(GameScreen screen, int changeX, int changeY) {
        if ((this.selectedPosition.x > -1) && (this.selectedPosition.y > -1) && (this.selectedPosition.x != this.selectedPosition.y)) {
            String s = this.curString.substring(0, this.selectedPosition.x);
            glyphLayout.setText(myFont, s);
            float x = glyphLayout.width + (int) (this.getX() + 5 + changeX);
            s = this.curString.substring(this.selectedPosition.x, this.selectedPosition.y);
            glyphLayout.setText(myFont, s);
            float width = glyphLayout.width;
            screen.getRenderer().begin(ShapeType.Filled);
            screen.getRenderer().setColor(SELECTED_COLOR[0], SELECTED_COLOR[1], SELECTED_COLOR[2], 0.3f);
            screen.getRenderer().rect(x, this.getY() + changeY + 1, width, this.getHeight() - 2);
            screen.getRenderer().end();
        }

        float[] color = COLOR_BLACK;
        if (!this.bCorrectString) {
            color = COLOR_RED;
        }
        String s = this.curString;
        glyphLayout.setText(myFont, s);
        screen.drawString(this.curString, this.getX() + 5 + changeX, this.getY() - glyphLayout.height / 2 + this.getHeight() / 2 + changeY - 5, color, myFont, false);
        if ((this.isSelect()) && (this.bLineOn) && (this.bEnabled)) {
            try {
                s = this.curString.substring(0, this.position);
                glyphLayout.setText(myFont, s);
                float w = glyphLayout.width;
                Gdx.gl20.glLineWidth(3f);
                screen.getRenderer().begin(ShapeType.Line);
                screen.getRenderer().setColor(color[0], color[1], color[2], 1f);
                screen.getRenderer().line((this.getX() + 5 + w + changeX), (this.getY() + this.getHeight() / 2 - glyphLayout.height / 2 + changeY), (this.getX() + 5 + w + changeX), (this.getY() + glyphLayout.height / 2 + this.getHeight() / 2 + changeY));
                screen.getRenderer().end();
                Gdx.gl20.glLineWidth(1f);
            } catch (StringIndexOutOfBoundsException ex) {
            }
        }
    }

    private void renderMultiLine(GameScreen screen, int changeX, int changeY) {
        List<String> lineTexts = new ArrayList<>();
        int[] lineStarts = wrapText(lineTexts);

        float[] color = COLOR_BLACK;
        if (!this.bCorrectString) {
            color = COLOR_RED;
        }

        float lineHeight = getMultiLineHeight();
        float textStartY = this.getY() + changeY + 5;
        float fieldX = this.getX() + 5 + changeX;

        // Draw selection highlight
        if ((this.selectedPosition.x > -1) && (this.selectedPosition.y > -1) && (this.selectedPosition.x != this.selectedPosition.y)) {
            int selStart = this.selectedPosition.x;
            int selEnd = this.selectedPosition.y;

            screen.getRenderer().begin(ShapeType.Filled);
            screen.getRenderer().setColor(SELECTED_COLOR[0], SELECTED_COLOR[1], SELECTED_COLOR[2], 0.3f);

            for (int i = 0; i < lineTexts.size(); i++) {
                float lineY = textStartY + i * lineHeight;
                if (lineY + lineHeight > this.getY() + changeY + this.getHeight()) break;

                int lineStart = lineStarts[i];
                int lineEnd = lineStart + lineTexts.get(i).length();

                if (selEnd <= lineStart || selStart >= lineEnd) continue;

                int hlStart = Math.max(selStart, lineStart) - lineStart;
                int hlEnd = Math.min(selEnd, lineEnd) - lineStart;

                String before = lineTexts.get(i).substring(0, hlStart);
                String selected = lineTexts.get(i).substring(hlStart, hlEnd);
                glyphLayout.setText(myFont, before.isEmpty() ? "" : before);
                float xStart = before.isEmpty() ? 0 : glyphLayout.width;
                glyphLayout.setText(myFont, selected);
                float selWidth = glyphLayout.width;

                screen.getRenderer().rect(fieldX + xStart, lineY, selWidth, lineHeight);
            }
            screen.getRenderer().end();
        }

        // Draw each line
        for (int i = 0; i < lineTexts.size(); i++) {
            float lineY = textStartY + i * lineHeight;
            if (lineY + lineHeight > this.getY() + changeY + this.getHeight()) break;
            String line = lineTexts.get(i);
            if (!line.isEmpty()) {
                screen.drawString(line, fieldX, lineY, color, myFont, false);
            }
        }

        // Draw cursor
        if ((this.isSelect()) && (this.bLineOn) && (this.bEnabled)) {
            try {
                int cursorLine = 0;
                int cursorCol = this.position;
                for (int i = lineStarts.length - 1; i >= 0; i--) {
                    if (this.position >= lineStarts[i]) {
                        cursorLine = i;
                        cursorCol = this.position - lineStarts[i];
                        break;
                    }
                }

                String lineText = lineTexts.get(cursorLine);
                String beforeCursor = lineText.substring(0, Math.min(cursorCol, lineText.length()));
                glyphLayout.setText(myFont, beforeCursor.isEmpty() ? " " : beforeCursor);
                float cursorH = glyphLayout.height;
                if (!beforeCursor.isEmpty()) {
                    glyphLayout.setText(myFont, beforeCursor);
                }
                float cursorX = this.getX() + 5 + (beforeCursor.isEmpty() ? 0 : glyphLayout.width) + changeX;
                float cursorY = textStartY + cursorLine * lineHeight;

                Gdx.gl20.glLineWidth(3f);
                screen.getRenderer().begin(ShapeType.Line);
                screen.getRenderer().setColor(color[0], color[1], color[2], 1f);
                screen.getRenderer().line(cursorX, cursorY, cursorX, cursorY + cursorH);
                screen.getRenderer().end();
                Gdx.gl20.glLineWidth(1f);
            } catch (Exception ex) {
            }
        }
    }

    private float getMultiLineHeight() {
        glyphLayout.setText(myFont, "Ag");
        return glyphLayout.height + 6;
    }

    /**
     * Wraps curString into display lines with word-wrap and explicit newlines.
     * Populates outLines with the text of each line.
     * Returns an int array of flat-string start positions for each line.
     */
    private int[] wrapText(List<String> outLines) {
        outLines.clear();
        List<Integer> starts = new ArrayList<>();

        if (curString == null || curString.isEmpty()) {
            outLines.add("");
            return new int[]{0};
        }

        float maxWidth = getWidth() - 10;
        int len = curString.length();
        int pos = 0;

        while (pos < len) {
            starts.add(pos);

            // Find the next explicit newline from current position
            int nlPos = curString.indexOf('\n', pos);
            int segEnd = (nlPos >= 0) ? nlPos : len;

            if (pos == segEnd) {
                // Empty line (newline at current position)
                outLines.add("");
                pos = segEnd + 1;
                continue;
            }

            // Check if text from pos to segEnd fits in one line
            String seg = curString.substring(pos, segEnd);
            glyphLayout.setText(myFont, seg);

            if (glyphLayout.width <= maxWidth) {
                // Fits entirely
                outLines.add(seg);
                pos = segEnd;
                if (pos < len && curString.charAt(pos) == '\n') {
                    pos++;
                }
            } else {
                // Need to wrap - find how many characters fit
                int fitEnd = pos;
                for (int i = pos + 1; i <= segEnd; i++) {
                    glyphLayout.setText(myFont, curString.substring(pos, i));
                    if (glyphLayout.width > maxWidth) {
                        break;
                    }
                    fitEnd = i;
                }

                // Try to break at the last space
                int breakAt = fitEnd;
                if (fitEnd < segEnd) {
                    for (int j = fitEnd; j > pos; j--) {
                        if (curString.charAt(j - 1) == ' ') {
                            breakAt = j;
                            break;
                        }
                    }
                }
                if (breakAt <= pos) {
                    breakAt = Math.max(pos + 1, fitEnd);
                }

                outLines.add(curString.substring(pos, breakAt));
                pos = breakAt;
            }
        }

        // Handle string ending with newline
        if (len > 0 && curString.charAt(len - 1) == '\n') {
            starts.add(len);
            outLines.add("");
        }

        if (outLines.isEmpty()) {
            outLines.add("");
            starts.add(0);
        }

        int[] result = new int[starts.size()];
        for (int i = 0; i < starts.size(); i++) {
            result[i] = starts.get(i);
        }
        return result;
    }

    private BitmapFont getCorrectFontforFunction() {
        if (this.fixedFont) {
            return this.myFont;
        }
        BitmapFont font = AssetLoader.font25;

        glyphLayout.setText(font, curString);
        float w = glyphLayout.width;

        if (w > this.getWidth() - 10) {
            font = AssetLoader.font20;
            glyphLayout.setText(myFont, curString);
            w = glyphLayout.width;
            if (w > this.getWidth() - 10) {
                font = AssetLoader.font15;
            }
        }

        return font;
    }

}
