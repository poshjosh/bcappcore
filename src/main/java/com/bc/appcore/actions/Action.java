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
import com.bc.appcore.AppCore;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Feb 8, 2017 10:54:48 PM
 * @param <O> The type returned by the execute methods
 */
public interface Action<A extends AppCore, O> {
    
    default Optional<O> executeSilently(A app) {
        return this.executeSilently(app, Collections.EMPTY_MAP);
    }
    
    default Optional<O> executeSilently(A app, Map<String, Object> params) {
        O output;
        try{
            output = this.execute(app, Objects.requireNonNull(params));
        }catch(ParameterException | TaskExecutionException e) {
            Logger.getLogger(Action.class.getName()).warning(() -> 
                    "Exception executing instance of " + this.getClass().getName() + 
                    ". " + e);
            output = null;
        }
        return Optional.ofNullable(output);
    }
    
    default O execute(A app) throws ParameterException, TaskExecutionException {
        return (O)this.execute(app, Collections.EMPTY_MAP);
    }
    
    O execute(A app, Map<String, Object> params) throws ParameterException, TaskExecutionException;
}
