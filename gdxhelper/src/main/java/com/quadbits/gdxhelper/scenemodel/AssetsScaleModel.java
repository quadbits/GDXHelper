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

/**
 *
 */
public class AssetsScaleModel extends AssetsScaleBaseModel {
    public Float multiplier;
    public ScreenProperty screenProperty;

    public static enum ScreenProperty {
        WIDTH, HEIGHT;

        @Override
        public String toString() {
            switch (this) {
                case WIDTH:
                    return "WIDTH";
                case HEIGHT:
                    return "HEIGHT";
                default:
                    throw new IllegalArgumentException("Unrecognized ScreenProperty value");
            }
        }

    }

    @Override
    public void validate() {
        super.validate();

        if (multiplier == null) {
            throw new NullPointerException("field 'multiplier' must not be null");
        }

        if (multiplier <= 0) {
            throw new IllegalArgumentException("field 'multiplier' must be a positive number");
        }
    }
}
