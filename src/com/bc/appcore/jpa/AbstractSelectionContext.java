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

import com.bc.appcore.util.Selection;
import com.bc.jpa.EntityUpdater;
import com.bc.jpa.JpaContext;
import com.bc.jpa.dao.BuilderForSelect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.appcore.AppCore;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 31, 2017 5:28:32 PM
 */
public abstract class AbstractSelectionContext implements SelectionContext {

    private static final Logger logger = Logger.getLogger(AbstractSelectionContext.class.getName());
    
    private final AppCore app;
    
    private final Map<Class, EntityUpdater> updaters;

    public AbstractSelectionContext(AppCore app) {
        this.app = app;
        this.updaters = new HashMap<>();
    }

    @Override
    public boolean isSelectionType(Class entityType) {
        final boolean output = this.getSelectionColumn(entityType, null) != null;
        return output;
    }
    
    @Override
    public List<Selection> getSelectionValues(Class entityType) {
        
        final String columnName = this.getSelectionColumn(entityType, null);
        
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Entity type: {0}, selection col: {1}", 
                    new Object[]{entityType.getName(), columnName});
        }
        
        final List<Selection> selectionList;
        
        if(columnName == null) {
            
            selectionList = Collections.EMPTY_LIST;
            
        }else{
            
            final JpaContext jpaContext = app.getJpaContext();
            
            try(BuilderForSelect builder = jpaContext.getBuilderForSelect(entityType)) {
                
                final List entityList = this.sort(
                        entityType, builder.from(entityType).createQuery().getResultList()
                );
                
                logger.log(Level.FINER, () -> "Entity list: " + entityList);
                
                selectionList = new ArrayList(entityList.size() + 1);
                
                selectionList.add(this.getDefaultSelection(entityType, columnName));
                
                for(Object entity : entityList) {
                    
                    selectionList.add(this.getSelection(entityType, entity, columnName));
                }
            }
        }
        
        if(!selectionList.isEmpty()) {
            
        }
        
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Value type: {0}, Selection values: {1}", 
                    new Object[]{entityType.getName(), selectionList});
        }
        
        return selectionList.isEmpty() ? Collections.EMPTY_LIST : Collections.unmodifiableList(selectionList);
    }
    
    public List sort(Class entityType, List entityList) {
        return entityList;
    }
    
    @Override
    public <T> Selection<T> getDefaultSelection(Class<T> entityType, String columnName) {
        return Selection.from("Select " + entityType.getSimpleName(), null);
    }

    @Override
    public <T> Selection<T> getSelection(T entity) {
        final Class entityType = entity.getClass();
        final String columnName = this.getSelectionColumn(entityType, null);
        Objects.requireNonNull(columnName);
        return this.getSelection(entityType, entity, columnName);
    }
    
    @Override
    public <T> Selection<T> getSelection(Class<T> entityType, T entity, String columnName) {
        final String displayText = this.getDisplayValue(entityType, entity, columnName);
        return Selection.from(displayText, entity);
    }
    
    public String getDisplayValue(Class entityType, Object entity, String columnName) {
        final String displayText = (String)this.getEntityUpdater(entityType).getValue(entity, columnName);
        return displayText;
    }
    
    public EntityUpdater getEntityUpdater(Class entityType) {
        EntityUpdater output = this.updaters.get(entityType);
        if(output == null) {
            output = app.getJpaContext().getEntityUpdater(entityType);
            this.updaters.put(entityType, output);
        }
        return output;
    }
}

