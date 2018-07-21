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

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on May 27, 2017 1:13:24 PM
 */
public class AcceptEachTypeOnlyOnce<T> implements Predicate<T> {

    private final Set<T> alreadyAccepted;

    public AcceptEachTypeOnlyOnce() {
        this.alreadyAccepted = new HashSet();
    }

    @Override
    public boolean test(T t) {
        if(this.alreadyAccepted.contains(t)) {
            return false;
        }else{
            this.alreadyAccepted.add(t);
            return true;
        }
    }
}
