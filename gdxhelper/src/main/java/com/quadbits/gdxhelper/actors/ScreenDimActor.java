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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.Recyclable;

import javax.inject.Inject;

/**
 *
 */
public class ScreenDimActor extends BaseActor implements Disposable, Recyclable<ScreenDimActor> {
    protected Pool<ScreenDimActor> screenDimActorPool;
    private float alpha;
    private Texture dimTexture;
    private Sprite dimSprite;
    protected ShaderProgram postDrawShader;

    @Inject
    public ScreenDimActor() {
        super();

        // Create a 1x1 black pixmap and send it to the graphics card (create texture)
        Pixmap dimPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        dimPixmap.drawPixel(0, 0, Color.rgba8888(0, 0, 0, 1));
        dimTexture = new Texture(dimPixmap);
        dimPixmap.dispose();

        this.dimSprite = new Sprite(dimTexture);
        setSize(dimSprite.getWidth(), dimSprite.getHeight());
        alpha = 0;
    }

    @Override
    public void reset() {
        alpha = 0;
    }

    @Override
    public void dispose() {
        dimTexture.dispose();
    }

    @Override
    public void free() {
        screenDimActorPool.free(this);
    }

    @Override
    public void setPool(Pool<ScreenDimActor> screenDimActorPool) {
        this.screenDimActorPool = screenDimActorPool;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        dimSprite.setPosition(0, 0);
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        if (dimSprite.getWidth() != width || dimSprite.getHeight() != height) {
            dimSprite.setSize(width, height);
        }
        dimSprite.setColor(getColor());
        dimSprite.setAlpha(parentAlpha * alpha);

        // Use default shader
        batch.setShader(null);

        dimSprite.draw(batch);

        // Restore shader
        if (postDrawShader != null) {
            batch.setShader(postDrawShader);
        }
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public ShaderProgram getPostDrawShader() {
        return postDrawShader;
    }

    public void setPostDrawShader(ShaderProgram postDrawShader) {
        this.postDrawShader = postDrawShader;
    }
}
