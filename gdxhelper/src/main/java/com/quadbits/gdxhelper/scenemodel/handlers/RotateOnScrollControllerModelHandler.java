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
import com.quadbits.gdxhelper.controllers.RotateOnScrollController;
import com.quadbits.gdxhelper.scenemodel.BaseModel;
import com.quadbits.gdxhelper.scenemodel.RotateOnScrollControllerModel;
import com.quadbits.gdxhelper.screens.LWPScreen;

import java.util.HashMap;

import javax.inject.Inject;

/**
 *
 */
public class RotateOnScrollControllerModelHandler extends ModelHandlerBaseImpl {
    @Inject
    protected Pool<RotateOnScrollController> rotateOnScrollControllerPool;

    @Inject
    protected LWPScreen screen;

    @Inject
    public RotateOnScrollControllerModelHandler() {
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

        RotateOnScrollControllerModel controllerModel = (RotateOnScrollControllerModel) model;
        HashMap<String, Controller> allControllers = sceneModelManager.getAllControllers();

        // Create controller
        RotateOnScrollController controller = rotateOnScrollControllerPool.obtain();
        allControllers.put(id, controller);

        // Min and max angle
        controller.setMinAngle(controllerModel.minAngle);
        controller.setMaxAngle(controllerModel.maxAngle);

        // Make controller listen to scroll changes
        if (controllerModel.listenToScrollX) {
            screen.addScrollXChangeListener(controller);
        }
        if (controllerModel.listenToScrollY) {
            screen.addScrollYChangeListener(controller);
        }

        return controller;
    }

    @Override
    public int getPreferenceForModel(BaseModel model) {
        if (model.getClass() == RotateOnScrollControllerModel.class) {
            return HANDLER_PREFERENCE_FOR_MODEL_EXACT_MATCH;
        }

        if (model instanceof RotateOnScrollControllerModel) {
            return HANDLER_PREFERENCE_FOR_MODEL_SUPERCLASS_MATCH;
        }

        return HANDLER_PREFERENCE_FOR_MODEL_NO_MATCH;
    }
}
