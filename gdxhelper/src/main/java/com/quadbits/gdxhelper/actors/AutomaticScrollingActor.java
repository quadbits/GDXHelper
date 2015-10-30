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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.screens.LWPScreen;
import com.quadbits.gdxhelper.utils.Recyclable;
import com.quadbits.gdxhelper.utils.ScrollTarget;

import javax.inject.Inject;

/**
 *
 */
public class AutomaticScrollingActor extends BaseActor
        implements Recyclable<AutomaticScrollingActor> {
    protected Pool<AutomaticScrollingActor> automaticScrollingActorPool;
    ScrollTarget scrollTarget;
    float scrollVelocityMillis;
    float scrollAccelFactor;
    State state;
    boolean paused;
    ScrollVariable scrollVariable;

    boolean debug;
    float debugAccelMillis;
    boolean debugBraking;
    float debugLineLengthMultiplier;

    @Inject
    LWPScreen screen;

    @Inject
    ShapeRenderer shapeRenderer;

    public static final float DEFAULT_SCROLL_ACCEL_FACTOR = 1;
    public static final float DEFAULT_DEBUG_LINE_LENGTH_MULTIPLIER = 1;

    /**
     * An enum for indicating the state of the actor
     */
    public static enum State {
        REST, BURST
    }

    /**
     * The scroll variable on which the actor will actuate: scrollX or scrollY
     */
    public static enum ScrollVariable {
        SCROLL_X, SCROLL_Y
    }

    @Inject
    public AutomaticScrollingActor() {
        super();
        this.reset();
    }

    @Override
    public void reset() {
        this.state = State.BURST;
        this.scrollVariable = ScrollVariable.SCROLL_Y;
        this.paused = false;
        this.scrollAccelFactor = DEFAULT_SCROLL_ACCEL_FACTOR;
        this.debugLineLengthMultiplier = DEFAULT_DEBUG_LINE_LENGTH_MULTIPLIER;
    }

    @Override
    public void free() {
        automaticScrollingActorPool.free(this);
    }

    @Override
    public void setPool(Pool<AutomaticScrollingActor> automaticScrollingActorPool) {
        this.automaticScrollingActorPool = automaticScrollingActorPool;
    }

    @Override
    public void act(float deltaTime) {
        super.act(deltaTime);

        // check parameters
        if (this.scrollTarget == null) {
            return;
        }

        // initialize debug variables
        this.debugAccelMillis = 0;
        this.debugBraking = false;

        // if paused, just return
        if (this.paused) {
            return;
        }

        // get current scroll value
        float currentScroll;
        if (this.scrollVariable == ScrollVariable.SCROLL_Y) {
            currentScroll = this.screen.getScrollY();
        } else {
            currentScroll = this.screen.getScrollX();
        }

        // get target scroll value
        float targetScroll = this.scrollTarget.getTargetScroll();
        if (this.debug) {
            Gdx.app.log("AutomaticScrollingActor", "scrollTarget = " + targetScroll);
        }

        // decide whether to interpolate or just simply advance scroll
        float newScroll;
        float minScrollVelocityMillis = this.scrollTarget.maxScrollVelocityMillis();
        float maxScrollDistance = minScrollVelocityMillis * 33;
        float scrollDiff = targetScroll - currentScroll;
        float scrollDiffAbs = Math.abs(scrollDiff);
        float scrollVelocityMillisAbs = Math.abs(this.scrollVelocityMillis);

        // set equal
        if (scrollDiffAbs <= maxScrollDistance &&
                scrollVelocityMillisAbs <= minScrollVelocityMillis) {
            newScroll = targetScroll;
            this.scrollVelocityMillis = 0;
            this.state = State.REST;
        }

        // accelerate
        else {

            float scrollAccelMillis =
                    minScrollVelocityMillis * this.scrollAccelFactor * Math.signum(scrollDiff);
            float scrollAccelMillisAbs = Math.abs(scrollAccelMillis);

            // Default behaviour is to accelerate; check for cases where we should stop accelerating
            // or directly break

            // If scroll velocity is 0, always accelerate; check other cases
            if (this.scrollVelocityMillis != 0) {
                // calculate required accel for arriving at the desired scroll value with velocity 0
                float targetAccelMillis =
                        -(this.scrollVelocityMillis * this.scrollVelocityMillis) / (2 * scrollDiff);
                float targetAccelMillisAbs = Math.abs(targetAccelMillis);
                float targetAccelSignum = Math.signum(targetAccelMillis);
                float scrollVelocitySignum = Math.signum(this.scrollVelocityMillis);

                // If target acceleration is in the same direction as the current velocity, we are
                // going the wrong way; continue accelerating. Otherwise, check for other cases
                if (scrollVelocitySignum != targetAccelSignum) {
                    // stop accelerating: target accel and scroll accel are approximately equal
                    // (their difference is less than 10% of scroll accel)
                    if (targetAccelMillisAbs >= scrollAccelMillisAbs * 0.9f &&
                            targetAccelMillisAbs <= scrollAccelMillisAbs * 1.1f) {
                        scrollAccelMillis = 0;
                    }

                    // brake: set the necessary acceleration to arrive at velocity 0
                    else if (targetAccelMillisAbs > scrollAccelMillisAbs * 1.1f) {
                        scrollAccelMillis = targetAccelMillis;
                        this.debugBraking = true;
                    }
                }
            }

            // save scroll accel for debug purposes
            this.debugAccelMillis = scrollAccelMillis;

            // update scroll velocity according to scroll acceleration
            this.scrollVelocityMillis += scrollAccelMillis * deltaTime * 1000;

            // update new scroll according to scroll velocity
            newScroll = currentScroll + this.scrollVelocityMillis * deltaTime * 1000;

            // do not overscroll
            if (newScroll > 1) {
                newScroll = 1;
                this.scrollVelocityMillis = 0;
            }

            if (newScroll < 0) {
                newScroll = 0;
                this.scrollVelocityMillis = 0;
            }

            this.state = State.BURST;
        }

        // change scroll according to alpha

        // scrollY
        if (this.scrollVariable == ScrollVariable.SCROLL_Y) {
            this.screen.setScrollY(newScroll);
        }

        // scrollX
        else {
            this.screen.setScrollX(newScroll);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (this.debug) {
            if (this.debugAccelMillis == 0) {
                return;
            }

            Color lineColor;
            if (this.debugBraking) {
                lineColor = Color.RED;
            } else {
                lineColor = Color.GREEN;
            }

            float lineLength = this.debugAccelMillis * this.debugLineLengthMultiplier;

            batch.end();

            this.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            this.shapeRenderer.setColor(lineColor);
            float cx = Gdx.graphics.getWidth() / 2;
            float cy = Gdx.graphics.getHeight() / 2;
            this.shapeRenderer.line(cx, cy, cx, cy + lineLength);
            this.shapeRenderer.end();

            batch.begin();
        }
    }

    @Override
    public long getMaxSleepTime() {
        if (this.paused) {
            return Long.MAX_VALUE;
        }

        if (this.state == State.BURST) {
            return 0;
        }

        return this.scrollTarget.getMaxSleepTime();
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;

        if (this.screen.isFlingEnabled()) {
            float scrollVelocitySeconds;
            if (this.scrollVariable == ScrollVariable.SCROLL_Y) {
                scrollVelocitySeconds = this.screen.getFlingVelocityY();
            } else {
                scrollVelocitySeconds = this.screen.getFlingVelocityX();
            }
            this.scrollVelocityMillis = scrollVelocitySeconds * 1000;
        }
    }

    public ScrollTarget getScrollTarget() {
        return scrollTarget;
    }

    public void setScrollTarget(ScrollTarget scrollTarget) {
        this.scrollTarget = scrollTarget;
    }

    public ScrollVariable getScrollVariable() {
        return scrollVariable;
    }

    public void setScrollVariable(ScrollVariable scrollVariable) {
        this.scrollVariable = scrollVariable;
    }

    public float getScrollAccelFactor() {
        return scrollAccelFactor;
    }

    public void setScrollAccelFactor(float scrollAccelFactor) {
        this.scrollAccelFactor = scrollAccelFactor;
    }

    public boolean isDebug() {
        return debug;
    }

    @Override
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public float getDebugLineLengthMultiplier() {
        return debugLineLengthMultiplier;
    }

    public void setDebugLineLengthMultiplier(float debugLineLengthMultiplier) {
        this.debugLineLengthMultiplier = debugLineLengthMultiplier;
    }
}
