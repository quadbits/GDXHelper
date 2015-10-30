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

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.Recyclable;
import com.quadbits.gdxhelper.utils.SpriteGrid;

import javax.inject.Inject;

/**
 *
 */
public class SpriteActor extends ControllableActor
        implements FlippableActor, Recyclable<SpriteActor> {
    protected Pool<SpriteActor> spriteActorPool;
    protected ShaderProgram preDrawShader;
    protected ShaderProgram postDrawShader;
    protected boolean useCustomShader;

    @Inject
    protected SpriteGrid spriteGrid;

    @Inject
    public SpriteActor() {
        super();
        init();
    }

    private void init() {
        setVisible(true);
        preDrawShader = null;
        postDrawShader = null;
        useCustomShader = false;
    }

    @Override
    public void reset() {
        super.reset();
        spriteGrid.reset();
        init();
    }

    @Override
    public void free() {
        spriteActorPool.free(this);
    }

    @Override
    public void setPool(Pool<SpriteActor> spriteActorPool) {
        this.spriteActorPool = spriteActorPool;
    }

    public void setTexture(String textureName) {
        spriteGrid.setTexture(textureName);
        setActorPropertiesFromSpriteGrid();
    }

    private void setActorPropertiesFromSpriteGrid() {
        setPosition(spriteGrid.getX(), spriteGrid.getY());
        //setScale(spriteGrid.getScaleX(), spriteGrid.getScaleY());
        setSize(spriteGrid.getWidth(), spriteGrid.getHeight());
        setOrigin(spriteGrid.getOriginX(), spriteGrid.getOriginY());
        setRotation(spriteGrid.getRotation());
        setColor(spriteGrid.getColor());
    }

    private void setSpriteGridPropertiesFromActor() {
        spriteGrid.setPosition(getX(), getY());
        //spriteGrid.setScale(getScaleX(), getScaleY());
        spriteGrid.setSize(getWidth(), getHeight());
        spriteGrid.setOrigin(getOriginX(), getOriginY());
        spriteGrid.setRotation(getRotation());
        spriteGrid.setColor(getColor());
    }

    public void setTextureFilter(Texture.TextureFilter minFilter, Texture.TextureFilter maxFilter) {
        spriteGrid.setTextureFilter(minFilter, maxFilter);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (useCustomShader) {
            batch.setShader(preDrawShader);
        }
        setSpriteGridPropertiesFromActor();
        spriteGrid.draw(batch, parentAlpha);
        if (useCustomShader && preDrawShader != postDrawShader) {
            batch.setShader(postDrawShader);
        }
    }

    public boolean isTileableX() {
        return spriteGrid.isTileableX();
    }

    public void setTileableX(boolean isTileableX) {
        spriteGrid.setTileableX(isTileableX);
    }

    public boolean isTileableY() {
        return spriteGrid.isTileableY();
    }

    public void setTileableY(boolean isTileableY) {
        spriteGrid.setTileableY(isTileableY);
    }

    @Override
    public boolean isFlipX() {
        return spriteGrid.isFlipX();
    }

    @Override
    public void setFlipX(boolean flipX) {
        spriteGrid.setFlipX(flipX);
    }

    @Override
    public boolean isFlipY() {
        return spriteGrid.isFlipY();
    }

    @Override
    public void setFlipY(boolean flipY) {
        spriteGrid.setFlipY(flipY);
    }

    public float getOriginalWidth() {
        return spriteGrid.getOriginalWidth();
    }

    public float getOriginalHeight() {
        return spriteGrid.getOriginalHeight();
    }

    public void scaleBy(float scale) {
        scaleBy(scale, scale);
    }

    public void scaleBy(float scaleX, float scaleY) {
        setSize(spriteGrid.getOriginalWidth() * spriteGrid.getScaleX() * scaleX,
                spriteGrid.getOriginalHeight() * spriteGrid.getScaleY() * scaleY);
    }

    @Override
    public void setScaleX(float scaleX) {
        //super.setScaleX(scaleX);
        spriteGrid.setScaleX(scaleX);
        setWidth(spriteGrid.getWidth());
    }

    @Override
    public void setScaleY(float scaleY) {
        //super.setScaleY(scaleY);
        spriteGrid.setScaleY(scaleY);
        setHeight(spriteGrid.getHeight());
    }

    @Override
    public float getScaleY() {
        return spriteGrid.getScaleY();
    }

    @Override
    public float getScaleX() {
        return spriteGrid.getScaleX();
    }

    @Override
    public void setScale(float scaleXY) {
        //super.setScale(scaleXY);
        setScale(scaleXY, scaleXY);
    }

    @Override
    public void setScale(float scaleX, float scaleY) {
        //super.setScale(scaleX, scaleY);
        spriteGrid.setScale(scaleX, scaleY);
        setSize(spriteGrid.getWidth(), spriteGrid.getHeight());
    }

    public float getMinTiledX() {
        return spriteGrid.getMinTiledX();
    }

    public void setMinTiledX(float minTiledX) {
        spriteGrid.setMinTiledX(minTiledX);
    }

    public float getMaxTiledX() {
        return spriteGrid.getMaxTiledX();
    }

    public void setMaxTiledX(float maxTiledX) {
        spriteGrid.setMaxTiledX(maxTiledX);
    }

    public float getMinTiledY() {
        return spriteGrid.getMinTiledY();
    }

    public void setMinTiledY(float minTiledY) {
        spriteGrid.setMinTiledY(minTiledY);
    }

    public float getMaxTiledY() {
        return spriteGrid.getMaxTiledY();
    }

    public void setMaxTiledY(float maxTiledY) {
        spriteGrid.setMaxTiledX(maxTiledY);
    }

    public void setTiledBoundsX(float minTiledX, float maxTiledX) {
        spriteGrid.setMinTiledX(minTiledX);
        spriteGrid.setMaxTiledX(maxTiledX);
    }

    public void setTiledBoundsY(float minTiledY, float maxTiledY) {
        spriteGrid.setMinTiledY(minTiledY);
        spriteGrid.setMaxTiledY(maxTiledY);
    }

    public ShaderProgram getPreDrawShader() {
        return preDrawShader;
    }

    public void setPreDrawShader(ShaderProgram preDrawShader) {
        this.preDrawShader = preDrawShader;
        this.useCustomShader = true;
    }

    public ShaderProgram getPostDrawShader() {
        return postDrawShader;
    }

    public void setPostDrawShader(ShaderProgram postDrawShader) {
        this.postDrawShader = postDrawShader;
        this.useCustomShader = true;
    }

    public boolean isUseCustomShader() {
        return useCustomShader;
    }

    public void setUseCustomShader(boolean useCustomShader) {
        this.useCustomShader = useCustomShader;
    }
}
