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
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on May 6, 2017 2:35:45 PM
 */
public class MethodHasGenericReturnType implements Predicate<Method> {

    private final Class genericReturnType;
    
    private final ReflectionUtil reflection;

    public MethodHasGenericReturnType(Class genericReturnType) {
        this.genericReturnType = genericReturnType;
        this.reflection = new ReflectionUtil();
    }

    @Override
    public boolean test(Method method) {
        final Type type = this.reflection.getGenericReturnTypeArguments(method)[0];
        if(type instanceof Class) {
            return this.genericReturnType.isAssignableFrom((Class)type);
        }else{
            return false;
        }
    }
}

