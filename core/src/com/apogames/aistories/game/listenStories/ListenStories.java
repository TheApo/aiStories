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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private Running running = Running.NONE;

    private String nextText = null;
    private Running nextRunning;
    private boolean reload = false;

    public ListenStories(final MainPanel game) {
        super(game);
        this.bookRenderer = new BookRenderer(game);
    }

    public void setNeededButtonsVisible() {
        getMainPanel().getButtonByFunction(FUNCTION_BACK).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_PLAY).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_STOP).setVisible(true);
        getMainPanel().getButtonByFunction(FUNCTION_PAUSE).setVisible(true);
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

        if (this.textArea == null) {
            this.textArea = new TextArea(0, 0, bookRenderer.getTextAreaWidth(), BookRenderer.PAGE_HEIGHT, "TextArea", "");
        }
        this.textArea.setFont(fontSize.getFont());
        this.setUpFont(0);

        this.choosenReadStory = -1;
        if (this.fileTXTHandles.length > 0) {
            this.choosenReadStory = this.fileTXTHandles.length - 1;
            if (chooseStory >= 0) {
                this.choosenReadStory = chooseStory;
            }
            String text = this.fileTXTHandles[this.choosenReadStory].readString();
            this.setTextInTextArea(text);
        }
        findListenStory();
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
                int padding = rowsPerPage - posInPage;
                for (int j = 0; j < padding; j++) {
                    lines.add(i, "");
                }
                i += padding;
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

    private EnumInterface resolveEntity(String name, EnumInterface current, CustomEntity custom) {
        if (custom != null && custom.getName().equals(name)) {
            return custom;
        }
        return current.getEnumByName(name);
    }

    public void createNewStory() {
        this.running = Running.CREATE_STORY;

        this.setButtonsInsivislbe();
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
                if (this.music.isPlaying()) {
                    this.music.stop();
                }
                this.music.dispose();
                this.music = null;
                System.gc();
            }
            FileHandle mp3FileHandle = this.fileMP3Handles[this.choosenListenStory];
            File mp3File = mp3FileHandle.file();

            Path path = Paths.get(mp3File.getAbsolutePath());
            try {
                Files.delete(path);
                System.out.println("File successfully deleted via NIO");
            } catch (Exception e) {
                System.out.println("Deletion failed via NIO: " + e.getMessage());
            }
        }
        this.fileTXTHandles[this.choosenReadStory].delete();
        int readStory = this.choosenReadStory;
        reloadFileHandler();
        this.choosenReadStory = readStory;
        this.nextStory(0);
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
        elevenlabIO.sendTextToSpeech(this.textArea.getText(), Prompt.DIRECTORY+searchName+".mp3");
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

        this.setTextInTextArea(this.fileTXTHandles[this.choosenReadStory].readString());

        findImageStory();
        findListenStory();
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
        this.music = null;
        int index = 0;
        for (FileHandle fileHandle : this.fileMP3Handles) {
            String fileName = fileHandle.file().getName();
            if (fileName.substring(0, fileName.length() - 4).equals(searchName)) {
                this.choosenListenStory = index;
                this.music = Gdx.audio.newMusic(this.fileMP3Handles[this.choosenListenStory]);
                break;
            }
            index += 1;
        }

        boolean visible = choosenListenStory != -1;

        getMainPanel().getButtonByFunction(FUNCTION_PLAY).setVisible(visible);
        getMainPanel().getButtonByFunction(FUNCTION_STOP).setVisible(visible);
        getMainPanel().getButtonByFunction(FUNCTION_PAUSE).setVisible(visible);
        getMainPanel().getButtonByFunction(FUNCTION_UPLOAD_TONIE).setVisible(visible);
        getMainPanel().getButtonByFunction(FUNCTION_CREATEMP3).setVisible(!visible);

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
        updatePageButtonVisibility();

        Gdx.graphics.requestRendering();
    }

    private void playMusic() {
        if (this.music != null) {
            this.music.play();
        }
    }

    private void pauseMusic() {
        if (this.music != null) {
            this.music.pause();
        }
    }

    private void stopMusic() {
        if (this.music != null) {
            this.music.stop();
        }
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
        if (pageAnimation.isAnimating()) {
            boolean finished = pageAnimation.update(delta);
            if (finished) {
                this.currentSpread = this.pendingSpread;
                Gdx.graphics.setContinuousRendering(false);
                updatePageButtonVisibility();
            }
            Gdx.graphics.requestRendering();
        }

        if (this.nextText != null) {
            String text = this.nextText;
            this.setTextInTextArea(text);

            this.saveText(text);

            reloadFileHandler();

            Gdx.graphics.requestRendering();

            this.nextText = null;
        } else if (this.nextRunning != null) {
            if (this.nextRunning == Running.NONE && this.running == Running.CREATE_STORY) {
                getMainPanel().getButtonByFunction(FUNCTION_NEXT_STORY).setVisible(true);
                getMainPanel().getButtonByFunction(FUNCTION_PREVIOUS_STORY).setVisible(true);
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
