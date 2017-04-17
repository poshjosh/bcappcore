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
import com.bc.appcore.TypeProvider;
import com.bc.jpa.EntityUpdater;
import com.bc.jpa.JpaMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 25, 2017 10:22:03 PM
 */
public class JpaTypeProvider implements TypeProvider {

    private static final Logger logger = Logger.getLogger(JpaTypeProvider.class.getName());
    
    private static final class HasSelectionValuesComparator implements Comparator<Class>{
        private final SelectionContext selectionValues;
        public HasSelectionValuesComparator(SelectionContext selectionValues) {
            this.selectionValues = selectionValues;
        }
        @Override
        public int compare(Class class1, Class class2) {
            final int i1 = this.selectionValues.getSelectionValues(class1).length;
            final int i2 = this.selectionValues.getSelectionValues(class2).length;
            return Integer.compare(i1, i2);
        }
    }
    
    private final AppCore app;
    
    private final Map<Class, Set<String>> typeColumnNames;
    
    private final Map<Class, EntityUpdater> entityUpdaters;
    
    public JpaTypeProvider(AppCore app, Predicate<String> testPersistenceUnitName) {
        this.app = Objects.requireNonNull(app);
        this.typeColumnNames = new HashMap();
        this.entityUpdaters = new HashMap();
        final JpaMetaData metaData = app.getJpaContext().getMetaData();
        final String [] puNames = metaData.getPersistenceUnitNames();
        for(String puName : puNames) {
            if(!testPersistenceUnitName.test(puName)) {
                continue;
            }
            Class [] puTypes = metaData.getEntityClasses(puName);
            for(Class puType : puTypes) {
                this.typeColumnNames.put(puType, new HashSet(Arrays.asList(metaData.getColumnNames(puType))));
            }
        }
    }
    
    @Override
    public Class getType(String name, Object value, Class outputIfNone) {
        
        final List<Class> types = new ArrayList();
        
        final Set<Class> entityTypes = this.typeColumnNames.keySet();
        
        for(Class entityType : entityTypes) {
            
            final Set<String> columnNames = this.typeColumnNames.get(entityType); 
            
            if(logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "Name {0}, entityType: {1}, columns: {2}", 
                        new Object[]{name, entityType.getName(), columnNames});
            }
            
            if(columnNames.contains(name)) {
                
                if(logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "Found {0} in {1} having columns: {2}", 
                            new Object[]{name, entityType.getName(), columnNames});
                }
                
                final Class type = this.getEntityUpdater(entityType).getMethod(false, name).getReturnType();
                
                types.add(type);
            }
        }
        
        final Class output;
        if(types.isEmpty()) {
            output = outputIfNone;
        }else if(types.size() == 1) {
            output = types.get(0);
        }else{
            logger.log(Level.FINE, "BEFORE Sort: {0}", types);
            Collections.sort(types, new HasSelectionValuesComparator(app.get(SelectionContext.class)));
            logger.log(Level.FINE, " AFTER Sort: {0}", types);
            output = types.get(types.size()-1);
        }
        return output;
    }
    
    public EntityUpdater getEntityUpdater(Class type) {
        EntityUpdater updater = this.entityUpdaters.get(type);
        if(updater == null) {
            updater = this.app.getJpaContext().getEntityUpdater(type);
            this.entityUpdaters.put(type, Objects.requireNonNull(updater));
        }
        return updater;
    }
}
