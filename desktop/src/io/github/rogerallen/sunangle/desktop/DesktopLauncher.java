package io.github.rogerallen.sunangle.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import io.github.rogerallen.sunangle.Sunangle;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1440;
		config.height = 720;
		new LwjglApplication(new Sunangle(), config);
	}
}
