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

package com.bc.appcore.jpa;

import com.bc.appcore.exceptions.UserRuntimeException;
import com.bc.jpa.context.PersistenceContext;
import com.bc.jpa.context.PersistenceContextEclipselinkOptimized;
import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.sync.predicates.PersistenceCommunicationsLinkFailureTest;
import com.bc.sql.MySQLDateTimePatterns;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 19, 2017 3:19:18 AM
 */
public class JpaContextManagerImpl implements JpaContextManager {

    private static final Logger logger = Logger.getLogger(JpaContextManagerImpl.class.getName());
    
    public JpaContextManagerImpl() {  }
    
    @Override
    public PersistenceContext createJpaContext(URI uri, int maxTrials, boolean freshInstall) 
            throws URISyntaxException {
        
        PersistenceContext jpaContext = null;
        
        int trials = 0;
        
        while(trials++ < maxTrials) {
            try{
                
                jpaContext = this.newJpaContext(uri);

                final Set<String> puNames = jpaContext.getMetaData(false).getPersistenceUnitNames();
                
                for(String puName : puNames) {
                    
                    PersistenceUnitContext puContext = jpaContext.getContext(puName);
                    
                    this.initJpaContext(puContext);
                }
                
                break;
                
            }catch(IOException | SQLException e) {
                
                if(new PersistenceCommunicationsLinkFailureTest().test(e)) {
                    throw new UserRuntimeException("Internet connection failed");
                }
                
                logger.log(Level.WARNING, "JpaContext Exception", e);
            }
        }
        
        Objects.requireNonNull(jpaContext, "JpaContext must not be NULL");
        
        return jpaContext;
    }
    
    @Override
    public PersistenceContext newJpaContext(URI uri) throws IOException, SQLException {
        return new PersistenceContextEclipselinkOptimized(
                        uri, new MySQLDateTimePatterns()
        );
    }
    
    public final void initJpaContext(PersistenceUnitContext puContext) 
            throws SQLException, IOException {

        logger.entering(this.getClass().getName(), "initJpaContext(PersistenceUnitContext)", puContext);

        if(this.isVirgin(puContext)) {

            this.initDatabaseData(puContext);
        }

        if(!puContext.isMetaDataLoaded()) {
            puContext.loadMetaData();
        }
        
        this.validate(puContext);
    }
    
    @Override
    public boolean isVirgin(PersistenceUnitContext puContext) throws SQLException {
            
        logger.entering(this.getClass().getName(), "isVirgin(PersistenceUnitContext)", puContext.getName());
        
        final List<String> existingListedTables = puContext.getMetaData(false)
                .fetchExistingListedTables(puContext.getPersistenceContext());
        
        final boolean virgin = existingListedTables.isEmpty();

        logger.fine(() -> "Virgin: "+virgin+", persistence unit; " + puContext.getPersistenceUnitName());
        
        return virgin;
    }
    
    @Override
    public void initDatabaseData(PersistenceUnitContext puContext) 
            throws IOException, SQLException { 
    }

    @Override
    public void validate(PersistenceUnitContext puContext) throws SQLException { 
        
        logger.entering(this.getClass().getName(), "validate(PersistenceUnitContext)", puContext.getName());
        
        if(!puContext.isMetaDataLoaded()) {
            puContext.loadMetaData();
        }
        
        final String persistenceUnit = puContext.getPersistenceUnitName();
        
        final Set<Class> classes = puContext.getMetaData().getEntityClasses();
        
        for(Class cls : classes) {
            
            final Number count = puContext.getDao().forSelect(Number.class)
                    .from(cls).count().getSingleResultAndClose();
            logger.finer(() -> "Count: " + count + ", persistence unit: " + 
                    persistenceUnit + ", entity type: " + cls.getName());
//            jpaContext.getDaoForSelect(cls).from(cls).createQuery()
//                    .setFirstResult(0).setMaxResults(1).getResultList();
        }
    }
}
