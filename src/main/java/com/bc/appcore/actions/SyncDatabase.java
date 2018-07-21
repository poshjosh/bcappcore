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
import com.bc.jpa.sync.JpaSync;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.appcore.AppCore;
import com.bc.appcore.Names;
import com.bc.appcore.ResultHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 8, 2017 10:29:57 PM
 */
public class SyncDatabase implements Action<AppCore, Boolean> {

    @Override
    public Boolean execute(AppCore app, Map<String, Object> params) throws TaskExecutionException {
        
        if(!app.getPersistenceContextSwitch().isMasterActive()) {
            throw new TaskExecutionException("Sync not allowed");
        }
        
        final JpaSync jpaSync = app.getJpaSync();
        
        final ResultHandler resultHandler = (ResultHandler)
                params.getOrDefault(Names.RESULT_HANDLER, app.getResultHandler("Sync"));
                
        new Thread(this.getClass().getName()+"_Thread") {
            @Override
            public void run() {

                try{

                    final List<Class> masterTypes = new ArrayList(
                            app.getMasterSlavePersistenceContext()
                                    .getMaster().getMetaData().getEntityClasses()
                    );
                    
                    Collections.sort(masterTypes, app.getEntityOrderComparator());
                    
                    app.getPendingSlaveUpdateQueue().pause();

                    jpaSync.sync(new LinkedHashSet(masterTypes));

                    resultHandler.handleSuccess("Sync successful");

                }catch(RuntimeException e) {

                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Unexpected exception syncing", e);

                    resultHandler.handleException(e);

                }finally{
                    app.getPendingSlaveUpdateQueue().resume();
                }
            }
        }.start();
        
        return Boolean.TRUE;
    }
}
