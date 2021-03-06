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

import com.quadbits.gdxhelper.scenemodel.AssetsScaleBaseModel;
import com.quadbits.gdxhelper.scenemodel.AssetsScaleMaxOperatorModel;
import com.quadbits.gdxhelper.scenemodel.BaseModel;

import javax.inject.Inject;

/**
 *
 */
public class AssetsScaleMaxOperatorModelHandler extends ModelHandlerBaseImpl
        implements AssetsScaleBaseModelHandler {

    @Inject
    public AssetsScaleMaxOperatorModelHandler() {
        super();
    }

    @Override
    public float getAssetsScale(AssetsScaleBaseModel assetsScaleBaseModel) {
        AssetsScaleMaxOperatorModel assetsScaleMaxOperatorModel =
                (AssetsScaleMaxOperatorModel) assetsScaleBaseModel;

        float assetsScale = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < assetsScaleMaxOperatorModel.children.length; i++) {
            AssetsScaleBaseModel child = assetsScaleMaxOperatorModel.children[i];
            float tmpAssetsScale = sceneModelManager.getAssetsScale(child);
            assetsScale = Math.max(tmpAssetsScale, assetsScale);
        }

        return assetsScale;
    }

    @Override
    public int getPreferenceForModel(BaseModel model) {
        if (model.getClass() == AssetsScaleMaxOperatorModel.class) {
            return HANDLER_PREFERENCE_FOR_MODEL_EXACT_MATCH;
        }

        if (model instanceof AssetsScaleMaxOperatorModel) {
            return HANDLER_PREFERENCE_FOR_MODEL_SUPERCLASS_MATCH;
        }

        return HANDLER_PREFERENCE_FOR_MODEL_NO_MATCH;
    }

}
