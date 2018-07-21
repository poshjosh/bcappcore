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

import com.bc.appcore.jpa.predicates.GenericReturnTypeArgumentIsEntityType;
import com.bc.appcore.predicates.IsSubClass;
import com.bc.appcore.predicates.MethodHasGenericReturnType;
import com.bc.appcore.predicates.MethodHasParameterType;
import com.bc.appcore.predicates.MethodHasReturnType;
import com.bc.reflection.predicates.MethodIsGetter;
import com.bc.reflection.predicates.MethodIsSetter;
import com.bc.reflection.ReflectionUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 29, 2017 3:38:21 PM
 */
public class RelationAccessImpl implements RelationAccess {
    
    private static final int UPDATE = 1;
    private static final int REMOVE = 2;

    private static final Logger logger = Logger.getLogger(RelationAccessImpl.class.getName());
    
    public RelationAccessImpl( ) { }
    
    @Override
    public <T> T getOrException(Object target, Class<T> relatedType) throws IllegalArgumentException {
        final T relatedEntity = this.getFirst(target, relatedType, null);
        if(relatedEntity == null) {
            throw new IllegalArgumentException("Unexpected entity: "+target);
        }
        return relatedEntity;
    }
    
    @Override
    public <T> void setOrException(Object target, Class<T> relatedType, T related) throws IllegalArgumentException{
        if(!this.set(target, relatedType, related)) {
            throw new IllegalArgumentException("Unexpected entity: "+target);
        }
    }
    
    @Override
    public <T> T getFirst(Object target, Class<T> relatedType, T outputIfNone) {
        
        final Predicate<Method> test = new MethodIsGetter().and(new MethodHasReturnType(relatedType));
        
        return getFirst(target, relatedType, outputIfNone, test);
    }
        
    public <T> T getFirst(Object target, Class<T> relatedType, T outputIfNone, Predicate<Method> test) {         
        final T relatedEntity;
        final Class targetClass = target.getClass();
        if(targetClass.equals(relatedType)) {
            relatedEntity = (T)target;
        }else{
            final Method method = this.getFirstMethod(targetClass, test);
            if(method != null) {
                Object returnVal;
                try{
                    
                    returnVal = method.invoke(target);
                    
                    if(logger.isLoggable(Level.FINER)) {
                        logger.log(Level.FINER, "Returned: {0}, from {1}#{2}", 
                                new Object[]{returnVal, targetClass.getName(), method.getName()});
                    }
                    
                }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
                    returnVal = null;
                }
                relatedEntity = returnVal == null ? outputIfNone : (T)returnVal;
            }else{
                relatedEntity = outputIfNone;
            }
        }
        return relatedEntity == null ? outputIfNone : relatedEntity;
    }

    @Override
    public <T> Set<T> getDistinctChildren(Object target, Class<T> relatedType, Predicate<Class> testForRecursion, boolean recurse) { 
        return this.collectChildren(target, relatedType, testForRecursion, recurse, new HashSet(), new HashSet());
    }
    
    @Override
    public <T> List<T> getChildren(Object target, Class<T> relatedType, Predicate<Class> testForRecursion, boolean recurse) { 
        return this.collectChildren(target, relatedType, testForRecursion, recurse, new ArrayList(), new HashSet());
    }
    
    public <T, C extends Collection> C collectChildren(Object target, Class<T> relatedType, 
            Predicate<Class> testForRecursion, boolean recurse, C collectInto, Set<Class> processed) { 

        final Class targetClass = target.getClass();
        
        if(processed.contains(targetClass)) {
            
        }else if(targetClass.equals(relatedType)) {
            
            processed.add(targetClass);
            
            collectInto.add((T)target);
            
        }else{
            
            processed.add(targetClass);
            
            final Predicate<Method> isGetter = new MethodIsGetter();
            final Predicate<Class> isSubClass = new IsSubClass(relatedType);
            
            final Method [] methods = target.getClass().getMethods();
            
            for(Method method : methods) {
                
                if(!isGetter.test(method)) {
                    continue;
                }
                
                final Class returnType = method.getReturnType();
//System.out.println("Return type: "+returnType);                
//                if(returnType.isPrimitive() || returnType.isEnum()) {
//                    continue;
//                }
                
                if(processed.contains(returnType)) {
                    continue;
                }

                final Object child;
                try{
                    child = method.invoke(target);
                }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                
                if(isSubClass.test(returnType)) {
                    
                    collectInto.add((T)child);
                    
                    break;
                }
                
                if(child == null) {
                    continue;
                }
                
                if(recurse) {
//System.out.println("Processing recurse on: "+child);                
                    final Collection collection;
                    if(child instanceof Object[]) {
                        final Object [] array = (Object[])child;
                        collection = Arrays.asList(array);
                    }else if(child instanceof Collection) {
                        collection = (Collection)child;
                    }else{
                        collection = null;
                    }
                    
                    if(collection == null) {
                        
                        if(!testForRecursion.test(returnType)) {
                            continue;
                        }

                        final int sizeBefore = collectInto.size();
                        this.collectChildren(child, relatedType, testForRecursion, recurse, collectInto, processed);
                        if(collectInto.size() > sizeBefore) {
                            break;
                        }
                    }else{
//System.out.println("Processing Collection/Array type");                        
                        boolean added = false;
                        for(Object element : collection) {
                            
                            if(element == null || !testForRecursion.test(element.getClass())) {
                                continue;
                            }
                            
                            final int sizeBefore = collectInto.size();
                            this.collectChildren(element, relatedType, testForRecursion, recurse, collectInto, processed);
                            if(collectInto.size() > sizeBefore) {
                                added = true;
                            }
                        }
                        if(added) {
                            break;
                        }
                    }
                }
            }
        }    
        
//System.out.println("From "+target+" returned "+collectInto);

        return collectInto;
    }

    @Override
    public <T> boolean set(Object target, Class<T> relatedType, T related) {
        boolean set;
        final Predicate<Method> test = new MethodIsSetter().and(new MethodHasParameterType(relatedType));
        final Method setter = this.getFirstMethod(target.getClass(), test);
        
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Entity: {0}, parameter type: {1}, method: {2}", 
                    new Object[]{target, relatedType, setter});
        }
        
        if(setter != null) {
            try{
                
                setter.invoke(target, related);
                
                set = true;
                
                if(logger.isLoggable(Level.FINER)) {
                    logger.log(Level.FINER, "{0}#{1} invoked with argument {2}", 
                            new Object[]{target.getClass().getName(), setter.getName(), related});
                }
                
            }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
                set = false;
            }
        }else{
            set = false;
        }
        return set;
    }

    @Override
    public List update(Collection entities, boolean collections) {
        final List entityList = new ArrayList(entities);
        final List output = new ArrayList();
        for(int i=0; i<entityList.size(); i++) {
            if(i == 0) {
                continue;
            }
            final Object entity = entityList.get(i);
            final List updateList = entityList.subList(0, i);
            output.addAll(this.updateWithAll(entity, updateList, collections));
        }
        return output;
    }
    
    @Override
    public List updateWithAll(Object entity, Collection updates, boolean collections) {    
        return this.updateWithAll(entity, updates, UPDATE, collections);
    }
    
    @Override
    public List updateAllWith(Collection entities, Class updateType, Object update, boolean collections) {
        return this.updateAllWith(entities, updateType, update, UPDATE, collections);
    }

    @Override
    public boolean updateWith(Object entity, Object update, boolean collections) {
        return this.updateWith(entity, update.getClass(), update, UPDATE, collections);
    }
    
    @Override
    public List remove(Collection entities, boolean collections) {
        final List entityList = new ArrayList(entities);
        final List output = new ArrayList();
        for(int i=0; i<entityList.size(); i++) {
            if(i == 0) {
                continue;
            }
            final Object entity = entityList.get(i);
            final List updateList = entityList.subList(0, i);
            output.addAll(this.removeAllFrom(entity, updateList, collections));
        }
        return output;
    }

    @Override
    public List removeFromAll(Collection entities, Class updateType, Object update, boolean collections) {
        return this.updateAllWith(entities, updateType, update, REMOVE, collections);
    }
    
    @Override
    public List removeAllFrom(Object entity, Collection updates, boolean collections) {    
        return this.updateWithAll(entity, updates, REMOVE, collections);
    }
    
    @Override
    public boolean removeFrom(Object entity, Object toRemove, boolean collections) {
        return this.updateWith(entity, toRemove.getClass(), toRemove, REMOVE, collections);
    }
    
    public List updateWithAll(Object entity, Collection updates, int actionType, boolean collections) {    
        final List output = new ArrayList();
        for(Object update : updates) {
            output.addAll(this.updateAllWith(Collections.singleton(entity), update.getClass(), update, actionType, collections));
        }
        return output;
    }
    
    public List updateAllWith(Collection entities, Class updateType, Object update, int actionType, boolean collections) {    
        
        Objects.requireNonNull(entities);
        Objects.requireNonNull(updateType);
        
        final List updated = new ArrayList(entities.size());
        
        final Predicate<Method> getterMethodTest = 
                new MethodIsGetter().and(new MethodHasReturnType(updateType));

        final Predicate<Method> setterMethodTest = 
                new MethodIsSetter().and(new MethodHasParameterType(updateType));

        for(Object entity : entities) {
            
            if(this.updateWith(entity, updateType, update, actionType, getterMethodTest, setterMethodTest, collections)) {
                
                updated.add(entity);
            }
        }
        
        return updated.isEmpty() ? Collections.EMPTY_LIST : Collections.unmodifiableList(updated);
    }
    
    public boolean updateWith(Object entity, Class updateType, Object update, int actionType, boolean collections) {
        
        final Predicate<Method> getterMethodTest = 
                new MethodIsGetter().and(new MethodHasReturnType(updateType));

        final Predicate<Method> setterMethodTest = 
                new MethodIsSetter().and(new MethodHasParameterType(updateType));
        
        return this.updateWith(entity, updateType, update, actionType, getterMethodTest, setterMethodTest, collections);
    }
    
    public boolean updateWith(Object entity, Class updateType, Object update, int actionType, 
            Predicate<Method> getterMethodTest, Predicate<Method> setterMethodTest, boolean collections) {
        
        final Method [] methods = entity.getClass().getMethods();

        final Method getter = this.getFirstMethod(methods, getterMethodTest);

        if(getter == null) {
            return false;
        }

        boolean updated = false;
        try{

            final Object value = getter.invoke(entity);

            if(logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "Returned {0}, from {1}#{2}", 
                        new Object[]{value, entity.getClass().getName(), getter.getName()});
            }

            if(!Objects.equals(update, value)) {

                final Method setter = this.getFirstMethod(methods, setterMethodTest);

                setter.invoke(entity, update);

                if(logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "{0}#{1} invoked with argument: {2}", 
                            new Object[]{entity.getClass().getName(), setter.getName(), update});
                }

                updated = true;
            }
            
            if(!collections) {
                return updated;
            }
            
            final Object target = update != null ? update : this.getFirst(entity, updateType, null);

            final Map<Method, Collection> getterMethods = this.getOneToManyGetterMethods(target);

            final Predicate<Method> updateMethodTest = new MethodHasGenericReturnType(entity.getClass());
            
            final ReflectionUtil reflection = new ReflectionUtil();

            for(Method getterMethod : getterMethods.keySet()) {

                if(updateMethodTest.test(getterMethod)) {

                    Collection collection = (Collection)getterMethods.get(getterMethod);

                    if(logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "With {0} update {1}#{2}", 
                                new Object[]{entity, target, getterMethod.getName()});
                    }

                    if(REMOVE == actionType) {
                        if(collection != null && collection.remove(entity)) {
//                            updated = true;
                        }
                    }else{

                        if(collection == null) {

                            final Class returnType = getterMethod.getReturnType();
                            collection = (Collection)reflection.newInstanceForCollectionType(returnType);
                            final String setterMethodName;
                            final String getterMethodName = getterMethod.getName();
                            if(getterMethodName.startsWith("get")) {
                                setterMethodName = getterMethod.getName().replaceFirst("get", "set");
                            }else if(getterMethodName.startsWith("is")) {
                                setterMethodName = getterMethod.getName().replaceFirst("is", "set");
                            }else{
                                throw new IllegalArgumentException("Expected getter method, found: " + getterMethod);
                            }
                            try{
                                final Method setterMethod = target.getClass().getMethod(setterMethodName, returnType);
                                setterMethod.invoke(target, collection);

                                if(logger.isLoggable(Level.FINE)) {
                                    logger.log(Level.FINE, "{0}#{1} invoked with argument: {2}", 
                                            new Object[]{entity.getClass().getName(), setterMethod.getName(), target});
                                }

                            }catch(NoSuchMethodException | SecurityException | IllegalAccessException | 
                                    IllegalArgumentException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        if(!collection.contains(entity)) {
                            if(collection.add(entity)) {
//                                updated = true;
                            }
                        }
                    }
                }
            }
        }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        
        return updated;
    }

    @Override
    public Map<Method, Collection> getOneToManyGetterMethods(Object entity, Class relatedType) {
        
        final Object related = this.getFirst(entity, relatedType, null);
        
        return this.getOneToManyGetterMethods(related);
    }
    
    @Override
    public Map<Method, Collection> getOneToManyGetterMethods(Object related) {
        
        final Map<Method, Collection> selectedMethods;

        if(related == null) {
            
            selectedMethods = Collections.EMPTY_MAP;
            
        }else { 
            
            selectedMethods = new HashMap();
            
            final Method [] methods = related.getClass().getMethods();
            
            final Predicate<Method> methodTest = new MethodIsGetter()
                    .and(new MethodHasReturnType(Collection.class))
                    .and(new GenericReturnTypeArgumentIsEntityType());
            
            for(Method method : methods) {
                
                if(methodTest.test(method)) {
                    
                    try{
                        
                        final Collection entityList = (Collection)method.invoke(related);

                        selectedMethods.put(method, entityList);
                        
                    }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        
        return selectedMethods;
    }
    
    @Override
    public Set<Class> getChildTypes(Class entityType, Predicate<Class> acceptTypeTest) {
        
        return this.getChildTypes(entityType, false, acceptTypeTest, (cls) -> false, false);
    }
    
    @Override
    public Set<Class> getChildTypes(Class entityType, boolean inclusive, 
            Predicate<Class> acceptTypeTest, Predicate<Class> recurseTypeTest,
            boolean useGenericTypeOfCollections) {
        
        final Set<Class> buffer = new LinkedHashSet();
        
        this.appendChildTypes(entityType, inclusive, acceptTypeTest, recurseTypeTest, buffer, useGenericTypeOfCollections);
        
        return buffer.isEmpty() ? Collections.EMPTY_SET : buffer;
    }

    public void appendChildTypes(Class entityType, boolean inclusive, 
            Predicate<Class> acceptTypeTest, Predicate<Class> recurseTypeTest, 
            Set<Class> buffer, boolean useGenericTypeOfCollections) {
//System.out.println(entityType+". @"+this.getClass());
        if(inclusive) {
            buffer.add(entityType);
        }
        
        final Predicate<Method> isGetterMethod = new MethodIsGetter();

        final Method [] methods = entityType.getMethods();
        
        final ReflectionUtil reflection = new ReflectionUtil();
        
        for(Method method : methods) {
            
            if(isGetterMethod.test(method)) {
                
                final Class returnType;
                
                if(useGenericTypeOfCollections && Collection.class.isAssignableFrom(method.getReturnType())) {
                    
                    final Type [] genericReturnTypes = reflection.getGenericReturnTypeArguments(method);
                    
                    if(genericReturnTypes != null && genericReturnTypes.length > 0) {
                        
                        returnType = (Class)genericReturnTypes[0];
                        
                    }else{
                     
                        returnType = method.getReturnType();
                    }
                }else{
                    
                    returnType = method.getReturnType();
                }
                
                if(buffer.contains(returnType)) {
                    continue;
                }
                
                if(!acceptTypeTest.test(returnType)) {
                    continue;
                }
                
                if(recurseTypeTest.test(returnType)) {
//System.out.println("- - - Recursing: "+returnType+". @"+this.getClass());                    
                    this.appendChildTypes(returnType, true, acceptTypeTest, 
                            recurseTypeTest, buffer, useGenericTypeOfCollections);
                }else{
//System.out.println("- - -    Adding: "+returnType+". @"+this.getClass());                                        
                    buffer.add(returnType);
                }
            }
        }
    }

    public Method getFirstMethod(Class target, Predicate<Method> test) {
        final Method [] methods = target.getMethods();
        return this.getFirstMethod(methods, test);
    }
    
    public Method getFirstMethod(Method [] methods, Predicate<Method> test) {
        Method selected = null;
        for(Method m : methods) {
            if(test.test(m)) {
                selected = m;
                break;
            }
        }
        return selected;
    }
}
