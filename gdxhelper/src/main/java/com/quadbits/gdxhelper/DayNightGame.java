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

import com.badlogic.gdx.Screen;
import com.quadbits.gdxhelper.screens.DayNightScreen;
import com.quadbits.gdxhelper.utils.ParabolaEquation;
import com.quadbits.gdxhelper.utils.TimeManager;

import javax.inject.Inject;

/**
 *
 */
public abstract class DayNightGame extends LWPGame {
    ParabolaEquation.TraverseDirection sunMoonTraverseDirection;
    float cloudsPerc;

    @Inject
    TimeManager timeManager;

    public DayNightGame() {
        super();

        sunMoonTraverseDirection = ParabolaEquation.TraverseDirection.LEFT_TO_RIGHT;
        cloudsPerc = 0;
    }

    public ParabolaEquation.TraverseDirection getSunMoonTraverseDirection() {
        return sunMoonTraverseDirection;
    }

    public void setSunMoonTraverseDirection(ParabolaEquation.TraverseDirection traverseDirection) {
        this.sunMoonTraverseDirection = traverseDirection;

        Screen screen = getScreen();
        if (screen == null || !(screen instanceof DayNightScreen)) {
            return;
        }
        DayNightScreen dayNightScreen = (DayNightScreen) screen;
        dayNightScreen.setSunMoonTraverseDirection(sunMoonTraverseDirection);
    }

    public float getCloudsPerc() {
        return cloudsPerc;
    }

    public void setCloudsPerc(float cloudsPerc) {
        this.cloudsPerc = cloudsPerc;

        Screen screen = getScreen();
        if (screen == null || !(screen instanceof DayNightScreen)) {
            return;
        }
        DayNightScreen dayNightScreen = (DayNightScreen) screen;
        dayNightScreen.setCloudsPerc(cloudsPerc);
    }
}
