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
package com.quadbits.gdxhelper;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

/**
 *
 */
public class LWPStage extends Stage {
    @Override
    public void dispose() {
        disposeActors(getRoot());
        super.dispose();
    }

    private void disposeActors(Group group) {
        for (Actor actor : group.getChildren()) {
            if (actor instanceof Group) {
                disposeActors((Group) actor);
            } else if (actor instanceof Disposable) {
                ((Disposable) actor).dispose();
            }
        }
    }
}
