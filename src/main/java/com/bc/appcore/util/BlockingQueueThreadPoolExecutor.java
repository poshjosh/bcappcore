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

import com.bc.util.concurrent.NamedThreadFactory;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 3, 2017 8:39:37 AM
 */
public class BlockingQueueThreadPoolExecutor extends ThreadPoolExecutor {
    
    public BlockingQueueThreadPoolExecutor(
            String threadFactoryName, 
            int queueSize, int corePoolSize, int maxPoolSize) {
        this(threadFactoryName, queueSize, corePoolSize, maxPoolSize, 
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }
    
    public BlockingQueueThreadPoolExecutor(
            String threadFactoryName, 
            int queueSize, int corePoolSize, int maxPoolSize,
            RejectedExecutionHandler rejectedExecutionHandler) {
        super(
                corePoolSize, maxPoolSize, 0L, TimeUnit.MILLISECONDS, 
                new ArrayBlockingQueue<>(queueSize), 
                new NamedThreadFactory(threadFactoryName), 
                rejectedExecutionHandler); 
    }
}
