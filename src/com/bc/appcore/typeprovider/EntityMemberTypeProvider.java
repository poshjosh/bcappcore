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

package com.bc.appcore.typeprovider;

import com.bc.jpa.EntityUpdater;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.util.ReflectionUtil;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on May 24, 2017 12:38:57 PM
 */
public class EntityMemberTypeProvider implements MemberTypeProvider {

    private static final Logger logger = Logger.getLogger(EntityMemberTypeProvider.class.getName());
    
    private final PersistenceUnitContext jpaContext;
    
    private final boolean columnNamesOnly;
    
    private final Map<Class, Set<String>> typeColumnNames;
    
    private final Map<Class, EntityUpdater> entityUpdaters;
    
    public EntityMemberTypeProvider(PersistenceUnitContext jpaContext, 
            Map<Class, Set<String>> typeColumnNames, boolean columnNamesOnly) {
        this.jpaContext = Objects.requireNonNull(jpaContext);
        this.typeColumnNames = Objects.requireNonNull(typeColumnNames);
        this.columnNamesOnly = columnNamesOnly;
        this.entityUpdaters = new HashMap();
    }
    
    @Override
    public List<Class> getTypeList(String name, Object value) {
        
        final Set<Class> output = new LinkedHashSet();
        
        final Set<Class> types = this.typeColumnNames.keySet();
        
        for(Class parentType : types) {
            
            final Class childType = this.getType(parentType, name, value, null);
            
            if(childType == null) {
                continue;
            }
            
            output.add(childType);
        }
        
        return new ArrayList(output);
    }

    @Override
    public Class getType(Class parentType, String name, Object value, Class outputIfNone) {
        final List<Type> types = this.getTypes(parentType, name, value, false);
        if(types.size() > 1) {
            throw new UnsupportedOperationException();
        }
        return types.isEmpty() ? outputIfNone : (Class)types.get(0);
    }

    @Override
    public List<Type> getGenericTypeArguments(Class parentType, String name, Object value) {
        return this.getTypes(parentType, name, value, true);
    }
    
    public List<Type> getTypes(Class parentType, String name, Object value, boolean generic) {
        
        Objects.requireNonNull(parentType);
        Objects.requireNonNull(name);
        
        final Set<String> columnNames = this.typeColumnNames.get(parentType);
        
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Name {0}, entityType: {1}, columns: {2}", 
                    new Object[]{name, parentType.getName(), columnNames});
        }

        final List<Type> output;
        
        if(!columnNamesOnly || columnNames.contains(name)) {

            if(logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "Found {0} in {1} having columns: {2}", 
                        new Object[]{name, parentType.getName(), columnNames});
            }

            final EntityUpdater updater = this.getEntityUpdater(parentType);

            final Method method = updater.getMethod(false, name);

            if(method == null) {
                
                if(logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "Getter method for {0} was not found in type: {1}", 
                            new Object[]{name, parentType});
                }
                
                output = Collections.EMPTY_LIST;
                
            }else{

                if(!generic) {
                    output = Collections.singletonList(method.getReturnType());
                }else{
                    final ReflectionUtil reflection = new ReflectionUtil();
                    output = Arrays.asList(reflection.getGenericReturnTypeArguments(method));
                }
            }
        }else{
            
            output = Collections.EMPTY_LIST;
        }
        
        return output;
    }
    
    public EntityUpdater getEntityUpdater(Class type) {
        EntityUpdater updater = this.entityUpdaters.get(type);
        if(updater == null) {
            updater = this.jpaContext.getEntityUpdater(type);
            this.entityUpdaters.put(type, Objects.requireNonNull(updater));
        }
        return updater;
    }
}
