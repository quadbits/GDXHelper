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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class TimeManagerTest {

    float tSunRise;
    float tSunSet;
    float tTwilight;
    float tMidday;
    float tMidnight;
    float tPreDusk;
    float tPostDusk;
    float tPreDawn;
    float tPostDawn;

    float twilightPreSunRiseWidth;
    float twilightPostSunRiseWidth;
    float preMiddayWidth;
    float postMiddayWidth;
    float twilightPreSunSetWidth;
    float twilightPostSunSetWidth;
    float preMidnightWidth;
    float postMidnightWidth;
    float sunTravelWidth;
    float moonTravelWidth;

    TimeManager timeManager;

    @Before
    public void setUp() {
        tSunRise = 0.3f;
        tSunSet = 0.9f;
        tTwilight = TimeManager.TTWILIGHT_DEFAULT_VALUE;
        tMidday = 0.6f;
        tMidnight = 0.1f;
        tPreDusk = tSunRise - tTwilight;
        tPostDusk = tSunRise + tTwilight;
        tPreDawn = tSunSet - tTwilight;
        tPostDawn = tSunSet + tTwilight;

        twilightPreSunRiseWidth = tTwilight;
        twilightPostSunRiseWidth = tTwilight;
        twilightPreSunSetWidth = tTwilight;
        twilightPostSunSetWidth = tTwilight;
        preMiddayWidth = tMidday - tPostDusk;
        postMiddayWidth = preMiddayWidth;
        postMidnightWidth = tPreDusk - tMidnight;
        preMidnightWidth = postMidnightWidth;
        if (tSunRise < tSunSet) {
            sunTravelWidth = tSunSet - tSunRise;
        } else {
            sunTravelWidth = 1 - (tSunRise - tSunSet);
        }
        moonTravelWidth = 1 - (sunTravelWidth + 2 * tTwilight);

        timeManager = new TimeManager(tSunRise, tSunSet);
    }

    @Test
    public void testConstructor() {
        // Arrange
        float deltaError = 0.001f;

        // Assert
        Assert.assertEquals(tMidday, timeManager.getTMidday(), deltaError);
        Assert.assertEquals(tMidnight, timeManager.getTMidnight(), deltaError);
        Assert.assertEquals(tPreDusk, timeManager.getTPreDusk(), deltaError);
        Assert.assertEquals(tPostDusk, timeManager.getTPostDusk(), deltaError);
        Assert.assertEquals(tPreDawn, timeManager.getTPreDawn(), deltaError);
        Assert.assertEquals(tPostDawn, timeManager.getTPostDawn(), deltaError);
        Assert.assertEquals(9, timeManager.dayIntervals.size);
        Assert.assertEquals(TimeManager.DayPeriod.PRE_MIDNIGHT, timeManager.dayIntervals.get(0).id);
        Assert.assertEquals(sunTravelWidth, timeManager.getSunTravelWidth(), deltaError);
        Assert.assertEquals(moonTravelWidth, timeManager.getMoonTravelWidth(), deltaError);
    }

    @Test
    public void testPreMidnight() {
        // Arrange
        float t;
        float expectedTPeriod;
        TimeManager.DayPeriod expectedDayPeriod = TimeManager.DayPeriod.PRE_MIDNIGHT;
        float xDeltaError = 0.001f;
        float yDeltaError = 0.001f;

        // Act + Assert
        t = tPostDawn + xDeltaError;
        expectedTPeriod = xDeltaError / preMidnightWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tPostDawn + 0.02f;
        expectedTPeriod = 0.02f / preMidnightWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = 1.f;
        expectedTPeriod = (1.0f - tPostDawn) / preMidnightWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = 0.f;
        expectedTPeriod = (1.0f - tPostDawn) / preMidnightWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = 0.05f;
        expectedTPeriod = (1.0f - tPostDawn + t) / preMidnightWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tMidnight - xDeltaError;
        expectedTPeriod = (preMidnightWidth - xDeltaError) / preMidnightWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);
    }

    @Test
    public void testPostMidnight() {
        // Arrange
        float t;
        float expectedTPeriod;
        TimeManager.DayPeriod expectedDayPeriod = TimeManager.DayPeriod.POST_MIDNIGHT;
        float xDeltaError = 0.001f;
        float yDeltaError = 0.001f;

        // Act + Assert
        t = tMidnight + xDeltaError;
        expectedTPeriod = xDeltaError / postMidnightWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tMidnight + 0.1f;
        expectedTPeriod = 0.1f / postMidnightWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tPreDusk - xDeltaError;
        expectedTPeriod = (postMidnightWidth - xDeltaError) / postMidnightWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);
    }

    @Test
    public void testTwilightPreSunRise() {
        // Arrange
        float t;
        float expectedTPeriod;
        TimeManager.DayPeriod expectedDayPeriod = TimeManager.DayPeriod.TWILIGHT_PRE_SUNRISE;
        float xDeltaError = 0.001f;
        float yDeltaError = 0.001f;

        // Act + Assert
        t = tPreDusk + xDeltaError;
        expectedTPeriod = xDeltaError / twilightPreSunRiseWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tPreDusk + 0.02f;
        expectedTPeriod = 0.02f / twilightPreSunRiseWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tSunRise - xDeltaError;
        expectedTPeriod = (twilightPreSunRiseWidth - xDeltaError) / twilightPreSunRiseWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);
    }

    @Test
    public void testTwilightPostSunRise() {
        // Arrange
        float t;
        float expectedTPeriod;
        TimeManager.DayPeriod expectedDayPeriod = TimeManager.DayPeriod.TWILIGHT_POST_SUNRISE;
        float xDeltaError = 0.001f;
        float yDeltaError = 0.001f;

        // Act + Assert
        t = tSunRise + xDeltaError;
        expectedTPeriod = xDeltaError / twilightPostSunRiseWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tSunRise + 0.02f;
        expectedTPeriod = 0.02f / twilightPostSunRiseWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tPostDusk - xDeltaError;
        expectedTPeriod = (twilightPostSunRiseWidth - xDeltaError) / twilightPostSunRiseWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);
    }

    @Test
    public void testPreMidday() {
        // Arrange
        float t;
        float expectedTPeriod;
        TimeManager.DayPeriod expectedDayPeriod = TimeManager.DayPeriod.PRE_MIDDAY;
        float xDeltaError = 0.001f;
        float yDeltaError = 0.001f;

        // Act + Assert
        t = tPostDusk + xDeltaError;
        expectedTPeriod = xDeltaError / preMiddayWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tPostDusk + 0.05f;
        expectedTPeriod = 0.05f / preMiddayWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tMidday - xDeltaError;
        expectedTPeriod = (preMiddayWidth - xDeltaError) / preMiddayWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);
    }

    @Test
    public void testPostMidday() {
        // Arrange
        float t;
        float expectedTPeriod;
        TimeManager.DayPeriod expectedDayPeriod = TimeManager.DayPeriod.POST_MIDDAY;
        float xDeltaError = 0.001f;
        float yDeltaError = 0.001f;

        // Act + Assert
        t = tMidday + xDeltaError;
        expectedTPeriod = xDeltaError / postMiddayWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tMidday + 0.1f;
        expectedTPeriod = 0.1f / postMiddayWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tPreDawn - xDeltaError;
        expectedTPeriod = (postMiddayWidth - xDeltaError) / postMiddayWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);
    }

    @Test
    public void testTwilightPreSunSet() {
        // Arrange
        float t;
        float expectedTPeriod;
        TimeManager.DayPeriod expectedDayPeriod = TimeManager.DayPeriod.TWILIGHT_PRE_SUNSET;
        float xDeltaError = 0.001f;
        float yDeltaError = 0.001f;

        // Act + Assert
        t = tPreDawn + xDeltaError;
        expectedTPeriod = xDeltaError / twilightPreSunSetWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tPreDawn + 0.02f;
        expectedTPeriod = 0.02f / twilightPreSunSetWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tSunSet - xDeltaError;
        expectedTPeriod = (twilightPreSunSetWidth - xDeltaError) / twilightPreSunSetWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);
    }

    @Test
    public void testTwilightPostSunSet() {
        // Arrange
        float t;
        float expectedTPeriod;
        TimeManager.DayPeriod expectedDayPeriod = TimeManager.DayPeriod.TWILIGHT_POST_SUNSET;
        float xDeltaError = 0.001f;
        float yDeltaError = 0.001f;

        // Act + Assert
        t = tSunSet + xDeltaError;
        expectedTPeriod = xDeltaError / twilightPostSunSetWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tSunSet + 0.02f;
        expectedTPeriod = 0.02f / twilightPostSunSetWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);

        t = tPostDawn - xDeltaError;
        expectedTPeriod = (twilightPostSunSetWidth - xDeltaError) / twilightPostSunSetWidth;
        timeManager.updateDayPeriod(t);
        Assert.assertEquals(expectedDayPeriod, timeManager.getPeriod());
        Assert.assertEquals(expectedTPeriod, timeManager.getTPeriod(), yDeltaError);
    }

    @Test
    public void testTSunPostSunrise() {
        // Arrange
        float t;
        float expectedTSun;
        float xDeltaError = 0.001f;
        float yDeltaError = 0.001f;

        // Act + Assert
        t = tSunRise + twilightPostSunRiseWidth / 2;
        expectedTSun = (twilightPostSunRiseWidth / 2) / sunTravelWidth;
        float tSun = timeManager.calculateTSun(t);
        Assert.assertEquals(expectedTSun, tSun, yDeltaError);
    }

    @Test
    public void testTSunPreMidday() {
        // Arrange
        float t;
        float expectedTSun;
        float xDeltaError = 0.001f;
        float yDeltaError = 0.001f;

        // Act + Assert
        t = tPostDusk + preMiddayWidth / 2;
        expectedTSun = (twilightPostSunRiseWidth + preMiddayWidth / 2) / sunTravelWidth;
        float tSun = timeManager.calculateTSun(t);
        Assert.assertEquals(expectedTSun, tSun, yDeltaError);
    }

    @Test
    public void testTSunPostMidday() {
        // Arrange
        float t;
        float expectedTSun;
        float xDeltaError = 0.001f;
        float yDeltaError = 0.001f;

        // Act + Assert
        t = tMidday + postMiddayWidth / 2;
        expectedTSun =
                (twilightPostSunRiseWidth + preMiddayWidth + postMiddayWidth / 2) / sunTravelWidth;
        float tSun = timeManager.calculateTSun(t);
        Assert.assertEquals(expectedTSun, tSun, yDeltaError);
    }

    @Test
    public void testTSunPreSunSet() {
        // Arrange
        float t;
        float expectedTSun;
        float xDeltaError = 0.001f;
        float yDeltaError = 0.001f;

        // Act + Assert
        t = tPreDawn + twilightPreSunSetWidth / 2;
        expectedTSun = (twilightPostSunRiseWidth + preMiddayWidth + postMiddayWidth +
                twilightPreSunSetWidth / 2) / sunTravelWidth;
        float tSun = timeManager.calculateTSun(t);
        Assert.assertEquals(expectedTSun, tSun, yDeltaError);
    }

    @Test
    public void testTNightSunSet() {
        // Arrange
        float t;
        float expectedTNight;
        float tDeltaError = 0.001f;

        t = tSunSet;
        expectedTNight = 0;
        timeManager.setT(t);
        float tNight = timeManager.getTNight();
        Assert.assertEquals(expectedTNight, tNight, tDeltaError);
    }

    @Test
    public void testTNightSunRise() {
        // Arrange
        float t;
        float expectedTNight;
        float tDeltaError = 0.001f;

        t = tSunRise;
        expectedTNight = 1;
        timeManager.setT(t);
        float tNight = timeManager.getTNight();
        Assert.assertEquals(expectedTNight, tNight, tDeltaError);
    }

    @Test
    public void testTNightMidnight() {
        // Arrange
        float t;
        float expectedTNight;
        float tDeltaError = 0.001f;

        t = tMidnight;
        expectedTNight = 0.5f;
        timeManager.setT(t);
        float tNight = timeManager.getTNight();
        Assert.assertEquals(expectedTNight, tNight, tDeltaError);
    }

    @Test
    public void testTNightMidday() {
        // Arrange
        float t;
        float expectedTNight;
        float tDeltaError = 0.001f;

        t = tMidday;
        expectedTNight = -1;
        timeManager.setT(t);
        float tNight = timeManager.getTNight();
        Assert.assertEquals(expectedTNight, tNight, tDeltaError);
    }
}
