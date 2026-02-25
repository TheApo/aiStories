package com.apogames.aistories.game.listenStories;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.MainPanel;
import com.apogames.aistories.game.main.*;
import com.apogames.aistories.game.objects.CustomEntity;
import com.apogames.aistories.game.objects.EnumInterface;
import com.apogames.aistories.game.objects.GameObjectives;
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

    private int changeY = 0;

    private Running running = Running.NONE;

    private String nextText = null;
    private Running nextRunning;
    private boolean reload = false;

    public ListenStories(final MainPanel game) {
        super(game);
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
            this.textArea = new TextArea(100, 100, 1200, 400, "TextArea", "");
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
        this.changeY = 0;
        this.textArea.setText(text);
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
        this.changeY = 0;

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
        if (this.changeY + add * this.textArea.getRows() >= 0 && this.changeY + add * this.textArea.getRows() < this.textArea.getMyText().size()) {
            this.changeY += add * this.textArea.getRows();
        }
    }

    private void setUpFont(int add) {
        this.changeY = 0;
        this.fontSize = this.fontSize.getNext(add);
        this.textArea.setAdd(this.fontSize.getAdd());
        this.textArea.setFont(this.fontSize.getFont());
        this.textArea.setText(this.textArea.getText());

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
        if (this.nextText != null) {
            String text = this.nextText;
            //Gdx.app.log("Text", "setText: " + text);
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
        getMainPanel().spriteBatch.begin();

        getMainPanel().spriteBatch.draw(AssetLoader.backgroundTextureRegion, 0, 0);

        getMainPanel().spriteBatch.end();

        Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
        int width = 700;
        getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
        if (this.running != Running.CREATE_STORY) {
            getMainPanel().getRenderer().setColor(Constants.COLOR_WHITE[0], Constants.COLOR_WHITE[1], Constants.COLOR_WHITE[2], 0.3f);
            getMainPanel().getRenderer().roundedRect(Constants.GAME_WIDTH / 2f - width / 2f, Constants.GAME_HEIGHT - 200, width, 180, 10);
        }
        this.textArea.renderFilled(this.getMainPanel(), 0, 0);

        getMainPanel().getRenderer().end();
        Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

        getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Line);
        if (this.running != Running.CREATE_STORY) {
            getMainPanel().getRenderer().setColor(Constants.COLOR_BLACK[0], Constants.COLOR_BLACK[1], Constants.COLOR_BLACK[2], 1f);
            getMainPanel().getRenderer().roundedRectLine(Constants.GAME_WIDTH/2f - width/2f, Constants.GAME_HEIGHT - 200, width, 180, 10);
        }

        this.textArea.renderLine(this.getMainPanel(), 0, 0);
        getMainPanel().getRenderer().end();

        getMainPanel().spriteBatch.begin();

        if (this.running != Running.CREATE_STORY) {
            if (this.choosenReadStory >= 0) {
                getMainPanel().drawString("Story: " + this.fileTXTHandles[this.choosenReadStory].file().getName(), Constants.GAME_WIDTH / 2f, 50, Constants.COLOR_WHITE, AssetLoader.font25, DrawString.MIDDLE, true, false);
            }

            if (this.choosenListenStory >= 0) {
                getMainPanel().drawString("Audio: " + this.fileMP3Handles[this.choosenListenStory].file().getName(), Constants.GAME_WIDTH / 2f, Constants.GAME_HEIGHT - 180f, Constants.COLOR_BLACK, AssetLoader.font20, DrawString.MIDDLE, false, false);
            }
        }
        getMainPanel().drawString("Version: " + Constants.VERSION, Constants.GAME_WIDTH / 2f, Constants.GAME_HEIGHT - 20f, Constants.COLOR_WHITE, AssetLoader.font15, DrawString.MIDDLE, false, false);

        this.textArea.renderSprite(this.getMainPanel(), 0, 0, changeY);

        drawGameObjectives();

        getMainPanel().spriteBatch.end();

        for (ApoButton button : this.getMainPanel().getButtons()) {
            button.render(this.getMainPanel());
        }

        if (this.running != Running.NONE) {
            width = 600;

            Gdx.graphics.getGL20().glEnable(GL20.GL_BLEND);
            getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Filled);
            getMainPanel().getRenderer().setColor(Constants.COLOR_WHITE[0], Constants.COLOR_WHITE[1], Constants.COLOR_WHITE[2], 0.8f);
            getMainPanel().getRenderer().roundedRect(Constants.GAME_WIDTH/2f - width/2f, Constants.GAME_HEIGHT/2f - 50, width, 100, 10);
            getMainPanel().getRenderer().end();
            Gdx.graphics.getGL20().glDisable(GL20.GL_BLEND);

            getMainPanel().getRenderer().begin(ShapeRenderer.ShapeType.Line);
            getMainPanel().getRenderer().setColor(Constants.COLOR_BLACK[0], Constants.COLOR_BLACK[1], Constants.COLOR_BLACK[2], 1f);
            getMainPanel().getRenderer().roundedRectLine(Constants.GAME_WIDTH/2f - width/2f, Constants.GAME_HEIGHT/2f - 50, width, 100, 10);
            getMainPanel().getRenderer().end();

            getMainPanel().spriteBatch.begin();
            getMainPanel().drawString(Localization.getInstance().getCommon().get(this.running.getId()), Constants.GAME_WIDTH / 2f, Constants.GAME_HEIGHT/2f - 20, Constants.COLOR_BLACK, AssetLoader.font40, DrawString.MIDDLE, false, false);
            getMainPanel().spriteBatch.end();
        }
    }

    private void drawGameObjectives() {
        if (this.getMainPanel().getPromptObject().getGameObjectives().getMainCharacter() != null) {
            getMainPanel().spriteBatch.draw(this.getMainPanel().getPromptObject().getGameObjectives().getMainCharacter().getImage(), 10, this.textArea.getY(), 80, 80);
            getMainPanel().spriteBatch.draw(this.getMainPanel().getPromptObject().getGameObjectives().getMainCharacter().getImage(), Constants.GAME_WIDTH - 90, this.textArea.getY(), 80, 80);
        }
        if (this.getMainPanel().getPromptObject().getGameObjectives().getSupportingCharacter() != null) {
            getMainPanel().spriteBatch.draw(this.getMainPanel().getPromptObject().getGameObjectives().getSupportingCharacter().getImage(), 10, this.textArea.getY() + 80, 80, 80);
            getMainPanel().spriteBatch.draw(this.getMainPanel().getPromptObject().getGameObjectives().getSupportingCharacter().getImage(), Constants.GAME_WIDTH - 90, this.textArea.getY() + 80, 80, 80);
        }
        if (this.getMainPanel().getPromptObject().getGameObjectives().getUniverse() != null) {
            getMainPanel().spriteBatch.draw(this.getMainPanel().getPromptObject().getGameObjectives().getUniverse().getImage(), 10, this.textArea.getY() + 2 * 80, 80, 80);
            getMainPanel().spriteBatch.draw(this.getMainPanel().getPromptObject().getGameObjectives().getUniverse().getImage(), Constants.GAME_WIDTH - 90, this.textArea.getY() + 2 * 80, 80, 80);
        }
        if (this.getMainPanel().getPromptObject().getGameObjectives().getPlaces() != null) {
            getMainPanel().spriteBatch.draw(this.getMainPanel().getPromptObject().getGameObjectives().getPlaces().getImage(), 10, this.textArea.getY() + 3 * 80, 80, 80);
            getMainPanel().spriteBatch.draw(this.getMainPanel().getPromptObject().getGameObjectives().getPlaces().getImage(), Constants.GAME_WIDTH - 90, this.textArea.getY() + 3 * 80, 80, 80);
        }
        if (this.getMainPanel().getPromptObject().getGameObjectives().getObjectives() != null) {
            getMainPanel().spriteBatch.draw(this.getMainPanel().getPromptObject().getGameObjectives().getObjectives().getImage(), 10, this.textArea.getY() + 4 * 80, 80, 80);
            getMainPanel().spriteBatch.draw(this.getMainPanel().getPromptObject().getGameObjectives().getObjectives().getImage(), Constants.GAME_WIDTH - 90, this.textArea.getY() + 4 * 80, 80, 80);
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
