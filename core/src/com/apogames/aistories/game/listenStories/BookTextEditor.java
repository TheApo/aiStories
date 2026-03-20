package com.apogames.aistories.game.listenStories;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.MainPanel;
import com.apogames.backend.GameScreen;
import com.apogames.entity.Textfield;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GridPoint2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookTextEditor extends Textfield {

    private static final int BOOK_MAX_LENGTH = 50000;

    private int[] lineStarts = new int[]{0};
    private List<String> displayLines = new ArrayList<>();
    private Set<Integer> paddingLines = new HashSet<>();
    private boolean textChanged = false;

    // Render context (set by caller before render)
    private FontSize renderFontSize;
    private int renderCurrentSpread;
    private int renderRowsPerPage;
    private BitmapFont renderHeadingFont;
    private Set<Integer> renderChapterLines;

    public BookTextEditor() {
        super(0, 0, 0, 0, null);
        this.curString = "";
        this.position = 0;
        this.selectedPosition = new GridPoint2(-1, -1);
        this.maxLength = BOOK_MAX_LENGTH;
        this.multiLine = true;
        this.bLineOn = true;
        this.time = 0;
        setFixedFont(true);
    }

    // --- API ---

    public boolean isActive() { return isSelect(); }
    public void setActive(boolean active) { setSelect(active); }
    public String getRawText() { return getCurString(); }
    public boolean isTextChanged() { return textChanged; }
    public void clearTextChanged() { textChanged = false; }
    public List<String> getDisplayLines() { return displayLines; }

    public void setText(String text) {
        this.curString = text != null ? text : "";
        this.position = 0;
        clearSelection();
        this.textChanged = false;
    }

    public void setDisplayData(ArrayList<String> lines, int[] starts, Set<Integer> padding) {
        this.displayLines = new ArrayList<>(lines);
        this.lineStarts = starts;
        this.paddingLines = new HashSet<>(padding);
    }

    public boolean hasSelection() {
        return selectedPosition.x >= 0 && selectedPosition.y >= 0
                && selectedPosition.x != selectedPosition.y;
    }

    // --- Select override for book coordinates ---

    @Override
    protected void onBecameSelected() {
        // Don't open keyboard on setSelect - only on explicit click in handleClick
    }

    // --- Text editing with textChanged tracking ---

    @Override
    public void addTypedCharacter(char character) {
        String before = this.curString;
        super.addTypedCharacter(character);
        if (!this.curString.equals(before)) {
            textChanged = true;
        }
    }

    @Override
    protected boolean handleCtrlKey(int keyCode) {
        String before = this.curString;
        boolean result = super.handleCtrlKey(keyCode);
        if (!this.curString.equals(before)) {
            textChanged = true;
        }
        return result;
    }

    // --- Navigation with book display lines ---

    @Override
    protected void navigateHome() {
        if (lineStarts == null) { setPosition(0); return; }
        int line = getDisplayLineForCursor();
        setPosition(lineStarts[line]);
    }

    @Override
    protected void navigateEnd() {
        if (lineStarts == null) { setPosition(curString.length()); return; }
        int line = getDisplayLineForCursor();
        setPosition(lineStarts[line] + displayLines.get(line).length());
    }

    @Override
    protected void navigateUp() {
        if (lineStarts == null) { setPosition(0); return; }
        int line = getDisplayLineForCursor();
        int target = findNonPaddingLine(line - 1, -1);
        if (target < 0) { setPosition(0); return; }
        int col = position - lineStarts[line];
        float pixelX = getPixelXForCol(displayLines.get(line), col);
        int targetCol = getColForPixelX(displayLines.get(target), pixelX);
        setPosition(lineStarts[target] + targetCol);
    }

    @Override
    protected void navigateDown() {
        if (lineStarts == null) { setPosition(curString.length()); return; }
        int line = getDisplayLineForCursor();
        int target = findNonPaddingLine(line + 1, +1);
        if (target < 0) { setPosition(curString.length()); return; }
        int col = position - lineStarts[line];
        float pixelX = getPixelXForCol(displayLines.get(line), col);
        int targetCol = getColForPixelX(displayLines.get(target), pixelX);
        setPosition(lineStarts[target] + targetCol);
    }

    private int findNonPaddingLine(int from, int direction) {
        int line = from;
        while (line >= 0 && line < displayLines.size() && paddingLines.contains(line)) {
            line += direction;
        }
        return (line < 0 || line >= displayLines.size()) ? -1 : line;
    }

    // --- Display line helpers ---

    public int getDisplayLineForCursor() {
        if (lineStarts == null || lineStarts.length == 0) return 0;
        for (int i = lineStarts.length - 1; i >= 0; i--) {
            if (paddingLines.contains(i)) continue;
            if (position >= lineStarts[i]) return i;
        }
        return 0;
    }

    public float getCursorXOffset(BitmapFont font, BitmapFont headingFont, Set<Integer> chapterLines) {
        int line = getDisplayLineForCursor();
        if (line >= displayLines.size()) return 0;
        int col = position - lineStarts[line];
        String lineText = displayLines.get(line);
        col = Math.min(col, lineText.length());
        if (col <= 0) return 0;
        BitmapFont f = (chapterLines != null && chapterLines.contains(line)) ? headingFont : font;
        Constants.glyphLayout.setText(f, lineText.substring(0, col));
        return Constants.glyphLayout.width;
    }

    public int getSpreadForCursor(int rowsPerPage) {
        if (rowsPerPage <= 0) return 0;
        return getDisplayLineForCursor() / (2 * rowsPerPage);
    }

    // --- Mouse handling for book pages ---

    public boolean handleClick(int mouseX, int mouseY, FontSize fontSize, int currentSpread,
                                int rowsPerPage, BitmapFont headingFont, Set<Integer> chapterLines) {
        if (!isSelect()) return false;
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
        setPosition(lineStarts[globalLine] + col);
        clearSelection();
        Gdx.input.setOnscreenKeyboardVisible(true);
        MainPanel.setActiveInput(BookRenderer.BOOK_Y, BookRenderer.getBookHeight());
        Gdx.graphics.requestRendering();
        return true;
    }

    public boolean handleDrag(int mouseX, int mouseY, FontSize fontSize, int currentSpread,
                               int rowsPerPage, BitmapFont headingFont, Set<Integer> chapterLines) {
        if (!isSelect()) return false;
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
        int oldPos = position;
        int newPos = lineStarts[globalLine] + col;
        setPosition(newPos);

        if (selectedPosition.x < 0) selectedPosition.set(oldPos, oldPos);
        updateSelectionTo(newPos, oldPos);
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

    // --- Think override ---

    @Override
    public void think(int delta) {
        if (!isSelect()) return;
        boolean wasBlink = bLineOn;
        super.think(delta);
        if (bLineOn != wasBlink) {
            Gdx.graphics.requestRendering();
        }
    }

    // --- Render context ---

    public void setRenderContext(FontSize fontSize, int currentSpread, int rowsPerPage,
                                  BitmapFont headingFont, Set<Integer> chapterLines) {
        this.renderFontSize = fontSize;
        this.renderCurrentSpread = currentSpread;
        this.renderRowsPerPage = rowsPerPage;
        this.renderHeadingFont = headingFont;
        this.renderChapterLines = chapterLines;
    }

    @Override
    public void render(GameScreen screen, int changeX, int changeY) {
        if (!isSelect() || renderFontSize == null) return;
        if (lineStarts == null || displayLines.isEmpty()) return;

        ShapeRenderer renderer = screen.getRenderer();

        if (hasSelection()) {
            renderSelection(renderer);
        }
        if (bLineOn) {
            renderCursor(renderer);
        }
    }

    private void renderCursor(ShapeRenderer renderer) {
        int line = getDisplayLineForCursor();
        int leftStart = renderCurrentSpread * 2 * renderRowsPerPage;
        int rightStart = leftStart + renderRowsPerPage;

        boolean isLeftPage;
        int localRow;

        if (line >= leftStart && line < leftStart + renderRowsPerPage) {
            isLeftPage = true;
            localRow = line - leftStart;
        } else if (line >= rightStart && line < rightStart + renderRowsPerPage) {
            isLeftPage = false;
            localRow = line - rightStart;
        } else {
            return;
        }

        int pageX = isLeftPage ? BookRenderer.LEFT_PAGE_X : BookRenderer.RIGHT_PAGE_X;
        int pageY = isLeftPage ? BookRenderer.LEFT_PAGE_Y : BookRenderer.RIGHT_PAGE_Y;

        float xOffset = getCursorXOffset(renderFontSize.getFont(), renderHeadingFont, renderChapterLines);
        float x = pageX + BookRenderer.TEXT_PADDING + xOffset;
        float y = pageY + BookRenderer.TEXT_PADDING + localRow * renderFontSize.getAdd();

        Gdx.gl20.glLineWidth(3f);
        renderer.begin(ShapeRenderer.ShapeType.Line);
        renderer.setColor(Constants.COLOR_BROWN[0], Constants.COLOR_BROWN[1], Constants.COLOR_BROWN[2], 1f);
        renderer.line(x, y, x, y + renderFontSize.getFont().getCapHeight());
        renderer.end();
        Gdx.gl20.glLineWidth(1f);
    }

    private void renderSelection(ShapeRenderer renderer) {
        int leftStart = renderCurrentSpread * 2 * renderRowsPerPage;
        int rightStart = leftStart + renderRowsPerPage;
        int visibleEnd = rightStart + renderRowsPerPage;

        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.setColor(0f, 150f / 255f, 255f / 255f, 0.3f);

        for (int i = 0; i < displayLines.size(); i++) {
            if (i < leftStart || i >= visibleEnd) continue;
            if (paddingLines.contains(i)) continue;

            int lineStart = lineStarts[i];
            int lineEnd = lineStart + displayLines.get(i).length();
            if (selectedPosition.y <= lineStart || selectedPosition.x >= lineEnd) continue;

            boolean isLeftPage = (i >= leftStart && i < rightStart);
            int localRow = isLeftPage ? (i - leftStart) : (i - rightStart);

            int pageX = isLeftPage ? BookRenderer.LEFT_PAGE_X : BookRenderer.RIGHT_PAGE_X;
            int pageY = isLeftPage ? BookRenderer.LEFT_PAGE_Y : BookRenderer.RIGHT_PAGE_Y;

            int hlStart = Math.max(selectedPosition.x, lineStart) - lineStart;
            int hlEnd = Math.min(selectedPosition.y, lineEnd) - lineStart;

            boolean isHeading = renderChapterLines != null && renderChapterLines.contains(i);
            BitmapFont font = isHeading ? renderHeadingFont : renderFontSize.getFont();

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
                    pageY + BookRenderer.TEXT_PADDING + localRow * renderFontSize.getAdd(),
                    selWidth, renderFontSize.getAdd());
        }

        renderer.end();
    }
}
