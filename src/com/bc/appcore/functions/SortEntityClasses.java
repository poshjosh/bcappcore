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

import com.bc.appcore.AppCore;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 12, 2017 12:26:48 PM
 */
public class SortEntityClasses implements Function<Collection<Class>, List<Class>> {

    private static final Logger logger = Logger.getLogger(SortEntityClasses.class.getName());

    private final AppCore app;
    
    public SortEntityClasses(AppCore app) {
        this.app = Objects.requireNonNull(app);
    }

    @Override
    public List<Class> apply(Collection<Class> masterTypes) {
        
        final List instances = this.createInstances(masterTypes);
        
        logger.fine(() -> "Comparator: " + app.getEntityOrderComparator());
        
        Collections.sort(instances, app.getEntityOrderComparator());
        
        final List<Class> output = new ArrayList(instances.size());
        
        for(Object instance : instances) {
            output.add(instance.getClass());
        }
        
        logger.fine(() -> "Before sort: " + masterTypes + "\n After sort: " + output);

        return output;
    }
    
    public List createInstances(Collection<Class> typeList) {
        final List output = new ArrayList(typeList.size());
        for(Class type : typeList) {
            output.add(this.newInstance(type));
        }
        return output;
    }
    
    public <T> T newInstance(Class<T> type) {
        try{
            return type.getConstructor().newInstance();
        }catch(NoSuchMethodException | SecurityException | InstantiationException | 
                IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
