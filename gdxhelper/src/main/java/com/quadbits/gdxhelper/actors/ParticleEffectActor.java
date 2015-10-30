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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.Recyclable;

import javax.inject.Inject;

/**
 *
 */
public class ParticleEffectActor extends ControllableActor
        implements Recyclable<ParticleEffectActor> {
    protected Pool<ParticleEffectActor> pool;
    protected ParticleEffectPool.PooledEffect effect;
    protected ShaderProgram preDrawShader;
    protected ShaderProgram postDrawShader;
    protected final BoundingBox screenBoundingBox;

    @Inject
    public ParticleEffectActor() {
        screenBoundingBox = new BoundingBox();
    }

    @Override
    public void reset() {
        super.reset();
        setPosition(0, 0);
        if (effect != null) {
            effect.free();
            effect = null;
        }
    }

    @Override
    public void free() {
        pool.free(this);
    }

    @Override
    public void setPool(Pool<ParticleEffectActor> pool) {
        this.pool = pool;
    }

    @Override
    public void scaleBy(float scale) {
        if (effect != null) {
            effect.scaleEffect(scale);
        }
    }

    @Override
    protected void positionChanged() {
        super.positionChanged();

        if (this.effect != null) {
            this.effect.setPosition(getX(), getY());
        }
    }

    public void setEffect(ParticleEffectPool.PooledEffect effect) {
        if (this.effect != null && this.effect != effect) {
            this.effect.free();
        }
        this.effect = effect;
        if (this.effect != null) {
            this.effect.setPosition(getX(), getY());
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (effect != null) {
            effect.update(delta);
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (effect != null) {
            effect.setPosition(getX(), getY());
            if (isVisible() && !effect.isComplete() && isWithinScreenBounds()) {
                if (preDrawShader != null) {
                    batch.setShader(preDrawShader);
                }
                effect.draw(batch);
                if (preDrawShader != postDrawShader) {
                    batch.setShader(postDrawShader);
                }
            }
        }
    }

    protected boolean isWithinScreenBounds() {
        if (effect == null) {
            return false;
        }

        BoundingBox boundingBox = effect.getBoundingBox();
        boundingBox.min.z = 0; // HACK! to make the bounding box valid
        boundingBox.max.z = 1; // HACK! to make the bounding box valid

        // Set stage's viewport coordinates
        float screenWidth = getStage().getViewport().getScreenWidth();
        float screenHeight = getStage().getViewport().getScreenHeight();
        screenBoundingBox.clr().ext(0, 0, 0)
                .ext(screenWidth, screenHeight, 1); // HACK! z = 1 to make bounding box valid

        return boundingBox.intersects(screenBoundingBox);
    }

    @Override
    public void drawDebug(ShapeRenderer shapes) {
        super.drawDebug(shapes);

        if (!this.getDebug() || effect == null)
            return;
        shapes.set(ShapeRenderer.ShapeType.Line);
        shapes.setColor(getStage().getDebugColor());
        BoundingBox boundingBox = effect.getBoundingBox();
        float centerX = boundingBox.getCenterX();
        float centerY = boundingBox.getCenterY();
        float width = boundingBox.getWidth();
        float height = boundingBox.getHeight();
        shapes.rect(centerX - width / 2, centerY - height / 2, width, height);
    }

    @Override
    public long getMaxSleepTime() {
        if (isVisible() && !effect.isComplete() && isWithinScreenBounds()) {
            return 0;
        }

        return super.getMaxSleepTime();
    }

    public ShaderProgram getPreDrawShader() {
        return preDrawShader;
    }

    public void setPreDrawShader(ShaderProgram preDrawShader) {
        this.preDrawShader = preDrawShader;
    }

    public ShaderProgram getPostDrawShader() {
        return postDrawShader;
    }

    public void setPostDrawShader(ShaderProgram postDrawShader) {
        this.postDrawShader = postDrawShader;
    }
}
