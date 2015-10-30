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
import com.quadbits.gdxhelper.utils.ParabolaEquation;
import com.quadbits.gdxhelper.utils.Recyclable;

import javax.inject.Inject;

/**
 *
 */
public class ParabolaAroundFixedPointController extends ParabolaController
        implements Recyclable<ParabolaAroundFixedPointController> {
    protected Pool<ParabolaAroundFixedPointController> parabolaAroundFixedPointControllerPool;
    float refActorMinX;
    float refActorMaxX;
    Actor refActor;
    boolean invertRefActorX;

    @Inject
    public ParabolaAroundFixedPointController(ParabolaEquation parabolaEq) {
        super(parabolaEq);
    }

    @Override
    public void reset() {
        super.reset();

        refActorMinX = refActorMaxX = 0;
        refActor = null;
        invertRefActorX = false;
    }

    @Override
    public void free() {
        parabolaAroundFixedPointControllerPool.free(this);
    }

    @Override
    public void setPool(
            Pool<ParabolaAroundFixedPointController> parabolaAroundFixedPointControllerPool) {
        this.parabolaAroundFixedPointControllerPool = parabolaAroundFixedPointControllerPool;
    }

    @Override
    public float getT() {
        float refActorX = refActor.getX();
        if (invertRefActorX) {
            refActorX = -refActorX;
        }

        float t = (refActorX - refActorMinX) / (refActorMaxX - refActorMinX);
        if (parabolaEq.getTraverseDirection() == ParabolaEquation.TraverseDirection.RIGHT_TO_LEFT) {
            t = 1 - t;
        }

        return t;
    }

    public float getRefActorMinX() {
        return refActorMinX;
    }

    public void setRefActorMinX(float refActorMinX) {
        this.refActorMinX = refActorMinX;
    }

    public float getRefActorMaxX() {
        return refActorMaxX;
    }

    public void setRefActorMaxX(float refActorMaxX) {
        this.refActorMaxX = refActorMaxX;
    }

    public Actor getRefActor() {
        return refActor;
    }

    public void setRefActor(Actor refActor) {
        this.refActor = refActor;
    }

    public boolean isInvertRefActorX() {
        return invertRefActorX;
    }

    public void setInvertRefActorX(boolean invertRefActorX) {
        this.invertRefActorX = invertRefActorX;
    }

    public void setFixedPointRef(Actor refActor, float refActorMinX, float refActorMaxX,
                                 boolean invertRefActorX) {
        this.refActor = refActor;
        this.refActorMinX = refActorMinX;
        this.refActorMaxX = refActorMaxX;
        this.invertRefActorX = invertRefActorX;
    }
}
