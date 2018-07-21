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

package com.bc.appcore.properties;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 1, 2017 7:35:43 PM
 */
public class PropertyNamesValidator implements Predicate<Properties> {

    private final Class type;
    
    public PropertyNamesValidator(Class type) {
        this.type = Objects.requireNonNull(type);
        if(!type.isInterface()) {
            throw new IllegalArgumentException("Only interface supported");
        }
    }

    @Override
    public boolean test(Properties properties) {
        
        return this.isValidNames(properties);
    }
    
    public boolean isValidNames(Properties properties) {
        final Logger logger = Logger.getLogger(this.getClass().getName());
        final Field [] fields = type.getFields(); 
        for(Field field : fields) {
            try{
                if(!properties.containsKey(field.get(null))) {
                    return false;
                }
            }catch(IllegalArgumentException | IllegalAccessException e) {
                logger.log(Level.WARNING, "Error accesing value of field: " + type.getName() + ' ' + field.getName(), e);
                return false;
            }
        }
        return true;
    }
}
