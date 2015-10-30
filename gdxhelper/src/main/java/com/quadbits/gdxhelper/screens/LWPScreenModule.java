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
package com.quadbits.gdxhelper.screens;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.quadbits.gdxhelper.LWPStage;
import com.quadbits.gdxhelper.actors.AutomaticScrollingActor;

import javax.inject.Provider;

import dagger.Module;
import dagger.Provides;

/**
 *
 */
@Module
public class LWPScreenModule {
    private final LWPScreen screen;

    public LWPScreenModule(LWPScreen screen) {
        this.screen = screen;
    }

    @Provides
    @PerGameScreen
    LWPScreen provideScreen() {
        return screen;
    }

    @Provides
    @PerGameScreen
    LWPStage provideLWPStage() {
        LWPStage stage = new LWPStage();
        stage.setViewport(new ScreenViewport());
        return stage;
    }

    //----------------------------------------------------------------
    // AutomaticScrollingActor
    //----------------------------------------------------------------
    @Provides
    @PerGameScreen
    Pool<AutomaticScrollingActor> provideAutomaticScrollingActorPool(
            final Provider<AutomaticScrollingActor> provider) {
        return new Pool<AutomaticScrollingActor>() {
            @Override
            protected AutomaticScrollingActor newObject() {
                AutomaticScrollingActor object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

}
