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
package com.quadbits.gdxhelper.screens;

import com.quadbits.gdxhelper.DayNightGame;
import com.quadbits.gdxhelper.controllers.MoonController;
import com.quadbits.gdxhelper.controllers.SunController;
import com.quadbits.gdxhelper.utils.ParabolaEquation;
import com.quadbits.gdxhelper.utils.TimeManager;

import javax.inject.Inject;

/**
 *
 */
public abstract class DayNightScreen extends LWPScreen {
    protected final DayNightGame dayNightGame;
    protected float cloudsPerc;

    @Inject
    protected TimeManager timeManager;

    public DayNightScreen(DayNightGame game) {
        super(game);

        // The game and time manager
        this.dayNightGame = game;
        cloudsPerc = game.getCloudsPerc();

    }

    protected void loadAssets(String textureAtlasResString) {
        super.loadAssets(textureAtlasResString);
    }

    @Override
    public void show() {
        // Sync time manager time
        timeManager.syncTime();

        super.show();
    }

    @Override
    public void render(float deltaTime) {
        // Update time manager
        timeManager.updateTime();

        super.render(deltaTime);

        // Set timemanager's max unnormalized time diff
        if (!isContinuousRendering()) {
            timeManager.setMaxUnnormalizedTimeDiff(maxSleepTimeMillis);
        }
    }

    @Override
    public boolean isContinuousRendering() {
        // Perform continuous rendering only if time manager is in fast-forward mode
        return (timeManager.getTimeMode() == TimeManager.TimeMode.FAST_FORWARD);
    }

    /**
     * Internal method for accessing the sun controller. Should be overriden by subclasses. If no
     * sun controller is present in the screen, return null.
     *
     * @return The sun controller of the screen, null if not present
     */
    protected abstract SunController getSunController();

    /**
     * Internal method for accessing the moon controller. Should be overriden by subclasses. If no
     * moon controller is present in the screen, return null.
     *
     * @return The moon controller of the screen, null if not present
     */
    public abstract MoonController getMoonController();

    public void setSunMoonTraverseDirection(ParabolaEquation.TraverseDirection traverseDirection) {
        SunController sunController = getSunController();
        if (sunController != null) {
            sunController.setTraverseDirection(traverseDirection);
        }

        MoonController moonController = getMoonController();
        if (moonController != null) {
            moonController.setTraverseDirection(traverseDirection);
        }
    }

    public float getCloudsPerc() {
        return cloudsPerc;
    }

    public void setCloudsPerc(float cloudsPerc) {
        this.cloudsPerc = cloudsPerc;
    }
}
