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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.DrawUtils;
import com.quadbits.gdxhelper.utils.Recyclable;
import com.quadbits.gdxhelper.utils.SpriteGrid;

import javax.inject.Inject;

/**
 *
 */
public class CloudsActor extends ControllableActor
        implements DrawUtils.BatchDrawableSprite, Recyclable<CloudsActor> {
    protected Pool<CloudsActor> cloudsActorPool;
    protected Array<SpriteGrid> spriteGrids;
    protected Array<Cloud> clouds;
    protected Array<Float> spritesMinScale;
    protected Array<Float> spritesMaxScale;
    protected float cloudsPerc; // [0, 1]
    protected float availableArea;
    protected float coveredArea;
    protected float fadeAnimDurationSeconds;
    protected float fadeAnimMaxDeltaSeconds;
    protected boolean fading;

    protected boolean tileableX;
    protected boolean tileableY;
    protected float minTiledX;
    protected float maxTiledX;
    protected float minTiledY;
    protected float maxTiledY;

    @Inject
    protected Pool<Cloud> cloudPool;

    @Inject
    protected Pool<SpriteGrid> spriteGridPool;

    public static final float DEFAULT_FADE_ANIM_DURATION_SECONDS = 1f;
    public static final float DEFAULT_FADE_ANIM_MAX_DELTA_SECONDS = 1.f / 30.f;

    @Inject
    public CloudsActor() {
        super();
        spriteGrids = new Array<SpriteGrid>();
        spritesMinScale = new Array<Float>();
        spritesMaxScale = new Array<Float>();
        clouds = new Array<Cloud>();
        init();
    }

    private void init() {
        cloudsPerc = 0;
        availableArea = 0;
        coveredArea = 0;
        fadeAnimDurationSeconds = DEFAULT_FADE_ANIM_DURATION_SECONDS;
        fadeAnimMaxDeltaSeconds = DEFAULT_FADE_ANIM_MAX_DELTA_SECONDS;
        fading = false;
    }

    @Override
    public void reset() {
        super.reset();

        for (SpriteGrid spriteGrid : spriteGrids) {
            spriteGrid.free();
        }
        spriteGrids.clear();
        spritesMinScale.clear();
        spritesMaxScale.clear();

        for (Cloud cloud : clouds) {
            cloud.free();
        }
        clouds.clear();
        init();
    }

    @Override
    public void free() {
        cloudsActorPool.free(this);
    }

    @Override
    public void setPool(Pool<CloudsActor> cloudsActorPool) {
        this.cloudsActorPool = cloudsActorPool;
    }

    public void addTexture(String textureName, float minScale, float maxScale) {
        SpriteGrid spriteGrid = spriteGridPool.obtain();
        spriteGrid.setTexture(textureName);
        spriteGrids.add(spriteGrid);
        spritesMinScale.add(minScale);
        spritesMaxScale.add(maxScale);
    }

    public void updateClouds() {
        recalculateAvailableArea();
        recalculateCoveredArea();

        // If already present clouds cover a greater area than available,
        // mark some clouds as removed
        for (Cloud cloud : clouds) {
            if (coveredArea <= availableArea) {
                break;
            }

            if (cloud.targetAlpha == 0) {
                continue;
            }

            cloud.targetAlpha = 0;
            coveredArea -= cloud.width * cloud.height;
        }

        // Add clouds until we fill up the available area
        while (coveredArea < availableArea) {
            Cloud cloud = createCloud(availableArea - coveredArea);
            if (cloud == null) {
                break;
            }

            clouds.add(cloud);
            coveredArea += cloud.width * cloud.height;
        }

        // Randomize positions of the clouds using a Halton sequence with bases 2 and 3,
        // discarding the first r numbers (r = random[1, 60])
        int base2 = 2;
        int base3 = 3;
        int index = MathUtils.random(1, 60) + 1;
        for (Cloud cloud : clouds) {
            if (cloud.targetAlpha == 1 && cloud.targetAlpha != cloud.alpha) {
                cloud.x = DrawUtils.haltonSequence(index, base2) * getWidth();
                cloud.x -= Math.max(0, cloud.x + cloud.width - getWidth());
                cloud.y = DrawUtils.haltonSequence(index, base3) * getHeight();
                index++;
            }
        }
    }

    protected void recalculateAvailableArea() {
        availableArea = getWidth() * getHeight() * cloudsPerc;
    }

    protected void recalculateCoveredArea() {
        coveredArea = 0;
        for (Cloud cloud : clouds) {
            if (cloud.targetAlpha == 1) {
                coveredArea += cloud.width * cloud.height;
            }
        }
    }

    protected Cloud createCloud(float remainingArea) {
        // Create the cloud object
        Cloud cloud = cloudPool.obtain();

        // Randomly choose the cloud sprite to use
        cloud.spriteGridIndex = MathUtils.random(spriteGrids.size - 1);
        SpriteGrid spriteGrid = spriteGrids.get(cloud.spriteGridIndex);

        // Find out the area occupied by the original sprite
        float originalArea = spriteGrid.getOriginalWidth() * spriteGrid.getOriginalHeight();

        // Calculate the maximum scale that we can use to create a cloud that would fit
        // in the remaining area
        float remainingAreaMaxScale = (float) Math.sqrt(remainingArea / originalArea);
        float minScale = spritesMinScale.get(cloud.spriteGridIndex);
        float maxScale = spritesMaxScale.get(cloud.spriteGridIndex);

        // If the scale is less than the minimum scale, return
        if (remainingAreaMaxScale < minScale) {
            cloud.free();
            return null;
        }

        // If the scale is less than the maximum scale, adjust max-scale
        if (remainingAreaMaxScale < maxScale) {
            maxScale = remainingAreaMaxScale;
        }

        // Set the cloud's properties
        cloud.scale = MathUtils.random(minScale, maxScale);
        cloud.width = spriteGrid.getOriginalWidth() * cloud.scale;
        cloud.height = spriteGrid.getOriginalHeight() * cloud.scale;
        //cloud.x = MathUtils.random(0, getWidth() - cloud.width);
        //cloud.y = MathUtils.random(0, getHeight() - cloud.height);
        cloud.flipX = MathUtils.randomBoolean();
        cloud.alpha = 0;
        cloud.targetAlpha = 1;

        return cloud;
    }

    protected void setSpriteGridPropertiesFromCloud(SpriteGrid spriteGrid, Cloud cloud) {
        spriteGrid.setPosition(getX() + cloud.x, getY() + cloud.y);
        spriteGrid.setSize(cloud.width, cloud.height);
        spriteGrid.setFlipX(cloud.flipX);
        spriteGrid.setFlipY(cloud.flipY);
        Color spriteGridColor = spriteGrid.getColor();
        Color actorColor = getColor();
        spriteGridColor.set(actorColor.r, actorColor.g, actorColor.b, cloud.alpha);
        spriteGrid.setColor(spriteGridColor);
    }

    public void setTextureFilter(Texture.TextureFilter minFilter, Texture.TextureFilter maxFilter) {
        for (SpriteGrid spriteGrid : spriteGrids) {
            spriteGrid.setTextureFilter(minFilter, maxFilter);
        }
    }

    @Override
    public void act(float deltaSeconds) {
        super.act(deltaSeconds);

        float deltaAlpha = deltaSeconds / fadeAnimDurationSeconds;

        // Update fade alphas
        for (Cloud cloud : clouds) {
            if (cloud.alpha != cloud.targetAlpha) {
                cloud.alpha += (cloud.targetAlpha == 1) ? deltaAlpha : -deltaAlpha;
                if (cloud.alpha < 0) {
                    cloud.alpha = 0;
                }
                if (cloud.alpha > 1) {
                    cloud.alpha = 1;
                }
            }
        }

        // If fade animations are completed, remove deleted clouds
        int i = 0;
        while (i < clouds.size) {
            Cloud cloud = clouds.get(i);
            if ((cloud.targetAlpha == 0) && (cloud.alpha == 0)) {
                clouds.removeIndex(i);
                cloud.free();
            } else {
                i++;
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Special case: not tileable
        if (!tileableX && !tileableY) {
            drawSprite(batch, parentAlpha);
        }

        // General case: tileable in at least one dimension
        else {
            DrawUtils.drawTileableSprite(batch, parentAlpha, this, tileableX, tileableY, minTiledX,
                    maxTiledX, minTiledY, maxTiledY);
        }
    }

    @Override
    public void drawSprite(Batch batch, float parentAlpha) {
        fading = false;
        float actorX = getX();
        float screenWidth = Gdx.graphics.getWidth();

        for (Cloud cloud : clouds) {
            // Do not draw out-of-screen clouds
            float cloudX = actorX + cloud.x;
            if (cloudX > screenWidth) {
                continue;
            }
            if (cloudX + cloud.width < 0) {
                continue;
            }

            // Set sprite grid properties from the cloud and draw it
            setSpriteGridPropertiesFromCloud(spriteGrids.get(cloud.spriteGridIndex), cloud);
            spriteGrids.get(cloud.spriteGridIndex).draw(batch, parentAlpha);
            if (cloud.alpha != cloud.targetAlpha) {
                fading = true;
            }
        }
    }

    @Override
    public long getMaxSleepTime() {
        if (fading) {
            return 0;
        }

        return super.getMaxSleepTime();
    }

    @Override
    public void scaleBy(float scale) {
        for (Cloud cloud : clouds) {
            SpriteGrid spriteGrid = spriteGrids.get(cloud.spriteGridIndex);
            float originalWidth = spriteGrid.getOriginalWidth();
            float originalHeight = spriteGrid.getOriginalHeight();
            cloud.width = originalWidth * cloud.scale * scale;
            cloud.height = originalHeight * cloud.scale * scale;
        }
    }

    public float getCloudsPerc() {
        return cloudsPerc;
    }

    public void setCloudsPerc(float cloudsPerc) {
        this.cloudsPerc = cloudsPerc;
    }

    public boolean isTileableX() {
        return tileableX;
    }

    public void setTileableX(boolean tileableX) {
        this.tileableX = tileableX;
    }

    public boolean isTileableY() {
        return tileableY;
    }

    public void setTileableY(boolean tileableY) {
        this.tileableY = tileableY;
    }

    public float getMinTiledX() {
        return minTiledX;
    }

    public void setMinTiledX(float minTiledX) {
        this.minTiledX = minTiledX;
    }

    public float getMaxTiledX() {
        return maxTiledX;
    }

    public void setMaxTiledX(float maxTiledX) {
        this.maxTiledX = maxTiledX;
    }

    public float getMinTiledY() {
        return minTiledY;
    }

    public void setMinTiledY(float minTiledY) {
        this.minTiledY = minTiledY;
    }

    public float getMaxTiledY() {
        return maxTiledY;
    }

    public void setMaxTiledY(float maxTiledY) {
        this.maxTiledY = maxTiledY;
    }

    public void setTiledBoundsX(float minTiledX, float maxTiledX) {
        this.minTiledX = minTiledX;
        this.maxTiledX = maxTiledX;
    }

    public void setTiledBoundsY(float minTiledY, float maxTiledY) {
        this.minTiledY = minTiledY;
        this.maxTiledY = maxTiledY;
    }

    public float getFadeAnimDurationSeconds() {
        return fadeAnimDurationSeconds;
    }

    public void setFadeAnimDurationSeconds(float fadeAnimDurationSeconds) {
        this.fadeAnimDurationSeconds = fadeAnimDurationSeconds;
    }

    public float getFadeAnimMaxDeltaSeconds() {
        return fadeAnimMaxDeltaSeconds;
    }

    public void setFadeAnimMaxDeltaSeconds(float fadeAnimMaxDeltaSeconds) {
        this.fadeAnimMaxDeltaSeconds = fadeAnimMaxDeltaSeconds;
    }

    public static class Cloud implements Pool.Poolable {
        protected Pool<Cloud> cloudPool;
        float x, y;
        float width, height;
        float scale;
        float alpha, targetAlpha;
        boolean flipX, flipY;
        int spriteGridIndex;

        @Inject
        public Cloud() {
            reset();
        }

        @Override
        public void reset() {
            x = y = width = height = 0;
            scale = 1;
            alpha = 0;
            targetAlpha = 1;
            flipX = flipY = false;
            spriteGridIndex = -1;
        }

        public void free() {
            cloudPool.free(this);
        }

        public void setPool(Pool<Cloud> cloudPool) {
            this.cloudPool = cloudPool;
        }
    }

}
