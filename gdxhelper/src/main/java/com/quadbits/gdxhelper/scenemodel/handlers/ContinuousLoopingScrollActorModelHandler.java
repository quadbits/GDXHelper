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
import com.quadbits.gdxhelper.actors.ContinuousLoopingScrollActor;
import com.quadbits.gdxhelper.scenemodel.BaseModel;
import com.quadbits.gdxhelper.scenemodel.ContinuousLoopingScrollActorModel;

import java.util.HashMap;

import javax.inject.Inject;

/**
 *
 */
public class ContinuousLoopingScrollActorModelHandler extends ModelHandlerBaseImpl {
    @Inject
    protected Pool<ContinuousLoopingScrollActor> continuousLoopingScrollActorPool;

    @Inject
    LWPStage stage;

    @Inject
    public ContinuousLoopingScrollActorModelHandler() {
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

        ContinuousLoopingScrollActorModel actorModel = (ContinuousLoopingScrollActorModel) model;
        HashMap<String, Actor> allActors = sceneModelManager.getAllActors();

        // Get parent
        Group parent = stage.getRoot();
        if (actorModel.parent != null) {
            parent = (Group) allActors.get(actorModel.parent);
        }

        // Create actor
        ContinuousLoopingScrollActor actor = continuousLoopingScrollActorPool.obtain();
        actor.setName(id);
        if (parent != null) {
            parent.addActor(actor);
        }
        allActors.put(id, actor);

        // Set scroll duration and rest duration
        actor.setRestDurationMillis(actorModel.restDurationMillis);
        actor.setScrollDurationMillis(actorModel.scrollDurationMillis);

        return actor;
    }

    @Override
    public int getPreferenceForModel(BaseModel model) {
        if (model.getClass() == ContinuousLoopingScrollActorModel.class) {
            return HANDLER_PREFERENCE_FOR_MODEL_EXACT_MATCH;
        }

        if (model instanceof ContinuousLoopingScrollActorModel) {
            return HANDLER_PREFERENCE_FOR_MODEL_SUPERCLASS_MATCH;
        }

        return HANDLER_PREFERENCE_FOR_MODEL_NO_MATCH;
    }
}
