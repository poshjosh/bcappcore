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

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 7, 2017 9:20:23 PM
 */
public class ListedOrder implements Comparator {

    private final Class [] order;

    public ListedOrder(List<Class> orderList) {
        this(orderList.toArray(new Class[0]));
    }

    public ListedOrder(Class... order) {
        this.order = Objects.requireNonNull(order);
    }
    
    @Override
    public int compare(Object o1, Object o2) {
        
        return Integer.compare(this.indexOf(o1), this.indexOf(o2));
    }
    
    public int indexOf(Object o) {
        
        int index = -1;
        
        if(o != null) {
            
            for(Class type : order) {
                
                ++index;
                
                final Class cls = o instanceof Class ? (Class)o : o.getClass();
                
                if(type.isAssignableFrom(cls)) {
                    break;
                }
            }
        }
        
        return index;
    }
}
