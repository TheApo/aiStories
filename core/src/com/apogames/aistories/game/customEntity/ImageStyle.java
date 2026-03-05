package com.apogames.aistories.game.customEntity;

public enum ImageStyle {
    CLAY("style_clay", "in cute clay/plasticine stop-motion style, soft rounded shapes, handmade look",
            0.76f, 0.60f, 0.42f),
    WATERCOLOR("style_watercolor", "in soft watercolor painting style, gentle colors, dreamy atmosphere",
            0.53f, 0.75f, 0.93f),
    CRAYON("style_crayon", "in colorful crayon drawing style, childlike and cheerful",
            0.95f, 0.85f, 0.20f),
    DISNEY("style_disney", "in Disney animation style, expressive eyes, warm and magical",
            0.30f, 0.30f, 0.80f),
    ANIME("style_anime", "in vibrant anime style, big expressive eyes, colorful and lively",
            0.90f, 0.35f, 0.55f),
    COMIC("style_comic", "in cartoon comic style, bold outlines, bright colors, funny expressions",
            0.95f, 0.55f, 0.10f),
    PIXEL("style_pixel", "in cute pixel art style, retro game aesthetic, colorful pixels",
            0.30f, 0.80f, 0.30f),
    REALISTIC("style_realistic", "in realistic illustration style, detailed and lifelike",
            0.45f, 0.45f, 0.45f),
    PAPERCUT("style_papercut", "in paper cut-out collage style, layered paper textures, colorful",
            0.85f, 0.40f, 0.70f),
    PIXAR("style_pixar", "in Pixar 3D animation style, smooth rounded characters, cinematic lighting, vibrant colors",
            0.20f, 0.60f, 0.90f),
    MINECRAFT("style_minecraft", "in Minecraft voxel style, blocky cubic shapes, pixelated textures, square world",
            0.40f, 0.70f, 0.20f),
    LEGO("style_lego", "in LEGO brick style, characters and objects built from LEGO bricks, plastic look, bright primary colors",
            0.90f, 0.20f, 0.20f);

    private final String localizationKey;
    private final String promptFragment;
    private final float colorR;
    private final float colorG;
    private final float colorB;

    ImageStyle(String localizationKey, String promptFragment, float r, float g, float b) {
        this.localizationKey = localizationKey;
        this.promptFragment = promptFragment;
        this.colorR = r;
        this.colorG = g;
        this.colorB = b;
    }

    public String getLocalizationKey() {
        return localizationKey;
    }

    public String getPromptFragment() {
        return promptFragment;
    }

    public float getColorR() {
        return colorR;
    }

    public float getColorG() {
        return colorG;
    }

    public float getColorB() {
        return colorB;
    }
}
