package io.github.rogerallen.sunangle;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import io.github.rogerallen.sunangle.Sunangle;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.depth = 24;
		config.stencil = 8;
		config.a = 8;
		config.r = 8;
		config.g = 8;
		config.b = 8;
		config.numSamples = 4;
		initialize(new Sunangle(), config);
	}
}
