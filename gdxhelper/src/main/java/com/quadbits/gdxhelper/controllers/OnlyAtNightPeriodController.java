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
import com.quadbits.gdxhelper.utils.Recyclable;
import com.quadbits.gdxhelper.utils.TimeManager;

import javax.inject.Inject;

/**
 *
 */
public class OnlyAtNightPeriodController extends BaseController
        implements Recyclable<OnlyAtNightPeriodController> {
    protected Pool<OnlyAtNightPeriodController> onlyAtNightPeriodControllerPool;
    protected float tBegin;
    protected float tEnd;
    protected float tTransition;

    @Inject
    protected TimeManager timeManager;

    @Inject
    public OnlyAtNightPeriodController() {
        super();
    }

    @Override
    public void reset() {
    }

    @Override
    public void free() {
        onlyAtNightPeriodControllerPool.free(this);
    }

    @Override
    public void setPool(Pool<OnlyAtNightPeriodController> onlyAtNightPeriodControllerPool) {
        this.onlyAtNightPeriodControllerPool = onlyAtNightPeriodControllerPool;
    }

    @Override
    public void control(Actor actor, float deltaSeconds) {
        float alpha;
        float tNight = timeManager.getTNight();

        if (tNight < tBegin) {
            alpha = 0;
        } else if (tNight > tBegin && tNight < tBegin + tTransition) {
            alpha = (tNight - tBegin) / tTransition;
        } else if (tNight < tEnd - tTransition) {
            alpha = 1;
        } else if (tNight < tEnd) {
            alpha = (tEnd - tNight) / tTransition;
        } else {
            alpha = 0;
        }

        actor.getColor().a = alpha;

        if (alpha <= 0) {
            actor.setVisible(false);
        } else {
            actor.setVisible(true);
        }
    }

    public float getTBegin() {
        return tBegin;
    }

    public float getTEnd() {
        return tEnd;
    }

    public float getTTransition() {
        return tTransition;
    }

    public void setTPeriod(float tBegin, float tEnd, float tTransition) {
        if (tBegin > tEnd) {
            throw new IllegalArgumentException(
                    String.format("tBegin (%f) must be smaller than tEnd (%f)", tBegin, tEnd));
        }
        if (tTransition > (tEnd - tBegin) / 2) {
            throw new IllegalArgumentException(String.format(
                    "tTransition (%f) must be smaller than half the diff between tBegin and tEnd " +
                            "(%f)", tTransition, (tEnd - tBegin) / 2));
        }
        this.tBegin = tBegin;
        this.tEnd = tEnd;
        this.tTransition = tTransition;
    }
}
