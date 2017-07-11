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

import com.bc.util.ReflectionUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Chinomso Bassey Ikwuagwu on May 24, 2017 1:28:47 PM
 */
public class MemberValueTypeProvider implements MemberTypeProvider {

    @Override
    public List<Class> getTypeList(String name, Object value) {
        final Class type = this.getType(null, name, value, null);
        return type == null ? Collections.EMPTY_LIST : Collections.singletonList(type);
    }

    @Override
    public Class getType(Class parentType, String name, Object value, Class outputIfNone) {
        return value == null ? outputIfNone : value.getClass();
    }

    @Override
    public List<Type> getGenericTypeArguments(Class parentType, String name, Object value) {
        return value == null ? Collections.EMPTY_LIST : 
                Arrays.asList(new ReflectionUtil().getGenericTypeArguments(this.getField(parentType, name)));
    }
    
    public Field getField(Class parentType, String name) {
        try{
            return parentType.getField(name);
        }catch(NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
