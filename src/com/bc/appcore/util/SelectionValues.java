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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @author Chinomso Bassey Ikwuagwu on May 2, 2017 8:55:55 PM
 */
public interface SelectionValues<K> {
    
    static <T> SelectionValues from(final Collection values) {
        return from(null, values);
    }
    
    static <T> SelectionValues from(final Selection noSelection, final Collection<T> values) {
        return (SelectionValues) (Object key) -> {
            final List<Selection> selectionValues = new ArrayList(values.size() + 1);
            if(noSelection != null) {
                selectionValues.add(noSelection);
            }
            final Function<T, Selection> mapper = (t) -> Selection.from(t);
            values.stream().map(mapper).forEach((s) -> selectionValues.add(s));
            return selectionValues;
        };
    }
    
    List<Selection> getSelectionValues(K key);
}
