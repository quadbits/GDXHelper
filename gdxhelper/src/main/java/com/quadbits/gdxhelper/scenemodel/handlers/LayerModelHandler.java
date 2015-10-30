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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.actors.Layer;
import com.quadbits.gdxhelper.controllers.Controller;
import com.quadbits.gdxhelper.scenemodel.ActorModel;
import com.quadbits.gdxhelper.scenemodel.BaseModel;
import com.quadbits.gdxhelper.scenemodel.LayerModel;
import com.quadbits.gdxhelper.utils.DependencyGraph;

import java.util.Collections;
import java.util.HashMap;

import javax.inject.Inject;

/**
 *
 */
public class LayerModelHandler extends ModelHandlerBaseImpl {
    @Inject
    protected Pool<Layer> layerPool;

    @Inject
    public LayerModelHandler() {
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

        LayerModel layerModel = (LayerModel) model;

        // Create layer
        Layer layer = layerPool.obtain();
        layer.setName(id);
        sceneModelManager.getAllActors().put(id, layer);

        // Process controllers
        if (layerModel.controllers != null) {
            for (String controllerId : layerModel.controllers) {
                Controller controller = sceneModelManager.getAllControllers().get(controllerId);
                if (controller != null) {
                    layer.addController(controller);
                }
            }
        }

        // Add all (actor) children to the processing queue
        if (layerModel.actors != null) {
            Collections.addAll(sceneModelManager.getModelProcessingQueue(), layerModel.actors);
        }

        return layer;
    }

    @Override
    public void layout(BaseModel model, Object object, boolean forceLayout) {
        // Safeguard against duplicate calls
        if (!forceLayout && model.laidout) {
            return;
        }
        model.laidout = true;

        LayerModel layerModel = (LayerModel) model;
        Layer layer = (Layer) object;
        HashMap<String, Actor> allActors = sceneModelManager.getAllActors();

        //----------------------------------------------------------------------
        // Width
        //----------------------------------------------------------------------
        float width = 1;

        // Relative sizing
        if (layerModel.wRelativeTo != null) {
            Actor wRelativeTo = allActors.get(layerModel.wRelativeTo);
            if (wRelativeTo != null) {
                width = wRelativeTo.getWidth();
            }
        }
        width *= layerModel.width;

        // Auto-adjust
        if (layerModel.autoAdjustHorizontally) {
            layer.autoAdjustHorizontally(layerModel.width);
        }

        // Manual sizing
        else {
            layer.setWidthCenterHorizontally(width, layerModel.repositionChildrenX);
        }

        //----------------------------------------------------------------------
        // Height
        //----------------------------------------------------------------------
        float height = 1;

        // Relative sizing
        if (layerModel.hRelativeTo != null) {
            Actor hRelativeTo = allActors.get(layerModel.hRelativeTo);
            if (hRelativeTo != null) {
                height = hRelativeTo.getHeight();
            }
        }
        height *= layerModel.height;

        // Auto-adjust
        if (layerModel.autoAdjustVertically) {
            layer.autoAdjustVertically(layerModel.height);
        }

        // Manual sizing
        else {
            layer.setHeightCenterVertically(height, layerModel.repositionChildrenY);
        }
    }

    @Override
    public int getPreferenceForModel(BaseModel model) {
        if (model.getClass() == LayerModel.class) {
            return HANDLER_PREFERENCE_FOR_MODEL_EXACT_MATCH;
        }

        if (model instanceof LayerModel) {
            return HANDLER_PREFERENCE_FOR_MODEL_SUPERCLASS_MATCH;
        }

        return HANDLER_PREFERENCE_FOR_MODEL_NO_MATCH;
    }

    @Override
    public void addModelDependenciesToGraph(BaseModel model,
                                            DependencyGraph<BaseModel> dependencyGraph) {
        super.addModelDependenciesToGraph(model, dependencyGraph);
        LayerModel layerModel = (LayerModel) model;

        // wRelativeTo dependency
        String wRelativeToId = layerModel.wRelativeTo;
        if (wRelativeToId != null) {
            BaseModel xRelativeTo = sceneModelManager.getModelById(wRelativeToId);
            dependencyGraph.addDependency(layerModel, xRelativeTo);
        }

        // hRelativeTo dependency
        String hRelativeToId = layerModel.hRelativeTo;
        if (hRelativeToId != null) {
            BaseModel yRelativeTo = sceneModelManager.getModelById(hRelativeToId);
            dependencyGraph.addDependency(layerModel, yRelativeTo);
        }

        // Child actor dependencies
        if (layerModel.actors != null) {
            for (ActorModel actor : layerModel.actors) {
                dependencyGraph.addDependency(layerModel, actor);
            }
        }
    }
}
