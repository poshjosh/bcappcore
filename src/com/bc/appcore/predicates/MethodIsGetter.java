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
 * @author Chinomso Bassey Ikwuagwu on Apr 30, 2017 10:35:01 AM
 */
public class MethodIsGetter implements Predicate<Method> {

    public MethodIsGetter() { }

    @Override
    public boolean test(Method method) {
        return method.getName().startsWith("get") && 
                method.getParameterCount() == 0 && 
                method.getReturnType() != null && method.getReturnType() != java.lang.Void.TYPE;
    }
}
