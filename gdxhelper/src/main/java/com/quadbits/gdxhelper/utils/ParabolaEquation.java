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

import javax.inject.Inject;

/**
 *
 */
public class ParabolaEquation {
    double minX;
    double maxX;
    double minY;
    double maxY;
    double a;
    double b;
    double c;
    double focalLength;
    double vertexX;
    double vertexY;
    TraverseDirection traverseDirection;

    public enum TraverseDirection {
        LEFT_TO_RIGHT(0), RIGHT_TO_LEFT(1);

        private final int intValue;

        private TraverseDirection(int intValue) {
            this.intValue = intValue;
        }

        public int toInt() {
            return intValue;
        }

        public static TraverseDirection fromInt(int intValue) {
            if (LEFT_TO_RIGHT.toInt() == intValue) {
                return LEFT_TO_RIGHT;
            } else {
                return RIGHT_TO_LEFT;
            }
        }
    }

    @Inject
    public ParabolaEquation() {
        super();
        traverseDirection = TraverseDirection.LEFT_TO_RIGHT;
        setBounds(0, 0, 0, 0);
    }

    public void setBounds(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;

        vertexX = minX + (maxX - minX) / 2f;
        vertexY = maxY;
        double tmp = minX - vertexX;
        a = (minY - vertexY) / (tmp * tmp);
        focalLength = 1f / (4f * a);
        b = -vertexX / (2 * focalLength);
        c = vertexX * vertexX / (4 * focalLength) + vertexY;
    }

    public void getPosition(double t, Vector2 pos) {
        if (traverseDirection == TraverseDirection.RIGHT_TO_LEFT) {
            t = 1 - t;
        }
        pos.x = (float) (minX + t * (maxX - minX));
        pos.y = (float) (a * pos.x * pos.x + b * pos.x + c);
    }

    public double getTDiffGivenXDiff(double xdiff) {
        return xdiff / (maxX - minX);
    }

    public double getTDiffGivenYDiff(double ydiff, double t) {
        if (traverseDirection == TraverseDirection.RIGHT_TO_LEFT) {
            t = 1 - t;
        }
        double x0 = minX + t * (maxX - minX);
        double c2;
        if (t < 0.5f) {
            c2 = -a * x0 * x0 - b * x0 - ydiff;
        } else {
            c2 = -a * x0 * x0 - b * x0 + ydiff;
        }
        double radical = b * b - 4 * a * c2;
        if (radical < 0) {
            return Double.MAX_VALUE;
        }
        double sqrtRadical = Math.sqrt(radical);
        double x1 = (-b + sqrtRadical) / (2 * a);
        if (x1 < x0) {
            x1 = (-b - sqrtRadical) / (2 * a);
        }

        return getTDiffGivenXDiff(x1 - x0);
    }

    public double getMinX() {
        return minX;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxY() {
        return maxY;
    }

    public double getA() {
        return a;
    }

    public double getB() {
        return b;
    }

    public double getC() {
        return c;
    }

    public double getFocalLength() {
        return focalLength;
    }

    public double getVertexX() {
        return vertexX;
    }

    public double getVertexY() {
        return vertexY;
    }

    public TraverseDirection getTraverseDirection() {
        return traverseDirection;
    }

    public void setTraverseDirection(TraverseDirection traverseDirection) {
        this.traverseDirection = traverseDirection;
    }
}
