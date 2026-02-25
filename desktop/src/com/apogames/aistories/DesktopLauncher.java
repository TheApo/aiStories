package com.apogames.aistories;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.apogames.aistories.AIStories;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

		config.setTitle(Constants.PROGRAM_NAME);
		config.useVsync(true);
		config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 2);
		config.setWindowedMode(Constants.GAME_WIDTH, Constants.GAME_HEIGHT);

		new Lwjgl3Application(new AIStories(), config);
	}
}
