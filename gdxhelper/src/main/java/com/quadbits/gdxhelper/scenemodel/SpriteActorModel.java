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
package com.quadbits.gdxhelper.scenemodel;

/**
 *
 */
public class SpriteActorModel extends ActorModel {
    public String textureName;
    public Float scaleX;
    public Float scaleY;
    public Float rotation;
    public float originX; // for rotation, relative to actor's size
    public float originY; // for rotation, relative to actor's size
    public boolean flipX;
    public boolean flipY;

    // Position-related properties
    public float x; // [0,1]
    public float y; // [0,1]
    public String xRelativeTo; // id
    public String yRelativeTo; // id
    public float positionOriginX; // [0,1]
    public float positionOriginY; // [0,1]

    @Override
    public void validate() {
        super.validate();

        if (textureName == null) {
            throw new NullPointerException("field 'textureName' must not be null");
        }
        if (scaleX == null) {
            scaleX = 1f;
        }
        if (scaleY == null) {
            scaleY = 1f;
        }
        if (rotation == null) {
            rotation = 0f;
        }
        if (xRelativeTo == null) {
            xRelativeTo = SCREEN_ID;
        }
        if (yRelativeTo == null) {
            yRelativeTo = SCREEN_ID;
        }
    }
}
