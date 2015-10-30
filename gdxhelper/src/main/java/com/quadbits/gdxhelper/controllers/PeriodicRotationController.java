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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.Recyclable;
import com.quadbits.gdxhelper.utils.TimePeriodManager;

import javax.inject.Inject;

/**
 *
 */
public class PeriodicRotationController extends BaseController
        implements NonContinuousRenderingController, Recyclable<PeriodicRotationController> {

    protected Pool<PeriodicRotationController> rotationControllerPool;
    protected float minAngle;
    protected float maxAngle;
    protected Interpolation interpolation;
    protected int rotateUpSubPeriod;
    protected int rotateDownSubPeriod;
    protected int stallUpSubPeriod;
    protected int stallDownSubPeriod;

    @Inject
    TimePeriodManager timePeriodManager;

    @Inject
    public PeriodicRotationController() {
    }

    @Override
    public void reset() {
        timePeriodManager.reset();
        interpolation = null;
    }

    @Override
    public void free() {
        rotationControllerPool.free(this);
    }

    @Override
    public void setPool(Pool<PeriodicRotationController> pool) {
        this.rotationControllerPool = pool;
    }

    public void setDurations(long rotationDurationMillis, long stallDurationMillis) {
        timePeriodManager.reset();
        int subPeriodCount = 0;
        timePeriodManager.addSubPeriod(rotationDurationMillis);
        rotateUpSubPeriod = subPeriodCount;
        subPeriodCount++;
        if (stallDurationMillis > 0) {
            timePeriodManager.addSubPeriod(stallDurationMillis);
            stallUpSubPeriod = subPeriodCount;
            subPeriodCount++;
        }
        timePeriodManager.addSubPeriod(rotationDurationMillis);
        rotateDownSubPeriod = subPeriodCount;
        subPeriodCount++;
        if (stallDurationMillis > 0) {
            timePeriodManager.addSubPeriod(stallDurationMillis);
            stallDownSubPeriod = subPeriodCount;
        }
    }

    @Override
    public void control(Actor actor, float deltaSeconds) {
        long deltaMillis = (long) (deltaSeconds * 1000);
        timePeriodManager.update(deltaMillis);
        float angle;
        float normalizedValue;
        int subPeriod = timePeriodManager.getCurrentSubPeriod();

        // Rotate up
        if (subPeriod == rotateUpSubPeriod) {
            normalizedValue = timePeriodManager.getNormalizedCurrentSubPeriodTime();
            Interpolation interpolation = Interpolation.linear;
            if (this.interpolation != null) {
                interpolation = this.interpolation;
            }
            angle = interpolation.apply(minAngle, maxAngle, normalizedValue);
        }

        // Stall up
        else if (subPeriod == stallUpSubPeriod) {
            angle = maxAngle;
        }

        // Rotate down
        else if (subPeriod == rotateDownSubPeriod) {
            normalizedValue = timePeriodManager.getNormalizedCurrentSubPeriodTime();
            Interpolation interpolation = Interpolation.linear;
            if (this.interpolation != null) {
                interpolation = this.interpolation;
            }
            angle = interpolation.apply(maxAngle, minAngle, normalizedValue);
        }

        // Stall up
        else {
            angle = minAngle;
        }

        actor.setRotation(angle);
    }

    @Override
    public long getMaxSleepTime(Actor actor) {
        int subPeriod = timePeriodManager.getCurrentSubPeriod();
        if (subPeriod == rotateUpSubPeriod || subPeriod == rotateDownSubPeriod) {
            return 0;
        }

        return timePeriodManager.getSubPeriodDuration(subPeriod) -
                timePeriodManager.getCurrentSubPeriodTime();
    }

    public float getMinAngle() {
        return minAngle;
    }

    public void setMinAngle(float minAngle) {
        this.minAngle = minAngle;
    }

    public float getMaxAngle() {
        return maxAngle;
    }

    public void setMaxAngle(float maxAngle) {
        this.maxAngle = maxAngle;
    }

    public Interpolation getInterpolation() {
        return interpolation;
    }

    public void setInterpolation(Interpolation interpolation) {
        this.interpolation = interpolation;
    }
}
