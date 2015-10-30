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
import com.quadbits.gdxhelper.actors.SpriteActor;
import com.quadbits.gdxhelper.utils.Recyclable;

import javax.inject.Inject;

/**
 *
 */
public class RotationSimpleController extends BaseController
        implements NonContinuousRenderingController, Recyclable<RotationSimpleController> {
    protected Pool<RotationSimpleController> rotationSimpleControllerPool;
    /**
     * Rotation speed in degrees / millis
     */
    float rotationSpeed;

    /**
     * A boolean indicating if the rotation direction should be reversed if the actor is flipped
     */
    boolean reverseOnFlip;

    @Inject
    public RotationSimpleController() {
        super();
        init();
    }

    private void init() {
        rotationSpeed = 0;
        reverseOnFlip = true;
    }

    @Override
    public void reset() {
        init();
    }

    @Override
    public void free() {
        rotationSimpleControllerPool.free(this);
    }

    @Override
    public void setPool(Pool<RotationSimpleController> rotationSimpleControllerPool) {
        this.rotationSimpleControllerPool = rotationSimpleControllerPool;
    }

    @Override
    public void control(Actor actor, float deltaSeconds) {
        // Do nothing on invisible actors
        if (!actor.isVisible()) {
            return;
        }

        // Calculate actual rotation
        float rotation = this.rotationSpeed * deltaSeconds * 1000;

        // Reverse rotation?
        if (actor instanceof SpriteActor) {
            SpriteActor spriteActor = (SpriteActor) actor;
            if (reverseOnFlip && (spriteActor.isFlipX() && !spriteActor.isFlipY() ||
                    !spriteActor.isFlipX() && spriteActor.isFlipY())) {
                rotation = -rotation;
            }
        }

        // Apply rotation
        actor.rotateBy(rotation);
    }

    @Override
    public long getMaxSleepTime(Actor actor) {
        if (actor.isVisible()) {
            return 0;
        }

        return Long.MAX_VALUE;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public boolean isReverseOnFlip() {
        return reverseOnFlip;
    }

    public void setReverseOnFlip(boolean reverseOnFlip) {
        this.reverseOnFlip = reverseOnFlip;
    }
}
