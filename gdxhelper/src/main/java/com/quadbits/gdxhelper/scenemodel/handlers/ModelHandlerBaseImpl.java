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
import com.quadbits.gdxhelper.scenemodel.LWPSceneModelManager;
import com.quadbits.gdxhelper.utils.DependencyGraph;

/**
 *
 */
public abstract class ModelHandlerBaseImpl implements ModelHandler {
    protected LWPSceneModelManager sceneModelManager;

    public static final int HANDLER_PREFERENCE_FOR_MODEL_EXACT_MATCH = 2;
    public static final int HANDLER_PREFERENCE_FOR_MODEL_SUPERCLASS_MATCH = 1;
    public static final int HANDLER_PREFERENCE_FOR_MODEL_NO_MATCH = 0;

    @Override
    public void setSceneModelManager(LWPSceneModelManager sceneModelManager) {
        this.sceneModelManager = sceneModelManager;
    }

    @Override
    public LWPSceneModelManager getSceneModelManager() {
        return sceneModelManager;
    }

    @Override
    public void addModelDependenciesToGraph(BaseModel model,
                                            DependencyGraph<BaseModel> dependencyGraph) {
        // Default implementation does nothing
    }

    @Override
    public Object create(BaseModel model) {
        return create(model, null, false);
    }

    @Override
    public Object create(BaseModel model, String id, boolean forceCreation) {
        // Default implementation creates nothing
        return null;
    }

    @Override
    public void layout(BaseModel model, Object object) {
        layout(model, object, false);
    }

    @Override
    public void layout(BaseModel model, Object object, boolean forceLayout) {
        // Default implementation does nothing
    }
}
