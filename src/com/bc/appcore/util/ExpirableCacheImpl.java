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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 29, 2017 12:46:59 PM
 */
public class ExpirableCacheImpl<K> extends HashMap<K, Expirable> implements ExpirableCache<K> {
    
    private final long timeout;
    
    private final TimeUnit timeUnit;

    private final ReadWriteLock lock;
    
    private final ScheduledExecutorService clearExpiredService;
    
    public ExpirableCacheImpl(long timeout, TimeUnit timeUnit, int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.lock = new ReentrantReadWriteLock();
        this.clearExpiredService = Executors.newSingleThreadScheduledExecutor();
        this.init();
    }

    public ExpirableCacheImpl(long timeout, TimeUnit timeUnit, int initialCapacity) {
        super(initialCapacity);
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.lock = new ReentrantReadWriteLock();
        this.clearExpiredService = Executors.newSingleThreadScheduledExecutor();
        this.init();
    }

    public ExpirableCacheImpl(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.lock = new ReentrantReadWriteLock();
        this.clearExpiredService = Executors.newSingleThreadScheduledExecutor();
        this.init();
    }

    public ExpirableCacheImpl(long timeout, TimeUnit timeUnit, Map<? extends K, ? extends Expirable> m) {
        super(m);
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.lock = new ReentrantReadWriteLock();
        this.clearExpiredService = Executors.newSingleThreadScheduledExecutor();
        this.init();
    }

    @Override
    public void close() {
        com.bc.util.Util.shutdownAndAwaitTermination(this.clearExpiredService, 1, TimeUnit.SECONDS);    
    }
    
    private void init() {
        final Runnable clearExpired = new Runnable() {
            @Override
            public void run() {
                try{
                    final Map<K, Expirable> expirables = ExpirableCacheImpl.this;
                    final Iterator<K> iter = expirables.keySet().iterator();
                    while(iter.hasNext()) {
                        final Expirable expirable;
                        try{
                            lock.readLock().lock();
                            final K key = iter.next();
                            expirable = expirables.get(key);
                        }finally{
                            lock.readLock().unlock();
                        }
                        if(expirable.isExpired()) {
                            try{
                                lock.writeLock().lock();
                                iter.remove();
                            }finally{
                                lock.writeLock().unlock();
                            }
                        }
                    }
                }catch(RuntimeException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, 
                            "Unexpected exception clearing expired "+Expirable.class.getName(), e);
                }
            }
        };
        clearExpiredService.scheduleWithFixedDelay(clearExpired, 10, 10, TimeUnit.SECONDS);
        clearExpiredService.shutdown();
    }
    
    @Override
    public long getDefaultExpirableTimeout(TimeUnit targetTimeUnit) {
        return targetTimeUnit.convert(this.timeout, this.timeUnit);
    }

    @Override
    public <V> Expirable<V> putFor(K id, V value) {
        final Expirable expirable = Expirable.from(
                value, this.getDefaultExpirableTimeout(this.timeUnit), this.timeUnit);
        return this.put(id, expirable);
    }
}
