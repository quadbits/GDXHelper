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

import javax.inject.Inject;

/**
 *
 */
public class RotateOnScrollController extends OnScrollBaseController
        implements Recyclable<RotateOnScrollController> {
    protected Pool<RotateOnScrollController> rotateOnScrollControllerPool;
    protected float minAngle;
    protected float maxAngle;

    @Inject
    public RotateOnScrollController() {
        super();
    }

    @Override
    public void reset() {
        super.reset();
        minAngle = 0;
        maxAngle = 0;
    }

    @Override
    public void free() {
        rotateOnScrollControllerPool.free(this);
    }

    @Override
    public void setPool(Pool<RotateOnScrollController> rotateOnScrollControllerPool) {
        this.rotateOnScrollControllerPool = rotateOnScrollControllerPool;
    }

    @Override
    public void control(Actor actor, float deltaSeconds) {
        // scroll is in the [0, 1] range; actor's rotation angle should be set in the range
        // [minAngle, maxAngle]
        actor.setRotation(minAngle + scroll * (maxAngle - minAngle));
    }

    /**
     * Min. rotation angle (in degrees): rotation angle when scroll is 0
     *
     * @return
     */
    public float getMinAngle() {
        return minAngle;
    }

    /**
     * Min. rotation angle (in degrees): rotation angle when scroll is 0
     *
     * @param minAngle
     */
    public void setMinAngle(float minAngle) {
        this.minAngle = minAngle;
    }

    /**
     * Max. rotation angle (in degrees): rotation angle when scroll is 1
     *
     * @return
     */
    public float getMaxAngle() {
        return maxAngle;
    }

    /**
     * Max. rotation angle (in degrees): rotation angle when scroll is 1
     *
     * @param maxAngle
     */
    public void setMaxAngle(float maxAngle) {
        this.maxAngle = maxAngle;
    }
}
