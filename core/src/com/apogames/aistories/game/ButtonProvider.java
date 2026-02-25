/*
 * Copyright (c) 2005-2025 Dirk Aporius <dirk.aporius@gmail.com>
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

package com.apogames.aistories.game;

import com.apogames.aistories.Constants;
import com.apogames.aistories.game.createStory.CreateStory;
import com.apogames.aistories.game.creativeTonie.CreativeTonie;
import com.apogames.aistories.game.customEntity.CustomEntityEditor;
import com.apogames.aistories.game.listenStories.ListenStories;
import com.apogames.aistories.game.menu.Menu;
import com.apogames.asset.AssetLoader;
import com.apogames.entity.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class ButtonProvider {

    private final MainPanel game;

    public ButtonProvider(MainPanel game) {
        this.game = game;
    }

    public void init() {
        if ((this.game.getButtons() == null) || (this.game.getButtons().size() <= 0)) {
            this.game.getButtons().clear();

            BitmapFont font = AssetLoader.font25;
            String text = "";
            String function = CreateStory.FUNCTION_BACK;
            int width = 64;
            int height = 64;
            int x = Constants.GAME_WIDTH - width - 15;
            int y = Constants.GAME_HEIGHT - height - 5;
            ApoButton button = new ApoButtonImageWithThree(x, y, width, height, function, text, AssetLoader.buttonXTextureRegion);
            //ApoButtonColor(x, y, width, height, function, text, Constants.COLOR_BACKGROUND, Constants.COLOR_WHITE);
            button.setStroke(1);
            button.setFont(AssetLoader.font40);
            this.game.getButtons().add(button);

            function = Menu.FUNCTION_BACK;
            width = 64;
            height = 64;
            x = Constants.GAME_WIDTH - width - 15;
            y = Constants.GAME_HEIGHT - height - 5;
            button = new ApoButtonImageWithThree(x, y, width, height, function, text, AssetLoader.buttonXTextureRegion);
            //ApoButtonColor(x, y, width, height, function, text, Constants.COLOR_BACKGROUND, Constants.COLOR_WHITE);
            button.setStroke(1);
            button.setFont(AssetLoader.font40);
            this.game.getButtons().add(button);

            text = "de";
            function = Menu.FUNCTION_LANGUAGE;
            width = 64;
            height = 64;
            x = 15;
            y = Constants.GAME_HEIGHT - height - 5;
            button = new ApoButtonImageThree(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "button_language_de");
            button.setFont(AssetLoader.font30);
            this.game.getButtons().add(button);


            text = "Start";
            function = CreateStory.FUNCTION_GENERATE_TEXT;
            width = 450;
            height = 64;
            x = Constants.GAME_WIDTH / 2 - width / 2;
            y = Constants.GAME_HEIGHT - 100;
            button = new ApoButtonImageThree(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "button_start");
            button.setFont(AssetLoader.font25);
            this.game.getButtons().add(button);

            text = "TONIE";
            function = CreateStory.FUNCTION_NEWPROMPT;
            width = 300;
            height = 64;
            x = 5;
            y = 10;
            button = new ApoButtonImageThree(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "button_newprompt");
            button.setFont(AssetLoader.font25);
            this.game.getButtons().add(button);

            text = "LLM";
            function = CreateStory.FUNCTION_LLM;
            width = 60;
            height = 40;
            x = Constants.GAME_WIDTH - width - 250;
            y = 15;
            button = new ApoButtonSwitch(x, y, width, height, function, Constants.COLOR_WHITE, Constants.COLOR_BLACK);
            button.setFont(AssetLoader.font15);
            this.game.getButtons().add(button);

            text = "TONIE";
            function = Menu.FUNCTION_CREATESTORY;
            width = 600;
            height = 150;
            x = Constants.GAME_WIDTH/2 - width/2;
            y = 150;
            button = new ApoButtonImageThree(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "button_createstory");
            button.setFont(AssetLoader.font30);
            this.game.getButtons().add(button);

            text = "TONIE";
            function = Menu.FUNCTION_ALLMP3S;
            width = 600;
            height = 150;
            x = Constants.GAME_WIDTH/2 - width/2;
            y = 450;
            button = new ApoButtonImageThree(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "button_allmp3s");
            button.setFont(AssetLoader.font30);
            this.game.getButtons().add(button);

            text = "";
            function = ListenStories.FUNCTION_BACK;
            width = 64;
            height = 64;
            x = Constants.GAME_WIDTH - width - 15;
            y = Constants.GAME_HEIGHT - height - 5;
            button = new ApoButtonImageWithThree(x, y, width, height, function, text, AssetLoader.buttonXTextureRegion);
            ((ApoButtonImageWithThree) (button)).setMouseOverText(AssetLoader.buttonBlancoTextureRegion[0], "Menu");
            button.setStroke(1);
            button.setFont(AssetLoader.font20);
            this.game.getButtons().add(button);

            text = "";
            function = ListenStories.FUNCTION_PLAY;
            width = 64;
            height = 64;
            x = Constants.GAME_WIDTH/2 - width * 3 / 2 - 15;
            y = Constants.GAME_HEIGHT - height - 50;
            button = new ApoButtonImageThreeExtra(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "");
            ((ApoButtonImageThreeExtra)(button)).setExtra(ApoButtonImageThreeExtra.EXTRA.PLAY);
            this.game.getButtons().add(button);

            text = "";
            function = ListenStories.FUNCTION_STOP;
            width = 64;
            height = 64;
            x = Constants.GAME_WIDTH/2 + width/2 + 15;
            y = Constants.GAME_HEIGHT - height - 50;
            button = new ApoButtonImageThreeExtra(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "");
            ((ApoButtonImageThreeExtra)(button)).setExtra(ApoButtonImageThreeExtra.EXTRA.STOP);
            this.game.getButtons().add(button);

            text = "";
            function = ListenStories.FUNCTION_PAUSE;
            width = 64;
            height = 64;
            x = Constants.GAME_WIDTH/2 - width/2;
            y = Constants.GAME_HEIGHT - height - 50;
            button = new ApoButtonImageThreeExtra(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "");
            ((ApoButtonImageThreeExtra)(button)).setExtra(ApoButtonImageThreeExtra.EXTRA.PAUSE);
            this.game.getButtons().add(button);

            text = "Start";
            function = ListenStories.FUNCTION_CREATEMP3;
            width = 450;
            height = 64;
            x = Constants.GAME_WIDTH / 2 - width / 2;
            y = Constants.GAME_HEIGHT - 100;
            button = new ApoButtonImageThree(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "button_read");
            button.setFont(AssetLoader.font25);
            this.game.getButtons().add(button);

            text = "";
            function = ListenStories.FUNCTION_NEXT_PAGE;
            width = 64;
            height = 64;
            x = Constants.GAME_WIDTH/2 + 200;
            y = Constants.GAME_HEIGHT - height - 220;
            button = new ApoButtonImageThreeExtra(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "");
            ((ApoButtonImageThreeExtra)(button)).setExtra(ApoButtonImageThreeExtra.EXTRA.NEXT);
            this.game.getButtons().add(button);

            text = "";
            function = ListenStories.FUNCTION_PREVIOUS_PAGE;
            width = 64;
            height = 64;
            x = Constants.GAME_WIDTH/2 - 200 - width;
            y = Constants.GAME_HEIGHT - height - 220;
            button = new ApoButtonImageThreeExtra(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "");
            ((ApoButtonImageThreeExtra)(button)).setExtra(ApoButtonImageThreeExtra.EXTRA.PREV);
            this.game.getButtons().add(button);

            text = "";
            function = ListenStories.FUNCTION_FONT_SMALLER;
            width = 64;
            height = 64;
            x = Constants.GAME_WIDTH/2 - width - 10;
            y = Constants.GAME_HEIGHT - height - 220;
            button = new ApoButtonImageThreeExtra(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "");
            ((ApoButtonImageThreeExtra)(button)).setExtra(ApoButtonImageThreeExtra.EXTRA.MINUS);
            this.game.getButtons().add(button);

            text = "";
            function = ListenStories.FUNCTION_FONT_BIGGER;
            width = 64;
            height = 64;
            x = Constants.GAME_WIDTH/2 + 10;
            y = Constants.GAME_HEIGHT - height - 220;
            button = new ApoButtonImageThreeExtra(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "");
            ((ApoButtonImageThreeExtra)(button)).setExtra(ApoButtonImageThreeExtra.EXTRA.PLUS);
            this.game.getButtons().add(button);

            text = "";
            function = ListenStories.FUNCTION_NEXT_STORY;
            width = 64;
            height = 64;
            x = Constants.GAME_WIDTH/2 + 400;
            y = 20;
            button = new ApoButtonImageThreeExtra(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "");
            ((ApoButtonImageThreeExtra)(button)).setExtra(ApoButtonImageThreeExtra.EXTRA.NEXT);
            this.game.getButtons().add(button);

            text = "";
            function = ListenStories.FUNCTION_PREVIOUS_STORY;
            width = 64;
            height = 64;
            x = Constants.GAME_WIDTH/2 - 400 - width;
            y = 20;
            button = new ApoButtonImageThreeExtra(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "");
            ((ApoButtonImageThreeExtra)(button)).setExtra(ApoButtonImageThreeExtra.EXTRA.PREV);
            this.game.getButtons().add(button);


            text = "READ";
            function = ListenStories.FUNCTION_UPLOAD_TONIE;
            width = 203;
            height = 248;
            x = 70;
            y = Constants.GAME_HEIGHT - height - 10;
            //button = new ApoButtonImageThree(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "button_upload");
            button = new ApoButtonImage(x, y, width, height, function, "", AssetLoader.tonieTextureRegion);
            button.setFont(AssetLoader.font20);
            this.game.getButtons().add(button);

            text = "";
            function = CreativeTonie.FUNCTION_BACK;
            width = 64;
            height = 64;
            x = Constants.GAME_WIDTH - width - 15;
            y = Constants.GAME_HEIGHT - height - 5;
            button = new ApoButtonImageWithThree(x, y, width, height, function, text, AssetLoader.buttonXTextureRegion);
            //ApoButtonColor(x, y, width, height, function, text, Constants.COLOR_BACKGROUND, Constants.COLOR_WHITE);
            button.setStroke(1);
            button.setFont(AssetLoader.font40);
            this.game.getButtons().add(button);

            text = "Upload";
            function = CreativeTonie.FUNCTION_UPLOAD;
            width = 350;
            height = 80;
            x = Constants.GAME_WIDTH/2 - width/2;
            y = Constants.GAME_HEIGHT - height - 10;
            button = new ApoButtonImageThree(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "button_upload");
            button.setFont(AssetLoader.font20);
            this.game.getButtons().add(button);

            text = "Delete";
            function = ListenStories.FUNCTION_DELETE;
            width = 128;
            height = 64;
            x = Constants.GAME_WIDTH - width - 100;
            y = Constants.GAME_HEIGHT - 220 - height;
            button = new ApoButtonImageThree(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "button_delete");
            button.setFont(AssetLoader.font20);
            this.game.getButtons().add(button);

            // CustomEntityEditor buttons
            text = "";
            function = CustomEntityEditor.FUNCTION_BACK;
            width = 64;
            height = 64;
            x = Constants.GAME_WIDTH - width - 15;
            y = Constants.GAME_HEIGHT - height - 5;
            button = new ApoButtonImageWithThree(x, y, width, height, function, text, AssetLoader.buttonXTextureRegion);
            button.setStroke(1);
            button.setFont(AssetLoader.font40);
            this.game.getButtons().add(button);

            text = "Confirm";
            function = CustomEntityEditor.FUNCTION_CONFIRM;
            width = 350;
            height = 64;
            x = 30 + 210 - width / 2;
            y = Constants.GAME_HEIGHT - height - 30;
            button = new ApoButtonImageThree(x, y, width, height, function, text, 0, 0, width, height, Constants.COLOR_BLACK, "custom_editor_confirm");
            button.setFont(AssetLoader.font25);
            this.game.getButtons().add(button);

            for (int i = 0; i < this.game.getButtons().size(); i++) {
                this.game.getButtons().get(i).setBOpaque(false);
            }
        }
    }

}
