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

import java.util.AbstractList;

/**
 * @author Chinomso Bassey Ikwuagwu on May 17, 2017 9:17:07 AM
 */
public class SingleElementFixedSizeList<E> extends AbstractList<E> {

    private final E onlyElement;
    
    private final int fixedSize;
    
    public SingleElementFixedSizeList(E onlyElement, int fixedSize) {
        this.onlyElement = onlyElement;
        if(fixedSize < 0) {
            throw new IllegalArgumentException("Size: "+fixedSize);
        }
        this.fixedSize = fixedSize;
    }

    @Override
    public E get(int index) {
        if(index > -1 && index < this.fixedSize) {
            return this.onlyElement;
        }else{
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
    }

    @Override
    public int size() {
        return this.fixedSize;
    }
    
    @Override
    public String toString() {
        return "[" + onlyElement + "]";
    }
}
