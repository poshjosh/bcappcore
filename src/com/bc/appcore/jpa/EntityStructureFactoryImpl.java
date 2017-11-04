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
import com.bc.appcore.predicates.MethodHasParameterType;
import com.bc.appcore.predicates.MethodIsGetter;
import com.bc.appcore.predicates.MethodIsSetter;
import com.bc.jpa.EntityUpdater;
import com.bc.jpa.util.EntityFromMapBuilder;
import com.bc.jpa.util.EntityRecursionFilter;
import com.bc.util.JsonFormat;
import com.bc.util.MapBuilder;
import com.bc.util.ReflectionUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Entity;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 29, 2017 9:50:47 AM
 */
public class EntityStructureFactoryImpl implements EntityStructureFactory {

    private static final Logger logger = Logger.getLogger(EntityStructureFactoryImpl.class.getName());

    private static class MapBuilderDontRecurseSelectionTypes extends EntityRecursionFilter {
        private final SelectionContext sc;
        private MapBuilderDontRecurseSelectionTypes(SelectionContext selectionContext) {
            this.sc = selectionContext;
        }
        @Override
        public boolean test(Class type, Object value) {
            final boolean output;
            if(this.sc.isSelectionType(type)) {
                output = false;
            }else{
                output = super.test(type, value);
            }
            return output;
        }
    }

    private final AppCore app;
    
    private final EntityFromMapBuilder.Formatter formatter;

    public EntityStructureFactoryImpl(AppCore app) {
        this.app = Objects.requireNonNull(app);
        this.formatter= app.getOrException(EntityFromMapBuilder.Formatter.class);
    }
    
    @Override
    public void add(Map parent, String column, Object value) {
        
        final Map target;

        final Map found = this.getFirstMapContainingKeyWithNonMapValue(parent, column, null);

//System.out.println(column + " = " + value + " has parent container\n" + new JsonFormat(true, true, "  ").toJSONString(parent));

        Objects.requireNonNull(found, "Data container is NULL for column: "+column);

        final Object previousValue = found.get(column);

//System.out.println(dbCol + " has previous value: " + previousValue);            

        if(previousValue == null || previousValue.toString().isEmpty()) {

            target = found;

        }else {    

            final Map copy = this.copyStructure(found, true);

            final Map.Entry<Object, Collection> entry = this.getFirstCollectionContainingStructure(parent, found, null);
//System.out.println("Found container: "+collection);
//if("coursetitle".equals(dbCol)) {
//System.out.println("Column: "+dbCol+"]. Adding value no " + entry.getValue().size()+" to "+entry.getKey()+"\n"+new JsonFormat(true, true, "  ").toJSONString(entry.getValue()));                 
//}
            this.add(entry.getValue(), copy);

            target = copy;
        }

        target.put(column, value);
    }
   
    @Override
    public List buildEntities(Class entityType, Map<String, Object> data) {
        
//System.out.println("= = = = = = = BEFORE "+entityType.getName()+", from data\n: "+new JsonFormat(true, true, "  ").toJSONString(data)+"\n @"+this.getClass());
        data = this.removeEmptyStructures(data);
//System.out.println("= = = = = = =  AFTER "+entityType.getName()+", from data\n: "+new JsonFormat(true, true, "  ").toJSONString(data)+"\n @"+this.getClass());
        
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Building: {0}, from data:\n{1}", 
                    new Object[]{entityType.getName(), new JsonFormat(true, true, "  ").toJSONString(data)});
        }else{
            logger.log(Level.FINE, "Building: {0}", entityType.getName());
        }
        
        final Map<Map, Object> resultBuffer = new LinkedHashMap();

        app.getOrException(EntityFromMapBuilder.class)
                .resultBuffer(resultBuffer)
                .formatter(this.formatter)
                .source(data)
                .target(entityType)
                .build();
        
        final boolean dontUseSetAsInSetsEntitiesWithoutIdsWillAllBeEqualByDefinition = true;
        
        final List builtEntities = new ArrayList(resultBuffer.values());
       
        logger.log(Level.FINE, "Entities: {0}", builtEntities);        
        
        return builtEntities;
    }
    
    @Override    
    public Map getNested(Class entityType) {
        
        final Object entity = this.newNestedInstance(entityType, (cls) -> true);
        
        final Map map = this.get(entity, true, true);
        
        return map;
    }  
    
    @Override
    public Map get(Class entityType) {
        
        final Object entity = this.newInstance(entityType);
        
        final Map map = this.get(entity, true, true);
        
        return map;
    }
    
    @Override
    public Map get(Object entity, boolean emptyContainersAllowed, boolean nullValuesAllowed) {
        
        Map map = this.getDefault(entity);
//System.out.println("Default: "+map+" @"+this.getClass());        
        if(!emptyContainersAllowed) {
            map = this.removeEmptyStructures(map);
//System.out.println("After remove empties: "+map+" @"+this.getClass());            
        }

        if(!nullValuesAllowed) {
            final Predicate test = (e) -> e == null || e.toString().trim().isEmpty();
            map = this.removeAll(map, test, test, true);
//System.out.println("After remove nulls: "+map+" @"+this.getClass());            
        }
        
        map = this.format(entity, map);
//System.out.println("After format: "+map+" @"+this.getClass());        
        return map;
    }
    
    public Map getDefault(Class entityType) {
        final Object entity = this.newInstance(entityType);
        return this.getDefault(entity);
    }
    
    @Override
    public <T> T newInstance(Class<T> entityType) {
        return new ReflectionUtil().newInstance(entityType);
    }
    
    public Map getDefault(Object entity) {
        
        final EntityUpdater updater = app.getActivePersistenceUnitContext().getEntityUpdater(entity.getClass());
        
        final boolean exists = updater.getId(entity) != null;
        
        final MapBuilder mapBuilder = app.getOrException(MapBuilder.class)
                .source(entity)
                .sourceType(entity.getClass())
                .target(new LinkedHashMap());
        
        if(exists) {
            mapBuilder.recursionFilter(new MapBuilderDontRecurseSelectionTypes(app.getOrException(SelectionContext.class)));
        }
        
        final Map entityStructure = mapBuilder.build();
        
        return entityStructure;
    }
    
    @Override
    public <T> T newNestedInstance(Class<T> type, Predicate<Class> test) {
        return (T)this.getEntity(null, type, test, new HashSet());
    }
    
    public <T> T getEntity(Class parentType, Class<T> type, Predicate<Class> test, Set<Class> treated) {
        
        treated.add(type);
        
        final T instance = this.newInstance(type);
//System.out.println("= = = = = = = " + type.getSimpleName() + " = " + instance);                
        final Method [] methods = type.getMethods();
        
        final Predicate<Method> isGetter = new MethodIsGetter();
        final Predicate<Method> isSetter = new MethodIsSetter();
        final ReflectionUtil reflection = new ReflectionUtil();
        
        for(Method getterCandidate : methods) {
            
            if(isGetter.test(getterCandidate)) {
                
                final Class returnType = getterCandidate.getReturnType();
                
                final boolean collectionReturnType = Collection.class.isAssignableFrom(returnType);
                
                final Class targetType;
                if(!collectionReturnType) {
                    targetType = returnType;
                }else{
                    targetType = (Class)reflection.getGenericReturnTypeArguments(getterCandidate)[0];
                }
                
                if(!test.test(targetType)) {
                    continue;
                }
                
                if(treated.contains(targetType)) {
                    continue;
                }
                
                if(targetType.getAnnotation(Entity.class) == null) {
                    continue;
                }
                
//System.out.println("- - - - - - - Getter: "+getterName+", return type: "+targetType.getSimpleName()+", is generic: "+collectionReturnType);                    
                
                for(Method setterCandidate : methods) {

                    if(isSetter.and(new MethodHasParameterType(targetType, collectionReturnType)).test(setterCandidate)) {

                        final Object entity = getEntity(type, targetType, test, treated);

                        final Object argument;
                        if(!collectionReturnType) {
                            argument = entity;
                        }else{
                            final Collection collection = (Collection)reflection.newInstanceForCollectionType(returnType);
                            collection.add(entity);
                            argument = collection;
                        }

                        try{
//System.out.println("  Parent: "+(parentType == null ? null : parentType.getSimpleName()));                            
//System.out.println("Invoking: "+instance.getClass().getSimpleName()+"#"+setterCandidate.getName()+", argument: "+argument);                            
                            setterCandidate.invoke(instance, argument);
                            
                        }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }

        return instance;
    }

    public Map getFirstMapContainingKeyWithNonMapValue(Map parent, String key, Map outputIfNone) {
        
        Map output = null;
        
        final Set entrySet = parent.entrySet();
        
        for(Object oval : entrySet) {
            
            final Map.Entry entry = (Map.Entry)oval;
            final Object entryKey = entry.getKey();
            final Object entryValue = entry.getValue();
            
            if(entryKey.equals(key) && !(entryValue instanceof Map)) {
                output = parent;
                break;
            }
            
            if(entryValue instanceof Map) {
                output = this.getFirstMapContainingKeyWithNonMapValue((Map)entryValue, key, null);
                if(output != null) {
                    break;
                }
            }
            
            if(entryValue instanceof Collection) {
                final Collection collection = (Collection)entry.getValue();
                final Iterator iter = collection.iterator();
                final Object first = iter.hasNext() ? iter.next() : null;
                if(first instanceof Map) {
                    output = this.getFirstMapContainingKeyWithNonMapValue((Map)first, key, null);   
                    if(output != null) {
                        break;
                    }
                }
            }
        }
        
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "For: {0} found Map structure with keys: {1}", 
                    new Object[]{key, output == null ? null : output.keySet()});
        }

        return output == null ? outputIfNone : output;
    }
    
    @Override
    public boolean add(Collection collection, Map structure) {
        if(collection.isEmpty()) {
            throw new IllegalArgumentException("Empty collections not allowed");
        }
        final boolean added;
        final Map first = (Map)collection.iterator().next();
        if(this.isStructureEqual(first, structure, true)) {
            added = collection.add(structure);
        }else{
//@todo support multiple level of recursively nested data
            final boolean ONLY_ONE_LEVEL_OF_DEPTH_SUPPORTED_FOR_NOW = true;
            final Map update = new HashMap(4, 0.75f);
            for(Object key : first.keySet()) {
                final Object value = first.get(key);
                if(value instanceof Map) {
                    final Map valueMap = (Map)value;
                    if(this.isStructureEqual(valueMap, structure, true)) {
                        update.put(key, structure);
                        break;
                    }
                }
            }
            if(update.isEmpty()) {
                throw new UnsupportedOperationException();
            }
            final Map firstCopy = this.copyStructure(first, true);
            firstCopy.putAll(update);
            return collection.add(firstCopy);
        }
        
        return added;
    }
    
    @Override
    public Map.Entry<Object, Collection> getFirstCollectionContainingStructure(
            Map parent, Map structure, Map.Entry<Object, Collection> outputIfNone) {
        Map.Entry<Object, Collection> output = null;
        final Set entrySet = parent.entrySet();
        for(Object _e : entrySet) {
            final Map.Entry entry = (Map.Entry)_e;
            final Object key = entry.getKey();
            final Object value = entry.getValue();
            if(value instanceof Map) {
                final Map valueMap = (Map)value;
                final Map.Entry<Object, Collection> found = this.getFirstCollectionContainingStructure(valueMap, structure, null);
                if(found != null) {
                    output = found;
                    break;
                }
            }else if(value instanceof Collection) {
//System.out.println("Collection: "+value+". @"+this.getClass());                
                final Collection collection = (Collection)value;
                final Iterator iter = collection.iterator();
                final Object first = iter.hasNext() ? iter.next() : null;
                if(first instanceof Map) {
                    final boolean structureEquals = this.isStructureEqual(structure, (Map)first, true);
//System.out.println("Structure equals: "+structureEquals+"\nStructure: "+structure+"\n   Found: "+first);                    
                    if(structureEquals) {
                        output = new HashMap.SimpleImmutableEntry<>(key, collection);
                        break;
                    }else{
                        final Map.Entry<Object, Collection>  found = this.getFirstCollectionContainingStructure((Map)first, structure, null);
                        if(found != null) {
                            output = found;
                            break;
                        }else{
                            final Map foundMap = this.getFirstMapContainingStructure((Map)first, structure, null);
                            if(foundMap != null) {
                                output = new HashMap.SimpleImmutableEntry<>(key, collection);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        return output == null ? outputIfNone : output;
    }
    
    @Override
    public Map getFirstMapContainingStructure(
            Map parent, Map structure, Map outputIfNone) {
        Map output = null;
        final Set entrySet = parent.entrySet();
        for(Object _e : entrySet) {
            final Map.Entry entry = (Map.Entry)_e;
            final Object value = entry.getValue();
            if(value instanceof Map) {
                final Map valueMap = (Map)value;
                final boolean structureEquals = this.isStructureEqual(valueMap, structure, true);
                if(structureEquals) {
                    output = parent;
                    break;
                }else{
                    final Map foundMap = this.getFirstMapContainingStructure(valueMap, structure, null);
                    if(foundMap != null) {
                        output = foundMap;
                        break;
                    }else{
                        final Map.Entry<Object, Collection> foundEntry = this.getFirstCollectionContainingStructure(valueMap, structure, null);
                        if(foundEntry != null) {
                            output = valueMap;
                        }
                    }
                }
            }else if(value instanceof Collection) {
//System.out.println("Collection: "+value+". @"+this.getClass());                
                final Collection collection = (Collection)value;
                final Iterator iter = collection.iterator();
                final Object first = iter.hasNext() ? iter.next() : null;
                if(first instanceof Map) {
                    
                    final Map firstMap = (Map)first;
                    
                    final Map found = this.getFirstMapContainingStructure(firstMap, structure, null);
                    
                    if(found != null) {
                        output = found;
                        break;
                    }else{
                        final Map.Entry<Object, Collection> foundEntry = this.getFirstCollectionContainingStructure(firstMap, structure, null);
                        if(foundEntry != null) {
                            output = firstMap;
                            break;
                        }
                    }
                }
            }
        }
        
        return output == null ? outputIfNone : output;
    }

    @Override
    public boolean isStructureEqual(Map m0, Map m1, boolean recurse) {
        boolean output = true;
        if(m0.size() != m1.size()) {
            output = false;
        }else if(!recurse) {
            output = m0.keySet().containsAll(m1.keySet()) && m1.keySet().containsAll(m0.keySet());
        }else{
            for(Object key : m0.keySet()) {
                if(!m1.containsKey(key)) {
                    output = false;
                    break;
                }
                final Object val0 = m0.get(key);
                final Object val1 = m1.get(key);
                if(val0 instanceof Map) {
                    if(val1 instanceof Map) {
                        final boolean structureEqual = this.isStructureEqual((Map)val0, (Map)val1, recurse);
                        if(!structureEqual) {
                            output = false;
                            break;
                        }
                    }else{
                        output = false;
                        break;
                    }
                }else{
                    if(val1 instanceof Map) {
                        output = false;
                        break;
                    }
                }
            }
        }
        return output;
    }
    
    @Override
    public Map copyStructure(Map input, boolean recurse) {
        
        final Map output = new LinkedHashMap();
        
        final ReflectionUtil reflection  = new ReflectionUtil();
        
        for(Object key : input.keySet()) {
            
            final Object val = input.get(key);
            
            final Object update;
            
            if(recurse) {
                if(val instanceof Map) {
                    update = this.copyStructure((Map)val, recurse);
                }else if(val instanceof Collection) {
                    final Collection collection = (Collection)val;
                    if(collection.isEmpty()) {
                        update = reflection.newInstanceForCollectionType(collection.getClass());
                    }else{
                        final Iterator iter = collection.iterator();
                        final Object first = iter.hasNext() ? iter.next() : null;
                        if(first instanceof Map) {
                            update = this.copyStructure((Map)first, recurse);
                        }else{
                            update = reflection.newInstanceForCollectionType(collection.getClass());
                        }
                    }
                }else{
                    update = null;
                }
            }else{
                update = null;
            }
            
            output.put(key, update);
        }
        
        if(input.size() != output.size()) {
            throw new AssertionError("Size mis-match");
        }
//System.out.println("oooooooooooo  Input: "+input+"\noooooooooooo Output: "+output);        
        return output;
    }
    
    public Map format(Object entity, Map data) {
        return this.sort(data);
    }
    
    public Map sort(Map data) {
        return this.pushMapValuesDown(data);
    }
    
    public Map pushMapValuesDown(Map data) {
        
        final List keys = new ArrayList(data.keySet());
        
        final Comparator comparator = (key1, key2) -> { 
            final int n1 = data.get(key1) instanceof Map ? 1 : 0;
            final int n2 = data.get(key2) instanceof Map ? 1 : 0;
            return Integer.compare(n1, n2); 
        };
        Collections.sort(keys, comparator);
        final Map output = new LinkedHashMap();
        for(Object key : keys) {
            output.put(key, data.get(key));
        }
        return output;
    }
    
    @Override
    public Map removeAll(Map data, Predicate keyTest, Predicate valueTest, boolean recurse) {
        
        final Set entrySet = data.entrySet();
        
        final Set keysToRemove = new HashSet();
        final Map updates = new HashMap();
        
        for(Object oval : entrySet) {
            final Entry entry = ((Entry)oval);
            final Object key = entry.getKey();
            Object value = entry.getValue();
            if(keyTest.test(key)) {
                keysToRemove.add(key);
            }else
            if(valueTest.test(value)) {
                keysToRemove.add(key);
            }else if (recurse){
                if(value instanceof Map) {
                    value = this.removeAll((Map)value, keyTest, valueTest, recurse);
                }else if(value instanceof Collection) {
                    value = this.removeAll((Collection)value, keyTest, valueTest, recurse);
                }
                updates.put(key, value);
            }
        }
        
//System.out.println("Removing keys " + keysToRemove+", from data: "+data.keySet());        
        data.keySet().removeAll(keysToRemove);
        data.putAll(updates);
        
        return data;
    }
    
//    @Override
    public Collection removeAll(Collection collection, Predicate keyTest, Predicate valueTest, boolean recurse) {
        
        final List list = collection instanceof List ? (List)collection : new ArrayList(collection);
        
        final Set toRemove = new HashSet();
        
        final Map<Integer, Object> updates = new HashMap<>();

        for(int i=0; i<list.size(); i++) {
            Object e = list.get(i);
            if(valueTest.test(e)) {
                toRemove.add(e);
            }else if (recurse){
                if(e instanceof Map) {
                    e = this.removeAll((Map)e, keyTest, valueTest, recurse);
                    updates.put(i, e);
                }else if(e instanceof Collection) {
                    e = this.removeAll((Collection)e, keyTest, valueTest, recurse);
                    updates.put(i, e);
                }
            }
        }
        
        list.removeAll(toRemove);
        if(!updates.isEmpty()) {
            final Set<Map.Entry<Integer, Object>> set = updates.entrySet();
            for(Map.Entry<Integer, Object> e : set) {
                list.set(e.getKey(), e.getValue());
            }
        }
        
        return list;
    }

    @Override
    public Map removeEmptyStructures(Map data) {
        
        final Predicate<Map> hasNoValue = (map) -> {
            boolean accept;
            if(map.isEmpty()) {
                accept = false;
            }else{
                accept = true;
                final Collection values = map.values();
                for(Object e : values) {
                    if(e != null && !e.toString().isEmpty()) {
                        accept = false;
                        break;
                    }
                }
            }    
            return accept;
        };
        
        data = this.removeMapValues(data, hasNoValue);

        return data;
    }

    @Override
    public <K, V> Map<K, V> removeMapValues(Map<K, V> parent, Predicate<Map> test) {
        
        parent = new LinkedHashMap(parent);
        
        final Set<Map.Entry<K, V>> entrySet = parent.entrySet();
        final Set<K> deletes = new HashSet();
        final Map<K, V> updates = new LinkedHashMap();
        for(Map.Entry<K, V> entry : entrySet) {
            final K key = entry.getKey();
            final V val = entry.getValue();
            if(val instanceof Map) {
                final Map child = (Map)val;
                if(child.isEmpty() || test.test(child)) {
                    deletes.add(key);
                }else {
                    final Map childUpdate = this.removeMapValues(child, test);
                    if(childUpdate.isEmpty() || test.test(childUpdate)) {
                        deletes.add(key);
                    }else {
                        updates.put(key, (V)childUpdate);
                    }
                }
            }else if(val instanceof Collection) {
                final Collection childCollection = (Collection)val;
                final Collection childCollectionUpdate = this.removeMapValues(childCollection, test);
                if(childCollectionUpdate.isEmpty()) {
                    deletes.add(key);
                }else{
                    updates.put(key, (V)childCollectionUpdate);
                }
            }
        }
        if(!updates.isEmpty()) {
            parent.putAll(updates);
        }
        if(!deletes.isEmpty()) {
            parent.keySet().removeAll(deletes);
        }
        
        return test.test(parent) ? Collections.EMPTY_MAP : parent;
    }
    
    @Override
    public <E> Collection<E> removeMapValues(Collection<E> collection, Predicate<Map> test) {
        
        final List useCopy = new ArrayList(collection);
        
        final Map<Integer, Object> listUpdates = new HashMap();
        final Set listDeletes = new HashSet();
        for(int i=0; i<useCopy.size(); i++) {
            final Object oval = useCopy.get(i);
            if(oval instanceof Map) {
                final Map child = (Map)oval;
                if(child.isEmpty() || test.test(child)) {
                    listDeletes.add(oval);
                }else {
                    final Map childUpdate = this.removeMapValues(child, test);
                    if(childUpdate.isEmpty() || test.test(childUpdate)) {
                        listDeletes.add(oval);
                    }else {
                        listUpdates.put(i, childUpdate);
                    }
                }
            }else if(oval instanceof Collection) {
                final Collection childCollection = (Collection)oval;
                final Collection childCollectionUpdate = this.removeMapValues(childCollection, test);
                if(childCollectionUpdate.isEmpty()) {
                    listDeletes.add(oval);
                }else{
                    listUpdates.put(i, oval);
                }
            }
        }
        if(!listUpdates.isEmpty()) {
            final Set<Map.Entry<Integer, Object>> set = listUpdates.entrySet();
            for(Map.Entry<Integer, Object> e : set) {
                useCopy.set(e.getKey(), e.getValue());
            }
        }
        
        if(!listDeletes.isEmpty()) {
            useCopy.removeAll(listDeletes);
        }
        
        return useCopy;
    }
    
    public AppCore getApp() {
        return app;
    }
}
