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
public class AutomaticScrollingActorModel extends ActorModel {
    public String scrollTarget;
    public String scrollVariable;
    public Float scrollAccel;
    public boolean debug;

    public static final String SCROLL_VARIABLE_X = "SCROLL_X";
    public static final String SCROLL_VARIABLE_Y = "SCROLL_Y";

    @Override
    public void validate() {
        super.validate();

        if (scrollTarget == null) {
            throw new NullPointerException("field 'scrollTarget' must not be null");
        }
        if (scrollVariable == null) {
            throw new NullPointerException("field 'scrollVariable' must not be null");
        }
        if (scrollAccel == null) {
            throw new NullPointerException("field 'scrollAccel' must not be null");
        }

        if (!scrollVariable.equals(SCROLL_VARIABLE_X) &&
                !scrollVariable.equals(SCROLL_VARIABLE_Y)) {
            throw new IllegalArgumentException(
                    "field 'scrollVariable' must be either '" + SCROLL_VARIABLE_X + " or '" +
                            SCROLL_VARIABLE_Y + "' ");
        }
    }
}
