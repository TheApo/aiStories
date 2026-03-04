package com.apogames.aistories.game.listenStories;

import com.apogames.aistories.Constants;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WordHighlighter {

    private static final float UNDERLINE_HEIGHT = 2f;

    private final WordTimingData timingData;
    private final List<WordPosition> positions = new ArrayList<>();

    @Getter
    private int currentWordIndex = -1;

    public WordHighlighter(WordTimingData timingData) {
        this.timingData = timingData;
    }

    public void buildMapping(ArrayList<String> displayLines, BitmapFont bodyFont,
                             BitmapFont headingFont, Set<Integer> chapterLines) {
        positions.clear();
        List<WordTimingData.WordTiming> ttsWords = timingData.getWords();
        int ttsIndex = 0;

        for (int lineIdx = 0; lineIdx < displayLines.size() && ttsIndex < ttsWords.size(); lineIdx++) {
            String line = displayLines.get(lineIdx);
            if (line.trim().isEmpty()) continue;

            boolean isHeading = chapterLines.contains(lineIdx);
            BitmapFont font = isHeading ? headingFont : bodyFont;

            String[] lineWords = line.split("\\s+");
            float xOffset = 0;

            for (String displayWord : lineWords) {
                if (displayWord.isEmpty()) continue;
                if (ttsIndex >= ttsWords.size()) break;

                Constants.glyphLayout.setText(font, displayWord);
                float wordWidth = Constants.glyphLayout.width;
                positions.add(new WordPosition(lineIdx, xOffset, wordWidth));
                ttsIndex++;

                // Advance xOffset by word width + space
                Constants.glyphLayout.setText(font, displayWord + " ");
                xOffset += Constants.glyphLayout.width;
            }
        }

        // Fill remaining TTS words with no-position entries
        while (positions.size() < ttsWords.size()) {
            positions.add(new WordPosition(-1, 0, 0));
        }
    }

    public void update(float playbackSeconds) {
        currentWordIndex = timingData.getCurrentWordIndex(playbackSeconds);
    }

    public int getCurrentLine() {
        if (currentWordIndex < 0 || currentWordIndex >= positions.size()) return -1;
        return positions.get(currentWordIndex).lineIndex;
    }

    public void renderHighlight(ShapeRenderer renderer, FontSize fontSize,
                                int leftStartLine, int rowsPerPage) {
        if (currentWordIndex < 0 || currentWordIndex >= positions.size()) return;

        WordPosition pos = positions.get(currentWordIndex);
        if (pos.lineIndex < 0 || pos.width <= 0) return;

        int rightStartLine = leftStartLine + rowsPerPage;
        boolean isLeftPage;
        int localRow;

        if (pos.lineIndex >= leftStartLine && pos.lineIndex < leftStartLine + rowsPerPage) {
            isLeftPage = true;
            localRow = pos.lineIndex - leftStartLine;
        } else if (pos.lineIndex >= rightStartLine && pos.lineIndex < rightStartLine + rowsPerPage) {
            isLeftPage = false;
            localRow = pos.lineIndex - rightStartLine;
        } else {
            return; // Not on visible pages
        }

        int pageX = isLeftPage ? BookRenderer.LEFT_PAGE_X : BookRenderer.RIGHT_PAGE_X;
        int pageY = isLeftPage ? BookRenderer.LEFT_PAGE_Y : BookRenderer.RIGHT_PAGE_Y;

        float x = pageX + BookRenderer.TEXT_PADDING + pos.xStart;
        float y = pageY + BookRenderer.TEXT_PADDING + localRow * fontSize.getAdd();
        float w = pos.width;
        float pad = 2f;

        // Black underline directly below text glyphs
        float underlineY = y + fontSize.getFont().getCapHeight() + 2;
        renderer.setColor(0f, 0f, 0f, 0.8f);
        renderer.rect(x - pad, underlineY, w + 2 * pad, UNDERLINE_HEIGHT);
    }

    private static class WordPosition {
        final int lineIndex;
        final float xStart;
        final float width;

        WordPosition(int lineIndex, float xStart, float width) {
            this.lineIndex = lineIndex;
            this.xStart = xStart;
            this.width = width;
        }
    }
}
