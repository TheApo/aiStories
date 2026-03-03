package com.apogames.aistories.game.listenStories;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.MainPanel;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.DrawString;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BookRenderer {

    public static final int BOOK_X = 60;
    public static final int BOOK_Y = 90;
    public static final int BOOK_WIDTH = 1280;
    public static final int BOOK_HEIGHT = 480;
    public static final int SPINE_WIDTH = 2;
    public static final int PAGE_MARGIN = 10;
    public static final int TEXT_PADDING = 15;

    public static final int SPINE_X = BOOK_X + BOOK_WIDTH / 2;

    public static final int LEFT_PAGE_X = BOOK_X + PAGE_MARGIN;
    public static final int LEFT_PAGE_Y = BOOK_Y + PAGE_MARGIN;
    public static final int PAGE_WIDTH = (BOOK_WIDTH / 2) - PAGE_MARGIN - (SPINE_WIDTH / 2);
    public static final int PAGE_HEIGHT = BOOK_HEIGHT - 2 * PAGE_MARGIN;

    public static final int RIGHT_PAGE_X = SPINE_X + SPINE_WIDTH / 2;
    public static final int RIGHT_PAGE_Y = BOOK_Y + PAGE_MARGIN;

    public static final int TEXT_WIDTH = PAGE_WIDTH - 2 * TEXT_PADDING;

    private final MainPanel mainPanel;
    private final Set<Integer> chapterLines = new HashSet<>();

    public Set<Integer> getChapterLines() { return chapterLines; }

    public BookRenderer(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public int getRowsPerPage(FontSize fontSize) {
        return (PAGE_HEIGHT - 30) / fontSize.getAdd();
    }

    public int getTextAreaWidth() {
        // TextArea subtracts 20 internally for wrapping, so width - 20 = TEXT_WIDTH
        return TEXT_WIDTH + 20;
    }

    public void renderBookFrame() {
        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        mainPanel.getRenderer().begin(ShapeRenderer.ShapeType.Filled);

        // Book cover (brown leather)
        mainPanel.getRenderer().setColor(Constants.COLOR_BROWN[0], Constants.COLOR_BROWN[1], Constants.COLOR_BROWN[2], 1f);
        mainPanel.getRenderer().roundedRect(BOOK_X, BOOK_Y, BOOK_WIDTH, BOOK_HEIGHT, 8);

        // Left page (warm parchment)
        mainPanel.getRenderer().setColor(Constants.COLOR_PAGE[0], Constants.COLOR_PAGE[1], Constants.COLOR_PAGE[2], 1f);
        mainPanel.getRenderer().rect(LEFT_PAGE_X, LEFT_PAGE_Y, PAGE_WIDTH, PAGE_HEIGHT);

        // Right page (warm parchment)
        mainPanel.getRenderer().setColor(Constants.COLOR_PAGE[0], Constants.COLOR_PAGE[1], Constants.COLOR_PAGE[2], 1f);
        mainPanel.getRenderer().rect(RIGHT_PAGE_X, RIGHT_PAGE_Y, PAGE_WIDTH, PAGE_HEIGHT);

        // Spine (dark brown)
        mainPanel.getRenderer().setColor(Constants.COLOR_SPINE[0], Constants.COLOR_SPINE[1], Constants.COLOR_SPINE[2], 1f);
        mainPanel.getRenderer().rect(SPINE_X - SPINE_WIDTH / 2f, BOOK_Y, SPINE_WIDTH, BOOK_HEIGHT);

        // Spine shadow on left page
        mainPanel.getRenderer().setColor(0f, 0f, 0f, 0.08f);
        mainPanel.getRenderer().rect(SPINE_X - SPINE_WIDTH / 2f - 4, LEFT_PAGE_Y, 4, PAGE_HEIGHT);
        mainPanel.getRenderer().setColor(0f, 0f, 0f, 0.04f);
        mainPanel.getRenderer().rect(SPINE_X - SPINE_WIDTH / 2f - 10, LEFT_PAGE_Y, 6, PAGE_HEIGHT);

        // Spine shadow on right page
        mainPanel.getRenderer().setColor(0f, 0f, 0f, 0.08f);
        mainPanel.getRenderer().rect(SPINE_X + SPINE_WIDTH / 2f, RIGHT_PAGE_Y, 4, PAGE_HEIGHT);
        mainPanel.getRenderer().setColor(0f, 0f, 0f, 0.04f);
        mainPanel.getRenderer().rect(SPINE_X + SPINE_WIDTH / 2f + 4, RIGHT_PAGE_Y, 6, PAGE_HEIGHT);

        mainPanel.getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        // Page borders (brown)
        mainPanel.getRenderer().begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl20.glLineWidth(1f);
        mainPanel.getRenderer().setColor(Constants.COLOR_BROWN[0], Constants.COLOR_BROWN[1], Constants.COLOR_BROWN[2], 0.4f);
        mainPanel.getRenderer().rect(LEFT_PAGE_X, LEFT_PAGE_Y, PAGE_WIDTH, PAGE_HEIGHT);
        mainPanel.getRenderer().rect(RIGHT_PAGE_X, RIGHT_PAGE_Y, PAGE_WIDTH, PAGE_HEIGHT);
        mainPanel.getRenderer().end();
    }

    public static boolean isChapterHeading(String line) {
        if (line == null || line.trim().isEmpty()) return false;
        String lower = line.trim().toLowerCase();
        return lower.startsWith("kapitel") || lower.startsWith("chapter")
                || lower.startsWith("chapitre") || lower.startsWith("capitolo")
                || lower.startsWith("capítulo") || lower.startsWith("bölüm");
    }

    public void renderPageText(ArrayList<String> lines, int startLine, int rows, FontSize fontSize, boolean isLeftPage) {
        int pageX = isLeftPage ? LEFT_PAGE_X : RIGHT_PAGE_X;
        int pageY = isLeftPage ? LEFT_PAGE_Y : RIGHT_PAGE_Y;

        for (int i = 0; i < rows && (startLine + i) < lines.size(); i++) {
            String line = lines.get(startLine + i);
            boolean isHeading = chapterLines.contains(startLine + i);
            mainPanel.drawString(line,
                    pageX + TEXT_PADDING,
                    pageY + TEXT_PADDING + i * fontSize.getAdd(),
                    Constants.COLOR_BROWN,
                    isHeading ? fontSize.getNext(1).getFont() : fontSize.getFont(),
                    DrawString.BEGIN, false, false);
        }
    }

    public void renderPageNumbers(int leftPageNum, int rightPageNum, int totalPages, FontSize fontSize) {
        mainPanel.drawString("- " + leftPageNum + " -",
                LEFT_PAGE_X + PAGE_WIDTH / 2f,
                LEFT_PAGE_Y + PAGE_HEIGHT - 25,
                Constants.COLOR_BROWN, AssetLoader.font15, DrawString.MIDDLE, false, false);

        if (rightPageNum <= totalPages) {
            mainPanel.drawString("- " + rightPageNum + " -",
                    RIGHT_PAGE_X + PAGE_WIDTH / 2f,
                    RIGHT_PAGE_Y + PAGE_HEIGHT - 25,
                    Constants.COLOR_BROWN, AssetLoader.font15, DrawString.MIDDLE, false, false);
        }
    }

    /**
     * Renders the page turn animation. The fold sweeps across the ENTIRE book width,
     * from the right edge all the way to the left edge, revealing new content underneath.
     */
    public void renderTurnAnimation(PageTurnAnimation animation, ArrayList<String> lines,
                                     int oldLeftStart, int oldRightStart,
                                     int newRightStart, int newLeftStart,
                                     int rowsPerPage, FontSize fontSize) {
        float progress = animation.getProgress();
        boolean isForward = animation.getDirection() == PageTurnAnimation.Direction.FORWARD;

        if (isForward) {
            renderForwardTurn(progress, lines, oldLeftStart, oldRightStart, newLeftStart, newRightStart, rowsPerPage, fontSize);
        } else {
            renderBackwardTurn(progress, lines, oldLeftStart, oldRightStart, newLeftStart, newRightStart, rowsPerPage, fontSize);
        }
    }

    private void renderForwardTurn(float progress, ArrayList<String> lines,
                                    int oldLeftStart, int oldRightStart,
                                    int newLeftStart, int newRightStart,
                                    int rowsPerPage, FontSize fontSize) {
        float rightEdge = RIGHT_PAGE_X + PAGE_WIDTH;

        if (progress <= 0.5f) {
            // Phase 1: fold sweeps across right page (right edge → spine)
            float p = progress / 0.5f;
            float foldX = rightEdge - p * PAGE_WIDTH;

            float liftedWidth = p * PAGE_WIDTH;
            float flapWidth = Math.min(liftedWidth * 0.45f, foldX - RIGHT_PAGE_X);
            if (flapWidth < 0) flapWidth = 0;
            float flapLeft = foldX - flapWidth;

            // Step 1: text
            // New right text revealed from foldX to rightEdge
            float revealedWidth = rightEdge - foldX;
            if (revealedWidth > 1) {
                clipAndRenderText(lines, newRightStart, rowsPerPage, fontSize, false,
                        foldX, RIGHT_PAGE_Y, revealedWidth, PAGE_HEIGHT);
            }
            // Left page: full old content
            renderPageText(lines, oldLeftStart, rowsPerPage, fontSize, true);

            // Step 2: shapes
            mainPanel.spriteBatch.end();
            Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
            mainPanel.getRenderer().begin(ShapeRenderer.ShapeType.Filled);

            // Flat overlay on right page (RIGHT_PAGE_X to flapLeft)
            if (flapLeft > RIGHT_PAGE_X + 1) {
                mainPanel.getRenderer().setColor(Constants.COLOR_PAGE[0], Constants.COLOR_PAGE[1], Constants.COLOR_PAGE[2], 1f);
                mainPanel.getRenderer().rect(RIGHT_PAGE_X, RIGHT_PAGE_Y, flapLeft - RIGHT_PAGE_X, PAGE_HEIGHT);
            }
            renderFlap(flapLeft, flapWidth, foldX, RIGHT_PAGE_Y, true);
            renderFoldShadow(foldX, RIGHT_PAGE_Y, PAGE_HEIGHT);

            mainPanel.getRenderer().end();
            Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);
            mainPanel.spriteBatch.begin();

            // Step 3: old right text clipped to flat area
            float flatWidth = flapLeft - RIGHT_PAGE_X;
            if (flatWidth > 1) {
                clipAndRenderText(lines, oldRightStart, rowsPerPage, fontSize, false,
                        RIGHT_PAGE_X, RIGHT_PAGE_Y, flatWidth, PAGE_HEIGHT);
            }

        } else {
            // Phase 2: page settles on left page (spine → left edge)
            float p = (progress - 0.5f) / 0.5f;
            float leftSpineEdge = LEFT_PAGE_X + PAGE_WIDTH;
            float foldX = leftSpineEdge - p * PAGE_WIDTH;

            float settledWidth = p * PAGE_WIDTH;
            float unsettledWidth = foldX - LEFT_PAGE_X;
            float flapWidth = Math.min(unsettledWidth * 0.45f, settledWidth * 0.45f);
            if (flapWidth < 0) flapWidth = 0;
            float flapLeft = foldX - flapWidth;

            // Step 1: text
            // New right page: full (fold has passed)
            renderPageText(lines, newRightStart, rowsPerPage, fontSize, false);
            // New left text revealed from foldX to spine edge
            float revealedWidth = leftSpineEdge - foldX;
            if (revealedWidth > 1) {
                clipAndRenderText(lines, newLeftStart, rowsPerPage, fontSize, true,
                        foldX, LEFT_PAGE_Y, revealedWidth, PAGE_HEIGHT);
            }

            // Step 2: shapes
            mainPanel.spriteBatch.end();
            Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
            mainPanel.getRenderer().begin(ShapeRenderer.ShapeType.Filled);

            // Flat overlay on left page (LEFT_PAGE_X to flapLeft) — covers new text so old can show on top
            if (flapLeft > LEFT_PAGE_X + 1) {
                mainPanel.getRenderer().setColor(Constants.COLOR_PAGE[0], Constants.COLOR_PAGE[1], Constants.COLOR_PAGE[2], 1f);
                mainPanel.getRenderer().rect(LEFT_PAGE_X, LEFT_PAGE_Y, flapLeft - LEFT_PAGE_X, PAGE_HEIGHT);
            }
            renderFlap(flapLeft, flapWidth, foldX, LEFT_PAGE_Y, true);
            renderFoldShadow(foldX, LEFT_PAGE_Y, PAGE_HEIGHT);

            mainPanel.getRenderer().end();
            Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);
            mainPanel.spriteBatch.begin();

            // Step 3: old left text clipped to flat area
            float flatWidth = flapLeft - LEFT_PAGE_X;
            if (flatWidth > 1) {
                clipAndRenderText(lines, oldLeftStart, rowsPerPage, fontSize, true,
                        LEFT_PAGE_X, LEFT_PAGE_Y, flatWidth, PAGE_HEIGHT);
            }
        }
    }

    private void renderBackwardTurn(float progress, ArrayList<String> lines,
                                     int oldLeftStart, int oldRightStart,
                                     int newLeftStart, int newRightStart,
                                     int rowsPerPage, FontSize fontSize) {
        if (progress <= 0.5f) {
            // Phase 1: fold sweeps across left page (left edge → spine)
            float p = progress / 0.5f;
            float foldX = LEFT_PAGE_X + p * PAGE_WIDTH;

            float liftedWidth = p * PAGE_WIDTH;
            float flapWidth = Math.min(liftedWidth * 0.45f, (LEFT_PAGE_X + PAGE_WIDTH) - foldX);
            if (flapWidth < 0) flapWidth = 0;
            float flapRight = foldX + flapWidth;

            // Step 1: text
            // New left text revealed from LEFT_PAGE_X to foldX
            float revealedWidth = foldX - LEFT_PAGE_X;
            if (revealedWidth > 1) {
                clipAndRenderText(lines, newLeftStart, rowsPerPage, fontSize, true,
                        LEFT_PAGE_X, LEFT_PAGE_Y, revealedWidth, PAGE_HEIGHT);
            }
            // Right page: full old content
            renderPageText(lines, oldRightStart, rowsPerPage, fontSize, false);

            // Step 2: shapes
            mainPanel.spriteBatch.end();
            Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
            mainPanel.getRenderer().begin(ShapeRenderer.ShapeType.Filled);

            // Flat overlay on left page (flapRight to spine edge)
            if (flapRight < LEFT_PAGE_X + PAGE_WIDTH - 1) {
                mainPanel.getRenderer().setColor(Constants.COLOR_PAGE[0], Constants.COLOR_PAGE[1], Constants.COLOR_PAGE[2], 1f);
                mainPanel.getRenderer().rect(flapRight, LEFT_PAGE_Y, LEFT_PAGE_X + PAGE_WIDTH - flapRight, PAGE_HEIGHT);
            }
            renderFlap(foldX, flapWidth, foldX, LEFT_PAGE_Y, false);
            renderFoldShadow(foldX, LEFT_PAGE_Y, PAGE_HEIGHT);

            mainPanel.getRenderer().end();
            Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);
            mainPanel.spriteBatch.begin();

            // Step 3: old left text clipped to flat area
            float flatWidth = LEFT_PAGE_X + PAGE_WIDTH - flapRight;
            if (flatWidth > 1) {
                clipAndRenderText(lines, oldLeftStart, rowsPerPage, fontSize, true,
                        flapRight, LEFT_PAGE_Y, flatWidth, PAGE_HEIGHT);
            }

        } else {
            // Phase 2: page settles on right page (spine → right edge)
            float p = (progress - 0.5f) / 0.5f;
            float foldX = RIGHT_PAGE_X + p * PAGE_WIDTH;

            float settledWidth = p * PAGE_WIDTH;
            float unsettledWidth = (RIGHT_PAGE_X + PAGE_WIDTH) - foldX;
            float flapWidth = Math.min(unsettledWidth * 0.45f, settledWidth * 0.45f);
            if (flapWidth < 0) flapWidth = 0;
            float flapRight = foldX + flapWidth;

            // Step 1: text
            // New left page: full (fold has passed)
            renderPageText(lines, newLeftStart, rowsPerPage, fontSize, true);
            // New right text revealed from RIGHT_PAGE_X to foldX
            float revealedWidth = foldX - RIGHT_PAGE_X;
            if (revealedWidth > 1) {
                clipAndRenderText(lines, newRightStart, rowsPerPage, fontSize, false,
                        RIGHT_PAGE_X, RIGHT_PAGE_Y, revealedWidth, PAGE_HEIGHT);
            }

            // Step 2: shapes
            mainPanel.spriteBatch.end();
            Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
            mainPanel.getRenderer().begin(ShapeRenderer.ShapeType.Filled);

            // Flat overlay on right page (flapRight to right edge)
            if (flapRight < RIGHT_PAGE_X + PAGE_WIDTH - 1) {
                mainPanel.getRenderer().setColor(Constants.COLOR_PAGE[0], Constants.COLOR_PAGE[1], Constants.COLOR_PAGE[2], 1f);
                mainPanel.getRenderer().rect(flapRight, RIGHT_PAGE_Y, RIGHT_PAGE_X + PAGE_WIDTH - flapRight, PAGE_HEIGHT);
            }
            renderFlap(foldX, flapWidth, foldX, RIGHT_PAGE_Y, false);
            renderFoldShadow(foldX, RIGHT_PAGE_Y, PAGE_HEIGHT);

            mainPanel.getRenderer().end();
            Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);
            mainPanel.spriteBatch.begin();

            // Step 3: old right text clipped to flat area
            float flatWidth = RIGHT_PAGE_X + PAGE_WIDTH - flapRight;
            if (flatWidth > 1) {
                clipAndRenderText(lines, oldRightStart, rowsPerPage, fontSize, false,
                        flapRight, RIGHT_PAGE_Y, flatWidth, PAGE_HEIGHT);
            }
        }
    }

    private void clipAndRenderText(ArrayList<String> lines, int startLine, int rows,
                                    FontSize fontSize, boolean isLeftPage,
                                    float clipX, float clipY, float clipW, float clipH) {
        mainPanel.spriteBatch.flush();
        Rectangle clipBounds = new Rectangle(clipX, clipY, clipW, clipH);
        Rectangle scissors = new Rectangle();
        ScissorStack.calculateScissors(
                mainPanel.getViewport().getCamera(),
                mainPanel.spriteBatch.getTransformMatrix(),
                clipBounds, scissors);
        if (ScissorStack.pushScissors(scissors)) {
            renderPageText(lines, startLine, rows, fontSize, isLeftPage);
            mainPanel.spriteBatch.flush();
            ScissorStack.popScissors();
        }
    }

    private void renderFlap(float flapStart, float flapWidth, float foldX, float pageY, boolean flapLeftOfFold) {
        if (flapWidth <= 1) return;

        mainPanel.getRenderer().setColor(Constants.COLOR_PAGE_BACK[0], Constants.COLOR_PAGE_BACK[1], Constants.COLOR_PAGE_BACK[2], 1f);
        mainPanel.getRenderer().rect(flapLeftOfFold ? flapStart : foldX, pageY, flapWidth, PAGE_HEIGHT);

        // Darkening near fold edge
        mainPanel.getRenderer().setColor(0f, 0f, 0f, 0.06f);
        float gradientWidth = Math.min(flapWidth, 30);
        if (flapLeftOfFold) {
            mainPanel.getRenderer().rect(foldX - gradientWidth, pageY, gradientWidth, PAGE_HEIGHT);
        } else {
            mainPanel.getRenderer().rect(foldX, pageY, gradientWidth, PAGE_HEIGHT);
        }

        // Light edge at far side of flap
        mainPanel.getRenderer().setColor(1f, 1f, 1f, 0.12f);
        float highlightWidth = Math.min(flapWidth, 4);
        if (flapLeftOfFold) {
            mainPanel.getRenderer().rect(flapStart, pageY, highlightWidth, PAGE_HEIGHT);
        } else {
            mainPanel.getRenderer().rect(foldX + flapWidth - highlightWidth, pageY, highlightWidth, PAGE_HEIGHT);
        }
    }

    private void renderFoldShadow(float foldX, float pageY, float pageHeight) {
        mainPanel.getRenderer().setColor(0f, 0f, 0f, 0.25f);
        mainPanel.getRenderer().rect(foldX - 2, pageY, 4, pageHeight);

        mainPanel.getRenderer().setColor(0f, 0f, 0f, 0.12f);
        mainPanel.getRenderer().rect(foldX - 8, pageY, 6, pageHeight);
        mainPanel.getRenderer().rect(foldX + 2, pageY, 6, pageHeight);

        mainPanel.getRenderer().setColor(0f, 0f, 0f, 0.05f);
        mainPanel.getRenderer().rect(foldX - 18, pageY, 10, pageHeight);
        mainPanel.getRenderer().rect(foldX + 8, pageY, 10, pageHeight);
    }
}
