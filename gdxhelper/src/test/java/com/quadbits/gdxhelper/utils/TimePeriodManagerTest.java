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
public class TimePeriodManagerTest {
    @Test
    public void testDuration() {
        // Arrange
        TimePeriodManager timePeriodManager = new TimePeriodManager();

        // Act
        timePeriodManager.addSubPeriod(500);
        timePeriodManager.addSubPeriod(1000);
        timePeriodManager.addSubPeriod(500);
        timePeriodManager.addSubPeriod(1000);

        // Assert
        Assert.assertEquals(3000, timePeriodManager.getPeriodDuration());
    }

    @Test
    public void testUpdate() {
        // Arrange
        TimePeriodManager timePeriodManager = new TimePeriodManager();
        timePeriodManager.addSubPeriod(500);
        timePeriodManager.addSubPeriod(1000);
        timePeriodManager.addSubPeriod(500);
        timePeriodManager.addSubPeriod(1000);

        // Act + Assert
        timePeriodManager.update(300); // 300
        Assert.assertEquals(300, timePeriodManager.getCurrentPeriodTime());
        Assert.assertEquals(300, timePeriodManager.getCurrentSubPeriodTime());
        Assert.assertEquals(0, timePeriodManager.getCurrentSubPeriod());

        timePeriodManager.update(300); // 600
        Assert.assertEquals(600, timePeriodManager.getCurrentPeriodTime());
        Assert.assertEquals(100, timePeriodManager.getCurrentSubPeriodTime());
        Assert.assertEquals(1, timePeriodManager.getCurrentSubPeriod());

        timePeriodManager.update(1000); // 1600
        Assert.assertEquals(1600, timePeriodManager.getCurrentPeriodTime());
        Assert.assertEquals(100, timePeriodManager.getCurrentSubPeriodTime());
        Assert.assertEquals(2, timePeriodManager.getCurrentSubPeriod());

        timePeriodManager.update(1000); // 2600
        Assert.assertEquals(2600, timePeriodManager.getCurrentPeriodTime());
        Assert.assertEquals(600, timePeriodManager.getCurrentSubPeriodTime());
        Assert.assertEquals(3, timePeriodManager.getCurrentSubPeriod());
    }

    @Test
    public void testOverflow() {
        // Arrange
        TimePeriodManager timePeriodManager = new TimePeriodManager();
        timePeriodManager.addSubPeriod(500);
        timePeriodManager.addSubPeriod(1000);
        timePeriodManager.addSubPeriod(500);
        timePeriodManager.addSubPeriod(1000);

        // Act
        timePeriodManager.update(1000); // 1000
        timePeriodManager.update(1000); // 2000
        timePeriodManager.update(1500); // 3500 -> 500

        // Assert
        Assert.assertEquals(500, timePeriodManager.getCurrentPeriodTime());
        Assert.assertEquals(500, timePeriodManager.getCurrentSubPeriodTime());
        Assert.assertEquals(0, timePeriodManager.getCurrentSubPeriod());
    }
}
