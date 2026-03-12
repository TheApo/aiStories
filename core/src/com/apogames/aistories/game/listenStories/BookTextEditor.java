package com.apogames.aistories.game.listenStories;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.MainPanel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookTextEditor {

    private static final int TIME_LINE = 700;
    private static final int REPEAT_DELAY = 400;
    private static final int REPEAT_RATE = 50;
    private static final int MAX_LENGTH = 50000;

    private String rawText = "";
    private int cursorPos = 0;
    private final GridPoint2 selection = new GridPoint2(-1, -1);

    private boolean active = false;
    private boolean cursorVisible = true;
    private int blinkTimer = 0;

    private int heldKeyCode = -1;
    private int heldKeyTimer = 0;
    private boolean heldKeyRepeating = false;

    private int[] lineStarts = new int[]{0};
    private List<String> displayLines = new ArrayList<>();
    private Set<Integer> paddingLines = new HashSet<>();

    private boolean textChanged = false;
    private BitmapFont cachedFont;

    public boolean isActive() { return active; }
    public void setActive(boolean active) {
        this.active = active;
        if (active) {
            Gdx.input.setOnscreenKeyboardVisible(true);
            MainPanel.setActiveInput(BookRenderer.BOOK_Y, BookRenderer.getBookHeight());
        }
    }
    public String getRawText() { return rawText; }
    public int getCursorPos() { return cursorPos; }
    public boolean isTextChanged() { return textChanged; }
    public void clearTextChanged() { this.textChanged = false; }
    public void setFont(BitmapFont font) { this.cachedFont = font; }
    public List<String> getDisplayLines() { return displayLines; }

    public void setText(String text) {
        this.rawText = text != null ? text : "";
        this.cursorPos = 0;
        this.selection.set(-1, -1);
        this.textChanged = false;
    }

    public void setDisplayData(ArrayList<String> lines, int[] starts, Set<Integer> padding) {
        this.displayLines = new ArrayList<>(lines);
        this.lineStarts = starts;
        this.paddingLines = new HashSet<>(padding);
    }

    // --- Cursor line/position ---

    public int getDisplayLineForCursor() {
        if (lineStarts == null || lineStarts.length == 0) return 0;
        for (int i = lineStarts.length - 1; i >= 0; i--) {
            if (paddingLines.contains(i)) continue;
            if (cursorPos >= lineStarts[i]) return i;
        }
        return 0;
    }

    private int findNonPaddingLine(int from, int direction) {
        int line = from;
        while (line >= 0 && line < displayLines.size() && paddingLines.contains(line)) {
            line += direction;
        }
        if (line < 0 || line >= displayLines.size()) return -1;
        return line;
    }

    public float getCursorXOffset(BitmapFont font, BitmapFont headingFont, Set<Integer> chapterLines) {
        int line = getDisplayLineForCursor();
        if (line >= displayLines.size()) return 0;
        int col = cursorPos - lineStarts[line];
        String lineText = displayLines.get(line);
        col = Math.min(col, lineText.length());
        if (col <= 0) return 0;
        BitmapFont f = (chapterLines != null && chapterLines.contains(line)) ? headingFont : font;
        Constants.glyphLayout.setText(f, lineText.substring(0, col));
        return Constants.glyphLayout.width;
    }

    public int getSpreadForCursor(int rowsPerPage) {
        if (rowsPerPage <= 0) return 0;
        int line = getDisplayLineForCursor();
        return line / (2 * rowsPerPage);
    }

    // --- Text editing ---

    public void addTypedCharacter(char character) {
        if (!active) return;
        if (character == 8) {
            deleteSelectedOrBackspace();
        } else if (character == 127) {
            deleteSelectedOrForward();
        } else if (character == 10 || character == 13) {
            deleteSelectedText();
            if (rawText.length() < MAX_LENGTH) {
                rawText = rawText.substring(0, cursorPos) + '\n' + rawText.substring(cursorPos);
                cursorPos++;
                textChanged = true;
            }
        } else if (character >= 32) {
            deleteSelectedText();
            if (rawText.length() < MAX_LENGTH) {
                rawText = rawText.substring(0, cursorPos) + character + rawText.substring(cursorPos);
                cursorPos++;
                textChanged = true;
            }
        }
        showCursor();
    }

    private void deleteSelectedOrBackspace() {
        if (hasSelection()) {
            deleteSelectedText();
        } else if (cursorPos > 0) {
            rawText = rawText.substring(0, cursorPos - 1) + rawText.substring(cursorPos);
            cursorPos--;
            textChanged = true;
        }
        clearSelection();
    }

    private void deleteSelectedOrForward() {
        if (hasSelection()) {
            deleteSelectedText();
        } else if (cursorPos < rawText.length()) {
            rawText = rawText.substring(0, cursorPos) + rawText.substring(cursorPos + 1);
            textChanged = true;
        }
        clearSelection();
    }

    private void deleteSelectedText() {
        if (!hasSelection()) return;
        rawText = rawText.substring(0, selection.x) + rawText.substring(selection.y);
        cursorPos = selection.x;
        clearSelection();
        textChanged = true;
    }

    // --- Selection ---

    public boolean hasSelection() {
        return selection.x >= 0 && selection.y >= 0 && selection.x != selection.y;
    }

    public String getSelectedText() {
        if (!hasSelection()) return null;
        return rawText.substring(selection.x, selection.y);
    }

    public GridPoint2 getSelection() { return selection; }
    public void clearSelection() { selection.set(-1, -1); }

    public void selectAll() {
        selection.set(0, rawText.length());
        cursorPos = rawText.length();
    }

    // --- Navigation keys (keyDown) ---

    public boolean keyDown(int keyCode) {
        if (!active) return false;
        if (isNavigationKey(keyCode) && heldKeyCode != keyCode) {
            heldKeyCode = keyCode;
            heldKeyTimer = 0;
            heldKeyRepeating = false;
            handleNavigationKey(keyCode);
            Gdx.graphics.setContinuousRendering(true);
            Gdx.graphics.requestRendering();
            return true;
        }
        return false;
    }

    // --- Ctrl combos (keyUp) ---

    public boolean keyUp(int keyCode) {
        if (keyCode == heldKeyCode) {
            heldKeyCode = -1;
            Gdx.graphics.setContinuousRendering(false);
        }
        if (!active) return false;
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
            return handleCtrlKey(keyCode);
        }
        return false;
    }

    private boolean isNavigationKey(int keyCode) {
        return keyCode == Input.Keys.LEFT || keyCode == Input.Keys.RIGHT
                || keyCode == Input.Keys.UP || keyCode == Input.Keys.DOWN
                || keyCode == Input.Keys.HOME || keyCode == Input.Keys.END;
    }

    private boolean handleNavigationKey(int keyCode) {
        boolean shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        int oldPos = cursorPos;
        if (shift && selection.x < 0) selection.set(cursorPos, cursorPos);

        switch (keyCode) {
            case Input.Keys.LEFT:  setCursorPos(cursorPos - 1); break;
            case Input.Keys.RIGHT: setCursorPos(cursorPos + 1); break;
            case Input.Keys.HOME:  navigateHome(); break;
            case Input.Keys.END:   navigateEnd(); break;
            case Input.Keys.UP:    navigateUp(); break;
            case Input.Keys.DOWN:  navigateDown(); break;
            default: return false;
        }

        if (shift) {
            updateSelectionTo(cursorPos, oldPos);
        } else {
            clearSelection();
        }
        showCursor();
        return true;
    }

    private void updateSelectionTo(int newPos, int oldPos) {
        if (selection.x < 0) {
            selection.set(Math.min(oldPos, newPos), Math.max(oldPos, newPos));
        } else {
            if (oldPos == selection.y) {
                selection.y = newPos;
            } else {
                selection.x = newPos;
            }
            if (selection.x > selection.y) {
                int tmp = selection.x;
                selection.x = selection.y;
                selection.y = tmp;
            }
            if (selection.x == selection.y) clearSelection();
        }
    }

    private boolean handleCtrlKey(int keyCode) {
        switch (keyCode) {
            case Input.Keys.A:
                selectAll();
                return true;
            case Input.Keys.C:
                String selC = getSelectedText();
                if (selC != null && !selC.isEmpty()) Gdx.app.getClipboard().setContents(selC);
                return true;
            case Input.Keys.X:
                String selX = getSelectedText();
                if (selX != null && !selX.isEmpty()) {
                    Gdx.app.getClipboard().setContents(selX);
                    deleteSelectedText();
                }
                return true;
            case Input.Keys.V:
                String clip = Gdx.app.getClipboard().getContents();
                if (clip != null && !clip.isEmpty()) {
                    deleteSelectedText();
                    if (rawText.length() + clip.length() <= MAX_LENGTH) {
                        rawText = rawText.substring(0, cursorPos) + clip + rawText.substring(cursorPos);
                        cursorPos += clip.length();
                        clearSelection();
                        textChanged = true;
                    }
                }
                return true;
            default:
                return false;
        }
    }

    private void setCursorPos(int pos) {
        cursorPos = Math.max(0, Math.min(pos, rawText.length()));
    }

    private BitmapFont getDefaultFont() {
        return cachedFont != null ? cachedFont : FontSize.FONT_25.getFont();
    }

    private void navigateHome() {
        if (lineStarts == null) { setCursorPos(0); return; }
        int line = getDisplayLineForCursor();
        setCursorPos(lineStarts[line]);
    }

    private void navigateEnd() {
        if (lineStarts == null) { setCursorPos(rawText.length()); return; }
        int line = getDisplayLineForCursor();
        setCursorPos(lineStarts[line] + displayLines.get(line).length());
    }

    private void navigateUp() {
        if (lineStarts == null) { setCursorPos(0); return; }
        int line = getDisplayLineForCursor();
        int target = findNonPaddingLine(line - 1, -1);
        if (target < 0) { setCursorPos(0); return; }
        int col = cursorPos - lineStarts[line];
        float pixelX = getPixelXForCol(displayLines.get(line), col);
        int targetCol = getColForPixelX(displayLines.get(target), pixelX);
        setCursorPos(lineStarts[target] + targetCol);
    }

    private void navigateDown() {
        if (lineStarts == null) { setCursorPos(rawText.length()); return; }
        int line = getDisplayLineForCursor();
        int target = findNonPaddingLine(line + 1, +1);
        if (target < 0) { setCursorPos(rawText.length()); return; }
        int col = cursorPos - lineStarts[line];
        float pixelX = getPixelXForCol(displayLines.get(line), col);
        int targetCol = getColForPixelX(displayLines.get(target), pixelX);
        setCursorPos(lineStarts[target] + targetCol);
    }

    private float getPixelXForCol(String lineText, int col) {
        col = Math.min(col, lineText.length());
        if (col <= 0) return 0;
        Constants.glyphLayout.setText(getDefaultFont(), lineText.substring(0, col));
        return Constants.glyphLayout.width;
    }

    private int getColForPixelX(String lineText, float targetX) {
        if (lineText.isEmpty()) return 0;
        BitmapFont font = getDefaultFont();
        for (int i = 1; i <= lineText.length(); i++) {
            Constants.glyphLayout.setText(font, lineText.substring(0, i));
            if (Constants.glyphLayout.width >= targetX) {
                float right = Constants.glyphLayout.width;
                float left = 0;
                if (i > 1) {
                    Constants.glyphLayout.setText(font, lineText.substring(0, i - 1));
                    left = Constants.glyphLayout.width;
                }
                return (targetX - left < right - targetX) ? i - 1 : i;
            }
        }
        return lineText.length();
    }

    // --- Mouse ---

    public boolean handleClick(int mouseX, int mouseY, FontSize fontSize, int currentSpread,
                                int rowsPerPage, BitmapFont headingFont, Set<Integer> chapterLines) {
        if (!active) return false;
        int globalLine = mouseToGlobalLine(mouseX, mouseY, fontSize, currentSpread, rowsPerPage);
        if (globalLine < 0) return false;

        // Snap away from padding lines
        if (paddingLines.contains(globalLine)) {
            int nearest = findNonPaddingLine(globalLine, +1);
            if (nearest < 0) nearest = findNonPaddingLine(globalLine, -1);
            if (nearest < 0) return false;
            globalLine = nearest;
        }

        float relX = mouseToRelX(mouseX);
        String lineText = displayLines.get(globalLine);
        boolean isHeading = chapterLines != null && chapterLines.contains(globalLine);
        BitmapFont font = isHeading ? headingFont : fontSize.getFont();

        int col = getColForPixelXWithFont(lineText, relX, font);
        setCursorPos(lineStarts[globalLine] + col);
        clearSelection();
        showCursor();
        Gdx.graphics.requestRendering();
        return true;
    }

    public boolean handleDrag(int mouseX, int mouseY, FontSize fontSize, int currentSpread,
                               int rowsPerPage, BitmapFont headingFont, Set<Integer> chapterLines) {
        if (!active) return false;
        int globalLine = mouseToGlobalLine(mouseX, mouseY, fontSize, currentSpread, rowsPerPage);
        if (globalLine < 0) return false;

        if (paddingLines.contains(globalLine)) {
            int nearest = findNonPaddingLine(globalLine, +1);
            if (nearest < 0) nearest = findNonPaddingLine(globalLine, -1);
            if (nearest < 0) return false;
            globalLine = nearest;
        }

        float relX = mouseToRelX(mouseX);
        String lineText = displayLines.get(globalLine);
        boolean isHeading = chapterLines != null && chapterLines.contains(globalLine);
        BitmapFont font = isHeading ? headingFont : fontSize.getFont();

        int col = getColForPixelXWithFont(lineText, relX, font);
        int oldPos = cursorPos;
        int newPos = lineStarts[globalLine] + col;
        setCursorPos(newPos);

        if (selection.x < 0) selection.set(oldPos, oldPos);
        updateSelectionTo(newPos, oldPos);
        showCursor();
        Gdx.graphics.requestRendering();
        return true;
    }

    private int mouseToGlobalLine(int mouseX, int mouseY, FontSize fontSize, int currentSpread, int rowsPerPage) {
        int leftPageX = BookRenderer.LEFT_PAGE_X;
        int rightPageX = BookRenderer.RIGHT_PAGE_X;
        int pageY = BookRenderer.LEFT_PAGE_Y;
        int pageWidth = BookRenderer.PAGE_WIDTH;
        int pageHeight = BookRenderer.getPageHeight();

        boolean onLeft = mouseX >= leftPageX && mouseX < leftPageX + pageWidth
                && mouseY >= pageY && mouseY < pageY + pageHeight;
        boolean onRight = mouseX >= rightPageX && mouseX < rightPageX + pageWidth
                && mouseY >= pageY && mouseY < pageY + pageHeight;
        if (!onLeft && !onRight) return -1;

        float relY = mouseY - pageY - BookRenderer.TEXT_PADDING;
        int lineOnPage = Math.max(0, Math.min((int)(relY / fontSize.getAdd()), rowsPerPage - 1));

        int leftStart = currentSpread * 2 * rowsPerPage;
        int globalLine = leftStart + lineOnPage;
        if (onRight) globalLine += rowsPerPage;

        if (globalLine >= displayLines.size()) globalLine = displayLines.size() - 1;
        return globalLine >= 0 ? globalLine : -1;
    }

    private float mouseToRelX(int mouseX) {
        int leftPageX = BookRenderer.LEFT_PAGE_X;
        int rightPageX = BookRenderer.RIGHT_PAGE_X;
        int pageWidth = BookRenderer.PAGE_WIDTH;
        boolean onLeft = mouseX >= leftPageX && mouseX < leftPageX + pageWidth;
        return mouseX - (onLeft ? leftPageX : rightPageX) - BookRenderer.TEXT_PADDING;
    }

    private int getColForPixelXWithFont(String lineText, float targetX, BitmapFont font) {
        if (lineText.isEmpty() || targetX <= 0) return 0;
        for (int i = 1; i <= lineText.length(); i++) {
            Constants.glyphLayout.setText(font, lineText.substring(0, i));
            if (Constants.glyphLayout.width >= targetX) {
                float right = Constants.glyphLayout.width;
                float left = 0;
                if (i > 1) {
                    Constants.glyphLayout.setText(font, lineText.substring(0, i - 1));
                    left = Constants.glyphLayout.width;
                }
                return (targetX - left < right - targetX) ? i - 1 : i;
            }
        }
        return lineText.length();
    }

    // --- Cursor blink ---

    public void think(int delta) {
        if (!active) return;
        blinkTimer += delta;
        if (blinkTimer >= TIME_LINE) {
            blinkTimer = 0;
            cursorVisible = !cursorVisible;
            Gdx.graphics.requestRendering();
        }
        if (heldKeyCode >= 0) {
            heldKeyTimer += delta;
            if (!heldKeyRepeating && heldKeyTimer >= REPEAT_DELAY) {
                heldKeyRepeating = true;
                heldKeyTimer = 0;
            }
            if (heldKeyRepeating && heldKeyTimer >= REPEAT_RATE) {
                heldKeyTimer -= REPEAT_RATE;
                handleNavigationKey(heldKeyCode);
            }
        }
    }

    private void showCursor() {
        cursorVisible = true;
        blinkTimer = 0;
    }

    // --- Rendering ---

    public void renderCursor(ShapeRenderer renderer, FontSize fontSize, int currentSpread,
                              int rowsPerPage, BitmapFont headingFont, Set<Integer> chapterLines) {
        if (!active || !cursorVisible) return;
        if (lineStarts == null || displayLines.isEmpty()) return;

        int line = getDisplayLineForCursor();
        int leftStart = currentSpread * 2 * rowsPerPage;
        int rightStart = leftStart + rowsPerPage;

        boolean isLeftPage;
        int localRow;

        if (line >= leftStart && line < leftStart + rowsPerPage) {
            isLeftPage = true;
            localRow = line - leftStart;
        } else if (line >= rightStart && line < rightStart + rowsPerPage) {
            isLeftPage = false;
            localRow = line - rightStart;
        } else {
            return;
        }

        int pageX = isLeftPage ? BookRenderer.LEFT_PAGE_X : BookRenderer.RIGHT_PAGE_X;
        int pageY = isLeftPage ? BookRenderer.LEFT_PAGE_Y : BookRenderer.RIGHT_PAGE_Y;

        float xOffset = getCursorXOffset(fontSize.getFont(), headingFont, chapterLines);
        float x = pageX + BookRenderer.TEXT_PADDING + xOffset;
        float y = pageY + BookRenderer.TEXT_PADDING + localRow * fontSize.getAdd();

        Gdx.gl20.glLineWidth(3f);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(Constants.COLOR_BROWN[0], Constants.COLOR_BROWN[1], Constants.COLOR_BROWN[2], 1f);
        renderer.line(x, y, x, y + fontSize.getFont().getCapHeight());
        renderer.end();
        Gdx.gl20.glLineWidth(1f);
    }

    public void renderSelection(ShapeRenderer renderer, FontSize fontSize, int currentSpread,
                                 int rowsPerPage, BitmapFont headingFont, Set<Integer> chapterLines) {
        if (!active || !hasSelection()) return;
        if (lineStarts == null || displayLines.isEmpty()) return;

        int leftStart = currentSpread * 2 * rowsPerPage;
        int rightStart = leftStart + rowsPerPage;
        int visibleEnd = rightStart + rowsPerPage;

        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(0f, 150f / 255f, 255f / 255f, 0.3f);

        for (int i = 0; i < displayLines.size(); i++) {
            if (i < leftStart || i >= visibleEnd) continue;
            if (paddingLines.contains(i)) continue;

            int lineStart = lineStarts[i];
            int lineEnd = lineStart + displayLines.get(i).length();
            if (selection.y <= lineStart || selection.x >= lineEnd) continue;

            boolean isLeftPage = (i >= leftStart && i < rightStart);
            int localRow = isLeftPage ? (i - leftStart) : (i - rightStart);

            int pageX = isLeftPage ? BookRenderer.LEFT_PAGE_X : BookRenderer.RIGHT_PAGE_X;
            int pageY = isLeftPage ? BookRenderer.LEFT_PAGE_Y : BookRenderer.RIGHT_PAGE_Y;

            int hlStart = Math.max(selection.x, lineStart) - lineStart;
            int hlEnd = Math.min(selection.y, lineEnd) - lineStart;

            boolean isHeading = chapterLines != null && chapterLines.contains(i);
            BitmapFont font = isHeading ? headingFont : fontSize.getFont();

            String lineText = displayLines.get(i);
            String before = lineText.substring(0, hlStart);
            String selected = lineText.substring(hlStart, hlEnd);
            float xStart = 0;
            if (!before.isEmpty()) {
                Constants.glyphLayout.setText(font, before);
                xStart = Constants.glyphLayout.width;
            }
            Constants.glyphLayout.setText(font, selected);
            float selWidth = Constants.glyphLayout.width;

            renderer.rect(pageX + BookRenderer.TEXT_PADDING + xStart,
                    pageY + BookRenderer.TEXT_PADDING + localRow * fontSize.getAdd(),
                    selWidth, fontSize.getAdd());
        }

        renderer.end();
    }
}
