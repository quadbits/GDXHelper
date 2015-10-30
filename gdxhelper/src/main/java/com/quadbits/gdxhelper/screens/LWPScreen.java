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
package com.quadbits.gdxhelper.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.LWPGame;
import com.quadbits.gdxhelper.LWPStage;
import com.quadbits.gdxhelper.actors.ScreenDimActor;
import com.quadbits.gdxhelper.utils.NonContinuousRendering;
import com.quadbits.gdxhelper.utils.TextureAtlasProxy;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 *
 */
public abstract class LWPScreen implements Screen, InputProcessor, GestureDetector.GestureListener {
    // Game
    protected final LWPGame game;

    // Assets
    protected float assetsSize;
    protected String textureAtlasResString;
    protected float assetsScaleAbsolute;
    protected float assetsScaleRelative;

    // Input processing
    protected InputMultiplexer inputMultiplexer;
    protected GestureDetector gestureDetector;
    protected boolean panningEnabled;
    protected boolean flingEnabled;
    protected float flingVelocityX;
    protected float flingVelocityY;
    protected float flingDampFactor;

    // Scroll
    private float scrollX;
    private float scrollY;
    private ArrayList<ScrollChangeListener> scrollXChangeListeners;
    private ArrayList<ScrollChangeListener> scrollYChangeListeners;

    // Rendering
    private ScheduledFuture<?> lastRunnable;
    private final Runnable requestRenderingRunnable = new Runnable() {
        @Override
        public void run() {
            Gdx.graphics.requestRendering();
        }
    };
    private boolean continuousRendering;
    private long renderCount;
    protected long maxSleepTimeMillis;
    protected float maxDeltaTime;

    // Stage
    protected ScreenDimActor screenDimActor;

    // Shaders
    protected ShaderProgram etc1Shader;
    protected ShaderProgram etc1aShader;
    protected DefaultShader defaultShader;

    // Injectable fields

    @Inject
    protected AssetManager assetManager;

    @Inject
    protected ScheduledThreadPoolExecutor stpe;

    @Inject
    protected LWPStage stage;

    @Inject
    protected TextureAtlasProxy textureAtlasProxy;

    @Inject
    protected Pool<ScreenDimActor> screenDimActorPool;

    // Constants
    public static final long MIN_SLEEP_TIME = 500;
    public static final float DEFAULT_FLING_DAMP_FACTOR = 0.95f;
    public static final float DEFAULT_FLING_MIN_VELOCITY = 30;
    public static final float DEFAULT_MAX_DELTA_TIME_MILLIS = Float.MAX_VALUE;

    public static interface ScrollChangeListener {
        public void scrollChanged(float scroll);
    }

    public static enum DefaultShader {NONE, ETC1, ETC1A}

    public LWPScreen(LWPGame game) {
        // Game
        this.game = game;

        // Input processing
        inputMultiplexer = new InputMultiplexer();
        gestureDetector = new GestureDetector(this);
        gestureDetector.setTapSquareSize(32 * Gdx.graphics.getDensity());

        panningEnabled = game.isPanningEnabled();
        flingEnabled = game.isFlingEnabled();
        flingVelocityX = 0;
        flingVelocityY = 0;
        flingDampFactor = DEFAULT_FLING_DAMP_FACTOR;

        // Rendering
        renderCount = 0;
        maxSleepTimeMillis = 0;
        maxDeltaTime = DEFAULT_MAX_DELTA_TIME_MILLIS;
        continuousRendering = false;

        // Scroll
        scrollXChangeListeners = new ArrayList<ScrollChangeListener>();
        scrollYChangeListeners = new ArrayList<ScrollChangeListener>();
        scrollX = 0.5f;
        scrollY = 0.5f;

        // Shaders
        etc1Shader = null;
        etc1aShader = null;
        defaultShader = DefaultShader.NONE;
    }

    @Override
    public void show() {
        // Set input processors
        inputMultiplexer.clear();
        inputMultiplexer.addProcessor(gestureDetector);
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(inputMultiplexer);

        // Set non-continuous rendering
        Gdx.graphics.setContinuousRendering(false);
        Gdx.graphics.requestRendering();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);

        float assetsScaleAbsolute = getAssetsScaleAbsolute(width, height);
        if (assetsScaleAbsolute != this.assetsScaleAbsolute) {
            this.assetsScaleAbsolute = assetsScaleAbsolute;
            assetsSize = getAssetsSizeFromScale();
            assetsScaleRelative = assetsScaleAbsolute / assetsSize;
            String textureAtlasResString = getTextureAtlasResStringFromScale();
            if (!textureAtlasResString.equals(this.textureAtlasResString)) {
                loadAssets(textureAtlasResString);
                this.textureAtlasResString = textureAtlasResString;
            }
            clearStage();
            createStage();
            scaleActors(this.assetsScaleAbsolute, this.assetsScaleRelative);
            layoutActors();
        }

    }

    protected abstract float getAssetsScaleAbsolute(float width, float height);

    protected float getAssetsSizeFromScale() {

        // XXXHDPI
        if (assetsScaleAbsolute > LWPGame.XXHDPI_SCALE) {
            return LWPGame.XXXHDPI_SCALE;
        }

        // XXHDPI
        else if (assetsScaleAbsolute > LWPGame.XHDPI_SCALE) {
            return LWPGame.XXHDPI_SCALE;
        }

        // XHDPI
        else if (assetsScaleAbsolute > LWPGame.HDPI_SCALE) {
            return LWPGame.XHDPI_SCALE;
        }

        // HDPI
        else if (assetsScaleAbsolute > LWPGame.MDPI_SCALE) {
            return LWPGame.HDPI_SCALE;
        }

        // MDPI
        return LWPGame.MDPI_SCALE;
    }

    protected String getTextureAtlasResStringFromScale() {
        String textureAtlasResString;

        // XXXHDPI
        if (assetsScaleAbsolute > LWPGame.XXHDPI_SCALE) {
            textureAtlasResString = getTextureAtlasResStringXXXHDPI();
        }

        // XXHDPI
        else if (assetsScaleAbsolute > LWPGame.XHDPI_SCALE) {
            textureAtlasResString = getTextureAtlasResStringXXHDPI();
        }

        // XHDPI
        else if (assetsScaleAbsolute > LWPGame.HDPI_SCALE) {
            textureAtlasResString = getTextureAtlasResStringXHDPI();
        }

        // HDPI
        else if (assetsScaleAbsolute > LWPGame.MDPI_SCALE) {
            textureAtlasResString = getTextureAtlasResStringHDPI();
        }

        // MDPI
        else {
            textureAtlasResString = getTextureAtlasResStringMDPI();
        }

        return textureAtlasResString;
    }

    protected abstract String getTextureAtlasResStringXXXHDPI();

    protected abstract String getTextureAtlasResStringXXHDPI();

    protected abstract String getTextureAtlasResStringXHDPI();

    protected abstract String getTextureAtlasResStringHDPI();

    protected abstract String getTextureAtlasResStringMDPI();

    protected void loadAssets(String textureAtlasResString) {
        if (this.textureAtlasResString != null) {
            assetManager.unload(this.textureAtlasResString);
        }
        assetManager.load(textureAtlasResString, TextureAtlas.class);
        assetManager.finishLoading();
        textureAtlasProxy.set(assetManager.get(textureAtlasResString, TextureAtlas.class));
    }

    public void clearStage() {
        if (screenDimActor != null) {
            screenDimActor.free();
        }
        stage.clear();
    }

    protected void createStage() {
        // A screen-dim actor for dimming the whole scene
        screenDimActor = screenDimActorPool.obtain();
        screenDimActor.setName("dimActor");
        stage.getRoot().addActor(screenDimActor);
        screenDimActor.setAlpha(game.getDim());
        if (defaultShader == DefaultShader.ETC1A) {
            screenDimActor.setPostDrawShader(etc1aShader);
        } else if (defaultShader == DefaultShader.ETC1) {
            screenDimActor.setPostDrawShader(etc1Shader);
        }
    }

    protected abstract void scaleActors(float assetsScaleAbsolute, float assetsScaleRelative);

    protected abstract void layoutActors();

    @Override
    public void render(float deltaTime) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Clear pending tasks
        stpe.remove((Runnable) lastRunnable);

        // Are we flinging?
        if (flingEnabled && (flingVelocityX != 0 || flingVelocityY != 0)) {
            flinging(deltaTime);
        }

        // Clamp delta time
        if (deltaTime > maxDeltaTime) {
            deltaTime = maxDeltaTime;
        }

        // Stage: update and draw
        stage.act(deltaTime);
        stage.draw();

        //Gdx.app.log("LWPScreen", String.format("Assets size = %s, scale = %f",
        //        LWPGame.scaleToString(assetsSize), assetsScaleRelative));

        // NORMAL mode: update maxSleepTimeMillis
        if (!isContinuousRendering()) {
            if (flingEnabled && (flingVelocityX != 0 || flingVelocityY != 0)) {
                maxSleepTimeMillis = 0;
            } else {
                maxSleepTimeMillis = getMaxSleepTimeFromGroup(stage.getRoot());
            }

            if (maxSleepTimeMillis < MIN_SLEEP_TIME) {
                if (!Gdx.graphics.isContinuousRendering()) {
                    Gdx.graphics.setContinuousRendering(true);
                }
            } else {
                if (Gdx.graphics.isContinuousRendering()) {
                    Gdx.graphics.setContinuousRendering(false);
                }

                // Log
                //            Gdx.app.log("LWPScreen",
                //                    String.format("render %d - next render in %d milliseconds",
                //                            renderCount, maxSleepTimeMillis)
                //            );
                renderCount++;

                // Schedule a new render with a 1 second delay
                lastRunnable = stpe.schedule(requestRenderingRunnable, maxSleepTimeMillis,
                        TimeUnit.MILLISECONDS);
            }
        }

        // FAST_FORWARD mode: request an immediate rendering
        else {
            Gdx.graphics.requestRendering();
        }
    }

    protected void flinging(float deltaTime) {
        // Update deltaX/deltaY based on fling velocity
        float deltaX = this.flingVelocityX * deltaTime;
        float deltaY = this.flingVelocityY * deltaTime;

        // Update scroll using deltaX/deltaY
        setScrollX(scrollX + deltaX / (2 * Gdx.graphics.getWidth()));
        setScrollY(scrollY + deltaY / (2 * Gdx.graphics.getHeight()));

        // Damp velocityX
        if (scrollX == 0 || scrollX == 1) {
            flingVelocityX = 0;
        } else {
            flingVelocityX =
                    dampFlingVelocity(flingVelocityX, flingDampFactor, DEFAULT_FLING_MIN_VELOCITY);
        }

        // Damp velocityY
        if (scrollY == 0 || scrollY == 1) {
            flingVelocityY = 0;
        } else {
            flingVelocityY =
                    dampFlingVelocity(flingVelocityY, flingDampFactor, DEFAULT_FLING_MIN_VELOCITY);
        }
    }

    protected float dampFlingVelocity(float flingVelocity, float flingDampFactor,
                                      float minVelocity) {
        flingVelocity *= flingDampFactor;

        if (Math.abs(flingVelocity) < minVelocity) {
            return 0;
        }

        return flingVelocity;
    }

    public boolean isContinuousRendering() {
        return continuousRendering;
    }

    public void setContinuousRendering(boolean continuousRendering) {
        this.continuousRendering = continuousRendering;
    }

    private long getMaxSleepTimeFromGroup(Group group) {
        long maxSleepTimeMillis = Long.MAX_VALUE;
        for (Actor actor : group.getChildren()) {
            if (!(actor instanceof NonContinuousRendering || actor instanceof Group)) {
                continue;
            }

            long actorMaxSleepTime;
            if (actor instanceof Group) {
                actorMaxSleepTime = getMaxSleepTimeFromGroup((Group) actor);
            } else {
                actorMaxSleepTime = ((NonContinuousRendering) actor).getMaxSleepTime();
            }
            if (actorMaxSleepTime < maxSleepTimeMillis) {
                maxSleepTimeMillis = actorMaxSleepTime;
            }
        }

        return maxSleepTimeMillis;
    }

    @Override
    public void hide() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        if (textureAtlasResString != null) {
            assetManager.unload(textureAtlasResString);
        }

        stpe.shutdown();
        stage.dispose();
    }

    public void setPanningEnabled(boolean panningEnabled) {
        this.panningEnabled = panningEnabled;
    }

    public boolean isPanningEnabled() {
        return panningEnabled;
    }

    public boolean isFlingEnabled() {
        return flingEnabled;
    }

    public void setFlingEnabled(boolean flingEnabled) {
        this.flingEnabled = flingEnabled;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        if (this.flingEnabled) {
            flingVelocityX = 0;
            flingVelocityY = 0;
        }
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        if (this.flingEnabled) {
            this.flingVelocityX = velocityX;
            this.flingVelocityY = velocityY;
            if (!Gdx.graphics.isContinuousRendering()) {
                Gdx.graphics.setContinuousRendering(true);
            }
            Gdx.graphics.requestRendering();
            return true;
        }

        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (panningEnabled) {
            setScrollX(scrollX + deltaX / (2 * Gdx.graphics.getWidth()));
            setScrollY(scrollY + deltaY / (2 * Gdx.graphics.getHeight()));
            return true;
        }

        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1,
                         Vector2 pointer2) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        continuousRendering = true;
        Gdx.graphics.setContinuousRendering(true);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        continuousRendering = false;
        Gdx.graphics.setContinuousRendering(false);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    public LWPStage getStage() {
        return stage;
    }

    public void setDim(float dim) {
        screenDimActor.setAlpha(dim);
    }

    public float getDim() {
        return screenDimActor.getAlpha();
    }

    public float getScrollX() {
        return scrollX;
    }

    public void setScrollX(float scrollX) {
        if (scrollX < 0) {
            scrollX = 0;
        }
        if (scrollX > 1) {
            scrollX = 1;
        }
        this.scrollX = scrollX;

        // notify listeners
        for (ScrollChangeListener listener : scrollXChangeListeners) {
            listener.scrollChanged(scrollX);
        }
    }

    public float getScrollY() {
        return scrollY;
    }

    public void setScrollY(float scrollY) {
        if (scrollY < 0) {
            scrollY = 0;
        }
        if (scrollY > 1) {
            scrollY = 1;
        }
        this.scrollY = scrollY;

        // notify listeners
        for (ScrollChangeListener listener : scrollYChangeListeners) {
            listener.scrollChanged(scrollY);
        }
    }

    public void addScrollXChangeListener(ScrollChangeListener listener) {
        scrollXChangeListeners.add(listener);
        listener.scrollChanged(scrollX);
    }

    public void removeScrollXChangeListener(ScrollChangeListener listener) {
        scrollXChangeListeners.remove(listener);
    }

    public void addScrollYChangeListener(ScrollChangeListener listener) {
        scrollYChangeListeners.add(listener);
        listener.scrollChanged(scrollY);
    }

    public void removeScrollYChangeListener(ScrollChangeListener listener) {
        scrollYChangeListeners.remove(listener);
    }

    public ShaderProgram getETC1Shader() {
        return etc1Shader;
    }

    /**
     * Set the ETC1 shader. NOTE: the shader is not disposed by the screen,
     * since it may be used in other places for other purposes. Callers are responsible for
     * disposing it.
     *
     * @param etc1Shader
     */
    public void setETC1Shader(ShaderProgram etc1Shader) {
        this.etc1Shader = etc1Shader;
    }

    public ShaderProgram getETC1aShader() {
        return etc1aShader;
    }

    /**
     * Set the ETC1a shader. NOTE: the shader is not disposed by the screen,
     * since it may be used in other places for other purposes. Callers are responsible for
     * disposing it.
     *
     * @param etc1aShader
     */
    public void setETC1aShader(ShaderProgram etc1aShader) {
        this.etc1aShader = etc1aShader;
    }

    public DefaultShader getDefaultShader() {
        return defaultShader;
    }

    /**
     * Set the default shader. If the default shader is not NONE,
     * the corresponding shader (ETC1/ETC1a) is set as the stage's batch shader. Actors that do
     * not use this shader need to change it prior to drawing and restore the default one once
     * they have finished.
     *
     * @param defaultShader
     */
    public void setDefaultShader(DefaultShader defaultShader) {
        this.defaultShader = defaultShader;

        if (defaultShader == DefaultShader.ETC1) {
            stage.getBatch().setShader(etc1Shader);
        } else if (defaultShader == DefaultShader.ETC1A) {
            stage.getBatch().setShader(etc1aShader);
        } else {
            stage.getBatch().setShader(null);
        }

        if (screenDimActor != null) {
            if (defaultShader == DefaultShader.ETC1) {
                screenDimActor.setPostDrawShader(etc1Shader);
            } else if (defaultShader == DefaultShader.ETC1A) {
                screenDimActor.setPostDrawShader(etc1aShader);
            } else {
                screenDimActor.setPostDrawShader(null);
            }
        }
    }

    public float getFlingVelocityX() {
        return flingVelocityX;
    }

    public float getFlingVelocityY() {
        return flingVelocityY;
    }

    public float getMaxDeltaTime() {
        return maxDeltaTime;
    }

    public void setMaxDeltaTime(float maxDeltaTime) {
        this.maxDeltaTime = maxDeltaTime;
    }
}
