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
package com.quadbits.gdxhelper.actors;

import com.badlogic.gdx.utils.Array;
import com.quadbits.gdxhelper.controllers.Controller;
import com.quadbits.gdxhelper.controllers.NonContinuousRenderingController;

/**
 *
 */
public abstract class ControllableGroup extends BaseGroup {
    protected Array<Controller> controllers;

    public ControllableGroup() {
        super();
        controllers = new Array<Controller>();
    }

    @Override
    public void reset() {
        controllers.clear();
    }

    @Override
    public void act(float deltaSeconds) {
        super.act(deltaSeconds);

        for (Controller controller : controllers) {
            controller.control(this, deltaSeconds);
        }
    }

    /**
     * Add a controller to the list of controllers acting on this actor
     *
     * @param controller
     *         The controller to add
     */
    public void addController(Controller controller) {
        controllers.add(controller);
    }

    /**
     * Removes the first instance of controller from the list of controllers of this actor.
     *
     * @param controller
     *         The controller to remove
     * @param identity
     *         If true, == comparison will be used. If false, .equals() comparison will be used.
     */
    public void removeController(Controller controller, boolean identity) {
        controllers.removeValue(controller, identity);
    }

    @Override
    public long getMaxSleepTime() {
        long maxSleepTimeMillis = super.getMaxSleepTime();

        for (Controller controller : controllers) {
            if (controller instanceof NonContinuousRenderingController) {
                long controllerMaxSleepTimeMillis =
                        ((NonContinuousRenderingController) controller).getMaxSleepTime(this);
                if (controllerMaxSleepTimeMillis < maxSleepTimeMillis) {
                    maxSleepTimeMillis = controllerMaxSleepTimeMillis;
                }
            }
        }

        return maxSleepTimeMillis;
    }
}
