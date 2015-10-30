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

import java.util.Collection;
import java.util.Collections;

/**
 *
 */
public class CompositeActorModel extends GroupModel {
    public ActorModel[] actors;

    public Float scaleX;
    public Float scaleY;
    public float rotation;
    public float originX; // for rotation, relative to actor's size
    public float originY; // for rotation, relative to actor's size
    public boolean flipX;
    public boolean flipY;

    // Position-related properties
    public float x;
    public float y;
    public String xRelativeTo;
    public String yRelativeTo;
    public float positionOriginX; // [0,1]
    public float positionOriginY; // [0,1]

    @Override
    public void validate() {
        super.validate();

        if (scaleX == null) {
            scaleX = 1f;
        }
        if (scaleY == null) {
            scaleY = 1f;
        }
        if (xRelativeTo == null) {
            xRelativeTo = SCREEN_ID;
        }
        if (yRelativeTo == null) {
            yRelativeTo = SCREEN_ID;
        }

        if (actors != null) {
            for (ActorModel actor : actors) {
                actor.parent = id;
                actor.validate();
            }
        }
    }

    @Override
    public void addSubModelsTo(Collection<BaseModel> subModelsCollection) {
        // Add children
        Collections.addAll(subModelsCollection, actors);
    }
}
