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

import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.controllers.Controller;
import com.quadbits.gdxhelper.controllers.RotationSimpleController;
import com.quadbits.gdxhelper.scenemodel.BaseModel;
import com.quadbits.gdxhelper.scenemodel.RotationSimpleControllerModel;

import java.util.HashMap;

import javax.inject.Inject;

/**
 *
 */
public class RotationSimpleControllerModelHandler extends ModelHandlerBaseImpl {
    @Inject
    protected Pool<RotationSimpleController> rotationSimpleControllerPool;

    @Inject
    public RotationSimpleControllerModelHandler() {
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

        RotationSimpleControllerModel controllerModel = (RotationSimpleControllerModel) model;
        HashMap<String, Controller> allControllers = sceneModelManager.getAllControllers();

        // Create controller
        RotationSimpleController controller = rotationSimpleControllerPool.obtain();
        allControllers.put(id, controller);

        // Set rotation speed
        controller.setRotationSpeed(controllerModel.rotationSpeed);

        // Reverse on flip
        controller.setReverseOnFlip(controllerModel.reverseOnFlip);

        return controller;
    }

    @Override
    public int getPreferenceForModel(BaseModel model) {
        if (model.getClass() == RotationSimpleControllerModel.class) {
            return ModelHandlerBaseImpl.HANDLER_PREFERENCE_FOR_MODEL_EXACT_MATCH;
        }

        if (model instanceof RotationSimpleControllerModel) {
            return ModelHandlerBaseImpl.HANDLER_PREFERENCE_FOR_MODEL_SUPERCLASS_MATCH;
        }

        return ModelHandlerBaseImpl.HANDLER_PREFERENCE_FOR_MODEL_NO_MATCH;
    }
}
