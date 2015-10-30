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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.ParabolaEquation;
import com.quadbits.gdxhelper.utils.Recyclable;

import javax.inject.Inject;

/**
 *
 */
public class ParabolaAnimationController extends ParabolaController
        implements NonContinuousRenderingController, Recyclable<ParabolaAnimationController> {

    protected Pool<ParabolaAnimationController> parabolaAnimationControllerPool;
    protected State state;
    protected long animationStartMillis;
    protected long durationMillis;
    protected Array<AnimationFinishedListener> animationFinishedListeners;
    protected Actor actor;

    public static final long DEFAULT_DURATION = 1000;

    public enum State {NOT_STARTED, STARTED, STOPPED}

    public interface AnimationFinishedListener {
        void onFinish(ParabolaAnimationController controller);
    }

    @Inject
    public ParabolaAnimationController(ParabolaEquation parabolaEq) {
        super(parabolaEq);
    }

    @Override
    public void reset() {
        super.reset();

        state = State.NOT_STARTED;
        durationMillis = DEFAULT_DURATION;
        animationStartMillis = -1;
        animationFinishedListeners = new Array<AnimationFinishedListener>();
        actor = null;
    }

    @Override
    public void free() {
        parabolaAnimationControllerPool.free(this);
    }

    @Override
    public void setPool(Pool<ParabolaAnimationController> parabolaAnimationControllerPool) {
        this.parabolaAnimationControllerPool = parabolaAnimationControllerPool;
    }

    @Override
    public void control(Actor actor, float deltaSeconds) {
        this.actor = actor;
        super.control(actor, deltaSeconds);
    }

    @Override
    public float getT() {
        if (state == State.NOT_STARTED) {
            return -1;
        }

        if (state == State.STOPPED) {
            return 2;
        }

        long currentTimeMillis = System.currentTimeMillis();
        float t = (float) (currentTimeMillis - animationStartMillis) / (float) durationMillis;
        if (t > 1) {
            state = State.STOPPED;
            for (AnimationFinishedListener listener : animationFinishedListeners) {
                listener.onFinish(this);
            }
        }

        return t;
    }

    @Override
    public long getMaxSleepTime(Actor actor) {
        if (state == State.STARTED) {
            // This is equivalent to requesting an immediate rendering
            return 0;
        }

        return Long.MAX_VALUE;
    }

    public void startAnimation() {
        animationStartMillis = System.currentTimeMillis();
        state = State.STARTED;
    }

    public void addAnimationFinishedListener(AnimationFinishedListener listener) {
        animationFinishedListeners.add(listener);
    }

    public void removeAnimationFinishedListener(AnimationFinishedListener listener) {
        animationFinishedListeners.removeValue(listener, true);
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public State getState() {
        return state;
    }

    public Actor getActor() {
        return actor;
    }
}
