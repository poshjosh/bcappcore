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

import com.bc.appcore.typeprovider.TypeProvider;
import com.bc.jpa.EntityUpdater;
import com.bc.sql.SQLUtils;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.appcore.typeprovider.MemberTypeProvider;
import com.bc.appcore.util.RelationAccess;
import java.util.Collection;
import com.bc.appcore.AppCore;
import com.bc.appcore.util.Pair;
import com.bc.jpa.context.PersistenceUnitContext;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import com.bc.jpa.dao.Delete;
import com.bc.jpa.metadata.PersistenceUnitMetaData;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 25, 2017 10:10:29 AM
 */
public class EntityResultModelImpl<T> implements EntityResultModel<T> {
    
    private final class UpdateConfig {
        public final int row;
        public final int column;
        public final Object entity; 
        public final String entityColumn;
        public final Object target;
        public final String targetColumn;
        public final Object targetValue;    
        public final String actionId;
        public UpdateConfig(
                int row, int column,
                Object entity, String entityColumn, 
                Object target, String targetColumn, 
                Object targetValue, String actionId) {
            this.row = row;
            this.column = column;
            this.entity = Objects.requireNonNull(entity);
            this.entityColumn = Objects.requireNonNull(entityColumn);
            this.target = Objects.requireNonNull(target);
            this.targetColumn = Objects.requireNonNull(targetColumn);
            this.targetValue = targetValue;
            this.actionId = Objects.requireNonNull(actionId);
        }
    }
    
    private transient static final Logger logger = Logger.getLogger(EntityResultModelImpl.class.getName());
    
    private final List<UpdateConfig> updateQueue;
    
    public static final String PERSIST = "Persist";
    public static final String MERGE = "Merge";
    public static final String REMOVE = "Remove";

    private final int serialColumnIndex;
    
    private final String serialColumnName;
    
    private final AppCore app;
    
    private final PersistenceUnitContext puContext;
    
    private final Class<T> entityType;
    
    private final List<String> columnNames;
    
    private final List<String> columnLabels;
    
    private final Map<String, Class> columnTypes;
    
    private final Map<Class, EntityUpdater> entityUpdaters;
    
    private final MemberTypeProvider typeProvider;
    
    private final BiPredicate<String, Object> updateFilter;
    
    private final BiConsumer<String, Exception> updateExceptionHandler;
    
    private final EntityRelation entityRelation;
    
    public EntityResultModelImpl(AppCore app, Class<T> coreEntityType, 
            List<String> columnNames, 
            BiPredicate<String, Object> updateFilter,
            BiConsumer<String, Exception> updateExceptionHandler) {
        this(app, coreEntityType, columnNames, 
                app.getOrException(TypeProvider.class),
                updateFilter, updateExceptionHandler);
    }

    public EntityResultModelImpl(AppCore app, Class<T> coreEntityType, 
            List<String> columnNames, 
            TypeProvider typeProvider, 
            BiPredicate<String, Object> updateFilter, 
            BiConsumer<String, Exception> updateExceptionHandler) {

        this.app = Objects.requireNonNull(app);
        this.entityType = Objects.requireNonNull(coreEntityType);
        this.updateFilter = Objects.requireNonNull(updateFilter);
        this.updateExceptionHandler = Objects.requireNonNull(updateExceptionHandler);
        this.columnNames = Collections.unmodifiableList(columnNames);
        this.typeProvider = typeProvider;
        
        this.updateQueue = new ArrayList<>();
        
        if(logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Serial column index: {0}, column names: {1}",
                    new Object[]{columnNames.indexOf(app.getSerialColumnName()), columnNames});
        }
        
        this.puContext = this.app.getActivePersistenceUnitContext();
        
        final Map<Class, Set<String>> entityColNames = new HashMap();
        
        final Map<Class, EntityUpdater> updaters = new HashMap();
        
        final Map<Class, Method[]> entityMethodMappings = new HashMap();
        
        final PersistenceUnitMetaData puMetaData = this.puContext.getMetaData();
        
        final Set<Class> puClasses = puMetaData.getEntityClasses();

        for(Class puClass : puClasses) {

            updaters.put(puClass, this.puContext.getEntityUpdater(puClass));

            final Set<String> entityCols = new HashSet(Arrays.asList(puMetaData.getColumnNames(puClass)));

            entityColNames.put(puClass, entityCols);

            entityMethodMappings.put(puClass, puClass.getMethods());
        }    
        
        this.entityRelation = new EntityRelationImpl(
                this.puContext, entityMethodMappings, columnNames);
        
        logger.fine(() -> "Column names: " + entityColNames);
        
        this.entityUpdaters = Collections.unmodifiableMap(updaters);
        this.columnLabels = EntityResultModelImpl.this.getColumnLabels(app, columnNames);
        
        this.columnTypes = Collections.unmodifiableMap(EntityResultModelImpl.this.getColumnTypes(coreEntityType, columnNames));
        
        this.serialColumnName = this.columnNames.contains(app.getSerialColumnName()) ? app.getSerialColumnName() : null;
        
        this.serialColumnIndex = this.serialColumnName == null ? -1 : this.columnNames.indexOf(this.serialColumnName);
        
        if(logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Column types: {0}\nEntity updaters keySet: {1}\nColumn labels: {2}", 
                    new Object[]{this.columnTypes, this.entityUpdaters.keySet(), this.columnLabels});
        }
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
        
        final Object value;
        
        if(columnName.equals(this.getSerialColumnName()) || 
                columnName.equals(this.getSerialColumnLabel())) {
            
            value = rowIndex + 1;
            
        }else{
            
            final Object target;
            if(this.isDirectlyRelated(entity, columnIndex)) {
                target = entity;
            }else{    
                target = entityRelation.getTargetEntity(entity, columnName);
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
        
        logger.fine(() -> MessageFormat.format("Value type: {0}, value: {1}",
                value==null?null:value.getClass().getName(), value));
        
        if(columnIndex == this.serialColumnIndex) {
            throw new IllegalArgumentException();
        }
        
        final Pair<Object, String> relation = this.getEntityRelation(entity, rowIndex, columnIndex, value);

        final Object target = relation.key;
        
        final String targetCol = relation.value;

        final EntityUpdater updater = this.entityUpdaters.get(target.getClass());

        final Object oldValue = updater.getValue(target, targetCol);

        logger.fine(() -> MessageFormat.format("[{0}:{1}] = {2}#{3}\n{4}#{5}, to be updated to: {6} from: {7}", 
                rowIndex, columnIndex, entity, columnName, target, targetCol, value, oldValue));

        this.update_internal(rowIndex, columnIndex, 
                entity, columnName, oldValue, target, targetCol, value);
        
        return oldValue;
    }
    
    public boolean isDirectlyRelated(Object entity, int columnIndex) {
        
        final String columnName = this.getColumnName(columnIndex);

        Object rawValue;
        try{
            rawValue = this.entityUpdaters.get(entity.getClass()).getValue(entity, columnName);
        }catch(IllegalArgumentException | UnsupportedOperationException e) {
            rawValue = e;
        }

        final Class columnClass = this.getColumnClass(columnIndex);

        final boolean directlyRelated = rawValue == null || columnClass.isAssignableFrom(rawValue.getClass());
        
        logger.finer(() -> "Directly related: " + directlyRelated + ", " + entity + '#' + columnName);
        
        return directlyRelated;
    }
    
    public Pair<Object, String> getEntityRelation(T entity, 
            int rowIndex, int columnIndex, Object value) {
        
        final String columnName = this.getColumnName(columnIndex);

        final Object target = entityRelation.getTargetEntity(entity, columnName);
        
        return new Pair(target, columnName);
    }

    public int getPos(String columnName) {
        return 0;
    }

    private boolean update_internal(int row, int column, 
            Object entity, String entityColumn, Object entityValue, 
            Object target, String targetColumn, Object targetValue) {
        
        if(this.acceptForUpdate(target, targetColumn, entityValue, targetValue)) {
                 
            return this.update(row, column, 
                    entity, entityColumn, entityValue, target, targetColumn, targetValue);
        }else{
            
            return false;
        }
    }
    
    public boolean acceptForUpdate(Object target, String targetCol, Object oldValue, Object value) {
        
        final boolean accept = target != null && !Objects.equals(value, oldValue);
        
        return accept;
    }
    
    public boolean update(int row, int column, 
            Object entity, String entityColumn, Object entityValue, 
            Object target, String targetColumn, Object targetValue) {
     
        if(updateFilter.test(targetColumn, targetValue)) {
            
            if(logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Setting {0} = {1} {2} for {3}", 
                        new Object[]{targetColumn, targetValue == null ? "" : targetValue.getClass().getSimpleName(), targetValue, target});
            }

            final String actionId = this.getUpdateActionId(target, targetColumn, targetValue);
            
            targetValue = this.formatBeforeUpdate(target, targetColumn, targetValue);

            final UpdateConfig updateConfig = new UpdateConfig(
                    row, column, entity, entityColumn, target, targetColumn, targetValue, actionId);
            
            this.updateQueue.add(updateConfig);
            
            this.updateLocal(updateConfig);
            
            return true;
            
        }else{
            
            return false;
        }
    }
    
    @Override
    public boolean isPendingUpdate(int row, int column) {
        for(UpdateConfig cfg : updateQueue) {
            if(row == cfg.row & column == cfg.column) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int update() {
        
        synchronized(updateQueue) {
            
            int updateCount = 0;
            
            final Iterator<UpdateConfig> iter = updateQueue.iterator();
            
            while(iter.hasNext()) {
                
                final UpdateConfig updateConfig = iter.next();
                
                try{
                    
                    this.updateDatabase(updateConfig);
                    
                    ++updateCount;
                    
                }catch(RuntimeException e) {

                    this.updateExceptionHandler.accept(updateConfig.targetColumn, e);
                }
                
                iter.remove();
            }
            
            return updateCount;
        }
    }
    
    public String getUpdateActionId(Object entity, String columnName, Object columnValue) {
        
        final EntityUpdater updater = this.entityUpdaters.get(entity.getClass());
        
        final Object idVal = updater.getId(entity);
        
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Entity: {0}, id: {1}", new Object[]{entity, idVal});
        }
        
        return idVal == null ? PERSIST: MERGE;
    }
    
    public Object formatBeforeUpdate(Object target, String targetColumn, Object targetValue) {
        return targetValue;
    }
    
    private void updateLocal(UpdateConfig updateConfig) {
        
        final Class targetClass = updateConfig.target.getClass();
        
        final EntityUpdater targetUpdater = this.entityUpdaters.get(updateConfig.target.getClass());
        
        switch(updateConfig.actionId) {
            
            case PERSIST:
                targetUpdater.setValue(updateConfig.target, updateConfig.targetColumn, updateConfig.targetValue);
                break;
                
            case MERGE:
                targetUpdater.setValue(updateConfig.target, updateConfig.targetColumn, updateConfig.targetValue);
                break;
                
            case REMOVE:
                this.remove(targetClass, updateConfig.target); 
                this.updateEntityRelations(updateConfig.entity, updateConfig.target, updateConfig.actionId);
                break;
                
            default: throw new UnsupportedOperationException(
                    "Unsupported action: " + updateConfig.actionId);    
        }
    }
    
    private void updateDatabase(UpdateConfig updateConfig) {
        
        final Class targetClass = updateConfig.target.getClass();
        
        switch(updateConfig.actionId) {
            
            case PERSIST:
                this.persist(targetClass, updateConfig.target);
                this.updateEntityRelations(updateConfig.entity, updateConfig.target, updateConfig.actionId);
                break;
                
            case MERGE:
                this.merge(targetClass, updateConfig.target); 
                this.updateEntityRelations(updateConfig.entity, updateConfig.target, updateConfig.actionId);
                break;
                
            case REMOVE:
                this.remove(targetClass, updateConfig.target); 
                this.updateEntityRelations(updateConfig.entity, updateConfig.target, updateConfig.actionId);
                break;
                
            default: throw new UnsupportedOperationException(
                    "Unsupported action: " + updateConfig.actionId);    
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
        app.getActivePersistenceUnitContext().getDao().begin().mergeAndClose(entity);
    }
    
    public void persist(Class entityClass, Object entity) {
        app.getActivePersistenceUnitContext().getDao().begin().persistAndClose(entity);
    }
    
    public void remove(Class entityClass, Object entity) {
        final Object idValue = this.entityUpdaters.get(entityClass).getId(entity);
        try(final Delete dao = app.getActivePersistenceUnitContext().getDao().forDelete(entityClass)) {
            final Object managed = dao.find(entityClass, idValue);
            entity = managed;
            if(logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "Entity type: {0}, id: {1}, entity: {2}", 
                        new Object[]{entityClass.getName(), idValue, entity});
            }
            if(entity != null) {
                dao.begin().remove(entity).commit();
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
    
    public Map<String, Class> getColumnTypes(Class coreEntityType, List<String> columnNames) {
//System.out.println("Entity type: " + coreEntityType.getName() + ", columns: " + columnNames);                    
        final Map<String, Class> colTypes = new HashMap<>();
        for(String columnName : columnNames) {
            
            final Class colClass;
            if(columnName.equals(app.getSerialColumnName())) {
                colClass = Integer.class;
            }else{
                if(this.typeProvider != null) {
                    final Class columnType = this.typeProvider.getType(coreEntityType, columnName, null, null);
//System.out.println("Entity type: " + coreEntityType.getName() + ", column: " + columnName + ", column type: " + columnType);                    
                    if(columnType == null) {
                        colClass = this.getColumnType(coreEntityType, columnName, Object.class);
//System.out.println("xxx1 Entity type: " + coreEntityType.getName() + ", column: " + columnName + ", column type: " + colClass);                    
//                        final List<Class> columnTypeList = this.typeProvider.getTypeList(columnName, null);
//System.out.println("Entity type: " + coreEntityType.getName() + ", column: " + columnName + ", column type list: " + columnTypeList);                    
//                        final Predicate<Class> acceptEntityType = (cls) -> cls.getAnnotation(Entity.class) != null;
//                        final Optional<Class> optionalFirst = columnTypeList.stream().filter(acceptEntityType).findFirst();
//System.out.println("Entity type: " + coreEntityType.getName() + ", column: " + columnName + ", found column type: " + optionalFirst);                    
//                        if(optionalFirst.isPresent()) {
//                            colClass = optionalFirst.get();
//                        }else{
//                            colClass = this.getColumnType(coreEntityType, columnName, Object.class);
//System.out.println("xxx1 Entity type: " + coreEntityType.getName() + ", column: " + columnName + ", column type: " + colClass);                    
//                        }
                    }else{
                        colClass = columnType;
                    }
                }else{
                    colClass = this.getColumnType(coreEntityType, columnName, Object.class);
//System.out.println("xxx2 Entity type: " + coreEntityType.getName() + ", column: " + columnName + ", column type: " + colClass);                    
                }
            }
            
            colTypes.put(columnName, colClass);
        }
        
        return colTypes;
    }
    
    public Class getColumnType(Class type, String columnName, Class outputIfNone) {
        
        final Pair<Class, Method> pair = entityRelation.getRelation(type, columnName, null);

        if(pair == null) {
            return outputIfNone;
        }

        final Class targetType = pair.key;
        
        final int colIndex = this.puContext.getMetaData().getColumnIndex(targetType, columnName);
        if(colIndex == -1) {
            throw new IllegalArgumentException("Index == -1 for column: " + columnName + " in type: " + targetType.getName());
        }
        
        final int colDataType;
        try{
            final int [] dataTypes = this.puContext.getMetaData().getColumnDataTypes(targetType);
            colDataType = dataTypes[colIndex];
        }catch(IndexOutOfBoundsException shouldNotHappen) {
            logger.warning(() -> "Type: " + type.getName() + ", columnName: " + columnName + 
                    "\nTarget type: " + targetType.getName() + ", column index: " + colIndex);
            throw shouldNotHappen;
        }
        final Class colClass = SQLUtils.getClass(colDataType, null);
//        System.out.println("Type: " + type.getName() + ", columnName: " + columnName + 
//                "\nTarget type: " + targetType.getName() + ", column index: " + colIndex +
//                "\ncolumn data type: " + colDataType + ", column type: " + colClass);

        logger.finer(() -> MessageFormat.format("{0}#{1} has type: {2}", 
                targetType.getName(), columnName, 
                colClass == null ? null : colClass.getSimpleName()
        ));
        
        return colClass == null ? outputIfNone : colClass;
    }

    private void requireNonNull(Object oval, String columnName) {
        Objects.requireNonNull(oval, "Unexpected column name: "+columnName);
    }
    
    public String getSerialColumnLabel() {
        final int index = this.getSerialColumnIndex();
        return index == -1 ? null : this.columnLabels.get(index);
    }

    public String getSerialColumnName() {
        return this.serialColumnName;
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
