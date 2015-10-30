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

/**
 *
 */
public class TimeBasedInterpolator {
    float startValue;
    float endValue;
    long totalDurationMillis;
    long accumulatedTimeMillis;

    public TimeBasedInterpolator(long totalDurationMillis) {
        this.startValue = 0;
        this.endValue = 1;
        this.accumulatedTimeMillis = 0;
        this.totalDurationMillis = totalDurationMillis;
    }

    public TimeBasedInterpolator(long totalDurationMillis, float startValue, float endValue) {
        this.startValue = startValue;
        this.endValue = endValue;
        this.accumulatedTimeMillis = 0;
        this.totalDurationMillis = totalDurationMillis;
    }

    public float interpolate(long deltaTimeMillis) {
        // If we have already reached the target value, simply return it
        if (this.accumulatedTimeMillis >= this.totalDurationMillis) {
            return this.endValue;
        }

        // Add delta time to the accumulated elapsed time
        this.accumulatedTimeMillis += deltaTimeMillis;

        // Clamp accumulated time
        if (this.accumulatedTimeMillis > this.totalDurationMillis) {
            this.accumulatedTimeMillis = this.totalDurationMillis;
        }

        // Interpolate value
        float alpha = this.startValue +
                (this.endValue - this.startValue) * this.accumulatedTimeMillis /
                        this.totalDurationMillis;

        return alpha;
    }

    public void reset() {
        this.accumulatedTimeMillis = 0;
    }

    public float getStartValue() {
        return startValue;
    }

    public void setStartValue(float startValue) {
        this.startValue = startValue;
    }

    public float getEndValue() {
        return endValue;
    }

    public void setEndValue(float endValue) {
        this.endValue = endValue;
    }

    public long getTotalDurationMillis() {
        return totalDurationMillis;
    }

    public void setTotalDurationMillis(long totalDurationMillis) {
        this.totalDurationMillis = totalDurationMillis;
    }

    public long getAccumulatedTimeMillis() {
        return accumulatedTimeMillis;
    }

    public void setAccumulatedTimeMillis(long accumulatedTimeMillis) {
        this.accumulatedTimeMillis = accumulatedTimeMillis;
    }
}
