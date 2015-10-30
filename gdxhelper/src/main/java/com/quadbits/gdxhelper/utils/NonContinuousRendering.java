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
package com.quadbits.gdxhelper.utils;

/**
 *
 */
public interface NonContinuousRendering {
    /**
     * Calculates the maximum time that a game could sleep without redrawing in order to
     * prevent visible changes in this object to be too abrupt.
     *
     * @return the max. sleep time, in milliseconds
     */
    public long getMaxSleepTime();
}
