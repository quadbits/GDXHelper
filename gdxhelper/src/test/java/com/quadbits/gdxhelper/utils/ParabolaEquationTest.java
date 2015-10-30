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

import com.badlogic.gdx.math.Vector2;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class ParabolaEquationTest {
    ParabolaEquation eq;
    Vector2 pos;
    float minX;
    float maxX;
    float minY;
    float maxY;

    public ParabolaEquationTest() {
        pos = new Vector2();
    }

    @Before
    public void setUp() {
        pos.set(0, 0);
        minX = -30;
        maxX = 480;
        minY = -30;
        maxY = 800;
        eq = new ParabolaEquation();
        eq.setBounds(minX, maxX, minY, maxY);
    }

    @Test
    public void testMinTProducesMinXMinY() {
        // Arrange
        float t = 0;

        // Act
        eq.getPosition(t, pos);

        // Assert
        Assert.assertEquals(minX, pos.x, 0.01f);
        Assert.assertEquals(minY, pos.y, 0.01f);
    }

    @Test
    public void testMaxTProducesMaxXMinY() {
        // Arrange
        float t = 1;

        // Act
        eq.getPosition(t, pos);

        // Assert
        Assert.assertEquals(maxX, pos.x, 0.01f);
        Assert.assertEquals(minY, pos.y, 0.01f);
    }

    @Test
    public void testMiddleTProducesMiddleXMaxY() {
        // Arrange
        float t = 0.5f;

        // Act
        eq.getPosition(t, pos);

        // Assert
        Assert.assertEquals(minX + (maxX - minX) / 2, pos.x, 0.01f);
        Assert.assertEquals(maxY, pos.y, 0.01f);
    }
}
