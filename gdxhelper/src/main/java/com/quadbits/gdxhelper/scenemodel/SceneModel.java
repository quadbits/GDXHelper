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
public class SceneModel extends BaseModel {
    public ControllerModel[] controllers;
    public ActorModel[] actors;
    public AssetsScaleBaseModel assetsScale;

    @Override
    public void validate() {
        super.validate();

        if (assetsScale == null) {
            throw new NullPointerException("field 'assetsScale' must not be null");
        }
        assetsScale.validate();

        if (controllers != null) {
            for (ControllerModel controller : controllers) {
                controller.validate();
            }
        }

        if (actors != null) {
            for (ActorModel actor : actors) {
                actor.validate();
            }
        }
    }

    @Override
    public void addSubModelsTo(Collection<BaseModel> subModelsCollection) {
        // Add all children
        Collections.addAll(subModelsCollection, controllers);
        Collections.addAll(subModelsCollection, actors);
        Collections.addAll(subModelsCollection, assetsScale);
    }
}
