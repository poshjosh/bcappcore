/*
 * Copyright 2018 NUROX Ltd.
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

package com.bc.appcore.functions;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 13, 2018 9:21:00 AM
 */
public class SubListFromEnd<E> implements BiFunction<List<E>, Integer, List<E>> {

    public SubListFromEnd() { }

    @Override
    public List<E> apply(List<E> list, Integer viewSize) {
        List<E> output;
        if(list == null || list.isEmpty()) {
            output = Collections.EMPTY_LIST;
        }else{
            final int n = list.size() - viewSize;
            final int offset = n < 0 ? 0 : n;
            final List<E> view = list.subList(offset, list.size());
            output = view.isEmpty() ? Collections.EMPTY_LIST : view;
        }
        return output;
    }
}
