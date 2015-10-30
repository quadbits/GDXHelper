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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.Recyclable;
import com.quadbits.gdxhelper.utils.TimeManager;

import javax.inject.Inject;

/**
 *
 */
public class TintAtNightController extends BaseController
        implements Recyclable<TintAtNightController> {
    protected Pool<TintAtNightController> tintAtNightControllerPool;
    protected Color nightColor;

    @Inject
    protected TimeManager timeManager;

    @Inject
    public TintAtNightController() {
        super();
        reset();
    }

    @Override
    public void reset() {
        nightColor = new Color(Color.WHITE);
    }

    @Override
    public void free() {
        tintAtNightControllerPool.free(this);
    }

    @Override
    public void setPool(Pool<TintAtNightController> tintAtNightControllerPool) {
        this.tintAtNightControllerPool = tintAtNightControllerPool;
    }

    @Override
    public void control(Actor actor, float deltaSeconds) {
        float blend;
        switch (timeManager.getPeriod()) {
            case PRE_MIDNIGHT:
            case POST_MIDNIGHT:
                blend = 1;
                break;

            case TWILIGHT_POST_SUNSET:
                blend = timeManager.getTPeriod();
                break;

            case TWILIGHT_PRE_SUNRISE:
                blend = 1 - timeManager.getTPeriod();
                break;

            default:
                blend = 0;
        }

        Color actorColor = actor.getColor();
        actorColor.set(1, 1, 1, actorColor.a);
        actorColor.lerp(nightColor, blend);
        actor.setColor(actorColor);
    }

    public Color getNightColor() {
        return nightColor;
    }

    public void setNightColor(Color nightColor) {
        this.nightColor = nightColor;
    }
}
