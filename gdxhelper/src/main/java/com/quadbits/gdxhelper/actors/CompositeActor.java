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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.Recyclable;

import javax.inject.Inject;

/**
 *
 */
public class CompositeActor extends ControllableGroup
        implements FlippableActor, Recyclable<CompositeActor> {
    protected Pool<CompositeActor> compositeActorPool;
    protected boolean dirty;
    protected boolean flipX;
    protected boolean flipY;

    @Inject
    public CompositeActor() {
        super();
        init();
    }

    private void init() {
        setPosition(0, 0);
        setSize(0, 0);
        getColor().set(1, 1, 1, 1);
        setName(null);
        setVisible(true);
        dirty = false;
        flipX = false;
        flipY = false;
    }

    @Override
    public void reset() {
        super.reset();
        clear();
        init();
    }

    @Override
    public void free() {
        compositeActorPool.free(this);
    }

    @Override
    public void setPool(Pool<CompositeActor> compositeActorPool) {
        this.compositeActorPool = compositeActorPool;
    }

    @Override
    protected void childrenChanged() {
        super.childrenChanged();
        dirty = true;
    }

    public void autoAdjustSize() {
        // No children
        if (getChildren().size == 0) {
            setSize(0, 0);
            dirty = false;
            return;
        }

        float minX = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxY = -Float.MAX_VALUE;
        for (Actor actor : getChildren()) {
            // X
            float actorX = actor.getX();
            if (actorX < minX) {
                minX = actorX;
            }
            float actorMaxX = actorX + actor.getWidth();
            if (actorMaxX > maxX) {
                maxX = actorMaxX;
            }

            // Y
            float actorY = actor.getY();
            if (actorY < minY) {
                minY = actorY;
            }
            float actorMaxY = actorY + actor.getHeight();
            if (actorMaxY > maxY) {
                maxY = actorMaxY;
            }
        }

        setSize(maxX - minX, maxY - minY);
        dirty = false;
    }

    @Override
    public float getWidth() {
        if (dirty) {
            autoAdjustSize();
        }
        return super.getWidth();
    }

    @Override
    public float getHeight() {
        if (dirty) {
            autoAdjustSize();
        }
        return super.getHeight();
    }

    @Override
    public boolean isFlipX() {
        return flipX;
    }

    @Override
    public void setFlipX(boolean flipX) {
        if (flipX == this.flipX) {
            return;
        }

        this.flipX = flipX;

        float compositeMidPoint = getWidth() / 2f;

        Actor[] children = getChildren().begin();
        for (Actor child : children) {
            if (child instanceof FlippableActor) {
                FlippableActor flippableActor = (FlippableActor) child;
                float childX = child.getX();
                float childMidPoint = childX + child.getWidth() / 2f;
                float shift = (compositeMidPoint - childMidPoint) * 2;
                child.setX(childX + shift);

                // IMPORTANT!: invert flip on children, instead of setting whatever value
                // the 'flipX' paramenter is
                flippableActor.setFlipX(!flippableActor.isFlipX());
            }
        }
        getChildren().end();
    }

    @Override
    public boolean isFlipY() {
        return flipY;
    }

    @Override
    public void setFlipY(boolean flipY) {
        if (flipY == this.flipY) {
            return;
        }

        this.flipY = flipY;

        float compositeMidPoint = getHeight() / 2f;

        Actor[] children = getChildren().begin();
        for (Actor child : children) {
            if (child instanceof FlippableActor) {
                FlippableActor flippableActor = (FlippableActor) child;
                float childY = child.getY();
                float childMidPoint = childY + child.getHeight() / 2f;
                float shift = (compositeMidPoint - childMidPoint) * 2;
                child.setY(childY + shift);

                // IMPORTANT!: invert flip on children, instead of setting whatever value
                // the 'flipY' paramenter is
                flippableActor.setFlipY(!flippableActor.isFlipY());
            }
        }
        getChildren().end();
    }
}
