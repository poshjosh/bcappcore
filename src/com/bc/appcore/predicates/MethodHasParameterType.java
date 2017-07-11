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

package com.bc.appcore.predicates;

import com.bc.util.ReflectionUtil;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 30, 2017 10:46:42 AM
 */
public class MethodHasParameterType implements Predicate<Method> {

    private final Class parameterType;
    
    private final boolean generic;
    
    private final ReflectionUtil reflection;
    
    public MethodHasParameterType(Class parameterType) {
        this(parameterType, false);
    }
    
    public MethodHasParameterType(Class parameterType, boolean generic) {
        this.parameterType = Objects.requireNonNull(parameterType);
        this.generic = generic;
        this.reflection = new ReflectionUtil();
    }

    @Override
    public boolean test(Method method) {
        final boolean output;
        if(method.getParameterCount() != 1) {
            output = false;
        }else{
            final Class methodParamType;
            if(generic) {
                final List<Type[]> list = this.reflection.getGenericParameterTypeArguments(method);
                if(list.isEmpty()) {
                    methodParamType = null;
                }else{
                    final Type [] arr = list.get(0);
                    if(arr == null || arr.length == 0) {
                        methodParamType = null;
                    }else{
                        methodParamType = (Class)arr[0];
                    }
                }
            }else{
                methodParamType = method.getParameterTypes()[0];
            }
            if(generic && methodParamType == null) {
                output = false;
            }else{
                output = this.parameterType.isAssignableFrom(methodParamType);
            }
        }
        return output;
    }
}

