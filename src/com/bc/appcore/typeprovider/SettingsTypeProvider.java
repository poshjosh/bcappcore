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

import com.bc.appcore.util.Settings;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 8, 2017 2:46:10 PM
 */
public class SettingsTypeProvider implements MemberTypeProvider {

    private final Settings settings;

    public SettingsTypeProvider(Settings settings) {
        this.settings = settings;
    }

    @Override
    public List<Class> getTypeList(String name, Object value) {
        final Class type = this.getType(null, name, value, null);
        return type == null ? Collections.EMPTY_LIST : Arrays.asList(type);
    }
    
    @Override
    public Class getType(Class parentType, String name, Object value, Class outputIfNone) {

        name = settings.getName(name, name);

        final Class type = settings.getValueType(name, String.class);
        
        return type;
    }

    @Override
    public List<Type> getGenericTypeArguments(Class parentType, String name, Object value) {
        throw new UnsupportedOperationException("Not supported!"); 
    }
}
