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

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.actors.FlippableActor;
import com.quadbits.gdxhelper.utils.Recyclable;

import javax.inject.Inject;

/**
 *
 */
public class LinearTrajectoryController extends BaseController
        implements NonContinuousRenderingController, Recyclable<LinearTrajectoryController> {
    protected Pool<LinearTrajectoryController> linearTrajectoryControllerPool;

    /**
     * The average speed of the actor, in pixels/millisecond
     */
    float avgSpeedMillis;

    /**
     * The standard deviation of the actor's speed, relative to pixels/millisecond units
     */
    float stdSpeedMillis;

    /**
     * Variables specifying the source line-segment from which the actor can depart
     */
    float sourceX1, sourceX2, sourceY1, sourceY2;

    /**
     * Variables specifying the target line-segment at which the actor can arrive
     */
    float targetX1, targetX2, targetY1, targetY2;

    /**
     * A vector for specifying the current source point (for internal use only)
     */
    Vector2 currentSource;

    /**
     * A vector for specifying the current target point (for internal use only)
     */
    Vector2 currentTarget;

    /**
     * A vector to use as a base reference direction when a maximum deviation angle is specified
     */
    Vector2 referenceDirection;

    /**
     * The maximum deviation angle allowed from the reference direction vector when creating new
     * trajectories (i.e., entering the scene)
     */
    float referenceMaxDeviationAngleDegrees;

    /**
     * Should the actor be rotated to match the direction vector's angle?
     */
    boolean adjustActorRotation;

    /**
     * A temporal vector for internal calculations
     */
    Vector2 tmpVector;

    /**
     * A value in [0,1] for interpolating over the trajectory vector (for internal use only)
     */
    float currentInterpolationValue;

    /**
     * The normalized value of the current speed for interpolating over trajectory (for internal
     * use only)
     */
    float interpolationStep;

    /**
     * If true, target and source are interchangeable, and the actor will
     * randomly depart from any of them
     */
    boolean reversable;

    /**
     * A boolean indicating if the actor is out of the scene (for internal use only)
     */
    boolean outOfScene;

    /**
     * The average time, in milliseconds, to be spent by the actor
     * outside of the scene once it arrives to target
     */
    long avgOutOfSceneTimeMillis;

    /**
     * The standard deviation, in units relative to milliseconds, to be spent by the actor
     * outside of the scene once it arrives to target
     */
    float stdOutOfSceneTimeMillis;

    /**
     * The actual number of milliseconds that the actor is going to spent
     * outside of the scene this time (for internal use only)
     */
    long outOfSceneTimeMillis;

    /**
     * The time already spent by the actor outside of the scene. This value is compared
     * with that of {@link #outOfSceneTimeMillis} to decide if the actor should be respawn
     * (for internal use only)
     */
    long currentOutOfSceneTimeMillis;

    /**
     * A random number generator (for internal use only)
     */
    @Inject
    RandomXS128 random;

    @Inject
    public LinearTrajectoryController() {
        currentSource = new Vector2();
        currentTarget = new Vector2();
        referenceDirection = new Vector2();
        tmpVector = new Vector2();

        reset();
    }

    @Override
    public void reset() {
        avgSpeedMillis = 0;
        stdSpeedMillis = 0;
        sourceX1 = sourceX2 = sourceY1 = sourceY2 = 0;
        targetX1 = targetX2 = targetY1 = targetY2 = 0;
        currentSource.set(0, 0);
        currentTarget.set(0, 0);
        referenceDirection.set(1, 0);
        referenceMaxDeviationAngleDegrees = 360;
        adjustActorRotation = true;
        tmpVector.set(0, 0);
        currentInterpolationValue = 0;
        interpolationStep = 0;
        reversable = false;
        outOfScene = true;
        avgOutOfSceneTimeMillis = 0;
        stdOutOfSceneTimeMillis = 0;
        outOfSceneTimeMillis = 0;
        currentOutOfSceneTimeMillis = 0;
    }

    @Override
    public void free() {
        linearTrajectoryControllerPool.free(this);
    }

    @Override
    public void setPool(Pool<LinearTrajectoryController> linearTrajectoryControllerPool) {
        this.linearTrajectoryControllerPool = linearTrajectoryControllerPool;
    }

    @Override
    public void control(Actor actor, float deltaSeconds) {

        float deltaMillis = deltaSeconds * 1000;

        // out of scene?
        if (outOfScene) {
            // increase time
            currentOutOfSceneTimeMillis += deltaMillis;

            // check if we should make the actor enter the scene again
            if (currentOutOfSceneTimeMillis >= outOfSceneTimeMillis) {
                enterScene(actor);
            }

            // otherwise, keep on waiting
            else {
                return;
            }
        }

        // advance interpolation
        currentInterpolationValue += interpolationStep * deltaMillis;
        if (currentInterpolationValue > 1) {
            currentInterpolationValue = 1;
        }

        // check if we have arrived to target
        if (currentInterpolationValue == 1) {
            exitScene(actor);
            return;
        }

        // move actor
        tmpVector.set(currentSource);
        tmpVector.lerp(currentTarget, currentInterpolationValue);
        actor.setPosition(tmpVector.x, tmpVector.y);
    }

    protected void enterScene(Actor actor) {
        outOfScene = false;
        currentOutOfSceneTimeMillis = 0;
        actor.setVisible(true);
        currentInterpolationValue = 0;

        boolean reverse = false;
        if (reversable) {
            reverse = random.nextBoolean();
        }

        // choose source and target points
        if (!reverse) {
            chooseRandomSourceAndTarget(sourceX1, sourceY1, sourceX2, sourceY2, targetX1, targetY1,
                    targetX2, targetY2);
            if (actor instanceof FlippableActor) {
                ((FlippableActor) actor).setFlipX(false);
            }
        } else {
            chooseRandomSourceAndTarget(targetX1, targetY1, targetX2, targetY2, sourceX1, sourceY1,
                    sourceX2, sourceY2);
            if (actor instanceof FlippableActor) {
                ((FlippableActor) actor).setFlipX(true);
            }
        }

        // calculate interpolation step
        tmpVector.set(currentTarget);
        tmpVector.sub(currentSource);
        float speed = avgSpeedMillis + ((float) random.nextGaussian()) * stdSpeedMillis;
        interpolationStep = speed / tmpVector.len();

        // rotate actor (if requested)
        if (adjustActorRotation) {
            float angle = tmpVector.angle();
            if (angle > 90 && angle <= 180) {
                angle = -(180 - angle);
            }
            if (angle > 180 && angle <= 270) {
                angle = angle - 180;
            }
            actor.setRotation(angle);
        }
    }

    protected void chooseRandomSourceAndTarget(float sx1, float sy1, float sx2, float sy2,
                                               float tx1, float ty1, float tx2, float ty2) {
        // Choose a random source point
        float sx = sx1 + random.nextFloat() * (sx2 - sx1);
        float sy = sy1 + random.nextFloat() * (sy2 - sy1);
        currentSource.set(sx, sy);

        float tx, ty;

        // Choose a random target point
        if (referenceMaxDeviationAngleDegrees >= 360) {
            tx = tx1 + random.nextFloat() * (tx2 - tx1);
            ty = ty1 + random.nextFloat() * (ty2 - ty1);
        }

        // Choose a target point with a deviation not greater than max deviation angle
        else {
            // choose a direction within the deviation angle range
            tmpVector.set(referenceDirection);
            tmpVector.rotate(referenceMaxDeviationAngleDegrees * (-1 + random.nextFloat() * 2));

            boolean trajectoryIsVertical = false, targetIsVertical = false;
            float m1 = 0, b1 = 0, m2 = 0, b2 = 0;

            // parameters of the linear equation of the trajectory
            if (tmpVector.x == 0) {
                trajectoryIsVertical = true;
            } else {
                m1 = tmpVector.y / tmpVector.x;
                b1 = sy - m1 * sx;
            }

            // parameters of the linear equation of the target line
            if (tx2 - tx1 == 0) {
                targetIsVertical = true;
            } else {
                m2 = (ty2 - ty1) / (tx2 - tx1);
                b2 = ty2 - m2 * tx2;
            }

            // find the intersection point (target)

            // case 1: trajectory vector is vertical
            if (trajectoryIsVertical) {
                ty = tmpVector.y;
                tx = (ty - b2) / m2;
            }

            // case 2: target region is vertical
            if (targetIsVertical) {
                tx = tx1;
                ty = m1 * tx + b1;
            }

            // case 3: none of the previous vectors are vertical
            else {
                tx = (b2 - b1) / (m1 - m2);
                ty = m2 * tx + b2;
            }

            // ensure the target point is within the target region
            float tmpSwap;
            if (tx1 > tx2) {
                tmpSwap = tx1;
                tx1 = tx2;
                tx2 = tmpSwap;
            }
            if (ty1 > ty2) {
                tmpSwap = ty1;
                ty1 = ty2;
                ty2 = tmpSwap;
            }
            if (tx < tx1)
                tx = tx1;
            if (tx > tx2)
                tx = tx2;
            if (ty < ty1)
                ty = ty1;
            if (ty > ty2)
                ty = ty2;
        }
        currentTarget.set(tx, ty);
    }

    protected void exitScene(Actor actor) {
        outOfScene = true;
        outOfSceneTimeMillis =
                avgOutOfSceneTimeMillis + (long) (random.nextGaussian() * stdOutOfSceneTimeMillis);
        currentOutOfSceneTimeMillis = 0;
        actor.setVisible(false);
    }

    public float getAvgSpeedMillis() {
        return avgSpeedMillis;
    }

    public void setAvgSpeedMillis(float avgSpeedMillis) {
        this.avgSpeedMillis = avgSpeedMillis;
    }

    public float getStdSpeedMillis() {
        return stdSpeedMillis;
    }

    public void setStdSpeedMillis(float stdSpeedMillis) {
        this.stdSpeedMillis = stdSpeedMillis;
    }

    public boolean isReversable() {
        return reversable;
    }

    public void setReversable(boolean reversable) {
        this.reversable = reversable;
    }

    public long getAvgOutOfSceneTimeMillis() {
        return avgOutOfSceneTimeMillis;
    }

    public void setAvgOutOfSceneTimeMillis(long avgOutOfSceneTimeMillis) {
        this.avgOutOfSceneTimeMillis = avgOutOfSceneTimeMillis;
    }

    public float getStdOutOfSceneTimeMillis() {
        return stdOutOfSceneTimeMillis;
    }

    public void setStdOutOfSceneTimeMillis(float stdOutOfSceneTimeMillis) {
        this.stdOutOfSceneTimeMillis = stdOutOfSceneTimeMillis;
    }

    public float getSourceX1() {
        return sourceX1;
    }

    public void setSourceX1(float sourceX1) {
        this.sourceX1 = sourceX1;
    }

    public float getSourceX2() {
        return sourceX2;
    }

    public void setSourceX2(float sourceX2) {
        this.sourceX2 = sourceX2;
    }

    public float getSourceY1() {
        return sourceY1;
    }

    public void setSourceY1(float sourceY1) {
        this.sourceY1 = sourceY1;
    }

    public float getSourceY2() {
        return sourceY2;
    }

    public void setSourceY2(float sourceY2) {
        this.sourceY2 = sourceY2;
    }

    public float getTargetX1() {
        return targetX1;
    }

    public void setTargetX1(float targetX1) {
        this.targetX1 = targetX1;
    }

    public float getTargetX2() {
        return targetX2;
    }

    public void setTargetX2(float targetX2) {
        this.targetX2 = targetX2;
    }

    public float getTargetY1() {
        return targetY1;
    }

    public void setTargetY1(float targetY1) {
        this.targetY1 = targetY1;
    }

    public float getTargetY2() {
        return targetY2;
    }

    public void setTargetY2(float targetY2) {
        this.targetY2 = targetY2;
    }

    public void setSource(float x1, float x2, float y1, float y2) {
        sourceX1 = x1;
        sourceX2 = x2;
        sourceY1 = y1;
        sourceY2 = y2;
    }

    public void setSource(float x, float y) {
        sourceX1 = x;
        sourceX2 = x;
        sourceY1 = y;
        sourceY2 = y;
    }

    public void setTarget(float x1, float x2, float y1, float y2) {
        targetX1 = x1;
        targetX2 = x2;
        targetY1 = y1;
        targetY2 = y2;
    }

    public void setTarget(float x, float y) {
        targetX1 = x;
        targetX2 = x;
        targetY1 = y;
        targetY2 = y;
    }

    public float getReferenceAngleDegrees() {
        return referenceDirection.angle();
    }

    public void setReferenceAngleDegrees(float referenceAngleDegrees) {
        this.referenceDirection.setAngle(referenceAngleDegrees).nor();
    }

    public float getReferenceMaxDeviationAngleDegrees() {
        return referenceMaxDeviationAngleDegrees;
    }

    public void setReferenceMaxDeviationAngleDegrees(float referenceMaxDeviationAngleDegrees) {
        this.referenceMaxDeviationAngleDegrees = referenceMaxDeviationAngleDegrees;
    }

    public boolean isAdjustActorRotation() {
        return adjustActorRotation;
    }

    public void setAdjustActorRotation(boolean adjustActorRotation) {
        this.adjustActorRotation = adjustActorRotation;
    }

    @Override
    public long getMaxSleepTime(Actor actor) {
        if (outOfScene) {
            return outOfSceneTimeMillis - currentOutOfSceneTimeMillis;
        }

        return 0;
    }
}
