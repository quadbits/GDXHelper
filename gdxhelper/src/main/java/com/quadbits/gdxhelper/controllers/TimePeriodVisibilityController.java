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
import com.quadbits.gdxhelper.utils.TimeManager;

import javax.inject.Inject;

/**
 *
 */
public class TimePeriodVisibilityController extends BaseController
        implements Recyclable<TimePeriodVisibilityController> {
    protected Pool<TimePeriodVisibilityController> timePeriodVisibilityControllerPool;
    protected TimeManager.TimeFuzzyPeriodMembershipFunction membershipFunction;

    @Inject
    protected TimeManager timeManager;

    @Inject
    public TimePeriodVisibilityController() {
        super();
        reset();
    }

    @Override
    public void reset() {
        membershipFunction = null;
    }

    @Override
    public void free() {
        timePeriodVisibilityControllerPool.free(this);
    }

    @Override
    public void setPool(Pool<TimePeriodVisibilityController> timePeriodVisibilityControllerPool) {
        this.timePeriodVisibilityControllerPool = timePeriodVisibilityControllerPool;
    }

    @Override
    public void control(Actor actor, float deltaSeconds) {
        if (timeManager == null || membershipFunction == null) {
            return;
        }

        float alpha = membershipFunction.evaluate(timeManager);
        actor.getColor().a = alpha;

        if (alpha <= 0) {
            actor.setVisible(false);
        } else {
            actor.setVisible(true);
        }
    }

    public TimeManager.TimeFuzzyPeriodMembershipFunction getMembershipFunction() {
        return membershipFunction;
    }

    public void setMembershipFunction(
            TimeManager.TimeFuzzyPeriodMembershipFunction membershipFunction) {
        this.membershipFunction = membershipFunction;
    }
}
