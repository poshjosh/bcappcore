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

import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.sql.script.SqlScriptImporter;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 31, 2017 11:55:36 AM
 */
public class ExecuteSqlFromScriptFile<S> 
        implements BiFunction<PersistenceUnitContext, S, List<Integer>>, Serializable {

    private transient static final Logger logger = Logger.getLogger(ExecuteSqlFromScriptFile.class.getName());

    @Override
    public List<Integer> apply(PersistenceUnitContext puContext, S scriptFile) {
        
        
        final String persistenceUnit = puContext.getPersistenceUnitName();
        
        logger.info(() -> "For persistence unit: " + persistenceUnit + ", executing SQL file: " + scriptFile);
        
        Objects.requireNonNull(persistenceUnit);
        Objects.requireNonNull(scriptFile);
    
        final EntityManager em = puContext.getEntityManager();
        try{
            final List<Integer> output = new SqlScriptImporter("utf-8", Level.INFO)
                    .executeSqlScript(em, scriptFile);
            if(!output.isEmpty()) {
                try{
                    puContext.loadMetaData();
                }catch(SQLException e) {
                    throw new RuntimeException("Failed to execute SQL script: " + scriptFile, e);
                }
            }

            return output;
        }finally{
            if(em.isOpen()) {
                em.close();
            }
        }
    }
}
