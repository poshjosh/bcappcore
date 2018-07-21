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

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 26, 2017 8:55:53 PM
 */
public interface ActionQueue {

    default long getEstimatedTimeLeftMillis(Action action, long outputIfNone) {
        if(!isRunning(action)) {
            throw new IllegalStateException(Action.class.getSimpleName()+" must be running before calling getEstimatedTimeLeftMillis()");
        }
        final long averageTimeForThisAction = this.getAverageTimeSpentMillis(action.getClass(), -1L);
        if(averageTimeForThisAction == -1L) {
            return outputIfNone;
        }
        return averageTimeForThisAction - (System.currentTimeMillis() - getStartTimeMillis(action));
    }
    
    long getAverageTimeSpentMillis(Class type, long outputIfNone);
    
    long getStartTimeMillis(Action action);
    
    default <T extends Action> boolean isAnyRunning(Class<T> type) {
        return this.stream().filter((action) -> type.isAssignableFrom(action.getClass()) && this.isRunning(action)).findAny().isPresent();
    }

    void onStarted(Action action);
    
    void onCompleted(Action action);
    
    boolean isRunning(Action action);

    boolean contains(Action action);

    Action element();

    void forEach(Consumer<? super Action> action);

    boolean isEmpty();

    Iterator<Action> iterator();

    Stream<Action> parallelStream();

    Action peek();

    int size();

    Spliterator<Action> spliterator();

    Stream<Action> stream();
}
