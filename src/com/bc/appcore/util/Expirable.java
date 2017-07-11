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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Chinomso Bassey Ikwuagwu on Apr 9, 2017 9:40:50 PM
 */
public interface Expirable<T> {
    
    public static Expirable from(long time, TimeUnit timeUnit) {
        return from(null, time, timeUnit);
    }
    
    public static <T> Expirable<T> from(T t, long time, TimeUnit timeUnit) {
        final long expiryTime = System.currentTimeMillis() + timeUnit.toMillis(time);
        final Expirable<T> e = new Expirable() {
            @Override
            public boolean isExpired() {
                return System.currentTimeMillis() > expiryTime;
            }
            @Override
            public Optional<T> get() {
                return Optional.ofNullable(t);
            }
        };
        return e;
    }
    
    boolean isExpired();
    
    Optional<T> get();
}
