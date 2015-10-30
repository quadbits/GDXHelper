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

import com.badlogic.gdx.graphics.g2d.Batch;

/**
 *
 */
public class DrawUtils {
    public static final String ETC1A_VERTEX_SHADER = "" //
            + "uniform mat4 u_projTrans;\n"//
            + "\n"//
            + "attribute vec4 a_position;\n"//
            + "attribute vec2 a_texCoord0;\n"//
            + "attribute vec4 a_color;\n"//
            + "\n"//
            + "varying vec4 v_color;\n"//
            + "varying vec2 v_texCoord;\n"//
            + "\n"//
            + "void main() {\n"//
            + " gl_Position = u_projTrans * a_position;\n"//
            + " v_texCoord = a_texCoord0;\n"//
            + " v_color = a_color;\n"//
            + "}\n";
    public static final String ETC1A_FRAGMENT_SHADER = ""//
            + "#ifdef GL_ES\n"//
            + "precision mediump float;\n"//
            + "#endif\n"//
            + "uniform sampler2D u_texture;\n"//
            + "\n"//
            + "varying vec4 v_color;\n"//
            + "varying vec2 v_texCoord;\n"//
            + "\n"//
            + "void main() {\n"//
            + " vec3 col = texture2D(u_texture, v_texCoord.st).rgb;\n"//
            + " float alpha = texture2D(u_texture, v_texCoord.st + vec2(0.0, 0.5)).r;\n"//
            + " gl_FragColor = vec4(col, alpha) * v_color;\n"//
            + "}\n";

    public static interface BatchDrawableSprite {
        public void drawSprite(Batch batch, float parentAlpha);

        public float getX();

        public float getY();

        public float getWidth();

        public float getHeight();

        public void setPosition(float x, float y);
    }

    public static void drawTileableSprite(Batch batch, float parentAlpha,
                                          BatchDrawableSprite drawable, boolean tileableX,
                                          boolean tileableY, float minTiledX, float maxTiledX,
                                          float minTiledY, float maxTiledY) {
        // Check values
        if (!tileableX) {
            minTiledX = 0;
        }
        if (!tileableY) {
            minTiledY = 0;
        }

        // Save old position
        float oldX = drawable.getX();
        float oldY = drawable.getY();

        // Cache width and height values
        float width = drawable.getWidth();
        float height = drawable.getHeight();

        // Calculate the number of repetitions on both dimensions X-Y
        int maxI = tileableX ? (int) Math.ceil((maxTiledX - minTiledX) / width) : 0;
        int maxJ = tileableY ? (int) Math.ceil((maxTiledY - minTiledY) / height) : 0;

        // Draw tiles
        for (int i = 0; i <= maxI; i++) {
            for (int j = 0; j <= maxJ; j++) {
                float tiledX = oldX;
                float tiledY = oldY;
                if (tileableX) {
                    float offsetX = oldX % width;
                    if (offsetX > 0) {
                        tiledX = offsetX + (i - 1) * width;
                    } else {
                        tiledX = offsetX + i * width;
                    }
                }
                if (tileableY) {
                    float offsetY = oldY % height;
                    if (offsetY > 0) {
                        tiledY = offsetY + (j - 1) * height;
                    } else {
                        tiledY = offsetY + j * height;
                    }
                }

                drawable.setPosition(minTiledX + tiledX, minTiledY + tiledY);
                drawable.drawSprite(batch, parentAlpha);
            }
        }

        // Restore old position
        drawable.setPosition(oldX, oldY);
    }

    /**
     * Produces the 'index'-th element of a Halton sequence with base 'base'.
     * See http://en.wikipedia.org/wiki/Halton_sequence
     *
     * @param index
     *         The element's position in the sequence we want to calculate
     * @param base
     *         The base to use (generally, a prime number).
     *
     * @return The element at the specified position of this Halton sequence
     */
    public static float haltonSequence(int index, int base) {
        float result = 0;
        float fraction = 1.f / (float) base;
        float i = (float) index;

        while (i > 0) {
            result += fraction * (i % base);
            i = (float) Math.floor(i / base);
            fraction /= base;
        }

        return result;
    }
}
