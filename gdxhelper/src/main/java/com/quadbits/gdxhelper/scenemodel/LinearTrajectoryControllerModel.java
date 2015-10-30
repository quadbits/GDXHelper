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
public class LinearTrajectoryControllerModel extends ControllerModel {
    public float avgSpeedMillis;
    public float stdSpeedMillis;
    public String speedRelativeTo;
    public String speedRelativeToVariable;

    public float sourceX; // [0,1]
    public float sourceY; // [0,1]
    public String sourceXRelativeTo; // id
    public String sourceYRelativeTo; // id
    public Float sourceX2; // [0,1]
    public Float sourceY2; // [0,1]
    public String sourceX2RelativeTo; // id
    public String sourceY2RelativeTo; // id

    public float targetX; // [0,1]
    public float targetY; // [0,1]
    public String targetXRelativeTo; // id
    public String targetYRelativeTo; // id
    public Float targetX2; // [0,1]
    public Float targetY2; // [0,1]
    public String targetX2RelativeTo; // id
    public String targetY2RelativeTo; // id

    public Float referenceAngleDegrees;
    public Float referenceMaxDeviationAngleDegrees;

    public boolean reversable;
    public long avgOutOfSceneTimeMillis;
    public float stdOutOfSceneTimeMillis;
    public boolean adjustActorRotation;

    // use 'relativeTo' width
    public static final String SPEED_VARIABLE_WIDTH = "WIDTH";

    // use 'relativeTo' height
    public static final String SPEED_VARIABLE_HEIGHT = "HEIGHT";

    // use 'relativeTo' max btw width and height
    public static final String SPEED_VARIABLE_MAX = "MAX";

    // use 'relativeTo' min btw width and height
    public static final String SPEED_VARIABLE_MIN = "MIN";

    @Override
    public void validate() {
        super.validate();

        if (speedRelativeTo != null && speedRelativeToVariable == null) {
            throw new NullPointerException(
                    "field 'speedRelativeToVariable' must not be null if field 'speedRelativeTo' " +
                            "is specified");
        }
        if ((referenceAngleDegrees != null && referenceMaxDeviationAngleDegrees == null) ||
                (referenceAngleDegrees == null && referenceMaxDeviationAngleDegrees != null)) {
            throw new NullPointerException(
                    "fields 'referenceAngleDegrees' and 'referenceMaxDeviationAngleDegrees' " +
                            "must either be both null or both not null");
        }
        if (sourceX2 == null) {
            sourceX2 = sourceX;
        }
        if (sourceY2 == null) {
            sourceY2 = sourceY;
        }
        if (targetX2 == null) {
            targetX2 = targetX;
        }
        if (targetY2 == null) {
            targetY2 = targetY;
        }
        if (sourceXRelativeTo == null) {
            sourceXRelativeTo = SCREEN_ID;
        }
        if (sourceYRelativeTo == null) {
            sourceYRelativeTo = SCREEN_ID;
        }
        if (targetXRelativeTo == null) {
            targetXRelativeTo = SCREEN_ID;
        }
        if (targetYRelativeTo == null) {
            targetYRelativeTo = SCREEN_ID;
        }
        if (sourceX2RelativeTo == null) {
            sourceX2RelativeTo = sourceXRelativeTo;
        }
        if (sourceY2RelativeTo == null) {
            sourceY2RelativeTo = sourceYRelativeTo;
        }
        if (targetX2RelativeTo == null) {
            targetX2RelativeTo = targetXRelativeTo;
        }
        if (targetY2RelativeTo == null) {
            targetY2RelativeTo = targetYRelativeTo;
        }
        if (speedRelativeToVariable != null) {
            if (speedRelativeToVariable.compareTo(SPEED_VARIABLE_WIDTH) != 0 &&
                    speedRelativeToVariable.compareTo(SPEED_VARIABLE_HEIGHT) != 0 &&
                    speedRelativeToVariable.compareTo(SPEED_VARIABLE_MAX) != 0 &&
                    speedRelativeToVariable.compareTo(SPEED_VARIABLE_MIN) != 0) {
                throw new IllegalArgumentException("Unrecognized value '" +
                        speedRelativeToVariable + "' for field 'speedRelativeToVariable'");
            }
        }
    }
}
