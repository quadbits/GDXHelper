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
public class PanXOnScrollController extends PanOnScrollController
        implements Recyclable<PanXOnScrollController> {
    protected Pool<PanXOnScrollController> panXOnScrollControllerPool;
    protected float minX;
    protected float maxX;

    @Inject
    public PanXOnScrollController() {
        super();
    }

    @Override
    public void free() {
        panXOnScrollControllerPool.free(this);
    }

    @Override
    public void setPool(Pool<PanXOnScrollController> panXOnScrollControllerPool) {
        this.panXOnScrollControllerPool = panXOnScrollControllerPool;
    }

    @Override
    public void control(Actor actor, float deltaSeconds) {
        float minX, maxX;

        if (autopan) {
            // scroll is in the [0, 1] range; actor's x should be set in the range
            // [0, Gdx.graphics.getWidth() - actor.getWidth()]
            minX = 0;
            maxX = Gdx.graphics.getWidth() - actor.getWidth();
            if (inverted) {
                minX = maxX;
                maxX = 0;
            }
        } else {
            minX = this.minX;
            maxX = this.maxX;
        }

        actor.setX(minX + scroll * (maxX - minX));
    }

    public float getMinX() {
        return minX;
    }

    public void setMinX(float minX) {
        this.minX = minX;
    }

    public float getMaxX() {
        return maxX;
    }

    public void setMaxX(float maxX) {
        this.maxX = maxX;
    }

}
