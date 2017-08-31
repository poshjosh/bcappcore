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

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 26, 2017 8:27:46 PM
 */
public class ActionQueueImpl implements ActionQueue {

    private final Queue<Action> queue;

    public ActionQueueImpl() {
        this(new LinkedList());
    }

    public ActionQueueImpl(Queue<Action> queue) {
        this.queue = Objects.requireNonNull(queue);
    }
    
    @Override
    public void onStarted(Action action) {
        queue.add(action);
    }
    
    @Override
    public void onCompleted(Action action) {
        queue.remove(action);
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
}
