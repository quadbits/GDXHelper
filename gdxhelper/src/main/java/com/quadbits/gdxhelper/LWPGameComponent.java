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
package com.quadbits.gdxhelper;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.Pool;
import com.quadbits.gdxhelper.actors.AnimatedSpriteActor;
import com.quadbits.gdxhelper.actors.BackgroundActor;
import com.quadbits.gdxhelper.actors.CloudsActor;
import com.quadbits.gdxhelper.actors.CompositeActor;
import com.quadbits.gdxhelper.actors.ContinuousLoopingScrollActor;
import com.quadbits.gdxhelper.actors.Layer;
import com.quadbits.gdxhelper.actors.ParticleEffectActor;
import com.quadbits.gdxhelper.actors.ScreenDimActor;
import com.quadbits.gdxhelper.actors.SkyActor;
import com.quadbits.gdxhelper.actors.SpriteActor;
import com.quadbits.gdxhelper.controllers.LinearTrajectoryController;
import com.quadbits.gdxhelper.controllers.MoonController;
import com.quadbits.gdxhelper.controllers.OnlyAtNightController;
import com.quadbits.gdxhelper.controllers.OnlyAtNightPeriodController;
import com.quadbits.gdxhelper.controllers.PanXOnScrollController;
import com.quadbits.gdxhelper.controllers.PanYOnScrollController;
import com.quadbits.gdxhelper.controllers.ParabolaAnimationController;
import com.quadbits.gdxhelper.controllers.ParabolaAroundFixedPointController;
import com.quadbits.gdxhelper.controllers.PeriodicRotationController;
import com.quadbits.gdxhelper.controllers.RotateOnScrollController;
import com.quadbits.gdxhelper.controllers.RotationSimpleController;
import com.quadbits.gdxhelper.controllers.SunController;
import com.quadbits.gdxhelper.controllers.TimePeriodTintController;
import com.quadbits.gdxhelper.controllers.TimePeriodVisibilityController;
import com.quadbits.gdxhelper.controllers.TintAtNightController;
import com.quadbits.gdxhelper.utils.AnimatedSpriteGrid;
import com.quadbits.gdxhelper.utils.SpriteGrid;
import com.quadbits.gdxhelper.utils.TextureAtlasProxy;
import com.quadbits.gdxhelper.utils.TimeManager;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import dagger.Component;

/**
 *
 */
@PerGame
@Component(modules = {LWPGameModule.class})
public interface LWPGameComponent {
    LWPGame getGame();

    AssetManager getAssetManager();

    ShapeRenderer getShapeRenderer();

    RandomXS128 getRandomXS128();

    TimeManager getTimeManager();

    ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor();

    InputMultiplexer getInputMultiplexer();

    TextureAtlasProxy getTextureAtlasProxy();

    Pool<AnimatedSpriteActor> getAnimatedSpriteActorPool();

    Pool<AnimatedSpriteGrid> getAnimatedSpriteGridPool();

    Pool<AnimatedSpriteGrid.AnimationSequence> getAnimationSequencePool();

    Pool<BackgroundActor> getBackgroundActorPool();

    Pool<CloudsActor.Cloud> getCloudPool();

    Pool<CloudsActor> getCloudsActorPool();

    Pool<CompositeActor> getCompositeActorPool();

    Pool<ContinuousLoopingScrollActor> getContinuousLoopingScrollActorPool();

    Pool<AnimatedSpriteGrid.Frame> getFramePool();

    Pool<Layer> getLayerPool();

    Pool<LinearTrajectoryController> getLinearTrajectoryControllerPool();

    Pool<MoonController> getMoonControllerPool();

    Pool<OnlyAtNightController> getOnlyAtNightControllerPool();

    Pool<OnlyAtNightPeriodController> getOnlyAtNightPeriodControllerPool();

    Pool<PanXOnScrollController> getPanXOnScrollControllerPool();

    Pool<PanYOnScrollController> getPanYOnScrollControllerPool();

    Pool<ParabolaAnimationController> getParabolaAnimationControllerPool();

    Pool<ParabolaAroundFixedPointController> getParabolaAroundFixedPointControllerPool();

    Pool<ParticleEffectActor> getParticleEffectActorPool();

    Pool<PeriodicRotationController> getPeriodicRotationControllerPool();

    Pool<RotateOnScrollController> getRotateOnScrollControllerPool();

    Pool<RotationSimpleController> getRotationSimpleControllerPool();

    Pool<ScreenDimActor> getScreenDimActorPool();

    Pool<SkyActor> getSkyActorPool();

    Pool<SpriteActor> getSpriteActorPool();

    Pool<SpriteGrid> getSpriteGridPool();

    Pool<SunController> getSunControllerPool();

    Pool<TimePeriodTintController> getTimePeriodTintControllerPool();

    Pool<TimePeriodVisibilityController> getTimePeriodVisibilityControllerPool();

    Pool<TintAtNightController> getTintAtNightControllerPool();
}
