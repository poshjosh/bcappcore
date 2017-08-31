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

import com.bc.jpa.sync.JpaSync;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.appcore.AppCore;
import com.bc.appcore.exceptions.TaskExecutionException;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 31, 2017 10:31:53 AM
 */
public class Exit implements Action<AppCore, Boolean> {

    @Override
    public Boolean execute(AppCore app, Map<String, Object> params) throws TaskExecutionException {

        final JpaSync jpaSync = app.getJpaSync();

        if(!jpaSync.isRunning()) {

            app.shutdown();

            System.exit(0);

            return Boolean.TRUE;
            
        }else{

            this.waitForJpaSyncThenExit(app, jpaSync);

            return Boolean.FALSE;
        }
    }
    
    private void waitForJpaSyncThenExit(AppCore app, JpaSync jpaSync) {
        
        final Thread waitForJpaSyncThread = new Thread("Wait_for_JpaSync_then_exit_Thread") {
            @Override
            public synchronized void run() {

                try{

                    while(jpaSync.isRunning()) {
                        this.wait(1000);
                    }

                }catch(RuntimeException | InterruptedException e) {

                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, 
                            "Exception while waiting for JpaSync to complete", e);
                }finally{

                    this.notifyAll();

                    app.shutdown();

                    System.exit(0);
                }
            }
        };

        waitForJpaSyncThread.start();
    }
}

