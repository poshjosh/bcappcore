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

package com.bc.appcore.util;

/**
 * Two instances of this class are equal if and only if their values are equal.
 * @author Chinomso Bassey Ikwuagwu on Mar 28, 2017 4:46:17 PM
 * @param <T> The type of the value this selection holds
 */
public interface Selection<T> {
    
    static <X> Selection<X> from(X value) {
        return from(String.valueOf(value), value);
    }

    static <X> Selection<X> from(String displayValue, X value) {
        return new SelectionImpl(displayValue, value);
    }
    
    T getValue();
    
    String getDisplayValue();
}
