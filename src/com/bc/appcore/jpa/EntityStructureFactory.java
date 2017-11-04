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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 29, 2017 9:50:12 AM
 */
public interface EntityStructureFactory {
    
    void add(Map parent, String column, Object value);
    
    boolean add(Collection collection, Map structure);
    
    Map copyStructure(Map input, boolean recurse);
    
    List buildEntities(Class entityType, Map<String, Object> data);
    
    boolean isStructureEqual(Map m0, Map m1, boolean recurse);
    
    Map getNested(Class entityType);
    
    Map get(Class entityType);
    
    Map get(Object entity, boolean emptyContainersAllowed, boolean nullValuesAllowed);
    
    <T> T newNestedInstance(Class<T> entityType, Predicate<Class> test);
    
    <T> T newInstance(Class<T> entityType);
    
    Map.Entry<Object, Collection> getFirstCollectionContainingStructure(
            Map parent, Map structure, Map.Entry<Object, Collection> outputIfNone);
    
    Map getFirstMapContainingStructure(Map parent, Map structure, Map outputIfNone);
    
    Map removeAll(Map data, Predicate keyTest, Predicate valueTest, boolean recurse);
    
    <K, V> Map<K, V> removeEmptyStructures(Map<K, V> data);
    
    <K, V> Map<K, V> removeMapValues(Map<K, V> parent, Predicate<Map> test);
    
    <E> Collection<E> removeMapValues(Collection<E> collection, Predicate<Map> test);
}
