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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.quadbits.gdxhelper.utils.ParabolaEquation;

/**
 *
 */
public abstract class ParabolaController extends BaseController {
    protected ParabolaEquation parabolaEq;
    protected OutOfBoundsPolicy outOfBoundsPolicy;
    private Vector2 tmpVec;

    public enum OutOfBoundsPolicy {
        HIDE, FREEZE
    }

    public ParabolaController(ParabolaEquation parabolaEq) {
        super();

        this.parabolaEq = parabolaEq;
        tmpVec = new Vector2();
        reset();
    }

    @Override
    public void reset() {
        parabolaEq.setBounds(0, 0, 0, 0);
        outOfBoundsPolicy = OutOfBoundsPolicy.HIDE;
        tmpVec.set(0, 0);
    }

    public abstract float getT();

    @Override
    public void control(Actor actor, float deltaSeconds) {
        float t = getT();

        // Check t bounds
        if (t < 0 || t > 1) {
            if (outOfBoundsPolicy == OutOfBoundsPolicy.HIDE) {
                actor.setVisible(false);
            }
            return;
        }

        // Calculate the actor's position using the parabola equation
        actor.setVisible(true);
        parabolaEq.getPosition(t, tmpVec);
        actor.setPosition(tmpVec.x, tmpVec.y);
    }

    public void setParabolaBounds(float minX, float maxX, float minY, float maxY) {
        parabolaEq.setBounds(minX, maxX, minY, maxY);
    }

    public void setTraverseDirection(ParabolaEquation.TraverseDirection traverseDirection) {
        parabolaEq.setTraverseDirection(traverseDirection);
    }

    public OutOfBoundsPolicy getOutOfBoundsPolicy() {
        return outOfBoundsPolicy;
    }

    public void setOutOfBoundsPolicy(OutOfBoundsPolicy outOfBoundsPolicy) {
        this.outOfBoundsPolicy = outOfBoundsPolicy;
    }
}
