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
package com.quadbits.gdxhelper.controllers;

/**
 *
 */
public abstract class PanOnScrollController extends OnScrollBaseController {
    protected boolean autopan;
    protected boolean inverted;

    public PanOnScrollController() {
        super();
    }

    @Override
    public void reset() {
        super.reset();
        autopan = true;
        inverted = false;
    }

    public boolean isAutopan() {
        return autopan;
    }

    public void setAutopan(boolean autopan) {
        this.autopan = autopan;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }
}
