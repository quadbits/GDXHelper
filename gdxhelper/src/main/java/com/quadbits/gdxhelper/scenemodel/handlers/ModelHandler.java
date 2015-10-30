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

import com.quadbits.gdxhelper.scenemodel.BaseModel;
import com.quadbits.gdxhelper.scenemodel.LWPSceneModelManager;
import com.quadbits.gdxhelper.utils.DependencyGraph;

/**
 *
 */
public interface ModelHandler {
    void setSceneModelManager(LWPSceneModelManager sceneModelManager);

    LWPSceneModelManager getSceneModelManager();

    /**
     * Returns an int value expressing the preference of this handler for handling values.
     * Models are assigned to handlers which express a higher preference for it. In
     * general, a preference value of 0 or less means that the handler is unable of
     * handling that model; 1 means the handler is able of handling a superclass of the model;
     * and 2 means that the handler handles exactly that model class.
     *
     * @param model
     *         the model for which to express the preference
     *
     * @return
     */
    int getPreferenceForModel(BaseModel model);

    void addModelDependenciesToGraph(BaseModel model, DependencyGraph<BaseModel> dependencyGraph);

    /**
     * Handles the creation of the object associated to the model passed. If the model's
     * 'created' property is true, no new object is created (returns null). This method is
     * equivalent to <pre>create(model, null, false)</pre>
     *
     * @param model
     *         the model
     *
     * @return the object created from the model
     */
    Object create(BaseModel model);

    /**
     * Handles the creation of the object associated to the model passed. If 'forceCreation' is
     * true, the object will always be created, no matter what the model's 'created' property is.
     *
     * @param model
     *         the model
     * @param id
     *         the id of the object. If null, 'model.id' is used
     * @param forceCreation
     *         a boolean indicating if the object should be created no matter what
     *         the model's 'created' property is.
     *
     * @return the object created from the model
     */
    Object create(BaseModel model, String id, boolean forceCreation);

    /**
     * Handles the layout of the object associated to the model passed. If the model's
     * 'laidout' property is true, no layout is performed and the object remains intact. This
     * method is equivalent to <pre>layout(model, object, false)</pre>
     *
     * @param model
     *         the model
     * @param object
     *         the object associated with the model
     */
    void layout(BaseModel model, Object object);

    /**
     * Handles the layout of the object associated to the model passed. If 'forceLayout' is
     * true, the object will always be laid-out, no matter what the model's 'laidout' property is.
     *
     * @param model
     *         the model
     * @param object
     *         the object associated with the model
     * @param forceLayout
     *         a boolean indicating if the object should be laid-out no matter what
     *         the model's 'laidout' property is.
     */
    void layout(BaseModel model, Object object, boolean forceLayout);
}
