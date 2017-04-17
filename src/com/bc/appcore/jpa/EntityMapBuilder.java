/*
 * Copyright 2017 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bc.appcore.jpa;

import com.bc.appcore.AppCore;
import com.bc.jpa.util.MapBuilderForEntity;
import com.bc.util.MapBuilder;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 15, 2017 9:22:31 PM
 */
public class EntityMapBuilder extends MapBuilderForEntity {

    private final AppCore app;

    public EntityMapBuilder(AppCore app) {
        this.app = app;
        this.recursionFilter(app.get(MapBuilder.RecursionFilter.class));
    }

    public AppCore getApp() {
        return app;
    }
}
