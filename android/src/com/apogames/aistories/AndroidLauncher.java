package com.apogames.aistories;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import com.apogames.aistories.game.MainPanel;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

public class AndroidLauncher extends AndroidApplication {

	private static final int MIN_KEYBOARD_HEIGHT_DP = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Constants.IS_ANDROID = true;
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useWakelock = true;
		initialize(new AIStories(), config);
		setupKeyboardHeightDetection();
	}

	private void setupKeyboardHeightDetection() {
		final View rootView = getWindow().getDecorView().getRootView();
		final int minKeyboardPx = (int) (MIN_KEYBOARD_HEIGHT_DP * getResources().getDisplayMetrics().density);

		rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				Rect visibleFrame = new Rect();
				rootView.getWindowVisibleDisplayFrame(visibleFrame);
				int screenHeight = rootView.getHeight();
				int heightDiff = screenHeight - visibleFrame.height();

				if (heightDiff > minKeyboardPx) {
					MainPanel.setKeyboardPixelHeight(heightDiff);
				} else {
					MainPanel.setKeyboardPixelHeight(0);
				}
			}
		});
	}
}
