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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.utils.NonContinuousRendering;

/**
 *
 */
public abstract class BaseActor extends Actor implements Pool.Poolable, NonContinuousRendering {

    @Override
    public long getMaxSleepTime() {
        if (getActions().size > 0) {
            return 0;
        }

        return Long.MAX_VALUE;
    }

    @Override
    public void reset() {
        clear();
    }

    public float getRelX(float relativeX) {
        return getX() + getWidth() * relativeX;
    }

    public float getRelY(float relativeY) {
        return getY() + getHeight() * relativeY;
    }

    public float centerHorizontally() {
        return 0.5f * (Gdx.graphics.getWidth() - getWidth());
    }

    public float centerVertically() {
        return 0.5f * (Gdx.graphics.getHeight() - getHeight());
    }

}
