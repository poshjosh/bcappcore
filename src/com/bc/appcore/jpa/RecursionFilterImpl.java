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

package com.bc.appcore.jpa;

import com.bc.appcore.AppCore;
import com.bc.jpa.util.EntityRecursionFilter;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 15, 2017 9:19:03 PM
 */
public class RecursionFilterImpl extends EntityRecursionFilter {
    
    private final AppCore app;

    public RecursionFilterImpl(AppCore app) {
        this.app = app;
    }

    @Override
    public boolean shouldRecurse(Class valueType, Object value) {
        final SelectionContext sc = app.get(SelectionContext.class);
        final boolean output;
        if(sc.getSelectionColumn(valueType, null) != null) {
            output = false; 
        }else{
            output = super.shouldRecurse(valueType, value);
        }
        return output;
    }
}
