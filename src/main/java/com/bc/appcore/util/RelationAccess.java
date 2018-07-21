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

package com.bc.appcore.util;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 30, 2017 11:09:17 AM
 */
public interface RelationAccess {

    <T> T getFirst(Object target, Class<T> valueType, T outputIfNone);

    <T> Set<T> getDistinctChildren(Object target, Class<T> relatedType, Predicate<Class> testForRecursion, boolean recurse);
    
    <T> List<T> getChildren(Object target, Class<T> relatedType, Predicate<Class> testForRecursion, boolean recurse);
    
    Set<Class> getChildTypes(Class entityType, Predicate<Class> acceptTypeTest);
    
    Set<Class> getChildTypes(Class entityType, boolean inclusive, 
            Predicate<Class> acceptTypeTest, Predicate<Class> recurseTypeTest,
            boolean useGenericTypeOfCollections);
    
    <T> T getOrException(Object target, Class<T> relatedType) throws IllegalArgumentException;

    Map<Method, Collection> getOneToManyGetterMethods(Object entity, Class relatedType);
    
    Map<Method, Collection> getOneToManyGetterMethods(Object related);

    <T> boolean set(Object target, Class<T> relatedType, T related);

    <T> void setOrException(Object target, Class<T> relatedType, T related) throws IllegalArgumentException;
    
    List update(Collection entities, boolean collections);
    
    List updateWithAll(Object entity, Collection updates, boolean collections);
            
    List updateAllWith(Collection entities, Class updateType, Object update, boolean collections);
    
    boolean updateWith(Object entity, Object update, boolean collections);
    
    List remove(Collection entities, boolean collections);
    
    List removeAllFrom(Object entity, Collection updates, boolean collections);
    
    List removeFromAll(Collection entities, Class updateType, Object update, boolean collections);
    
    boolean removeFrom(Object entity, Object update, boolean collections);
}
