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

import com.bc.appcore.util.Pair;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.metadata.PersistenceUnitMetaData;
import com.bc.reflection.ReflectionUtil;
import com.bc.reflection.predicates.MethodIsGetter;
import com.bc.reflection.predicates.MethodIsSetter;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 18, 2017 8:40:21 PM
 */
public class EntityRelationImpl<T> implements EntityRelation<T> {
    
    private transient static final Logger logger = Logger.getLogger(EntityRelationImpl.class.getName());

    private final PersistenceUnitContext puContext;
    
    private final Map<Class, Method[]> entityMethods;
    
    private final List<String> columnNames;
    
    private final ReflectionUtil reflection;
    
    public EntityRelationImpl(
            PersistenceUnitContext puContext,
            Map<Class, Method[]> entityMethods, 
            List<String> columnNames) { 
        this.puContext = Objects.requireNonNull(puContext);
        this.entityMethods = Objects.requireNonNull(entityMethods);
        this.columnNames = Objects.requireNonNull(columnNames);
        this.reflection = new ReflectionUtil();
    }
    
    /**
     * Consider that the following method signatures are valid:
     * <pre><code>
     * Doc Task#getDoc(); 
     * String Doc#getSubject();
     * </code></pre>
     * <p>
     *   Given Entity entityType of Task.class and column name of subject, then this
     *   method returns <code>Pair&lt;Doc, Method&gt;</code> where the Method is 
     *   <code>Doc getDoc()</code>
     * </p>
     * @param refType
     * @param columnName
     * @param outputIfNone
     * @return 
     */
    @Override
    public Pair<Class, Method> getRelation(
            Class refType, String columnName, Pair<Class, Method> outputIfNone) {
        
        Pair<Class, Method> output = outputIfNone;
        
        final PersistenceUnitMetaData metaData = this.puContext.getMetaData();
        
        final Set<Class> puClasses = metaData.getEntityClasses();
        
        for(Class type : puClasses) {
            
            final String [] columns = metaData.getColumnNames(type);
            
            boolean contains = false;
            for(String column : columns) {
                if(column.equals(columnName)) {
                    contains = true;
                    break;
                }
            }
            
            if(contains) {
                final Method getter;
                if(type.equals(refType)) {
                    getter = this.getMethod(false, refType, columnName);
                }else{
                    getter = this.getMethod(false, refType, type);
                }
                if(getter != null) {
                    output = new Pair<>(type, getter);
                    break;
                }else{
                    logger.finer(() -> MessageFormat.format(
                            "{0} contains {1} but is not related to {2}", 
                            type.getName(), columnName, refType.getName()));
                }
            }
        }

        if(output != null && output != outputIfNone) {
            final Pair<Class, Method> pair = output;
            logger.finer(() -> MessageFormat.format(" Input: {0}#{1}\nOutput: {2}#{3}", 
                    refType.getName(), columnName, 
                    pair.key==null?null:pair.key.getName(), 
                    pair.value==null?null:pair.value.getName()
            ));
        }else{
            logger.fine(() -> "No relation found between: " + refType.getName() + " and: " + columnName);                    
        }
        return output;
    }

    public Method getMethod(boolean setter, Class entityType, String columnName) {
        return this.reflection.getMethodAlphaNumeric(setter, this.entityMethods.get(entityType), columnName);
    }
    
    public Method getMethod(boolean setter, Class entityType, Class targetEntityType) {
        if(entityType == targetEntityType) {
            throw new UnsupportedOperationException();
        }else{
            final Predicate<Method> methodTest = setter ? new MethodIsSetter() : new MethodIsGetter();
            Method selected = null;
            final Method [] methods = this.entityMethods.get(entityType);
            Objects.requireNonNull(methods, "No methods found for entity type: "+entityType.getName());
            for(Method method : methods) {
                if(methodTest.test(method) && method.getReturnType().equals(targetEntityType)) {
                    selected = method;
                    break;
                }
            }
            if(selected == null) {
                if(logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "No method found having return type: {0} in methods: {1}", 
                            new Object[]{targetEntityType.getName(), Arrays.toString(methods)});
                }
            }
            return selected;
        }    
    }
    
    public String getColumnName(int columnIndex) {
        return columnNames.get(columnIndex);
    }
}
