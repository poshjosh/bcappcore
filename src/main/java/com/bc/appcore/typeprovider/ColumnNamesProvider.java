/*
 * Copyright 2018 NUROX Ltd.
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

package com.bc.appcore.typeprovider;

import com.bc.reflection.MemberNamesProvider;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.metadata.PersistenceMetaData;
import com.bc.jpa.metadata.PersistenceUnitMetaData;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 15, 2018 2:43:58 PM
 */
public class ColumnNamesProvider implements Serializable, MemberNamesProvider{

    private transient Map<Class, Set<String>> _typeColumnNames;
    
    private final PersistenceUnitContext puContext;

    public ColumnNamesProvider(PersistenceUnitContext puContext) {
        this.puContext = Objects.requireNonNull(puContext);
    }

    public Map<Class, Set<String>> getTypeColumnNames() {

        if(_typeColumnNames == null) {

            _typeColumnNames = this.fetchTypeColumNames();
        }

        return _typeColumnNames;
    }
    
    @Override
    public Set<String> get(Class type) {
        
        return this.get().get(type);
    }

    @Override
    public Map<Class, Set<String>> get() {

        return this.getTypeColumnNames();
    }
    
    public Map<Class, Set<String>> fetchTypeColumNames() {
        
        final Map<Class, Set<String>> output = new LinkedHashMap();
        
        final PersistenceMetaData metaData = puContext.getPersistenceContext().getMetaData();
        
        final String puName = puContext.getName();
            
        final Set<Class> puTypes = metaData.getEntityClasses(puName);

        final PersistenceUnitMetaData puMeta = metaData.getMetaData(puName);

        for(Class puType : puTypes) {

            final String [] colNamesArray = puMeta.getColumnNames(puType);
            final Set<String> colNamesSet = new LinkedHashSet(colNamesArray.length);
            for(String colName : colNamesArray) {
                colNamesSet.add(colName);
            }
            
            output.put(puType, colNamesSet.isEmpty() ? Collections.EMPTY_SET : Collections.unmodifiableSet(colNamesSet));
        }
        
        return output.isEmpty() ? Collections.EMPTY_MAP : Collections.unmodifiableMap(output);
    }
}
