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

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on May 6, 2017 6:42:19 PM
 */
public class IsSubClass implements Predicate<Class> {
    
    private final Class test;

    public IsSubClass(Class test) {
        this.test = Objects.requireNonNull(test);
    }

    @Override
    public boolean test(Class candidate) {
        try{
            candidate.asSubclass(test);
            return true;
        }catch(ClassCastException ignored) {
            return false;
        }
    }
}
