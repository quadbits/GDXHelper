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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import javax.inject.Inject;

/**
 *
 */
public class SpriteGrid implements Pool.Poolable, DrawUtils.BatchDrawableSprite {
    protected Pool<SpriteGrid> spriteGridPool;
    protected Array<Sprite> spriteGrid;
    protected int nCols;
    protected int nRows;
    protected float originalWidth;
    protected float originalHeight;
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected float scaleX;
    protected float scaleY;
    protected float originX;
    protected float originY;
    protected float rotation;
    protected boolean tileableX;
    protected boolean tileableY;
    protected float minTiledX;
    protected float maxTiledX;
    protected float minTiledY;
    protected float maxTiledY;
    protected boolean flipX;
    protected boolean flipY;
    protected Color color;
    protected boolean dirty;

    @Inject
    TextureAtlasProxy textureAtlasProxy;

    public static final int NCOLS_MAX = 1024;
    public static final int NROWS_MAX = 1024;

    @Inject
    public SpriteGrid() {
        spriteGridPool = null;
        spriteGrid = new Array<Sprite>();
        color = new Color();
        reset();
    }

    @Override
    public void reset() {
        spriteGrid.clear();
        nCols = nRows = 0;
        originalWidth = originalHeight = 0;
        x = y = 0;
        width = height = 0;
        scaleX = scaleY = 1;
        originX = originY = 0;
        rotation = 0;
        tileableX = tileableY = false;
        minTiledX = maxTiledX = minTiledY = maxTiledY = 0;
        flipX = flipY = false;
        color.set(1, 1, 1, 1);
        dirty = false;
    }

    public void setPool(Pool<SpriteGrid> spriteGridPool) {
        this.spriteGridPool = spriteGridPool;
    }

    public void free() {
        spriteGridPool.free(this);
    }

    public void setTexture(String textureName) {
        TextureRegion textureRegion = textureAtlasProxy.get().findRegion(textureName);

        // One single texture
        if (textureRegion != null) {
            nRows = 1;
            nCols = 1;
            spriteGrid.add(new Sprite(textureRegion));
        }

        // A grid of textures
        else {
            // Initialize nCols and nRows to a reasonably high value; these values will be adjusted
            // as the actual number of rows and columns is obtained by reading textures from the
            // atlas.
            nRows = NROWS_MAX;
            nCols = NCOLS_MAX;
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    String textureCellName = String.format("%s-%d-%d", textureName, i, j);
                    textureRegion = textureAtlasProxy.get().findRegion(textureCellName);
                    if (textureRegion == null) {
                        if (i == 0) {
                            nCols = j;
                        }
                        if (j == 0) {
                            nRows = i;
                        }
                        break;
                    }
                    spriteGrid.add(new Sprite(textureRegion));
                }
            }
        }

        // Check if we have really loaded an image
        if (nRows == 0 || nCols == 0) {
            throw new IllegalArgumentException(
                    String.format("Texture '%s' not found in texture atlas", textureName));
        }

        // Calculate original width
        originalWidth = 0;
        for (int j = 0; j < nCols; j++) {
            originalWidth += getSprite(0, j).getRegionWidth();
        }

        // Calculate original height
        originalHeight = 0;
        for (int i = 0; i < nRows; i++) {
            originalHeight += getSprite(i, 0).getRegionHeight();
        }

        // Set current width/height to the original values
        setSize(originalWidth, originalHeight);
    }

    protected Sprite getSprite(int i, int j) {
        return spriteGrid.get(i * nCols + j);
    }

    public void setTextureFilter(Texture.TextureFilter minFilter, Texture.TextureFilter maxFilter) {
        for (Sprite sprite : spriteGrid) {
            sprite.getTexture().setFilter(minFilter, maxFilter);
        }
    }

    private void adjustSpriteCellsPositionsAndSizes() {
        float widthScale = width / originalWidth;
        float heightScale = height / originalHeight;
        int offsetY = 0;
        for (int i = 0; i < nRows; i++) {
            int offsetX = 0;
            for (int j = 0; j < nCols; j++) {
                Sprite spriteCell = getSprite(i, j);
                int spriteCellWidth = (int) (widthScale * spriteCell.getRegionWidth());
                int spriteCellHeight = (int) (heightScale * spriteCell.getRegionHeight());
                spriteCell.setPosition(offsetX, offsetY);
                spriteCell.setSize(spriteCellWidth, spriteCellHeight);
                offsetX += spriteCell.getWidth();
            }
            offsetY += getSprite(i, 0).getHeight();
        }

        dirty = false;
    }

    public void draw(Batch batch, float parentAlpha) {
        if (dirty) {
            adjustSpriteCellsPositionsAndSizes();
        }

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

    public void drawSprite(Batch batch, float parentAlpha) {
        final float cos = MathUtils.cosDeg(rotation);
        final float sin = MathUtils.sinDeg(rotation);
        final float alpha = color.a * parentAlpha;
        for (int i = 0; i < nRows; i++) {
            for (int j = 0; j < nCols; j++) {
                // get the sprite
                int row = i;
                int col = j;
                if (flipX) {
                    col = nCols - 1 - j;
                }
                if (flipY) {
                    row = nRows - 1 - i;
                }
                Sprite spriteCell = getSprite(row, col);

                // save old position
                float oldSpriteCellX = spriteCell.getX();
                float oldSpriteCellY = spriteCell.getY();
                float spriteCellX = MathUtils.floor(oldSpriteCellX);
                float spriteCellY = MathUtils.floor(oldSpriteCellY);
                if (flipX) {
                    spriteCellX = width - (spriteCellX + spriteCell.getWidth());
                }
                if (flipY) {
                    spriteCellY = height - (spriteCellY + spriteCell.getHeight());
                }

                // set rotation and position
                if (rotation != 0) {
                    spriteCell.setOrigin(originX, originY);
                    spriteCell.setRotation(rotation);
                    spriteCell.setPosition(x + cos * spriteCellX - sin * spriteCellY,
                            y + sin * spriteCellX + cos * spriteCellY);
                } else {
                    spriteCell.setPosition(x + spriteCellX, y + spriteCellY);
                }
                spriteCell.setColor(color);
                // set alpha *after* color; otherwise, alpha is overwritten by the color's alpha
                spriteCell.setAlpha(alpha);

                // set flip
                spriteCell.setFlip(flipX, flipY);

                // draw the sprite
                spriteCell.draw(batch);

                // restore position and rotation
                spriteCell.setPosition(oldSpriteCellX, oldSpriteCellY);
                spriteCell.setRotation(0);
            }
        }
    }

    public float getOriginalWidth() {
        return originalWidth;
    }

    public float getOriginalHeight() {
        return originalHeight;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
        dirty = true;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        dirty = true;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        dirty = true;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
        dirty = true;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
        dirty = true;
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        dirty = true;
    }

    public void scaleBy(float scale) {
        setSize(originalWidth * this.scaleX * scale, originalHeight * this.scaleY * scale);
    }

    public void scaleBy(float scaleX, float scaleY) {
        setSize(originalWidth * this.scaleX * scaleX, originalHeight * this.scaleY * scaleY);
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
        setWidth(originalWidth * scaleX);
        dirty = true;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
        setHeight(originalHeight * scaleY);
        dirty = true;
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        setSize(originalWidth * scaleX, originalHeight * scaleY);
        dirty = true;
    }

    public float getOriginX() {
        return originX;
    }

    public void setOriginX(float originX) {
        this.originX = originX;
    }

    public float getOriginY() {
        return originY;
    }

    public void setOriginY(float originY) {
        this.originY = originY;
    }

    public void setOrigin(float originX, float originY) {
        this.originX = originX;
        this.originY = originY;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public boolean isTileableX() {
        return tileableX;
    }

    public void setTileableX(boolean isTileableX) {
        this.tileableX = isTileableX;
    }

    public boolean isTileableY() {
        return tileableY;
    }

    public void setTileableY(boolean isTileableY) {
        this.tileableY = isTileableY;
    }

    public boolean isFlipX() {
        return flipX;
    }

    public void setFlipX(boolean flipX) {
        this.flipX = flipX;
    }

    public boolean isFlipY() {
        return flipY;
    }

    public void setFlipY(boolean flipY) {
        this.flipY = flipY;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
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
}
