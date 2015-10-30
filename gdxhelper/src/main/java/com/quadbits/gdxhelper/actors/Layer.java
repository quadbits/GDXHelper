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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.Recyclable;

import javax.inject.Inject;

/**
 *
 */
public class Layer extends ControllableGroup implements Pool.Poolable, Recyclable<Layer> {
    protected Pool<Layer> layerPool;

    @Inject
    public Layer() {
        setTransform(false);
    }

    @Override
    public void reset() {
        super.reset();
        clear();
        setPosition(0, 0);
        setSize(0, 0);
        getColor().set(1, 1, 1, 1);
    }

    @Override
    public void free() {
        layerPool.free(this);
    }

    @Override
    public void setPool(Pool<Layer> layerPool) {
        this.layerPool = layerPool;
    }

    public void setWidthCenterHorizontally(float width, boolean autoAdjustChildren) {
        setWidth(width);
        float layerX = centerHorizontally();
        setX(layerX);
        if (autoAdjustChildren) {
            for (Actor actor : getChildren()) {
                actor.setX(actor.getX() - layerX);
            }
        }
    }

    public void setHeightCenterVertically(float height, boolean autoAdjustChildren) {
        setHeight(height);
        float layerY = centerVertically();
        setY(layerY);
        if (autoAdjustChildren) {
            for (Actor actor : getChildren()) {
                actor.setY(actor.getY() - layerY);
            }
        }
    }

    /**
     * Adjusts the layer's x and width to the minimum rectangle that contains all of its
     * children, horizontally centered.
     *
     * @see #autoAdjustHorizontally(float)
     */
    public void autoAdjustHorizontally() {
        autoAdjustHorizontally(1.f);
    }

    /**
     * Adjusts the layer's x and width to the minimum rectangle that contains all of its
     * children, horizontally centered. After this, the layer's width is multiplied by 'scale',
     * typically for adding or subtracting some margin (the layer is kept horizontally centered).
     *
     * @param scale
     *         The value that will multiply the layer's width
     */
    public void autoAdjustHorizontally(float scale) {
        // Calculate x-size respect to the middle of the screen
        float screenSemiWidth = Gdx.graphics.getWidth() / 2;
        float minX = screenSemiWidth;
        float maxX = screenSemiWidth;
        for (Actor actor : getChildren()) {
            float actorX = actor.getX();
            if (actorX < minX) {
                minX = actorX;
            }
            float actorMaxX = actorX + actor.getWidth();
            if (actorMaxX > maxX) {
                maxX = actorMaxX;
            }
        }

        // Subtract the screen's semi-width to make results 0-based
        minX -= screenSemiWidth;
        maxX -= screenSemiWidth;

        // Max shift
        float maxShiftX = Math.max(-minX, maxX);

        // Scale max shift
        maxShiftX *= scale;

        // Adjust layer's x, width, and children's x
        setWidthCenterHorizontally(2 * maxShiftX, true);
    }

    /**
     * Adjusts the layer's y and height to the minimum rectangle that contains all of its
     * children, vertically centered.
     *
     * @see #autoAdjustVertically(float)
     */
    public void autoAdjustVertically() {
        autoAdjustVertically(1.f);
    }

    /**
     * Adjusts the layer's y and height to the minimum rectangle that contains all of its
     * children, vertically centered. After this, the layer's height is multiplied by 'scale',
     * typically for adding or subtracting some margin (the layer is kept vertically centered).
     *
     * @param scale
     *         The value that will multiply the layer's width
     */
    public void autoAdjustVertically(float scale) {
        // Calculate y-size respect to the middle of the screen
        float screenSemiHeight = Gdx.graphics.getHeight() / 2;
        float minY = screenSemiHeight;
        float maxY = screenSemiHeight;
        for (Actor actor : getChildren()) {
            float actorY = actor.getY();
            if (actorY < minY) {
                minY = actorY;
            }
            float actorMaxY = actorY + actor.getHeight();
            if (actorMaxY > maxY) {
                maxY = actorMaxY;
            }
        }

        // Subtract the screen's semi-height to make results 0-based
        minY -= screenSemiHeight;
        maxY -= screenSemiHeight;

        // Max shift
        float maxShiftY = Math.max(-minY, maxY);

        // Scale max shift
        maxShiftY *= scale;

        // Adjust layer's y, height, and children's y
        setHeightCenterVertically(2 * maxShiftY, true);
    }

    public float getRelX(float relativeX) {
        return getX() + getWidth() * relativeX;
    }

    public float getRelY(float relativeY) {
        return getY() + getHeight() * relativeY;
    }

    public float centerHorizontally() {
        return 0.5f * (Gdx.graphics.getWidth() - getWidth());
    }

    public float centerVertically() {
        return 0.5f * (Gdx.graphics.getHeight() - getHeight());
    }
}
