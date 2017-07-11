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

package com.bc.appcore.predicates;

import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on May 9, 2017 6:06:05 PM
 */
public class IntegerIsWithinRange implements Predicate<Integer> {
    
    private transient static final Logger logger = Logger.getLogger(IntegerIsWithinRange.class.getName());

    private final Integer from;
    
    private final Integer to;

    public IntegerIsWithinRange(Integer from, Integer to) {
        this.from = from;
        this.to = to;
        if(logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Start: {0}. End: {1}", new Object[]{from, to});
        }
    }
    
    @Override
    public boolean test(Integer number) {
        
        boolean accept = false;
        
        if(from != null && to != null) {
            if(number != null && number >= from  && number < to) {
                accept = true;
            }
        }else if(from == null && to != null){
            if(number != null && number < to) {
                accept = true;
            }
        }else if(from != null && to == null){
            if(number != null && number >= from) {
                accept = true;
            }
        }else{
            accept = true;
        }
        
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Accepted: {0}, number: {1}", new Object[]{accept, number});
        }
        
        return accept;
    }
}

