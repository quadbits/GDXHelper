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
public class LayerModel extends GroupModel {
    public ActorModel[] actors;
    public boolean autoAdjustHorizontally;
    public boolean autoAdjustVertically;
    public Float width;
    public Float height;
    public String wRelativeTo;
    public String hRelativeTo;
    public boolean repositionChildrenX;
    public boolean repositionChildrenY;

    @Override
    public void validate() {
        super.validate();

        if (!autoAdjustHorizontally && width == null) {
            throw new NullPointerException(
                    "field 'width' must not be null when 'autoAdjustHorizontally' is false");
        }

        if (!autoAdjustVertically && height == null) {
            throw new NullPointerException(
                    "field 'height' must not be null when 'autoAdjustVertically' is false");
        }

        if (width == null) {
            width = 1f;
        }

        if (height == null) {
            height = 1f;
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
