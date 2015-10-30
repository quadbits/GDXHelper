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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.Recyclable;

import javax.inject.Inject;

/**
 *
 */
public class PanYOnScrollController extends PanOnScrollController
        implements Recyclable<PanYOnScrollController> {
    protected Pool<PanYOnScrollController> panYOnScrollControllerPool;
    protected float minY;
    protected float maxY;

    @Inject
    public PanYOnScrollController() {
        super();
    }

    @Override
    public void free() {
        panYOnScrollControllerPool.free(this);
    }

    @Override
    public void setPool(Pool<PanYOnScrollController> panYOnScrollControllerPool) {
        this.panYOnScrollControllerPool = panYOnScrollControllerPool;
    }

    @Override
    public void control(Actor actor, float deltaSeconds) {
        float minY, maxY;

        if (autopan) {
            // scroll is in the [0, 1] range; actor's y should be set in the range
            // [0, Gdx.graphics.getHeight() - actor.getHeight()]
            minY = 0;
            maxY = Gdx.graphics.getHeight() - actor.getHeight();
            if (inverted) {
                minY = maxY;
                maxY = 0;
            }
        } else {
            minY = this.minY;
            maxY = this.maxY;
        }

        actor.setY(minY + scroll * (maxY - minY));
    }

    public float getMinY() {
        return minY;
    }

    public void setMinY(float minY) {
        this.minY = minY;
    }

    public float getMaxY() {
        return maxY;
    }

    public void setMaxY(float maxY) {
        this.maxY = maxY;
    }
}
