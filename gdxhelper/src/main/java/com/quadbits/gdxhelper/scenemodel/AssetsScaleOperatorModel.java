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

import java.util.Collection;
import java.util.Collections;

/**
 *
 */
public abstract class AssetsScaleOperatorModel extends AssetsScaleBaseModel {
    public AssetsScaleBaseModel[] children;

    @Override
    public void validate() {
        super.validate();

        if (children == null) {
            throw new NullPointerException("field 'children' must not be null");
        }

        for (int i = 0; i < children.length; i++) {
            children[i].validate();
        }
    }

    @Override
    public void addSubModelsTo(Collection<BaseModel> subModelsCollection) {
        // Add children
        Collections.addAll(subModelsCollection, children);
    }
}
