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

package com.bc.appcore;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 10, 2017 12:23:41 PM
 */
public class ProcessLogImpl implements ProcessLog {
    
    private final Logger logger;
    
    private final Level level;
    
    public ProcessLogImpl(Class aClass, Level level) {
        this(Logger.getLogger(aClass.getName()), level);
    }
    
    public ProcessLogImpl(Logger logger, Level level) {
        this.logger = Objects.requireNonNull(logger);
        this.level = Objects.requireNonNull(level);
    }
    
    @Override
    public void destroy() {}
    
    @Override
    public void init() { }
    
    @Override
    public void log(Throwable t, String msg) {
        logger.log(Level.WARNING, msg, t);
    }
    
    @Override
    public void log(String msg) {
        logger.log(level, "{0}", msg);
    }
}
