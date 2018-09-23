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

package com.bc.appcore.table.model;

import com.bc.jpa.context.PersistenceUnitContext;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import com.bc.jpa.dao.Update;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 9, 2017 1:31:45 PM
 */
public class MapTableModelUpdatesDb<T> extends MapTableModel {

    private static final Logger logger = Logger.getLogger(MapTableModelUpdatesDb.class.getName());

    private final PersistenceUnitContext puContext;
    
    private final Class<T> entityType;
    
    private final String idColumnName;
    
    private final int idColumnIndex;
    
    public MapTableModelUpdatesDb(PersistenceUnitContext puContext, Class<T> entityType, 
            Collection<Map> rows, String serialColumnName) {
        super(rows, serialColumnName);
        this.puContext = Objects.requireNonNull(puContext);
        this.entityType = Objects.requireNonNull(entityType);
        this.idColumnName = puContext.getMetaData().getIdColumnName(entityType);
        this.idColumnIndex = this.getColumnIndex(idColumnName);
        
        logger.fine(() -> MessageFormat.format("ID column of {0}, name: {1}, index: {2}", 
                entityType.getSimpleName(), this.idColumnName, this.idColumnIndex));
        
        if(idColumnIndex == -1) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void onSetValueAt(Object aValue, int rowIndex, int columnIndex) {
        
        final String columnName = this.getColumnName(columnIndex);
        
        try(final Update<T> dao = puContext.getDaoForUpdate(entityType)) {
            
            final Object idValue = this.getValueAt(rowIndex, this.idColumnIndex);
            
            logger.fine(() -> MessageFormat.format("For {0}\nSET {1} = {2}\nWHERE {3} = {4}", 
                    entityType.getName(), columnName, aValue, idColumnName, idValue));
        
            dao.from(entityType).where(idColumnName, idValue).set(columnName, aValue).executeUpdate();
        }
    }
}
