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
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.LWPStage;
import com.quadbits.gdxhelper.actors.AutomaticScrollingActor;
import com.quadbits.gdxhelper.scenemodel.AutomaticScrollingActorModel;
import com.quadbits.gdxhelper.scenemodel.BaseModel;
import com.quadbits.gdxhelper.utils.DependencyGraph;
import com.quadbits.gdxhelper.utils.ScrollTarget;

import java.util.HashMap;

import javax.inject.Inject;

/**
 *
 */
public class AutomaticScrollingActorModelHandler extends ModelHandlerBaseImpl {

    @Inject
    protected Pool<AutomaticScrollingActor> automaticScrollingActorPool;

    @Inject
    LWPStage stage;

    @Inject
    public AutomaticScrollingActorModelHandler() {
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

        AutomaticScrollingActorModel actorModel = (AutomaticScrollingActorModel) model;
        HashMap<String, Actor> allActors = sceneModelManager.getAllActors();

        // Get parent
        Group parent = stage.getRoot();
        if (actorModel.parent != null) {
            parent = (Group) allActors.get(actorModel.parent);
        }

        // Create actor
        AutomaticScrollingActor actor = automaticScrollingActorPool.obtain();
        actor.setName(id);
        if (parent != null) {
            parent.addActor(actor);
        }
        allActors.put(id, actor);

        // Scroll variable
        if (actorModel.scrollVariable.equals(AutomaticScrollingActorModel.SCROLL_VARIABLE_X)) {
            actor.setScrollVariable(AutomaticScrollingActor.ScrollVariable.SCROLL_X);
        } else {
            actor.setScrollVariable(AutomaticScrollingActor.ScrollVariable.SCROLL_Y);
        }

        // Scroll accel factor
        actor.setScrollAccelFactor(actorModel.scrollAccel);

        // Debug
        actor.setDebug(actorModel.debug);

        return actor;
    }

    @Override
    public void layout(BaseModel model, Object object, boolean forceLayout) {
        // Safeguard against duplicate calls
        if (!forceLayout && model.laidout) {
            return;
        }
        model.laidout = true;

        AutomaticScrollingActorModel actorModel = (AutomaticScrollingActorModel) model;
        AutomaticScrollingActor actor = (AutomaticScrollingActor) object;
        HashMap<String, Actor> allActors = sceneModelManager.getAllActors();

        // Scroll target
        ScrollTarget scrollTarget = (ScrollTarget) allActors.get(actorModel.scrollTarget);
        actor.setScrollTarget(scrollTarget);
    }

    @Override
    public int getPreferenceForModel(BaseModel model) {
        if (model.getClass() == AutomaticScrollingActorModel.class) {
            return HANDLER_PREFERENCE_FOR_MODEL_EXACT_MATCH;
        }

        if (model instanceof AutomaticScrollingActorModel) {
            return HANDLER_PREFERENCE_FOR_MODEL_SUPERCLASS_MATCH;
        }

        return HANDLER_PREFERENCE_FOR_MODEL_NO_MATCH;
    }

    @Override
    public void addModelDependenciesToGraph(BaseModel model,
                                            DependencyGraph<BaseModel> dependencyGraph) {
        super.addModelDependenciesToGraph(model, dependencyGraph);
        AutomaticScrollingActorModel actorModel = (AutomaticScrollingActorModel) model;

        if (actorModel.scrollTarget != null) {
            BaseModel scrollTarget = sceneModelManager.getModelById(actorModel.scrollTarget);
            dependencyGraph.addDependency(actorModel, scrollTarget);
        }
    }
}
