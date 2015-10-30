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
import com.quadbits.gdxhelper.utils.TimeManager;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.inject.Provider;

import dagger.Module;
import dagger.Provides;

/**
 *
 */
@Module
public class LWPGameModule {
    private final LWPGame game;

    public LWPGameModule(LWPGame game) {
        this.game = game;
    }

    @Provides
    @PerGame
    LWPGame provideGame() {
        return game;
    }

    @Provides
    @PerGame
    AssetManager provideAssetManager() {
        return new AssetManager();
    }

    @Provides
    @PerGame
    ShapeRenderer provideShapeRenderer() {
        return new ShapeRenderer();
    }

    @Provides
    @PerGame
    RandomXS128 provideRandomXS128() {
        return new RandomXS128();
    }

    @Provides
    @PerGame
    TimeManager provideTimeManager() {
        long periodMillis = TimeManager.ONE_DAY_PERIOD_IN_MILLIS;
        float tSunRise =
                TimeManager.getNormalizedTime(8, 0, 0, 0, TimeManager.ONE_DAY_PERIOD_IN_MILLIS);
        float tSunSet =
                TimeManager.getNormalizedTime(20, 0, 0, 0, TimeManager.ONE_DAY_PERIOD_IN_MILLIS);
        float tMoonRise =
                TimeManager.getNormalizedTime(21, 0, 0, 0, TimeManager.ONE_DAY_PERIOD_IN_MILLIS);
        float tMoonSet =
                TimeManager.getNormalizedTime(7, 0, 0, 0, TimeManager.ONE_DAY_PERIOD_IN_MILLIS);

        TimeManager timeManager = new TimeManager(tSunRise, tSunSet);
        timeManager.setMoonTimes(tMoonRise, tMoonSet);
        timeManager.setPeriodMillis(periodMillis);

        return timeManager;
    }

    @Provides
    ScheduledThreadPoolExecutor provideScheduledThreadPoolExecutor() {
        return new ScheduledThreadPoolExecutor(1);
    }

    @Provides
    InputMultiplexer provideInputMultiplexer() {
        return new InputMultiplexer();
    }

    //----------------------------------------------------------------
    // Stage elements pools
    //----------------------------------------------------------------

    //----------------------------------------------------------------
    // AnimatedSpriteActor
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<AnimatedSpriteActor> provideAnimatedSpriteActorPool(
            final Provider<AnimatedSpriteActor> provider) {
        return new Pool<AnimatedSpriteActor>() {
            @Override
            protected AnimatedSpriteActor newObject() {
                AnimatedSpriteActor object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // AnimatedSpriteGrid
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<AnimatedSpriteGrid> provideAnimatedSpriteGridPool(
            final Provider<AnimatedSpriteGrid> provider) {
        return new Pool<AnimatedSpriteGrid>() {
            @Override
            protected AnimatedSpriteGrid newObject() {
                AnimatedSpriteGrid object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // AnimationSequence
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<AnimatedSpriteGrid.AnimationSequence> provideAnimationSequencePool(
            final Provider<AnimatedSpriteGrid.AnimationSequence> provider) {
        return new Pool<AnimatedSpriteGrid.AnimationSequence>() {
            @Override
            protected AnimatedSpriteGrid.AnimationSequence newObject() {
                AnimatedSpriteGrid.AnimationSequence object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // BackgroundActor
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<BackgroundActor> provideBackgroundActorPool(final Provider<BackgroundActor> provider) {
        return new Pool<BackgroundActor>() {
            @Override
            protected BackgroundActor newObject() {
                BackgroundActor object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // Cloud
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<CloudsActor.Cloud> provideCloudPool(final Provider<CloudsActor.Cloud> provider) {
        return new Pool<CloudsActor.Cloud>() {
            @Override
            protected CloudsActor.Cloud newObject() {
                CloudsActor.Cloud object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // CloudsActor
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<CloudsActor> provideCloudsActorPool(final Provider<CloudsActor> provider) {
        return new Pool<CloudsActor>() {
            @Override
            protected CloudsActor newObject() {
                CloudsActor object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // CompositeActor
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<CompositeActor> provideCompositeActorPool(final Provider<CompositeActor> provider) {
        return new Pool<CompositeActor>() {
            @Override
            protected CompositeActor newObject() {
                CompositeActor object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // ContinuousLoopingScrollActor
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<ContinuousLoopingScrollActor> provideContinuousLoopingScrollActorPool(
            final Provider<ContinuousLoopingScrollActor> provider) {
        return new Pool<ContinuousLoopingScrollActor>() {
            @Override
            protected ContinuousLoopingScrollActor newObject() {
                ContinuousLoopingScrollActor object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // Frame
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<AnimatedSpriteGrid.Frame> provideFramePool(
            final Provider<AnimatedSpriteGrid.Frame> provider) {
        return new Pool<AnimatedSpriteGrid.Frame>() {
            @Override
            protected AnimatedSpriteGrid.Frame newObject() {
                AnimatedSpriteGrid.Frame object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // Layer
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<Layer> provideLayerPool(final Provider<Layer> provider) {
        return new Pool<Layer>() {
            @Override
            protected Layer newObject() {
                Layer object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // LinearTrajectoryController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<LinearTrajectoryController> provideLinearTrajectoryControllerPool(
            final Provider<LinearTrajectoryController> provider) {
        return new Pool<LinearTrajectoryController>() {
            @Override
            protected LinearTrajectoryController newObject() {
                LinearTrajectoryController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // MoonController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<MoonController> provideMoonControllerPool(final Provider<MoonController> provider) {
        return new Pool<MoonController>() {
            @Override
            protected MoonController newObject() {
                MoonController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // OnlyAtNightController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<OnlyAtNightController> provideOnlyAtNightControllerPool(
            final Provider<OnlyAtNightController> provider) {
        return new Pool<OnlyAtNightController>() {
            @Override
            protected OnlyAtNightController newObject() {
                OnlyAtNightController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // OnlyAtNightPeriodController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<OnlyAtNightPeriodController> provideOnlyAtNightPeriodControllerPool(
            final Provider<OnlyAtNightPeriodController> provider) {
        return new Pool<OnlyAtNightPeriodController>() {
            @Override
            protected OnlyAtNightPeriodController newObject() {
                OnlyAtNightPeriodController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // PanXOnScrollController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<PanXOnScrollController> providePanXOnScrollControllerPool(
            final Provider<PanXOnScrollController> provider) {
        return new Pool<PanXOnScrollController>() {
            @Override
            protected PanXOnScrollController newObject() {
                PanXOnScrollController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // PanYOnScrollController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<PanYOnScrollController> providePanYOnScrollControllerPool(
            final Provider<PanYOnScrollController> provider) {
        return new Pool<PanYOnScrollController>() {
            @Override
            protected PanYOnScrollController newObject() {
                PanYOnScrollController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // ParabolaAnimationController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<ParabolaAnimationController> provideParabolaAnimationControllerPool(
            final Provider<ParabolaAnimationController> provider) {
        return new Pool<ParabolaAnimationController>() {
            @Override
            protected ParabolaAnimationController newObject() {
                ParabolaAnimationController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // ParabolaAroundFixedPointController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<ParabolaAroundFixedPointController> provideParabolaAroundFixedPointControllerPool(
            final Provider<ParabolaAroundFixedPointController> provider) {
        return new Pool<ParabolaAroundFixedPointController>() {
            @Override
            protected ParabolaAroundFixedPointController newObject() {
                ParabolaAroundFixedPointController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // ParticleEffectActor
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<ParticleEffectActor> provideParticleEffectActorPool(
            final Provider<ParticleEffectActor> provider) {
        return new Pool<ParticleEffectActor>() {
            @Override
            protected ParticleEffectActor newObject() {
                ParticleEffectActor object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // PeriodicRotationController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<PeriodicRotationController> providePeriodicRotationControllerPool(
            final Provider<PeriodicRotationController> provider) {
        return new Pool<PeriodicRotationController>() {
            @Override
            protected PeriodicRotationController newObject() {
                PeriodicRotationController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // RotateOnScrollController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<RotateOnScrollController> provideRotateOnScrollControllerPool(
            final Provider<RotateOnScrollController> provider) {
        return new Pool<RotateOnScrollController>() {
            @Override
            protected RotateOnScrollController newObject() {
                RotateOnScrollController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // RotationSimpleController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<RotationSimpleController> provideRotationSimpleControllerPool(
            final Provider<RotationSimpleController> provider) {
        return new Pool<RotationSimpleController>() {
            @Override
            protected RotationSimpleController newObject() {
                RotationSimpleController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // ScreenDimActor
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<ScreenDimActor> provideScreenDimActorPool(final Provider<ScreenDimActor> provider) {
        return new Pool<ScreenDimActor>() {
            @Override
            protected ScreenDimActor newObject() {
                ScreenDimActor object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // SkyActor
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<SkyActor> provideSkyActorPool(final Provider<SkyActor> provider) {
        return new Pool<SkyActor>() {
            @Override
            protected SkyActor newObject() {
                SkyActor object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // SpriteActor
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<SpriteActor> provideSpriteActorPool(final Provider<SpriteActor> provider) {
        return new Pool<SpriteActor>() {
            @Override
            protected SpriteActor newObject() {
                SpriteActor object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // SpriteGrid
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<SpriteGrid> provideSpriteGridPool(final Provider<SpriteGrid> provider) {
        return new Pool<SpriteGrid>() {
            @Override
            protected SpriteGrid newObject() {
                SpriteGrid object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // SunController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<SunController> provideSunControllerPool(final Provider<SunController> provider) {
        return new Pool<SunController>() {
            @Override
            protected SunController newObject() {
                SunController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // TimePeriodTintController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<TimePeriodTintController> provideTimePeriodTintControllerPool(
            final Provider<TimePeriodTintController> provider) {
        return new Pool<TimePeriodTintController>() {
            @Override
            protected TimePeriodTintController newObject() {
                TimePeriodTintController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // TimePeriodVisibilityController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<TimePeriodVisibilityController> provideTimePeriodVisibilityControllerPool(
            final Provider<TimePeriodVisibilityController> provider) {
        return new Pool<TimePeriodVisibilityController>() {
            @Override
            protected TimePeriodVisibilityController newObject() {
                TimePeriodVisibilityController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }

    //----------------------------------------------------------------
    // TintAtNightController
    //----------------------------------------------------------------
    @Provides
    @PerGame
    Pool<TintAtNightController> provideTintAtNightControllerPool(
            final Provider<TintAtNightController> provider) {
        return new Pool<TintAtNightController>() {
            @Override
            protected TintAtNightController newObject() {
                TintAtNightController object = provider.get();
                object.setPool(this);
                return object;
            }
        };
    }
}
