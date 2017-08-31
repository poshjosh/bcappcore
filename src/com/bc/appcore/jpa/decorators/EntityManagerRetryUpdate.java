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

package com.bc.appcore.jpa.decorators;

import com.bc.jpa.sync.PendingUpdatesManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 12, 2017 6:05:56 AM
 */
public class EntityManagerRetryUpdate extends EntityManagerUpdateListenerImpl {

    private static final Logger logger = Logger.getLogger(EntityManagerRetryUpdate.class.getName());
    
    private final List remove = new ArrayList();
    private final List merge = new ArrayList();
    private final List persist = new ArrayList();
    
    private final PendingUpdatesManager pendingUpdatesManager;
    
    private final Predicate<Exception> queueUpdateForRetryTest;

    public EntityManagerRetryUpdate(PendingUpdatesManager pum, Predicate<Exception> queueUpdateForRetryTest) {
        this.pendingUpdatesManager = Objects.requireNonNull(pum);
        this.queueUpdateForRetryTest = Objects.requireNonNull(queueUpdateForRetryTest);
    }

    @Override
    public void postPersist(Object entity) {
        persist.add(entity);
    }

    @Override
    public void postRemove(Object entity) {
        remove.add(entity);
    }

    @Override
    public void postMerge(Object entity) {
        merge.add(entity);
    }

    @Override
    public void postCommit() {
        this.clearCaches();
    }

    @Override
    public void onCommitException(Exception e) {
        
        try{
            
            if(this.queueUpdateForRetryTest.test(e)) {

                logger.log(Level.WARNING, "Exception while commiting updates", e);
                
                this.addFailedUpdatesToPendingUpdateQueue();

            }else{

                if(e instanceof RuntimeException) {
                    throw (RuntimeException)e;
                }else{
                    throw new RuntimeException(e);
                }
            }
        }finally{
            this.clearCaches();
        }
    }
    
    public void addFailedUpdatesToPendingUpdateQueue() {
        logger.fine(() -> "Adding failed updates to pending update queue");
        remove.stream().forEach((entity) -> pendingUpdatesManager.addRemove(entity));
        merge.stream().forEach((entity) -> pendingUpdatesManager.addMerge(entity));
        persist.stream().forEach((entity) -> pendingUpdatesManager.addPersist(entity));
    }
    
    public void clearCaches() {
        logger.finer(() -> "Clearing caches");
        remove.clear();
        merge.clear();
        persist.clear();
    }
}
