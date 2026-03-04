/*
 * Copyright (c) 2005-2020 Dirk Aporius <dirk.aporius@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.apogames.asset;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/**
 * The type Asset loader.
 */
public class AssetLoader {

    public static final String FONT_CHARACTERS = FreeTypeFontGenerator.DEFAULT_CHARS
            + "\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF"
            + "\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF"
            + "\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB\u00CC\u00CD\u00CE\u00CF"
            + "\u00D0\u00D1\u00D2\u00D3\u00D4\u00D5\u00D6\u00D7\u00D8\u00D9\u00DA\u00DB\u00DC\u00DD\u00DE\u00DF"
            + "\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00E8\u00E9\u00EA\u00EB\u00EC\u00ED\u00EE\u00EF"
            + "\u00F0\u00F1\u00F2\u00F3\u00F4\u00F5\u00F6\u00F7\u00F8\u00F9\u00FA\u00FB\u00FC\u00FD\u00FE\u00FF"
            + "\u011E\u011F\u0130\u0131\u015E\u015F"
            + "\u2013\u2014\u2018\u2019\u201A\u201C\u201D\u201E\u2026\u2022\u20AC";
    public static TextureRegion backgroundTextureRegion;
    public static TextureRegion backgroundMenuTextureRegion;
    public static TextureRegion[] buttonBlancoTextureRegion;
    public static TextureRegion[] buttonXTextureRegion;
    public static TextureRegion[] maincharacterTextureRegion;
    public static TextureRegion[] supportCharacterTextureRegion;
    public static TextureRegion[] universeTextureRegion;
    public static TextureRegion[] placesTextureRegion;
    public static TextureRegion[] objectivesTextureRegion;
    public static TextureRegion xTextureRegion;
    public static TextureRegion tonieTextureRegion;
    public static BitmapFont font40;
    public static BitmapFont font20;
    public static BitmapFont font15;
    public static BitmapFont font10;
    public static BitmapFont font25;
    public static BitmapFont font30;
    public static BitmapFont fontTitle;
    private static Texture backgroundTexture;
    private static Texture backgroundMenuTexture;
    private static Texture buttonBlancoTexture;
    private static Texture buttonXTexture;
    private static Texture xTexture;
    private static Texture maincharactertileTexture;
    private static Texture tonieTexture;
    private static Texture supportCharacterTexture;
    private static Texture universeTexture;
    private static Texture placesTexture;
    private static Texture objectivesTexture;

    public static void load() {
        backgroundTexture = new Texture(Gdx.files.internal("images/background.png"));
        backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        backgroundTextureRegion = new TextureRegion(backgroundTexture, 0, 0, 1400, 800);
        backgroundTextureRegion.flip(false, true);

        backgroundMenuTexture = new Texture(Gdx.files.internal("images/background_menu.png"));
        backgroundMenuTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        backgroundMenuTextureRegion = new TextureRegion(backgroundMenuTexture, 0, 0, 1400, 800);
        backgroundMenuTextureRegion.flip(false, true);

        buttonBlancoTexture = new Texture(Gdx.files.internal("images/buttons_blanco.png"));
        buttonBlancoTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        buttonBlancoTextureRegion = new TextureRegion[3];
        for (int i = 0; i < buttonBlancoTextureRegion.length; i++) {
            buttonBlancoTextureRegion[i] = new TextureRegion(buttonBlancoTexture, 314 * i, 0, 314, 149);
            buttonBlancoTextureRegion[i].flip(false, true);
        }

        buttonXTexture = new Texture(Gdx.files.internal("images/buttons_x.png"));
        buttonXTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        buttonXTextureRegion = new TextureRegion[3];
        for (int i = 0; i < buttonXTextureRegion.length; i++) {
            buttonXTextureRegion[i] = new TextureRegion(buttonXTexture, 70 * i, 0, 70, 70);
            buttonXTextureRegion[i].flip(false, true);
        }

        xTexture = new Texture(Gdx.files.internal("images/x.png"));
        xTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        xTextureRegion = new TextureRegion(xTexture, 0, 0, 90, 90);
        xTextureRegion.flip(false, true);

        maincharactertileTexture = new Texture(Gdx.files.internal("images/tiles_maincharacter.png"));
        maincharactertileTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        maincharacterTextureRegion = new TextureRegion[30];
        int x = 0;
        int y = 0;
        for (int i = 0; i < maincharacterTextureRegion.length; i++) {
            maincharacterTextureRegion[i] = new TextureRegion(maincharactertileTexture, 200 * x, 200 * y, 200, 200);
            maincharacterTextureRegion[i].flip(false, true);

            x += 1;
            if (x%10 == 0) {
                x = 0;
                y += 1;
            }
        }

        tonieTexture = new Texture(Gdx.files.internal("images/creative_tonie.png"));
        tonieTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        tonieTextureRegion = new TextureRegion(tonieTexture, 0, 0, 203, 248);
        tonieTextureRegion.flip(false, true);

        supportCharacterTexture = new Texture(Gdx.files.internal("images/tiles_supportcharacter.png"));
        supportCharacterTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        supportCharacterTextureRegion = new TextureRegion[30];
        x = 0;
        y = 0;
        for (int i = 0; i < supportCharacterTextureRegion.length; i++) {
            supportCharacterTextureRegion[i] = new TextureRegion(supportCharacterTexture, 200 * x, 200 * y, 200, 200);
            supportCharacterTextureRegion[i].flip(false, true);

            x += 1;
            if (x%10 == 0) {
                x = 0;
                y += 1;
            }
        }

        universeTexture = new Texture(Gdx.files.internal("images/tiles_universe.png"));
        universeTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        universeTextureRegion = new TextureRegion[30];
        x = 0;
        y = 0;
        for (int i = 0; i < universeTextureRegion.length; i++) {
            universeTextureRegion[i] = new TextureRegion(universeTexture, 200 * x, 200 * y, 200, 200);
            universeTextureRegion[i].flip(false, true);

            x += 1;
            if (x%10 == 0) {
                x = 0;
                y += 1;
            }
        }

        placesTexture = new Texture(Gdx.files.internal("images/tiles_places.png"));
        placesTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        placesTextureRegion = new TextureRegion[30];
        x = 0;
        y = 0;
        for (int i = 0; i < placesTextureRegion.length; i++) {
            placesTextureRegion[i] = new TextureRegion(placesTexture, 200 * x, 200 * y, 200, 200);
            placesTextureRegion[i].flip(false, true);

            x += 1;
            if (x%10 == 0) {
                x = 0;
                y += 1;
            }
        }

        objectivesTexture = new Texture(Gdx.files.internal("images/tiles_objectives.png"));
        objectivesTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        objectivesTextureRegion = new TextureRegion[30];
        x = 0;
        y = 0;
        for (int i = 0; i < objectivesTextureRegion.length; i++) {
            objectivesTextureRegion[i] = new TextureRegion(objectivesTexture, 200 * x, 200 * y, 200, 200);
            objectivesTextureRegion[i].flip(false, true);

            x += 1;
            if (x%10 == 0) {
                x = 0;
                y += 1;
            }
        }

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/OpenSans.ttf"));
        font10 = generateFont(generator, 10);
        font15 = generateFont(generator, 15);
        font20 = generateFont(generator, 20);
        font25 = generateFont(generator, 25);
        font30 = generateFont(generator, 30);
        font40 = generateFont(generator, 40);
        fontTitle = generateFont(generator, 48);
        generator.dispose();
    }

    private static BitmapFont generateFont(FreeTypeFontGenerator generator, int size) {
        FreeTypeFontParameter param = new FreeTypeFontParameter();
        param.size = size;
        param.flip = true;
        param.characters = FONT_CHARACTERS;
        param.minFilter = Texture.TextureFilter.Linear;
        param.magFilter = Texture.TextureFilter.Linear;
        param.genMipMaps = true;
        return generator.generateFont(param);
    }

    public static void dispose() {
        backgroundTexture.dispose();
        backgroundMenuTexture.dispose();
        buttonBlancoTexture.dispose();
        buttonXTexture.dispose();
        xTexture.dispose();
        maincharactertileTexture.dispose();
        tonieTexture.dispose();
        supportCharacterTexture.dispose();
        universeTexture.dispose();
        placesTexture.dispose();
        objectivesTexture.dispose();

        font40.dispose();
        font30.dispose();
        font25.dispose();
        font20.dispose();
        font15.dispose();
        font10.dispose();
        fontTitle.dispose();
    }

}

