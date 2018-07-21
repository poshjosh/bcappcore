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

import java.util.Collections;
import java.util.List;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 7, 2017 7:18:33 PM
 */
public interface TargetQueue<T> {
    
    TargetQueue NO_OP = new TargetQueue() {
        @Override
        public List getElements() {
            return Collections.EMPTY_LIST;
        }
        @Override
        public int getMark() {
            return this.getElementCount();
        }
        @Override
        public int mark(int n) {
            return this.getElementCount();
        }
        @Override
        public void rollbackToMarkedPosition() { }
        @Override
        public void requestStop() { }
        @Override
        public boolean isStopRequested() { return false; }
        @Override
        public boolean isPaused() { return false; }
        @Override
        public boolean pause() { return false; }
        @Override
        public boolean resume() { return false; }
        @Override
        public boolean add(Object target) { return false; }
        @Override
        public boolean contains(Object target) { return false; }
        @Override
        public int getElementCount() { return this.getElements().size(); }
    };
    
    List<T> getElements();
    
    default boolean isMarked() {
        return this.getMark() > -1;
    }
    
    default void unmark() {
        this.mark(-1);
    }
    
    default int mark() {
        return this.mark(this.getElementCount());
    }
    
    int getMark();
    
    int mark(int n);
    
    void rollbackToMarkedPosition();  
    
    void requestStop();
    
    boolean isStopRequested();
    
    boolean isPaused();
    
    boolean pause();

    boolean resume();
    
    boolean add(T target);
    
    boolean contains(T target);
    
    int getElementCount();
}
