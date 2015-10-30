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
public class TimeManagerDayIntervalTest {

    private TimeManager.DayInterval interval;

    @Before
    public void setUp() {
        // Arrange
        interval = new TimeManager.DayInterval();
        interval.init(TimeManager.DayPeriod.POST_MIDNIGHT, 0, 0.2f);
        interval.tExtraPreWidth = 0.2f;
    }

    @Test
    public void testContains() {
        // Completely in the interval
        Assert.assertTrue(interval.contains(0));
        Assert.assertTrue(interval.contains(0.2f));
        Assert.assertTrue(interval.contains(0.1f));

        // Completely out of the interval
        Assert.assertFalse(interval.contains(0.5f));

        // In the pre-interval, but should not be considered in the interval
        Assert.assertFalse(interval.contains(0.8f));
        Assert.assertFalse(interval.contains(0.9f));
        Assert.assertFalse(interval.contains(1.0f));
    }

    @Test
    public void testCalculateRelativeT() {
        double tolerance = 0.001;

        // Interval values
        Assert.assertEquals(interval.calculateRelativeT(0), 0.5, tolerance);
        Assert.assertEquals(interval.calculateRelativeT(0.2f), 1, tolerance);

        // Out of interval values
        Assert.assertEquals(interval.calculateRelativeT(0.5f), -1, tolerance);

        // In the pre-interval, but should not be considered in the interval
        Assert.assertEquals(interval.calculateRelativeT(0.8f), -1, tolerance);
        Assert.assertEquals(interval.calculateRelativeT(0.9f), -1, tolerance);
        Assert.assertEquals(interval.calculateRelativeT(1.0f), -1, tolerance);
    }

    @Test
    public void testCalculateAbsoluteT() {
        double tolerance = 0.001;

        // Interval values
        Assert.assertEquals(interval.calculateAbsoluteT(0.5f), 0, tolerance);
        Assert.assertEquals(interval.calculateAbsoluteT(0.75f), 0.1, tolerance);
        Assert.assertEquals(interval.calculateAbsoluteT(1), 0.2, tolerance);

        // Out of interval values
        Assert.assertEquals(interval.calculateAbsoluteT(1.5f), -1, tolerance);
        Assert.assertEquals(interval.calculateAbsoluteT(-1.5f), -1, tolerance);

        // In the pre-interval, but should not be considered in the interval
        Assert.assertEquals(interval.calculateAbsoluteT(0), -1, tolerance);
        Assert.assertEquals(interval.calculateAbsoluteT(0.25f), -1, tolerance);
        Assert.assertEquals(interval.calculateAbsoluteT(0.49f), -1, tolerance);
    }
}
