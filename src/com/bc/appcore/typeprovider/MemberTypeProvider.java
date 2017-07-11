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
import java.util.Collections;
import java.util.List;

/**
 * @author Chinomso Bassey Ikwuagwu on May 24, 2017 12:07:16 PM
 */
public interface MemberTypeProvider {

    class SingleMemberTypeProvider implements MemberTypeProvider {
        private final Class type;
        public SingleMemberTypeProvider(Class type) {
            this.type = type;
        }
        @Override
        public List<Class> getTypeList(String name, Object value) {
            return this.type == null ? Collections.EMPTY_LIST : Collections.singletonList(this.type);
        }
        @Override
        public Class getType(Class parentType, String name, Object value, Class outputIfNone) {
            return this.type == null ? outputIfNone : this.type;
        }
        @Override
        public List<Type> getGenericTypeArguments(Class parentType, String name, Object value) {
            return this.type == null ? Collections.EMPTY_LIST : Collections.singletonList(this.type);
        }
    }
    
    static MemberTypeProvider from(final Class type) {
        return new SingleMemberTypeProvider(type);
    }
    
    static MemberTypeProvider fromValueType() {
        return new MemberValueTypeProvider();
    }
    
    List<Class> getTypeList(String name, Object value);

    Class getType(Class parentType, String name, Object value, Class outputIfNone);

    /**
     * <p>Given the code below:</p>
     * <code><pre>
     * public class Woman {
     *      List&lt;Child&gt; getChildren();
     * }
     * </pre></code>
     * <p>
     * For <code>parentType</code> of <code><b>Woman</b></code> and <code>name</code>
     * of <code><b>children</b></code> This method returns type <code><b>Child.class</b></code>
     * </p>
     * @param parentType
     * @param name
     * @param value
     * @return 
     */
    List<Type> getGenericTypeArguments(Class parentType, String name, Object value);
}
