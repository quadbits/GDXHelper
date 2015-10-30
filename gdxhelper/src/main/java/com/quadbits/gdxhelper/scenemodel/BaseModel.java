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

import com.quadbits.gdxhelper.scenemodel.handlers.ModelHandler;

import java.util.Collection;

/**
 *
 */
public abstract class BaseModel {
    public String id;
    public transient String parent;
    public transient ModelHandler handler;
    public transient boolean created;
    public transient boolean laidout;

    public static final String SCREEN_ID = "SCREEN";

    public void validate() {
    }

    public void addSubModelsTo(Collection<BaseModel> subModelsCollection) {
        // By default, do nothing
    }
}
