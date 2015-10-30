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
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.AnimatedSpriteGrid;
import com.quadbits.gdxhelper.utils.Recyclable;
import com.quadbits.gdxhelper.utils.SpriteGrid;

import javax.inject.Inject;

/**
 *
 */
public class AnimatedSpriteActor extends ControllableActor
        implements FlippableActor, Recyclable<AnimatedSpriteActor> {
    protected Pool<AnimatedSpriteActor> animatedSpriteActorPool;

    @Inject
    protected AnimatedSpriteGrid animatedSpriteGrid;

    @Inject
    public AnimatedSpriteActor() {
        super();
        init();
    }

    private void init() {
        setPosition(0, 0);
        setSize(0, 0);
        getColor().set(1, 1, 1, 1);
        setName(null);
        setVisible(true);
    }

    @Override
    public void reset() {
        super.reset();
        animatedSpriteGrid.reset();
        clear();
        init();
    }

    @Override
    public void free() {
        animatedSpriteActorPool.free(this);
    }

    @Override
    public void setPool(Pool<AnimatedSpriteActor> animatedSpriteActorPool) {
        this.animatedSpriteActorPool = animatedSpriteActorPool;
    }

    public int addFrame(Object state, String textureName, float duration, float comX, float comY) {
        return animatedSpriteGrid.addFrame(state, textureName, duration, comX, comY);
    }

    public int addFrame(Object state, AnimatedSpriteGrid.Frame frame) {
        return animatedSpriteGrid.addFrame(state, frame);
    }

    public Object getState() {
        return animatedSpriteGrid.getState();
    }

    public void setState(Object state) {
        animatedSpriteGrid.setState(state);
        setSize(getActiveFrame().getSprite().getWidth(), getActiveFrame().getSprite().getHeight());
    }

    public void setActiveFrame(Object state, int index) {
        animatedSpriteGrid.setActiveFrame(state, index);
        setSize(getActiveFrame().getSprite().getWidth(), getActiveFrame().getSprite().getHeight());
    }

    public AnimatedSpriteGrid.Frame getActiveFrame() {
        return animatedSpriteGrid.getActiveFrame();
    }

    public int getActiveFrameIndex() {
        return animatedSpriteGrid.getActiveFrameIndex();
    }

    public AnimatedSpriteGrid.Frame getFrame(Object state, int index) {
        return animatedSpriteGrid.getFrame(state, index);
    }

    public AnimatedSpriteGrid.AnimationSequence getAnimationSequence(Object state) {
        return animatedSpriteGrid.getAnimationSequence(state);
    }

    public AnimatedSpriteGrid.Frame calculateCurrentAnimationFrame(float deltaSeconds) {
        AnimatedSpriteGrid.Frame frame =
                animatedSpriteGrid.calculateCurrentAnimationFrame(deltaSeconds);
        if (frame != null) {
            setSize(frame.getSprite().getWidth(), frame.getSprite().getHeight());
        }
        return frame;
    }

    public boolean isShowEachFrameAtLeastOnce() {
        return animatedSpriteGrid.isShowEachFrameAtLeastOnce();
    }

    public void setShowEachFrameAtLeastOnce(boolean showEachFrameAtLeastOnce) {
        animatedSpriteGrid.setShowEachFrameAtLeastOnce(showEachFrameAtLeastOnce);
    }

    private void setActorPropertiesFromSpriteGrid() {
        AnimatedSpriteGrid.Frame activeFrame = getActiveFrame();
        SpriteGrid sprite = activeFrame.getSprite();
        float shiftX = activeFrame.getCenterOfMassX() * sprite.getWidth() -
                (isFlipX() ? 1f : 0f) * sprite.getWidth();
        float shiftY = activeFrame.getCenterOfMassY() * sprite.getHeight() -
                (isFlipY() ? 1f : 0f) * sprite.getHeight();
        setPosition(sprite.getX() + shiftX, sprite.getY() + shiftY);
        setScale(sprite.getScaleX(), sprite.getScaleY());
        setSize(sprite.getWidth(), sprite.getHeight());
        setOrigin(sprite.getOriginX(), sprite.getOriginY());
        setRotation(sprite.getRotation());
        setColor(sprite.getColor());
    }

    private void setSpriteGridPropertiesFromActor() {
        AnimatedSpriteGrid.Frame activeFrame = getActiveFrame();
        SpriteGrid sprite = activeFrame.getSprite();
        float shiftX = activeFrame.getCenterOfMassX() * sprite.getWidth() -
                (isFlipX() ? 1f : 0f) * sprite.getWidth();
        float shiftY = activeFrame.getCenterOfMassY() * sprite.getHeight() -
                (isFlipY() ? 1f : 0f) * sprite.getHeight();
        sprite.setPosition(getX() - shiftX, getY() - shiftY);
        sprite.setScale(getScaleX(), getScaleY());
        sprite.setSize(getWidth(), getHeight());
        sprite.setOrigin(getOriginX(), getOriginY());
        sprite.setRotation(getRotation());
        sprite.setColor(getColor());
    }

    public void setTextureFilter(Object state, int index, Texture.TextureFilter minFilter,
                                 Texture.TextureFilter maxFilter) {
        getFrame(state, index).getSprite().setTextureFilter(minFilter, maxFilter);
    }

    public void setTextureFilter(Texture.TextureFilter minFilter, Texture.TextureFilter maxFilter) {
        for (Object state : animatedSpriteGrid.getAllStates()) {
            AnimatedSpriteGrid.AnimationSequence animation =
                    animatedSpriteGrid.getAnimationSequence(state);
            for (int i = 0; i < animation.getSize(); i++) {
                animation.getFrame(i).getSprite().setTextureFilter(minFilter, maxFilter);
            }
        }
    }

    @Override
    public void act(float deltaSeconds) {
        super.act(deltaSeconds);
        calculateCurrentAnimationFrame(deltaSeconds);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        setSpriteGridPropertiesFromActor();
        getActiveFrame().getSprite().draw(batch, parentAlpha);
    }

    public boolean isTileableX() {
        return getActiveFrame().getSprite().isTileableX();
    }

    public boolean isTileableX(Object state, int index) {
        return getFrame(state, index).getSprite().isTileableX();
    }

    public void setTileableX(boolean tileableX) {
        for (Object state : animatedSpriteGrid.getAllStates()) {
            AnimatedSpriteGrid.AnimationSequence animation =
                    animatedSpriteGrid.getAnimationSequence(state);
            for (int i = 0; i < animation.getSize(); i++) {
                animation.getFrame(i).getSprite().setTileableX(tileableX);
            }
        }
    }

    public void setTileableX(Object state, int index, boolean tileableX) {
        getFrame(state, index).getSprite().setTileableX(tileableX);
    }

    public boolean isTileableY() {
        return getActiveFrame().getSprite().isTileableY();
    }

    public boolean isTileableY(Object state, int index) {
        return getFrame(state, index).getSprite().isTileableY();
    }

    public void setTileableY(boolean tileableY) {
        for (Object state : animatedSpriteGrid.getAllStates()) {
            AnimatedSpriteGrid.AnimationSequence animation =
                    animatedSpriteGrid.getAnimationSequence(state);
            for (int i = 0; i < animation.getSize(); i++) {
                animation.getFrame(i).getSprite().setTileableY(tileableY);
            }
        }
    }

    public void setTileableY(Object state, int index, boolean tileableY) {
        getFrame(state, index).getSprite().setTileableY(tileableY);
    }

    @Override
    public boolean isFlipX() {
        return getAnimationSequence(getState()).isFlipX();
    }

    public boolean isFlipX(Object state, int index) {
        return getFrame(state, index).isFlipX();
    }

    @Override
    public void setFlipX(boolean flipX) {
        for (Object state : animatedSpriteGrid.getAllStates()) {
            AnimatedSpriteGrid.AnimationSequence animation =
                    animatedSpriteGrid.getAnimationSequence(state);
            animation.setFlipX(flipX);
        }
    }

    public void setFlipX(Object state, int index, boolean flipX) {
        AnimatedSpriteGrid.Frame frame = getFrame(state, index);
        frame.getSprite().setFlipX(flipX);
    }

    @Override
    public boolean isFlipY() {
        return getAnimationSequence(getState()).isFlipY();
    }

    public boolean isFlipY(Object state, int index) {
        return getFrame(state, index).isFlipY();
    }

    @Override
    public void setFlipY(boolean flipY) {
        for (Object state : animatedSpriteGrid.getAllStates()) {
            AnimatedSpriteGrid.AnimationSequence animation =
                    animatedSpriteGrid.getAnimationSequence(state);
            animation.setFlipY(flipY);
        }
    }

    public void setFlipY(Object state, int index, boolean flipY) {
        AnimatedSpriteGrid.Frame frame = getFrame(state, index);
        frame.getSprite().setFlipY(flipY);
    }

    public float getOriginalWidth() {
        return getActiveFrame().getSprite().getOriginalWidth();
    }

    public float getOriginalHeight() {
        return getActiveFrame().getSprite().getOriginalHeight();
    }

    public void scaleBy(float scale) {
        scaleBy(scale, scale);
    }

    public void scaleBy(float scaleX, float scaleY) {
        for (Object state : animatedSpriteGrid.getAllStates()) {
            AnimatedSpriteGrid.AnimationSequence animation =
                    animatedSpriteGrid.getAnimationSequence(state);
            for (int i = 0; i < animation.getSize(); i++) {
                animation.getFrame(i).getSprite().scaleBy(scaleX, scaleY);
            }
        }
        AnimatedSpriteGrid.Frame frame = getActiveFrame();
        if (frame != null) {
            setSize(frame.getSprite().getWidth(), frame.getSprite().getHeight());
        }
    }
}
