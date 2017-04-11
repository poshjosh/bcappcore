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
import com.bc.jpa.EntityUpdater;
import com.bc.jpa.dao.BuilderForSelect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 31, 2017 5:28:32 PM
 */
public abstract class AbstractSelectionContext implements SelectionContext {

    private static final Logger logger = Logger.getLogger(AbstractSelectionContext.class.getName());
    
    private final AppCore app;

    public AbstractSelectionContext(AppCore app) {
        this.app = app;
    }
    
    @Override
    public Selection[] getSelectionValues(Class entityType) {
        
        final String columnName = this.getSelectionColumn(entityType, null);
        
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Entity type: {0}, selection col: {1}", 
                    new Object[]{entityType.getName(), columnName});
        }
        
        final Selection [] selection;
        if(columnName == null) {
            selection = new Selection[0];
        }else{
            try(BuilderForSelect builder = app.getJpaContext().getBuilderForSelect(entityType)) {
                final List values = builder.from(entityType).createQuery().getResultList();
                final List<Selection> selectionList = new ArrayList(values.size() + 1);
                final EntityUpdater updater = app.getJpaContext().getEntityUpdater(entityType);
                selectionList.add(new SelectionImpl("Select " + entityType.getSimpleName(), null));
                for(Object entity : values) {
                    final String label = (String)updater.getValue(entity, columnName);
                    selectionList.add(new SelectionImpl(label, entity));
                }
                selection = selectionList.toArray(new Selection[0]);
            }
        }
        
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Value type: {0}, Selection values: {1}", 
                    new Object[]{entityType.getName(), selection==null?null:Arrays.toString(selection)});
        }
        
        return selection;
    }
}

