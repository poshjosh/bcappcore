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

package com.bc.appcore.functions;

import com.bc.appcore.actions.Action;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 29, 2017 9:46:53 AM
 */
public class CreateActionFromClassName implements Function<String, Action> {

    private static final Logger logger = Logger.getLogger(CreateActionFromClassName.class.getName());
    
    private final Level logLevel;

    public CreateActionFromClassName() {
        this(Level.FINER);
    }
    
    public CreateActionFromClassName(Level logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public Action apply(String className) {
        try{
            final Class aClass = Class.forName(className);
            final Action action = (Action)aClass.getConstructor().newInstance();
            logger.log(logLevel, "Created action: {0}", action);
            return action;
        }catch(ClassNotFoundException | NoSuchMethodException | SecurityException | 
                InstantiationException | IllegalAccessException | 
                IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Exception create instance of " + className, e);
        }
    }
}
