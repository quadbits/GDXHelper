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
public class PeriodicRotationControllerModel extends ControllerModel {
    public Float minAngle;
    public Float maxAngle;
    public Long rotationDuration;
    public long stallDuration;
    public boolean decelerate;

    @Override
    public void validate() {
        super.validate();

        if (minAngle == null) {
            throw new NullPointerException("field 'minAngle' must not be null");
        }
        if (maxAngle == null) {
            throw new NullPointerException("field 'maxAngle' must not be null");
        }
        if (rotationDuration == null) {
            throw new NullPointerException("field 'rotationDuration' must not be null");
        }

        if (rotationDuration <= 0) {
            throw new IllegalArgumentException("field 'rotationDuration' must be > 0");
        }

        if (stallDuration < 0) {
            throw new IllegalArgumentException("field 'stallDuration' must be >= 0");
        }
    }
}
