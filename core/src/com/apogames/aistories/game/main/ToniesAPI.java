package com.apogames.aistories.game.main;

import com.apogames.aistories.game.main.tonie.Chapter;
import com.apogames.aistories.game.main.tonie.CreativeTonie;
import com.apogames.aistories.game.main.tonie.Household;
import com.apogames.aistories.game.main.tonie.TonieHandler;
import com.badlogic.gdx.Gdx;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ToniesAPI {

    public static String USERNAME;
    public static String PASSWORD;

    private final TonieHandler tonieHandler;

    @Getter
    private List<CreativeTonie> creativeTonies = new ArrayList<>();

    private final List<List<Chapter>> chapters = new ArrayList<>();

    public static void main(String[] args) {
    }

    public ToniesAPI() {
        tonieHandler = new TonieHandler();
    }

    public void connect() {
        try {
            tonieHandler.login(USERNAME, PASSWORD);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            tonieHandler.disconnect();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public void receiveEverything() {
        try {
            List<Household> households = tonieHandler.getHouseholds();

            this.creativeTonies = tonieHandler.getCreativeTonies(households.get(0));

            this.chapters.clear();
            for (int i = 0; i < this.creativeTonies.size(); i++) {
                chapters.add(new ArrayList<>());
                Chapter[] chapters = creativeTonies.get(i).getChapters();
                for (Chapter chapter : chapters) {
                    this.chapters.get(i).add(chapter);
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public void deleteByTitle(int tonieID, String title) {
        try {
            CreativeTonie creativeTonie = creativeTonies.get(tonieID);
            creativeTonie.deleteChapter(creativeTonie.findChapterByTitle(title));
            creativeTonie.commit();
            // Refresh and get the latest states of the tonie
            creativeTonie.refresh();
            this.receiveEverything();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public List<List<Chapter>> getTitles() {
        return chapters;
    }

    public void testUpload() {
        String extRoot = Gdx.files.getExternalStoragePath();
        Gdx.app.log("tonie", "String extRoot: " + extRoot);

        File myObj = new File(extRoot+"Geschichte.mp3");
        Gdx.app.log("tonie", "File myObj: " + myObj);

        this.uploadOnTonie(myObj);
    }

    public void uploadOnTonie(File file) {
        uploadOnTonie(file, 1);
    }

    public void uploadOnTonie(File file, int creativeTonieID) {
        TonieHandler tonieHandler = new TonieHandler();

        try {
            tonieHandler.login(USERNAME, PASSWORD);
            // get all households you're in & select first one
            List<Household> households = tonieHandler.getHouseholds();
            Household household = households.get(0);

            // get all creative tonies & select chosen one
            List<CreativeTonie> creativeTonies = tonieHandler.getCreativeTonies(household);
            CreativeTonie creativeTonie = creativeTonies.get(creativeTonieID);

            // upload the MP3 to the tonie box
            creativeTonie.uploadFile(file.getName(), file.getAbsolutePath());
            creativeTonie.commit();

            // Refresh and get the latest states of the tonie
            creativeTonie.refresh();
            Gdx.app.log("tonie", "Fertig mit dem ToniesBox Upload");
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        try {
            tonieHandler.disconnect();
        }  catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public void listAllTracksOnTonies() {
        TonieHandler tonieHandler = new TonieHandler();

        try {
            tonieHandler.login(USERNAME, PASSWORD);
            List<Household> households = tonieHandler.getHouseholds();
            for (Household household : households) {
                Gdx.app.log("tonie", "Household: " + household.getName());

                List<CreativeTonie> creativeTonies = tonieHandler.getCreativeTonies(household);
                for (CreativeTonie creativeTonie : creativeTonies) {
                    Gdx.app.log("tonie", "Tonie: " + creativeTonie.getName());
                    Chapter[] chapters = creativeTonie.getChapters();
                    for (Chapter chapter : chapters) {
                        Gdx.app.log("tonie", "Chapter: " + chapter.getId() + "\t" + chapter.getTitle());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            tonieHandler.disconnect();
        }  catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
