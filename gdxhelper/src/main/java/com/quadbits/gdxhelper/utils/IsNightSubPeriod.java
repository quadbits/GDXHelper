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
package com.quadbits.gdxhelper.utils;

import com.badlogic.gdx.utils.Pool;

/**
 *
 */
public class IsNightSubPeriod
        implements TimeManager.TimeFuzzyPeriodMembershipFunction, Pool.Poolable {
    protected float tBegin;
    protected float tEnd;
    protected float tTransition;

    @Override
    public void reset() {
        tBegin = tEnd = tTransition = 0;
    }

    @Override
    public float evaluate(TimeManager timeManager) {
        float tNight = timeManager.getTNight();

        if (tNight < tBegin) {
            return 0;
        } else if (tNight > tBegin && tNight < tBegin + tTransition) {
            return (tNight - tBegin) / tTransition;
        } else if (tNight < tEnd - tTransition) {
            return 1;
        } else if (tNight < tEnd) {
            return (tEnd - tNight) / tTransition;
        } else {
            return 0;
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
