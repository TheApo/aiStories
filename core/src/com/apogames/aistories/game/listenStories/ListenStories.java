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
    private WordHighlighter highlighter = null;
    private ArrayList<Integer> choosenImageStory = new ArrayList<>();
    private int choosenReadStory = -1;

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

    private static final int ROW1_Y_NORMAL = 580;
    private static final int ROW2_Y = 665;
    private static final int ROW1_Y_EXTENDED = 660;
    private static final int ROW_HEIGHT = 70;
    private static final int ROW_BG_WIDTH = 560;
    private static final int ROW_BG_X = Constants.GAME_WIDTH / 2 - ROW_BG_WIDTH / 2;

    private Running running = Running.NONE;
    private boolean audioCapable = false;

    private String nextText = null;
    private Running nextRunning;
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

        FontSize.refreshAll();
        this.getMainPanel().reInit();

        this.getMainPanel().resetSize(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);

        this.setNeededButtonsVisible();
        this.setButtonsVisibility();
        if (this.running == Running.NONE) {
            this.reloadFileHandler();
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
        if (text.contains(SEPARATOR_IN_FILE)) {
            String values = text.substring(0,text.indexOf(SEPARATOR_IN_FILE));
            String[] split = values.split(";");

            GameObjectives go = this.getMainPanel().getPromptObject().getGameObjectives();
            go.setMainCharacter(resolveEntity(split[0], go.getMainCharacter(), this.getMainPanel().getCustomMainEntity()));
            go.setSupportingCharacter(resolveEntity(split[1], go.getSupportingCharacter(), this.getMainPanel().getCustomSupportingEntity()));
            go.setUniverse(resolveEntity(split[2], go.getUniverse(), this.getMainPanel().getCustomUniverse()));
            go.setPlaces(resolveEntity(split[3], go.getPlaces(), this.getMainPanel().getCustomPlaces()));
            go.setObjectives(resolveEntity(split[4], go.getObjectives(), this.getMainPanel().getCustomObjectives()));
            text = text.substring(text.indexOf(SEPARATOR_IN_FILE) + SEPARATOR_IN_FILE.length());
        }
        this.currentSpread = 0;
        this.pendingSpread = 0;
        this.textArea.setText(cleanText(text));
        processAndLayoutChapters();
        updatePageButtonVisibility();
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
            String heading = fullText.toString()
                    .replaceFirst("(?i)(Kapitel|Chapter|Chapitre|Capitolo|Capítulo|Bölüm)\\s+", "");

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
        if (this.textArea == null || this.textArea.getMyText() == null) return;
        int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
        boolean canGoBack = this.currentSpread > 0;
        boolean canGoForward = (this.currentSpread + 1) * 2 * rowsPerPage < this.textArea.getMyText().size();
        getMainPanel().getButtonByFunction(FUNCTION_PREVIOUS_PAGE).setVisible(canGoBack);
        getMainPanel().getButtonByFunction(FUNCTION_NEXT_PAGE).setVisible(canGoForward);
    }

    private boolean hasAudioCapability() {
        boolean hasMp3 = this.choosenListenStory >= 0;
        boolean hasElevenLabs = ElvenlabIO.API_KEY != null && !ElvenlabIO.API_KEY.isEmpty()
                && !ElvenlabIO.API_KEY.equals("Dein ELEVENLABS_API_KEY");
        return hasMp3 || hasElevenLabs;
    }

    private void updateAudioButtonVisibility() {
        boolean hasMp3 = this.choosenListenStory >= 0;
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

    private String formatTime(float seconds) {
        int totalSeconds = (int) seconds;
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    private EnumInterface resolveEntity(String name, EnumInterface current, CustomEntity custom) {
        if (custom != null && custom.getName().equals(name)) {
            return custom;
        }
        return current.getEnumByName(name);
    }

    public void createNewStory() {
        this.running = Running.CREATE_STORY;
        this.currentSpread = 0;
        this.pendingSpread = 0;
        fitPromptToTwoPages();
        this.setButtonsInsivislbe();
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
    public void setTextForTextArea(String text) {
        this.nextText = text;
        Gdx.graphics.requestRendering();
    }

    private void saveText(String text) {
        String dirString = Prompt.DIRECTORY;
        String ids = this.getMainPanel().getPromptObject().getGameObjectives().getMainCharacter().getName()+";"+
                this.getMainPanel().getPromptObject().getGameObjectives().getSupportingCharacter().getName()+";"+
                this.getMainPanel().getPromptObject().getGameObjectives().getUniverse().getName()+";"+
                this.getMainPanel().getPromptObject().getGameObjectives().getPlaces().getName()+";"+
                this.getMainPanel().getPromptObject().getGameObjectives().getObjectives().getName()+SEPARATOR_IN_FILE;
        String fileName = dirString+this.prompt.getFileNameTxt();
        Gdx.app.log("SaveText", "saveText " + ids+" "+fileName);
        FileHandle fileHandle = Gdx.files.local(fileName);
        fileHandle.writeString(ids+text, false);
    }

    @Override
    public void keyPressed(int keyCode, char character) {
        super.keyPressed(keyCode, character);

        keys[keyCode] = true;
    }

    @Override
    public void keyButtonReleased(int keyCode, char character) {
        super.keyButtonReleased(keyCode, character);

        keys[keyCode] = false;
    }

    public void mouseMoved(int mouseX, int mouseY) {
    }

    public void mouseButtonReleased(int mouseX, int mouseY, boolean isRightButton) {
        this.isPressed = false;
    }

    public void mousePressed(int x, int y, boolean isRightButton) {
        if (isRightButton && !this.isPressed) {
            this.isPressed = true;
        }
    }

    public void mouseDragged(int x, int y, boolean isRightButton) {
        if (isRightButton) {
            if (!this.isPressed) {
                this.mousePressed(x, y, isRightButton);
            }
        }
    }

    @Override
    public void mouseButtonFunction(String function) {
        super.mouseButtonFunction(function);

        if (this.running != Running.NONE) {
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
                this.createMissingMp3();
                break;
        }
    }

    private void deleteCurrentFileAndText() {
        if (this.choosenListenStory >= 0) {
            if (this.music != null) {
                Gdx.app.log("DELETE", "Stopping and disposing Music object");
                this.music.stop();
                this.music.dispose();
                this.music = null;
            }
            this.highlighter = null;
            this.timingData = null;

            FileHandle mp3FileHandle = this.fileMP3Handles[this.choosenListenStory];
            File mp3File = mp3FileHandle.file();
            Gdx.app.log("DELETE", "MP3 path: " + mp3File.getAbsolutePath());
            Gdx.app.log("DELETE", "MP3 exists: " + mp3File.exists() + ", canWrite: " + mp3File.canWrite() + ", length: " + mp3File.length());

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

        ElvenlabIO elevenlabIO = new ElvenlabIO(this);
        elevenlabIO.sendTextToSpeech(buildTtsText(), Prompt.DIRECTORY+searchName+".mp3");
    }

    private String buildTtsText() {
        StringBuilder sb = new StringBuilder();
        for (String line : this.textArea.getMyText()) {
            if (line.trim().isEmpty()) continue;
            if (sb.length() > 0) sb.append(" ");
            sb.append(line);
        }
        return sb.toString();
    }

    private void nextStory(int add) {
        this.stopMusic();
        this.currentSpread = 0;
        this.pendingSpread = 0;

        this.choosenReadStory += add;

        if (this.choosenReadStory < 0) {
            this.choosenReadStory = this.fileTXTHandles.length - 1;
        } else if (this.choosenReadStory >= this.fileTXTHandles.length) {
            this.choosenReadStory = 0;
        }

        findImageStory();
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
        if (this.music != null) {
            this.music.stop();
            this.music.dispose();
            this.music = null;
        }
        this.totalDuration = 0f;
        this.timingData = null;
        this.highlighter = null;
        int index = 0;
        for (FileHandle fileHandle : this.fileMP3Handles) {
            String fileName = fileHandle.file().getName();
            if (fileName.substring(0, fileName.length() - 4).equals(searchName)) {
                this.choosenListenStory = index;
                this.music = Gdx.audio.newMusic(this.fileMP3Handles[this.choosenListenStory]);
                this.music.setOnCompletionListener(m -> {
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
                // Load word timing data if JSON exists
                String jsonPath = Prompt.DIRECTORY + searchName + ".json";
                this.timingData = WordTimingData.loadFromFile(jsonPath);
                Gdx.app.log("ListenStories", "Word timing JSON " + (this.timingData != null ? "loaded (" + this.timingData.getWords().size() + " words)" : "not found") + " for " + searchName);
                break;
            }
            index += 1;
        }

        boolean hasMp3 = choosenListenStory != -1;
        getMainPanel().getButtonByFunction(FUNCTION_UPLOAD_TONIE).setVisible(hasMp3);
        getMainPanel().getButtonByFunction(FUNCTION_CREATEMP3).setVisible(!hasMp3);

        updateLayout();
        updateAudioButtonVisibility();
        this.setVisibleFalseWhenNotSet();
    }

    private void nextPage(int add) {
        if (pageAnimation.isAnimating()) return;

        int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
        int newSpread = this.currentSpread + add;
        int newLeftStart = newSpread * 2 * rowsPerPage;

        if (newSpread >= 0 && newLeftStart < this.textArea.getMyText().size()) {
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
        processAndLayoutChapters();
        rebuildHighlighterMapping();
        updatePageButtonVisibility();

        Gdx.graphics.requestRendering();
    }

    private void playMusic() {
        if (this.music != null) {
            this.music.play();
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
        if (this.highlighter != null && this.textArea != null && this.textArea.getMyText() != null) {
            this.highlighter.buildMapping(
                    this.textArea.getMyText(),
                    this.fontSize.getFont(),
                    this.fontSize.getNext(1).getFont(),
                    this.bookRenderer.getChapterLines());
        }
    }

    private void pauseMusic() {
        if (this.music != null) {
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
        if (this.music != null) {
            this.music.dispose();
        }
        getMainPanel().changeToMenu();
    }

    @Override
    public void doThink(float delta) {
        if (this.music != null && this.music.isPlaying()) {
            int currentSecond = (int) this.music.getPosition();
            if (currentSecond != this.lastRenderedSecond) {
                this.lastRenderedSecond = currentSecond;
                Gdx.graphics.requestRendering();
            }
            // Update word highlighter and auto-page-turn
            if (this.highlighter != null && !pageAnimation.isAnimating()) {
                this.highlighter.update(this.music.getPosition());
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
            this.fontSize = FontSize.FONT_25;
            this.setTextInTextArea(text);

            this.saveText(text);

            reloadFileHandler();

            Gdx.graphics.requestRendering();

            this.nextText = null;
        } else if (this.nextRunning != null) {
            if (this.nextRunning == Running.NONE && this.running == Running.CREATE_STORY) {
                getMainPanel().getButtonByFunction(FUNCTION_NEXT_STORY).setVisible(true);
                getMainPanel().getButtonByFunction(FUNCTION_PREVIOUS_STORY).setVisible(true);
                getMainPanel().getButtonByFunction(FUNCTION_DELETE).setVisible(true);
                this.reloadFileHandler();
            }

            this.running = this.nextRunning;

            Gdx.graphics.requestRendering();

            this.nextRunning = null;
        } else if (this.reload) {
            reloadFileHandler(this.choosenReadStory);

            this.reload = false;
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

        // Story and audio info above book
        getMainPanel().spriteBatch.begin();
        if (this.running != Running.CREATE_STORY) {
            if (this.choosenReadStory >= 0) {
                getMainPanel().drawString("Story: " + this.fileTXTHandles[this.choosenReadStory].file().getName(), Constants.GAME_WIDTH / 2f, 50, Constants.COLOR_WHITE, AssetLoader.font20, DrawString.MIDDLE, true, false);
            }
            if (this.choosenListenStory >= 0) {
                getMainPanel().drawString("Audio: " + this.fileMP3Handles[this.choosenListenStory].file().getName(), Constants.GAME_WIDTH / 2f, 70, Constants.COLOR_WHITE, AssetLoader.font15, DrawString.MIDDLE, false, false);
            }
        }

        // Page content
        int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
        int leftStart = this.currentSpread * 2 * rowsPerPage;
        int rightStart = leftStart + rowsPerPage;

        if (this.textArea.getMyText() != null && !this.textArea.getMyText().isEmpty()) {
            if (pageAnimation.isAnimating()) {
                // Calculate old and new spread line positions
                int oldLeftStart = this.currentSpread * 2 * rowsPerPage;
                int oldRightStart = oldLeftStart + rowsPerPage;
                int newLeftStart = this.pendingSpread * 2 * rowsPerPage;
                int newRightStart = newLeftStart + rowsPerPage;

                bookRenderer.renderTurnAnimation(pageAnimation, this.textArea.getMyText(),
                        oldLeftStart, oldRightStart,
                        newRightStart, newLeftStart,
                        rowsPerPage, this.fontSize);
            } else {
                bookRenderer.renderPageText(this.textArea.getMyText(), leftStart, rowsPerPage, this.fontSize, true);
                bookRenderer.renderPageText(this.textArea.getMyText(), rightStart, rowsPerPage, this.fontSize, false);
            }

            // Page numbers - during animation, show numbers matching visible content per phase
            int totalPages = (int) Math.ceil((double) this.textArea.getMyText().size() / rowsPerPage);
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
            int width = 600;

            Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
            getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
            getMainPanel().getRenderer().setColor(Constants.COLOR_WHITE[0], Constants.COLOR_WHITE[1], Constants.COLOR_WHITE[2], 0.8f);
            getMainPanel().getRenderer().roundedRect(Constants.GAME_WIDTH / 2f - width / 2f, Constants.GAME_HEIGHT / 2f - 50, width, 100, 10);
            getMainPanel().getRenderer().end();
            Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

            getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Line);
            getMainPanel().getRenderer().setColor(Constants.COLOR_BLACK[0], Constants.COLOR_BLACK[1], Constants.COLOR_BLACK[2], 1f);
            getMainPanel().getRenderer().roundedRectLine(Constants.GAME_WIDTH / 2f - width / 2f, Constants.GAME_HEIGHT / 2f - 50, width, 100, 10);
            getMainPanel().getRenderer().end();

            getMainPanel().spriteBatch.begin();
            getMainPanel().drawString(Localization.getInstance().getCommon().get(this.running.getId()), Constants.GAME_WIDTH / 2f, Constants.GAME_HEIGHT / 2f - 20, Constants.COLOR_BLACK, AssetLoader.font40, DrawString.MIDDLE, false, false);
            getMainPanel().spriteBatch.end();
        }
    }

    private void renderWordHighlight() {
        if (this.highlighter == null || this.highlighter.getCurrentWordIndex() < 0) return;
        if (this.textArea == null || this.textArea.getMyText() == null) return;

        int rowsPerPage = bookRenderer.getRowsPerPage(this.fontSize);
        int leftStart = this.currentSpread * 2 * rowsPerPage;

        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
        this.highlighter.renderHighlight(getMainPanel().getRenderer(), this.fontSize, leftStart, rowsPerPage);
        getMainPanel().getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);
    }

    private void renderButtonBackgrounds() {
        if (this.running != Running.NONE) return;

        int row1Y = audioCapable ? ROW1_Y_NORMAL : ROW1_Y_EXTENDED;

        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);

        // Row 1: Page navigation background (centered, compact)
        getMainPanel().getRenderer().setColor(0f, 0f, 0f, 0.5f);
        getMainPanel().getRenderer().roundedRect(ROW_BG_X, row1Y, ROW_BG_WIDTH, ROW_HEIGHT, 10);

        // Row 2: Audio background (only if audio capable)
        if (audioCapable) {
            getMainPanel().getRenderer().setColor(0f, 0f, 0f, 0.5f);
            getMainPanel().getRenderer().roundedRect(ROW_BG_X, ROW2_Y, ROW_BG_WIDTH, ROW_HEIGHT, 10);
        }

        getMainPanel().getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        // Label and duration on audio row (only when MP3 exists)
        if (this.choosenListenStory >= 0) {
            float textY = ROW2_Y + ROW_HEIGHT / 2f - 12;
            getMainPanel().spriteBatch.begin();
            String label = Localization.getInstance().getCommon().get("listen_music");
            getMainPanel().drawString(label, ROW_BG_X + 15, textY, Constants.COLOR_WHITE, AssetLoader.font25, DrawString.BEGIN, false, false);

            if (this.music != null && this.totalDuration > 0) {
                String timeDisplay = formatTime(this.music.getPosition()) + " / " + formatTime(this.totalDuration);
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
