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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

/**
 *
 */
public class DependencyGraph<T> {

    /**
     * The nodes of the graph
     */
    protected HashMap<T, GraphNode<T>> nodes = new HashMap<T, GraphNode<T>>();

    /**
     * A stack for processing the nodes to produce a topological ordering
     */
    protected Stack<GraphNode<T>> processingStack = new Stack<GraphNode<T>>();

    /**
     * A pool of nodes, for reducing allocation
     */
    protected Queue<GraphNode<T>> nodesPool = new LinkedList<GraphNode<T>>();

    public static final String GRAPH_CONTAINS_CYCLES = "Graph contains cycles";

    /**
     * Clear all internal data
     */
    public void clear() {
        processingStack.clear();
        for (GraphNode<T> node : nodes.values()) {
            node.reset();
            nodesPool.add(node);
        }
        nodes.clear();
    }

    /**
     * Creates a graph node of the <T> generic type
     *
     * @param value
     *         The value that is hosted by the node
     *
     * @return a generic GraphNode object
     */
    protected GraphNode<T> createNode(T value) {
        GraphNode<T> node = nodesPool.poll();
        if (node == null) {
            node = new GraphNode<T>();
        }
        node.value = value;
        return node;
    }

    /**
     * Adds a single node to the graph, without specifying any dependency.
     *
     * @param value
     */
    public void addNode(T value) {
        if (!nodes.containsKey(value)) {
            GraphNode<T> node = createNode(value);
            nodes.put(value, node);
        }
    }

    /**
     * Adds a dependency between two nodes. The dependentValue is considered dependent on
     * parentValue, and therefore, parentValue should be processed <b>before</b> dependentValue.
     *
     * @param dependentValue
     * @param parentValue
     */
    public void addDependency(T dependentValue, T parentValue) {
        GraphNode<T> dependentNode = nodes.get(dependentValue);
        if (dependentNode == null) {
            dependentNode = createNode(dependentValue);
            nodes.put(dependentValue, dependentNode);
        }

        GraphNode<T> parentNode = nodes.get(parentValue);
        if (parentNode == null) {
            parentNode = createNode(parentValue);
            nodes.put(parentValue, parentNode);
        }

        if (dependentNode.getParents().contains(parentNode)) {
            return;
        }

        dependentNode.addParent(parentNode);
        parentNode.addChildren(dependentNode);
    }

    /**
     * Populates the parameter list with the values of the graph in a topological order. This
     * means that if value 'a' depends on value 'b', value 'b' appears in the list before value
     * 'a'. If the graph contains cycles, the list will remain unaltered.
     * <p/>
     * NOTE: this method does not clear the list before inserting nodes in topological order,
     * consider doing it yourself before calling this method.
     *
     * @param dependencyList
     */
    public void insertValuesInTopologicalOrderInto(Collection<T> dependencyList) {
        // Clear the processing stack
        processingStack.clear();

        // Initialize node
        for (GraphNode<T> node : nodes.values()) {
            node.isVisited = false;
            node.isProcessed = false;
        }

        // Add independent nodes to the stack (those with no parents)
        for (GraphNode<T> node : nodes.values()) {
            if (!node.isProcessed) {
                processingStack.push(node);
                processNodes(dependencyList);
            }
        }

    }

    protected void processNodes(Collection<T> dependencyList) {
        // Process nodes in the stack
        while (!processingStack.isEmpty()) {
            GraphNode<T> node = processingStack.pop();

            if (!node.isProcessed) {
                // Mark node as visited, to check for cycles
                node.isVisited = true;

                // Insert node in the stack, in case there are unprocessed parent nodes
                processingStack.push(node);

                // Add unprocessed parent nodes to the stack
                int numParentsNotProcessed = 0;
                for (GraphNode<T> parent : node.getParents()) {
                    if (!parent.isProcessed) {
                        // Check cycles
                        if (parent.isVisited) {
                            throw new IllegalArgumentException(GRAPH_CONTAINS_CYCLES);
                        }

                        numParentsNotProcessed++;
                        processingStack.push(parent);
                    }
                }

                // If there are no unprocessed parents it means all dependencies are satisfied:
                // add node to the dependency list
                if (numParentsNotProcessed == 0) {
                    processingStack.pop();
                    dependencyList.add(node.value);
                    node.isVisited = false;
                    node.isProcessed = true;
                }
            }
        }
    }

    /**
     * A graph node, holding the value, a list of children (dependent nodes) and a
     * list of parents (nodes that this node depends on).
     *
     * @param <K>
     */
    final class GraphNode<K> {
        public K value;
        public boolean isVisited;
        public boolean isProcessed;
        private List<GraphNode<K>> children = new ArrayList<GraphNode<K>>();
        private List<GraphNode<K>> parents = new ArrayList<GraphNode<K>>();

        /**
         * Adds an incoming node to the current node
         *
         * @param node
         *         The incoming node
         */
        public void addChildren(GraphNode<K> node) {
            if (children == null) {
                children = new ArrayList<GraphNode<K>>();
            }
            children.add(node);
        }

        /**
         * Adds an outgoing node from the current node
         *
         * @param node
         *         The outgoing node
         */
        public void addParent(GraphNode<K> node) {
            if (parents == null) {
                parents = new ArrayList<GraphNode<K>>();
            }
            parents.add(node);
        }

        /**
         * Provides all the coming in nodes
         *
         * @return The coming in nodes
         */
        public List<GraphNode<K>> getChildren() {
            return children;
        }

        /**
         * Provides all the going out nodes
         *
         * @return The going out nodes
         */
        public List<GraphNode<K>> getParents() {
            return parents;
        }

        /**
         * A method for resetting the node, clearing all its internal data
         */
        public void reset() {
            value = null;
            isVisited = false;
            isProcessed = false;
            parents.clear();
            children.clear();
        }
    }
}
