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
package com.quadbits.gdxhelper.scenemodel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Disposable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.quadbits.gdxhelper.LWPStage;
import com.quadbits.gdxhelper.controllers.Controller;
import com.quadbits.gdxhelper.scenemodel.handlers.AssetsScaleModelHandler;
import com.quadbits.gdxhelper.scenemodel.handlers.ModelHandler;
import com.quadbits.gdxhelper.utils.DependencyGraph;
import com.quadbits.gdxhelper.utils.Recyclable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 */
public class LWPSceneModelManager implements Disposable {
    boolean loaded;

    // Model-related fields
    SceneModel sceneModel;
    HashMap<String, BaseModel> allModelsWithId;
    HashMap<String, ActorModel> allActorModels;
    HashMap<String, ActorModel> nonGroupActorModels;
    HashMap<String, ActorModel> groupActorModels;
    HashMap<String, ParticleEffectPool> particleEffectPools;

    Queue<BaseModel> modelProcessingQueue;

    DependencyGraph<BaseModel> modelDependencyGraph;

    Gson gson;

    // Stage-related fields
    ArrayList<Actor> resizeableActorsAbsolute;
    ArrayList<Actor> resizeableActorsRelative;
    HashMap<String, Actor> allActors;
    HashMap<String, Controller> allControllers;

    // Injectable fields
    @Inject
    LWPStage stage;

    @Inject
    @Named("SceneModelManagerHandlers")
    List<ModelHandler> modelHandlers;

    @Inject
    @Named("SceneModelManagerGSONSubtypes")
    List<Class<? extends AssetsScaleBaseModel>> gsonAssetsScaleBaseModelSubtypes;

    @Inject
    @Named("SceneModelManagerGSONSubtypes")
    List<Class<? extends ActorModel>> gsonActorModelSubtypes;

    @Inject
    @Named("SceneModelManagerGSONSubtypes")
    List<Class<? extends ControllerModel>> gsonControllerModelSubtypes;

    @Inject
    public LWPSceneModelManager() {
        init();
    }

    private void init() {
        loaded = false;

        if (allModelsWithId == null) {
            allModelsWithId = new HashMap<String, BaseModel>();
        }
        allModelsWithId.clear();

        if (allActorModels == null) {
            allActorModels = new HashMap<String, ActorModel>();
        }
        allActorModels.clear();

        if (nonGroupActorModels == null) {
            nonGroupActorModels = new HashMap<String, ActorModel>();
        }
        nonGroupActorModels.clear();

        if (groupActorModels == null) {
            groupActorModels = new HashMap<String, ActorModel>();
        }
        groupActorModels.clear();

        if (particleEffectPools == null) {
            particleEffectPools = new HashMap<String, ParticleEffectPool>();
        }
        particleEffectPools.clear();

        if (modelProcessingQueue == null) {
            modelProcessingQueue = new LinkedList<BaseModel>();
        }
        modelProcessingQueue.clear();

        if (modelDependencyGraph == null) {
            modelDependencyGraph = new DependencyGraph<BaseModel>();
        }
        modelDependencyGraph.clear();

        if (resizeableActorsAbsolute == null) {
            resizeableActorsAbsolute = new ArrayList<Actor>();
        }
        resizeableActorsAbsolute.clear();

        if (resizeableActorsRelative == null) {
            resizeableActorsRelative = new ArrayList<Actor>();
        }
        resizeableActorsRelative.clear();

        if (allActors == null) {
            allActors = new HashMap<String, Actor>();
        }
        allActors.clear();

        if (allControllers == null) {
            allControllers = new HashMap<String, Controller>();
        }
        allControllers.clear();

        if (modelHandlers == null) {
            modelHandlers = new ArrayList<ModelHandler>();
        }
        modelHandlers.clear();
    }

    public void loadSceneModelFromJSON(String sceneModelJSONFilename) {
        // Parse JSON file to create a SceneModel
        Gson gson = getGson();
        String sceneModelJSON = Gdx.files.internal(sceneModelJSONFilename).readString();
        try {
            // Parse the JSON file to a tmp variable. If everything goes fine,
            // the new model replaces the old one. If an exception is thrown, we just simply
            // keep the old one and print the stacktrace
            SceneModel tmpSceneModel = gson.fromJson(sceneModelJSON, SceneModel.class);
            sceneModel = tmpSceneModel;
        } catch (JsonSyntaxException e) {
            Gdx.app.error(this.getClass().getSimpleName(), e.getMessage());
            return;
        }

        // Validate model
        sceneModel.validate();

        // After validation, store layers and actors in 2 HashMaps for quick access
        allActorModels.clear();
        groupActorModels.clear();
        nonGroupActorModels.clear();
        modelProcessingQueue.clear();

        // Initialize handlers
        for (ModelHandler modelHandler : modelHandlers) {
            modelHandler.setSceneModelManager(this);
        }

        // Traverse all model elements, starting from the root SceneModel
        modelProcessingQueue.add(sceneModel);
        while (!modelProcessingQueue.isEmpty()) {
            BaseModel model = modelProcessingQueue.poll();

            // Assign a handler to the model
            int maxPreferenceForModel = 0;
            for (ModelHandler handler : modelHandlers) {
                int preferenceForModel = handler.getPreferenceForModel(model);
                if (preferenceForModel > maxPreferenceForModel) {
                    model.handler = handler;
                    maxPreferenceForModel = preferenceForModel;
                }
            }
            if (maxPreferenceForModel == 0) {
                throw new IllegalArgumentException(
                        "No handler registered for model type " + model.getClass().getSimpleName());
            }

            // If it's a model with id, add it to the corresponding HashMap
            if (model.id != null) {
                allModelsWithId.put(model.id, model);
            }

            // If it's an actor model, perform some additional checks
            if (model instanceof ActorModel) {
                ActorModel actorModel = (ActorModel) model;

                // Put actor in a HashMap for faster access
                allActorModels.put(actorModel.id, actorModel);

                // If it's a group, put it in the group's HashMap and process all of its children
                if (actorModel instanceof GroupModel) {
                    groupActorModels.put(actorModel.id, actorModel);
                }

                // It's an actor, put it in the actors HashMap
                else {
                    nonGroupActorModels.put(actorModel.id, actorModel);
                }
            }

            // Add all sub-models to the processing queue
            model.addSubModelsTo(modelProcessingQueue);

            // Set loaded true
            loaded = true;
        }
    }

    public float getAssetsScale() {
        return getAssetsScale(sceneModel.assetsScale);
    }

    public float getAssetsScale(AssetsScaleBaseModel assetsScaleBaseModel) {
        return ((AssetsScaleModelHandler) assetsScaleBaseModel.handler)
                .getAssetsScale(assetsScaleBaseModel);
    }

    public void createStage() {
        // Reset models
        for (BaseModel model : allModelsWithId.values()) {
            model.created = false;
        }
        sceneModel.created = false;

        modelProcessingQueue.clear();
        modelProcessingQueue.add(sceneModel);
        while (!modelProcessingQueue.isEmpty()) {
            BaseModel model = modelProcessingQueue.poll();
            if (model == null) {
                continue;
            }

            Object element = model.handler.create(model);

            // Special case for actors, because they need to be added to the scene if requested
            if (element instanceof Actor) {
                // Add to parent?
                if (((ActorModel) model).addToParent) {
                    Group parent;
                    if (model.parent != null) {
                        parent = (Group) allActors.get(model.parent);
                    } else {
                        parent = stage.getRoot();
                    }
                    parent.addActor((Actor) element);
                }
            }
        }
    }

    public Object createElement(BaseModel modelElement) {
        if (modelElement == null) {
            return null;
        }

        return modelElement.handler.create(modelElement);
    }

    public void scaleActors(float assetsScaleAbsolute, float assetsScaleRelative) {
        for (Actor actor : resizeableActorsAbsolute) {
            actor.scaleBy(assetsScaleAbsolute);
        }
        for (Actor actor : resizeableActorsRelative) {
            actor.scaleBy(assetsScaleRelative);
        }
    }

    public void layoutStage() {
        // Reset the layout status of all models
        for (BaseModel model : allModelsWithId.values()) {
            model.laidout = false;
        }

        //-------------------------------------------------------------------------------
        // IMPORTANT! layout in this order: first actors, then groups, then controllers.
        // Groups may need to re-position the actors they contain after calculating
        // their size, but may also need actors' sizes and positions to calculate their
        // own size; thus, actors need to be processed first. Controllers may also need
        // the sizes and positions of both actors and groups, so they are processed in
        // the last place
        //-------------------------------------------------------------------------------

        // First, layout non-layer actors
        addElementsToProcessQueueInTopologicalOrder(nonGroupActorModels.values());
        while (!modelProcessingQueue.isEmpty()) {
            layoutElement(modelProcessingQueue.remove());
        }

        // Then, layout layers
        addElementsToProcessQueueInTopologicalOrder(groupActorModels.values());
        while (!modelProcessingQueue.isEmpty()) {
            layoutElement(modelProcessingQueue.remove());
        }

        // Finally, process controllers, in case there are some that depend on some actor's
        // properties that are set on layout
        addElementsToProcessQueueInTopologicalOrder(Arrays.asList(sceneModel.controllers));
        while (!modelProcessingQueue.isEmpty()) {
            layoutElement(modelProcessingQueue.remove());
        }
    }

    protected void addElementsToProcessQueueInTopologicalOrder(
            Collection<? extends BaseModel> modelList) {
        // Clear structures
        modelDependencyGraph.clear();
        modelProcessingQueue.clear();

        for (BaseModel model : modelList) {
            modelDependencyGraph.addNode(model);
            model.handler.addModelDependenciesToGraph(model, modelDependencyGraph);
        }

        modelDependencyGraph.insertValuesInTopologicalOrderInto(modelProcessingQueue);
    }

    protected void layoutElement(BaseModel modelElement) {
        if (modelElement == null) {
            return;
        }

        Object element = null;
        if (modelElement instanceof ActorModel) {
            element = allActors.get(modelElement.id);
        } else if (modelElement instanceof ControllerModel) {
            element = allControllers.get(modelElement.id);
        }
        modelElement.handler.layout(modelElement, element);
    }

    public void clearStage() {
        for (Actor actor : allActors.values()) {
            if (actor instanceof Recyclable) {
                ((Recyclable) actor).free();
            }
        }
        resizeableActorsAbsolute.clear();
        resizeableActorsRelative.clear();
        allActors.clear();

        particleEffectPools.clear();

        for (Controller controller : allControllers.values()) {
            if (controller instanceof Recyclable) {
                ((Recyclable) controller).free();
            }
        }
        allControllers.clear();
    }

    @Override
    public void dispose() {
    }

    public SceneModel getSceneModel() {
        return sceneModel;
    }

    public void setSceneModel(SceneModel sceneModel) {
        this.sceneModel = sceneModel;
    }

    public ArrayList<Actor> getResizeableActorsAbsolute() {
        return resizeableActorsAbsolute;
    }

    public ArrayList<Actor> getResizeableActorsRelative() {
        return resizeableActorsRelative;
    }

    public HashMap<String, Actor> getAllActors() {
        return allActors;
    }

    public HashMap<String, Controller> getAllControllers() {
        return allControllers;
    }

    public Queue<BaseModel> getModelProcessingQueue() {
        return modelProcessingQueue;
    }

    public BaseModel getModelById(String id) {
        return allModelsWithId.get(id);
    }

    public HashMap<String, ParticleEffectPool> getParticleEffectPools() {
        return particleEffectPools;
    }

    protected Gson getGson() {
        if (gson != null) {
            return gson;
        }

        // AssetsScale types
        RuntimeTypeAdapterFactory<AssetsScaleBaseModel> assetsScaleTypeFactory =
                RuntimeTypeAdapterFactory.of(AssetsScaleBaseModel.class, "type");
        for (Class<? extends AssetsScaleBaseModel> modelSubtype :
                gsonAssetsScaleBaseModelSubtypes) {
            assetsScaleTypeFactory.registerSubtype(modelSubtype);
        }

        // Actor types
        RuntimeTypeAdapterFactory<ActorModel> actorTypeFactory =
                RuntimeTypeAdapterFactory.of(ActorModel.class, "type");
        for (Class<? extends ActorModel> modelSubtype : gsonActorModelSubtypes) {
            actorTypeFactory.registerSubtype(modelSubtype);
        }

        // Controller types
        RuntimeTypeAdapterFactory<ControllerModel> controllerTypeFactory =
                RuntimeTypeAdapterFactory.of(ControllerModel.class, "type");
        for (Class<? extends ControllerModel> modelSubtype : gsonControllerModelSubtypes) {
            controllerTypeFactory.registerSubtype(modelSubtype);
        }

        // Create gson
        gson = new GsonBuilder().registerTypeAdapterFactory(assetsScaleTypeFactory)
                .registerTypeAdapterFactory(actorTypeFactory)
                .registerTypeAdapterFactory(controllerTypeFactory).setPrettyPrinting().create();

        return gson;
    }

    public boolean isLoaded() {
        return loaded;
    }
}
