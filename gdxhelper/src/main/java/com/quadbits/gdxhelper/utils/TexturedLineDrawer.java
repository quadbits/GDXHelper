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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 *
 */
public class TexturedLineDrawer {

    Vector2 tmpPoint;
    float tmpLineHeight;
    float tmpHalfLineHeight;
    Sprite lineSprite;
    float scale;
    float pxDensity;

    public TexturedLineDrawer() {
        super();
        lineSprite = null;
        pxDensity = 1;
        init();
    }

    public TexturedLineDrawer(Sprite lineSprite, float dpi) {
        super();
        setLineSprite(lineSprite);
        pxDensity = dpi;
        init();
    }

    protected void init() {
        scale = 1.f;
        tmpPoint = new Vector2();
    }

    public void setLineSprite(Sprite lineSprite) {
        this.lineSprite = lineSprite;
        // re-set line scale to update its value using the new sprite height
        setScale(scale);
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
        tmpLineHeight = lineSprite.getRegionHeight() * scale;
        tmpHalfLineHeight = tmpLineHeight / 2;
    }

    public void drawLine(Batch batch, float x1, float y1, float x2, float y2) {
        drawLine(batch, x1, y1, x2, y2, null);
    }

    public void drawLine(Batch batch, float x1, float y1, float x2, float y2, Color color) {
        // Get the diff vector between origin and destination to obtain size and rotation
        tmpPoint.set(x2, y2);
        tmpPoint.sub(x1, y1);

        // Set size and rotation
        lineSprite.setSize(tmpPoint.len(), tmpLineHeight * pxDensity);
        float angle = tmpPoint.angle();
        lineSprite.setOrigin(0, 0);
        lineSprite.setRotation(angle);

        // Set position
        float xDelta = MathUtils.sinDeg(angle) * tmpHalfLineHeight;
        float yDelta = -MathUtils.cosDeg(angle) * tmpHalfLineHeight;
        lineSprite.setPosition(x1 + xDelta, y1 + yDelta);

        // Set color
        if (color != null) {
            lineSprite.setColor(color);
        }

        // Draw line
        lineSprite.draw(batch);
    }

    public Sprite getLineSprite() {
        return lineSprite;
    }

    public float getPxDensity() {
        return pxDensity;
    }

    public void setPxDensity(float pxDensity) {
        this.pxDensity = pxDensity;
    }
}
