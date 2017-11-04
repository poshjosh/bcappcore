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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on May 24, 2017 12:06:00 PM
 */
public class TypeProviderImpl implements TypeProvider {

    private static final Logger logger = Logger.getLogger(TypeProviderImpl.class.getName());
    
    private final Set<Class> parentTypes;

    private final MemberTypeProvider memberTypeProvider;

    public TypeProviderImpl(Set<Class> parentTypes, MemberTypeProvider memberTypeProvider) {
        this.parentTypes = Objects.requireNonNull(parentTypes);
        this.memberTypeProvider = Objects.requireNonNull(memberTypeProvider);
    }
    
    @Override
    public List<Class> getParentTypeList(Class valueType, String name, Object value) {
        
        Objects.requireNonNull(valueType);
        Objects.requireNonNull(name);
        
        final Predicate<Class> valueTypeTest = (cls) -> valueType.equals(cls);
        
        return this.getTypeList(name, value, (cls) -> true, valueTypeTest, true);
    }
    
    @Override
    public List<Class> getParentTypeList(String name, Object value) {
        
        final Predicate<Class> acceptAll = (cls) -> true;
        
        return this.getTypeList(name, value, acceptAll, acceptAll, true);
    }
    
    @Override
    public List<Class> getTypeList(String name, Object value) {
        
        final Predicate<Class> acceptAll = (cls) -> true;
        
        return this.getTypeList(name, value, acceptAll, acceptAll, false);
    }
    
    public List<Class> getTypeList(String name, Object value, 
            Predicate<Class> parentTest, Predicate<Class> childTest, boolean parentNotChild) {
        
        final Set<Class> output = new LinkedHashSet();
        
        for(Class parentType : this.parentTypes) {
            
            if(!parentTest.test(parentType)) {
                continue;
            }
            
            final Class childType = this.getType(parentType, name, value, null);
            
            if(childType == null || !childTest.test(childType)) {
                continue;
            }
            
            output.add(parentNotChild ? parentType : childType);
        }
        
        return new ArrayList(output);
    }

    @Override
    public Class getType(Class parentType, String name, Object value, Class outputIfNone) {
        return this.memberTypeProvider.getType(parentType, name, value, outputIfNone);
    }

    @Override
    public List<Type> getGenericTypeArguments(Class parentType, String name, Object value) {
        return this.memberTypeProvider.getGenericTypeArguments(parentType, name, value);
    }
}
