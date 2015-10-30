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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.controllers.Controller;
import com.quadbits.gdxhelper.controllers.LinearTrajectoryController;
import com.quadbits.gdxhelper.scenemodel.BaseModel;
import com.quadbits.gdxhelper.scenemodel.LinearTrajectoryControllerModel;
import com.quadbits.gdxhelper.utils.DependencyGraph;

import java.util.HashMap;

import javax.inject.Inject;

/**
 *
 */
public class LinearTrajectoryControllerModelHandler extends ModelHandlerBaseImpl {
    @Inject
    protected Pool<LinearTrajectoryController> linearTrajectoryControllerPool;

    @Inject
    public LinearTrajectoryControllerModelHandler() {
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

        LinearTrajectoryControllerModel controllerModel = (LinearTrajectoryControllerModel) model;
        HashMap<String, Controller> allControllers = sceneModelManager.getAllControllers();

        // Create controller
        LinearTrajectoryController controller = linearTrajectoryControllerPool.obtain();
        allControllers.put(id, controller);

        // Reversable?
        controller.setReversable(controllerModel.reversable);

        // Out-of-scene time
        controller.setAvgOutOfSceneTimeMillis(controllerModel.avgOutOfSceneTimeMillis);
        controller.setStdOutOfSceneTimeMillis(controllerModel.stdOutOfSceneTimeMillis);

        // Reference angles
        if (controllerModel.referenceAngleDegrees != null) {
            controller.setReferenceAngleDegrees(controllerModel.referenceAngleDegrees);
            controller.setReferenceMaxDeviationAngleDegrees(
                    controllerModel.referenceMaxDeviationAngleDegrees);
        }

        // Adjust actor's rotation?
        controller.setAdjustActorRotation(controllerModel.adjustActorRotation);

        return controller;
    }

    @Override
    public void layout(BaseModel model, Object object, boolean forceLayout) {
        // Safeguard against duplicate calls
        if (!forceLayout && model.laidout) {
            return;
        }
        model.laidout = true;

        LinearTrajectoryControllerModel controllerModel = (LinearTrajectoryControllerModel) model;
        LinearTrajectoryController controller = (LinearTrajectoryController) object;
        HashMap<String, Actor> allActors = sceneModelManager.getAllActors();

        //----------------------------------------------------------------------
        // Source
        //----------------------------------------------------------------------

        // Source X1
        float sourceX1 =
                controllerGetRelPosition(controllerModel.sourceXRelativeTo, controllerModel.sourceX,
                        "x");
        controller.setSourceX1(sourceX1);

        // Source X2
        float sourceX2 = controllerGetRelPosition(controllerModel.sourceX2RelativeTo,
                controllerModel.sourceX2, "x");
        controller.setSourceX2(sourceX2);

        // Source Y1
        float sourceY1 =
                controllerGetRelPosition(controllerModel.sourceYRelativeTo, controllerModel.sourceY,
                        "y");
        controller.setSourceY1(sourceY1);

        // Source Y2
        float sourceY2 = controllerGetRelPosition(controllerModel.sourceY2RelativeTo,
                controllerModel.sourceY2, "y");
        controller.setSourceY2(sourceY2);

        //----------------------------------------------------------------------
        // Target
        //----------------------------------------------------------------------

        // Target X1
        float targetX1 =
                controllerGetRelPosition(controllerModel.targetXRelativeTo, controllerModel.targetX,
                        "x");
        controller.setTargetX1(targetX1);

        // Target Y1
        float targetY1 =
                controllerGetRelPosition(controllerModel.targetYRelativeTo, controllerModel.targetY,
                        "y");
        controller.setTargetY1(targetY1);

        // Target X2
        float targetX2 = controllerGetRelPosition(controllerModel.targetX2RelativeTo,
                controllerModel.targetX2, "x");
        controller.setTargetX2(targetX2);

        // Target Y2
        float targetY2 = controllerGetRelPosition(controllerModel.targetY2RelativeTo,
                controllerModel.targetY2, "y");
        controller.setTargetY2(targetY2);

        //----------------------------------------------------------------------
        // Speed
        //----------------------------------------------------------------------
        float avgSpeedMillis, stdSpeedMillis;

        // Absolute values (px/millis)
        if (controllerModel.speedRelativeTo == null) {
            avgSpeedMillis = controllerModel.avgSpeedMillis;
            stdSpeedMillis = controllerModel.stdSpeedMillis;
        }

        // Relative values
        else {
            float refValue;

            // Screen
            if (controllerModel.speedRelativeTo.equals(BaseModel.SCREEN_ID)) {

                // Width
                if (controllerModel.speedRelativeToVariable
                        .equals(LinearTrajectoryControllerModel.SPEED_VARIABLE_WIDTH)) {
                    refValue = Gdx.graphics.getWidth();
                }

                // Height
                else if (controllerModel.speedRelativeToVariable
                        .equals(LinearTrajectoryControllerModel.SPEED_VARIABLE_HEIGHT)) {
                    refValue = Gdx.graphics.getHeight();
                }

                // Max
                else if (controllerModel.speedRelativeToVariable
                        .equals(LinearTrajectoryControllerModel.SPEED_VARIABLE_MAX)) {
                    refValue = Math.max(Gdx.graphics.getHeight(), Gdx.graphics.getHeight());
                }

                // Min
                else {
                    refValue = Math.min(Gdx.graphics.getHeight(), Gdx.graphics.getHeight());
                }
            }

            // Actor
            else {
                Actor refActor = allActors.get(controllerModel.speedRelativeTo);

                // Width
                if (controllerModel.speedRelativeToVariable
                        .equals(LinearTrajectoryControllerModel.SPEED_VARIABLE_WIDTH)) {
                    refValue = refActor.getWidth();
                }

                // Height
                else if (controllerModel.speedRelativeToVariable
                        .equals(LinearTrajectoryControllerModel.SPEED_VARIABLE_HEIGHT)) {
                    refValue = refActor.getHeight();
                }

                // Max
                else if (controllerModel.speedRelativeToVariable
                        .equals(LinearTrajectoryControllerModel.SPEED_VARIABLE_MAX)) {
                    refValue = Math.max(refActor.getHeight(), refActor.getHeight());
                }

                // Min
                else {
                    refValue = Math.min(refActor.getHeight(), refActor.getHeight());
                }
            }

            avgSpeedMillis = controllerModel.avgSpeedMillis * refValue;
            stdSpeedMillis = controllerModel.stdSpeedMillis * refValue;
        }

        controller.setAvgSpeedMillis(avgSpeedMillis);
        controller.setStdSpeedMillis(stdSpeedMillis);
    }

    protected float controllerGetRelPosition(String relativeTo, float value, String xy) {
        // Relative to screen
        if (relativeTo.equals(BaseModel.SCREEN_ID)) {
            if (xy.equals("x")) {
                return Gdx.graphics.getWidth() * value;
            } else {
                return Gdx.graphics.getHeight() * value;
            }
        }

        // Relative to actor
        Actor refActor = sceneModelManager.getAllActors().get(relativeTo);
        if (xy.equals("x")) {
            return refActor.getX() + value * refActor.getWidth();
        } else {
            return refActor.getY() + value * refActor.getHeight();
        }
    }

    @Override
    public int getPreferenceForModel(BaseModel model) {
        if (model.getClass() == LinearTrajectoryControllerModel.class) {
            return HANDLER_PREFERENCE_FOR_MODEL_EXACT_MATCH;
        }

        if (model instanceof LinearTrajectoryControllerModel) {
            return HANDLER_PREFERENCE_FOR_MODEL_SUPERCLASS_MATCH;
        }

        return HANDLER_PREFERENCE_FOR_MODEL_NO_MATCH;
    }

    @Override
    public void addModelDependenciesToGraph(BaseModel model,
                                            DependencyGraph<BaseModel> dependencyGraph) {
//        LinearTrajectoryControllerModel controllerModel = (LinearTrajectoryControllerModel) model;
//
//        if (!BaseModel.SCREEN_ID.equals(controllerModel.sourceXRelativeTo)) {
//            dependencyGraph.addDependency(controllerModel,
//                    sceneModelManager.getModelById(controllerModel.sourceXRelativeTo));
//        }
//        if (!BaseModel.SCREEN_ID.equals(controllerModel.sourceX2RelativeTo)) {
//            dependencyGraph.addDependency(controllerModel,
//                    sceneModelManager.getModelById(controllerModel.sourceX2RelativeTo));
//        }
//        if (!BaseModel.SCREEN_ID.equals(controllerModel.sourceYRelativeTo)) {
//            dependencyGraph.addDependency(controllerModel,
//                    sceneModelManager.getModelById(controllerModel.sourceYRelativeTo));
//        }
//        if (!BaseModel.SCREEN_ID.equals(controllerModel.sourceY2RelativeTo)) {
//            dependencyGraph.addDependency(controllerModel,
//                    sceneModelManager.getModelById(controllerModel.sourceY2RelativeTo));
//        }
//        if (!BaseModel.SCREEN_ID.equals(controllerModel.targetXRelativeTo)) {
//            dependencyGraph.addDependency(controllerModel,
//                    sceneModelManager.getModelById(controllerModel.targetXRelativeTo));
//        }
//        if (!BaseModel.SCREEN_ID.equals(controllerModel.targetX2RelativeTo)) {
//            dependencyGraph.addDependency(controllerModel,
//                    sceneModelManager.getModelById(controllerModel.targetX2RelativeTo));
//        }
//        if (!BaseModel.SCREEN_ID.equals(controllerModel.targetYRelativeTo)) {
//            dependencyGraph.addDependency(controllerModel,
//                    sceneModelManager.getModelById(controllerModel.targetYRelativeTo));
//        }
//        if (!BaseModel.SCREEN_ID.equals(controllerModel.targetY2RelativeTo)) {
//            dependencyGraph.addDependency(controllerModel,
//                    sceneModelManager.getModelById(controllerModel.targetY2RelativeTo));
//        }
//        if (!BaseModel.SCREEN_ID.equals(controllerModel.speedRelativeTo)) {
//            dependencyGraph.addDependency(controllerModel,
//                    sceneModelManager.getModelById(controllerModel.speedRelativeTo));
//        }
    }
}
