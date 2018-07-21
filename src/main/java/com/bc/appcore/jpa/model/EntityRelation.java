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

package com.bc.appcore.jpa.model;

import com.bc.appcore.util.Pair;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 18, 2017 9:36:28 PM
 */
public interface EntityRelation<T> {
    
    Logger logger = Logger.getLogger(EntityRelation.class.getName());
    
    default Object getTargetEntity(Object ref, String columnName) {

        final Pair<Class, Method> pair = this.getRelation(
                ref.getClass(), columnName, (Pair<Class, Method>)null);

        Objects.requireNonNull(pair, "Relation is null for: "+ref+"#"+columnName);

        final Class targetEntityType = pair.key;

        logger.finer(() -> "Column: "+columnName+", entity type: " + targetEntityType);
        Objects.requireNonNull(targetEntityType, "Unexpected column name: " + columnName);

        final Object target;
        
        if(targetEntityType == ref.getClass()) {
            target = ref;
        }else{

            final Method getter = pair.value;

            if(logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "Entity type: {0}, target type: {1}, method: {2}", 
                        new Object[]{ref.getClass().getName(), targetEntityType.getName(), getter==null?null:getter.getName()});
            }

            try{
                target = getter.invoke(ref);
            }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            if(logger.isLoggable(Level.FINER)) {
                logger.log(Level.FINER, "{0}#{1} with method {2} returned: {3}", 
                        new Object[]{ref, columnName, getter.getName(), target});
            }
        }
        
        return target;
    }

    /**
     * Consider that the following method signatures are valid:
     * <pre><code>
     * Doc Task#getDoc();
     * String Doc#getSubject();
     * </code></pre>
     * <p>
     *   Given Entity entityType of Task.class and column name of subject, then this
     *   method returns <code>Pair&lt;Doc, Method&gt;</code> where the Method is
     *   <code>Doc getDoc()</code>
     * </p>
     * @param refType
     * @param columnName
     * @param outputIfNone
     * @return
     */
    Pair<Class, Method> getRelation(Class refType, String columnName, Pair<Class, Method> outputIfNone);
}
