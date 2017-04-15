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

import com.bc.appcore.AppCore;
import com.bc.appcore.TypeProvider;
import com.bc.appcore.predicates.AcceptAll;
import com.bc.config.Config;
import com.bc.jpa.EntityUpdater;
import com.bc.jpa.JpaMetaData;
import com.bc.sql.SQLUtils;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.appcore.jpa.SelectionContext;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 25, 2017 10:10:29 AM
 */
public class ResultModelImpl<T> implements ResultModel<T> {
    
    public class Pair<K, V>{
        public final K key;
        public final V value;
        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
    
    private transient static final Logger logger = Logger.getLogger(ResultModelImpl.class.getName());

    private final AppCore app;
    
    private final JpaMetaData metaData;
    
    private final Class<T> type;
    
    private final List<String> columnNames;
    
    private final List<String> columnLabels;
    
    private final int serialColumnIndex;
    
    private final Map<String, Class> columnTypes;
    
    private final Map<Class, Set<String>> entityColumnNames;
    
    private final Map<Class, EntityUpdater> entityUpdaters;
    
    private final Map<Class, Method[]> entityMethods;
    
    private final TypeProvider typeProvider;
    
    public ResultModelImpl(AppCore app, Class<T> coreEntityType, 
            List<String> columnNames, int serialColumnIndex) {
        this(app, coreEntityType, columnNames, serialColumnIndex, 
                app.get(TypeProvider.class), new AcceptAll());
    }

    public ResultModelImpl(AppCore app, Class<T> coreEntityType, List<String> columnNames, 
            int serialColumnIndex, TypeProvider typeProvider, Predicate<String> persistenceUnitTest) {
        this.app = Objects.requireNonNull(app);
        this.type = Objects.requireNonNull(coreEntityType);
        this.metaData = this.app.getJpaContext().getMetaData();
        this.columnNames = Collections.unmodifiableList(columnNames);
        this.serialColumnIndex = serialColumnIndex;
        this.typeProvider = typeProvider;
        if(logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Serial column index: {0}, column names: {1}",
                    new Object[]{serialColumnIndex, columnNames});
        }
        
        final Map<Class, Set<String>> entityColNames = new HashMap();
        
        final Map<Class, EntityUpdater> updaters = new HashMap();
        
        final Map<Class, Method[]> entityMethodMappings = new HashMap();
        
        final String [] puNames = metaData.getPersistenceUnitNames();
        
        for(String puName : puNames) {
            
            if(!persistenceUnitTest.test(puName)) {
                continue;
            }
            
            final Class [] puClasses = metaData.getEntityClasses(puName);
            
            for(Class puClass : puClasses) {
                
                updaters.put(puClass, app.getJpaContext().getEntityUpdater(puClass));
                
                final Set<String> entityCols = new HashSet(Arrays.asList(metaData.getColumnNames(puClass)));
                
                for(String columnName : columnNames) {
                    if(entityCols.contains(columnName)) {
                        
                        entityMethodMappings.put(puClass, puClass.getMethods());
                        entityColNames.put(puClass, entityCols);
                    }
                }
            }
        }
        this.entityColumnNames = Collections.unmodifiableMap(entityColNames);
        this.entityUpdaters = Collections.unmodifiableMap(updaters);
        this.entityMethods = Collections.unmodifiableMap(entityMethodMappings);
        this.columnLabels = ResultModelImpl.this.getColumnLabels(app, columnNames);
        
        this.columnTypes = Collections.unmodifiableMap(this.getColumnTypes(coreEntityType, columnNames));
        
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Column types: {0}\nEntity updaters keySet: {1}\nEntity methods keySet: {2}\nColumn labels: {3}", 
                    new Object[]{this.columnTypes, this.entityUpdaters.keySet(), this.entityMethods.keySet(), this.columnLabels});
        }
//        logger.log(Level.INFO, "Column types: {0}", this.columnTypes);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex != this.serialColumnIndex;
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        final Class columnClass;
        if(columnIndex == this.getSerialColumnIndex()) {
            columnClass = Integer.class;
        }else{
            final String columnName = this.getColumnName(columnIndex);
            columnClass = this.columnTypes.get(columnName);
            this.requireNonNull(columnClass, columnName);
        }    
        return columnClass;
    }
  
    @Override
    public Object get(T entity, int rowIndex, int columnIndex) {
        final String columnName = this.getColumnName(columnIndex);
        final Object value = this.get(entity, rowIndex, columnName);
        return value;
    }
    
    @Override
    public Object get(T entity, int rowIndex, String columnName) {
        
        
        final Object value;
        
        if(columnName.equals(this.getSerialColumnName())) {
            
            value = rowIndex + 1;
            
        }else{

            final Object target = getEntity(entity, columnName);
              
            final EntityUpdater updater = this.entityUpdaters.get(target.getClass());
            
            this.requireNonNull(updater, columnName);
            
            final Object updaterValue = updater.getValue(target, columnName);
            
            if(updaterValue != null && 
                    this.entityUpdaters.keySet().contains(updaterValue.getClass())) {
                
                value = this.getTextRepresentation(updaterValue, updaterValue);
                
                if(logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "{0} @[{1}:{2}] representation: {3}", 
                            new Object[]{updaterValue, rowIndex, columnName, value});
                }
            }else{
                
                value = updaterValue;
            }
        }
        
        return value;
    }
    
    @Override
    public Object set(T entity, int rowIndex, int columnIndex, Object value) {
        final String columnName = this.getColumnName(columnIndex);
        final Object oldValue = this.set(entity, rowIndex, columnName, value);
        return oldValue;
    }

    @Override
    public Object set(T entity, int rowIndex, String columnName, Object value) {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Value type: {0}, value: {1}",
                    new Object[]{value==null?null:value.getClass().getName(), value});
        }
        
        final Pair<Object, String> entityPair = this.getEntityRelation(entity, rowIndex, columnName, value);
        
        final Object target = entityPair.key;
        
        final String targetCol = entityPair.value;
        
        final EntityUpdater updater = this.entityUpdaters.get(target.getClass());
        
        final Object oldValue = updater.getValue(target, targetCol);

        final boolean updated = this.update(target, targetCol, oldValue, value);
        
        return oldValue;
    }
    
    public boolean update(Object target, String columnName, Object oldValue, Object value) {
        final boolean update;
        if(target != null) {
            if(value == null && oldValue == null) {
                update = false;
            }else if(value != null && oldValue != null) {
                update = !value.equals(oldValue);
            }else{
                update = true;
            }
        }else{
            update = false;
        }
        if(update) {
            this.update(target, columnName, value);
        }
        return update;
    }
    
    public Pair<Object, String> getEntityRelation(T entity, int rowIndex, String columnName, Object value) {
        final Object target;
        if(columnName.equals(this.getSerialColumnName())) {
            target = null;
            columnName = null;
        }else{
            target = this.getEntity(entity, columnName);
        }
        return new Pair(target, columnName);
    }
    
    public Object getEntity(Object ref, String columnName) {
        
        final Pair<Class, Method> pair = this.getRelation(
                ref.getClass(), columnName, (Pair<Class, Method>)null);
        
        final Class targetEntityType = pair.key;
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Column: {0}, entity type: {1}", 
                    new Object[]{columnName, targetEntityType});
        }

        this.requireNonNull(targetEntityType, columnName);

        final Object target;
        if(targetEntityType == ref.getClass()) {
            target = ref;
        }else{

            final Method getter = pair.value;

            if(logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "Entity type: {0}, target type: {1}, method: {2}", 
                        new Object[]{ref.getClass().getName(), targetEntityType.getName(), getter==null?null:getter.getName()});
            }

            try{
                target = getter.invoke(ref);
            }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            if(logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "{0}#{1} with method {2} returned: {3}", 
                        new Object[]{ref, columnName, getter.getName(), target});
            }
        }
        
        return target;
    }
    
    public Object getTextRepresentation(Object entity, Object outputIfNone) {
        final SelectionContext selectionValues;
        try{
            selectionValues = app.get(SelectionContext.class);
        }catch(RuntimeException e) {
            logger.log(Level.WARNING, "Failed to instantiate type: {0}, reason: "+e, SelectionContext.class);
            return outputIfNone;
        }
        final String col = selectionValues.getSelectionColumn(entity.getClass(), null);
        if(col == null) {
            return outputIfNone;
        }else{
            return this.entityUpdaters.get(entity.getClass()).getValue(entity, col);
        }
    }
    
    public int getPos(String columnName) {
        return 0;
    }
    
    public void update(Object entity, String columnName, Object value) {
        
        final Class entityClass = entity.getClass();
        
        if(logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Setting {0} = {1} {2} for {3}", 
                    new Object[]{columnName, value.getClass().getSimpleName(), value, entity});
        }
        
        final EntityUpdater updater = this.entityUpdaters.get(entityClass);
        
        final Object idVal = updater.getId(entity);
        updater.setValue(entity, columnName, value);
        if(idVal != null) {
            app.getJpaContext().getDao(entityClass).begin().mergeAndClose(entity);
            app.getSlaveUpdates().addMerge(entity);
        }else{
            try{
                app.getJpaContext().getDao(entityClass).begin().persistAndClose(entity);
                app.getSlaveUpdates().addPersist(entity);
            }catch(Exception ignored) {
                app.getJpaContext().getDao(entityClass).begin().mergeAndClose(entity);
                app.getSlaveUpdates().addMerge(entity);
            }
        }
    }
    
    public <E> List<E> filter(List<E> list, Predicate<E> test) {
        final List<E> output;
        if(list == null || list.isEmpty()) {
            output = Collections.EMPTY_LIST;
        }else{
            output = new LinkedList();
            for(E e : list) {
                if(test == null || test.test(e)) {
                    output.add(e);
                }
            }
        }
        return output;
    }
    
    public <E> E getFromEnd(List<E> list, String columnName, int index, int viewSize) {
        E output;
        if(list == null || list.isEmpty() || list.size() <= index) {
            output = null;
        }else{
            final int n = list.size() - viewSize;
            final int offset = n < 0 ? 0 : n;
            final List<E> view = list.subList(offset, list.size());
            if(view.isEmpty() || view.size() <= index) {
                output = null;
            }else{
                output = view.get(index);
            }
        }
        return output;
    }
    
    public List<String> getColumnLabels(AppCore app, List<String> colNames) {
        final List<String> labels = new ArrayList<>(colNames.size());
        for(String columnName : colNames) {
            
            final String label = this.getColumnLabel(columnName);
            
            labels.add(label);
        }
        return labels;
    }
    
    public String getColumnLabelPropertyPrefix() {
        return "columnLabel";
    }
    
    public String getColumnLabel(String columnName) {
        final String prefix = this.getColumnLabelPropertyPrefix();
        String label = null;
        final Config config = app.getConfig();
        if(!columnName.equals(this.getSerialColumnName())) {
            final List<Class> entityClasses = this.getEntityClasses(columnName);
            for(Class type : entityClasses) {
                label = config.getString(prefix + '.' + type.getName() + '.' + columnName, null);
                if(label != null) {
                    break;
                }
            }
            if(label == null) {
                for(Class type : entityClasses) {
                    label = config.getString(prefix + '.' + type.getSimpleName() + '.' + columnName, null);
                    if(label != null) {
                        break;
                    }
                }
            }    
        }
        if(label == null) {
            label = config.getString(prefix + '.' + columnName, null);
            if(label == null) {
                label = Character.toUpperCase(columnName.charAt(0)) + columnName.substring(1);
            }
        }
        return label;
    }
    
    /**
     * Consider that the following method signatures are valid:
     * <pre><code>
     * Doc Task#getDoc(); 
     * String Doc#getSubject();
     * </code></pre>
     * <p>
     * Given Entity type of Task.class and column name of subject, then this
     * method returns <code>Pair&lt;Doc, Method&gt;</code> where the Method is 
     * <code>Doc getDoc()</code>
     * </p>
     * @param refType
     * @param columnName
     * @param outputIfNone
     * @return 
     */
    public Pair<Class, Method> getRelation(
            Class refType, String columnName, Pair<Class, Method> outputIfNone) {
        Pair<Class, Method> output = outputIfNone;
        final Set<Entry<Class, Set<String>>> entrySet = this.entityColumnNames.entrySet();
        for(Entry<Class, Set<String>> entry : entrySet) {
            Class entityType = entry.getKey();
            Set<String> entityCols = entry.getValue();
            if(entityCols.contains(columnName)) {
                final Method getter;
                if(entityType.equals(refType)) {
                    getter = this.getMethod(false, refType, columnName);
                }else{
                    getter = this.getMethod(false, refType, entityType);
                }
                if(getter != null) {
                    output = new Pair<>(entityType, getter);
                    break;
                }else{
                    if(logger.isLoggable(Level.FINER)) {
                        logger.log(Level.FINER, "{0} contains {1} but is not connected to {2}", 
                                new Object[]{entityType.getName(), columnName, refType.getName()});
                    }
                }
            }
        }
        if(output != outputIfNone) {
            if(logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "{0}#{1} is connected to {2}#{3}", 
                        new Object[]{refType, columnName, 
                            output.key==null?null:output.key.getName(), output.value
                        });
            }
        }
        return output;
    }
    
    public List<Class> getEntityClasses(String columnName) {
        List<Class> entityClasses = new ArrayList<>();
        final Set<Entry<Class, Set<String>>> entrySet = this.entityColumnNames.entrySet();
        for(Entry<Class, Set<String>> entry : entrySet) {
            Class entityType = entry.getKey();
            Set<String> entityCols = entry.getValue();
            if(entityCols.contains(columnName)) {
                entityClasses.add(entityType);
            }
        }
        return entityClasses;
    }

    public Method getMethod(boolean setter, Class entityType, String columnName) {
        return this.entityUpdaters.get(entityType).getMethod(setter, columnName);
    }
    
    public Method getMethod(boolean setter, Class entityType, Class targetEntityType) {
        if(entityType == targetEntityType) {
            throw new UnsupportedOperationException();
        }else{
            final String prefix = setter ? "set" : "get";
            Method getter = null;
            final Method [] methods = this.entityMethods.get(entityType);
            for(Method method : methods) {
                if(method.getName().startsWith(prefix) && method.getReturnType().equals(targetEntityType)) {
                    getter = method;
                    break;
                }
            }
            if(getter == null) {
                if(logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "No method found having return type: {0} in methods: {1}", 
                            new Object[]{targetEntityType.getName(), Arrays.toString(methods)});
                }
            }
            return getter;
        }    
    }
    
    public Map<String, Class> getColumnTypes(Class coreEntityType, List<String> columnNames) {
        final Map<String, Class> colTypes = new HashMap<>();
        for(String columnName : columnNames) {
            
            final Class colClass;
            if(columnName.equals(this.getSerialColumnName())) {
                colClass = Integer.class;
            }else{
                 if(typeProvider != null) {
                     colClass = typeProvider.getType(columnName, null, Object.class);
                 }else{
                     colClass = this.getColumnType(coreEntityType, columnName, Object.class);
                 }
            }
            
            colTypes.put(columnName, colClass);
        }
        
        return colTypes;
    }
    
    public Class getColumnType(Class coreEntityType, String columnName, Class outputIfNone) {
        
        final Pair<Class, Method> pair = this.getRelation(coreEntityType, columnName, null);

        if(pair == null) {
            logger.log(Level.FINE, "No connection found for: {0}#{1}", new Object[]{coreEntityType.getName(), columnName});                    
            return outputIfNone;
        }

        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "{0}#{1} is connected to {2} thus: {0}#{3}", 
                    new Object[]{
                        coreEntityType.getName(), columnName, 
                        pair.key==null?null:pair.key.getName(), pair.value
                    });
        }

        final Class entityType = pair.key;

        final int colIndex = metaData.getColumnIndex(entityType, columnName);
        final int colDataType = metaData.getColumnDataTypes(entityType)[colIndex];
        final Class colClass = SQLUtils.getClass(colDataType, null);
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "{0}#{1} has type: {2}", 
                    new Object[]{entityType.getName(), columnName, colClass == null ? null : colClass.getSimpleName()});
        }
        
        return colClass == null ? outputIfNone : colClass;
    }

    private void requireNonNull(Object oval, String columnName) {
        Objects.requireNonNull(oval, "Unexpected column name: "+columnName);
    }

    public String getSerialColumnName() {
        return this.serialColumnIndex == -1 ? null : this.columnNames.get(this.serialColumnIndex);
    }

    @Override
    public int getSerialColumnIndex() {
        return this.serialColumnIndex;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames.get(columnIndex);
    }

    @Override
    public String getColumnLabel(int columnIndex) {
        return columnLabels.get(columnIndex);
    }

    @Override
    public Set<String> getColumnNames() {
        return Collections.unmodifiableSet(new LinkedHashSet(columnNames));
    }

    @Override
    public Set<String> getColumnLabels() {
        return Collections.unmodifiableSet(new LinkedHashSet(columnLabels));
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    public AppCore getApp() {
        return app;
    }
    
    public Map<String, Class> getColumnTypes() {
        return this.columnTypes;
    }
}
/**
 * 
    public Map<String, Class> getColumnTypes(Class coreEntityType, List<String> columnNames) {
        final Map<String, Class> colTypes = new HashMap<>();
        for(String columnName : columnNames) {
            
            final Class colClass;
            if(columnName.equals(this.getSerialColumnName())) {
                colClass = Integer.class;
            }else{
                
                final Pair<Class, Method> pair = this.getRelation(coreEntityType, columnName, null);
                
                if(pair == null) {
                    logger.log(Level.FINE, "No connection found for: {0}#{1}", new Object[]{coreEntityType.getName(), columnName});                    
                    continue;
                }
                
                if(logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "{0}#{1} is connected to {2} thus: {0}#{3}", 
                            new Object[]{
                                coreEntityType.getName(), columnName, 
                                pair.key==null?null:pair.key.getName(), pair.value
                            });
                }
                
                final Class entityType = pair.key;
                
                final int colIndex = metaData.getColumnIndex(entityType, columnName);
                final int colDataType = metaData.getColumnDataTypes(entityType)[colIndex];
                colClass = SQLUtils.getClass(colDataType);
                if(logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "{0}#{1} has type: {2}", 
                            new Object[]{entityType.getName(), columnName, colClass.getSimpleName()});
                }
            }
            
            colTypes.put(columnName, colClass);
        }
        
        return colTypes;
    }
 * 
 */