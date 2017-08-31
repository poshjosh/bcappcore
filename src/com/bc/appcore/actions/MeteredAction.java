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

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 26, 2017 7:56:45 PM
 */
public interface MeteredAction<A extends AppCore, O> extends Action<A, O> {

    default long getEstimatedTimeLeftMillis(long outputIfNone) {
        if(!isRunning()) {
            throw new IllegalStateException(Action.class.getSimpleName()+" must be running before calling getEstimatedTimeLeftMillis()");
        }
        final long averageTimeForThisAction = this.getAverageTimeSpentMillis(-1L);
        if(averageTimeForThisAction == -1L) {
            return outputIfNone;
        }
        return getAverageTimeSpentMillis(outputIfNone) - (System.currentTimeMillis() - getStartTimeMillis());
    }
    
    default boolean isRunning() {
        return this.isStarted() && !this.isCompleted();
    }

    boolean isStarted();
    
    boolean isCompleted();
    
    long getTimeSpentMillis();
    
    long getAverageTimeSpentMillis(long outputIfNone);

    long getStartTimeMillis();
}
