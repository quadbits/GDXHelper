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
import com.quadbits.gdxhelper.actors.SpriteActor;
import com.quadbits.gdxhelper.controllers.Controller;
import com.quadbits.gdxhelper.scenemodel.BaseModel;
import com.quadbits.gdxhelper.scenemodel.SpriteActorModel;
import com.quadbits.gdxhelper.screens.LWPScreen;
import com.quadbits.gdxhelper.utils.DependencyGraph;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

/**
 *
 */
public class SpriteActorModelHandler extends ModelHandlerBaseImpl {
    @Inject
    protected Pool<SpriteActor> spriteActorPool;

    @Inject
    protected LWPScreen screen;

    @Inject
    public SpriteActorModelHandler() {
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

        SpriteActorModel actorModel = (SpriteActorModel) model;
        HashMap<String, Actor> allActors = sceneModelManager.getAllActors();
        ArrayList<Actor> resizeableActorsRelative = sceneModelManager.getResizeableActorsRelative();
        HashMap<String, Controller> allControllers = sceneModelManager.getAllControllers();

        // Get parent
        Group parent = null;
        if (actorModel.parent != null && actorModel.addToParent) {
            parent = (Group) allActors.get(actorModel.parent);
        }

        // Create sprite actor
        SpriteActor actor = spriteActorPool.obtain();
        actor.setTexture(actorModel.textureName);
        actor.setName(id);
        if (parent != null) {
            parent.addActor(actor);
        }
        allActors.put(id, actor);
        resizeableActorsRelative.add(actor);

        // Set ETC1 shaders
        if (actorModel.textureName.startsWith("etc1/")) {
            if (screen.getDefaultShader() != LWPScreen.DefaultShader.ETC1) {
                actor.setPreDrawShader(screen.getETC1Shader());
                actor.setPostDrawShader(screen.getETC1aShader());
            }
        }

        // Set ETC1a shaders
        else if (actorModel.textureName.startsWith("etc1a/")) {
            if (screen.getDefaultShader() != LWPScreen.DefaultShader.ETC1A) {
                actor.setPreDrawShader(screen.getETC1aShader());
                actor.setPostDrawShader(screen.getETC1Shader());
            }
        }

        // Set scale
        // (NOTE: this *MUST* be set here, at creation time, before scaling actors,
        // since scale will remain the same through all the actor's lifecycle,
        // independently of its layout).
        actor.setScale(actorModel.scaleX, actorModel.scaleY);

        // Set rotation
        actor.setRotation(actorModel.rotation);

        // Set flip
        actor.setFlipX(actorModel.flipX);
        actor.setFlipY(actorModel.flipY);

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

        SpriteActorModel actorModel = (SpriteActorModel) model;
        SpriteActor actor = (SpriteActor) object;
        HashMap<String, Actor> allActors = sceneModelManager.getAllActors();

        // Origin x,y for rotations
        actor.setOrigin(actorModel.originX * actor.getWidth(),
                actorModel.originY * actor.getHeight());

        //----------------------------------------------------------------------
        // X
        //----------------------------------------------------------------------
        float x;

        // Absolute
        if (actorModel.xRelativeTo.equals(BaseModel.SCREEN_ID)) {
            x = actorModel.x * Gdx.graphics.getWidth() -
                    actorModel.positionOriginX * actor.getWidth();
        }

        // Relative
        else {
            Actor xRelativeTo = allActors.get(actorModel.xRelativeTo);
            x = xRelativeTo.getX() + actorModel.x * xRelativeTo.getWidth() -
                    actorModel.positionOriginX * actor.getWidth();
        }

        //----------------------------------------------------------------------
        // Y
        //----------------------------------------------------------------------
        float y;

        // Absolute
        if (actorModel.yRelativeTo.equals(BaseModel.SCREEN_ID)) {
            y = actorModel.y * Gdx.graphics.getHeight() -
                    actorModel.positionOriginY * actor.getHeight();
        }

        // Relative
        else {
            Actor yRelativeTo = allActors.get(actorModel.yRelativeTo);
            y = yRelativeTo.getY() + actorModel.y * yRelativeTo.getHeight() -
                    actorModel.positionOriginY * actor.getHeight();
        }

        // Set position
        actor.setPosition(x, y);
    }

    @Override
    public int getPreferenceForModel(BaseModel model) {
        if (model.getClass() == SpriteActorModel.class) {
            return HANDLER_PREFERENCE_FOR_MODEL_EXACT_MATCH;
        }

        if (model instanceof SpriteActorModel) {
            return HANDLER_PREFERENCE_FOR_MODEL_SUPERCLASS_MATCH;
        }

        return HANDLER_PREFERENCE_FOR_MODEL_NO_MATCH;
    }

    @Override
    public void addModelDependenciesToGraph(BaseModel model,
                                            DependencyGraph<BaseModel> dependencyGraph) {
        SpriteActorModel spriteActorModel = (SpriteActorModel) model;

        // xRelativeTo dependency
        String xRelativeToId = spriteActorModel.xRelativeTo;
        if (!xRelativeToId.equals(BaseModel.SCREEN_ID)) {
            BaseModel xRelativeTo = sceneModelManager.getModelById(xRelativeToId);
            dependencyGraph.addDependency(spriteActorModel, xRelativeTo);
        }

        // yRelativeTo dependency
        String yRelativeToId = spriteActorModel.yRelativeTo;
        if (!yRelativeToId.equals(BaseModel.SCREEN_ID)) {
            BaseModel yRelativeTo = sceneModelManager.getModelById(yRelativeToId);
            dependencyGraph.addDependency(spriteActorModel, yRelativeTo);
        }
    }
}
