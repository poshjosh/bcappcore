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

package com.bc.appcore.jpa.model;

import com.bc.config.Config;
import com.bc.reflection.TypeProvider;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on May 24, 2017 2:44:13 PM
 */
public class ColumnLabelProviderImpl implements Serializable, ColumnLabelProvider {

    private transient static final Logger LOG = 
            Logger.getLogger(ColumnLabelProviderImpl.class.getName());

    private final Config config;
    
    private final TypeProvider typeProvider;
    
    private final String propertyPrefix;

    public ColumnLabelProviderImpl(Config config, TypeProvider typeProvider) {
        this(config, typeProvider, "columnLabel");
    }
    
    public ColumnLabelProviderImpl(Config config, TypeProvider typeProvider, String columnLabelPropertyPrefix) {
        this.config = Objects.requireNonNull(config);
        this.typeProvider = Objects.requireNonNull(typeProvider);
        this.propertyPrefix = Objects.requireNonNull(columnLabelPropertyPrefix);
    }
    
    @Override
    public String getColumnLabel(Class entityType, String columnName) {
        Objects.requireNonNull(entityType);
        return this.getColumnLabel(Collections.singleton(entityType), columnName);
    }
    
    @Override
    public String getColumnLabel(String columnName) {
        final long tb4 = System.currentTimeMillis();
        final long mb4 = com.bc.util.Util.availableMemory();
        
        final List<Class> entityTypes = this.typeProvider.getParentTypeList(columnName, null);
        final String columnLabel = this.getColumnLabel(entityTypes, columnName);
        
        LOG.finer(() -> MessageFormat.format(
                "Computed label: {0} for name: {1}. Consumed time: {2}, memory: {3}", 
                 columnLabel, columnName, (System.currentTimeMillis()-tb4), com.bc.util.Util.usedMemory(mb4))
        );
        
        return columnLabel;
    }

    public String getColumnLabel(Collection<Class> entityTypes, String columnName) {
//System.out.println("==================================================="+this.getClass());        
//System.out.println("Entity types: "+entityTypes+", columnName: "+columnName);        
        String label = null;
        for(Class type : entityTypes) {
            label = config.getString(propertyPrefix + '.' + type.getName() + '.' + columnName, null);
            if(label != null) {
                break;
            }
        }
        if(label == null) {
            for(Class type : entityTypes) {
                label = config.getString(propertyPrefix + '.' + type.getSimpleName() + '.' + columnName, null);
                if(label != null) {
                    break;
                }
            }
        }    
        if(label == null) {
            label = config.getString(propertyPrefix + '.' + columnName, null);
            if(label == null) {
                label = Character.toUpperCase(columnName.charAt(0)) + columnName.substring(1);
            }
        }
//System.out.println("Column. name: "+columnName+", label: "+label);        
        return label;
    }
}
