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

package com.bc.appcore.table.model;

import com.bc.appcore.util.RelationAccess;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.persistence.Entity;

/**
 * @author Chinomso Bassey Ikwuagwu on May 20, 2017 1:16:51 PM
 */
public class EntityXYCountValues<X, Y, E> implements XYValues<X, Y, Integer> {

    private final List<E> resultList;
    
    private final RelationAccess relationAccess;
    
    private final Predicate<Class> relationAccessTestForRecursion;
    
    public EntityXYCountValues(List<E> resultList, RelationAccess relationAccess) {
        this.resultList = Objects.requireNonNull(resultList);
        this.relationAccess = Objects.requireNonNull(relationAccess);
        this.relationAccessTestForRecursion = (cls) -> cls.getAnnotation(Entity.class) != null;
    }

    @Override
    public Integer getValue(X xValue, Y yValue) {
        
        int count = 0;
        
        for(E entity : resultList) {
            
            if(this.isEqual(entity, xValue) &&
                    this.isEqual(entity, yValue)) {
                
                ++count;
            }
        }
        
        return count;
    }
    
    public boolean isEqual(E entity, Object value) {
        Objects.requireNonNull(entity);
        if(value == null) {
            return false;
        }else{
            final List children = this.relationAccess.getChildren(
                    entity, value.getClass(), relationAccessTestForRecursion, true);
            return children.contains(value);
        }
    }
}
