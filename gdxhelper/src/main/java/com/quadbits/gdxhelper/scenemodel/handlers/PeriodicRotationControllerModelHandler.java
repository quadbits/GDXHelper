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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.controllers.Controller;
import com.quadbits.gdxhelper.controllers.PeriodicRotationController;
import com.quadbits.gdxhelper.scenemodel.BaseModel;
import com.quadbits.gdxhelper.scenemodel.PeriodicRotationControllerModel;

import java.util.HashMap;

import javax.inject.Inject;

/**
 *
 */
public class PeriodicRotationControllerModelHandler extends ModelHandlerBaseImpl {
    @Inject
    protected Pool<PeriodicRotationController> periodicRotationControllerPool;

    @Inject
    public PeriodicRotationControllerModelHandler() {
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

        PeriodicRotationControllerModel controllerModel = (PeriodicRotationControllerModel) model;
        HashMap<String, Controller> allControllers = sceneModelManager.getAllControllers();

        // Create controller
        PeriodicRotationController controller = periodicRotationControllerPool.obtain();
        allControllers.put(id, controller);

        // Min and max angle
        controller.setMinAngle(controllerModel.minAngle);
        controller.setMaxAngle(controllerModel.maxAngle);

        // Durations
        controller.setDurations(controllerModel.rotationDuration, controllerModel.stallDuration);

        // Interpolation
        if (controllerModel.decelerate) {
            controller.setInterpolation(Interpolation.pow2);
        }

        return controller;
    }

    @Override
    public int getPreferenceForModel(BaseModel model) {
        if (model.getClass() == PeriodicRotationControllerModel.class) {
            return ModelHandlerBaseImpl.HANDLER_PREFERENCE_FOR_MODEL_EXACT_MATCH;
        }

        if (model instanceof PeriodicRotationControllerModel) {
            return ModelHandlerBaseImpl.HANDLER_PREFERENCE_FOR_MODEL_SUPERCLASS_MATCH;
        }

        return ModelHandlerBaseImpl.HANDLER_PREFERENCE_FOR_MODEL_NO_MATCH;
    }
}
