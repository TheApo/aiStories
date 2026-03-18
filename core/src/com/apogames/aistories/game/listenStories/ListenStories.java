package com.apogames.aistories.game.listenStories;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.MainPanel;
import com.apogames.aistories.game.main.*;
import com.apogames.aistories.game.objects.*;
import com.apogames.asset.AssetLoader;
import com.apogames.backend.DrawString;
import com.apogames.backend.SequentiallyThinkingScreenModel;
import com.apogames.common.Localization;
import com.apogames.entity.ApoButton;
import com.apogames.entity.TextArea;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Input;
import lombok.Getter;

import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

public class ListenStories extends SequentiallyThinkingScreenModel implements MainInterface {

    public static final String SEPARATOR_IN_FILE = ";!;!";

    public static final String FUNCTION_BACK = "LISTEN_QUIT";
    public static final String FUNCTION_PLAY = "LISTEN_PLAY";
    public static final String FUNCTION_STOP = "LISTEN_STOP";
    public static final String FUNCTION_PAUSE = "LISTEN_PAUSE";
    public static final String FUNCTION_CREATEMP3 = "LISTEN_CREATEMP3";
    public static final String FUNCTION_FONT_BIGGER = "LISTEN_FONT_BIGGER";
    public static final String FUNCTION_FONT_SMALLER = "LISTEN_FONT_SMALLER";
    public static final String FUNCTION_NEXT_PAGE = "LISTEN_NEXT_PAGE";
    public static final String FUNCTION_PREVIOUS_PAGE = "LISTEN_PREVIOUS_PAGE";
    public static final String FUNCTION_NEXT_STORY = "LISTEN_NEXT_STORY";
    public static final String FUNCTION_PREVIOUS_STORY = "LISTEN_PREVIOUS_STORY";
    public static final String FUNCTION_UPLOAD_TONIE = "LISTEN_UPLOAD_TONIE";
    public static final String FUNCTION_DELETE = "LISTEN_DELETE";
    public static final String FUNCTION_SONG_VARIANT = "LISTEN_SONG_VARIANT";

    private final boolean[] keys = new boolean[256];

    private boolean isPressed = false;

    private FileHandle[] fileMP3Handles;
    private FileHandle[] fileTXTHandles;
    private FileHandle[] fileImageHandles;

    private Texture[] imageStoryTexture;
    private TextureRegion[] imageStoryTextureRegion;

    private Music music;

    private int choosenListenStory = -1;
    private float totalDuration = 0f;
    private int lastRenderedSecond = -1;
    private WordTimingData timingData = null;

    // Independent position tracking (avoids Android MediaPlayer.getCurrentPosition() drift)
    private long playbackStartNanos = 0;
    private float playbackStartOffset = 0f;
    private WordHighlighter highlighter = null;
    private ArrayList<Integer> choosenImageStory = new ArrayList<>();
    private int choosenReadStory = -1;
    private boolean isSong = false;
    private GameObjectives savedObjectives;
    private int songVariantIndex = 0;
    private FileHandle[] songVariantHandles;

    private boolean pendingSunoCustom = false;
    private boolean pendingSunoReady = false;
    private String pendingSunoStyle = "";
    private String pendingSunoHeader = "";
    private String savedSongFilePrefix = "";

    private FontSize fontSize = FontSize.FONT_25;

    @Getter
    private TextArea textArea = null;
    @Getter
    private final Prompt prompt = new Prompt();
    @Getter
    private final ChatGPTIO chatGPT = new ChatGPTIO(this);

    private int currentSpread = 0;
    private int pendingSpread = 0;

    private final BookRenderer bookRenderer;
    private final PageTurnAnimation pageAnimation = new PageTurnAnimation();
    private final BookTextEditor textEditor = new BookTextEditor();
    private String fileMetadataPrefix = "";

    private static final int ROW1_Y_NORMAL = 580;
    private static final int ROW2_Y = 700;
    private static final int ROW1_Y_EXTENDED = 660;
    private static final int ROW_HEIGHT = 70;
    private static final int ROW_BG_WIDTH = 560;
    private static final int ROW_BG_X = Constants.GAME_WIDTH / 2 - ROW_BG_WIDTH / 2;

    private Running running = Running.NONE;
    private boolean audioCapable = false;

    private String nextText = null;
    private Running nextRunning;
    private String nextStatusText = null;
    private String statusText = "";
    private boolean reload = false;

    public ListenStories(final MainPanel game) {
        super(game);
        this.bookRenderer = new BookRenderer(game);
    }

    public void setNeededButtonsVisible() {
        getMainPanel().getButtonByFunction(FUNCTION_BACK).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_CREATEMP3).setVisible(false);
        getMainPanel().getButtonByFunction(FUNCTION_FONT_BIGGER).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_FONT_SMALLER).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_NEXT_PAGE).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_PREVIOUS_PAGE).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_NEXT_STORY).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_PREVIOUS_STORY).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_UPLOAD_TONIE).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_DELETE).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_SONG_VARIANT).setVisible(false);

        this.setVisibleFalseWhenNotSet();
    }

    private void setVisibleFalseWhenNotSet() {
        boolean hasTonieUser = ToniesAPI.USERNAME != null && !ToniesAPI.USERNAME.isEmpty() && !ToniesAPI.USERNAME.equals("Dein Username zur Anmeldung in der Tonies Cloud");
        boolean hasToniePass = ToniesAPI.PASSWORD != null && !ToniesAPI.PASSWORD.isEmpty() && !ToniesAPI.PASSWORD.equals("Dein Passwort zur Anemdlung in der Tonies Cloud");
        if (!hasTonieUser || !hasToniePass) {
            getMainPanel().getButtonByFunction(FUNCTION_UPLOAD_TONIE).setVisible(false);
        }

        boolean hasElevenLabs = ElvenlabIO.API_KEY != null && !ElvenlabIO.API_KEY.isEmpty() && !ElvenlabIO.API_KEY.equals("Dein ELEVENLABS_API_KEY");
        if (!hasElevenLabs) {
            getMainPanel().getButtonByFunction(FUNCTION_CREATEMP3).setVisible(false);
        }
    }

    @Override
    public void init() {
        if (getGameProperties() == null) {
            setGameProperties(new ListenStoriesPreferences(this));
            loadProperties();
        }

        this.savedObjectives = this.getMainPanel().getPromptObject().getGameObjectives().copy();

        FontSize.refreshAll();
        this.getMainPanel().reInit();

        this.getMainPanel().resetSize(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);

        this.setNeededButtonsVisible();
        this.setButtonsVisibility();
        if (this.running == Running.NONE) {
            this.reloadFileHandler();
        } else {
            if (this.textArea == null) {
                this.textArea = new TextArea(0, 0, bookRenderer.getTextAreaWidth(), BookRenderer.getPageHeight(), "TextArea", "");
            } else {
                this.textArea.setHeight(BookRenderer.getPageHeight());
            }
            this.textArea.setFont(fontSize.getFont());
            String promptText = this.getMainPanel().getTextArea().getText();
            if (promptText != null && !promptText.isEmpty()) {
                this.textArea.setText(promptText);
                this.textEditor.setText(promptText);
                this.textEditor.setFont(this.fontSize.getFont());
                processAndLayoutChapters();
                this.textEditor.setDisplayData(this.textArea.getMyText(), new int[0], new java.util.HashSet<>());
                this.textEditor.setActive(false);
            }
        }

        if (isBackgroundGeneration()) {
            setButtonsInsivislbe();
            getMainPanel().getButtonByFunction(FUNCTION_BACK).setVisible(false);
            getMainPanel().getButtonByFunction(FUNCTION_FONT_BIGGER).setVisible(true);
            getMainPanel().getButtonByFunction(FUNCTION_FONT_SMALLER).setVisible(true);
            updatePageButtonVisibility();
        }
    }

    public boolean isStoryAvailable() {
        return this.fileTXTHandles != null && this.fileTXTHandles.length > 0;
    }

    private void reloadFileHandler() {
        this.reloadFileHandler(-1);
    }

    private void reloadFileHandler(int chooseStory) {
        this.fileMP3Handles = Gdx.files.local(Prompt.DIRECTORY).list(".mp3");
        Arrays.sort(this.fileMP3Handles, (file1, file2) -> file1.name().compareToIgnoreCase(file2.name()));
        this.fileTXTHandles = Gdx.files.local(Prompt.DIRECTORY).list(".txt");
        Arrays.sort(this.fileTXTHandles, (file1, file2) -> file1.name().compareToIgnoreCase(file2.name()));
        this.fileImageHandles = Gdx.files.local(Prompt.DIRECTORY).list(".png");
        Arrays.sort(this.fileImageHandles, (file1, file2) -> file1.name().compareToIgnoreCase(file2.name()));

        // Determine story and audio state first (affects book height)
        this.choosenReadStory = -1;
        if (this.fileTXTHandles.length > 0) {
            this.choosenReadStory = this.fileTXTHandles.length - 1;
            if (chooseStory >= 0) {
                this.choosenReadStory = chooseStory;
            }
        }
        // Detect song from filename before findListenStory
        this.isSong = false;
        if (this.choosenReadStory >= 0) {
            String fileName = this.fileTXTHandles[this.choosenReadStory].file().getName();
            this.isSong = fileName.endsWith("_song.txt");
        }
        findListenStory();

        // Create/update TextArea with correct height
        if (this.textArea == null) {
            this.textArea = new TextArea(0, 0, bookRenderer.getTextAreaWidth(), BookRenderer.getPageHeight(), "TextArea", "");
        } else {
            this.textArea.setHeight(BookRenderer.getPageHeight());
        }
        this.textArea.setFont(fontSize.getFont());
        this.setUpFont(0);

        // Now set text with correct layout
        if (this.choosenReadStory >= 0) {
            String text = this.fileTXTHandles[this.choosenReadStory].readString();
            this.setTextInTextArea(text);
        }
    }

    private void setTextInTextArea(String text) {
        this.isSong = false;
        this.fileMetadataPrefix = "";
        if (text.contains(SEPARATOR_IN_FILE)) {
            String values = text.substring(0,text.indexOf(SEPARATOR_IN_FILE));
            this.fileMetadataPrefix = values + SEPARATOR_IN_FILE;

            if (values.startsWith("song")) {
                this.isSong = true;
                String[] songParts = values.split(";");
                if (songParts.length >= 6) {
                    GameObjectives go = this.getMainPanel().getPromptObject().getGameObjectives();
                    go.setMainCharacter(resolveEntity(songParts[1], go.getMainCharacter(), this.getMainPanel().getCustomMainEntity(), MainCharacter.values()[0]));
                    go.setSupportingCharacter(resolveEntity(songParts[2], go.getSupportingCharacter(), this.getMainPanel().getCustomSupportingEntity(), SupportingCharacter.values()[0]));
                    go.setUniverse(resolveEntity(songParts[3], go.getUniverse(), this.getMainPanel().getCustomUniverse(), Universe.values()[0]));
                    go.setPlaces(resolveEntity(songParts[4], go.getPlaces(), this.getMainPanel().getCustomPlaces(), Places.values()[0]));
                    go.setObjectives(resolveEntity(songParts[5], go.getObjectives(), this.getMainPanel().getCustomObjectives(), Objectives.values()[0]));
                }
                if (songParts.length >= 7) {
                    this.pendingSunoStyle = songParts[6];
                } else {
                    this.pendingSunoStyle = "";
                }
                text = text.substring(text.indexOf(SEPARATOR_IN_FILE) + SEPARATOR_IN_FILE.length());
            } else {
                String[] split = values.split(";");

                GameObjectives go = this.getMainPanel().getPromptObject().getGameObjectives();
                go.setMainCharacter(resolveEntity(split[0], go.getMainCharacter(), this.getMainPanel().getCustomMainEntity(), MainCharacter.values()[0]));
                go.setSupportingCharacter(resolveEntity(split[1], go.getSupportingCharacter(), this.getMainPanel().getCustomSupportingEntity(), SupportingCharacter.values()[0]));
                go.setUniverse(resolveEntity(split[2], go.getUniverse(), this.getMainPanel().getCustomUniverse(), Universe.values()[0]));
                go.setPlaces(resolveEntity(split[3], go.getPlaces(), this.getMainPanel().getCustomPlaces(), Places.values()[0]));
                go.setObjectives(resolveEntity(split[4], go.getObjectives(), this.getMainPanel().getCustomObjectives(), Objectives.values()[0]));
                text = text.substring(text.indexOf(SEPARATOR_IN_FILE) + SEPARATOR_IN_FILE.length());
            }
        }
        this.currentSpread = 0;
        this.pendingSpread = 0;
        String cleaned = cleanText(text);
        this.textArea.setText(cleaned);
        this.textEditor.setText(cleaned);
        this.textEditor.setFont(this.fontSize.getFont());

        boolean editable = isEditable();
        if (editable) {
            buildEditableLayout();
        } else {
            processAndLayoutChapters();
            this.textEditor.setDisplayData(this.textArea.getMyText(), new int[0], new java.util.HashSet<>());
        }
        this.textEditor.setActive(editable);
        updatePageButtonVisibility();
    }

    private void buildEditableLayout() {
        bookRenderer.getChapterLines().clear();
        ArrayList<String> bodyLines = this.textArea.getMyText();
        String rawText = this.textEditor.getRawText();
        com.badlogic.gdx.graphics.g2d.BitmapFont headingFont = this.fontSize.getNext(1).getFont();
        float maxWidth = BookRenderer.TEXT_WIDTH;
        int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);

        // Step 1: compute raw text positions for body-font-wrapped lines
        int[] bodyStarts = new int[bodyLines.size()];
        int rawPos = 0;
        for (int i = 0; i < bodyLines.size(); i++) {
            bodyStarts[i] = rawPos;
            String line = bodyLines.get(i);
            if (line.isEmpty()) {
                if (rawPos < rawText.length() && rawText.charAt(rawPos) == '\n') rawPos++;
            } else {
                rawPos += line.length();
                if (rawPos < rawText.length()) {
                    char c = rawText.charAt(rawPos);
                    if (c == ' ' || c == '\n') rawPos++;
                }
            }
        }

        // Step 2: build final display lines with heading re-wrap + page padding
        ArrayList<String> finalLines = new ArrayList<>();
        java.util.List<Integer> finalStarts = new ArrayList<>();
        java.util.Set<Integer> paddingSet = new java.util.HashSet<>();

        int i = 0;
        while (i < bodyLines.size()) {
            String line = bodyLines.get(i);

            if (!BookRenderer.isChapterHeading(line)) {
                finalLines.add(line);
                finalStarts.add(bodyStarts[i]);
                i++;
                continue;
            }

            // Collect full heading text (may span multiple body-wrapped lines)
            StringBuilder fullHeading = new StringBuilder(line.trim());
            int headingRawStart = bodyStarts[i];
            int lastIdx = i;
            for (int j = i + 1; j < bodyLines.size(); j++) {
                String next = bodyLines.get(j).trim();
                if (next.isEmpty() || BookRenderer.isChapterHeading(next)) break;
                fullHeading.append(" ").append(next);
                lastIdx = j;
            }

            // Page padding: push chapter to next page boundary
            int currentSize = finalLines.size();
            int posInPage = currentSize % rowsPerPage;
            if (posInPage != 0) {
                int pageStart = currentSize - posInPage;
                boolean allEmpty = true;
                for (int j = pageStart; j < currentSize; j++) {
                    if (!finalLines.get(j).trim().isEmpty() && !paddingSet.contains(j)) {
                        allEmpty = false;
                        break;
                    }
                }
                if (allEmpty) {
                    while (finalLines.size() > pageStart) {
                        int removeIdx = finalLines.size() - 1;
                        finalLines.remove(removeIdx);
                        finalStarts.remove(removeIdx);
                        paddingSet.remove(removeIdx);
                    }
                } else {
                    int padding = rowsPerPage - posInPage;
                    for (int p = 0; p < padding; p++) {
                        paddingSet.add(finalLines.size());
                        finalLines.add("");
                        finalStarts.add(headingRawStart);
                    }
                }
            }

            // Strip "Kapitel" word, keep number
            String heading = BookRenderer.stripChapterWord(fullHeading.toString());

            // Re-wrap heading with heading font
            ArrayList<String> wrapped = wrapTextForFont(heading, headingFont, maxWidth);
            int rp = headingRawStart;
            for (String wLine : wrapped) {
                bookRenderer.getChapterLines().add(finalLines.size());
                finalLines.add(wLine);
                finalStarts.add(rp);
                rp += wLine.length();
                if (rp < rawText.length() && rawText.charAt(rp) == ' ') rp++;
            }

            // Ensure 1 empty line after heading
            int nextIdx = lastIdx + 1;
            int emptyCount = 0;
            while (nextIdx + emptyCount < bodyLines.size() && bodyLines.get(nextIdx + emptyCount).trim().isEmpty()) {
                emptyCount++;
            }
            if (nextIdx < bodyLines.size()) {
                finalLines.add("");
                finalStarts.add(bodyStarts[Math.min(nextIdx, bodyLines.size() - 1)]);
            } else {
                finalLines.add("");
                finalStarts.add(rawText.length());
            }

            i = nextIdx + Math.max(emptyCount, 1);
            if (i > bodyLines.size()) i = bodyLines.size();
        }

        int[] startsArray = new int[finalStarts.size()];
        for (int s = 0; s < finalStarts.size(); s++) startsArray[s] = finalStarts.get(s);
        this.textEditor.setDisplayData(finalLines, startsArray, paddingSet);
    }

    private void relayoutAfterEdit() {
        String rawText = this.textEditor.getRawText();
        this.textArea.setText(rawText);
        buildEditableLayout();
        updatePageButtonVisibility();

        int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
        int neededSpread = this.textEditor.getSpreadForCursor(rowsPerPage);
        if (neededSpread != this.currentSpread && !pageAnimation.isAnimating()) {
            this.pendingSpread = neededSpread;
            if (neededSpread > this.currentSpread) {
                pageAnimation.start(PageTurnAnimation.Direction.FORWARD);
            } else {
                pageAnimation.start(PageTurnAnimation.Direction.BACKWARD);
            }
            Gdx.graphics.setContinuousRendering(true);
        }
        Gdx.graphics.requestRendering();
    }

    private ArrayList<String> getActiveDisplayLines() {
        if (this.textEditor.isActive() && !this.textEditor.getDisplayLines().isEmpty()) {
            return new ArrayList<>(this.textEditor.getDisplayLines());
        }
        return this.textArea.getMyText();
    }

    private void saveEditedText() {
        if (this.choosenReadStory < 0) return;
        String rawText = this.textEditor.getRawText();
        FileHandle fileHandle = this.fileTXTHandles[this.choosenReadStory];
        fileHandle.writeString(this.fileMetadataPrefix + rawText, false);
    }

    private String cleanText(String text) {
        // Remove zero-width and invisible Unicode characters
        text = text.replace("\u200B", "").replace("\u200C", "")
                   .replace("\u200D", "").replace("\uFEFF", "");
        // Non-breaking space → normal space
        text = text.replace('\u00A0', ' ');
        // Process line by line: remove # headers, trim, ensure blank line after headings
        String[] lines = text.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String original = lines[i].trim();
            boolean isHeading = original.startsWith("#") || BookRenderer.isChapterHeading(original);
            String line = original.replaceAll("^#+\\s*", "").trim();
            if (i > 0) sb.append("\n");
            sb.append(line);
            // Ensure blank line after heading so body text is separated
            if (isHeading) {
                boolean nextIsEmpty = (i + 1 < lines.length && lines[i + 1].trim().isEmpty());
                if (!nextIsEmpty) {
                    sb.append("\n");
                }
            }
        }
        // Collapse multiple spaces
        return sb.toString().replaceAll(" {2,}", " ");
    }

    private void processAndLayoutChapters() {
        bookRenderer.getChapterLines().clear();
        ArrayList<String> lines = this.textArea.getMyText();
        int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
        com.badlogic.gdx.graphics.g2d.BitmapFont headingFont = this.fontSize.getNext(1).getFont();
        float maxWidth = BookRenderer.TEXT_WIDTH;

        for (int i = 0; i < lines.size(); i++) {
            if (!BookRenderer.isChapterHeading(lines.get(i))) continue;

            // Pad to page boundary
            int posInPage = i % rowsPerPage;
            if (posInPage != 0) {
                // Check if all lines from page start to here are empty
                int pageStart = i - posInPage;
                boolean allEmpty = true;
                for (int j = pageStart; j < i; j++) {
                    if (!lines.get(j).trim().isEmpty()) {
                        allEmpty = false;
                        break;
                    }
                }
                if (allEmpty) {
                    // Remove empty lines to avoid a blank page
                    for (int j = 0; j < posInPage; j++) {
                        lines.remove(pageStart);
                    }
                    i -= posInPage;
                } else {
                    int padding = rowsPerPage - posInPage;
                    for (int j = 0; j < padding; j++) {
                        lines.add(i, "");
                    }
                    i += padding;
                }
            }

            // Collect full heading text (TextArea may have wrapped across lines)
            StringBuilder fullText = new StringBuilder(lines.get(i).trim());
            int lastIdx = i;
            for (int j = i + 1; j < lines.size(); j++) {
                String next = lines.get(j).trim();
                if (next.isEmpty() || BookRenderer.isChapterHeading(next)) break;
                fullText.append(" ").append(next);
                lastIdx = j;
            }

            // Strip "Kapitel" word, keep number
            String heading = BookRenderer.stripChapterWord(fullText.toString());

            // Remove old heading lines
            for (int j = lastIdx; j >= i; j--) {
                lines.remove(j);
            }

            // Re-wrap with heading font
            ArrayList<String> wrapped = wrapTextForFont(heading, headingFont, maxWidth);
            for (int j = 0; j < wrapped.size(); j++) {
                lines.add(i + j, wrapped.get(j));
                bookRenderer.getChapterLines().add(i + j);
            }

            // Ensure exactly 1 empty line after heading
            int afterIdx = i + wrapped.size();
            int emptyCount = 0;
            while (afterIdx + emptyCount < lines.size() && lines.get(afterIdx + emptyCount).trim().isEmpty()) {
                emptyCount++;
            }
            while (emptyCount > 1) {
                lines.remove(afterIdx + emptyCount - 1);
                emptyCount--;
            }
            if (emptyCount < 1) {
                lines.add(afterIdx, "");
            }

            i = afterIdx; // loop will increment
        }
    }

    private ArrayList<String> wrapTextForFont(String text, com.badlogic.gdx.graphics.g2d.BitmapFont font, float maxWidth) {
        ArrayList<String> result = new ArrayList<>();
        Constants.glyphLayout.setText(font, text);
        if (Constants.glyphLayout.width <= maxWidth) {
            result.add(text);
            return result;
        }
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String test = current.length() == 0 ? word : current + " " + word;
            Constants.glyphLayout.setText(font, test);
            if (Constants.glyphLayout.width > maxWidth && current.length() > 0) {
                result.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(test);
            }
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }
        return result;
    }

    private void updatePageButtonVisibility() {
        ArrayList<String> lines = getActiveDisplayLines();
        if (lines == null || lines.isEmpty()) return;
        int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
        boolean canGoBack = this.currentSpread > 0;
        boolean canGoForward = (this.currentSpread + 1) * 2 * rowsPerPage < lines.size();
        getMainPanel().getButtonByFunction(FUNCTION_PREVIOUS_PAGE).setVisible(canGoBack);
        getMainPanel().getButtonByFunction(FUNCTION_NEXT_PAGE).setVisible(canGoForward);
    }

    private boolean hasAudioCapability() {
        if (this.pendingSunoReady) return true;
        boolean hasMp3 = this.choosenListenStory >= 0;
        boolean hasSongVariants = this.songVariantHandles != null && this.songVariantHandles.length > 0;
        boolean hasElevenLabs = ElvenlabIO.API_KEY != null && !ElvenlabIO.API_KEY.isEmpty()
                && !ElvenlabIO.API_KEY.equals("Dein ELEVENLABS_API_KEY");
        return hasMp3 || hasSongVariants || hasElevenLabs;
    }

    private boolean isEditable() {
        boolean hasMp3 = this.choosenListenStory >= 0;
        boolean hasSongVariants = this.songVariantHandles != null && this.songVariantHandles.length > 0;
        return !hasMp3 && !hasSongVariants && this.choosenReadStory >= 0 && this.running == Running.NONE;
    }

    private boolean isBackgroundGeneration() {
        if (this.running == Running.NONE) return false;
        ArrayList<String> lines = getActiveDisplayLines();
        return lines != null && !lines.isEmpty();
    }

    private void updateAudioButtonVisibility() {
        boolean hasMp3 = this.choosenListenStory >= 0 || (this.songVariantHandles != null && this.songVariantHandles.length > 0);
        if (!hasMp3) {
            getMainPanel().getButtonByFunction(FUNCTION_PLAY).setVisible(false);
            getMainPanel().getButtonByFunction(FUNCTION_PAUSE).setVisible(false);
            getMainPanel().getButtonByFunction(FUNCTION_STOP).setVisible(false);
            return;
        }
        boolean isPlaying = this.music != null && this.music.isPlaying();
        getMainPanel().getButtonByFunction(FUNCTION_PLAY).setVisible(!isPlaying);
        getMainPanel().getButtonByFunction(FUNCTION_PAUSE).setVisible(isPlaying);
        getMainPanel().getButtonByFunction(FUNCTION_STOP).setVisible(true);
    }

    private void updateLayout() {
        this.audioCapable = hasAudioCapability();
        int bookHeight = audioCapable ? BookRenderer.DEFAULT_BOOK_HEIGHT : BookRenderer.EXTENDED_BOOK_HEIGHT;
        BookRenderer.setBookHeight(bookHeight);

        int row1Y = audioCapable ? ROW1_Y_NORMAL : ROW1_Y_EXTENDED;
        int buttonCenterY = row1Y + (ROW_HEIGHT - 64) / 2;

        // Reposition row 1 buttons (page nav, font size, delete)
        getMainPanel().getButtonByFunction(FUNCTION_PREVIOUS_PAGE).setY(buttonCenterY);
        getMainPanel().getButtonByFunction(FUNCTION_NEXT_PAGE).setY(buttonCenterY);
        getMainPanel().getButtonByFunction(FUNCTION_FONT_SMALLER).setY(buttonCenterY);
        getMainPanel().getButtonByFunction(FUNCTION_FONT_BIGGER).setY(buttonCenterY);
        getMainPanel().getButtonByFunction(FUNCTION_DELETE).setY(buttonCenterY);

        // Tonie button: left side, below book, top aligned with row1
        getMainPanel().getButtonByFunction(FUNCTION_UPLOAD_TONIE).setY(row1Y);

        // Reposition row 2 buttons (audio) - centered
        int audioY = ROW2_Y + (ROW_HEIGHT - 64) / 2;
        float playPauseX = Constants.GAME_WIDTH / 2f - 64 - 10;
        getMainPanel().getButtonByFunction(FUNCTION_PLAY).setX(playPauseX);
        getMainPanel().getButtonByFunction(FUNCTION_PLAY).setY(audioY);
        getMainPanel().getButtonByFunction(FUNCTION_PAUSE).setX(playPauseX);
        getMainPanel().getButtonByFunction(FUNCTION_PAUSE).setY(audioY);
        getMainPanel().getButtonByFunction(FUNCTION_STOP).setX(Constants.GAME_WIDTH / 2f + 10);
        getMainPanel().getButtonByFunction(FUNCTION_STOP).setY(audioY);
        getMainPanel().getButtonByFunction(FUNCTION_CREATEMP3).setY(audioY);

        // Song variant button: right of audio bar
        getMainPanel().getButtonByFunction(FUNCTION_SONG_VARIANT).setY(audioY);

        // Hide audio row entirely if no audio capability
        if (!audioCapable) {
            getMainPanel().getButtonByFunction(FUNCTION_PLAY).setVisible(false);
            getMainPanel().getButtonByFunction(FUNCTION_PAUSE).setVisible(false);
            getMainPanel().getButtonByFunction(FUNCTION_STOP).setVisible(false);
            getMainPanel().getButtonByFunction(FUNCTION_CREATEMP3).setVisible(false);
        }

        // Update TextArea for new page height if needed
        if (this.textArea != null) {
            this.textArea.setHeight(BookRenderer.getPageHeight());
        }
    }

    private float getPlaybackPosition() {
        if (playbackStartNanos == 0 || music == null) return 0f;
        float pos = playbackStartOffset + (System.nanoTime() - playbackStartNanos) / 1_000_000_000f;
        return Math.min(pos, totalDuration);
    }

    private String formatTime(float seconds) {
        int totalSeconds = (int) seconds;
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    private EnumInterface resolveEntity(String name, EnumInterface current, CustomEntity custom) {
        return resolveEntity(name, current, custom, null);
    }

    private EnumInterface resolveEntity(String name, EnumInterface current, CustomEntity custom, EnumInterface fallback) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        if (custom != null && custom.getName().equals(name)) {
            return custom;
        }
        String category = profileCategoryFor(custom);
        if (category != null) {
            for (CharacterProfile p : this.getMainPanel().getCustomProfiles(category)) {
                if (p.getName().equals(name)) {
                    return p;
                }
            }
            CharacterProfile override = this.getMainPanel().getOverrideFor(category, name);
            if (override != null) {
                return override;
            }
        }
        EnumInterface lookup = current != null ? current : fallback;
        if (lookup != null) {
            return lookup.getEnumByName(name);
        }
        return null;
    }

    private String profileCategoryFor(CustomEntity custom) {
        if (custom == null) return null;
        String enumName = custom.getEnumName();
        if ("mainCharacter".equals(enumName) || "supportingCharacter".equals(enumName)) return "characters";
        return enumName;
    }

    public void createNewStory() {
        this.running = Running.CREATE_STORY;
        this.currentSpread = 0;
        this.pendingSpread = 0;
        this.pendingSunoCustom = false;
        this.pendingSunoReady = false;
        this.bookRenderer.getChapterLines().clear();
        this.setButtonsInsivislbe();
    }

    public void createNewSong() {
        this.running = Running.CREATE_SONG;
        this.isSong = true;
        this.currentSpread = 0;
        this.pendingSpread = 0;
        this.pendingSunoCustom = false;
        this.pendingSunoReady = false;
        this.bookRenderer.getChapterLines().clear();
        this.setButtonsInsivislbe();
    }

    public void createNewLyrics() {
        this.running = Running.CREATE_LYRICS;
        this.isSong = true;
        this.currentSpread = 0;
        this.pendingSpread = 0;
        this.pendingSunoCustom = false;
        this.pendingSunoReady = false;
        this.bookRenderer.getChapterLines().clear();
        this.setButtonsInsivislbe();
    }

    public void prepareLyricsToSuno(String style, String characterHeader) {
        this.pendingSunoCustom = true;
        this.pendingSunoStyle = style;
        this.pendingSunoHeader = characterHeader;
    }

    private void fitPromptToTwoPages() {
        if (this.textArea == null) return;
        String text = this.textArea.getText();
        if (text == null || text.isEmpty()) return;

        bookRenderer.getChapterLines().clear();

        FontSize[] candidates = {FontSize.FONT_25, FontSize.FONT_20, FontSize.FONT_15};
        for (FontSize candidate : candidates) {
            this.fontSize = candidate;
            this.textArea.setAdd(candidate.getAdd());
            this.textArea.setFont(candidate.getFont());
            this.textArea.setText(text);
            int rowsPerPage = bookRenderer.getRowsPerPage(candidate);
            if (this.textArea.getMyText().size() <= 2 * rowsPerPage) {
                return;
            }
        }
    }

    public void setButtonsInsivislbe() {
        getMainPanel().getButtonByFunction(FUNCTION_PLAY).setVisible(false);
        getMainPanel().getButtonByFunction(FUNCTION_STOP).setVisible(false);
        getMainPanel().getButtonByFunction(FUNCTION_PAUSE).setVisible(false);
        getMainPanel().getButtonByFunction(FUNCTION_UPLOAD_TONIE).setVisible(false);
        getMainPanel().getButtonByFunction(FUNCTION_CREATEMP3).setVisible(false);
        getMainPanel().getButtonByFunction(FUNCTION_NEXT_STORY).setVisible(false);
        getMainPanel().getButtonByFunction(FUNCTION_PREVIOUS_STORY).setVisible(false);
        getMainPanel().getButtonByFunction(FUNCTION_DELETE).setVisible(false);
        getMainPanel().getButtonByFunction(FUNCTION_SONG_VARIANT).setVisible(false);
    }

    @Override
    public void setFileHandle(FileHandle file) {
        this.reload = true;

        Gdx.graphics.requestRendering();
    }

    @Override
    public void setRunning(Running running) {
        this.nextRunning = running;
        Gdx.graphics.requestRendering();
    }

    @Override
    public void setStatusText(String statusText) {
        this.nextStatusText = statusText;
        Gdx.graphics.requestRendering();
    }

    @Override
    public void setTextForTextArea(String text) {
        this.nextText = text;
        Gdx.graphics.requestRendering();
    }

    private String extractTitleFromLyrics(String lyrics) {
        // Try to find a meaningful title from the first verse line (skip tags like [Verse 1])
        String[] lines = lyrics.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("[")) continue;
            if (trimmed.length() > 100) trimmed = trimmed.substring(0, 100);
            return trimmed;
        }
        return "Song";
    }

    private String safeGetName(EnumInterface e) {
        return e != null ? e.getName() : "";
    }

    private void saveText(String text) {
        String dirString = Prompt.DIRECTORY;
        GameObjectives go = this.getMainPanel().getPromptObject().getGameObjectives();
        String ids = safeGetName(go.getMainCharacter()) + ";"
                + safeGetName(go.getSupportingCharacter()) + ";"
                + safeGetName(go.getUniverse()) + ";"
                + safeGetName(go.getPlaces()) + ";"
                + safeGetName(go.getObjectives()) + SEPARATOR_IN_FILE;
        String fileName = dirString+this.prompt.getFileNameTxt();
        Gdx.app.log("SaveText", "saveText " + ids+" "+fileName);
        FileHandle fileHandle = Gdx.files.local(fileName);
        fileHandle.writeString(ids+text, false);
    }

    private void saveSongText(String text) {
        GameObjectives go = this.getMainPanel().getPromptObject().getGameObjectives();
        String header = "song;" + safeGetName(go.getMainCharacter()) + ";"
                + safeGetName(go.getSupportingCharacter()) + ";"
                + safeGetName(go.getUniverse()) + ";"
                + safeGetName(go.getPlaces()) + ";"
                + safeGetName(go.getObjectives()) + ";"
                + this.pendingSunoStyle + SEPARATOR_IN_FILE;

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_");
        String formattedDate = now.format(fmt);

        String main = go.getMainCharacter() != null ? go.getMainCharacter().getDisplayName() : "";
        String support = go.getSupportingCharacter() != null ? go.getSupportingCharacter().getDisplayName() : "";
        String universe = go.getUniverse() != null ? go.getUniverse().getDisplayName() : "";
        String namePart = main;
        if (!support.isEmpty()) namePart += "_" + support;
        if (!universe.isEmpty()) namePart += "_" + universe;
        if (namePart.isEmpty()) namePart = "Song";

        this.savedSongFilePrefix = formattedDate + namePart + "_song";
        String fileName = Prompt.DIRECTORY + this.savedSongFilePrefix + ".txt";
        Gdx.app.log("SaveSongText", "saveSongText " + fileName);
        FileHandle fileHandle = Gdx.files.local(fileName);
        fileHandle.writeString(header + text, false);
    }

    @Override
    public void keyPressed(int keyCode, char character) {
        super.keyPressed(keyCode, character);
        keys[keyCode] = true;

        // keyDown: only navigation keys for the editor (key repeat)
        if (this.textEditor.isActive()) {
            this.textEditor.keyDown(keyCode);
            ensureCursorVisible();
        }
    }

    @Override
    public void keyButtonReleased(int keyCode, char character) {
        super.keyButtonReleased(keyCode, character);

        if (keys[keyCode]) {
            // This is a real keyUp event (keyDown set keys[keyCode]=true)
            keys[keyCode] = false;

            if (this.textEditor.isActive()) {
                // Ctrl combos are handled on keyUp, like in Textfield
                this.textEditor.keyUp(keyCode);
                handleEditorTextChange();
            }
        } else {
            // This is a keyTyped event (character is the real typed char)
            if (this.textEditor.isActive()) {
                this.textEditor.addTypedCharacter(character);
                handleEditorTextChange();
                ensureCursorVisible();
            }
        }
    }

    private void handleEditorTextChange() {
        if (this.textEditor.isTextChanged()) {
            this.textEditor.clearTextChanged();
            relayoutAfterEdit();
            saveEditedText();
        }
    }

    private void ensureCursorVisible() {
        int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
        int neededSpread = this.textEditor.getSpreadForCursor(rowsPerPage);
        if (neededSpread != this.currentSpread && !pageAnimation.isAnimating()) {
            this.pendingSpread = neededSpread;
            if (neededSpread > this.currentSpread) {
                pageAnimation.start(PageTurnAnimation.Direction.FORWARD);
            } else {
                pageAnimation.start(PageTurnAnimation.Direction.BACKWARD);
            }
            Gdx.graphics.setContinuousRendering(true);
        }
        Gdx.graphics.requestRendering();
    }

    public void mouseMoved(int mouseX, int mouseY) {
    }

    public void mouseButtonReleased(int mouseX, int mouseY, boolean isRightButton) {
        this.isPressed = false;
    }

    public void mousePressed(int x, int y, boolean isRightButton) {
        if (isRightButton && !this.isPressed) {
            this.isPressed = true;
            return;
        }
        if (this.textEditor.isActive() && !isRightButton) {
            int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
            boolean handled = this.textEditor.handleClick(x, y, this.fontSize, this.currentSpread,
                    rowsPerPage, this.fontSize.getNext(1).getFont(),
                    this.bookRenderer.getChapterLines());
            if (!handled) {
                Gdx.input.setOnscreenKeyboardVisible(false);
                MainPanel.clearActiveInput();
            }
        }
    }

    public void mouseDragged(int x, int y, boolean isRightButton) {
        if (isRightButton) {
            if (!this.isPressed) {
                this.mousePressed(x, y, isRightButton);
            }
            return;
        }
        if (this.textEditor.isActive()) {
            int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
            this.textEditor.handleDrag(x, y, this.fontSize, this.currentSpread,
                    rowsPerPage, this.fontSize.getNext(1).getFont(),
                    this.bookRenderer.getChapterLines());
        }
    }

    @Override
    public void mouseButtonFunction(String function) {
        super.mouseButtonFunction(function);

        if (this.running != Running.NONE) {
            if (isBackgroundGeneration()) {
                switch (function) {
                    case FUNCTION_FONT_BIGGER:
                        this.setUpFont(+1);
                        return;
                    case FUNCTION_FONT_SMALLER:
                        this.setUpFont(-1);
                        return;
                    case FUNCTION_NEXT_PAGE:
                        this.nextPage(+1);
                        return;
                    case FUNCTION_PREVIOUS_PAGE:
                        this.nextPage(-1);
                        return;
                }
            }
            return;
        }

        switch (function) {
            case ListenStories.FUNCTION_BACK:
                quit();
                break;
            case ListenStories.FUNCTION_PLAY:
                playMusic();
                break;
            case ListenStories.FUNCTION_PAUSE:
                pauseMusic();
                break;
            case ListenStories.FUNCTION_STOP:
                this.stopMusic();
                break;
            case ListenStories.FUNCTION_FONT_BIGGER:
                this.setUpFont(+1);
                break;
            case ListenStories.FUNCTION_FONT_SMALLER:
                this.setUpFont(-1);
                break;
            case ListenStories.FUNCTION_NEXT_PAGE:
                this.nextPage(+1);
                break;
            case ListenStories.FUNCTION_PREVIOUS_PAGE:
                this.nextPage(-1);
                break;
            case ListenStories.FUNCTION_NEXT_STORY:
                this.nextStory(+1);
                break;
            case ListenStories.FUNCTION_PREVIOUS_STORY:
                this.nextStory(-1);
                break;
            case ListenStories.FUNCTION_UPLOAD_TONIE:
                //uploadToTonieBox(this.fileMP3Handles[this.choosenListenStory]);
                getMainPanel().changeToCreativeTonie(this.fileMP3Handles[this.choosenListenStory]);
                break;
            case ListenStories.FUNCTION_DELETE:
                deleteCurrentFileAndText();
                break;
            case ListenStories.FUNCTION_CREATEMP3:
                if (this.pendingSunoReady) {
                    startSunoFromLyrics();
                } else {
                    this.createMissingMp3();
                }
                break;
            case ListenStories.FUNCTION_SONG_VARIANT:
                switchSongVariant();
                break;
        }
    }

    private void switchSongVariant() {
        if (songVariantHandles == null || songVariantHandles.length <= 1) return;
        stopMusic();
        int nextVariant = (songVariantIndex + 1) % songVariantHandles.length;
        loadSongVariant(nextVariant);
        updateAudioButtonVisibility();
        Gdx.graphics.requestRendering();
    }

    private void deleteCurrentFileAndText() {
        if (this.music != null) {
            Gdx.app.log("DELETE", "Stopping and disposing Music object");
            this.music.stop();
            this.music.dispose();
            this.music = null;
        }
        this.highlighter = null;
        this.timingData = null;

        // Delete song variant MP3s
        if (this.isSong && this.songVariantHandles != null) {
            for (FileHandle variant : this.songVariantHandles) {
                deleteFileWithNio(variant.file());
            }
            this.songVariantHandles = null;
        } else if (this.choosenListenStory >= 0) {
            FileHandle mp3FileHandle = this.fileMP3Handles[this.choosenListenStory];
            File mp3File = mp3FileHandle.file();
            Gdx.app.log("DELETE", "MP3 path: " + mp3File.getAbsolutePath());
            deleteFileWithNio(mp3File);

            // Delete associated JSON timing file
            String mp3Path = mp3FileHandle.path();
            File jsonFile = Gdx.files.local(mp3Path.replace(".mp3", ".json")).file();
            if (jsonFile.exists()) {
                deleteFileWithNio(jsonFile);
            }
        }
        this.fileTXTHandles[this.choosenReadStory].delete();
        int readStory = this.choosenReadStory;
        reloadFileHandler();
        this.choosenReadStory = readStory;
        this.nextStory(0);
    }

    private void deleteFileWithNio(File file) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                Files.deleteIfExists(file.toPath());
                Gdx.app.log("DELETE", "Deleted: " + file.getName() + " (attempt " + attempt + ")");
                return;
            } catch (Exception e) {
                Gdx.app.log("DELETE", "Attempt " + attempt + " failed for " + file.getName() + ": " + e.getMessage());
                System.gc();
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
        }
        Gdx.app.log("DELETE", "FAILED to delete after 3 attempts: " + file.getAbsolutePath());
    }

    public void uploadToTonieBox(FileHandle fileHandle) {
        if (fileHandle == null) {
            return;
        }

        Gdx.graphics.requestRendering();
    }

    @Override
    public void onImageReceived(String sectionIdentifier, String imageUrl) {
        Gdx.graphics.requestRendering();
    }

    private void createMissingMp3() {
        String searchName = this.fileTXTHandles[this.choosenReadStory].file().getName();
        searchName = searchName.substring(0, searchName.length() - 4);

        this.statusText = Localization.getInstance().getCommon().get("text_audio_wait_time");

        ElvenlabIO elevenlabIO = new ElvenlabIO(this);
        elevenlabIO.sendTextToSpeech(buildTtsText(), Prompt.DIRECTORY+searchName+".mp3");
    }

    private void startSunoFromLyrics() {
        this.pendingSunoReady = false;
        String lyrics = this.textArea.getText();
        if (lyrics == null || lyrics.trim().isEmpty()) return;

        getMainPanel().getButtonByFunction(FUNCTION_CREATEMP3).setId("button_read");
        setButtonsInsivislbe();
        getMainPanel().getButtonByFunction(FUNCTION_BACK).setVisible(false);
        getMainPanel().getButtonByFunction(FUNCTION_FONT_BIGGER).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_FONT_SMALLER).setVisible(true);
        updatePageButtonVisibility();

        String title = extractTitleFromLyrics(lyrics);
        SunoApiIO sunoApi = new SunoApiIO(this);
        sunoApi.setCharacterHeader(this.pendingSunoHeader);
        if (!this.savedSongFilePrefix.isEmpty()) {
            sunoApi.setExistingFilePrefix(this.savedSongFilePrefix);
        }
        sunoApi.generateSongCustom(lyrics, this.pendingSunoStyle, title);
    }

    private String buildTtsText() {
        StringBuilder sb = new StringBuilder();
        for (String line : getActiveDisplayLines()) {
            if (line.trim().isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(line);
        }
        return sb.toString();
    }

    private void nextStory(int add) {
        this.stopMusic();
        this.pendingSunoReady = false;
        this.currentSpread = 0;
        this.pendingSpread = 0;
        this.textEditor.setActive(false);

        this.choosenReadStory += add;

        if (this.choosenReadStory < 0) {
            this.choosenReadStory = this.fileTXTHandles.length - 1;
        } else if (this.choosenReadStory >= this.fileTXTHandles.length) {
            this.choosenReadStory = 0;
        }

        findImageStory();
        // Detect song from filename before findListenStory
        String fileName = this.fileTXTHandles[this.choosenReadStory].file().getName();
        this.isSong = fileName.endsWith("_song.txt");
        findListenStory();
        this.setTextInTextArea(this.fileTXTHandles[this.choosenReadStory].readString());
    }

    private void findImageStory() {
        if (this.choosenReadStory < 0) {
            return;
        }
        String searchName = this.fileTXTHandles[this.choosenReadStory].file().getName();
        searchName = searchName.substring(0, searchName.length() - 4);
        this.choosenImageStory.clear();
        int index = 0;
        for (FileHandle fileHandle : this.fileImageHandles) {
            String fileName = fileHandle.file().getName();
            if (fileName.substring(0, fileName.length() - 4).equals(searchName)) {
                this.choosenImageStory.add(index);
            }
            index += 1;
        }
        this.imageStoryTexture = new Texture[this.choosenImageStory.size()];
        this.imageStoryTextureRegion = new TextureRegion[this.choosenImageStory.size()];
        for (int indexImage : this.choosenImageStory) {

        }
    }

    private void findListenStory() {
        if (this.choosenReadStory < 0) {
            return;
        }
        String searchName = this.fileTXTHandles[this.choosenReadStory].file().getName();
        searchName = searchName.substring(0, searchName.length() - 4);
        this.choosenListenStory = -1;
        this.songVariantIndex = 0;
        this.songVariantHandles = null;
        if (this.music != null) {
            this.music.stop();
            this.music.dispose();
            this.music = null;
        }
        this.totalDuration = 0f;
        this.timingData = null;
        this.highlighter = null;

        // For songs, find all variant MP3s (_song_v1, _song_v2, or just _song)
        if (this.isSong) {
            ArrayList<FileHandle> variants = new ArrayList<>();
            for (FileHandle fileHandle : this.fileMP3Handles) {
                String fileName = fileHandle.file().getName();
                String fileBase = fileName.substring(0, fileName.length() - 4);
                if (fileBase.equals(searchName) || fileBase.startsWith(searchName + "_v")) {
                    variants.add(fileHandle);
                }
            }
            if (!variants.isEmpty()) {
                this.songVariantHandles = variants.toArray(new FileHandle[0]);
                this.songVariantIndex = 0;
                loadSongVariant(0);
            }
        } else {
            int index = 0;
            for (FileHandle fileHandle : this.fileMP3Handles) {
                String fileName = fileHandle.file().getName();
                if (fileName.substring(0, fileName.length() - 4).equals(searchName)) {
                    this.choosenListenStory = index;
                    loadMusicFromFile(fileHandle, searchName);
                    break;
                }
                index += 1;
            }
        }

        boolean hasMp3 = choosenListenStory != -1 || (songVariantHandles != null && songVariantHandles.length > 0);
        getMainPanel().getButtonByFunction(FUNCTION_UPLOAD_TONIE).setVisible(hasMp3);

        if (isSong && !hasMp3) {
            // Song without MP3 → show "Generate Song" button
            ApoButton genBtn = getMainPanel().getButtonByFunction(FUNCTION_CREATEMP3);
            genBtn.setId("button_generate_song");
            genBtn.setVisible(true);
            this.pendingSunoReady = true;
            this.savedSongFilePrefix = searchName;
        } else if (!hasMp3 && !isSong) {
            getMainPanel().getButtonByFunction(FUNCTION_CREATEMP3).setVisible(true);
        } else {
            getMainPanel().getButtonByFunction(FUNCTION_CREATEMP3).setVisible(false);
        }

        // Show song variant button only for songs with more than 1 variant
        if (isSong && songVariantHandles != null && songVariantHandles.length > 1) {
            getMainPanel().getButtonByFunction(FUNCTION_SONG_VARIANT).setVisible(true);
        } else {
            getMainPanel().getButtonByFunction(FUNCTION_SONG_VARIANT).setVisible(false);
        }

        updateLayout();
        updateAudioButtonVisibility();
        this.setVisibleFalseWhenNotSet();
    }

    private void loadMusicFromFile(FileHandle fileHandle, String searchName) {
        this.music = Gdx.audio.newMusic(fileHandle);
        this.playbackStartNanos = 0;
        this.playbackStartOffset = 0f;
        this.music.setOnCompletionListener(m -> {
            this.playbackStartNanos = 0;
            this.playbackStartOffset = 0f;
            this.lastRenderedSecond = -1;
            this.highlighter = null;
            Gdx.graphics.setContinuousRendering(false);
            updateAudioButtonVisibility();
            Gdx.graphics.requestRendering();
        });
        try {
            Mp3File mp3 = new Mp3File(fileHandle.file());
            this.totalDuration = mp3.getLengthInSeconds();
        } catch (Exception e) {
            Gdx.app.log("ListenStories", "Could not read MP3 duration: " + e.getMessage());
        }
        String jsonPath = Prompt.DIRECTORY + searchName + ".json";
        this.timingData = WordTimingData.loadFromFile(jsonPath);
        if (this.timingData != null && this.timingData.getDuration() > 0) {
            this.totalDuration = this.timingData.getDuration();
        }
        Gdx.app.log("ListenStories", "Word timing JSON " + (this.timingData != null ? "loaded (" + this.timingData.getWords().size() + " words)" : "not found") + " for " + searchName);
    }

    private void updateSongVariantButtonText() {
        if (songVariantHandles != null && songVariantHandles.length > 1) {
            getMainPanel().getButtonByFunction(FUNCTION_SONG_VARIANT).setText(
                    "V" + (songVariantIndex + 1) + "/" + songVariantHandles.length);
        }
    }

    private void loadSongVariant(int variantIndex) {
        if (songVariantHandles == null || variantIndex >= songVariantHandles.length) return;
        if (this.music != null) {
            this.music.stop();
            this.music.dispose();
            this.music = null;
        }
        this.songVariantIndex = variantIndex;
        updateSongVariantButtonText();
        FileHandle variantFile = songVariantHandles[variantIndex];
        this.choosenListenStory = -1;
        // Find the index in fileMP3Handles
        for (int i = 0; i < fileMP3Handles.length; i++) {
            if (fileMP3Handles[i].path().equals(variantFile.path())) {
                this.choosenListenStory = i;
                break;
            }
        }
        this.music = Gdx.audio.newMusic(variantFile);
        this.playbackStartNanos = 0;
        this.playbackStartOffset = 0f;
        this.music.setOnCompletionListener(m -> {
            this.playbackStartNanos = 0;
            this.playbackStartOffset = 0f;
            this.lastRenderedSecond = -1;
            Gdx.graphics.setContinuousRendering(false);
            updateAudioButtonVisibility();
            Gdx.graphics.requestRendering();
        });
        try {
            Mp3File mp3 = new Mp3File(variantFile.file());
            this.totalDuration = mp3.getLengthInSeconds();
        } catch (Exception e) {
            Gdx.app.log("ListenStories", "Could not read MP3 duration: " + e.getMessage());
        }
        this.timingData = null;
        this.highlighter = null;
    }

    private void nextPage(int add) {
        if (pageAnimation.isAnimating()) return;

        int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
        int newSpread = this.currentSpread + add;
        int newLeftStart = newSpread * 2 * rowsPerPage;

        if (newSpread >= 0 && newLeftStart < getActiveDisplayLines().size()) {
            this.pendingSpread = newSpread;
            if (add > 0) {
                pageAnimation.start(PageTurnAnimation.Direction.FORWARD);
            } else {
                pageAnimation.start(PageTurnAnimation.Direction.BACKWARD);
            }
            Gdx.graphics.setContinuousRendering(true);
        }
    }

    private void setUpFont(int add) {
        this.currentSpread = 0;
        this.pendingSpread = 0;
        this.fontSize = this.fontSize.getNext(add);
        this.textArea.setAdd(this.fontSize.getAdd());
        this.textArea.setFont(this.fontSize.getFont());
        this.textArea.setText(this.textArea.getText());
        this.textEditor.setFont(this.fontSize.getFont());
        if (this.textEditor.isActive()) {
            buildEditableLayout();
        } else {
            processAndLayoutChapters();
        }
        rebuildHighlighterMapping();
        updatePageButtonVisibility();

        Gdx.graphics.requestRendering();
    }

    private void playMusic() {
        if (this.music != null) {
            this.music.play();
            this.playbackStartNanos = System.nanoTime();
            // playbackStartOffset is 0 after stop/load, or saved position after pause
            Gdx.graphics.setContinuousRendering(true);
            if (this.timingData != null && this.highlighter == null) {
                this.highlighter = new WordHighlighter(this.timingData);
                rebuildHighlighterMapping();
                Gdx.app.log("ListenStories", "WordHighlighter created and mapping built");
            }
        }
        updateAudioButtonVisibility();
    }

    private void rebuildHighlighterMapping() {
        ArrayList<String> lines = getActiveDisplayLines();
        if (this.highlighter != null && lines != null && !lines.isEmpty()) {
            this.highlighter.buildMapping(
                    lines,
                    this.fontSize.getFont(),
                    this.fontSize.getNext(1).getFont(),
                    this.bookRenderer.getChapterLines());
        }
    }

    private void pauseMusic() {
        if (this.music != null) {
            this.playbackStartOffset = getPlaybackPosition();
            this.playbackStartNanos = 0;
            this.music.pause();
        }
        Gdx.graphics.setContinuousRendering(false);
        updateAudioButtonVisibility();
    }

    private void stopMusic() {
        if (this.music != null) {
            this.music.stop();
            this.music.setPosition(0);
        }
        this.playbackStartNanos = 0;
        this.playbackStartOffset = 0f;
        this.lastRenderedSecond = -1;
        this.highlighter = null;
        Gdx.graphics.setContinuousRendering(false);
        updateAudioButtonVisibility();
    }

    private void setButtonsVisibility() {

    }

    @Override
    protected void quit() {
        this.stopMusic();
        this.pendingSunoReady = false;
        if (this.music != null) {
            this.music.dispose();
        }
        if (this.savedObjectives != null) {
            this.getMainPanel().getPromptObject().getGameObjectives().copyFrom(this.savedObjectives);
        }
        getMainPanel().changeToMenu();
    }

    @Override
    public void doThink(float delta) {
        this.textEditor.think((int) delta);

        if (this.music != null && this.music.isPlaying()) {
            float position = getPlaybackPosition();
            int currentSecond = (int) position;
            if (currentSecond != this.lastRenderedSecond) {
                this.lastRenderedSecond = currentSecond;
                Gdx.graphics.requestRendering();
            }
            // Update word highlighter and auto-page-turn
            if (this.highlighter != null && !pageAnimation.isAnimating()) {
                this.highlighter.update(position);
                int currentLine = this.highlighter.getCurrentLine();
                if (currentLine >= 0) {
                    int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
                    int visibleStart = this.currentSpread * 2 * rowsPerPage;
                    int visibleEnd = visibleStart + 2 * rowsPerPage;
                    if (currentLine >= visibleEnd) {
                        nextPage(+1);
                    }
                }
            }
        }

        if (pageAnimation.isAnimating()) {
            boolean finished = pageAnimation.update(delta);
            if (finished) {
                this.currentSpread = this.pendingSpread;
                boolean musicPlaying = this.music != null && this.music.isPlaying();
                if (!musicPlaying) {
                    Gdx.graphics.setContinuousRendering(false);
                }
                updatePageButtonVisibility();
            }
            Gdx.graphics.requestRendering();
        }

        if (this.nextText != null) {
            String text = this.nextText;
            this.nextText = null;

            if (this.pendingSunoCustom) {
                // Two-step flow: lyrics arrived from LLM → display in book for review
                this.pendingSunoCustom = false;
                this.fontSize = FontSize.FONT_25;
                this.setTextInTextArea(text);
                this.isSong = true;

                if (text == null || text.trim().isEmpty()) {
                    Gdx.app.error("ListenStories", "LLM returned empty lyrics, skipping Suno");
                    this.running = Running.NONE;
                    this.statusText = "Error: No lyrics received";
                } else {
                    // Save lyrics immediately so they persist
                    this.saveSongText(text);
                    this.reloadFileHandler();

                    // Show lyrics in book form, let user review before generating song
                    this.pendingSunoReady = true;
                    this.running = Running.NONE;

                    getMainPanel().getButtonByFunction(FUNCTION_BACK).setVisible(true);
                    getMainPanel().getButtonByFunction(FUNCTION_FONT_BIGGER).setVisible(true);
                    getMainPanel().getButtonByFunction(FUNCTION_FONT_SMALLER).setVisible(true);
                    getMainPanel().getButtonByFunction(FUNCTION_NEXT_STORY).setVisible(true);
                    getMainPanel().getButtonByFunction(FUNCTION_PREVIOUS_STORY).setVisible(true);
                    getMainPanel().getButtonByFunction(FUNCTION_DELETE).setVisible(true);

                    ApoButton genBtn = getMainPanel().getButtonByFunction(FUNCTION_CREATEMP3);
                    genBtn.setId("button_generate_song");
                    genBtn.setVisible(true);

                    updateLayout();
                    updatePageButtonVisibility();
                }
            } else {
                this.running = Running.NONE;
                if (this.nextRunning != null && this.nextRunning == Running.NONE) {
                    this.nextRunning = null;
                }
                this.fontSize = FontSize.FONT_25;
                this.setTextInTextArea(text);

                this.saveText(text);

                getMainPanel().getButtonByFunction(FUNCTION_BACK).setVisible(true);
                getMainPanel().getButtonByFunction(FUNCTION_NEXT_STORY).setVisible(true);
                getMainPanel().getButtonByFunction(FUNCTION_PREVIOUS_STORY).setVisible(true);
                getMainPanel().getButtonByFunction(FUNCTION_DELETE).setVisible(true);
                reloadFileHandler();
            }

            Gdx.graphics.requestRendering();
        } else if (this.nextRunning != null) {
            if (this.nextRunning == Running.NONE && this.running != Running.NONE) {
                getMainPanel().getButtonByFunction(FUNCTION_BACK).setVisible(true);
                getMainPanel().getButtonByFunction(FUNCTION_NEXT_STORY).setVisible(true);
                getMainPanel().getButtonByFunction(FUNCTION_PREVIOUS_STORY).setVisible(true);
                getMainPanel().getButtonByFunction(FUNCTION_DELETE).setVisible(true);
                this.reloadFileHandler();
            }

            this.running = this.nextRunning;

            if (isBackgroundGeneration()) {
                setButtonsInsivislbe();
                getMainPanel().getButtonByFunction(FUNCTION_BACK).setVisible(false);
                getMainPanel().getButtonByFunction(FUNCTION_FONT_BIGGER).setVisible(true);
                getMainPanel().getButtonByFunction(FUNCTION_FONT_SMALLER).setVisible(true);
                updatePageButtonVisibility();
            }

            if (this.nextRunning == Running.NONE) {
                this.statusText = "";
            }

            Gdx.graphics.requestRendering();

            this.nextRunning = null;
        } else if (this.nextStatusText != null) {
            this.statusText = this.nextStatusText;
            this.nextStatusText = null;
            Gdx.graphics.requestRendering();
        }

        if (this.reload) {
            reloadFileHandler(this.choosenReadStory);
            this.reload = false;
            Gdx.graphics.requestRendering();
        }
    }

    @Override
    public void render() {
        // Background
        getMainPanel().spriteBatch.begin();
        getMainPanel().spriteBatch.draw(AssetLoader.backgroundTextureRegion, 0, 0);
        getMainPanel().spriteBatch.end();

        // Book frame (cover, pages, spine)
        bookRenderer.renderBookFrame();

        // Word highlight (between page background and text)
        renderWordHighlight();

        // Editor selection + cursor
        if (this.textEditor.isActive()) {
            int editorRowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
            this.textEditor.setRenderContext(this.fontSize, this.currentSpread, editorRowsPerPage,
                    this.fontSize.getNext(1).getFont(), this.bookRenderer.getChapterLines());
        }

        // Story and audio info above book
        getMainPanel().spriteBatch.begin();
        if (isBackgroundGeneration()) {
            String genText = Localization.getInstance().getCommon().get(this.running.getId());
            getMainPanel().drawString(genText, Constants.GAME_WIDTH / 2f, 50, Constants.COLOR_WHITE, AssetLoader.font20, DrawString.MIDDLE, true, false);
        } else if (this.running != Running.CREATE_STORY) {
            if (this.choosenReadStory >= 0) {
                String fileLabel = (this.isSong ? "Song: " : "Story: ") + this.fileTXTHandles[this.choosenReadStory].file().getName();
                getMainPanel().drawString(fileLabel, Constants.GAME_WIDTH / 2f, 50, Constants.COLOR_WHITE, AssetLoader.font20, DrawString.MIDDLE, true, false);
            }
            if (this.choosenListenStory >= 0) {
                getMainPanel().drawString("Audio: " + this.fileMP3Handles[this.choosenListenStory].file().getName(), Constants.GAME_WIDTH / 2f, 70, Constants.COLOR_WHITE, AssetLoader.font15, DrawString.MIDDLE, false, false);
            }
        }

        // Page content
        int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
        int leftStart = this.currentSpread * 2 * rowsPerPage;
        int rightStart = leftStart + rowsPerPage;
        ArrayList<String> displayLines = getActiveDisplayLines();

        if (displayLines != null && !displayLines.isEmpty()) {
            if (pageAnimation.isAnimating()) {
                // Calculate old and new spread line positions
                int oldLeftStart = this.currentSpread * 2 * rowsPerPage;
                int oldRightStart = oldLeftStart + rowsPerPage;
                int newLeftStart = this.pendingSpread * 2 * rowsPerPage;
                int newRightStart = newLeftStart + rowsPerPage;

                bookRenderer.renderTurnAnimation(pageAnimation, displayLines,
                        oldLeftStart, oldRightStart,
                        newRightStart, newLeftStart,
                        rowsPerPage, this.fontSize);
            } else {
                bookRenderer.renderPageText(displayLines, leftStart, rowsPerPage, this.fontSize, true);
                bookRenderer.renderPageText(displayLines, rightStart, rowsPerPage, this.fontSize, false);
            }

            // Editor selection + cursor
            if (this.textEditor.isActive() && !pageAnimation.isAnimating()) {
                getMainPanel().spriteBatch.end();
                Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
                this.textEditor.render(getMainPanel(), 0, 0);
                Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);
                getMainPanel().spriteBatch.begin();
            }

            // Page numbers - during animation, show numbers matching visible content per phase
            int totalPages = (int) Math.ceil((double) displayLines.size() / rowsPerPage);
            if (pageAnimation.isAnimating()) {
                float progress = pageAnimation.getProgress();
                boolean isForward = pageAnimation.getDirection() == PageTurnAnimation.Direction.FORWARD;
                boolean inPhase2 = progress > 0.5f;

                int leftSpread = (isForward && inPhase2) ? this.pendingSpread : this.currentSpread;
                int rightSpread = (isForward && !inPhase2) ? this.currentSpread : this.pendingSpread;
                if (!isForward) {
                    leftSpread = (!inPhase2) ? this.currentSpread : this.pendingSpread;
                    rightSpread = inPhase2 ? this.pendingSpread : this.currentSpread;
                }

                bookRenderer.renderPageNumbers(leftSpread * 2 + 1, rightSpread * 2 + 2, totalPages, this.fontSize);
            } else {
                int leftPageNum = this.currentSpread * 2 + 1;
                int rightPageNum = this.currentSpread * 2 + 2;
                bookRenderer.renderPageNumbers(leftPageNum, rightPageNum, totalPages, this.fontSize);
            }
        }

        drawGameObjectives();

        getMainPanel().drawString("Version: " + Constants.VERSION, Constants.GAME_WIDTH / 2f, Constants.GAME_HEIGHT - 20f, Constants.COLOR_WHITE, AssetLoader.font15, DrawString.MIDDLE, false, false);
        getMainPanel().spriteBatch.end();

        // Semi-transparent backgrounds behind button rows
        renderButtonBackgrounds();

        // Buttons
        for (ApoButton button : this.getMainPanel().getButtons()) {
            button.render(this.getMainPanel());
        }

        // Loading overlay
        if (this.running != Running.NONE) {
            int width = 700;
            float overlayX = Constants.GAME_WIDTH / 2f - width / 2f;
            float overlayY;
            int overlayHeight;

            if (isBackgroundGeneration()) {
                int row1Y = audioCapable ? ROW1_Y_NORMAL : ROW1_Y_EXTENDED;
                overlayY = row1Y + ROW_HEIGHT + 10;
                overlayHeight = Math.min(100, Constants.GAME_HEIGHT - (int) overlayY - 5);
            } else {
                overlayY = Constants.GAME_HEIGHT / 2f - 50;
                overlayHeight = 100;
            }

            Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
            getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
            getMainPanel().getRenderer().setColor(Constants.COLOR_WHITE[0], Constants.COLOR_WHITE[1], Constants.COLOR_WHITE[2], 0.8f);
            getMainPanel().getRenderer().roundedRect(overlayX, overlayY, width, overlayHeight, 10);
            getMainPanel().getRenderer().end();
            Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

            getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Line);
            getMainPanel().getRenderer().setColor(Constants.COLOR_BLACK[0], Constants.COLOR_BLACK[1], Constants.COLOR_BLACK[2], 1f);
            getMainPanel().getRenderer().roundedRectLine(overlayX, overlayY, width, overlayHeight, 10);
            getMainPanel().getRenderer().end();

            float textY = overlayY + Math.max(5, overlayHeight / 2f - 30);
            getMainPanel().spriteBatch.begin();
            getMainPanel().drawString(Localization.getInstance().getCommon().get(this.running.getId()), Constants.GAME_WIDTH / 2f, textY, Constants.COLOR_BLACK, AssetLoader.font40, DrawString.MIDDLE, false, false);
            if (!this.statusText.isEmpty()) {
                getMainPanel().drawString(this.statusText, Constants.GAME_WIDTH / 2f, textY + 35, Constants.COLOR_BLACK, AssetLoader.font20, DrawString.MIDDLE, false, false);
            }
            getMainPanel().spriteBatch.end();
        }
    }

    private void renderWordHighlight() {
        if (this.highlighter == null || this.highlighter.getCurrentWordIndex() < 0) return;
        ArrayList<String> lines = getActiveDisplayLines();
        if (lines == null || lines.isEmpty()) return;

        int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
        int leftStart = this.currentSpread * 2 * rowsPerPage;

        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
        this.highlighter.renderHighlight(getMainPanel().getRenderer(), this.fontSize, leftStart, rowsPerPage);
        getMainPanel().getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);
    }


    private void renderButtonBackgrounds() {
        boolean bgGen = isBackgroundGeneration();
        if (this.running != Running.NONE && !bgGen) return;

        int row1Y = audioCapable ? ROW1_Y_NORMAL : ROW1_Y_EXTENDED;

        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);

        // Row 1: Page navigation background (centered, compact)
        getMainPanel().getRenderer().setColor(0f, 0f, 0f, 0.5f);
        getMainPanel().getRenderer().roundedRect(ROW_BG_X, row1Y, ROW_BG_WIDTH, ROW_HEIGHT, 10);

        // Row 2: Audio background (only if audio capable and not in background generation)
        if (audioCapable && !bgGen) {
            getMainPanel().getRenderer().setColor(0f, 0f, 0f, 0.5f);
            getMainPanel().getRenderer().roundedRect(ROW_BG_X, ROW2_Y, ROW_BG_WIDTH, ROW_HEIGHT, 10);
        }

        getMainPanel().getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        // Label and duration on audio row (only when MP3 exists and not in background generation)
        if (!bgGen && (this.choosenListenStory >= 0 || (this.songVariantHandles != null && this.songVariantHandles.length > 0))) {
            float textY = ROW2_Y + ROW_HEIGHT / 2f - 12;
            getMainPanel().spriteBatch.begin();
            String label;
            if (this.isSong) {
                label = Localization.getInstance().getCommon().get("listen_song");
            } else {
                label = Localization.getInstance().getCommon().get("listen_music");
            }
            getMainPanel().drawString(label, ROW_BG_X + 15, textY, Constants.COLOR_WHITE, AssetLoader.font25, DrawString.BEGIN, false, false);

            if (this.music != null && this.totalDuration > 0) {
                String timeDisplay = formatTime(getPlaybackPosition()) + " / " + formatTime(this.totalDuration);
                getMainPanel().drawString(timeDisplay, ROW_BG_X + ROW_BG_WIDTH - 15, textY, Constants.COLOR_WHITE, AssetLoader.font25, DrawString.END, false, false);
            }
            getMainPanel().spriteBatch.end();
        }
    }

    private void drawGameObjectives() {
        int iconY = BookRenderer.LEFT_PAGE_Y + BookRenderer.TEXT_PADDING;
        GameObjectives go = this.getMainPanel().getPromptObject().getGameObjectives();
        if (go.getMainCharacter() != null) {
            getMainPanel().spriteBatch.draw(go.getMainCharacter().getImage(), 10, iconY, 45, 45);
            getMainPanel().spriteBatch.draw(go.getMainCharacter().getImage(), Constants.GAME_WIDTH - 55, iconY, 45, 45);
        }
        if (go.getSupportingCharacter() != null) {
            getMainPanel().spriteBatch.draw(go.getSupportingCharacter().getImage(), 10, iconY + 50, 45, 45);
            getMainPanel().spriteBatch.draw(go.getSupportingCharacter().getImage(), Constants.GAME_WIDTH - 55, iconY + 50, 45, 45);
        }
        if (go.getUniverse() != null) {
            getMainPanel().spriteBatch.draw(go.getUniverse().getImage(), 10, iconY + 100, 45, 45);
            getMainPanel().spriteBatch.draw(go.getUniverse().getImage(), Constants.GAME_WIDTH - 55, iconY + 100, 45, 45);
        }
        if (go.getPlaces() != null) {
            getMainPanel().spriteBatch.draw(go.getPlaces().getImage(), 10, iconY + 150, 45, 45);
            getMainPanel().spriteBatch.draw(go.getPlaces().getImage(), Constants.GAME_WIDTH - 55, iconY + 150, 45, 45);
        }
        if (go.getObjectives() != null) {
            getMainPanel().spriteBatch.draw(go.getObjectives().getImage(), 10, iconY + 200, 45, 45);
            getMainPanel().spriteBatch.draw(go.getObjectives().getImage(), Constants.GAME_WIDTH - 55, iconY + 200, 45, 45);
        }
    }

    @Override
    public void dispose() {
        stopMusic();
        if (this.music != null) {
            this.music.dispose();
            this.music = null;
        }
    }

}
