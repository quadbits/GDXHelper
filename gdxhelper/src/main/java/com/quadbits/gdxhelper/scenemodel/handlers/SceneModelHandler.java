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
package com.quadbits.gdxhelper.scenemodel.handlers;

import com.quadbits.gdxhelper.scenemodel.BaseModel;
import com.quadbits.gdxhelper.scenemodel.SceneModel;

import java.util.Collections;
import java.util.Queue;

import javax.inject.Inject;

/**
 *
 */
public class SceneModelHandler extends ModelHandlerBaseImpl {

    @Inject
    public SceneModelHandler() {
        super();
    }

    @Override
    public Object create(BaseModel model, String id, boolean forceCreation) {
        // Safeguard against duplicate calls
        if (!forceCreation && model.created) {
            return null;
        }
        model.created = true;

        // Id
        if (id == null) {
            id = model.id;
        }

        SceneModel sceneModel = (SceneModel) model;
        Queue<BaseModel> modelProcessingQueue = sceneModelManager.getModelProcessingQueue();

        // First, add all controllers to the processing queue
        if (sceneModel.controllers != null) {
            Collections.addAll(modelProcessingQueue, sceneModel.controllers);
        }

        // Then, add all (actor) children to the processing queue
        if (sceneModel.actors != null) {
            Collections.addAll(modelProcessingQueue, sceneModel.actors);
        }

        return null;
    }

    @Override
    public int getPreferenceForModel(BaseModel model) {
        if (model.getClass() == SceneModel.class) {
            return HANDLER_PREFERENCE_FOR_MODEL_EXACT_MATCH;
        }

        if (model instanceof SceneModel) {
            return HANDLER_PREFERENCE_FOR_MODEL_SUPERCLASS_MATCH;
        }

        return HANDLER_PREFERENCE_FOR_MODEL_NO_MATCH;
    }
}
