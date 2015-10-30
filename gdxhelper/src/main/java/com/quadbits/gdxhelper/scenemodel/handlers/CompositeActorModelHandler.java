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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.actors.CompositeActor;
import com.quadbits.gdxhelper.controllers.Controller;
import com.quadbits.gdxhelper.scenemodel.ActorModel;
import com.quadbits.gdxhelper.scenemodel.BaseModel;
import com.quadbits.gdxhelper.scenemodel.CompositeActorModel;
import com.quadbits.gdxhelper.utils.DependencyGraph;

import java.util.Collections;
import java.util.HashMap;

import javax.inject.Inject;

/**
 *
 */
public class CompositeActorModelHandler extends ModelHandlerBaseImpl {
    @Inject
    protected Pool<CompositeActor> compositeActorPool;

    @Inject
    public CompositeActorModelHandler() {
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

        CompositeActorModel actorModel = (CompositeActorModel) model;
        HashMap<String, Actor> allActors = sceneModelManager.getAllActors();

        // Get parent
        Group parent = null;
        if (actorModel.parent != null) {
            parent = (Group) allActors.get(actorModel.parent);
        }

        // Create CompositeActor
        CompositeActor actor = compositeActorPool.obtain();
        actor.setName(id);
        if (parent != null) {
            parent.addActor(actor);
        }
        allActors.put(id, actor);

        // Set scale
        // (NOTE: this *MUST* be set here, at creation time, before scaling actors,
        // since scale will remain the same through all the actor's lifecycle,
        // independently of its layout).
        actor.setScale(actorModel.scaleX, actorModel.scaleY);

        // Set rotation
        actor.setRotation(actorModel.rotation);

        // Process controllers
        if (actorModel.controllers != null) {
            for (String controllerId : actorModel.controllers) {
                Controller controller = sceneModelManager.getAllControllers().get(controllerId);
                if (controller != null) {
                    actor.addController(controller);
                }
            }
        }

        // Add all (actor) children to the processing queue
        if (actorModel.actors != null) {
            Collections.addAll(sceneModelManager.getModelProcessingQueue(), actorModel.actors);
        }

        return actor;
    }

    @Override
    public void layout(BaseModel model, Object object, boolean forceLayout) {
        // Safeguard against duplicate calls
        if (!forceLayout && model.laidout) {
            return;
        }
        model.laidout = true;

        CompositeActorModel compositeActorModel = (CompositeActorModel) model;
        CompositeActor compositeActor = (CompositeActor) object;
        HashMap<String, Actor> allActors = sceneModelManager.getAllActors();

        // Origin x,y for rotations
        compositeActor.setOrigin(compositeActorModel.originX * compositeActor.getWidth(),
                compositeActorModel.originY * compositeActor.getHeight());

        //----------------------------------------------------------------------
        // X
        //----------------------------------------------------------------------
        float x;

        // Absolute
        if (compositeActorModel.xRelativeTo.equals(BaseModel.SCREEN_ID)) {
            x = compositeActorModel.x * Gdx.graphics.getWidth() -
                    compositeActorModel.positionOriginX * compositeActor.getWidth();
        }

        // Relative
        else {
            Actor xRelativeTo = allActors.get(compositeActorModel.xRelativeTo);
            x = xRelativeTo.getX() + compositeActorModel.x * xRelativeTo.getWidth() -
                    compositeActorModel.positionOriginX * compositeActor.getWidth();
        }

        //----------------------------------------------------------------------
        // Y
        //----------------------------------------------------------------------
        float y;

        // Absolute
        if (compositeActorModel.yRelativeTo.equals(BaseModel.SCREEN_ID)) {
            y = compositeActorModel.y * Gdx.graphics.getHeight() -
                    compositeActorModel.positionOriginY * compositeActor.getHeight();
        }

        // Relative
        else {
            Actor yRelativeTo = allActors.get(compositeActorModel.yRelativeTo);
            y = yRelativeTo.getY() + compositeActorModel.y * yRelativeTo.getHeight() -
                    compositeActorModel.positionOriginY * compositeActor.getHeight();
        }

        // Set position
        compositeActor.setPosition(x, y);

        // Flip X/Y must be set here, since it needs its children to be completely layout
        compositeActor.setFlipX(compositeActorModel.flipX);
        compositeActor.setFlipY(compositeActorModel.flipY);
    }

    @Override
    public int getPreferenceForModel(BaseModel model) {
        if (model.getClass() == CompositeActorModel.class) {
            return ModelHandlerBaseImpl.HANDLER_PREFERENCE_FOR_MODEL_EXACT_MATCH;
        }

        if (model instanceof CompositeActorModel) {
            return ModelHandlerBaseImpl.HANDLER_PREFERENCE_FOR_MODEL_SUPERCLASS_MATCH;
        }

        return ModelHandlerBaseImpl.HANDLER_PREFERENCE_FOR_MODEL_NO_MATCH;
    }

    @Override
    public void addModelDependenciesToGraph(BaseModel model,
                                            DependencyGraph<BaseModel> dependencyGraph) {
        super.addModelDependenciesToGraph(model, dependencyGraph);
        CompositeActorModel compositeActorModel = (CompositeActorModel) model;

        // xRelativeTo dependency
        String xRelativeToId = compositeActorModel.xRelativeTo;
        if (xRelativeToId != null) {
            BaseModel xRelativeTo = sceneModelManager.getModelById(xRelativeToId);
            dependencyGraph.addDependency(compositeActorModel, xRelativeTo);
        }

        // yRelativeTo dependency
        String yRelativeToId = compositeActorModel.yRelativeTo;
        if (yRelativeToId != null) {
            BaseModel yRelativeTo = sceneModelManager.getModelById(yRelativeToId);
            dependencyGraph.addDependency(compositeActorModel, yRelativeTo);
        }

        // Child actor dependencies
        if (compositeActorModel.actors != null) {
            for (ActorModel actor : compositeActorModel.actors) {
                dependencyGraph.addDependency(compositeActorModel, actor);
            }
        }
    }
}
