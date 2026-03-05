package com.apogames.aistories.game.customEntity;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.List;

public class CustomImageManager {

    private static final int IMAGE_SIZE = 200;
    private static final String BASE_DIR = "custom_images";

    private final String entityType;
    private final List<TextureRegion> textures = new ArrayList<>();
    private final List<String> fileNames = new ArrayList<>();

    public CustomImageManager(String entityType) {
        this.entityType = entityType;
        loadExistingImages();
    }

    private FileHandle getDirectory() {
        return Gdx.files.local(BASE_DIR + "/" + entityType);
    }

    private void loadExistingImages() {
        textures.clear();
        fileNames.clear();
        FileHandle dir = getDirectory();
        if (!dir.exists()) {
            return;
        }
        FileHandle[] files = dir.list(".png");
        java.util.Arrays.sort(files, (a, b) -> a.name().compareTo(b.name()));
        for (FileHandle file : files) {
            try {
                Texture tex = new Texture(file);
                tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                TextureRegion region = new TextureRegion(tex, 0, 0, tex.getWidth(), tex.getHeight());
                region.flip(false, true);
                textures.add(region);
                fileNames.add(file.name());
            } catch (Exception e) {
                Gdx.app.error("CustomImageManager", "Failed to load " + file.name(), e);
            }
        }
    }

    public int addImage(byte[] pngData) {
        FileHandle dir = getDirectory();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = "img_" + System.currentTimeMillis() + ".png";
        FileHandle file = dir.child(fileName);

        Pixmap original = new Pixmap(pngData, 0, pngData.length);
        Pixmap scaled;
        if (original.getWidth() != IMAGE_SIZE || original.getHeight() != IMAGE_SIZE) {
            scaled = new Pixmap(IMAGE_SIZE, IMAGE_SIZE, original.getFormat());
            scaled.setFilter(Pixmap.Filter.BiLinear);
            scaled.drawPixmap(original,
                    0, 0, original.getWidth(), original.getHeight(),
                    0, 0, IMAGE_SIZE, IMAGE_SIZE);
            original.dispose();
        } else {
            scaled = original;
        }

        PixmapIO.writePNG(file, scaled);
        scaled.dispose();

        Texture tex = new Texture(file);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TextureRegion region = new TextureRegion(tex, 0, 0, tex.getWidth(), tex.getHeight());
        region.flip(false, true);
        textures.add(region);
        fileNames.add(fileName);

        return textures.size() - 1;
    }

    public void deleteImage(int index) {
        if (index < 0 || index >= textures.size()) {
            return;
        }
        String fileName = fileNames.get(index);
        FileHandle file = getDirectory().child(fileName);
        if (file.exists()) {
            file.delete();
        }
        TextureRegion region = textures.get(index);
        if (region.getTexture() != null) {
            region.getTexture().dispose();
        }
        textures.remove(index);
        fileNames.remove(index);
    }

    public int getCount() {
        return textures.size();
    }

    public TextureRegion getTexture(int index) {
        if (index < 0 || index >= textures.size()) {
            return null;
        }
        return textures.get(index);
    }

    public TextureRegion[] toArray() {
        return textures.toArray(new TextureRegion[0]);
    }

    public void dispose() {
        for (TextureRegion region : textures) {
            if (region.getTexture() != null) {
                region.getTexture().dispose();
            }
        }
        textures.clear();
        fileNames.clear();
    }
}
