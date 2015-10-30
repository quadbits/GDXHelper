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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.Recyclable;
import com.quadbits.gdxhelper.utils.TimeManager;

import javax.inject.Inject;

/**
 *
 */
public class TimePeriodTintController extends BaseController
        implements NonContinuousRenderingController, Recyclable<TimePeriodTintController> {
    protected Pool<TimePeriodTintController> timePeriodTintControllerPool;

    protected TimeManager.TimeFuzzyPeriodMembershipFunction membershipFunction;

    protected Array<Color> colors;
    protected float colorCrossBlend;
    protected int primaryColorIndex;
    protected int secondaryColorIndex;

    protected float fadeAnimDurationSeconds;
    protected float fadeAnimMaxDeltaSeconds;
    protected boolean crossFading;
    protected float targetColorCrossBlend;

    protected Color tmpColor;

    public static final float DEFAULT_FADE_ANIM_DURATION_SECONDS = 1f;
    public static final float DEFAULT_FADE_ANIM_MAX_DELTA_SECONDS = 1.f / 30.f;

    @Inject
    protected TimeManager timeManager;

    @Inject
    public TimePeriodTintController() {
        super();
        colors = new Array<Color>();
        colors.add(new Color());
        tmpColor = new Color();

        reset();
    }

    @Override
    public void reset() {
        membershipFunction = null;

        primaryColorIndex = 0;
        secondaryColorIndex = -1;
        colorCrossBlend = 0;

        fadeAnimDurationSeconds = DEFAULT_FADE_ANIM_DURATION_SECONDS;
        fadeAnimMaxDeltaSeconds = DEFAULT_FADE_ANIM_MAX_DELTA_SECONDS;
        crossFading = false;
        targetColorCrossBlend = 0;
    }

    @Override
    public void free() {
        timePeriodTintControllerPool.free(this);
    }

    @Override
    public void setPool(Pool<TimePeriodTintController> timePeriodTintControllerPool) {
        this.timePeriodTintControllerPool = timePeriodTintControllerPool;
    }

    public void setColorsSize(int colorsSize) {
        if (colorsSize < 1) {
            throw new IndexOutOfBoundsException(
                    "palette size must be >= 1, provided size = " + colorsSize);
        }

        while (colors.size < colorsSize) {
            colors.add(new Color());
        }
    }

    @Override
    public long getMaxSleepTime(Actor actor) {
        if (crossFading) {
            return 0;
        }

        return Long.MAX_VALUE;
    }

    @Override
    public void control(Actor actor, float deltaSeconds) {
        if (crossFading) {
            float deltaColorCrossBlend = deltaSeconds / fadeAnimDurationSeconds;
            colorCrossBlend +=
                    (targetColorCrossBlend == 1) ? deltaColorCrossBlend : -deltaColorCrossBlend;

            if (colorCrossBlend <= 0) {
                colorCrossBlend = 0;
                crossFading = false;
            }

            if (colorCrossBlend >= 1) {
                colorCrossBlend = 1;
                crossFading = false;
            }
        }

        float blend = membershipFunction.evaluate(timeManager);
        //if (blend == 0) {
        //    return;
        //}

        tmpColor.set(colors.get(primaryColorIndex));
        if (colorCrossBlend != 0 && secondaryColorIndex >= 0) {
            tmpColor.lerp(colors.get(secondaryColorIndex), colorCrossBlend);
        }

        Color actorColor = actor.getColor();
        actorColor.set(1, 1, 1, actorColor.a);
        actorColor.lerp(tmpColor, blend);
        actor.setColor(actorColor);
    }

    public int getPrimaryColorIndex() {
        return primaryColorIndex;
    }

    public void setPrimaryColorIndex(int primaryColorIndex) {
        if (primaryColorIndex < 0 || primaryColorIndex >= colors.size) {
            throw new IndexOutOfBoundsException("index value = " + primaryColorIndex +
                    ", size = " + colors.size);
        }
        this.primaryColorIndex = primaryColorIndex;
    }

    public int getSecondaryColorIndex() {
        return secondaryColorIndex;
    }

    public void setSecondaryColorIndex(int secondaryColorIndex) {
        if (secondaryColorIndex < 0 || secondaryColorIndex >= colors.size) {
            throw new IndexOutOfBoundsException("index value = " + secondaryColorIndex +
                    ", size = " + colors.size);
        }
        this.secondaryColorIndex = secondaryColorIndex;
    }

    public float getColorCrossBlend() {
        return colorCrossBlend;
    }

    public void activatePrimaryPalette() {
        if (colorCrossBlend == 0) {
            return;
        }

        targetColorCrossBlend = 0;
        crossFading = true;
    }

    public void activateSecondaryPalette() {
        if (colorCrossBlend == 1) {
            return;
        }

        targetColorCrossBlend = 1;
        crossFading = true;
    }

    public float getFadeAnimDurationSeconds() {
        return fadeAnimDurationSeconds;
    }

    public void setFadeAnimDurationSeconds(float fadeAnimDurationSeconds) {
        this.fadeAnimDurationSeconds = fadeAnimDurationSeconds;
    }

    public float getFadeAnimMaxDeltaSeconds() {
        return fadeAnimMaxDeltaSeconds;
    }

    public void setFadeAnimMaxDeltaSeconds(float fadeAnimMaxDeltaSeconds) {
        this.fadeAnimMaxDeltaSeconds = fadeAnimMaxDeltaSeconds;
    }

    // Shortcuts to primary palette accessors
    public Color getColor() {
        return colors.get(primaryColorIndex);
    }

    public void setColor(Color color) {
        colors.get(primaryColorIndex).set(color);
    }

    // General accessors
    public Color getColor(int paletteIndex) {
        return colors.get(paletteIndex);
    }

    public void setColor(int paletteIndex, Color color) {
        colors.get(paletteIndex).set(color);
    }

    public TimeManager.TimeFuzzyPeriodMembershipFunction getMembershipFunction() {
        return membershipFunction;
    }

    public void setMembershipFunction(
            TimeManager.TimeFuzzyPeriodMembershipFunction membershipFunction) {
        this.membershipFunction = membershipFunction;
    }
}
