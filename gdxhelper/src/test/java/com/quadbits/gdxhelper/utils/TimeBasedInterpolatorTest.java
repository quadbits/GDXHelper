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
import org.junit.Test;

/**
 *
 */
public class TimeBasedInterpolatorTest {
    @Test
    public void testNormalRange() {
        long duration = 1000;
        long deltaTime = 400;
        float deltaAssert = 0.001f;

        // Arrange
        TimeBasedInterpolator timeBasedInterpolator = new TimeBasedInterpolator(duration);

        // Act
        float interpolatedValue1 = timeBasedInterpolator.interpolate(deltaTime);
        float interpolatedValue2 = timeBasedInterpolator.interpolate(deltaTime);
        float interpolatedValue3 = timeBasedInterpolator.interpolate(deltaTime);

        // Assert
        float expectedValue1 = (1.f * deltaTime) / duration;
        Assert.assertEquals(expectedValue1, interpolatedValue1, deltaAssert);
        float expectedValue2 = (2.f * deltaTime) / duration;
        Assert.assertEquals(expectedValue2, interpolatedValue2, deltaAssert);
        float expectedValue3 = 1;
        Assert.assertEquals(expectedValue3, interpolatedValue3, deltaAssert);
    }

    @Test
    public void testCustomRange() {
        long duration = 1000;
        long deltaTime = 400;
        float deltaAssert = 0.001f;

        // Arrange
        TimeBasedInterpolator timeBasedInterpolator = new TimeBasedInterpolator(duration, 1, 0);

        // Act
        float interpolatedValue1 = timeBasedInterpolator.interpolate(deltaTime);
        float interpolatedValue2 = timeBasedInterpolator.interpolate(deltaTime);
        float interpolatedValue3 = timeBasedInterpolator.interpolate(deltaTime);

        // Assert
        float expectedValue1 = 1.f - (1.f * deltaTime) / duration;
        Assert.assertEquals(expectedValue1, interpolatedValue1, deltaAssert);
        float expectedValue2 = 1.f - (2.f * deltaTime) / duration;
        Assert.assertEquals(expectedValue2, interpolatedValue2, deltaAssert);
        float expectedValue3 = 0;
        Assert.assertEquals(expectedValue3, interpolatedValue3, deltaAssert);
    }

    @Test
    public void testTotalDurationReached() {
        long duration = 100;
        long deltaTime = 200;
        float deltaAssert = 0.001f;

        // Arrange
        TimeBasedInterpolator timeBasedInterpolator = new TimeBasedInterpolator(duration);

        // Act
        float interpolatedValue = timeBasedInterpolator.interpolate(deltaTime);

        // Assert
        float expectedValue1 = 1;
        Assert.assertEquals(expectedValue1, interpolatedValue, deltaAssert);
    }
}
