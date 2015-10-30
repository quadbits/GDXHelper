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

import java.util.ArrayList;

import javax.inject.Inject;

/**
 *
 */
public class TimePeriodManager implements Pool.Poolable {
    protected ArrayList<Long> subPeriodDurations;
    protected long periodDuration;
    protected long currentPeriodTime;
    protected long currentSubPeriodTime;
    protected int currentSubPeriod;

    @Inject
    public TimePeriodManager() {
        subPeriodDurations = new ArrayList<Long>();
    }

    private void init() {
        subPeriodDurations.clear();
        periodDuration = 0;
        currentPeriodTime = 0;
        currentSubPeriodTime = 0;
        currentSubPeriod = -1;
    }

    @Override
    public void reset() {
        init();
    }

    public void addSubPeriod(long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("duration must be >= 0 (current value is " +
                    duration + ")");
        }
        subPeriodDurations.add(duration);
        periodDuration += duration;
        if (currentSubPeriod == -1) {
            currentSubPeriod = 0;
        }

    }

    public void update(long deltaTime) {
        currentPeriodTime = (currentPeriodTime + deltaTime) % periodDuration;
        long tmpTime = currentPeriodTime;
        for (currentSubPeriod = 0;
             currentSubPeriod < subPeriodDurations.size();
             currentSubPeriod++) {
            long subPeriodDuration = subPeriodDurations.get(currentSubPeriod);
            if (tmpTime <= subPeriodDuration) {
                break;
            }
            tmpTime -= subPeriodDuration;
        }
        currentSubPeriodTime = tmpTime;
    }

    public long getPeriodDuration() {
        return periodDuration;
    }

    public int getNumSubPeriods() {
        return subPeriodDurations.size();
    }

    public long getSubPeriodDuration(int subPeriod) {
        return subPeriodDurations.get(subPeriod);
    }

    public int getCurrentSubPeriod() {
        return currentSubPeriod;
    }

    public long getCurrentPeriodTime() {
        return currentPeriodTime;
    }

    public long getCurrentSubPeriodTime() {
        return currentSubPeriodTime;
    }

    public float getNormalizedCurrentPeriodTime() {
        return (float) currentPeriodTime / (float) periodDuration;
    }

    public float getNormalizedCurrentSubPeriodTime() {
        return (float) currentSubPeriodTime / (float) subPeriodDurations.get(currentSubPeriod);
    }
}
