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

package com.bc.appcore;

import java.util.function.BiFunction;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 7, 2017 9:12:04 PM
 */
public class TestSubClass implements BiFunction<Class, Class, Boolean> {

    @Override
    public Boolean apply(Class t, Class u) {
        try{
            t.asSubclass(u);
            return Boolean.TRUE;
        }catch(ClassCastException ignored) {
            return Boolean.FALSE;
        }
    }
}
