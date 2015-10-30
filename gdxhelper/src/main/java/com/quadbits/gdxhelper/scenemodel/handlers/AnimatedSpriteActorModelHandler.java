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
import com.quadbits.gdxhelper.actors.AnimatedSpriteActor;
import com.quadbits.gdxhelper.controllers.Controller;
import com.quadbits.gdxhelper.scenemodel.AnimatedSpriteActorModel;
import com.quadbits.gdxhelper.scenemodel.BaseModel;
import com.quadbits.gdxhelper.utils.DependencyGraph;
import com.quadbits.gdxhelper.utils.SpriteGrid;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

/**
 *
 */
public class AnimatedSpriteActorModelHandler extends ModelHandlerBaseImpl {
    @Inject
    protected Pool<AnimatedSpriteActor> animatedSpriteActorPool;

    @Inject
    public AnimatedSpriteActorModelHandler() {
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

        AnimatedSpriteActorModel actorModel = (AnimatedSpriteActorModel) model;
        HashMap<String, Actor> allActors = sceneModelManager.getAllActors();
        ArrayList<Actor> resizeableActorsRelative = sceneModelManager.getResizeableActorsRelative();
        HashMap<String, Controller> allControllers = sceneModelManager.getAllControllers();

        // Get parent
        Group parent = null;
        if (actorModel.parent != null) {
            parent = (Group) allActors.get(actorModel.parent);
        }

        // Create animated sprite actor
        AnimatedSpriteActor actor = animatedSpriteActorPool.obtain();
        actor.setName(id);
        if (parent != null) {
            parent.addActor(actor);
        }
        allActors.put(id, actor);
        resizeableActorsRelative.add(actor);

        // Show each frame at least once?
        actor.setShowEachFrameAtLeastOnce(actorModel.showEachFrameAtLeastOnce);

        // Create states
        for (AnimatedSpriteActorModel.StateModel stateModel : actorModel.states) {
            createState(stateModel, actor);
        }

        // Set initial state
        actor.setState(actorModel.state);

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

    protected void createState(AnimatedSpriteActorModel.StateModel stateModel,
                               AnimatedSpriteActor actor) {
        // Create animation frames
        for (AnimatedSpriteActorModel.FrameModel frameModel : stateModel.animation) {
            int frameIndex = actor.addFrame(stateModel.id, frameModel.textureName,
                    frameModel.durationMillis / 1000.f, frameModel.centerOfMassX,
                    frameModel.centerOfMassY);
            SpriteGrid spriteGrid = actor.getFrame(stateModel.id, frameIndex).getSprite();
            spriteGrid.setScale(frameModel.scaleX, frameModel.scaleY);
            spriteGrid.setRotation(frameModel.rotation);
            spriteGrid.setOrigin(frameModel.originX, frameModel.originY);
            spriteGrid.setFlipX(frameModel.flipX);
            spriteGrid.setFlipY(frameModel.flipY);
        }

        // Loop animation must be set after at least one frame has been set; otherwise, the
        // animation returned is still null
        actor.getAnimationSequence(stateModel.id).setLoopAnimation(stateModel.loopAnimation);
    }

    @Override
    public void layout(BaseModel model, Object object, boolean forceLayout) {
        // Safeguard against duplicate calls
        if (!forceLayout && model.laidout) {
            return;
        }
        model.laidout = true;

        AnimatedSpriteActorModel actorModel = (AnimatedSpriteActorModel) model;
        AnimatedSpriteActor actor = (AnimatedSpriteActor) object;
        HashMap<String, Actor> allActors = sceneModelManager.getAllActors();

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
    public void addModelDependenciesToGraph(BaseModel model,
                                            DependencyGraph<BaseModel> dependencyGraph) {
        super.addModelDependenciesToGraph(model, dependencyGraph);

        AnimatedSpriteActorModel actorModel = (AnimatedSpriteActorModel) model;

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

    @Override
    public int getPreferenceForModel(BaseModel model) {
        if (model.getClass() == AnimatedSpriteActorModel.class) {
            return HANDLER_PREFERENCE_FOR_MODEL_EXACT_MATCH;
        }

        if (model instanceof AnimatedSpriteActorModel) {
            return HANDLER_PREFERENCE_FOR_MODEL_SUPERCLASS_MATCH;
        }

        return HANDLER_PREFERENCE_FOR_MODEL_NO_MATCH;
    }
}
