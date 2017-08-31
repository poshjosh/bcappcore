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

package com.bc.appcore.actions;

import com.bc.appcore.exceptions.TaskExecutionException;
import java.util.Map;
import com.bc.appcore.parameter.ParameterException;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.appcore.AppCore;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 8, 2017 10:54:48 PM
 * @param <O> The type returned by the execute methods
 */
public interface Action<A extends AppCore, O> {
    
    default O executeSilently(A app, O outputIfNone) {
        return (O)this.executeSilently(app, Collections.EMPTY_MAP, outputIfNone);
    }
    
    default O executeSilently(A app, Map<String, Object> params, O outputIfNone) {
        try{
            return this.execute(app, params);
        }catch(ParameterException | TaskExecutionException e) {
            Logger.getLogger(Action.class.getName()).log(Level.WARNING, 
                    "Exception executing instance of " + Action.class.getName(), e);
            return outputIfNone;
        }
    }
    
    default O execute(A app) throws ParameterException, TaskExecutionException {
        return (O)this.execute(app, Collections.EMPTY_MAP);
    }
    
    O execute(A app, Map<String, Object> params) throws ParameterException, TaskExecutionException;
}
