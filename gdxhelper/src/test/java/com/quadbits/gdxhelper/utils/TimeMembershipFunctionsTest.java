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
public class TimeMembershipFunctionsTest {
    float tSunRise;
    float tSunSet;

    TimeManager timeManager;

    @Before
    public void setUp() {
        tSunRise = 0.3f;
        tSunSet = 0.9f;
        timeManager = new TimeManager(tSunRise, tSunSet);
    }

    @Test
    public void testDay() {
        // Arrange
        timeManager.setT(0.6f);

        // Act
        float mu = TimeManager.isDay.evaluate(timeManager);

        // Assert
        float expectedMu = 1;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testNotDay() {
        // Arrange
        timeManager.setT(0.1f);

        // Act
        float mu = TimeManager.isDay.evaluate(timeManager);

        // Assert
        float expectedMu = 0;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testDaySunrise() {
        // Arrange
        timeManager.setT(timeManager.getTSunRise() + timeManager.getTTwilight() / 2);

        // Act
        float mu = TimeManager.isDay.evaluate(timeManager);

        // Assert
        float expectedMu = 0.5f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testDaySunset() {
        // Arrange
        timeManager.setT(timeManager.getTSunSet() - timeManager.getTTwilight() / 2);

        // Act
        float mu = TimeManager.isDay.evaluate(timeManager);

        // Assert
        float expectedMu = 0.5f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testDaySunrisePreTwilight() {
        // Arrange
        timeManager.setT(timeManager.getTSunRise());

        // Act
        float mu = TimeManager.isDay.evaluate(timeManager);

        // Assert
        float expectedMu = 0f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testDaySunrisePostTwilight() {
        // Arrange
        timeManager.setT(timeManager.getTSunRise() + timeManager.getTTwilight());

        // Act
        float mu = TimeManager.isDay.evaluate(timeManager);

        // Assert
        float expectedMu = 1f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testDaySunsetPreTwilight() {
        // Arrange
        timeManager.setT(timeManager.getTSunSet() - timeManager.getTTwilight());

        // Act
        float mu = TimeManager.isDay.evaluate(timeManager);

        // Assert
        float expectedMu = 1f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testDaySunsetPostTwilight() {
        // Arrange
        timeManager.setT(timeManager.getTSunSet());

        // Act
        float mu = TimeManager.isDay.evaluate(timeManager);

        // Assert
        float expectedMu = 0f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testNight() {
        // Arrange
        timeManager.setT(0.2f);

        // Act
        float mu = TimeManager.isNight.evaluate(timeManager);

        // Assert
        float expectedMu = 1;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testNotNight() {
        // Arrange
        timeManager.setT(0.6f);

        // Act
        float mu = TimeManager.isNight.evaluate(timeManager);

        // Assert
        float expectedMu = 0;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testNightMoonset() {
        // Arrange
        timeManager.setT(timeManager.getTSunRise() - timeManager.getTTwilight() / 2);

        // Act
        float mu = TimeManager.isNight.evaluate(timeManager);

        // Assert
        float expectedMu = 0.5f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testNightMoonrise() {
        // Arrange
        timeManager.setT(timeManager.getTSunSet() + timeManager.getTTwilight() / 2);

        // Act
        float mu = TimeManager.isNight.evaluate(timeManager);

        // Assert
        float expectedMu = 0.5f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testNightMoonsetPostTwilight() {
        // Arrange
        timeManager.setT(timeManager.getTSunRise());

        // Act
        float mu = TimeManager.isNight.evaluate(timeManager);

        // Assert
        float expectedMu = 0f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testNightMoonsetPreTwilight() {
        // Arrange
        timeManager.setT(timeManager.getTSunRise() - timeManager.getTTwilight());

        // Act
        float mu = TimeManager.isNight.evaluate(timeManager);

        // Assert
        float expectedMu = 1f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testNightMoonrisePostTwilight() {
        // Arrange
        timeManager.setT(timeManager.getTSunSet() + timeManager.getTTwilight());

        // Act
        float mu = TimeManager.isNight.evaluate(timeManager);

        // Assert
        float expectedMu = 1f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testNightMoonrisePreTwilight() {
        // Arrange
        timeManager.setT(timeManager.getTSunSet());

        // Act
        float mu = TimeManager.isNight.evaluate(timeManager);

        // Assert
        float expectedMu = 0f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testSunrise() {
        // Arrange
        timeManager.setT(timeManager.getTSunRise());

        // Act
        float mu = TimeManager.isSunrise.evaluate(timeManager);

        // Assert
        float expectedMu = 1f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testPreSunrise() {
        // Arrange
        timeManager.setT(timeManager.getTSunRise() - timeManager.getTTwilight() / 2);

        // Act
        float mu = TimeManager.isSunrise.evaluate(timeManager);

        // Assert
        float expectedMu = 0.5f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testPostSunrise() {
        // Arrange
        timeManager.setT(timeManager.getTSunRise() + timeManager.getTTwilight() / 2);

        // Act
        float mu = TimeManager.isSunrise.evaluate(timeManager);

        // Assert
        float expectedMu = 0.5f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testNotSunrise() {
        // Arrange
        timeManager.setT(timeManager.getTMidday());

        // Act
        float mu = TimeManager.isSunrise.evaluate(timeManager);

        // Assert
        float expectedMu = 0f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testSunset() {
        // Arrange
        timeManager.setT(timeManager.getTSunSet());

        // Act
        float mu = TimeManager.isSunset.evaluate(timeManager);

        // Assert
        float expectedMu = 1f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testPreSunset() {
        // Arrange
        timeManager.setT(timeManager.getTSunSet() - timeManager.getTTwilight() / 2);

        // Act
        float mu = TimeManager.isSunset.evaluate(timeManager);

        // Assert
        float expectedMu = 0.5f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testPostSunset() {
        // Arrange
        timeManager.setT(timeManager.getTSunSet() + timeManager.getTTwilight() / 2);

        // Act
        float mu = TimeManager.isSunset.evaluate(timeManager);

        // Assert
        float expectedMu = 0.5f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }

    @Test
    public void testNotSunset() {
        // Arrange
        timeManager.setT(timeManager.getTMidday());

        // Act
        float mu = TimeManager.isSunset.evaluate(timeManager);

        // Assert
        float expectedMu = 0f;
        float delta = 0.001f;
        Assert.assertEquals(expectedMu, mu, delta);
    }
}
