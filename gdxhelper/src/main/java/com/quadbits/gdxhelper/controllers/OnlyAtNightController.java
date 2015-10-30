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
public class OnlyAtNightController extends BaseController
        implements Recyclable<OnlyAtNightController> {
    protected Pool<OnlyAtNightController> onlyAtNightControllerPool;

    @Inject
    protected TimeManager timeManager;

    @Inject
    public OnlyAtNightController() {
        super();
    }

    @Override
    public void reset() {
    }

    @Override
    public void free() {
        onlyAtNightControllerPool.free(this);
    }

    @Override
    public void setPool(Pool<OnlyAtNightController> onlyAtNightControllerPool) {
        this.onlyAtNightControllerPool = onlyAtNightControllerPool;
    }

    @Override
    public void control(Actor actor, float deltaSeconds) {
        float alpha;
        switch (timeManager.getPeriod()) {
            case PRE_MIDNIGHT:
            case POST_MIDNIGHT:
                alpha = 1;
                break;

            case TWILIGHT_POST_SUNSET:
                alpha = timeManager.getTPeriod();
                break;

            case TWILIGHT_PRE_SUNRISE:
                alpha = 1 - timeManager.getTPeriod();
                break;

            default:
                alpha = 0;
        }

        actor.getColor().a = alpha;

        if (alpha <= 0) {
            actor.setVisible(false);
        } else {
            actor.setVisible(true);
        }
    }
}
