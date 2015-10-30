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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.Recyclable;

import javax.inject.Inject;

/**
 *
 */
public class BackgroundActor extends ControllableActor implements Recyclable<BackgroundActor> {
    protected Pool<BackgroundActor> backgroundActorPool;
    protected Color topLeftColor;
    protected Color topRightColor;
    protected Color bottomLeftColor;
    protected Color bottomRightColor;

    @Inject
    protected ShapeRenderer shapeRenderer;

    @Inject
    public BackgroundActor() {
        super();

        topLeftColor = new Color();
        topRightColor = new Color();
        bottomLeftColor = new Color();
        bottomRightColor = new Color();
    }

    @Override
    public void free() {
        backgroundActorPool.free(this);
    }

    @Override
    public void setPool(Pool<BackgroundActor> backgroundActorPool) {
        this.backgroundActorPool = backgroundActorPool;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        // ------------------------------------------------------------------------
        // We are going to use a shapeRenderer -> end current batch
        // ------------------------------------------------------------------------
        batch.end();

        // ------------------------------------------------------------------------
        // Draw a rectangle with the specified colors
        // ------------------------------------------------------------------------
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer
                .rect(getX(), getY(), getWidth(), getHeight(), bottomLeftColor, bottomRightColor,
                        topRightColor, topLeftColor);
        shapeRenderer.end();

        // ------------------------------------------------------------------------
        // We are done -> begin a batch again for the next actors
        // ------------------------------------------------------------------------
        batch.begin();
    }

    public void setCornerColors(Color topLeftColor, Color topRightColor, Color bottomLeftColor,
                                Color bottomRightColor) {
        this.topLeftColor = topLeftColor;
        this.topRightColor = topRightColor;
        this.bottomLeftColor = bottomLeftColor;
        this.bottomRightColor = bottomRightColor;
    }

    public Color getTopLeftColor() {
        return topLeftColor;
    }

    public void setTopLeftColor(Color topLeftColor) {
        this.topLeftColor = topLeftColor;
    }

    public Color getTopRightColor() {
        return topRightColor;
    }

    public void setTopRightColor(Color topRightColor) {
        this.topRightColor = topRightColor;
    }

    public Color getBottomLeftColor() {
        return bottomLeftColor;
    }

    public void setBottomLeftColor(Color bottomLeftColor) {
        this.bottomLeftColor = bottomLeftColor;
    }

    public Color getBottomRightColor() {
        return bottomRightColor;
    }

    public void setBottomRightColor(Color bottomRightColor) {
        this.bottomRightColor = bottomRightColor;
    }
}
