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
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 26, 2017 7:57:34 PM
 */
public abstract class AbstractMeteredAction<A extends AppCore, O> implements MeteredAction<A, O> {
    
    private static final AtomicLong averageTimeSpentMillis = new AtomicLong(0);
    
    private boolean started;
    
    private boolean completed;
    
    private long startTimeMillis;
    
    private long timeSpentMillis;

    public AbstractMeteredAction() { }

    @Override
    public O execute(A app, Map<String, Object> params) 
            throws ParameterException, TaskExecutionException {
        
        try{
            
            this.started = true;

            app.getActionQueue().onStarted(this);
            
            this.startTimeMillis = System.currentTimeMillis();

            final O result = this.doExecute(app, params);

            this.timeSpentMillis = System.currentTimeMillis() - startTimeMillis;

            final long averageTime = averageTimeSpentMillis.get() < 1 ? this.timeSpentMillis : averageTimeSpentMillis.get();

            averageTimeSpentMillis.set((averageTime + timeSpentMillis) / 2);
            
            return result;
            
        }finally{
            
            this.completed = true;
            
            app.getActionQueue().onCompleted(this);
        }
    }

    protected abstract O doExecute(A app, Map<String, Object> params) 
            throws ParameterException, TaskExecutionException;

    @Override
    public long getAverageTimeSpentMillis(long outputIfNone) {
        return averageTimeSpentMillis.get() < 1 ? outputIfNone : averageTimeSpentMillis.get();
    }
    
    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    @Override
    public long getTimeSpentMillis() {
        if(!this.isCompleted()) {
            throw new IllegalStateException(Action.class.getSimpleName()+" must have completed running before calling getTimeSpentMillis()");
        }
        return timeSpentMillis;
    }
}
