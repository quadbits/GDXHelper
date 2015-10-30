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

import java.util.ArrayList;

/**
 *
 */
public class DependencyGraphTest {
    @Test
    public void testSimpleGraph() {
        // Arrange
        DependencyGraph<String> dependencyGraph = new DependencyGraph<String>();
        dependencyGraph.addDependency("b", "a");
        dependencyGraph.addDependency("c", "a");
        dependencyGraph.addDependency("d", "c");
        ArrayList<String> topologicalOrderList = new ArrayList<String>();

        // Act
        dependencyGraph.insertValuesInTopologicalOrderInto(topologicalOrderList);

        // Assert
        Assert.assertEquals(topologicalOrderList.indexOf("a"), 0);
        Assert.assertTrue(topologicalOrderList.indexOf("a") < topologicalOrderList.indexOf("b"));
        Assert.assertTrue(topologicalOrderList.indexOf("a") < topologicalOrderList.indexOf("c"));
        Assert.assertTrue(topologicalOrderList.indexOf("c") < topologicalOrderList.indexOf("d"));
    }

    @Test
    public void testGraphWithCycles() {
        // Arrange
        DependencyGraph<String> dependencyGraph = new DependencyGraph<String>();
        dependencyGraph.addDependency("b", "a");
        dependencyGraph.addDependency("c", "b");
        dependencyGraph.addDependency("a", "c");
        ArrayList<String> topologicalOrderList = new ArrayList<String>();

        // Act
        try {
            dependencyGraph.insertValuesInTopologicalOrderInto(topologicalOrderList);
            // Assert
            Assert.fail("Test should throw exception if cycles were found");
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().equals(DependencyGraph.GRAPH_CONTAINS_CYCLES)) {
                throw e;
            }
        }
    }

    @Test
    public void testGraphWithMultipleRoots() {
        // Arrange
        DependencyGraph<String> dependencyGraph = new DependencyGraph<String>();
        dependencyGraph.addDependency("b", "a");
        dependencyGraph.addDependency("c", "a");
        dependencyGraph.addDependency("e", "d");
        ArrayList<String> topologicalOrderList = new ArrayList<String>();

        // Act
        dependencyGraph.insertValuesInTopologicalOrderInto(topologicalOrderList);

        // Assert
        Assert.assertTrue(topologicalOrderList.indexOf("a") < topologicalOrderList.indexOf("b"));
        Assert.assertTrue(topologicalOrderList.indexOf("a") < topologicalOrderList.indexOf("c"));
        Assert.assertTrue(topologicalOrderList.indexOf("d") < topologicalOrderList.indexOf("e"));
    }
}
