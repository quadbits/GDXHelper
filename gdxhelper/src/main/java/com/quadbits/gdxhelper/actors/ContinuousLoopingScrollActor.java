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
package com.quadbits.gdxhelper.actors;

import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.Recyclable;
import com.quadbits.gdxhelper.utils.ScrollTarget;

import javax.inject.Inject;

/**
 *
 */
public class ContinuousLoopingScrollActor extends BaseActor
        implements ScrollTarget, Recyclable<ContinuousLoopingScrollActor> {
    protected Pool<ContinuousLoopingScrollActor> continuousLoopingScrollActorPool;
    long scrollDurationMillis;
    long restDurationMillis;
    long currentTimeMillis;
    State currentState;

    public static enum State {
        REST_LOW, FORWARD, REST_HIGH, BACKWARD
    }

    @Inject
    public ContinuousLoopingScrollActor() {
        super();
        reset();
    }

    @Override
    public void reset() {
        currentState = State.REST_LOW;
    }

    @Override
    public void free() {
        continuousLoopingScrollActorPool.free(this);
    }

    @Override
    public void setPool(Pool<ContinuousLoopingScrollActor> continuousLoopingScrollActorPool) {
        this.continuousLoopingScrollActorPool = continuousLoopingScrollActorPool;
    }

    protected long getPeriod() {
        return 2 * (scrollDurationMillis + restDurationMillis);
    }

    protected long getNormalizedTime() {
        long period = getPeriod();
        return currentTimeMillis % period;
    }

    protected void updateCurrentState() {
        long normalizedTime = getNormalizedTime();

        if (normalizedTime < restDurationMillis) {
            currentState = State.REST_LOW;
        } else if (normalizedTime < restDurationMillis + scrollDurationMillis) {
            currentState = State.FORWARD;
        } else if (normalizedTime < 2 * restDurationMillis + scrollDurationMillis) {
            currentState = State.REST_HIGH;
        } else {
            currentState = State.BACKWARD;
        }
    }

    @Override
    public void act(float deltaTime) {
        super.act(deltaTime);

        // Add deltaTime to current time
        currentTimeMillis += (long) (deltaTime * 1000);
        updateCurrentState();
    }

    @Override
    public float getTargetScroll() {
        // Calculate target scroll depending on the state
        long normalizedTime;
        switch (currentState) {
            case REST_LOW:
                return 0f;
            case FORWARD:
                normalizedTime = getNormalizedTime();
                return (float) (normalizedTime - restDurationMillis) / (float) scrollDurationMillis;
            case REST_HIGH:
                return 1f;
            default:
                long period = getPeriod();
                normalizedTime = getNormalizedTime();
                return (float) (period - normalizedTime) / (float) scrollDurationMillis;
        }
    }

    @Override
    public long getMaxSleepTime() {
        long normalizedTime = getNormalizedTime();
        switch (currentState) {
            case REST_LOW:
                return (restDurationMillis - normalizedTime);
            case FORWARD:
                return 0;
            case REST_HIGH:
                return (2 * restDurationMillis + scrollDurationMillis - normalizedTime);
            default:
                return 0;
        }
    }

    @Override
    public float maxScrollVelocityMillis() {
        return 1.f / scrollDurationMillis;
    }

    public long getScrollDurationMillis() {
        return scrollDurationMillis;
    }

    public void setScrollDurationMillis(long scrollDurationMillis) {
        this.scrollDurationMillis = scrollDurationMillis;
    }

    public long getRestDurationMillis() {
        return restDurationMillis;
    }

    public void setRestDurationMillis(long restDurationMillis) {
        this.restDurationMillis = restDurationMillis;
    }
}
