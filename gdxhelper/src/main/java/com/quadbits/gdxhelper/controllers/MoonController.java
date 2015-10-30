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
package com.quadbits.gdxhelper.controllers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.ParabolaEquation;
import com.quadbits.gdxhelper.utils.Recyclable;
import com.quadbits.gdxhelper.utils.TimeManager;

import javax.inject.Inject;

/**
 *
 */
public class MoonController extends ParabolaController
        implements NonContinuousRenderingController, Recyclable<MoonController> {
    protected Pool<MoonController> moonControllerPool;

    @Inject
    protected TimeManager timeManager;

    @Inject
    public MoonController(ParabolaEquation parabolaEq) {
        super(parabolaEq);
    }

    @Override
    public void free() {
        moonControllerPool.free(this);
    }

    @Override
    public void setPool(Pool<MoonController> moonControllerPool) {
        this.moonControllerPool = moonControllerPool;
    }

    @Override
    public float getT() {
        return timeManager.getTMoon();
    }

    @Override
    public long getMaxSleepTime(Actor actor) {
        long maxSleepTimeX;
        long maxSleepTimeY;

        float tMoon = timeManager.getTMoon();
        if (tMoon < 0) {
            return Long.MAX_VALUE;
        }

        maxSleepTimeX = (long) Math
                .floor(parabolaEq.getTDiffGivenXDiff(1) * timeManager.getMoonTravelWidth() *
                        timeManager.getPeriodMillis());
        maxSleepTimeY = (long) Math
                .floor(parabolaEq.getTDiffGivenYDiff(1, tMoon) * timeManager.getMoonTravelWidth() *
                        timeManager.getPeriodMillis());
        return Math.min(maxSleepTimeX, maxSleepTimeY);
    }
}
