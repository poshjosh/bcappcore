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

package com.bc.appcore.jpa.predicates;

import com.bc.reflection.ReflectionUtil;
import java.lang.reflect.Method;
import java.util.function.Predicate;
import javax.persistence.Entity;

/**
 * @author Chinomso Bassey Ikwuagwu on May 19, 2017 9:28:08 PM
 */
public class GenericReturnTypeArgumentIsEntityType implements Predicate<Method> {

    private final ReflectionUtil reflection;

    public GenericReturnTypeArgumentIsEntityType() {
        this.reflection = new ReflectionUtil();
    }
    
    @Override
    public boolean test(Method method) {
        try{
            final Class cls = (Class)reflection.getGenericReturnTypeArguments(method)[0];
            return cls.getAnnotation(Entity.class) != null;
        }catch(ClassCastException ignored) {
            return false;
        }
    }
}
