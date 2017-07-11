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

import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 30, 2017 10:44:49 AM
 */
public class MethodIsSetter implements Predicate<Method> {

    public MethodIsSetter() { }

    @Override
    public boolean test(Method method) {
        return method.getName().startsWith("set") 
                && method.getParameterCount() == 1
                && method.getReturnType() == java.lang.Void.TYPE;
    }
}
