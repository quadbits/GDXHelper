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
package com.quadbits.gdxhelper.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import java.util.HashMap;
import java.util.Set;

import javax.inject.Inject;

/**
 *
 */
public class AnimatedSpriteGrid implements Pool.Poolable {
    protected Pool<AnimatedSpriteGrid> animatedSpriteGridPool;
    protected HashMap<Object, AnimationSequence> animations;
    protected Object state;
    protected int activeFrameIndex;
    protected Frame activeFrame;
    protected float accumulatedAnimTime;
    protected boolean showEachFrameAtLeastOnce;

    @Inject
    protected Pool<AnimationSequence> animationSequencePool;

    @Inject
    protected Pool<Frame> framePool;

    @Inject
    public AnimatedSpriteGrid() {
        animations = new HashMap<Object, AnimationSequence>();
        init();
    }

    protected void init() {
        state = null;
        activeFrame = null;
        activeFrameIndex = -1;
        accumulatedAnimTime = 0;
        showEachFrameAtLeastOnce = false;
    }

    @Override
    public void reset() {
        for (AnimationSequence animation : animations.values()) {
            animation.free();
        }
        animations.clear();

        init();
    }

    public void free() {
        animatedSpriteGridPool.free(this);
    }

    public void setPool(Pool<AnimatedSpriteGrid> animatedSpriteGridPool) {
        this.animatedSpriteGridPool = animatedSpriteGridPool;
    }

    public int addFrame(Object state, String textureName, float duration, float comX, float comY) {
        Frame frame = framePool.obtain();
        frame.setTexture(textureName);
        frame.setDuration(duration);
        frame.setCenterOfMassX(comX);
        frame.setCenterOfMassY(comY);

        return addFrame(state, frame);
    }

    public int addFrame(Object state, Frame frame) {
        // Get the animation sequence associated to the state
        AnimationSequence animation = animations.get(state);

        // If there is no animation yet, create it and associate it with the state
        if (animation == null) {
            animation = animationSequencePool.obtain();
            animations.put(state, animation);
        }

        // Add the given sprite to the array and Return the index of the sprite in the array
        return animation.addFrame(frame);
    }

    public Object getState() {
        return state;
    }

    public void setState(Object state) {
        setActiveFrame(state, 0);
        setAccumulatedAnimTime(0);
    }

    public void setActiveFrame(Object state, int index) {
        activeFrame = animations.get(state).getFrame(index);
        this.state = state;
        activeFrameIndex = index;
    }

    public Frame getActiveFrame() {
        return activeFrame;
    }

    public int getActiveFrameIndex() {
        return activeFrameIndex;
    }

    public AnimationSequence getAnimationSequence(Object state) {
        return animations.get(state);
    }

    public Frame getFrame(Object state, int index) {
        return animations.get(state).getFrame(index);
    }

    public Set<Object> getAllStates() {
        return animations.keySet();
    }

    public float getAccumulatedAnimTime() {
        return accumulatedAnimTime;
    }

    public void setAccumulatedAnimTime(float accumulatedAnimTime) {
        this.accumulatedAnimTime = accumulatedAnimTime;
    }

    public Frame calculateCurrentAnimationFrame(float deltaSeconds) {
        // Get the animation corresponding to the current state
        AnimationSequence animation = getAnimationSequence(getState());

        // If the accumulated anim. time is 0, it means that the first frame has not been shown yet
        boolean currentFrameHasBeenShown = true;
        if (accumulatedAnimTime == 0) {
            currentFrameHasBeenShown = false;
        }

        // Add the delta time
        accumulatedAnimTime += deltaSeconds;

        // If looping is enabled, perform a modulo operation to go back again
        if (animation.isLoopAnimation()) {
            accumulatedAnimTime = accumulatedAnimTime % animation.getDuration();
        }

        // Search for the frame corresponding to the amount of time passed
        boolean animationFinished = true;
        for (int i = activeFrameIndex; i < animation.getSize(); ) {
            Frame frame = animation.getFrame(i);

            // If the current frame corresponds to the time passed, return it
            float frameEndTime = frame.getEndTimeFromAnimStart();
            float frameDuration = frame.getDuration();
            if (frameEndTime == -1 || (accumulatedAnimTime <= frameEndTime &&
                    accumulatedAnimTime >= frameEndTime - frameDuration)) {
                activeFrameIndex = i;
                activeFrame = frame;
                animationFinished = false;
                break;
            }

            // The current frame does not correspond to the time passed,
            // but it has not been shown and each frame should be drawn at least once: return it
            else if (showEachFrameAtLeastOnce && !currentFrameHasBeenShown) {
                activeFrameIndex = i;
                activeFrame = frame;
                animationFinished = false;
                break;
            }

            // The next frame has not been shown
            currentFrameHasBeenShown = false;

            // If in loop mode, ensure i goes back again to the begin
            if (animation.isLoopAnimation() && i == animation.getSize() - 1) {
                i = 0;
            } else {
                i++;
            }
        }

        if (animationFinished) {
            activeFrameIndex = -1;
            activeFrame = null;
        }

        return activeFrame;
    }

    public boolean isShowEachFrameAtLeastOnce() {
        return showEachFrameAtLeastOnce;
    }

    public void setShowEachFrameAtLeastOnce(boolean showEachFrameAtLeastOnce) {
        this.showEachFrameAtLeastOnce = showEachFrameAtLeastOnce;
    }

    public static class AnimationSequence implements Pool.Poolable {
        protected Pool<AnimationSequence> animationSequencePool;
        protected Array<Frame> frames;
        protected boolean loopAnimation;
        protected boolean flipX;
        protected boolean flipY;

        @Inject
        protected Pool<Frame> framePool;

        @Inject
        public AnimationSequence() {
            frames = new Array<Frame>();
            init();
        }

        protected void init() {
            loopAnimation = false;
            flipX = false;
            flipY = false;
        }

        @Override
        public void reset() {
            for (Frame frame : frames) {
                frame.free();
            }
            frames.clear();
            init();
        }

        public void free() {
            animationSequencePool.free(this);
        }

        public void setPool(Pool<AnimationSequence> animationSequencePool) {
            this.animationSequencePool = animationSequencePool;
        }

        public int addFrame(String textureName, float duration, float comX, float comY) {
            Frame frame = framePool.obtain();
            frame.setTexture(textureName);
            frame.setDuration(duration);
            frame.setCenterOfMassX(comX);
            frame.setCenterOfMassY(comY);

            return addFrame(frame);
        }

        public int addFrame(Frame frame) {
            Frame previousFrame = frames.size == 0 ? null : frames.get(frames.size - 1);
            float endTimeFromAnimStart = previousFrame == null ?
                    frame.duration :
                    previousFrame.getEndTimeFromAnimStart() + frame.duration;
            frame.setEndTimeFromAnimStart(endTimeFromAnimStart);

            frames.add(frame);
            return frames.size - 1;
        }

        public Frame getFrame(int index) {
            return frames.get(index);
        }

        public int getSize() {
            return frames.size;
        }

        public boolean isLoopAnimation() {
            return loopAnimation;
        }

        public void setLoopAnimation(boolean loopAnimation) {
            this.loopAnimation = loopAnimation;
        }

        public float getDuration() {
            if (frames.size == 0) {
                return 0;
            }
            return frames.get(frames.size - 1).getEndTimeFromAnimStart();
        }

        public boolean isFlipX() {
            return flipX;
        }

        public void setFlipX(boolean flipX) {
            if (flipX != this.flipX) {
                for (Frame frame : frames) {
                    frame.setFlipX(!frame.isFlipX());
                }
            }
            this.flipX = flipX;
        }

        public boolean isFlipY() {
            return flipY;
        }

        public void setFlipY(boolean flipY) {
            if (flipY != this.flipY) {
                for (Frame frame : frames) {
                    frame.setFlipY(!frame.isFlipY());
                }
            }
            this.flipY = flipY;
        }
    }

    public static class Frame implements Pool.Poolable {
        protected Pool<Frame> framePool;
        protected float duration;
        protected float endTimeFromAnimStart;
        protected float centerOfMassX;
        protected float centerOfMassY;

        @Inject
        protected SpriteGrid sprite;

        @Inject
        public Frame() {
            init();
        }

        protected void init() {
            duration = 0;
            endTimeFromAnimStart = 0;
            centerOfMassX = 0;
            centerOfMassY = 0;
        }

        @Override
        public void reset() {
            sprite.reset();
            init();
        }

        public void free() {
            framePool.free(this);
        }

        public void setPool(Pool<Frame> framePool) {
            this.framePool = framePool;
        }

        public void setTexture(String textureName) {
            sprite.setTexture(textureName);
        }

        public void setDuration(float duration) {
            this.duration = duration;
        }

        public void setEndTimeFromAnimStart(float endTimeFromAnimStart) {
            this.endTimeFromAnimStart = endTimeFromAnimStart;
        }

        public void setCenterOfMassX(float centerOfMassX) {
            this.centerOfMassX = centerOfMassX;
        }

        public void setCenterOfMassY(float centerOfMassY) {
            this.centerOfMassY = centerOfMassY;
        }

        public SpriteGrid getSprite() {
            return sprite;
        }

        public float getDuration() {
            return duration;
        }

        public float getEndTimeFromAnimStart() {
            return endTimeFromAnimStart;
        }

        public float getCenterOfMassX() {
            return centerOfMassX;
        }

        public float getCenterOfMassY() {
            return centerOfMassY;
        }

        public void setFlipX(boolean flipX) {
            if (flipX != sprite.isFlipX()) {
                centerOfMassX = 1 - centerOfMassX;
            }
            sprite.setFlipX(flipX);
        }

        public boolean isFlipX() {
            return sprite.isFlipX();
        }

        public void setFlipY(boolean flipY) {
            if (flipY != sprite.isFlipY()) {
                centerOfMassY = 1 - centerOfMassY;
            }
            sprite.setFlipY(flipY);
        }

        public boolean isFlipY() {
            return sprite.isFlipY();
        }
    }

}
