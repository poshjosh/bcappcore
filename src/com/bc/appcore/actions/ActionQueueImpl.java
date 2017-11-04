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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 26, 2017 8:27:46 PM
 */
public class ActionQueueImpl implements ActionQueue {

    private final Queue<Action> queue;
    
    private final Map<String, long[]> averageTimes;
    
    private final Map<Action, Long> startTimes;

    public ActionQueueImpl() {
        this(new LinkedList());
    }

    public ActionQueueImpl(Queue<Action> queue) {
        this.queue = Objects.requireNonNull(queue);
        this.averageTimes = new HashMap<>();
        this.startTimes = new HashMap<>();
    }
    
    @Override
    public <T extends Action> T getAction(Class<T> type, T outputIfNone) {
        for(Action a : queue) {
            if(type.isAssignableFrom(a.getClass())) {
                return (T)a;
            }
        }
        return outputIfNone;
    }
    
    @Override
    public void onStarted(Action action) {
        startTimes.put(action, System.currentTimeMillis());
        queue.add(action);
    }
    
    @Override
    public void onCompleted(Action action) {
        final Long startTime = startTimes.remove(action);
        if(startTime != null) {
            updateAverageTime(getKey(action), startTime);
        }
        queue.remove(action);
    }

    @Override
    public long getStartTimeMillis(Action action) {
        final Long startTime = startTimes.get(action);
        return startTime == null ? -1 : startTime;
    }
    
    @Override
    public boolean isRunning(Action action) {
        return getStartTimeMillis(action) != -1;
    }

    @Override
    public long getAverageTimeSpentMillis(Class type, long outputIfNone) {
        synchronized(averageTimes) {
            final long [] arr = averageTimes.get(getKey(type));
            return arr == null || arr[0] == 0 || arr[1] == 0 ? outputIfNone : arr[0] / arr[1];
        }
    }
    
    public void updateAverageTime(String className, Long startTime) {
        synchronized(averageTimes) {
            long [] arr = averageTimes.get(className);
            if(arr == null) {
                arr = new long[]{0, 0};
                averageTimes.put(className, arr);
            }
            final long timeTaken = System.currentTimeMillis() - startTime;
            arr[0] += timeTaken;
            arr[1] += 1;
        }
    }
    
    public String getKey(Action action) {
        return getKey(action.getClass());
    }
    
    public String getKey(Class type) {
        return type.getName();
    }
}
