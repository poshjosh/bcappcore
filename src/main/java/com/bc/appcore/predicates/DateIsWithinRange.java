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

import java.util.Date;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 10, 2017 4:37:46 PM
 */
public class DateIsWithinRange implements Predicate<Date> {
    
    private transient static final Logger logger = Logger.getLogger(DateIsWithinRange.class.getName());

    private final Date from;
    
    private final Date to;

    public DateIsWithinRange(Date from, Date to) {
        this.from = from;
        this.to = to;
        if(logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Start: {0}. End: {1}", new Object[]{from, to});
        }
    }
    
    @Override
    public boolean test(Date date) {
        boolean accept = false;
        if(from != null && to != null) {
            if(date != null && (date.after(from) || date.equals(from))  && date.before(to)) {
                accept = true;
            }
        }else if(from == null && to != null){
            if(date != null && date.before(to)) {
                accept = true;
            }
        }else if(from != null && to == null){
            if(date != null && (date.after(from) || date.equals(from))) {
                accept = true;
            }
        }else{
            accept = true;
        }
        
        if(logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Accepted: {0}, date: {1}", new Object[]{accept, date});
        }
        
        return accept;
    }
}
