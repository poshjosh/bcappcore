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

import com.bc.appcore.AppCore;
import com.bc.appcore.exceptions.TaskExecutionException;
import com.bc.appcore.parameter.ParameterException;
import com.bc.jpa.context.PersistenceUnitContext;
import java.util.Map;
import java.util.Set;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 7, 2017 8:22:44 PM
 */
public class SyncIfSlaveDatabaseEmpty implements Action<AppCore, Boolean> {

    @Override
    public Boolean execute(AppCore app, Map<String, Object> params) 
            throws ParameterException, TaskExecutionException {
        
        boolean sync = true;
        
        final PersistenceUnitContext puContext = app.getMasterSlavePersistenceContext().getSlave();
        
        final Set<Class> puClasses = puContext.getMetaData().getEntityClasses();
        
        for(Class entityType : puClasses) {

            final Long count = this.count(puContext, entityType);

            if(count > 0) {

                sync = false;

                break;
            }
        }
        
        final boolean output;
        
        if(sync) {
        
            output = (Boolean)app.getAction(ActionCommandsCore.SYNC_DATABASE).execute(app, params);
            
        }else{
            
            output = false;
        }
        
        return output;
    }
    
    public Long count(PersistenceUnitContext puContext, Class puClass) {
        return puContext.getDao().forSelect(Long.class)
                .from(puClass).count().getSingleResultAndClose();
    }
}
