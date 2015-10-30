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
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.actors.ParticleEffectActor;
import com.quadbits.gdxhelper.controllers.Controller;
import com.quadbits.gdxhelper.scenemodel.BaseModel;
import com.quadbits.gdxhelper.scenemodel.ParticleEffectActorModel;
import com.quadbits.gdxhelper.screens.LWPScreen;
import com.quadbits.gdxhelper.utils.DependencyGraph;
import com.quadbits.gdxhelper.utils.TextureAtlasProxy;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

/**
 *
 */
public class ParticleEffectActorModelHandler extends ModelHandlerBaseImpl {
    @Inject
    protected Pool<ParticleEffectActor> particleEffectActorPool;

    @Inject
    protected TextureAtlasProxy textureAtlasProxy;

    @Inject
    protected LWPScreen screen;

    @Inject
    public ParticleEffectActorModelHandler() {
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

        ParticleEffectActorModel actorModel = (ParticleEffectActorModel) model;
        HashMap<String, Actor> allActors = sceneModelManager.getAllActors();
        ArrayList<Actor> resizeableActorsAbsolute = sceneModelManager.getResizeableActorsAbsolute();
        HashMap<String, Controller> allControllers = sceneModelManager.getAllControllers();
        HashMap<String, ParticleEffectPool> particleEffectPools =
                sceneModelManager.getParticleEffectPools();

        // Get parent
        Group parent = null;
        if (actorModel.parent != null && actorModel.addToParent) {
            parent = (Group) allActors.get(actorModel.parent);
        }

        // Create particle effect actor
        ParticleEffectActor actor = particleEffectActorPool.obtain();
        actor.setName(id);
        if (parent != null) {
            parent.addActor(actor);
        }
        allActors.put(id, actor);
        resizeableActorsAbsolute.add(actor);

        // Get pooled effect from pool, create pool if it does not exist
        ParticleEffectPool effectPool = particleEffectPools.get(actorModel.effectFile);
        if (effectPool == null) {
            ParticleEffect prototype = new ParticleEffect();
            if (actorModel.atlasPrefix == null) {
                prototype.load(Gdx.files.internal(actorModel.effectFile), textureAtlasProxy.get());
            } else {
                prototype.load(Gdx.files.internal(actorModel.effectFile), textureAtlasProxy.get(),
                        actorModel.atlasPrefix);
            }
            effectPool = new ParticleEffectPool(prototype, 1, 70);
            particleEffectPools.put(actorModel.effectFile, effectPool);
            //            prototype.dispose();
        }
        ParticleEffectPool.PooledEffect effect = effectPool.obtain();
        effect.start();
        actor.setEffect(effect);

        // Process controllers
        if (actorModel.controllers != null) {
            for (String controllerId : actorModel.controllers) {
                Controller controller = allControllers.get(controllerId);
                if (controller != null) {
                    actor.addController(controller);
                }
            }
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

        ParticleEffectActorModel actorModel = (ParticleEffectActorModel) model;
        ParticleEffectActor actor = (ParticleEffectActor) object;
        HashMap<String, Actor> allActors = sceneModelManager.getAllActors();

        //----------------------------------------------------------------------
        // X
        //----------------------------------------------------------------------
        float x;

        // Absolute
        if (actorModel.xRelativeTo.equals(BaseModel.SCREEN_ID)) {
            x = actorModel.x * Gdx.graphics.getWidth();
        }

        // Relative
        else {
            Actor xRelativeTo = allActors.get(actorModel.xRelativeTo);
            x = xRelativeTo.getX() + actorModel.x * xRelativeTo.getWidth();
        }

        //----------------------------------------------------------------------
        // Y
        //----------------------------------------------------------------------
        float y;

        // Absolute
        if (actorModel.yRelativeTo.equals(BaseModel.SCREEN_ID)) {
            y = actorModel.y * Gdx.graphics.getHeight();
        }

        // Relative
        else {
            Actor yRelativeTo = allActors.get(actorModel.yRelativeTo);
            y = yRelativeTo.getY() + actorModel.y * yRelativeTo.getHeight();
        }

        // Set position
        actor.setPosition(x, y);

        // Set ETC1a shaders
        if (screen.getDefaultShader() != LWPScreen.DefaultShader.ETC1A) {
            actor.setPreDrawShader(screen.getETC1aShader());
            actor.setPostDrawShader(screen.getETC1Shader());
        }

    }

    @Override
    public int getPreferenceForModel(BaseModel model) {
        if (model.getClass() == ParticleEffectActorModel.class) {
            return HANDLER_PREFERENCE_FOR_MODEL_EXACT_MATCH;
        }

        if (model instanceof ParticleEffectActorModel) {
            return HANDLER_PREFERENCE_FOR_MODEL_SUPERCLASS_MATCH;
        }

        return HANDLER_PREFERENCE_FOR_MODEL_NO_MATCH;
    }

    @Override
    public void addModelDependenciesToGraph(BaseModel model,
                                            DependencyGraph<BaseModel> dependencyGraph) {
        ParticleEffectActorModel actorModel = (ParticleEffectActorModel) model;

        // xRelativeTo dependency
        String xRelativeToId = actorModel.xRelativeTo;
        if (!xRelativeToId.equals(BaseModel.SCREEN_ID)) {
            BaseModel xRelativeTo = sceneModelManager.getModelById(xRelativeToId);
            dependencyGraph.addDependency(actorModel, xRelativeTo);
        }

        // yRelativeTo dependency
        String yRelativeToId = actorModel.yRelativeTo;
        if (!yRelativeToId.equals(BaseModel.SCREEN_ID)) {
            BaseModel yRelativeTo = sceneModelManager.getModelById(yRelativeToId);
            dependencyGraph.addDependency(actorModel, yRelativeTo);
        }
    }
}
