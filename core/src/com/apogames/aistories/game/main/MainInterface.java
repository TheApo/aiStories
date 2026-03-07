package com.apogames.aistories.game.main;

import com.badlogic.gdx.files.FileHandle;

public interface MainInterface {

    void setFileHandle(FileHandle file);

    void setRunning(Running running);

    void setStatusText(String statusText);

    void setTextForTextArea(String text);

    void uploadToTonieBox(FileHandle fileHandle);

    void onImageReceived(String sectionIdentifier, String imageUrl);

    Prompt getPrompt();

}
