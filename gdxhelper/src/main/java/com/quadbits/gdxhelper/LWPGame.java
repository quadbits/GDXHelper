/*
 * Copyright (c) 2015 Quadbits SLU
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quadbits.gdxhelper;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.quadbits.gdxhelper.screens.LWPScreen;

import javax.inject.Inject;

import dagger.Lazy;

/**
 *
 */
public abstract class LWPGame extends Game {
    float dim;
    boolean panningEnabled;
    boolean flingEnabled;
    int currentScreenId;

    @Inject
    Lazy<AssetManager> lazyAssetManager;

    public static final float MDPI_SCALE = 0.25f;
    public static final float HDPI_SCALE = 0.375f;
    public static final float XHDPI_SCALE = 0.5f;
    public static final float XXHDPI_SCALE = 0.75f;
    public static final float XXXHDPI_SCALE = 1;

    public static final String MDPI_SCALE_STR = "MDPI_SCALE_STR";
    public static final String HDPI_SCALE_STR = "HDPI_SCALE_STR";
    public static final String XHDPI_SCALE_STR = "XHDPI_SCALE_STR";
    public static final String XXHDPI_SCALE_STR = "XXHDPI_SCALE_STR";
    public static final String XXXHDPI_SCALE_STR = "XXXHDPI_SCALE_STR";

    public static final int SCREEN_NO_SCREEN = -1;

    public LWPGame() {
        panningEnabled = false;
        flingEnabled = false;
        dim = 0f;
        currentScreenId = SCREEN_NO_SCREEN;
    }

    @Override
    public void dispose() {
        // dispose current screen
        Screen screen = getScreen();
        if (screen != null) {
            screen.dispose();
        }

        // dispose assets
        lazyAssetManager.get().dispose();

        super.dispose();
    }

    public float getDim() {
        return dim;
    }

    public void setDim(float dim) {
        this.dim = dim;

        Screen screen = getScreen();
        if (screen == null || !(screen instanceof LWPScreen)) {
            return;
        }
        LWPScreen lwpScreen = (LWPScreen) screen;
        lwpScreen.setDim(dim);
    }

    public boolean isPanningEnabled() {
        return panningEnabled;
    }

    public void setPanningEnabled(boolean panningEnabled) {
        this.panningEnabled = panningEnabled;

        Screen screen = getScreen();
        if (screen == null || !(screen instanceof LWPScreen)) {
            return;
        }
        LWPScreen lwpScreen = (LWPScreen) screen;
        lwpScreen.setPanningEnabled(panningEnabled);
    }

    public boolean isFlingEnabled() {
        return flingEnabled;
    }

    public void setFlingEnabled(boolean flingEnabled) {
        this.flingEnabled = flingEnabled;

        Screen screen = getScreen();
        if (screen == null || !(screen instanceof LWPScreen)) {
            return;
        }
        LWPScreen lwpScreen = (LWPScreen) screen;
        lwpScreen.setFlingEnabled(flingEnabled);
    }

    public int getCurrentScreenId() {
        return currentScreenId;
    }

    public void setCurrentScreenId(int screenId) {
        this.currentScreenId = screenId;
    }

    public void setParallaxScroll(float scrollX, float scrollY) {
        Screen screen = getScreen();
        if (screen == null || !(screen instanceof LWPScreen)) {
            return;
        }
        LWPScreen lwpScreen = (LWPScreen) screen;
        lwpScreen.setScrollX(scrollX);
        lwpScreen.setScrollY(scrollY);
    }

    public static String scaleToString(float scale) {
        if (scale <= MDPI_SCALE) {
            return MDPI_SCALE_STR;
        } else if (scale <= HDPI_SCALE) {
            return HDPI_SCALE_STR;
        } else if (scale <= XHDPI_SCALE) {
            return XHDPI_SCALE_STR;
        } else if (scale <= XXHDPI_SCALE) {
            return XXHDPI_SCALE_STR;
        } else {
            return XXXHDPI_SCALE_STR;
        }
    }
}
