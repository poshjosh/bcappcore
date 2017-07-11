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
import com.bc.appcore.typeprovider.TypeProvider;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.appcore.predicates.AcceptAll;
import com.bc.appcore.typeprovider.MemberTypeProvider;
import com.bc.appcore.util.RelationAccess;
import com.bc.jpa.dao.BuilderForDelete;
import java.util.Collection;
import java.util.Optional;
import javax.persistence.Entity;

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
    
    public static final String PERSIST = "Persist";
    public static final String MERGE = "Merge";
    public static final String REMOVE = "Remove";

    private final AppCore app;
    
    private final JpaMetaData metaData;
    
    private final Class<T> entityType;
    
    private final List<String> columnNames;
    
    private final List<String> columnLabels;
    
    private final int serialColumnIndex;
    
    private final Map<String, Class> columnTypes;
    
    private final Map<Class, Set<String>> entityColumnNames;
    
    private final Map<Class, EntityUpdater> entityUpdaters;
    
    private final Map<Class, Method[]> entityMethods;
    
    private final MemberTypeProvider typeProvider;
    
    public ResultModelImpl(AppCore app, Class<T> coreEntityType, 
            List<String> columnNames, int serialColumnIndex) {
        this(app, coreEntityType, columnNames, serialColumnIndex, 
                app.getOrException(TypeProvider.class), new AcceptAll());
    }

    public ResultModelImpl(AppCore app, Class<T> coreEntityType, List<String> columnNames, 
            int serialColumnIndex, TypeProvider typeProvider, Predicate<String> persistenceUnitTest) {
        this.app = Objects.requireNonNull(app);
        this.entityType = Objects.requireNonNull(coreEntityType);
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
        
        this.columnTypes = Collections.unmodifiableMap(ResultModelImpl.this.getColumnTypes(coreEntityType, columnNames));
        
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Column types: {0}\nEntity updaters keySet: {1}\nEntity methods keySet: {2}\nColumn labels: {3}", 
                    new Object[]{this.columnTypes, this.entityUpdaters.keySet(), this.entityMethods.keySet(), this.columnLabels});
        }
//        logger.log(Level.INFO, "Column types: {0}", this.columnTypes);
    }

    public List<String> getColumnLabels(AppCore app, List<String> colNames) {
        final ColumnLabelProvider columnLabelProvider = app.getOrException(ColumnLabelProvider.class);
        final List<String> labels = new ArrayList<>(colNames.size());
        for(String columnName : colNames) {
            final String label = columnLabelProvider.getColumnLabel(columnName);
            labels.add(label);
        }
        return labels;
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

            Object target = getEntity(entity, columnName);
            
//@todo target is not supposed to be null            
            if(target == null) {
                target = entity;
            }
              
            final EntityUpdater updater = this.entityUpdaters.get(target.getClass());
            
            this.requireNonNull(updater, columnName);
            
            final Object updaterValue = updater.getValue(target, columnName);
            
            if(logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST, rowIndex + " Ref: {0}, entity: {1}, name: {2}, value: {3}", 
                        new Object[]{entity, target, columnName, updaterValue});
            }
            
            value = updaterValue;
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

        if(logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, rowIndex + " For {0}#{1}, {2}#{3}, to be updated to: {4} from: {5}", 
                    new Object[]{entity, columnName, target, targetCol, value, oldValue});
        }

        if(this.acceptForUpdate(target, targetCol, oldValue, value)) {
                 
            this.update(entity, columnName, target, targetCol, value);
        }
        
        return oldValue;
    }
    
    public boolean acceptForUpdate(Object target, String targetCol, Object oldValue, Object value) {
        
        final boolean accept = target != null && !Objects.equals(value, oldValue);
        
        return accept;
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
        
        Objects.requireNonNull(pair, "Relation is null for: "+ref+"#"+columnName);
        
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
    public Pair<Class, Method> getRelation(
            Class refType, String columnName, Pair<Class, Method> outputIfNone) {
        Pair<Class, Method> output = outputIfNone;
        final Set<Entry<Class, Set<String>>> entrySet = this.entityColumnNames.entrySet();
        for(Entry<Class, Set<String>> entry : entrySet) {
            Class type = entry.getKey();
            Set<String> entityCols = entry.getValue();
            if(entityCols.contains(columnName)) {
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
                    if(logger.isLoggable(Level.FINER)) {
                        logger.log(Level.FINER, "{0} contains {1} but is not connected to {2}", 
                                new Object[]{type.getName(), columnName, refType.getName()});
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
    
    public int getPos(String columnName) {
        return 0;
    }
    
    public void update(Object entity, String entityColumn, Object target, String targetColumn, Object targetValue) {
        
        if(logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Setting {0} = {1} {2} for {3}", 
                    new Object[]{targetColumn, targetValue == null ? "" : targetValue.getClass().getSimpleName(), targetValue, target});
        }
        
        final String actionId = this.getUpdateActionId(target, targetColumn, targetValue);
                
        this.update(entity, entityColumn, target, targetColumn, targetValue, actionId);
    }
    
    public String getUpdateActionId(Object entity, String columnName, Object columnValue) {
        
        final EntityUpdater updater = this.entityUpdaters.get(entity.getClass());
        
        final Object idVal = updater.getId(entity);
        
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Entity: {0}, id: {1}", new Object[]{entity, idVal});
        }
        
        final boolean hasIdValue = idVal != null;
        
        return hasIdValue ? MERGE : PERSIST;
    }
    
    public void update(Object entity, String entityColumn, 
            Object target, String targetColumn, Object targetValue, String actionId) {
        
        final Class targetClass = target.getClass();
        
        switch(actionId) {
            
            case PERSIST:
                final EntityUpdater targetUpdater = this.entityUpdaters.get(target.getClass());
                targetUpdater.setValue(target, targetColumn, targetValue);
                try{
                    this.persist(targetClass, target);
                }catch(RuntimeException mayBeIgnored) {
                    try{
                        this.merge(targetClass, target);
                    }catch(RuntimeException ignored) {
                        throw mayBeIgnored;
                    }
                }
                this.updateEntityRelations(entity, target, actionId);
                break;
                
            case MERGE:
                final EntityUpdater targetUpdater2 = this.entityUpdaters.get(target.getClass());
                targetUpdater2.setValue(target, targetColumn, targetValue);
                this.merge(targetClass, target); 
                this.updateEntityRelations(entity, target, actionId);
                break;
                
            case REMOVE:
                this.remove(targetClass, target); 
                this.updateEntityRelations(entity, target, actionId);
                break;
                
            default: throw new UnsupportedOperationException("Unsupported action: "+actionId);    
        }
    }

    private void updateEntityRelations(Object entity, Object target, String actionId) {
        
        Collection updates;
        Object update;
        
        final RelationAccess relationAccess = app.getOrException(RelationAccess.class);

        final boolean collections = true;
        
        if(!REMOVE.equals(actionId)) {
            
            updates = relationAccess.updateAllWith(Collections.singleton(target), entity.getClass(), entity, collections);
            update = entity;
            if(updates.isEmpty()) {
                updates = relationAccess.updateAllWith(Collections.singleton(entity), target.getClass(), target, collections);
                update = target;
            }
        } else{
            
            updates = relationAccess.removeFromAll(Collections.singleton(target), entity.getClass(), entity, collections);
            update = entity;
            if(updates.isEmpty()) {
                updates = relationAccess.removeFromAll(Collections.singleton(entity), target.getClass(), target, collections);
                update = target;
            }
        }
        
        if(logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Updated: {0} with: {1}", new Object[]{updates, update});
        }
    }
    
    public void merge(Class entityClass, Object entity) {
        app.getJpaContext().getDao(entityClass).begin().mergeAndClose(entity);
        app.getSlaveUpdates().addMerge(entity);
    }
    
    public void persist(Class entityClass, Object entity) {
        app.getJpaContext().getDao(entityClass).begin().persistAndClose(entity);
        app.getSlaveUpdates().addPersist(entity);
    }
    
    public void remove(Class entityClass, Object entity) {
        final Object idValue = this.entityUpdaters.get(entityClass).getId(entity);
        try(final BuilderForDelete dao = app.getJpaContext().getBuilderForDelete(entityClass)) {
            final Object managed = dao.find(entityClass, idValue);
            entity = managed;
            if(logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "Entity type: {0}, id: {1}, entity: {2}", 
                        new Object[]{entityClass.getName(), idValue, entity});
            }
            if(entity != null) {
                dao.begin().remove(entity).commit();
                app.getSlaveUpdates().addRemove(entity);
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
            Objects.requireNonNull(methods, "No methods found for entity type: "+entityType.getName());
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
                if(this.typeProvider != null) {
                    final Class columnType = this.typeProvider.getType(coreEntityType, columnName, null, null);
                    if(columnType == null) {
                        final List<Class> columnTypeList = this.typeProvider.getTypeList(columnName, null);
                        final Predicate<Class> acceptEntityType = (cls) -> cls.getAnnotation(Entity.class) != null;
                        final Optional<Class> optionalFirst = columnTypeList.stream().filter(acceptEntityType).findFirst();
                        if(optionalFirst.isPresent()) {
                            colClass = optionalFirst.get();
                        }else{
                            colClass = this.getColumnType(coreEntityType, columnName, Object.class);
                        }
                    }else{
                        colClass = columnType;
                    }
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
    public List<String> getColumnNames() {
        return Collections.unmodifiableList(this.columnNames);
    }

    @Override
    public List<String> getColumnLabels() {
        return Collections.unmodifiableList(this.columnLabels);
    }

    @Override
    public Class<T> getEntityType() {
        return entityType;
    }

    public AppCore getApp() {
        return app;
    }
    
    public Map<String, Class> getColumnTypes() {
        return this.columnTypes;
    }
}
