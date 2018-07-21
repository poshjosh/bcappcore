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

package com.bc.appcore.functions;

import com.bc.appcore.ObjectFactory;
import com.bc.util.MapBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 9, 2017 9:19:39 AM
 */
public class BuildMapImpl implements BuildMap {

    private final ObjectFactory objectFactory;

    public BuildMapImpl(ObjectFactory objectFactory) {
        this.objectFactory = Objects.requireNonNull(objectFactory);
    }
    
    @Override
    public Map apply(Class type, Object instance) {
        final Map map = objectFactory.getOrException(MapBuilder.class)
                .sourceType(type)
                .source(instance)
                .target(new LinkedHashMap())
                .maxCollectionSize(Integer.MAX_VALUE)
                .maxDepth(1)
                .methodFilter(MapBuilder.MethodFilter.ACCEPT_ALL)
                .nullsAllowed(true)
                .recursionFilter((aType, anInstance) -> false)
                .build();
        return map;
    }
}
