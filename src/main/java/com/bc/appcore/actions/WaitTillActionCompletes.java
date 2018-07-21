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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 26, 2017 5:52:39 PM
 */
public class WaitTillActionCompletes implements Action<AppCore, Boolean>{

    private transient static final Logger logger = Logger.getLogger(WaitTillActionCompletes.class.getName());
    
    public WaitTillActionCompletes() { }

    @Override
    public Boolean execute(AppCore app, Map<String, Object> params) 
            throws ParameterException, TaskExecutionException {
        
        final Object firstValue = params.values().stream().findFirst()
                .orElseThrow(() -> newParameterException(null));
        
        Boolean interrupted = Boolean.FALSE;
        
        try{
            this.waitTillActionCompletes(app, firstValue, 100);
        }catch(InterruptedException e) {
            interrupted = Boolean.TRUE;
            logger.log(Level.WARNING, "", e);
        }
        
        return interrupted;
    }
    
    public synchronized void waitTillActionCompletes(
            AppCore app, Object obj, long interval) 
            throws InterruptedException, ParameterException {
        if(obj instanceof Class) {
        
            this.waitTillActionCompletes(app, (Class)obj, interval);
            
        }else if (obj instanceof Action) {
            
            this.waitTillActionCompletes(app, (Action)obj, interval);
            
        }else{
            
            throw this.newParameterException(obj);
        }
    }
    
    public synchronized void waitTillActionCompletes(
            AppCore app, Class clazz, long interval) 
            throws InterruptedException {
        
        try{
            while(app.getActionQueue().isAnyRunning(clazz)) {
                this.wait(interval);
            }
        }finally{
            this.notifyAll();
        }
    }

    public synchronized void waitTillActionCompletes(
            AppCore app, Action action, long interval) 
            throws InterruptedException {
        
        try{
            while(app.getActionQueue().isRunning(action)) {
                this.wait(interval);
            }
        }finally{
            this.notifyAll();
        }
    }
    
    public ParameterException newParameterException(Object param) {
        return new ParameterException("Expected instance of java.lang.Class or " + 
                com.bc.appcore.actions.Action.class.getName() + ", but found: " + param);
    }
}
